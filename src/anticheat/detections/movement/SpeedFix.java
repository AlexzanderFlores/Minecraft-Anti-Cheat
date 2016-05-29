package anticheat.detections.movement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import anticheat.AntiCheatBase;
import anticheat.events.AsyncPlayerLeaveEvent;
import anticheat.events.BPSEvent;
import anticheat.events.TimeEvent;
import anticheat.util.DB;
import anticheat.util.DelayedTask;
import anticheat.util.EventUtil;
import anticheat.util.Timer;

public class SpeedFix extends AntiCheatBase {
    private Map<String, List<Long>> violations = null;
    private Map<String, Integer> delay = null;
    private List<String> badBlockDelay = null;
    private String[] badBlocks = null;
    private long ticks = 0;

    public SpeedFix() {
        super("Speed");
        violations = new HashMap<String, List<Long>>();
        delay = new HashMap<String, Integer>();
        badBlockDelay = new ArrayList<String>();
        badBlocks = new String[]{"STAIR", "SLAB", "ICE"};
        EventUtil.register(this);
    }

    @EventHandler
    public void onTime(TimeEvent event) {
        long ticks = event.getTicks();
        if (ticks == 1 && isEnabled()) {
            ++this.ticks;
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
        }
    }

    @EventHandler
    public void onPlayerVelocity(PlayerVelocityEvent event) {
        if (isEnabled()) {
            Player player = event.getPlayer();
            Vector vel = player.getVelocity();
            double x = vel.getX() < 0 ? vel.getX() * -1 : vel.getX();
            double y = vel.getY() < 0 ? vel.getY() * -1 : vel.getY();
            double z = vel.getZ() < 0 ? vel.getZ() * -1 : vel.getZ();
            double value = x + y + z;
            delay.put(player.getName(), ((int) value * 5));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (isEnabled() && event.getEntity() instanceof Player && !event.isCancelled()) {
            Player player = (Player) event.getEntity();
            delay.put(player.getName(), 40);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (isEnabled() && event.getEntity() instanceof Player && !event.isCancelled()) {
            Player player = (Player) event.getEntity();
            delay.put(player.getName(), 40);
        }
    }

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        if (isEnabled()) {
            Player player = event.getPlayer();
            if (!(player.getAllowFlight() && player.isFlying())) {
                delay.put(player.getName(), 20);
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (isEnabled()) {
            Player player = event.getPlayer();
            Location to = event.getTo();
            Block below = to.getBlock().getRelative(0, -1, 0);
            if (to.getBlock().getType() == Material.SLIME_BLOCK || below.getType() == Material.SLIME_BLOCK) {
                delay.put(player.getName(), 5 * ((int) (player.getFallDistance())));
            }
        }
    }
    
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
    	if(isEnabled()) {
    		Player player = event.getPlayer();
    		violations.remove(player.getName());
    		delay.put(player.getName(), 20);
    	}
    }

    @EventHandler
    public void onBPS(BPSEvent event) {
        if (isEnabled()) {
            Player player = event.getPlayer();
            final String name = player.getName();
            if (Timer.getPing(player) > getMaxPing()) {
            	violations.remove(name);
                return;
            }
            if (!player.isFlying() && player.getVehicle() == null && !player.hasPotionEffect(PotionEffectType.SPEED)) {
            	if (notIgnored(player) && !badBlockDelay.contains(name) && !delay.containsKey(name) && player.getWalkSpeed() == 0.2f) {
                	Location location = player.getLocation();
                    for (int a = -2; a <= 0; ++a) {
                        Block block = location.getBlock().getRelative(0, a, 0);
                        for (String badBlock : badBlocks) {
                            if (block.getType().toString().contains(badBlock)) {
                                if (!badBlockDelay.contains(name)) {
                                    badBlockDelay.add(name);
                                    new DelayedTask(new Runnable() {
                                        @Override
                                        public void run() {
                                            badBlockDelay.remove(name);
                                        }
                                    }, 20 * 2);
                                }
                                violations.remove(name);
                                return;
                            }
                        }
                    }
                    if (location.getBlock().getRelative(0, 2, 0).getType() != Material.AIR) {
                    	violations.remove(name);
                    	return;
                    }
                    double distance = event.getDistance();
                    double max = 9;
                    if (distance > max) {
                    	Bukkit.getLogger().info(name + ": " + distance);
                    	List<Long> violation = violations.get(name);
                        if (violation == null) {
                            violation = new ArrayList<Long>();
                        }
                        violation.add(ticks);
                        violations.put(name, violation);
                        int recent = 0;
                        for (long ticks : violation) {
                            if (this.ticks - ticks <= 120) {
                                if (++recent >= 2) {
                                    ban(player);
                                    return;
                                }
                            }
                        }
                    } else {
                    	violations.remove(name);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onAsyncPlayerLeave(AsyncPlayerLeaveEvent event) {
        if (isEnabled()) {
            String name = event.getName();
            if (violations.containsKey(name)) {
                UUID uuid = event.getUUID();
                List<Long> loggings = violations.get(name);
                if (loggings != null) {
                    int average = 0;
                    for (long logging : loggings) {
                        average += logging;
                    }
                    if (average > 0) {
                        DB.NETWORK_DISTANCE_LOGS.insert("'" + uuid.toString() + "', '" + (average / loggings.size()) + "'");
                    }
                    violations.get(name).clear();
                    loggings.clear();
                    loggings = null;
                }
                violations.remove(name);
            }
        }
    }
}
