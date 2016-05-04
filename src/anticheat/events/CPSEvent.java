package anticheat.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CPSEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private String name= null;
    private int cps = 0;
    
    public CPSEvent(String name, int cps) {
    	this.name = name;
    	this.cps = cps;
    }
    
    public String getName() {
    	return this.name;
    }
    
    public int getCPS() {
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
