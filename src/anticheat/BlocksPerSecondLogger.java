package anticheat;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import anticheat.events.BPSEvent;
import anticheat.events.PlayerLeaveEvent;
import anticheat.events.TimeEvent;
import anticheat.util.EventUtil;

public class BlocksPerSecondLogger implements Listener {
	private Map<String, Location> lastLocations = null;
	
	public BlocksPerSecondLogger() {
		lastLocations = new HashMap<String, Location>();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20) {
			for(Player player : Bukkit.getOnlinePlayers()) {
				if(player.getTicksLived() >= 20 * 3) {
					String name = player.getName();
					Location pLoc = player.getLocation();
					Location lLoc = pLoc;
					if(lastLocations.containsKey(name)) {
						lLoc = lastLocations.get(name);
					}
					lastLocations.put(name, pLoc);
					if(pLoc.getWorld().getName().equals(lLoc.getWorld().getName()) && pLoc.getY() >= lLoc.getY()) {
						double distance = pLoc.distance(lLoc);
						if(distance > 0) {
							Bukkit.getPluginManager().callEvent(new BPSEvent(player, distance));
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		lastLocations.remove(event.getPlayer().getName());
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		lastLocations.remove(event.getPlayer().getName());
	}
}
