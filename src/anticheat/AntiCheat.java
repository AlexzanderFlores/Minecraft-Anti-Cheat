package anticheat;

import org.bukkit.plugin.java.JavaPlugin;

import anticheat.events.PlayerLeaveEvent;
import anticheat.util.DB;
import anticheat.util.DB.Databases;
import anticheat.util.Timer;

public class AntiCheat extends JavaPlugin {
	private static AntiCheat instance = null;

    @Override
    public void onEnable() {
        instance = this;
        for (Databases database : Databases.values()) {
            database.connect();
        }
        DB.values();
        new Timer();
        new PlayerLeaveEvent();
        new AntiCheatBase();
    }

    @Override
    public void onDisable() {
        instance = null;
        for (Databases database : Databases.values()) {
            database.disconnect();
        }
    }

    public static AntiCheat getInstance() {
        return instance;
    }
}
