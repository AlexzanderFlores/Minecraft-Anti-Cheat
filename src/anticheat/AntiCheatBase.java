package anticheat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import anticheat.killaura.AttackThroughWalls;
import anticheat.killaura.InventoryKillAuraDetection;
import ostb.OSTB;
import ostb.customevents.player.PlayerBanEvent;
import ostb.gameapi.SpectatorHandler;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler;
import ostb.server.DB;
import ostb.server.PerformanceHandler;
import ostb.server.util.StringUtil;
import ostb.server.util.TimeUtil;
import ostb.staff.ban.BanHandler;

@SuppressWarnings("unused")
public class AntiCheatBase implements Listener {
	private static boolean enabled = true;
	private static List<String> banned = null; //TODO: Remove this on player leave
	private String name = null;
	private int maxPing = 135;
	
	public AntiCheatBase() {
		banned = new ArrayList<String>();
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
		return enabled;
	}
	
	public static void setEnabled(boolean enabled) {
		AntiCheatBase.enabled = enabled;
	}
	
	protected int getMaxPing() {
		return maxPing;
	}
	
	public boolean notIgnored(Player player) {
		int ping = PerformanceHandler.getPing(player);
		return ping > 0 && ping <= maxPing && !BanHandler.checkForBanned(player) && !SpectatorHandler.contains(player);
	}
	
	public void kick(Player player) {
		kick(player, "KICKED");
	}
	
	public void kick(Player player, String action) {
		String message = StringUtil.color("&bAnti Cheat: &f" + player.getName() + " &chas been &4" + action + "&c: \"&e" + this.name + "&c\"");
		MessageHandler.alert(message);
	}
	
	public void ban(Player player) {
		ban(player, Bukkit.getConsoleSender());
	}
	
	public void ban(Player player, CommandSender sender) {
		if(!BanHandler.checkForBanned(player) && !banned.contains(player.getName())) {
			banned.add(player.getName());
			int playerPing = PerformanceHandler.getPing(player);
			int staffPing = 0;
			if(sender instanceof Player) {
				Player staff = (Player) sender;
				staffPing = PerformanceHandler.getPing(staff);
			}
			if(PerformanceHandler.getTicksPerSecond() < 19) {
				MessageHandler.sendMessage(sender, "&cThere is possible server lag, ban cannot be executed");
			} else if(playerPing >= maxPing) {
				MessageHandler.sendMessage(sender, "&c" + player.getName() + " has a ping above " + maxPing + ", ban cannot be executed due to possible lag");
			} else if(staffPing >= maxPing) {
				MessageHandler.sendMessage(sender, "&cYou have a ping above " + maxPing + ", ban cannot be executed due to possible lag");
			} else {
				String information = playerPing + "-" + staffPing + "-" + OSTB.getServerName();
				String message = StringUtil.color("&bAnti Cheat: &f" + player.getName() + " &chas been &4BANNED &cfor use of the black-listed modification: \"&e" + this.name + "&c\" " + information);
				//toKick.put(player.getName(), message);
				MessageHandler.alert(message);
				String time = TimeUtil.getTime();
				String date = time.substring(0, 7);
				String uuid = "CONSOLE";
				if(sender instanceof Player) {
					Player staff = (Player) sender;
					uuid = staff.getUniqueId().toString();
					MessageHandler.sendMessage(sender, "(Note that this ban will be credited to you)");
				}
				UUID playerUUID = player.getUniqueId();
				Bukkit.getPluginManager().callEvent(new PlayerBanEvent(playerUUID, sender));
				int day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
				//DB.STAFF_BAN.insert("'" + playerUUID.toString() + "', 'null', '" + uuid + "', 'null', 'HACKING', '" + date + "', '" + time + "', 'null', 'null', '" + day + "', '1'");
				String proof = this.name + "-" + information;
				String [] keys = new String [] {"uuid", "active"};
				String [] values = new String [] {playerUUID.toString(), "1"};
				int id = DB.STAFF_BAN.getInt(keys, values, "id");
				//DB.STAFF_BAN_PROOF.insert("'" + id + "', '" + proof + "'");
				int counter = 0;
				for(String uuidString : DB.PLAYERS_ACCOUNTS.getAllStrings("uuid", "address", AccountHandler.getAddress(playerUUID))) {
					if(!uuidString.equals(playerUUID.toString())) {
						Player online = Bukkit.getPlayer(UUID.fromString(uuidString));
						if(online != null) {
							//toKick.put(online.getName(), StringUtil.color("&cYou have been banned due to sharing the IP of " + player.getName()));
						}
						//DB.STAFF_BAN.insert("'" + uuidString + "', '" + playerUUID.toString() + "', '" + uuid + "', 'null', 'HACKING', '" + date + "', '" + time + "', 'null', 'null', '" + day + "', '1'");
						keys = new String [] {"uuid", "active"};
						values = new String [] {uuidString, "1"};
						id = DB.STAFF_BAN.getInt(keys, values, "id");
						//DB.STAFF_BAN_PROOF.insert("'" + id + "', '" + proof + "'");
						++counter;
					}
				}
				if(counter > 0) {
					MessageHandler.alert("&cBanning &e" + counter + " &caccount" + (counter == 1 ? "" : "s") + " that shared the same IP as &e" + player.getName());
				}
			}
		}
	}
}
