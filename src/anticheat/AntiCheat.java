package anticheat;

import org.bukkit.plugin.java.JavaPlugin;

import anticheat.events.PlayerLeaveEvent;
import anticheat.util.DB;
import anticheat.util.Timer;

public class AntiCheat extends JavaPlugin {
	private static AntiCheat instance = null;
	
	@Override
	public void onEnable() {
		instance = this;
		DB.values();
		new Timer();
		new PlayerLeaveEvent();
		new AntiCheatBase();
	}
	
	public static AntiCheat getInstance() {
		return instance;
	}
}
