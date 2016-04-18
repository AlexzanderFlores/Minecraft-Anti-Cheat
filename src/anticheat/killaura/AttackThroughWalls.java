package anticheat.killaura;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import anticheat.AntiCheatBase;
import ostb.customevents.TimeEvent;
import ostb.customevents.player.PlayerLeaveEvent;
import ostb.server.util.EventUtil;

@SuppressWarnings({"unchecked", "rawtypes"})
public class AttackThroughWalls extends AntiCheatBase implements Listener {
	private Map<String, Integer> counters = null;
	
	public AttackThroughWalls() {
		super("Attack Through Walls");
		counters = new HashMap<String, Integer>();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(isEnabled() && event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
			Player damager = (Player) event.getDamager();
			if(notIgnored(damager)) {
				Block block = damager.getTargetBlock((Set) null, 5);
				if(block.getType() != Material.AIR) {
					String name = damager.getName();
					int amount = 0;
					if(counters.containsKey(name)) {
						amount = counters.get(name);
					}
					counters.put(name, ++amount);
					if(amount >= 3) {
						Bukkit.getLogger().info("ANTI CHEAT: " + name + " has attacked through walls 3 times");
						counters.remove(name);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20 * 5) {
			counters.clear();
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		counters.remove(event.getPlayer().getName());
	}
}
