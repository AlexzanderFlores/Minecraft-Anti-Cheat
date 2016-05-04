package anticheat.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CPSEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player player = null;
    private int cps = 0;
    
    public CPSEvent(Player player, int cps) {
    	this.player = player;
    	this.cps = cps;
    }
    
    public Player getPlayer() {
    	return this.player;
    }
    
    public double getCPS() {
    	return this.cps;
    }
 
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
