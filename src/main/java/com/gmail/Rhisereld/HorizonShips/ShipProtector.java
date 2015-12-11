package com.gmail.Rhisereld.HorizonShips;

import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class ShipProtector implements Listener
{
	ConfigAccessor data;
	ConfigAccessor config;
	
	public ShipProtector(ConfigAccessor data, ConfigAccessor config)
	{
		this.data = data;
		this.config = config;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBreakBlock(BlockBreakEvent event)
	{
		Set<String> ships = data.getConfig().getConfigurationSection("ships").getKeys(false);
		
		Ship ship;
		Location min;
		Location max;
		Player player = event.getPlayer();
		int x = event.getBlock().getX();
		int y = event.getBlock().getY();
		int z = event.getBlock().getZ();
		
		//Check if the block is in any destination of any ship.
		for (String s: ships)
		{
			ship = new Ship(data, s);
			Set<String> destinations = ship.getAllDestinations();
			
			for (String d: destinations)
			{
				min = ship.getDestination(d).getLocation();
				max = new Location(min.getWorld(), min.getBlockX() + ship.getLength(), min.getBlockY() + ship.getHeight(), 
						min.getBlockY() + ship.getWidth());
				
				if (min.getWorld().equals(player.getLocation().getWorld()) 
						&& min.getBlockX() < x && max.getBlockX() > x
						&& min.getBlockY() < y && max.getBlockY() > y
						&& min.getBlockZ() < z && max.getBlockZ() > z)
				{
					//If the player isn't the owner or a permitted pilot, notify them and cancel the event.
					if ((ship.getOwner() == null || ship.getOwner().equals(player.getUniqueId())) || ship.isPilot(player.getUniqueId()))
					{
						player.sendMessage(ChatColor.RED + "You don't own this ship!");
						event.setCancelled(true);
					}
						
				}
			}
			

		}
	}
}
