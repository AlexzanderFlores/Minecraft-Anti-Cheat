package anticheat;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.potion.PotionEffectType;

import anticheat.util.AsyncDelayedTask;
import anticheat.util.EventUtil;
import anticheat.util.PlayerLeaveEvent;
import anticheat.util.TimeEvent;
import anticheat.util.Timer;

public class SurvivalFly extends AntiCheatBase {
	private Map<String, Integer> heightIncreasing = null;
	private Map<String, Integer> wouldBan = null;
	private Map<String, Integer> disabledCounters = null;
	private Map<String, Integer> floating = null;
	
	public SurvivalFly() {
		super("Survival Fly");
		heightIncreasing = new HashMap<String, Integer>();
		wouldBan = new HashMap<String, Integer>();
		disabledCounters = new HashMap<String, Integer>();
		floating = new HashMap<String, Integer>();
		EventUtil.register(this);
	}
	
	private void disable(Player player, int seconds) {
		if(isEnabled()) {
			disabledCounters.put(player.getName(), seconds);
		}
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20) {
			if(isEnabled()) {
				Iterator<String> iterator = disabledCounters.keySet().iterator();
				while(iterator.hasNext()) {
					String name = iterator.next();
					int counter = disabledCounters.get(name);
					if(--counter <= 0) {
						iterator.remove();
					} else {
						disabledCounters.put(name, counter);
					}
				}
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						for(Player player : Bukkit.getOnlinePlayers()) {
							if(Timer.getPing(player) < getMaxPing() && player.getTicksLived() >= 20 * 3 && !player.getAllowFlight() && player.getVehicle() == null) {
								if(notIgnored(player) && !disabledCounters.containsKey(player.getName()) && !player.hasPotionEffect(PotionEffectType.JUMP)) {
									Location location = player.getLocation();
									int blocks = 0;
									for(int a = 0; a < 3; ++a) {
										Block block = location.getBlock().getRelative(0, -a, 0);
										if(block.getType() == Material.AIR) {
											++blocks;
										} else {
											blocks = 0;
											floating.remove(player.getName());
											return;
										}
									}
									if(blocks == 3) {
										for(int y = 0; y >= -1; --y) {
											for(int x = 1; x >= -1; --x) {
												for(int z = 1; z >= -1; --z) {
													if(player.getLocation().getBlock().getRelative(x, y, z).getType() != Material.AIR) {
														return;
													}
												}
											}
										}
										int counter = 0;
										if(floating.containsKey(player.getName())) {
											counter = floating.get(player.getName());
										}
										if(++counter >= 3) {
											ban(player);
										} else {
											floating.put(player.getName(), counter);
										}
									}
								}
							}
						}
					}
				});
			}
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if(isEnabled()) {
			Player player = event.getPlayer();
			if(event.getTo().getY() < event.getFrom().getY()) {
				floating.remove(player.getName());
			}
			if(Timer.getPing(player) < getMaxPing() && player.getTicksLived() >= 20 * 3 && !player.getAllowFlight() && player.getVehicle() == null) {
				if(notIgnored(player) && !disabledCounters.containsKey(player.getName()) && !player.hasPotionEffect(PotionEffectType.JUMP)) {
					for(int y = 0; y >= -2; --y) {
						for(int x = 2; x >= -2; --x) {
							for(int z = 2; z >= -2; --z) {
								if(player.getLocation().getBlock().getRelative(x, y, z).getType() != Material.AIR) {
									return;
								}
							}
						}
					}
					Location to = event.getTo();
					Location from = event.getFrom();
					if(to.getY() >= from.getY()) {
						int distance = 0;
						for(int a = 0; a < 3; ++a) {
							Block block = to.clone().getBlock().getRelative(0, -a, 0);
							if(block.getType() == Material.AIR) {
								++distance;
							} else {
								distance = 0;
								return;
							}
						}
						if(distance >= 3) {
				        	int counter = 0;
					        if(heightIncreasing.containsKey(player.getName())) {
					        	counter = heightIncreasing.get(player.getName());
					        }
					        if(++counter >= 5) {
					        	heightIncreasing.remove(player.getName());
					        	counter = 0;
					        	if(wouldBan.containsKey(player.getName())) {
					        		counter = wouldBan.get(player.getName());
					        	}
					        	if(++counter >= 3) {
					        		ban(player);
					        	} else {
					        		wouldBan.put(player.getName(), counter);
					        	}
					        } else {
					        	heightIncreasing.put(player.getName(), counter);
					        }
				        }
					} else {
						heightIncreasing.remove(player.getName());
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if(isEnabled() && event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			disable(player, 5);
		}
	}
	
	@EventHandler
	public void onPlayerVelocity(PlayerVelocityEvent event) {
		if(isEnabled()) {
			heightIncreasing.remove(event.getPlayer().getName());
			disable(event.getPlayer(), 10);
		}
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if(isEnabled()) {
			heightIncreasing.remove(event.getPlayer().getName());
			disable(event.getPlayer(), 10);
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		if(isEnabled()) {
			heightIncreasing.remove(event.getPlayer().getName());
			wouldBan.remove(event.getPlayer().getName());
			floating.remove(event.getPlayer().getName());
		}
	}
}
