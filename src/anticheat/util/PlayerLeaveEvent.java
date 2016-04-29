package anticheat.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerLeaveEvent extends Event implements Listener {
    private static final HandlerList handlers = new HandlerList();
    private Player player = null;
    private String leaveMessage = null;
    
    public PlayerLeaveEvent() {
    	EventUtil.register(this);
    }
 
    public PlayerLeaveEvent(Player player, String leaveMessage) {
    	this.player = player;
    	this.leaveMessage = leaveMessage;
    }
    
    public Player getPlayer() {
    	return this.player;
    }
    
    public String getLeaveMessage() {
    	return this.leaveMessage;
    }
    
    public void setLeaveMessage(String leaveMessage) {
    	this.leaveMessage = leaveMessage;
    }
 
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		PlayerLeaveEvent leaveEvent = new PlayerLeaveEvent(event.getPlayer(), event.getQuitMessage());
		Bukkit.getPluginManager().callEvent(leaveEvent);
		event.setQuitMessage(leaveEvent.getLeaveMessage());
	}
	
	@EventHandler
	public void onPlayerKick(PlayerKickEvent event) {
		PlayerLeaveEvent leaveEvent = new PlayerLeaveEvent(event.getPlayer(), event.getLeaveMessage());
		Bukkit.getPluginManager().callEvent(leaveEvent);
		event.setLeaveMessage(leaveEvent.getLeaveMessage());
	}
}
