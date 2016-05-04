package anticheat.events;

import java.util.UUID;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import anticheat.util.DB;

public class PlayerBanEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private UUID uuid = null;
    private String reason = null;
    private boolean queue = false;
    
    public PlayerBanEvent(UUID uuid, String reason) {
    	this(uuid, reason, false);
    }
    
    public PlayerBanEvent(UUID uuid, String reason, boolean queue) {
    	this.uuid = uuid;
    	this.reason = reason;
    	this.queue = queue;
    	if(queue) {
    		DB.NETWORK_ANTI_CHEAT_BAN_QUEUE.insert("'" + uuid.toString() + "'");
    	}
    }
    
    public UUID getUUID() {
    	return this.uuid;
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
