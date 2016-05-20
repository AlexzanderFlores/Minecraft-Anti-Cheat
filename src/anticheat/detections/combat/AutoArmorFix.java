package anticheat.detections.combat;

import anticheat.AntiCheatBase;
import anticheat.events.PlayerLeaveEvent;
import anticheat.events.TimeEvent;
import anticheat.util.AsyncDelayedTask;
import anticheat.util.DB;
import anticheat.util.EventUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AutoArmorFix extends AntiCheatBase {
    private Map<String, Integer> lastAction = null;
    private int ticks = 0;

    public AutoArmorFix() {
        super("AutoArmor");
        lastAction = new HashMap<String, Integer>();
        EventUtil.register(this);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (isEnabled()) {
            lastAction.put(event.getPlayer().getName(), ticks);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (isEnabled() && event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            if (event.getClick() == ClickType.SHIFT_LEFT) {
                InventoryAction action = event.getAction();
                if (action == InventoryAction.NOTHING || action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                    if (lastAction.containsKey(player.getName())) {
                        int lastTicks = lastAction.get(player.getName());
                        int diff = ticks - lastTicks;
                        if (diff == 0) {
                            final UUID uuid = player.getUniqueId();
                            new AsyncDelayedTask(new Runnable() {
                                @Override
                                public void run() {
                                    DB.NETWORK_AUTO_ARMOR_TEST.insert("'" + uuid.toString() + "'");
                                }
                            });
                        }
                    }
                    lastAction.put(player.getName(), ticks);
                }
            }
        }
    }

    @EventHandler
    public void onTime(TimeEvent event) {
        long ticks = event.getTicks();
        if (ticks == 1 && isEnabled()) {
            ++this.ticks;
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerLeaveEvent event) {
        if (isEnabled()) {
            lastAction.remove(event.getPlayer().getName());
        }
    }
}
