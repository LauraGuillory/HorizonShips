package com.gmail.Rhisereld.HorizonShips;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.regions.RegionOperationException;

@SuppressWarnings("deprecation")
public class Ship 
{	
	ConfigAccessor data;
	Plugin plugin;
	
	public Ship(ConfigAccessor data, Plugin plugin) 
	{
		this.data = data;
		this.plugin = plugin;
	}

	/**
	 * createShip() creates a new ship to be recorded in the data.yml file. It will have a name, current destination,
	 * a list of destinations (containing only the current destination), and a list of pilots (empty).
	 * It will have a fuel level of zero and will not be broken.
	 * 
	 * @param shipName
	 * @param player
	 * @param destinationName
	 * @throws DataException
	 * @throws IOException
	 * @throws NullPointerException
	 * @throws IllegalArgumentException
	 */
	public void createShip(String shipName, Player player, String destinationName) throws DataException, IOException, NullPointerException, IllegalArgumentException
	{
		SchematicManager sm = new SchematicManager(player.getWorld());
		Selection s = sm.getPlayerSelection(player);
		Location min = s.getMinimumPoint();
		Location max = s.getMaximumPoint();
		int length = max.getBlockX() - min.getBlockX();
		int width = max.getBlockZ() - min.getBlockZ();
		int height = max.getBlockY() - min.getBlockY(); //TODO getBlockX() vs. getX()? One could be more efficient.
		
		Set<String> shipNames = data.getConfig().getConfigurationSection("ships.").getKeys(false);
		for (String sh : shipNames)
			if (sh.equalsIgnoreCase(shipName))
				throw new IllegalArgumentException();
	
		data.getConfig().set("ships." + shipName + ".destinations." + destinationName + ".world", min.getWorld());
		data.getConfig().set("ships." + shipName + ".destinations." + destinationName + ".x", min.getX());
		data.getConfig().set("ships." + shipName + ".destinations." + destinationName + ".y", min.getY());
		data.getConfig().set("ships." + shipName + ".destinations." + destinationName + ".z", min.getZ());
		data.getConfig().set("ships." + shipName + ".currentDestination", destinationName);
		data.getConfig().set("ships." + shipName + ".fuel", 0);
		data.getConfig().set("ships." + shipName + ".broken", false);
		data.getConfig().set("ships." + shipName + ".partRequired", null);
		data.getConfig().set("ships." + shipName + ".partConsumed", false);
		data.getConfig().set("ships." + shipName + ".pilots", new ArrayList<String>());
		data.getConfig().set("ships." + shipName + ".length", length);
		data.getConfig().set("ships." + shipName + ".width", width);
		data.getConfig().set("ships." + shipName + ".height", height);

		data.saveConfig();
		
		sm.saveSchematic(s, shipName, shipName + "\\ship");
	}

	/**
	 * deleteShip() removes all saved information on the given ship.
	 * 
	 * @param player
	 * @param shipName
	 * @throws IllegalArgumentException
	 */
	public void deleteShip(Player player, String shipName) throws IllegalArgumentException, IOException
	{
		Set<String> shipNames = data.getConfig().getConfigurationSection("ships.").getKeys(false);
		boolean shipFound = false;
		
		//Check that the ship exists
		for (String s : shipNames)
			if (s.equalsIgnoreCase(shipName))
				shipFound = true;
		if (!shipFound)
			throw new IllegalArgumentException("Ship not found.");
		
		//Check that the person has permission
		if (!player.hasPermission("horizonShips.admin.delete"))
			throw new IllegalArgumentException("You don't have permission to delete that ship.");
		
		//Delete all information on the ship.
		data.getConfig().getConfigurationSection("ships.").set(shipName, null);
		data.saveConfig();
		
		//Delete ship schematic
		File file = new File(plugin.getDataFolder() + "\\schematics\\" + shipName + "\\ship.schematic");
		file.delete();
		
		//Delete all dock schematics
		//TODO: When dock schematics are implemented.
		
		//Delete directory
		file = new File(plugin.getDataFolder() + "\\schematics\\" + shipName);
		file.delete();	
	}

	public void testDestination(SchematicManager sm, Player player, String shipName, String destinationName) throws MaxChangedBlocksException, DataException, IOException
	{
		Selection s;

		//Check that the player has permission.
		if (!player.hasPermission("horizonShips.admin.destination"))
			throw new IllegalArgumentException("You don't have permission to create a destination!");
		
		//Check that there isn't already a destination by that name.
		if (data.getConfig().getConfigurationSection("ships." + shipName + ".destinations").getKeys(false).contains(shipName))
			throw new IllegalArgumentException("This ship already has a destination by that name.");
		
		s = sm.getPlayerSelection(player);
		sm.loadSchematic(shipName, s, shipName + "\\ship");
	}
	
	public void tweakDestination(SchematicManager sm, Player player, String direction, String shipName) throws MaxChangedBlocksException, DataException, IOException, RegionOperationException, IncompleteRegionException
	{
		Vector dir = null;
		Selection s = sm.getPlayerSelection(player);
		
		//Undo previous paste.
		sm.undoSession();
		
		//Shift 1 block in direction specified.
		switch (direction)
		{
			case "north": 	dir = new Vector(0, 0, -1);
							break;
			case "south": 	dir = new Vector(0, 0, 1);
							break;
			case "east": 	dir = new Vector(1, 0, 0);
							break;
			case "west": 	dir = new Vector(-1, 0, 0);
							break;
			case "up": 		dir = new Vector(0, 1, 0);
							break;
			case "down": 	dir = new Vector(0, -1, 0);
							break;
			default:		throw new IllegalArgumentException("That is not a valid direction.");
							
		}
		sm = new SchematicManager(player.getWorld());
		sm.shiftSelection(player, s, dir);

		//Paste

		sm.loadSchematic(shipName, s, shipName + "\\ship");
	}
	
	public void cancelDestination(SchematicManager sm)
	{
		sm.undoSession();
	}
	
	public void addDestination(SchematicManager sm, Player player, String shipName, String destinationName)
	{
		//Save all the information about the new destination.
		data.getConfig().set("ships." + shipName + ".destinations." + destinationName, sm.getPlayerSelection(player).getMinimumPoint());

		//Undo paste.
		sm.undoSession();
	}
}
