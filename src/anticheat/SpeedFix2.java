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
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffectType;

import anticheat.events.BPSEvent;
import ostb.customevents.TimeEvent;
import ostb.server.tasks.DelayedTask;
import ostb.server.util.EventUtil;

public class SpeedFix2 extends AntiCheatBase {
	private Map<String, Integer> disabled = null;
	private Map<String, List<Long>> violations = null;
	private Map<String, Integer> damageDelays = null;
	private List<String> badBlockDelay = null;
	private String [] badBlocks = null;
	private long ticks = 0;
	
	public SpeedFix2() {
		super("Speed");
		disabled = new HashMap<String, Integer>();
		violations = new HashMap<String, List<Long>>();
		damageDelays = new HashMap<String, Integer>();
		badBlockDelay = new ArrayList<String>();
		badBlocks = new String [] {"STAIR", "SLAB", "ICE"};
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 1) {
			++this.ticks;
			Iterator<String> iterator = damageDelays.keySet().iterator();
			while(iterator.hasNext()) {
				String name = iterator.next();
				int counter = damageDelays.get(name);
				if(--counter <= 0) {
					iterator.remove();
				} else {
					damageDelays.put(name, counter);
				}
			}
		}
		if(ticks == 20) {
			Iterator<String> iterator = disabled.keySet().iterator();
			while(iterator.hasNext()) {
				String name = iterator.next();
				int counter = disabled.get(name);
				if(--counter <= 0) {
					iterator.remove();
				} else {
					disabled.put(name, counter);
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDamage(EntityDamageEvent event) {
		if(!event.isCancelled() && event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			damageDelays.put(player.getName(), 20);
		}
	}
	
	@EventHandler
	public void onBPS(BPSEvent event) {
		Player player = event.getPlayer();
		final String name = player.getName();
		if(!player.getAllowFlight() && player.getVehicle() == null && !player.hasPotionEffect(PotionEffectType.SPEED) && !disabled.containsKey(name) && notIgnored(player) && !badBlockDelay.contains(name)) {
			if(!badBlockDelay.contains(name) && !damageDelays.containsKey(name) /*&& player.getWalkSpeed() == 0.2f && player.getFlySpeed() == 0.1f*/) {
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
							Bukkit.broadcastMessage("Ignoring " + name + " (On bad block)");
							return;
						}
					}
				}
				if(location.getBlock().getRelative(0, 2, 0).getType() != Material.AIR) {
					Bukkit.broadcastMessage("Ignoring " + name + " (Block above)");
					return;
				}
				double distance = event.getDistance();
				boolean sprinting = player.isSprinting();
				double max = 0;
				if(sprinting) {
					max = 7.5;
				} else {
					max= 4.5;
				}
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
								Bukkit.broadcastMessage("ANTI CHEAT: " + name + " BANNED");
								return;
							}
						}
					}
					Bukkit.broadcastMessage("ANTI CHEAT: " + name + " is moving too fast (" + ((int) distance) + ") VL " + violation.size() + " VLR " + recent);
				}
			}
		}
	}
}
