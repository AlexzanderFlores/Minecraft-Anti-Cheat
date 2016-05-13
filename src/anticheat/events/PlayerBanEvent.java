package anticheat.events;

import java.util.UUID;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerBanEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private UUID uuid = null;
    private String name = null;
    private String reason = null;
    private boolean queue = false;
    
    public PlayerBanEvent(UUID uuid, String name, String reason) {
    	this(uuid, name, reason, false);
    }
    
    public PlayerBanEvent(UUID uuid, String name, String reason, boolean queue) {
    	this.uuid = uuid;
    	this.name = name;
    	this.reason = reason;
    	this.queue = queue;
    }
    
    public UUID getUUID() {
    	return this.uuid;
    }
    
    public String getName() {
    	return this.name;
    }
    
    public String getReason() {
    	return this.reason;
    }
    
    public boolean getQueue() {
    	return this.queue;
    }
 
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
