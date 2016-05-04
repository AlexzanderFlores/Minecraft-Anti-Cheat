package anticheat.events;

import java.util.UUID;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerBanEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private UUID uuid = null;
    private String reason = null;
    
    public PlayerBanEvent(UUID uuid, String reason) {
    	this.uuid = uuid;
    	this.reason = reason;
    }
    
    public UUID getUUID() {
    	return this.uuid;
    }
    
    public String getReason() {
    	return this.reason;
    }
 
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
