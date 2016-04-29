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
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import anticheat.util.EventUtil;
import anticheat.util.TimeEvent;
import anticheat.util.Timer;
import net.md_5.bungee.api.ChatColor;

public class FlyFix extends AntiCheatBase {
	private Map<String, Integer> delay = null;
	private Map<String, Integer> floating = null;
	private Map<String, Integer> flying = null;
	
	public FlyFix() {
		super("Fly");
		delay = new HashMap<String, Integer>();
		floating = new HashMap<String, Integer>();
		flying = new HashMap<String, Integer>();
		EventUtil.register(this);
	}
	
	private boolean checkForFly(Player player) {
		if(Timer.getPing(player) < getMaxPing() && player.getTicksLived() >= 20 * 3 && !player.isFlying() && player.getVehicle() == null) {
			if(notIgnored(player) && !player.hasPotionEffect(PotionEffectType.JUMP) && player.getWalkSpeed() == 0.2f) {
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
							delay.put(player.getName(), 30);
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
			Iterator<String> iterator = delay.keySet().iterator();
			while(iterator.hasNext()) {
				String name = iterator.next();
				int counter = delay.get(name);
				if(--counter <= 0) {
					iterator.remove();
				} else {
					delay.put(name, counter);
				}
			}
		} else if(ticks == 20) {
			for(Player player : Bukkit.getOnlinePlayers()) {
				if(!delay.containsKey(player.getName()) && checkForFly(player) && !onEdgeOfBlock(player)) {
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
				} else {
					floating.put(player.getName(), -1);
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerVelocity(PlayerVelocityEvent event) {
		Player player = event.getPlayer();
		Vector vel = player.getVelocity();
		double x = vel.getX() < 0 ? vel.getX() * -1 : vel.getX();
		double y = vel.getY() < 0 ? vel.getY() * -1 : vel.getY();
		double z = vel.getZ() < 0 ? vel.getZ() * -1 : vel.getZ();
		double value = x + y + z;
		delay.put(player.getName(), ((int) value * 5));
	}
	
	@EventHandler
	public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
		Player player = event.getPlayer();
		if(!(player.getAllowFlight() && player.isFlying())) {
			delay.put(player.getName(), 20);
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		Location to = event.getTo();
		Location from = event.getFrom();
		Block below = to.getBlock().getRelative(0, -1, 0);
		if(to.getBlock().getType() == Material.SLIME_BLOCK || below.getType() == Material.SLIME_BLOCK) {
			delay.put(player.getName(), 5 * ((int) (player.getFallDistance())));
			return;
		}
		if(!delay.containsKey(player.getName()) && to.getY() >= from.getY() && checkForFly(player) && !onEdgeOfBlock(player)) {
			int counter = 0;
			if(flying.containsKey(player.getName())) {
				counter = flying.get(player.getName());
			}
			if(++counter >= 10) {
				ban(player);
			} else {
				flying.put(player.getName(), counter);
			}
			return;
		}
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
