package anticheat.killaura;

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
import ostb.OSTB;
import ostb.OSTB.Plugins;
import ostb.customevents.TimeEvent;
import ostb.customevents.player.PlayerLeaveEvent;
import ostb.server.PerformanceHandler;
import ostb.server.util.EventUtil;

public class InventoryKillAuraDetection extends AntiCheatBase {
	private boolean hub = false;
	private Map<String, Integer> attacksPerSecond = null;
	private Map<String, Location> spawningLocation = null;
	private Map<String, Integer> secondsLived = null;
	private int maxSeconds = 5;
	
	public InventoryKillAuraDetection() {
		super("Kill Aura");
		Plugins plugin = OSTB.getPlugin();
		hub = plugin == Plugins.HUB;
		if(hub) {
			attacksPerSecond = new HashMap<String, Integer>();
			spawningLocation = new HashMap<String, Location>();
			secondsLived = new HashMap<String, Integer>();
			EventUtil.register(this);
		}
	}
	
	private int getSecondsLived(Player player) {
		return hub ? player.getTicksLived() / 20 : secondsLived.get(player.getName());
	}
	
	private boolean ableToCheck(Player player) {
		//int seconds = getSecondsLived(player);
		//return hub ? seconds < maxSeconds : seconds > 1 && seconds < maxSeconds;
		return true;
	}
	
	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent event) {
		if(isEnabled() && event.getPlayer() instanceof Player) {
			Player player = (Player) event.getPlayer();
			int ping = PerformanceHandler.getPing(player);
			boolean pingOk = /*ping > 0 && */ping < 100;
			if(player.getLocation().getBlock().getRelative(0, -1, 0).getType() != Material.AIR && ableToCheck(player) && notIgnored(player) && pingOk) {
				if(hub && spawningLocation.containsKey(player.getName())) {
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
				Bukkit.getLogger().info("ANTI CHEAT: " + player.getName() + " attacks: " + attacks);
				if(++attacks >= 7) {
					ban(player);
				} else {
					attacksPerSecond.put(player.getName(), attacks);
				}
			} else if(PerformanceHandler.getPing(player) > getMaxPing()) {
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
		if(ticks == 20) {
			if(isEnabled()) {
				attacksPerSecond.clear();
				for(Player player : Bukkit.getOnlinePlayers()) {
					secondsLived.put(player.getName(), secondsLived.get(player.getName()) + 1);
					if(getSecondsLived(player) >= maxSeconds && spawningLocation.containsKey(player.getName())) {
						spawningLocation.remove(player.getName());
					}
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		if(isEnabled()) {
			if(hub) {
				PlayerRespawnEvent.getHandlerList().unregister(this);
			} else if(event.getPlayer() != null) {
				secondsLived.put(event.getPlayer().getName(), 0);
				spawningLocation.put(event.getPlayer().getName(), event.getRespawnLocation());
			}
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
