package com.gmail.Rhisereld.HorizonShips;

import java.util.HashMap;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * ShipRegionNotifier monitors all player's locations and notifies them if they enter or leave a region that defines a dock.
 * 
 * @author Rhisereld
 */
public class ShipRegionNotifier implements Listener
{
	FileConfiguration data;
	HashMap<String, String> playersInsideDocks = new HashMap<String, String>();
	
	public ShipRegionNotifier(FileConfiguration data)
	{
		this.data = data;
	}
	
	/**
	 * onPlayerMove() is triggered every time a player moves.
	 * 
	 * @param event
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerMove(PlayerMoveEvent event)
	{
		int x = event.getTo().getBlockX();
		int z = event.getTo().getBlockZ();	
		int y = event.getTo().getBlockY();
		
		//If they haven't left the block, return immediately.
		if (event.getFrom().getBlockX() == x && event.getFrom().getBlockY() == y && event.getFrom().getBlockZ() == z)
			return;

		//Check if the new destination is inside any docks
		Set<String> destinations;
		Set<String> docks;
		
		try { destinations = data.getConfigurationSection("docks.").getKeys(false); }
		catch (NullPointerException e)
		{ return; }
		
		Location min;
		Location max;
		Player player = event.getPlayer();
		
		//If player is currently inside a ship, detect leaving the region
		if (playersInsideDocks.containsKey(player.getName()))
		{			
			String[] arguments = playersInsideDocks.get(player.getName()).split(" ");
			Dock dock = new Dock(data, arguments[0], Integer.parseInt(arguments[1]));
			min = dock.getLocation();
			max = new Location(min.getWorld(), min.getBlockX() + dock.getLength(), min.getBlockY() + dock.getHeight(), 
					min.getBlockZ() + dock.getWidth());
			
			//If the dock isn't in a world that's loaded, how is the player inside it?
			if (!dock.exists())
			{
				playersInsideDocks.remove(player.getName());
				return;
			}

			//Check if the player has left the dock region.
			if (!min.getWorld().equals(player.getLocation().getWorld()) 
					|| min.getBlockX() > x || max.getBlockX() < x
					|| min.getBlockY() >= y || max.getBlockY() < y
					|| min.getBlockZ() > z || max.getBlockZ() < z)
			{
				//If so, notify the player
				player.sendMessage(ChatColor.GOLD + "Now leaving dock " + dock.getID() + " at destination " 
				+ dock.getDestination() + ".");
				//Remove player from list of players inside this dock
				playersInsideDocks.remove(player.getName());
			}
		}
		//If player is not currently inside a dock, detect entering the region
		else
		{	
			for (String dest: destinations)
			{
				//Get the docks at this destination. If there are no docks, continue to the next destination immediately.
				try { docks = data.getConfigurationSection("docks." + dest).getKeys(false); }
				catch (NullPointerException e)
				{ continue; }
				
				Dock dock;
				
				//Go through each dock.
				for (String d: docks)
				{
					dock = new Dock(data, dest, Integer.parseInt(d));
					min = dock.getLocation();
					max = new Location(min.getWorld(), min.getBlockX() + dock.getLength(), min.getBlockY() + dock.getHeight(), 
							min.getBlockZ() + dock.getWidth());
					
					//If the dock isn't in a world that's loaded, skip it.
					if (!dock.exists())
						continue;
					
					//Check if the player is within the region.
					if (min.getWorld().equals(player.getLocation().getWorld()) 
							&& min.getBlockX() <= x && max.getBlockX() >= x
							&& min.getBlockY() < y && max.getBlockY() >= y
							&& min.getBlockZ() <= z && max.getBlockZ() >= z)
					{
						//If so, notify the player
						player.sendMessage(ChatColor.GOLD + "Now entering dock " + dock.getID() + " at destination " 
											+ dest + ".");
						//If a ship is docked here, notify the player of that as well.
						if (dock.getShip() != null)
						{
							Ship ship = new Ship(data, dock.getShip());
							player.sendMessage(ChatColor.GOLD + "The ship " + ship.getName() + " is docked here.");
						}
						//Add player to list of players inside this ship
						playersInsideDocks.put(player.getName(), dest + dock.getID());
						//No need to check the rest if it's found.
						break;
					}
				}
			}
		}
	}
}
