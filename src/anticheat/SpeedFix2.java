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
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import anticheat.util.BPSEvent;
import anticheat.util.DelayedTask;
import anticheat.util.EventUtil;
import anticheat.util.PlayerLeaveEvent;
import anticheat.util.TimeEvent;
import anticheat.util.Timer;

public class SpeedFix2 extends AntiCheatBase {
	private Map<String, List<Long>> violations = null;
	private Map<String, Integer> delay = null;
	private List<String> badBlockDelay = null;
	private String [] badBlocks = null;
	private long ticks = 0;
	
	public SpeedFix2() {
		super("Speed");
		violations = new HashMap<String, List<Long>>();
		delay = new HashMap<String, Integer>();
		badBlockDelay = new ArrayList<String>();
		badBlocks = new String [] {"STAIR", "SLAB", "ICE"};
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 1) {
			++this.ticks;
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
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDamage(EntityDamageEvent event) {
		if(!event.isCancelled() && event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			delay.put(player.getName(), 20);
		}
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
		Block below = to.getBlock().getRelative(0, -1, 0);
		if(to.getBlock().getType() == Material.SLIME_BLOCK || below.getType() == Material.SLIME_BLOCK) {
			delay.put(player.getName(), 5 * ((int) (player.getFallDistance())));
		}
	}
	
	@EventHandler
	public void onBPS(BPSEvent event) {
		Player player = event.getPlayer();
		final String name = player.getName();
		if(Timer.getPing(player) < getMaxPing()) {
			return;
		}
		if(!player.isFlying() && player.getVehicle() == null && !player.hasPotionEffect(PotionEffectType.SPEED)) {
			if(notIgnored(player) && !badBlockDelay.contains(name) && !badBlockDelay.contains(name) && !delay.containsKey(name) && player.getWalkSpeed() == 0.2f) {
				Location location = player.getLocation();
				for(int a = -2; a <= 0; ++a) {
					Block block = location.getBlock().getRelative(0, a, 0);
					for(String badBlock : badBlocks) {
						if(block.getType().toString().contains(badBlock)) {
							if(!badBlockDelay.contains(name)) {
								badBlockDelay.add(name);
								new DelayedTask(new Runnable() {
									@Override
									public void run() {
										badBlockDelay.remove(name);
									}
								}, 20 * 2);
							}
							return;
						}
					}
				}
				if(location.getBlock().getRelative(0, 2, 0).getType() != Material.AIR) {
					return;
				}
				double distance = event.getDistance();
				double max = 7.5;
				if(distance > max) {
					List<Long> violation = violations.get(name);
					if(violation == null) {
						violation = new ArrayList<Long>();
					}
					violation.add(ticks);
					violations.put(name, violation);
					int recent = 0;
					for(long ticks : violation) {
						if(this.ticks - ticks <= 120) {
							if(++recent >= 2) {
								ban(player);
								return;
							}
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		Player player = event.getPlayer();
		String name = player.getName();
		if(violations.containsKey(name)) {
			UUID uuid = player.getUniqueId();
			List<Long> loggings = violations.get(name);
			if(loggings != null) {
				int average = 0;
				for(long logging : loggings) {
					average += logging;
				}
				if(average > 0) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "antiCheat NETWORK_DISTANCE_LOGS " + uuid.toString() + " " + (average / loggings.size()));
				}
				violations.get(name).clear();
				loggings.clear();
				loggings = null;
			}
			violations.remove(name);
		}
	}
}
