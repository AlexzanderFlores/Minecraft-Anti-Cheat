package anticheat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
import org.bukkit.util.Vector;

import anticheat.events.TimeEvent;
import anticheat.util.AsyncDelayedTask;
import anticheat.util.EventUtil;
import anticheat.util.Timer;
import net.md_5.bungee.api.ChatColor;

public class FlyFix extends AntiCheatBase {
	private Map<String, Integer> delay = null;
	private Map<String, Integer> floating = null;
	private Map<String, Integer> flying = null;
	private List<Material> ignores = null;
	
	public FlyFix() {
		super("Fly");
		delay = new HashMap<String, Integer>();
		floating = new HashMap<String, Integer>();
		flying = new HashMap<String, Integer>();
		ignores = new ArrayList<Material>();
		ignores.add(Material.LADDER);
		ignores.add(Material.WATER);
		ignores.add(Material.STATIONARY_WATER);
		ignores.add(Material.LAVA);
		ignores.add(Material.STATIONARY_LAVA);
		EventUtil.register(this);
	}
	
	private boolean checkForFly(Player player) {
		if(Timer.getPing(player) < getMaxPing() && player.getTicksLived() >= 20 * 3 && !player.isFlying() && player.getVehicle() == null) {
			if(notIgnored(player) && player.getWalkSpeed() == 0.2f) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isOnIgnored(Player player) {
		if(ignores.contains(player.getLocation().getBlock().getType())) {
			return true;
		}
		return false;
	}
	
	private boolean onEdgeOfBlock(Player player, boolean checkBelow) {
		if(isOnIgnored(player)) {
			return true;
		}
		for(int a = checkBelow ? -2 : 0; a <= 0; ++a) {
			Block block = player.getLocation().getBlock().getRelative(0, a, 0);
			for(int x = -1; x <= 1; ++x) {
				//for(int y = 0; y <= 1; ++y) {
					for(int z = -1; z <= 1; ++z) {
						if(block.getRelative(x, 0, z).getType() != Material.AIR) {
							delay.put(player.getName(), 30);
							return true;
						}
					}
				//}
			}
		}
		return false;
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 1 && isEnabled()) {
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
		} else if(ticks == 20 && isEnabled()) {
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					for(Player player : Bukkit.getOnlinePlayers()) {
						if(!delay.containsKey(player.getName()) && checkForFly(player) && !onEdgeOfBlock(player, true)) {
							int counter = 0;
							if(floating.containsKey(player.getName())) {
								counter = floating.get(player.getName());
							}
							if(++counter >= 2) {
								//player.kickPlayer("Floating too long (Send this to leet)");
								player.sendMessage(ChatColor.DARK_RED + "KICKED FOR FLOATING (TELL LEET THIS ASAP)");
							} else {
								floating.put(player.getName(), counter);
							}
						} else {
							floating.put(player.getName(), -1);
						}
					}
				}
			});
		}
	}
	
	@EventHandler
	public void onPlayerVelocity(PlayerVelocityEvent event) {
		if(isEnabled()) {
			Player player = event.getPlayer();
			Vector vel = player.getVelocity();
			double x = vel.getX() < 0 ? vel.getX() * -1 : vel.getX();
			double y = vel.getY() < 0 ? vel.getY() * -1 : vel.getY();
			double z = vel.getZ() < 0 ? vel.getZ() * -1 : vel.getZ();
			double value = x + y + z;
			delay.put(player.getName(), ((int) value * 5));
		}
	}
	
	@EventHandler
	public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
		if(isEnabled()) {
			Player player = event.getPlayer();
			if(!(player.getAllowFlight() && player.isFlying())) {
				delay.put(player.getName(), 20);
			}
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if(isEnabled()) {
			Player player = event.getPlayer();
			if(delay.containsKey(player.getName()) || player.isFlying() || !notIgnored(player)) {
				return;
			}
			double y = player.getVelocity().getY();
			if(y % 1 != 0) {
				String vel = y + "";
				vel = vel.substring(3);
				if(vel.startsWith("199999")) {
					delay.put(player.getName(), 12);
					return;
				}
			}
			y = player.getLocation().getY();
			if(y % 1 == 0 || y % .5 == 0) {
				return;
			}
			Location to = event.getTo();
			Location from = event.getFrom();
			Block below = to.getBlock().getRelative(0, -1, 0);
			if(to.getBlock().getType() == Material.SLIME_BLOCK || below.getType() == Material.SLIME_BLOCK) {
				delay.put(player.getName(), 5 * ((int) (player.getFallDistance())));
				return;
			}
			if(!delay.containsKey(player.getName()) && to.getY() >= from.getY() && checkForFly(player) && !onEdgeOfBlock(player, false)) {
				int counter = 0;
				if(flying.containsKey(player.getName())) {
					counter = flying.get(player.getName());
				}
				if(++counter >= 1) {
					player.sendMessage(ChatColor.DARK_RED + "KICKED FOR FLYING (TELL LEET THIS ASAP)");
					//ban(player);
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
}
