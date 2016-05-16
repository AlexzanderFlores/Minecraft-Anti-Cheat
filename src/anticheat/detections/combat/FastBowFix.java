package anticheat.detections.combat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;

import anticheat.AntiCheatBase;
import anticheat.events.AsyncPlayerLeaveEvent;
import anticheat.events.TimeEvent;
import anticheat.util.DB;
import anticheat.util.EventUtil;

public class FastBowFix extends AntiCheatBase {
	private Map<String, Integer> timesFired = null;
	private Map<String, Integer> totalTimesFired = null;
	private Map<String, Integer> fastBowUses = null;
	
	public FastBowFix() {
		super("FastBow");
		timesFired = new HashMap<String, Integer>();
		totalTimesFired = new HashMap<String, Integer>();
		fastBowUses = new HashMap<String, Integer>();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20 && isEnabled()) {
			timesFired.clear();
		}
	}
	
	@EventHandler
	public void onEntityShootBow(EntityShootBowEvent event) {
		if(isEnabled() && event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if(event.getForce() >= 0.95 && notIgnored(player)) {
				int times = 0;
				if(timesFired.containsKey(player.getName())) {
					times = timesFired.get(player.getName());
				}
				timesFired.put(player.getName(), ++times);
				if(times >= 2) {
					if(times >= 10) {
						int uses = 1;
						if(fastBowUses.containsKey(player.getName())) {
							uses += fastBowUses.get(player.getName());
						}
						fastBowUses.put(player.getName(), uses);
					}
					event.setCancelled(true);
				}
				times = 0;
				if(totalTimesFired.containsKey(player.getName())) {
					times = totalTimesFired.get(player.getName());
				}
				totalTimesFired.put(player.getName(), ++times);
			}
		}
	}
	
	@EventHandler
	public void onAsyncPlayerLeave(AsyncPlayerLeaveEvent event) {
		if(isEnabled()) {
			String name = event.getName();
			UUID uuid = event.getUUID();
			if(fastBowUses.containsKey(name)) {
				int timesShot = fastBowUses.get(name);
				if(timesShot > 0) {
					int percentage = (int) (timesShot * 100.0 / totalTimesFired.get(name));
					DB.NETWORK_POWER_BOW_LOGS.insert("'" + uuid.toString() + "', '" + percentage + "'");
				}
				fastBowUses.remove(name);
			}
			totalTimesFired.remove(name);
		}
	}
}
