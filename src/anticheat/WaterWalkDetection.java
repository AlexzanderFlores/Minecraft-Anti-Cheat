package anticheat;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import ostb.customevents.TimeEvent;
import ostb.customevents.player.PlayerLeaveEvent;
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
	
	@EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
		if(isEnabled()) {
			Material material = event.getTo().getBlock().getType();
	        if(material == Material.STATIONARY_WATER && event.getFrom().getBlock().getType() == Material.AIR && event.getPlayer().getVelocity().getY() < -0.40d) {
	        	Player player = event.getPlayer();
	        	Bukkit.getLogger().info("ANTI CHEAT: " + player.getName() + " has moved into water");
				int counter = 0;
				if(counters.containsKey(player.getName())) {
					counter = counters.get(player.getName());
				}
				counters.put(player.getName(), ++counter);
				if(counter >= 5) {
					ban(player);
				}
	        }
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		counters.remove(event.getPlayer().getName());
	}
}
