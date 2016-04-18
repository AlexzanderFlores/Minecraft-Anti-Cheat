package anticheat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;

import ostb.server.util.EventUtil;

public class AntiNoSlow extends AntiCheatBase implements Listener {
	public AntiNoSlow() {
        super("No Slowdown");
        EventUtil.register(this);
    }

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event) {
        if(event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if(player.isSprinting() && player.getVehicle() == null) {
                player.setSprinting(false);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerEating(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if(player.isSprinting() && player.getVehicle() == null) {
            player.setSprinting(false);
            event.setCancelled(true);
        }
    }
}