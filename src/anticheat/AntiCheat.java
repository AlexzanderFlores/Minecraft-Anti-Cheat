package anticheat;

import org.bukkit.plugin.java.JavaPlugin;

public class AntiCheat extends JavaPlugin {
	@Override
	public void onEnable() {
		new AntiCheatBase();
	}
}
