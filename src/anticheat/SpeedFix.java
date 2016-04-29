package anticheat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.potion.PotionEffectType;

import anticheat.util.EventUtil;
import anticheat.util.PlayerLeaveEvent;
import anticheat.util.TimeEvent;
import anticheat.util.Timer;

public class SpeedFix extends AntiCheatBase {
	private Map<String, Location> lastLocations = null;
	private Map<String, Integer> counters = null;
	private Map<String, Integer> disabledCounters = null;
	private Map<String, List<Double>> loggings = null;
	private static Map<String, Double> speeds = null;
	
	public SpeedFix() {
		super("Speed");
		lastLocations = new HashMap<String, Location>();
		counters = new HashMap<String, Integer>();
		disabledCounters = new HashMap<String, Integer>();
		loggings = new HashMap<String, List<Double>>();
		speeds = new HashMap<String, Double>();
		EventUtil.register(this);
	}
	
	private void disable(Player player, int seconds) {
		disabledCounters.put(player.getName(), seconds);
	}
	
	public static double getSpeed(Player player) {
		return speeds == null || !speeds.containsKey(player.getName()) ? 0.0d : speeds.get(player.getName());
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20 && isEnabled()) {
			Iterator<String> iterator = disabledCounters.keySet().iterator();
			while(iterator.hasNext()) {
				String name = iterator.next();
				int counter = disabledCounters.get(name);
				if(--counter <= 0) {
					iterator.remove();
				} else {
					disabledCounters.put(name, counter);
				}
			}
			for(Player player : Bukkit.getOnlinePlayers()) {
				if(Timer.getPing(player) < getMaxPing()) {
					Location from = player.getLocation();
					if(lastLocations.containsKey(player.getName())) {
						from = lastLocations.get(player.getName());
					}
					Location to = player.getLocation();
					double distance = to.distance(from);
					speeds.put(player.getName(), distance);
					if(!player.getAllowFlight() && !player.isFlying() && player.getVehicle() == null && to.getWorld().getName().equals(from.getWorld().getName()) && !player.hasPotionEffect(PotionEffectType.SPEED)) {
						if(to.getY() >= from.getY() && !disabledCounters.containsKey(player.getName()) && notIgnored(player) && (to.getX() != from.getX() || to.getY() != from.getY() || to.getZ() != from.getZ())) {
							if(player.getWalkSpeed() == 0.2f) {
								if(distance >= 5.0d && player.isSneaking()) {
									player.setSneaking(false);
								}
								boolean somethingAbove = player.getLocation().add(0, 2, 0).getBlock().getType() != Material.AIR;
								if(!somethingAbove && distance >= 8.5) {
									List<Double> logging = loggings.get(player.getName());
									if(logging == null) {
										logging = new ArrayList<Double>();
									}
									logging.add(distance);
									loggings.put(player.getName(), logging);
									int counter = 0;
									if(counters.containsKey(player.getName())) {
										counter = counters.get(player.getName());
									}
									if(++counter >= 3) {
										ban(player);
									} else {
										counters.put(player.getName(), counter);
									}
								}
							}
						}
					}
					lastLocations.put(player.getName(), to);
				}
			}
		} else if(ticks == 20 * 5 && isEnabled()) {
			counters.clear();
		}
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if(isEnabled() && event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			disable(player, 5);
		}
	}
	
	@EventHandler
	public void onPlayerVelocity(PlayerVelocityEvent event) {
		if(isEnabled()) {
			disable(event.getPlayer(), 5);
		}
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if(isEnabled()) {
			lastLocations.remove(event.getPlayer().getName());
			counters.remove(event.getPlayer().getName());
			disable(event.getPlayer(), 5);
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if(isEnabled()) {
			Location to = event.getTo();
			String below = to.getBlock().getRelative(0, -1, 0).getType().toString();
			double toY = to.getY();
			if(below.contains("ICE") || below.contains("PACKED_ICE") || below.contains("STAIR") || below.contains("SLAB") || (!(toY == Math.floor(toY) && !Double.isInfinite(toY)) && toY - (int) toY == .5)) {
				lastLocations.remove(event.getPlayer().getName());
				counters.remove(event.getPlayer().getName());
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		if(isEnabled()) {
			Player player = event.getPlayer();
			String name = player.getName();
			speeds.remove(name);
			if(loggings.containsKey(name)) {
				UUID uuid = player.getUniqueId();
				List<Double> logging = loggings.get(name);
				if(logging != null) {
					for(double dis : logging) {
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "antiCheat NETWORK_DISTANCE_LOGS " + uuid.toString() + " " + dis);
					}
					loggings.get(name).clear();
				}
				loggings.remove(name);
			}
		}
	}
}
