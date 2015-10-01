package com.gmail.Rhisereld.HorizonShips;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.entity.Player;

import com.sk89q.worldedit.data.DataException;

@SuppressWarnings("deprecation")
public class Ship 
{	
	ConfigAccessor data;
	
	public Ship(ConfigAccessor data) 
	{
		this.data = data;
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
		sm.saveSchematic(sm.getPlayerSelection(player), shipName, "ships\\");
	}

	public void deleteShip(Player player, String shipName) throws IllegalArgumentException
	{
		Set<String> shipNames = data.getConfig().getConfigurationSection("ships.").getKeys(false);
		boolean shipFound = false;
		
		//Check that the ship exists
		for (String s : shipNames)
			if (s.equalsIgnoreCase(shipName))
				shipFound = true;
		if (!shipFound)
			throw new IllegalArgumentException("Ship not found");
		
		//Check that the person has permission
		if (!player.hasPermission("horizonShips.admin.delete"))
			throw new IllegalArgumentException("You don't have permission to delete that ship.");
		
		//Delete all information on the ship.
		data.getConfig().set("ships." + shipName + ".destinations", 0);
		data.getConfig().set("ships." + shipName + ".currentDestination", 0);
		data.getConfig().set("ships." + shipName + ".fuel", 0);
		data.getConfig().set("ships." + shipName + ".broken", 0);
		data.getConfig().set("ships." + shipName + ".partRequired", 0);
		data.getConfig().set("ships." + shipName + ".partConsumed", 0);
		data.getConfig().set("ships." + shipName + ".pilots", 0);
		data.getConfig().set("ships." + shipName + ".owner", 0);
		
		//Delete ship schematic
		Path path = FileSystems.getDefault().getPath("\\plugins\\HorizonShips\\schematics\\ships\\" + shipName + ".schematic");
		try {
			Files.deleteIfExists(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//Delete all dock schematics
		//TODO: When dock schematics are implemented.
	}
}
