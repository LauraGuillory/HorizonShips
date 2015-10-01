package com.gmail.Rhisereld.HorizonShips;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldedit.data.DataException;

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
		List<String> destinations = new ArrayList<String>();
		destinations.add(destinationName);
		
		Set<String> shipNames = data.getConfig().getConfigurationSection("ships.").getKeys(false);
		for (String s : shipNames)
			if (s.equalsIgnoreCase(shipName))
				throw new IllegalArgumentException();
		
		data.getConfig().set("ships." + shipName + ".destinations", destinations);
		data.getConfig().set("ships." + shipName + ".currentDestination", destinationName);
		data.getConfig().set("ships." + shipName + ".fuel", 0);
		data.getConfig().set("ships." + shipName + ".broken", false);
		data.getConfig().set("ships." + shipName + ".partRequired", null);
		data.getConfig().set("ships." + shipName + ".partConsumed", false);
		data.getConfig().set("ships." + shipName + ".pilots", new ArrayList<String>());
		
		data.saveConfig();
		
		SchematicManager sm = new SchematicManager(player.getWorld());
		sm.saveSchematic(sm.getPlayerSelection(player), shipName, shipName + "\\ship");
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
}
