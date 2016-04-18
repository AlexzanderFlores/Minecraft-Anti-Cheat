package anticheat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import ostb.OSTB;
import ostb.customevents.TimeEvent;
import ostb.customevents.player.AsyncPlayerLeaveEvent;
import ostb.server.DB;
import ostb.server.util.EventUtil;

public class BlocksPerSecondLogger implements Listener {
	private Map<String, Location> lastLocations = null;
	private Map<String, List<Double>> loggings = null;
	
	public BlocksPerSecondLogger() {
		lastLocations = new HashMap<String, Location>();
		loggings = new HashMap<String, List<Double>>();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20) {
			for(Player player : Bukkit.getOnlinePlayers()) {
				String name = player.getName();
				Location pLoc = player.getLocation();
				Location lLoc = pLoc;
				if(lastLocations.containsKey(name)) {
					lLoc = lastLocations.get(name);
				}
				lastLocations.put(name, pLoc);
				double distance = pLoc.distance(lLoc);
				List<Double> logging = loggings.get(name);
				if(logging == null) {
					logging = new ArrayList<Double>();
				}
				logging.add(distance);
				loggings.put(name, logging);
				Bukkit.getLogger().info("ANTI CHEAT: " + name + " distance: " + distance);
			}
		}
	}
	
	@EventHandler
	public void onAsyncPlayerLeave(AsyncPlayerLeaveEvent event) {
		String name = event.getName();
		if(loggings.containsKey(name)) {
			UUID uuid = event.getUUID();
			List<Double> logging = loggings.get(name);
			if(logging != null) {
				double average = 0;
				for(double distance : logging) {
					average += distance;
				}
				if(average > 0) {
					DB.NETWORK_DISTANCE_LOGS.insert("'" + uuid.toString() + "', '" + average + "', '" + OSTB.getServerName() + "'");
				}
				loggings.get(name).clear();
				logging.clear();
				logging = null;
			}
			loggings.remove(name);
		}
	}
}
