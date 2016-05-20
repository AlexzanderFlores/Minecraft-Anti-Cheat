package anticheat.detections;

import anticheat.util.EventUtil;
import anticheat.util.Timer;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class SpamBotFix implements Listener {
    public SpamBotFix() {
        EventUtil.register(this);
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        if (Timer.getPing(event.getPlayer()) == 0) {
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot talk with your current connection (0 ping)!");
            event.setCancelled(true);
        }
    }
}
