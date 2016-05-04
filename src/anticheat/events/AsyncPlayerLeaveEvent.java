package anticheat.events;

import java.util.UUID;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AsyncPlayerLeaveEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private UUID uuid = null;
    private String name = null;
    
    public AsyncPlayerLeaveEvent(UUID uuid, String name) {
    	this.uuid = uuid;
    	this.name = name;
    }
    
    public UUID getUUID() {
    	return this.uuid;
    }
    
    public String getName() {
    	return this.name;
    }
 
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
