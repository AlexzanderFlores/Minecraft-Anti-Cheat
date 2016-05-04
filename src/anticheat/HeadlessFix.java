package anticheat;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import anticheat.events.PlayerLeaveEvent;
import anticheat.util.EventUtil;

public class HeadlessFix extends AntiCheatBase {
	private Map<String, Integer> counters = null;
	
	public HeadlessFix() {
		super("Headless");
		counters = new HashMap<String, Integer>();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		float pitch = event.getTo().getPitch();
		if(isEnabled() && (pitch == 180.0f || pitch == -180.0f)) {
			String name = event.getPlayer().getName();
			int counter = 0;
			if(counters.containsKey(name)) {
				counter = counters.get(name);
			}
			counters.put(name, ++counter);
			if(counter >= 3) {
				ban(event.getPlayer());
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		if(isEnabled()) {
			counters.remove(event.getPlayer().getName());
		}
	}
}
