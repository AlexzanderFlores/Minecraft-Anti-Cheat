package anticheat.detections;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import anticheat.util.EventUtil;

@SuppressWarnings("deprecation")
public class InvisibleFireGlitchFix implements Listener {
	private List<Material> blocked = null;
	
	public InvisibleFireGlitchFix() {
		blocked = new ArrayList<Material>();
		blocked.add(Material.STEP);
		blocked.add(Material.WOOD_STEP);
		blocked.add(Material.WOOD_STAIRS);
		blocked.add(Material.COBBLESTONE_STAIRS);
		blocked.add(Material.BRICK_STAIRS);
		blocked.add(Material.NETHER_BRICK_STAIRS);
		blocked.add(Material.SANDSTONE_STAIRS);
		blocked.add(Material.SPRUCE_WOOD_STAIRS);
		blocked.add(Material.BIRCH_WOOD_STAIRS);
		blocked.add(Material.JUNGLE_WOOD_STAIRS);
		blocked.add(Material.QUARTZ_STAIRS);
		blocked.add(Material.IRON_FENCE);
		blocked.add(Material.GLASS);
		blocked.add(Material.STAINED_GLASS);
		blocked.add(Material.STAINED_GLASS_PANE);
		blocked.add(Material.GLOWSTONE);
		blocked.add(Material.CHEST);
		blocked.add(Material.TRAPPED_CHEST);
		blocked.add(Material.WATER_LILY);
		blocked.add(Material.CAKE);
		blocked.add(Material.WEB);
		blocked.add(Material.PISTON_BASE);
		blocked.add(Material.PISTON_STICKY_BASE);
		blocked.add(Material.POWERED_MINECART);
		blocked.add(Material.DETECTOR_RAIL);
		blocked.add(Material.RAILS);
		blocked.add(Material.ACTIVATOR_RAIL);
		blocked.add(Material.TORCH);
		blocked.add(Material.SNOW);
		blocked.add(Material.TRAP_DOOR);
		blocked.add(Material.LADDER);
		blocked.add(Material.FENCE_GATE);
		blocked.add(Material.NETHER_FENCE);
		blocked.add(Material.WOOD_PLATE);
		blocked.add(Material.STONE_PLATE);
		blocked.add(Material.GOLD_PLATE);
		blocked.add(Material.IRON_PLATE);
		blocked.add(Material.COBBLE_WALL);
		blocked.add(Material.ENCHANTMENT_TABLE);
		blocked.add(Material.ANVIL);
		blocked.add(Material.REDSTONE_LAMP_ON);
		blocked.add(Material.REDSTONE_LAMP_OFF);
		blocked.add(Material.ICE);
		blocked.add(Material.PACKED_ICE);
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(!event.isCancelled() && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if(event.getItem() != null && event.getItem().getType() == Material.FLINT_AND_STEEL) {
				if(blocked.contains(event.getClickedBlock().getType()) || event.getClickedBlock().getTypeId() == 109 || event.getClickedBlock().getTypeId() == 102) {
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if(!event.isCancelled()) {
			Block above = event.getBlock().getRelative(0, 1, 0);
			Material material = event.getBlock().getType();
			if((material == Material.LONG_GRASS || material == Material.DEAD_BUSH) && above.getType() == Material.FIRE) {
				above.setType(Material.AIR);
			}
		}
	}
}
