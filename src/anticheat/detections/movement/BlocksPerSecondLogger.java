package anticheat.detections.movement;

import anticheat.events.BPSEvent;
import anticheat.events.PlayerLeaveEvent;
import anticheat.events.TimeEvent;
import anticheat.util.EventUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class BlocksPerSecondLogger implements Listener {
    private Map<String, Location> lastLocations = null;
    private Map<String, Integer> delay = null;

    public BlocksPerSecondLogger() {
        lastLocations = new HashMap<String, Location>();
        delay = new HashMap<String, Integer>();
        EventUtil.register(this);
    }

    @EventHandler
    public void onTime(TimeEvent event) {
        long ticks = event.getTicks();
        if (ticks == 1) {
            Iterator<String> iterator = delay.keySet().iterator();
            while (iterator.hasNext()) {
                String name = iterator.next();
                int counter = delay.get(name);
                if (--counter <= 0) {
                    iterator.remove();
                } else {
                    delay.put(name, counter);
                }
            }
        } else if (ticks == 20) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getTicksLived() >= 20 * 3 && !player.isFlying() && !delay.containsKey(player.getName())) {
                    String name = player.getName();
                    Location pLoc = player.getLocation();
                    Location lLoc = pLoc;
                    if (lastLocations.containsKey(name)) {
                        lLoc = lastLocations.get(name);
                    }
                    lastLocations.put(name, pLoc);
                    if (pLoc.getWorld().getName().equals(lLoc.getWorld().getName()) && pLoc.getY() >= lLoc.getY()) {
                    	double lX = lLoc.getX();
                    	double lZ = lLoc.getZ();
                    	double pX = pLoc.getX();
            			double pZ = pLoc.getZ();
            			double distance = Math.sqrt((lX - pX) * (lX - pX) + (lZ - pZ) * (lZ - pZ));
                        if (distance > 0) {
                            Bukkit.getPluginManager().callEvent(new BPSEvent(player, distance));
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerVelocity(PlayerVelocityEvent event) {
    	Player player = event.getPlayer();
        Vector vel = player.getVelocity();
        double x = vel.getX() < 0 ? vel.getX() * -1 : vel.getX();
        double y = vel.getY() < 0 ? vel.getY() * -1 : vel.getY();
        double z = vel.getZ() < 0 ? vel.getZ() * -1 : vel.getZ();
        double value = x + y + z;
        lastLocations.remove(player.getName());
        delay.put(player.getName(), ((int) value * 5));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && !event.isCancelled()) {
            Player player = (Player) event.getEntity();
            lastLocations.remove(player.getName());
            delay.put(player.getName(), 40);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && !event.isCancelled()) {
            Player player = (Player) event.getEntity();
            lastLocations.remove(player.getName());
            delay.put(player.getName(), 40);
        }
    }

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
    	Player player = event.getPlayer();
        if (!(player.getAllowFlight() && player.isFlying())) {
        	lastLocations.remove(player.getName());
            delay.put(player.getName(), 20);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
    	Player player = event.getPlayer();
        Location to = event.getTo();
        Block below = to.getBlock().getRelative(0, -1, 0);
        if (to.getBlock().getType() == Material.SLIME_BLOCK || below.getType() == Material.SLIME_BLOCK) {
        	lastLocations.remove(player.getName());
            delay.put(player.getName(), 5 * ((int) (player.getFallDistance())));
        }
    }
    
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
    	Player player = event.getPlayer();
    	lastLocations.remove(player.getName());
		delay.put(player.getName(), 60);
    }

    @EventHandler
    public void onPlayerLeave(PlayerLeaveEvent event) {
        lastLocations.remove(event.getPlayer().getName());
    }
}
