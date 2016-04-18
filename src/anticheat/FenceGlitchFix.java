package anticheat;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;

import ostb.server.tasks.DelayedTask;
import ostb.server.util.EventUtil;


public class FenceGlitchFix implements Listener {
	private static List<String> cannotRun = null;
	
	public FenceGlitchFix() {
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onPlayerToggleSpring(PlayerToggleSprintEvent event) {
		if(cannotRun != null && cannotRun.contains(event.getPlayer().getName())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getItem() != null && (event.getItem().getType().isEdible() || event.getItem().getType() == Material.BOW) && (event.getClickedBlock().getType() == Material.FENCE || event.getClickedBlock().getType() == Material.NETHER_FENCE)) {
			event.getPlayer().setSprinting(false);
			event.setCancelled(true);
			if(cannotRun == null) {
				cannotRun = new ArrayList<String>();
			}
			final String name = event.getPlayer().getName();
			cannotRun.add(name);
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					cannotRun.remove(name);
				}
			}, 10);
		}
	}
}
