package anticheat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.util.Vector;

import ostb.customevents.TimeEvent;
import ostb.customevents.player.AsyncPlayerLeaveEvent;
import ostb.server.DB;
import ostb.server.PerformanceHandler;
import ostb.server.util.EventUtil;


public class FastBowFix extends AntiCheatBase implements Listener {
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
			if(PerformanceHandler.getPing(player) < getMaxPing()) {
				Vector velocity = event.getProjectile().getVelocity();
				double x = velocity.getX() < 0 ? velocity.getX() * -1 : velocity.getX();
				double z = velocity.getZ() < 0 ? velocity.getZ() * -1 : velocity.getZ();
				if((x + z) >= 2.75 && notIgnored(player)) {
					int times = 0;
					if(timesFired.containsKey(player.getName())) {
						times = timesFired.get(player.getName());
					}
					if(++times >= 2) {
						if(times >= 13) {
							List<Integer> logging = loggings.get(player.getName());
							if(logging == null) {
								logging = new ArrayList<Integer>();
							}
							logging.add(times);
							loggings.put(player.getName(), logging);
						}
						event.setCancelled(true);
					}
					timesFired.put(player.getName(), times);
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
				for(int shots : logging) {
					DB.NETWORK_POWER_BOW_LOGS.insert("'" + uuid.toString() + "', '" + shots + "'");
				}
				loggings.get(name).clear();
			}
			loggings.remove(name);
		}
	}
}
