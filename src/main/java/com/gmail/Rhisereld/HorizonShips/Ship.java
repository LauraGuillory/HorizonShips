package com.gmail.Rhisereld.HorizonShips;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.data.DataException;

/**
 * This class contains all the information pertaining to a ship. It is controlled by a ShipHandler
 * and always has one or more Destinations.
 */
@SuppressWarnings("deprecation")
public class Ship 
{
	private ConfigAccessor data;
	private String name;
	private Destination currentDestination;
	private Set<String> destinations = new HashSet<String>();
	private int fuel;
	private boolean broken;
	private String repairItem;
	private boolean consumePart;
	private List<String> pilots = new ArrayList<String>();
	private int length;
	private int width;
	private int height;
	
	/**
	 * Constructor for fetching an existing ship from the data file.
	 * 
	 * @param data
	 * @param name
	 */
	public Ship(ConfigAccessor data, String name)
	{
		String path = "ships." + name + ".";
		
		this.data = data;
		this.name = name;
		currentDestination = new Destination(data, name, data.getConfig().getString(path + "currentDestination"));
		destinations = data.getConfig().getConfigurationSection(path + "destinations").getKeys(false);
		fuel = data.getConfig().getInt(path + "fuel");
		broken = data.getConfig().getBoolean(path + "broken");
		repairItem = data.getConfig().getString(path + "repairItem");
		consumePart = data.getConfig().getBoolean(path + consumePart);
		pilots = data.getConfig().getStringList(path + "pilots");
		length = data.getConfig().getInt(path + "length");
		width = data.getConfig().getInt(path + "width");
		height = data.getConfig().getInt(path + "height");
		
	}

	/**
	 * Constructor for creating an entirely new ship.
	 * 
	 * @param data
	 * @param name
	 * @param destinationName
	 * @param selection
	 * @param location
	 * @param length
	 * @param width
	 * @param height
	 * @throws DataException
	 * @throws IOException
	 */
	public Ship(ConfigAccessor data, String name, String destinationName, Selection selection) throws DataException, IOException
	{
		this.data = data;
		this.name = name;
		currentDestination = new Destination(data, name, destinationName, selection.getMinimumPoint());
		destinations.add(destinationName);
		fuel = 10;
		broken = false;
		pilots = new ArrayList<String>();
		
		Location min = selection.getMinimumPoint();
		Location max = selection.getMaximumPoint();
		length = max.getBlockX() - min.getBlockX();
		width = max.getBlockZ() - min.getBlockZ();
		height = max.getBlockY() - min.getBlockY();
		
		data.getConfig().set("ships." + name + ".currentDestination", destinationName);
		data.getConfig().set("ships." + name + ".fuel", fuel);
		data.getConfig().set("ships." + name + ".broken", broken);
		data.getConfig().set("ships." + name + ".pilots", pilots);
		data.getConfig().set("ships." + name + ".length", length);
		data.getConfig().set("ships." + name + ".width", width);
		data.getConfig().set("ships." + name + ".height", height);
		data.saveConfig();
		
		SchematicManager sm = new SchematicManager(selection.getMinimumPoint().getWorld());
		sm.saveSchematic(selection, name + "\\", "ship");
	}
	
	/**
	 * deleteShip() removes all the information about the ship and its destinations.
	 * The instance or its destinations should NOT continue to be used after this is called.
	 */
	public void deleteShip()
	{
		data.getConfig().getConfigurationSection("ships.").set(name, null);
		data.saveConfig();
		
		//Delete ship schematic
		SchematicManager sm = new SchematicManager(Bukkit.getWorld("world"));
		sm.deleteSchematic(name + "\\", "ship");
	}
	
	/**
	 * getDestination() attempts to return a Destination object identified by its string name.
	 * Not case sensitive. Returns null upon fail.
	 * 
	 * @param destinationName
	 * @return
	 */
	public Destination getDestination(String destinationName)
	{
		return new Destination(data, name, destinationName);
	}
	
	/**
	 * addDestination() creates, saves and returns a new destination.
	 * 
	 * @param destinationName
	 * @param location
	 */
	public Destination addDestination(String destinationName, Location location)
	{
		return new Destination(data, name, destinationName, location);
	}
	
	/**
	 * getCurrentDestination() returns the current destination.
	 * 
	 * @return
	 */
	public Destination getCurrentDestination()
	{
		return currentDestination;
	}
	
	/**
	 * setCurrentDestination() attempts to fetch the destination by string name, and sets the current
	 * destination to the destination fetched.
	 * Not case sensitive.
	 * Returns false upon fail.
	 * 
	 * @param destination
	 */
	public boolean setCurrentDestination(String destination)
	{
		currentDestination = new Destination(data, name, destination);
		if (currentDestination == null)
			return false;
		data.getConfig().set("ships." + name + ".currentDestination", destination);
		data.saveConfig();
		
		return true;
	}
	
	/**
	 * isPilot() checks if the Player provided is allowed to pilot the ship.
	 * 
	 * @param player
	 * @return
	 */
	public boolean isPilot(UUID player)
	{
		List <String> pilots = data.getConfig().getStringList("ships." + name + ".pilots");
		
		for (String p : pilots)
			if (player.toString().equalsIgnoreCase(p))
				return true;
		return false;
	}
	
	/**
	 * isBroken() returns true if the ship is broken, false otherwise.
	 * 
	 * @return
	 */
	public boolean isBroken()
	{
		return broken;
	}
	
	/**
	 * setBroken() sets the broken status of the ship.
	 * 
	 * @param broken
	 */
	public void setBroken(boolean broken)
	{
		data.getConfig().set("ships." + name + ".broken", broken);
		data.saveConfig();
	}

	/**
	 * getLength() returns the length of the ship.
	 * 
	 * @return
	 */
	public int getLength()
	{
		return length;
	}
	
	/**
	 * getWidth() returns the width of the ship.
	 * 
	 * @return
	 */
	public int getWidth()
	{
		return width;
	}
	
	/**
	 * getHeight() returns the height of the ship.
	 * 
	 * @return
	 */
	public int getHeight()
	{
		return height;
	}
	
	/**
	 * getFuel() returns the fuel level of the ship.
	 * 
	 * @return
	 */
	public int getFuel()
	{
		return fuel;
	}
	
	/**
	 * setFuel() sets the fuel level of the ship.
	 * 
	 * @param fuel
	 */
	public void setFuel(int fuel)
	{
		data.getConfig().set("ships." + name + ".fuel", fuel);
		data.saveConfig();
	}
	
	/**
	 * reduceFuel() reduces the fuel level by one.
	 * 
	 */
	public void reduceFuel()
	{
		data.getConfig().set("ships." + name + ".fuel", --fuel);
		data.saveConfig();
	}
	
	/**
	 * getName() returns the name of the ship.
	 * 
	 * @return
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * getRepairItem() returns the item required to repair the ship.
	 * 
	 * @return
	 */
	public String getRepairItem()
	{
		return repairItem;
	}
	
	/**
	 * setRepairItem() sets the item required to repair the ship.
	 * 
	 * @param item
	 */
	public void setRepairItem(String item)
	{
		this.repairItem = item;
		data.getConfig().set("ships." + name + ".repairItem", item);
		data.saveConfig();
	}
	
	/**
	 * getConsumePart() returns whether the item required to repair the ship
	 * is confumsed upon repair
	 * @return 
	 * 
	 * @return
	 */
	public boolean getConsumePart()
	{
		return consumePart;
	}
	
	/**
	 * setConsumePart() sets whether the item required to repair the ship is
	 * consumed upon repair.
	 * 
	 * @param consumePart
	 */
	public void setConsumePart(boolean consumePart)
	{
		this.consumePart = consumePart;
		data.getConfig().set("ships." + name + ".consumePart", consumePart);
		data.saveConfig();
	}
}
