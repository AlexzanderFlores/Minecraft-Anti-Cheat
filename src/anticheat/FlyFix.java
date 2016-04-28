package anticheat;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.potion.PotionEffectType;

import net.md_5.bungee.api.ChatColor;
import ostb.customevents.TimeEvent;
import ostb.server.PerformanceHandler;
import ostb.server.util.EventUtil;

public class FlyFix extends AntiCheatBase {
	private Map<String, Integer> delays = null;
	private Map<String, Integer> floating = null;
	private Map<String, Integer> flying = null;
	
	public FlyFix() {
		super("Fly");
		delays = new HashMap<String, Integer>();
		floating = new HashMap<String, Integer>();
		flying = new HashMap<String, Integer>();
		EventUtil.register(this);
	}
	
	private void delay(Player player, int ticks) {
		delays.put(player.getName(), ticks);
	}
	
	private boolean checkForFly(Player player) {
		if(PerformanceHandler.getPing(player) < getMaxPing() && player.getTicksLived() >= 20 * 3 && !player.isFlying() && player.getVehicle() == null) {
			if(notIgnored(player) && !delays.containsKey(player.getName()) && !player.hasPotionEffect(PotionEffectType.JUMP)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean onEdgeOfBlock(Player player) {
		for(int a = -2; a <= 0; ++a) {
			Block block = player.getLocation().getBlock().getRelative(0, a, 0);
			for(int x = -1; x <= 1; ++x) {
				for(int y = -1; y <= 1; ++y) {
					for(int z = -1; z <= 1; ++z) {
						if(block.getRelative(x, y, z).getType() != Material.AIR) {
							delay(player, 3);
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 1) {
			Iterator<String> iterator = delays.keySet().iterator();
			while(iterator.hasNext()) {
				String name = iterator.next();
				int counter = delays.get(name);
				if(--counter <= 0) {
					iterator.remove();
				} else {
					delays.put(name, counter);
				}
			}
		} else if(ticks == 20) {
			for(Player player : Bukkit.getOnlinePlayers()) {
				if(checkForFly(player) && !onEdgeOfBlock(player)) {
					int counter = 0;
					if(floating.containsKey(player.getName())) {
						counter = floating.get(player.getName());
					}
					if(++counter >= 2) {
						//player.kickPlayer("Floating too long (Send this to leet)");
						player.sendMessage(ChatColor.RED + "KICKED FOR FLOATING (TELL LEET THIS)");
					} else {
						floating.put(player.getName(), counter);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerVelocity(PlayerVelocityEvent event) {
		Player player = event.getPlayer();
		player.sendMessage("vel event (" + event.getVelocity().toString() + ")");
		//TODO: Add a delay based off of event.getVelocity()
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		Location to = event.getTo();
		Location from = event.getFrom();
		if(to.getY() >= from.getY() && checkForFly(player) && !onEdgeOfBlock(player)) {
			int counter = 0;
			if(flying.containsKey(player.getName())) {
				counter = flying.get(player.getName());
			}
			if(++counter >= 10) {
				//player.kickPlayer("Flying (Send this to leet)");
				player.sendMessage(ChatColor.RED + "KICKED FOR FLY (TELL LEET THIS)");
			} else {
				flying.put(player.getName(), counter);
			}
			player.sendMessage("Flying counter: " + counter + " (Tell leet this)");
			return;
		}
		floating.put(player.getName(), -1);
		if(flying.containsKey(player.getName())) {
			int counter = flying.get(player.getName()) - 1;
			if(counter <= 0) {
				flying.remove(player.getName());
			} else {
				flying.put(player.getName(), counter);
			}
		}
	}
}
