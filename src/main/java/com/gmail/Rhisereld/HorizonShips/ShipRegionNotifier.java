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
 * ShipRegionNotifier monitors all player's locations and notifies them if they enter or leave a region that defines a ship.
 * 
 * @author Rhisereld
 */
public class ShipRegionNotifier implements Listener
{
	FileConfiguration data;
	HashMap<String, String> playersInsideShip = new HashMap<String, String>();
	
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

		//Check if the new destination is inside any of these ships
		Set<String> ships;
		
		try { ships = data.getConfigurationSection("ships").getKeys(false); }
		catch (NullPointerException e)
		{ return; }
		
		Ship ship;
		Location min;
		Location max;
		Player player = event.getPlayer();
		
		//If player is currently inside a ship, detect leaving the region
		if (playersInsideShip.containsKey(player.getName()))
		{			
			ship = new Ship(data, playersInsideShip.get(player.getName()));
			min = ship.getCurrentDestination().getLocation();
			max = new Location(min.getWorld(), min.getBlockX() + ship.getLength(), min.getBlockY() + ship.getHeight(), 
					min.getBlockZ() + ship.getWidth());

			if (!min.getWorld().equals(player.getLocation().getWorld()) 
					|| min.getBlockX() > x || max.getBlockX() < x
					|| min.getBlockY() >= y || max.getBlockY() < y
					|| min.getBlockZ() > z || max.getBlockZ() < z)
			{
				//If so, notify the player
				player.sendMessage(ChatColor.GOLD + "Now leaving ship: " + ship.getName());
				//Remove player from list of players inside this ship
				playersInsideShip.remove(player.getName());
			}
		}
		//If player is not currently inside a ship, detect entering the region
		else
		{	
			for (String s: ships)
			{
				ship = new Ship(data, s);
				min = ship.getCurrentDestination().getLocation();
				max = new Location(min.getWorld(), min.getBlockX() + ship.getLength(), min.getBlockY() + ship.getHeight(), 
						min.getBlockZ() + ship.getWidth());
				
				if (min.getWorld().equals(player.getLocation().getWorld()) 
						&& min.getBlockX() <= x && max.getBlockX() >= x
						&& min.getBlockY() < y && max.getBlockY() >= y
						&& min.getBlockZ() < z && max.getBlockZ() >= z)
				{
					//If so, notify the player
					player.sendMessage(ChatColor.GOLD + "Now entering ship: " + ship.getName());
					//Add player to list of players inside this ship
					playersInsideShip.put(player.getName(), ship.getName());
					//No need to check the rest if it's found.
					break;
				}
			}
		}
	}
}
