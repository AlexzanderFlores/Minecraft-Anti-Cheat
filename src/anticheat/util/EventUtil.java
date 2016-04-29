package anticheat.util;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import anticheat.AntiCheat;

public class EventUtil {
	public static void register(Listener listener) {
		Bukkit.getPluginManager().registerEvents(listener, AntiCheat.getInstance());
	}
}
