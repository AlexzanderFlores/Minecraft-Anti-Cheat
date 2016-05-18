package anticheat.detections.movement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import anticheat.AntiCheatBase;
import anticheat.events.PlayerLeaveEvent;
import anticheat.events.TimeEvent;
import anticheat.util.AsyncDelayedTask;
import anticheat.util.DB;
import anticheat.util.EventUtil;

public class WaterWalkDetection extends AntiCheatBase {
	private Map<String, Integer> counters = null;
	private Map<String, Integer> violations = null;
	private List<String> reported = null;
	
	public WaterWalkDetection() {
		super("WaterWalking");
		counters = new HashMap<String, Integer>();
		violations = new HashMap<String, Integer>();
		reported = new ArrayList<String>();
		EventUtil.register(this);
	}
	
	private boolean isWater(Material type) {
		return type == Material.WATER || type == Material.STATIONARY_WATER;
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20 && isEnabled()) {
			counters.clear();
			violations.clear();
		}
	}
	
	@EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
		if(isEnabled()) {
			Player player = event.getPlayer();
			// Check type one
			Material material = event.getTo().getBlock().getType();
	        if(material == Material.STATIONARY_WATER && event.getFrom().getBlock().getType() == Material.AIR && event.getPlayer().getVelocity().getY() < -0.40d) {
				int counter = 0;
				if(counters.containsKey(player.getName())) {
					counter = counters.get(player.getName());
				}
				counters.put(player.getName(), ++counter);
				if(counter >= 5) {
					ban(player);
				}
	        }
	        
	        // Check type two
	        double y = player.getLocation().getY();
	        Block block = player.getLocation().getBlock().getRelative(0, -1, 0);
	        Material type = block.getType();
			if(y % 1 == 0 && isWater(type)) {
				for(int x = -1; x <= 1; ++x) {
					for(int z = -1; z <= 1; ++z) {
						if(!isWater(block.getRelative(x, 0, z).getType())) {
							return;
						}
					}
				}
				int violation = 0;
				if(violations.containsKey(player.getName())) {
					violation = violations.get(player.getName());
				}
				violations.put(player.getName(), ++violation);
				if(violation >= 5) {
					if(!reported.contains(player.getName())) {
						reported.add(player.getName());
						final UUID uuid = player.getUniqueId();
						new AsyncDelayedTask(new Runnable() {
							@Override
							public void run() {
								DB.NETWORK_WATER_WALK_TEST.insert("'" + uuid.toString() + "'");
							}
						});
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		if(isEnabled()) {
			counters.remove(event.getPlayer().getName());
			reported.remove(event.getPlayer().getName());
		}
	}
}
