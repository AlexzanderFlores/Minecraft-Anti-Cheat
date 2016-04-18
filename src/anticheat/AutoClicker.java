package anticheat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import ostb.customevents.TimeEvent;
import ostb.customevents.player.AsyncPlayerLeaveEvent;
import ostb.server.DB;
import ostb.server.tasks.DelayedTask;
import ostb.server.util.EventUtil;

public class AutoClicker extends AntiCheatBase implements Listener {
	private Map<String, Integer> clicks = null;
	private Map<String, Integer> logs = null;
	private Map<String, List<Integer>> loggings = null;
	private List<String> delayed = null;
	private int delay = 1;
	
	public AutoClicker() {
		super("Auto Clicker");
		clicks = new HashMap<String, Integer>();
		logs = new HashMap<String, Integer>();
		loggings = new HashMap<String, List<Integer>>();
		delayed = new ArrayList<String>();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20) {
			if(isEnabled()) {
				clicks.clear();
			}
		} else if(ticks == 20 * 10) {
			if(isEnabled()) {
				logs.clear();
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(isEnabled() && event.getAction() != Action.PHYSICAL) {
			Player player = event.getPlayer();
			int click = 0;
			if(clicks.containsKey(player.getName())) {
				click = clicks.get(player.getName());
			}
			clicks.put(player.getName(), ++click);
			final String name = player.getName();
			if(click >= 20) {
				int cps = clicks.get(name);
				List<Integer> logging = loggings.get(name);
				if(logging == null) {
					logging = new ArrayList<Integer>();
				}
				logging.add(cps);
				loggings.put(name, logging);
			}
			if(click >= 30 && !delayed.contains(player.getName())) {
				delayed.add(name);
				new DelayedTask(new Runnable() {
					@Override
					public void run() {
						delayed.remove(name);
					}
				}, 20 * delay);
				int log = 0;
				if(logs.containsKey(name)) {
					log = logs.get(name);
				}
				if(++log >= 3) {
					event.setCancelled(true);
					//ban(player);
				} else {
					logs.put(name, log);
				}
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
				for(int cps : logging) {
					DB.NETWORK_CPS_LOGS.insert("'" + uuid.toString() + "', '" + cps + "'");
				}
				loggings.get(name).clear();
			}
			loggings.remove(name);
		}
	}
}
