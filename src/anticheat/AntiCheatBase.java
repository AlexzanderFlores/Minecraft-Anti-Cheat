package anticheat;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import anticheat.killaura.AttackThroughWalls;
import anticheat.killaura.InventoryKillAuraDetection;
import anticheat.util.Timer;

public class AntiCheatBase implements Listener {
	private static boolean enabled = true;
	private String name = null;
	private int maxPing = 135;
	
	public AntiCheatBase() {
		new BlocksPerSecondLogger();
		new InvisibleFireGlitchFix();
		new FastBowFix();
		new AutoCritFix();
		new AttackThroughWalls();
		new AttackDistanceLogger();
		//new SpeedFix();
		new SpeedFix2();
		//new SurvivalFly();
		new FlyFix();
		new InventoryKillAuraDetection();
		new HeadlessFix();
		new SpamBotFix();
		new WaterWalkDetection();
		new AutoClicker();
	}
	
	public AntiCheatBase(String name) {
		this.name = name;
	}
	
	public static boolean isEnabled() {
		return enabled && Timer.getTicksPerSecond() >= 18.75 && Timer.getMemory() <= 85;
	}
	
	public static void setEnabled(boolean enabled) {
		AntiCheatBase.enabled = enabled;
	}
	
	protected int getMaxPing() {
		return maxPing;
	}
	
	public boolean notIgnored(Player player) {
		int ping = Timer.getPing(player);
		return ping > 0 && ping <= maxPing && player.getGameMode() == GameMode.SURVIVAL;
	}
	
	public void kick(Player player) {
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "antiCheat kick " + player.getName() + " " + this.name);
	}
	
	public void ban(Player player) {
		if(notIgnored(player)) {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "antiCheat ban " + player.getName() + " " + this.name);
		}
	}
}
