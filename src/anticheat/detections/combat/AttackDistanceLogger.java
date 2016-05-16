package anticheat.detections.combat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import anticheat.AntiCheatBase;
import anticheat.events.AsyncPlayerLeaveEvent;
import anticheat.util.DB;
import anticheat.util.EventUtil;

public class AttackDistanceLogger extends AntiCheatBase {
	private Map<String, List<Double>> loggings = null;
	
	public AttackDistanceLogger() {
		super("Reach");
		loggings = new HashMap<String, List<Double>>();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(isEnabled() && event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
			Player player = (Player) event.getEntity();
			Player damager = (Player) event.getDamager();
			Location playerLocation = player.getLocation();
			Location damagerLocation = damager.getLocation();
			double distance = playerLocation.distance(damagerLocation);
			if(distance > 4.5d) {
				List<Double> logging = loggings.get(damager.getName());
				if(logging == null) {
					logging = new ArrayList<Double>();
				}
				logging.add(distance);
				loggings.put(damager.getName(), logging);
			}
		}
	}
	
	@EventHandler
	public void onAsyncPlayerLeave(AsyncPlayerLeaveEvent event) {
		if(isEnabled()) {
			String name = event.getName();
			if(loggings.containsKey(name)) {
				UUID uuid = event.getUUID();
				List<Double> logging = loggings.get(name);
				if(logging != null) {
					double average = 0.0d;
					for(double distance : logging) {
						average += distance;
					}
					if(average > 0) {
						DB.NETWORK_ATTACK_DISTANCE_LOGS.insert("'" + uuid.toString() + "', '" + (average / logging.size()) + "'");
					}
					loggings.get(name).clear();
					logging.clear();
					logging = null;
				}
				loggings.remove(name);
			}
		}
	}
}
