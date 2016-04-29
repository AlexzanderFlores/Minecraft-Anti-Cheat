package anticheat.util;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TimeEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private long ticks = 0;
    
    public TimeEvent(long ticks) {
    	this.ticks = ticks;
    }
    
    public long getTicks() {
    	return this.ticks;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
