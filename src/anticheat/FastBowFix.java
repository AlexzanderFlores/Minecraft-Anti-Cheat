package anticheat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.util.Vector;

import anticheat.util.AsyncPlayerLeaveEvent;
import anticheat.util.DB;
import anticheat.util.EventUtil;
import anticheat.util.TimeEvent;
import anticheat.util.Timer;

public class FastBowFix extends AntiCheatBase {
	private Map<String, Integer> timesFired = null;
	private Map<String, List<Integer>> loggings = null;
	
	public FastBowFix() {
		super("Fast Bow");
		timesFired = new HashMap<String, Integer>();
		loggings = new HashMap<String, List<Integer>>();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20) {
			if(isEnabled()) {
				timesFired.clear();
			}
		}
	}
	
	@EventHandler
	public void onEntityShootBow(EntityShootBowEvent event) {
		if(isEnabled() && event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if(Timer.getPing(player) < getMaxPing()) {
				Vector velocity = event.getProjectile().getVelocity();
				double x = velocity.getX() < 0 ? velocity.getX() * -1 : velocity.getX();
				double z = velocity.getZ() < 0 ? velocity.getZ() * -1 : velocity.getZ();
				if((x + z) >= 2.75 && notIgnored(player)) {
					int times = 0;
					if(timesFired.containsKey(player.getName())) {
						times = timesFired.get(player.getName());
					}
					timesFired.put(player.getName(), ++times);
					if(times >= 2) {
						if(times >= 10) {
							List<Integer> logging = loggings.get(player.getName());
							if(logging == null) {
								logging = new ArrayList<Integer>();
							}
							logging.add(times);
							loggings.put(player.getName(), logging);
						}
						event.setCancelled(true);
					}
				}
			} else {
				timesFired.remove(player.getName());
			}
		}
	}
	
	@EventHandler
	public void onAsyncPlayerLeave(AsyncPlayerLeaveEvent event) {
		String name = event.getName();
		UUID uuid = event.getUUID();
		if(loggings.containsKey(name)) {
			List<Integer> logging = loggings.get(name);
			if(logging != null) {
				int average = 0;
				for(int shots : logging) {
					average += shots;
				}
				if(average > 0) {
					DB.NETWORK_POWER_BOW_LOGS.insert("'" + uuid.toString() + "', '" + (average / loggings.size()) + "'");
				}
				loggings.get(name).clear();
				logging.clear();
				logging = null;
			}
			loggings.remove(name);
		}
	}
}
