package anticheat.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import anticheat.AntiCheat;

public class Timer implements Listener {
	private int counter = 0;
	
	public Timer() {
		final List<Integer> counters = new ArrayList<Integer>();
		for(int a = 1; a <= 20; ++a) {
			counters.add(a);
		}
		counters.add(20 * 2);
		counters.add(20 * 5);
		counters.add(20 * 10);
		counters.add(20 * 60);
		Bukkit.getScheduler().runTaskTimer(AntiCheat.getInstance(), new Runnable() {
			@Override
			public void run() {
				++counter;
				for(int a : counters) {
					if(counter % a == 0) {
						Bukkit.getPluginManager().callEvent(new TimeEvent(a));
					}
				}
			}
		}, 1, 1);
		EventUtil.register(this);
	}
	
	public static int getPing(Player player) {
		CraftPlayer craftPlayer = (CraftPlayer) player;
		return craftPlayer.getHandle().ping / 2;
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		final UUID uuid = event.getPlayer().getUniqueId();
		final String name = event.getPlayer().getName();
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				Bukkit.getPluginManager().callEvent(new AsyncPlayerLeaveEvent(uuid, name));
			}
		});
	}
}
