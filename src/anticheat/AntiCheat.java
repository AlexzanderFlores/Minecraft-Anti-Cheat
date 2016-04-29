package anticheat;

import org.bukkit.plugin.java.JavaPlugin;

import anticheat.util.PlayerLeaveEvent;
import anticheat.util.Timer;

public class AntiCheat extends JavaPlugin {
	private static AntiCheat instance = null;
	
	@Override
	public void onEnable() {
		instance = this;
		new Timer();
		new PlayerLeaveEvent();
		new AntiCheatBase();
	}
	
	public static AntiCheat getInstance() {
		return instance;
	}
}
