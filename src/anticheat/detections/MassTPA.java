package anticheat.detections;

import anticheat.AntiCheatBase;
import anticheat.events.PlayerLeaveEvent;
import anticheat.events.TimeEvent;
import anticheat.util.EventUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class MassTPA extends AntiCheatBase {
    private int ticks = 0;
    private Map<String, Integer> commandTicks = null;
    private Map<String, Integer> violations = null;

    public MassTPA() {
        super("Mass TPA");
        commandTicks = new HashMap<String, Integer>();
        violations = new HashMap<String, Integer>();
        EventUtil.register(this);
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();
        if (message.toLowerCase().startsWith("/tpa ")) {
            Player player = event.getPlayer();
            String name = player.getName();
            int commandTick = ticks;
            if (commandTicks.containsKey(name)) {
                commandTick = commandTicks.get(name);
            }

            commandTicks.put(name, ticks);
        }
    }

    @EventHandler
    public void onTime(TimeEvent event) {
        long ticks = event.getTicks();
        if (ticks == 1) {
            ++this.ticks;
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerLeaveEvent event) {
        if (isEnabled()) {
            Player player = event.getPlayer();
            String name = player.getName();
            commandTicks.remove(name);
            violations.remove(name);
        }
    }
}
