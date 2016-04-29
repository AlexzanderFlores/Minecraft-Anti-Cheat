package anticheat.util;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import anticheat.AntiCheat;

public class DelayedTask implements Listener {
	private int id = -1;
	
	public DelayedTask(Runnable runnable) {
		this(runnable, 1);
	}
	
	public DelayedTask(Runnable runnable, long delay) {
		AntiCheat instance = AntiCheat.getInstance();
		if(instance.isEnabled()) {
			id = Bukkit.getScheduler().scheduleSyncDelayedTask(instance, runnable, delay);
		} else {
			runnable.run();
		}
	}
	
	public int getId() {
		return id;
	}
}
