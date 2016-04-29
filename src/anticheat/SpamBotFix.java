package anticheat;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import anticheat.util.EventUtil;
import anticheat.util.MessageHandler;
import anticheat.util.Timer;

public class SpamBotFix implements Listener {
	public SpamBotFix() {
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		if(Timer.getPing(event.getPlayer()) == 0) {
			MessageHandler.sendMessage(event.getPlayer(), "&cYou cannot talk with your current connection (0 ping)");
			event.setCancelled(true);
		}
	}
}
