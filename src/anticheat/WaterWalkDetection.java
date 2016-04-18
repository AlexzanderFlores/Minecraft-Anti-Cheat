package anticheat;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import ostb.customevents.TimeEvent;
import ostb.server.util.EventUtil;

public class WaterWalkDetection extends AntiCheatBase implements Listener {
	private Map<String, Integer> counters = null;
	
	public WaterWalkDetection() {
		super("Water Walking");
		counters = new HashMap<String, Integer>();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20) {
			if(isEnabled()) {
				counters.clear();
			}
		}
	}
	
	//TODO: Add the functionality for this event just to this class to help detect water walking
	/*@EventHandler
	public void onWaterSplash(WaterSplashEvent event) {
		if(isEnabled()) {
			Player player = event.getPlayer();
			int counter = 0;
			if(counters.containsKey(player.getName())) {
				counter = counters.get(player.getName());
			}
			if(++counter >= 5) {
				ban(player);
			}
			counters.put(player.getName(), counter);
		}
	}*/
}
