package anticheat.util;

import anticheat.AntiCheat;
import anticheat.events.TimeEvent;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class Timer implements Listener {
    private int counter = 0;
    private static double ticksPerSecond = 0;
    private long seconds = 0;
    private long currentSecond = 0;
    private int tickCounter = 0;

    public Timer() {
        final List<Integer> counters = new ArrayList<Integer>();
        for (int a = 1; a <= 20; ++a) {
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
                for (int a : counters) {
                    if (counter % a == 0) {
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

    public static double getTicksPerSecond() {
        return ticksPerSecond;
    }

    public static double getMemory() {
        return getMemory(true);
    }

    public static double getMemory(boolean round) {
        double total = Runtime.getRuntime().totalMemory() / (1024 * 1024);
        double allocated = Runtime.getRuntime().maxMemory() / (1024 * 1024);
        return (int) (total * 100.0d / allocated + 0.5);
    }

    @EventHandler
    public void onTime(TimeEvent event) {
        long ticks = event.getTicks();
        if (ticks == 1) {
            seconds = (System.currentTimeMillis() / 1000);
            if (currentSecond == seconds) {
                ++tickCounter;
            } else {
                currentSecond = seconds;
                ticksPerSecond = (ticksPerSecond == 0 ? tickCounter : ((ticksPerSecond + tickCounter) / 2));
                if (ticksPerSecond < 19.0d) {
                    ++ticksPerSecond;
                }
                if (ticksPerSecond > 20.0d) {
                    ticksPerSecond = 20.0d;
                }
                ticksPerSecond = new BigDecimal(ticksPerSecond).setScale(2, RoundingMode.HALF_UP).doubleValue();
                tickCounter = 0;
            }
        }
    }
}
