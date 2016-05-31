package anticheat.detections.combat;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;

import anticheat.AntiCheatBase;
import anticheat.events.TimeEvent;
import anticheat.util.EventUtil;

public class AutoRegenFix extends AntiCheatBase {
	private Map<String, Integer> lastHealed = null;
	
	public AutoRegenFix() {
		super("AutoRegen");
		lastHealed = new HashMap<String, Integer>();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20) {
			lastHealed.clear();
		}
	}
	
	@EventHandler
	public void onEntityRegainHealth(EntityRegainHealthEvent event) {
		if(event.getEntity() instanceof Player && event.getRegainReason() == RegainReason.SATIATED) {
			Player player = (Player) event.getEntity();
			int counter = 0;
			if(lastHealed.containsKey(player.getName())) {
				counter = lastHealed.get(player.getName());
			}
			lastHealed.put(player.getName(), ++counter);
			if(counter >= 2) {
				event.setCancelled(true);
				if(counter >= 5) {
					ban(player);
				}
			}
		}
	}
}
