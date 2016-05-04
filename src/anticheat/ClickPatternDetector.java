package anticheat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;

import anticheat.events.CPSEvent;
import anticheat.events.PlayerLeaveEvent;
import anticheat.util.EventUtil;

public class ClickPatternDetector extends AntiCheatBase {
	private Map<String, Map<Integer, Integer>> patternLogs = null;
	private List<String> resetOnce = null;
	
	public ClickPatternDetector() {
		super("ClickPattern");
		patternLogs = new HashMap<String, Map<Integer, Integer>>();
		resetOnce = new ArrayList<String>();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onCPS(CPSEvent event) {
		String name = event.getName();
		if(resetOnce.contains(name)) {
			int cps = event.getCPS();
			Map<Integer, Integer> logs = patternLogs.get(name);
			if(logs == null) {
				logs = new HashMap<Integer, Integer>();
			}
			if(logs.containsKey(cps)) {
				int counter = logs.get(cps);
				logs.put(cps, ++counter);
				int required = 60;
				if(counter >= required) {
					Bukkit.getLogger().info("ANTI CHEAT: Click pattern detected" + name + " has had a CPS of " + cps + " for " + required + "+ seconds of clicking without any change");
				}
			} else {
				if(!logs.isEmpty()) {
					resetOnce.add(name);
				}
				logs.clear();
				logs.put(cps, 1);
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		String name = event.getPlayer().getName();
		resetOnce.remove(name);
		if(patternLogs.containsKey(name)) {
			patternLogs.get(name).clear();
			patternLogs.remove(name);
		}
	}
}
