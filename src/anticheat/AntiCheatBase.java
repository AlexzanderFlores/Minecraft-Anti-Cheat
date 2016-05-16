package anticheat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import anticheat.events.PlayerBanEvent;
import anticheat.events.PlayerLeaveEvent;
import anticheat.killaura.AttackThroughWalls;
import anticheat.killaura.InventoryKillAuraDetection;
import anticheat.killaura.KillAura;
import anticheat.util.AsyncDelayedTask;
import anticheat.util.DB;
import anticheat.util.EventUtil;
import anticheat.util.Timer;

public class AntiCheatBase implements Listener {
	private static boolean enabled = true;
	private List<String> banned = new ArrayList<String>();
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
		new FlyFix();
		new InventoryKillAuraDetection();
		new HeadlessFix();
		new SpamBotFix();
		new WaterWalkDetection();
		new AutoClicker();
		new ClickPatternDetector();
		new KillAura();
		new AntiAutoArmor();
		new AntiAutoEat();
		new AntiAutoSprint();
		new AntiAutoSteal();
		EventUtil.register(this);
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
		return ping > 0 && ping <= maxPing && player.getGameMode() == GameMode.SURVIVAL && Timer.getTicksPerSecond() >= 19;
	}
	
	public void ban(Player player) {
		ban(player, false);
	}
	
	public void ban(Player player, boolean queue) {
		if(notIgnored(player) && !banned.contains(player.getName())) {
			banned.add(player.getName());
			if(queue) {
				final UUID uuid = player.getUniqueId();
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						DB.NETWORK_ANTI_CHEAT_BAN_QUEUE.insert("'" + uuid.toString() + ", '" + name + "''");
					}
				});
			} else {
				Bukkit.getPluginManager().callEvent(new PlayerBanEvent(player.getUniqueId(), player.getName(), name, queue));
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						if(DB.NETWORK_ANTI_CHEAT_DATA.isKeySet("cheat", name)) {
							int amount = DB.NETWORK_ANTI_CHEAT_DATA.getInt("cheat", name, "bans") + 1;
							DB.NETWORK_ANTI_CHEAT_DATA.updateInt("bans", amount, "cheat", name);
						} else {
							DB.NETWORK_ANTI_CHEAT_DATA.insert("'" + name + "', '1'");
						}
					}
				});
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		banned.remove(event.getPlayer().getName());
	}
}
