package anticheat.detections.movement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

public class ConstantMovement extends AntiCheatBase {
	private Map<String, Double> lastMovements = null;
	private Map<String, Integer> violations = null;
	private List<String> reported = null; // temporary list for people reported for high jump. To prevent multiple MySQL queries
	private List<String> reported2 = null; // temporary list for people reported for constant movement. To prevent multiple MySQL queries
	
	public ConstantMovement() {
		super("Constant Movement");
		lastMovements = new HashMap<String, Double>();
		violations = new HashMap<String, Integer>();
		reported = new ArrayList<String>();
		reported2 = new ArrayList<String>();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if(isEnabled()) {
			Location to = event.getTo();
			Location from = event.getFrom();
			double difference = to.getY() - from.getY();			
			if(difference != 0) {
				Player player = event.getPlayer();
				String name = player.getName();
				
				/*
				 * Check for:
				 * Wurst High Jump
				 */
				if(("" + difference).startsWith("1.02000")) {
					if(!reported.contains(player.getName())) {
						reported.add(player.getName());
						final UUID uuid = player.getUniqueId();
						new AsyncDelayedTask(new Runnable() {
							@Override
							public void run() {
								DB.NETWORK_HIGH_JUMP_TEST.insert("'" + uuid.toString() + "'");
							}
						});
					}
					return;
				}
				
				/*
				 * Check for:
				 * Wurst Glide
				 * Wurst Spider
				 * Various other constant changing Y velocity cheats
				 */
				if(lastMovements.containsKey(name)) {
					double lastMovement = lastMovements.get(name);
					if(lastMovement == difference) {
						int violation = 0;
						if(violations.containsKey(name)) {
							violation = violations.get(name);
						}
						violations.put(name, ++violation);
						if(violation >= 5) {
							if(!reported2.contains(player.getName())) {
								reported2.add(player.getName());
								final UUID uuid = player.getUniqueId();
								new AsyncDelayedTask(new Runnable() {
									@Override
									public void run() {
										DB.NETWORK_CONSTANT_MOVEMENT_TEST.insert("'" + uuid.toString() + "'");
									}
								});
							}
						}
					} else {
						violations.remove(name);
					}
				}
				lastMovements.put(name, difference);
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		if(isEnabled()) {
			Player player = event.getPlayer();
			String name = player.getName();
			lastMovements.remove(name);
			violations.remove(name);
			reported.remove(name);
			reported2.remove(name);
		}
	}
}
