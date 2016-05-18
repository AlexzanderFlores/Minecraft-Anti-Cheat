package anticheat.detections.combat.killaura;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import anticheat.AntiCheatBase;
import anticheat.events.PlayerLeaveEvent;
import anticheat.events.TimeEvent;
import anticheat.util.EventUtil;
import anticheat.util.Timer;

public class InventoryKillAuraDetection extends AntiCheatBase {
	private Map<String, Integer> attacksPerSecond = null;
	private Map<String, Location> spawningLocation = null;
	private Map<String, Integer> secondsLived = null;
	private int maxSeconds = 5;
	
	public InventoryKillAuraDetection() {
		super("KillAura");
		attacksPerSecond = new HashMap<String, Integer>();
		spawningLocation = new HashMap<String, Location>();
		secondsLived = new HashMap<String, Integer>();
		EventUtil.register(this);
	}
	
	private int getSecondsLived(Player player) {
		return secondsLived == null ? -1 : secondsLived.get(player.getName());
	}
	
	private boolean ableToCheck(Player player) {
		int seconds = getSecondsLived(player);
		return seconds > 1 && seconds < maxSeconds;
	}
	
	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent event) {
		if(isEnabled() && event.getPlayer() instanceof Player) {
			Player player = (Player) event.getPlayer();
			int ping = Timer.getPing(player);
			boolean pingOk = ping > 0 && ping < 100;
			if(player.getLocation().getBlock().getRelative(0, -1, 0).getType() != Material.AIR && ableToCheck(player) && notIgnored(player) && pingOk) {
				if(spawningLocation.containsKey(player.getName())) {
					double x1 = player.getLocation().getX();
					double z1 = player.getLocation().getZ();
					double x2 = spawningLocation.get(player.getName()).getX();
					double z2 = spawningLocation.get(player.getName()).getZ();
					if(x1 != x2 || z1 != z2) {
						spawningLocation.remove(player.getName());
						return;
					}
				}
				int attacks = 0;
				if(attacksPerSecond.containsKey(player.getName())) {
					attacks = attacksPerSecond.get(player.getName());
				}
				if(++attacks >= 7) {
					ban(player, true);
				} else {
					attacksPerSecond.put(player.getName(), attacks);
				}
			} else if(Timer.getPing(player) > getMaxPing()) {
				attacksPerSecond.remove(player.getName());
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent event) {
		if(isEnabled()) {
			secondsLived.put(event.getPlayer().getName(), 0);
			spawningLocation.put(event.getPlayer().getName(), event.getPlayer().getLocation());
		}
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20 && isEnabled()) {
			attacksPerSecond.clear();
			for(Player player : Bukkit.getOnlinePlayers()) {
				int seconds = 0;
				if(secondsLived.containsKey(player.getName())) {
					seconds = secondsLived.get(player.getName());
				}
				secondsLived.put(player.getName(), ++seconds);
				if(getSecondsLived(player) >= maxSeconds && spawningLocation.containsKey(player.getName())) {
					spawningLocation.remove(player.getName());
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		if(isEnabled()) {
			secondsLived.put(event.getPlayer().getName(), 0);
			spawningLocation.put(event.getPlayer().getName(), event.getRespawnLocation());
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		if(isEnabled()) {
			secondsLived.remove(event.getPlayer().getName());
			spawningLocation.remove(event.getPlayer().getName());
		}
	}
}
