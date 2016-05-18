package anticheat.detections.movement;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import anticheat.AntiCheatBase;
import anticheat.events.PlayerLeaveEvent;
import anticheat.util.AsyncDelayedTask;
import anticheat.util.DB;
import anticheat.util.EventUtil;

public class GlideFix extends AntiCheatBase {
	private List<String> reported = null;
	private List<Double> values = null;
	
	public GlideFix() {
		super("Glide");
		reported = new ArrayList<String>();
		values = new ArrayList<Double>();
		values.add(-0.125); // Wurst
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if(isEnabled()) {
			Location to = event.getTo();
			Location from = event.getFrom();
			if(values.contains(to.getY() - from.getY())) {
				Player player = event.getPlayer();
				if(!reported.contains(player.getName())) {
					reported.add(player.getName());
					final UUID uuid = player.getUniqueId();
					new AsyncDelayedTask(new Runnable() {
						@Override
						public void run() {
							DB.NETWORK_GLIDE_TEST.insert("'" + uuid.toString() + "'");
						}
					});
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		if(isEnabled()) {
			reported.remove(event.getPlayer().getName());
		}
	}
}
