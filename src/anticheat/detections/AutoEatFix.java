package anticheat.detections;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import anticheat.AntiCheatBase;
import anticheat.events.PlayerLeaveEvent;
import anticheat.events.TimeEvent;
import anticheat.util.AsyncDelayedTask;
import anticheat.util.DB;
import anticheat.util.EventUtil;

public class AutoEatFix extends AntiCheatBase {
	private int ticks = 0;
	private Map<String, Integer> eating = null;
	private Map<String, Integer> violations = null;
	
	public AutoEatFix() {
		super("Auto Eat");
		eating = new HashMap<String, Integer>();
		violations = new HashMap<String, Integer>();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(isEnabled()) {
			Player player = event.getPlayer();
			Action action = event.getAction();
			if(action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
				ItemStack item = player.getItemInHand();
				if(item != null && item.getType().isEdible()) {
					if(eating.containsKey(player.getName())) {
						int movedTicks = eating.get(player.getName());
						if(ticks - movedTicks == 1) {
							event.setCancelled(true);
							int violation = 0;
							if(violations.containsKey(player.getName())) {
								violation = violations.get(player.getName());
							}
							violations.put(player.getName(), ++violation);
							if(violation == 5) {
								final UUID uuid = player.getUniqueId();
								new AsyncDelayedTask(new Runnable() {
									@Override
									public void run() {
										DB.NETWORK_AUTO_EAT_TEST.insert("'" + uuid.toString() + "'");
									}
								});
							}
						}
					}
					eating.put(player.getName(), ticks);
				}
			}
		}
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 1 && isEnabled()) {
			++this.ticks;
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		if(isEnabled()) {
			eating.remove(event.getPlayer().getName());
			violations.remove(event.getPlayer().getName());
		}
	}
}
