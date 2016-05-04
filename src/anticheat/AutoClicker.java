package anticheat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import anticheat.events.AsyncPlayerLeaveEvent;
import anticheat.events.CPSEvent;
import anticheat.events.TimeEvent;
import anticheat.util.DB;
import anticheat.util.EventUtil;

public class AutoClicker extends AntiCheatBase {
	private Map<String, Integer> clicks = null;
	private Map<String, Integer> fastClicks = null;
	private Map<String, Integer> seriousViolations = null;
	
	public AutoClicker() {
		super("AutoClicker");
		clicks = new HashMap<String, Integer>();
		fastClicks = new HashMap<String, Integer>();
		seriousViolations = new HashMap<String, Integer>();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20 && isEnabled()) {
			for(String name : clicks.keySet()) {
				int clickCount = clicks.get(name);
				if(clickCount > 0) {
					Bukkit.getPluginManager().callEvent(new CPSEvent(name, clickCount));
				}
			}
			clicks.clear();
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Action action = event.getAction();
		if(isEnabled() && (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK)) {
			Player player = event.getPlayer();
			final String name = player.getName();
			int click = 0;
			if(clicks.containsKey(name)) {
				click = clicks.get(name);
			}
			clicks.put(name, ++click);
			if(click >= 20) {
				int clicksLogged = 1;
				if(fastClicks.containsKey(player.getName())) {
					clicksLogged += fastClicks.get(player.getName());
				}
				fastClicks.put(player.getName(), clicksLogged);
				if(click >= 35) {
					clicksLogged = 0;
					if(seriousViolations.containsKey(name)) {
						click = seriousViolations.get(name);
					}
					seriousViolations.put(name, ++clicksLogged);
					if(clicksLogged >= 3) {
						ban(player);
					}
				}
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onAsyncPlayerLeave(AsyncPlayerLeaveEvent event) {
		if(isEnabled()) {
			String name = event.getName();
			if(fastClicks.containsKey(name)) {
				UUID uuid = event.getUUID();
				int fastClickCount = fastClicks.get(name);
				if(fastClickCount > 0) {
					DB.NETWORK_CPS_LOGS.insert("'" + uuid.toString() + "', '" + fastClickCount + "'");
				}
				fastClicks.remove(name);
			}
			seriousViolations.remove(name);
		}
	}
}
