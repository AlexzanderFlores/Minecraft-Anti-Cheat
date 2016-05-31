package anticheat.detections.movement;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import anticheat.AntiCheatBase;
import anticheat.events.PlayerLeaveEvent;
import anticheat.util.EventUtil;

public class ConstantMovement extends AntiCheatBase {
    private Map<String, Integer> headlessViolations = null;
    private Map<String, Double> lastMovements = null;
    private Map<String, Integer> movementViolations = null;

    public ConstantMovement() {
        super("ConstantMovement");
        headlessViolations = new HashMap<String, Integer>();
        lastMovements = new HashMap<String, Double>();
        movementViolations = new HashMap<String, Integer>();
        EventUtil.register(this);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (isEnabled()) {
            Player player = event.getPlayer();
            String name = player.getName();
            Location to = event.getTo();

			/*
			 * Check for:
			 * Headless
			 */
            float pitch = to.getPitch();
            if (pitch == 180.0f || pitch == -180.0f) {
                int headlessViolation = 0;
                if (headlessViolations.containsKey(name)) {
                    headlessViolation = headlessViolations.get(name);
                }
                headlessViolations.put(name, ++headlessViolation);
                if (headlessViolation >= 3) {
                    ban(player);
                }
                return;
            }

            Location from = event.getFrom();
            double difference = to.getY() - from.getY();
            if (difference != 0) {
				/*
				 * Check for:
				 * Wurst High Jump
				 */
                if (("" + difference).startsWith("1.02000")) {
                    ban(player);
                    return;
                }
				
				/*
				 * Check for:
				 * Wurst Glide
				 * Wurst Spider
				 * Various other constant changing Y velocity cheats
				 */
                if (lastMovements.containsKey(name)) {
                	Material type = to.getBlock().getType();
                	if(type == Material.LADDER || type == Material.VINE) {
                		return;
                	}
                    double lastMovement = lastMovements.get(name);
                    if (lastMovement == difference) {
                        int violation = 0;
                        if (movementViolations.containsKey(name)) {
                            violation = movementViolations.get(name);
                        }
                        movementViolations.put(name, ++violation);
                        if (violation >= 5) {
                        	ban(player);
                        	return;
                        }
                    } else {
                        movementViolations.remove(name);
                    }
                }
                lastMovements.put(name, difference);
            }
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerLeaveEvent event) {
        if (isEnabled()) {
            Player player = event.getPlayer();
            String name = player.getName();
            headlessViolations.remove(name);
            lastMovements.remove(name);
            movementViolations.remove(name);
        }
    }
}
