package anticheat.util;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BPSEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player player = null;
    private double distance = 0;
    
    public BPSEvent(Player player, double distance) {
    	this.player = player;
    	this.distance = distance;
    }
    
    public Player getPlayer() {
    	return this.player;
    }
    
    public double getDistance() {
    	return this.distance;
    }
 
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
