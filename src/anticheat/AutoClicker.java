package anticheat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import anticheat.util.EventUtil;
import anticheat.util.PlayerLeaveEvent;
import anticheat.util.TimeEvent;

public class AutoClicker extends AntiCheatBase {
	private Map<String, Integer> clicks = null;
	private Map<String, List<Integer>> loggings = null;
	
	public AutoClicker() {
		super("Auto Clicker");
		clicks = new HashMap<String, Integer>();
		loggings = new HashMap<String, List<Integer>>();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20) {
			if(isEnabled()) {
				clicks.clear();
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Action action = event.getAction();
		if(isEnabled() && (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK)) {
			Player player = event.getPlayer();
			final String name = player.getName();
			Bukkit.getLogger().info("ANTI CHEAT: " + name + " is left clicking");
			int click = 0;
			if(clicks.containsKey(name)) {
				click = clicks.get(name);
			}
			clicks.put(name, ++click);
			if(click >= 20) {
				int cps = clicks.get(name);
				List<Integer> logging = loggings.get(name);
				if(logging == null) {
					logging = new ArrayList<Integer>();
				}
				logging.add(cps);
				loggings.put(name, logging);
				Bukkit.getLogger().info("ANTI CHEAT: cancelling click for " + name);
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		Player player = event.getPlayer();
		String name = player.getName();
		if(loggings.containsKey(name)) {
			UUID uuid = player.getUniqueId();
			List<Integer> logging = loggings.get(name);
			if(logging != null) {
				int average = 0;
				for(int cps : logging) {
					average += cps;
				}
				if(average > 0) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "antiCheat NETWORK_CPS_LOGS " + uuid.toString() + " " + (average / logging.size()));
				}
				loggings.get(name).clear();
				logging.clear();
				logging = null;
			}
			loggings.remove(name);
		}
	}
}
