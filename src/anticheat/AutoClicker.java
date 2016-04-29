package anticheat;

import java.util.HashMap;
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
	private Map<String, Integer> loggings = null;
	
	public AutoClicker() {
		super("AutoClicker");
		clicks = new HashMap<String, Integer>();
		loggings = new HashMap<String, Integer>();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20 && isEnabled()) {
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
				int logging = 1;
				if(loggings.containsKey(player.getName())) {
					logging += loggings.get(player.getName());
				}
				loggings.put(player.getName(), logging);
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		if(isEnabled()) {
			Player player = event.getPlayer();
			String name = player.getName();
			if(loggings.containsKey(name)) {
				UUID uuid = player.getUniqueId();
				int timesLogged = loggings.get(name);
				if(timesLogged > 0) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "antiCheat NETWORK_CPS_LOGS " + uuid.toString() + " " + timesLogged);
				}
				loggings.remove(name);
			}
		}
	}
}
