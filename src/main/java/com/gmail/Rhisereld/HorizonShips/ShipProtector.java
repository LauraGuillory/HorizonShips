package com.gmail.Rhisereld.HorizonShips;

import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * ShipProtector listens for block breaking or block placing and prevents these events from occuring within a ship
 * unless the actor is the ship's owner or a permitted pilot.
 * 
 * @author Rhisereld
 */
public class ShipProtector implements Listener
{
	FileConfiguration data;
	
	public ShipProtector(FileConfiguration data)
	{
		this.data = data;
	}
	
	/**
	 * onBreakBlock() is triggered each time a block is broken.
	 * 
	 * @param event
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBreakBlock(BlockBreakEvent event)
	{
		Set<String> destinations;
		Set<String> docks;
		Dock dock;
		Location min;
		Location max;
		Player player = event.getPlayer();
		int x = player.getLocation().getBlockX();
		int y = player.getLocation().getBlockY();
		int z = player.getLocation().getBlockZ();
		Ship ship;
		
		//Get all the destinations. If there are none, return because there are no regions to protect.
		try { destinations = data.getConfigurationSection("docks").getKeys(false); }
		catch (NullPointerException e)
		{ return; }
		
		//Check if the block is in any dock with a ship inside.
		for (String dest: destinations)
		{
			//Get all the docks. If there are none, continue to the next destination.
			try { docks = data.getConfigurationSection("docks." + dest).getKeys(false); }
			catch (NullPointerException e)
			{ continue; }
			
			for (String d: docks)
			{
				if (d.equalsIgnoreCase("exists"))
					continue;
				
				dock = new Dock(data, dest, Integer.parseInt(d));
				
				//If the dock doesn't have a ship inhabiting it, continue to the next dock.
				if (dock.getShip() == null)
					continue;
				
				//If the dock isn't loaded in the world, no point protecting it.
				if (!dock.exists())
					continue;
				
				//Now check if the block is within the ship region.
				min = dock.getLocation();
				max = new Location(min.getWorld(), min.getBlockX() + dock.getLength(), min.getBlockY() + dock.getHeight(),
						min.getBlockZ() + dock.getWidth());
				
				if (min.getWorld().equals(player.getLocation().getWorld()) 
						&& min.getBlockX() <= x && max.getBlockX() >= x
						&& min.getBlockY() <= y && max.getBlockY() >= y
						&& min.getBlockZ() <= z && max.getBlockZ() >= z)
				{
					//If the player isn't the owner or a permitted pilot, notify them and cancel the event.
					ship = new Ship(data, dock.getShip());
					if ((ship.getOwner() != null && !ship.getOwner().equals(player.getUniqueId())) 
							&& !ship.isPilot(player.getUniqueId()) && !player.hasPermission("horizonships.admin.canbuildinsideships"))
					{
						player.sendMessage(ChatColor.RED + "You don't own this ship!");
						event.setCancelled(true);
					}
					//If the player IS, make sure they have access.
					else
						event.setCancelled(false);
				}
			}
		}
	}
	
	/**
	 * onBlockPlace() is triggered every time a block is placed.
	 * 
	 * @param event
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPlace(BlockPlaceEvent event)
	{
		Set<String> destinations;
		Set<String> docks;
		Dock dock;
		Location min;
		Location max;
		Player player = event.getPlayer();
		int x = player.getLocation().getBlockX();
		int y = player.getLocation().getBlockY();
		int z = player.getLocation().getBlockZ();
		Ship ship;
		
		//Get all the destinations. If there are none, return because there are no regions to protect.
		try { destinations = data.getConfigurationSection("docks").getKeys(false); }
		catch (NullPointerException e)
		{ return; }
		
		//Check if the block is in any dock with a ship inside.
		for (String dest: destinations)
		{
			//Get all the docks. If there are none, continue to the next destination.
			try { docks = data.getConfigurationSection("docks." + dest).getKeys(false); }
			catch (NullPointerException e)
			{ continue; }
			
			for (String d: docks)
			{
				int ID;
				try { ID = Integer.parseInt(d); }
				catch (NumberFormatException e) { continue; }
				
				dock = new Dock(data, dest, ID);
				
				//If the dock doesn't have a ship inhabiting it, continue to the next dock.
				if (dock.getShip() == null)
					continue;
				
				//If the dock isn't loaded in the world, no point protecting it.
				if (!dock.exists())
					continue;
				
				//Now check if the block is within the ship region.
				min = dock.getLocation();
				max = new Location(min.getWorld(), min.getBlockX() + dock.getLength(), min.getBlockY() + dock.getHeight(),
						min.getBlockZ() + dock.getWidth());
				
				if (min.getWorld().equals(player.getLocation().getWorld()) 
						&& min.getBlockX() <= x && max.getBlockX() >= x
						&& min.getBlockY() <= y && max.getBlockY() >= y
						&& min.getBlockZ() <= z && max.getBlockZ() >= z)
				{
					//If the player isn't the owner or a permitted pilot, notify them and cancel the event.
					ship = new Ship(data, dock.getShip());
					if ((ship.getOwner() != null && !ship.getOwner().equals(player.getUniqueId())) 
							&& !ship.isPilot(player.getUniqueId()) && !player.hasPermission("horizonships.admin.canbuildinsideships"))
					{
						player.sendMessage(ChatColor.RED + "You don't own this ship!");
						event.setCancelled(true);
					}
					//If the player IS, make sure they have access.
					else
						event.setCancelled(false);
				}
			}
		}
	}
}
