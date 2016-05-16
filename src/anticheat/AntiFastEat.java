package anticheat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import anticheat.events.PlayerLeaveEvent;
import anticheat.events.TimeEvent;
import anticheat.util.AsyncDelayedTask;
import anticheat.util.DB;
import anticheat.util.EventUtil;

public class AntiFastEat extends AntiCheatBase {
	private int ticks = 0;
	private Map<String, Integer> startedEating = null;
	private List<String> reported = null;
	
	public AntiFastEat() {
		super("Fast Eat");
		startedEating = new HashMap<String, Integer>();
		reported = new ArrayList<String>();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		ItemStack item = player.getItemInHand();
		if(item != null && item.getType().isEdible()) {
			Action action = event.getAction();
			if(action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
				startedEating.put(player.getName(), ticks);
			}
		}
	}
	
	@EventHandler
	public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
		Player player = event.getPlayer();
		if(startedEating.containsKey(player.getName())) {
			if(ticks - startedEating.get(player.getName()) == 0) {
				if(!reported.contains(player.getName())) {
					reported.add(player.getName());
					final UUID uuid = player.getUniqueId();
					new AsyncDelayedTask(new Runnable() {
						@Override
						public void run() {
							DB.NETWORK_FAST_EAT_TEST.insert("'" + uuid.toString() + "'");
						}
					});
				}
			}
		}
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 1) {
			++this.ticks;
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		startedEating.remove(event.getPlayer().getName());
		reported.remove(event.getPlayer().getName());
	}
}
