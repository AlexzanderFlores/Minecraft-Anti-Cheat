package anticheat.killaura;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import anticheat.AntiCheatBase;
import anticheat.util.EventUtil;
import net.minecraft.server.v1_8_R3.AxisAlignedBB;

@SuppressWarnings({"rawtypes", "unchecked"})
public class KillAura extends AntiCheatBase {
	public KillAura() {
		super("KillAura");
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
			Player damager = (Player) event.getDamager();
			Player player = (Player) event.getEntity();
			Player near = getNearestEntityInSight(damager, 5);
			double distance = damager.getLocation().distance(near.getLocation());
			Bukkit.getLogger().info("Distance = " + distance);
			if((near == null || near != player) && distance > 2) {
				Bukkit.getLogger().info(damager.getName() + " damaging player out of sight <<<===");
			}
		}
	}
	
	private Player getNearestEntityInSight(Player player, int range) {
	    List<Entity> entities = player.getNearbyEntities(range, range, range); //Get the entities within range
	    Iterator<Entity> iterator = entities.iterator(); //Create an iterator
	    while(iterator.hasNext()) {
	        Entity next = iterator.next(); //Get the next entity in the iterator
	        if(!(next instanceof LivingEntity) || next == player) { //If the entity is not a living entity or the player itself, remove it from the list
	            iterator.remove();
	        }
	    }
		List<Block> sight = player.getLineOfSight((Set) null, range); //Get the blocks in the player's line of sight (the Set is null to not ignore any blocks)
	    for(Block block : sight) { //For each block in the list
	        if(block.getType() != Material.AIR) { //If the block is not air -> obstruction reached, exit loop/seach
	            break;
	        }
	        Location low = block.getLocation(); //Lower corner of the block
	        Location high = low.clone().add(1, 1, 1); //Higher corner of the block
	        AxisAlignedBB blockBoundingBox = AxisAlignedBB.a(low.getX(), low.getY(), low.getZ(), high.getX(), high.getY(), high.getZ()); //The bounding or collision box of the block
	        for(Entity entity : entities) { //For every living entity in the player's range
	            //If the entity is truly close enough and the bounding box of the block (1x1x1 box) intersects with the entity's bounding box, return it
	            if(entity.getLocation().distance(player.getEyeLocation()) <= range && ((CraftEntity) entity).getHandle().getBoundingBox().b(blockBoundingBox) && entity instanceof Player) {
	                return (Player) entity;
	            }
	        }
	    }
	    return null; //Return null/nothing if no entity was found
	}
}
