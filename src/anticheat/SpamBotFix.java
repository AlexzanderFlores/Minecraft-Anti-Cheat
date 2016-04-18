package anticheat;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.PerformanceHandler;
import ostb.server.util.EventUtil;

public class SpamBotFix implements Listener {
	public SpamBotFix() {
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		if(PerformanceHandler.getPing(event.getPlayer()) == 0 && !Ranks.PREMIUM.hasRank(event.getPlayer())) {
			MessageHandler.sendMessage(event.getPlayer(), "&cYou cannot talk with your current connection (0 ping) unless you have " + Ranks.PREMIUM.getPrefix());
			event.setCancelled(true);
		}
	}
}
