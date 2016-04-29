package anticheat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.util.Vector;

import anticheat.util.EventUtil;
import anticheat.util.PlayerLeaveEvent;
import anticheat.util.TimeEvent;
import anticheat.util.Timer;

public class FastBowFix extends AntiCheatBase {
	private Map<String, Integer> timesFired = null;
	private Map<String, Integer> loggings = null;
	
	public FastBowFix() {
		super("FastBow");
		timesFired = new HashMap<String, Integer>();
		loggings = new HashMap<String, Integer>();
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
			if(Timer.getPing(player) < getMaxPing()) {
				Vector velocity = event.getProjectile().getVelocity();
				double x = velocity.getX() < 0 ? velocity.getX() * -1 : velocity.getX();
				double z = velocity.getZ() < 0 ? velocity.getZ() * -1 : velocity.getZ();
				if((x + z) >= 2.75 && notIgnored(player)) {
					int times = 0;
					if(timesFired.containsKey(player.getName())) {
						times = timesFired.get(player.getName());
					}
					timesFired.put(player.getName(), ++times);
					if(times >= 2) {
						if(times >= 10) {
							int logging = 1;
							if(loggings.containsKey(player.getName())) {
								logging += loggings.get(player.getName());
							}
							loggings.put(player.getName(), logging);
						}
						event.setCancelled(true);
					}
				}
			} else {
				timesFired.remove(player.getName());
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		if(isEnabled()) {
			Player player = event.getPlayer();
			String name = player.getName();
			UUID uuid = player.getUniqueId();
			if(loggings.containsKey(name)) {
				int timesShot = loggings.get(name);
				if(timesShot > 0) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "antiCheat NETWORK_POWER_BOW_LOGS " + uuid.toString() + " " + timesShot);
				}
				loggings.remove(name);
			}
		}
	}
}
