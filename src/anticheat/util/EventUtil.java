package anticheat.util;

import anticheat.AntiCheat;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public class EventUtil {
    public static void register(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, AntiCheat.getInstance());
    }
}
