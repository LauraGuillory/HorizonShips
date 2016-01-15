package com.gmail.Rhisereld.HorizonShips;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.data.DataException;

/**
 * This class contains all the information pertaining to a ship. It is controlled by a ShipHandler
 * and always has one or more Destinations.
 */
@SuppressWarnings("deprecation")
public class Ship 
{
	private FileConfiguration data;
	private String name;
	private Destination currentDestination;
	private Set<String> destinations = new HashSet<String>();
	private int fuel;
	private boolean broken;
	private String repairItem;
	private boolean consumePart;
	private List<UUID> pilots = new ArrayList<UUID>();
	private int length;
	private int width;
	private int height;
	private UUID owner;
	
	/**
	 * Constructor for fetching an existing ship from the data file.
	 * 
	 * @param data
	 * @param name
	 */
	public Ship(FileConfiguration data, String name)
	{
		String path = "ships." + name + ".";
		currentDestination = new Destination(data, name, data.getString(path + "currentDestination"));
		this.data = data;
		
		if (currentDestination.getName() == null) //Ship does not exist.
			return;
		
		this.name = name;
		try { destinations = data.getConfigurationSection(path + "destinations").getKeys(false); }
		catch (NullPointerException e) { }
		fuel = data.getInt(path + "fuel");
		broken = data.getBoolean(path + "broken");
		repairItem = data.getString(path + "repairItem");
		consumePart = data.getBoolean(path + "consumePart");
		List<String> pilotsString = data.getStringList(path + "pilots");
		for (String s: pilotsString)
			pilots.add(UUID.fromString(s));
		length = data.getInt(path + "length");
		width = data.getInt(path + "width");
		height = data.getInt(path + "height");
		if (data.getString(path + "owner") != null)
			owner = UUID.fromString(data.getString(path + "owner"));
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
	public Ship(FileConfiguration data, String name, String destinationName, Selection selection) throws DataException, IOException
	{
		this.data = data;
		this.name = name;
		currentDestination = new Destination(data, name, destinationName, selection.getMinimumPoint());
		destinations.add(destinationName);
		fuel = 10;
		broken = false;
		pilots = new ArrayList<UUID>();
		
		Location min = selection.getMinimumPoint();
		Location max = selection.getMaximumPoint();
		length = max.getBlockX() - min.getBlockX();
		width = max.getBlockZ() - min.getBlockZ();
		height = max.getBlockY() - min.getBlockY();
		
		data.set("ships." + name + ".currentDestination", destinationName);
		data.set("ships." + name + ".fuel", fuel);
		data.set("ships." + name + ".broken", broken);
		data.set("ships." + name + ".pilots", pilots);
		data.set("ships." + name + ".length", length);
		data.set("ships." + name + ".width", width);
		data.set("ships." + name + ".height", height);
		
		SchematicManager sm = new SchematicManager(selection.getMinimumPoint().getWorld());
		sm.saveSchematic(selection, name + "\\ship");
	}
	
	/**
	 * deleteShip() removes all the information about the ship and its destinations.
	 * The instance or its destinations should NOT continue to be used after this is called.
	 */
	void deleteShip()
	{
		data.set("ships." + name, null);
		
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
	Destination getDestination(String destinationName)
	{
		return new Destination(data, name, destinationName);
	}
	
	/**
	 * getAllDestinations() returns a list of all available destinations.
	 * 
	 * @return
	 */
	Set<String> getAllDestinations()
	{
		return destinations;
	}
	
	/**
	 * addDestination() creates, saves and returns a new destination.
	 * 
	 * @param destinationName
	 * @param location
	 */
	Destination addDestination(String destinationName, Location location)
	{
		return new Destination(data, name, destinationName, location);
	}
	
	/**
	 * removeDestination() removes the destination given and all its information from this ship.
	 * 
	 * @param destinationName
	 */
	void removeDestination(String destinationName)
	{
		Destination destination = new Destination(data, name, destinationName);
		destination.delete();
	}
	
	/**
	 * getCurrentDestination() returns the current destination.
	 * 
	 * @return
	 */
	Destination getCurrentDestination()
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
	boolean setCurrentDestination(String destination)
	{
		currentDestination = new Destination(data, name, destination);
		if (currentDestination == null)
			return false;
		data.set("ships." + name + ".currentDestination", destination);
		
		return true;
	}
	
	/**
	 * isBroken() returns true if the ship is broken, false otherwise.
	 * 
	 * @return
	 */
	boolean isBroken()
	{
		return broken;
	}
	
	/**
	 * setBroken() sets the broken status of the ship.
	 * 
	 * @param broken
	 */
	void setBroken(boolean broken)
	{
		data.set("ships." + name + ".broken", broken);
	}

	/**
	 * getLength() returns the length of the ship.
	 * 
	 * @return
	 */
	int getLength()
	{
		return length;
	}
	
	/**
	 * setLength() sets the length of the ship.
	 * 
	 * @param length
	 */
	void setLength(int length)
	{
		this.length = length;
		data.set("ships." + name + ".length", length);
	}
	
	/**
	 * getWidth() returns the width of the ship.
	 * 
	 * @return
	 */
	int getWidth()
	{
		return width;
	}
	
	/**
	 * setWidth() sets the length of the ship.
	 * 
	 * @param length
	 */
	void setWidth(int width)
	{
		this.width = width;
		data.set("ships." + name + ".width", width);
	}
	
	/**
	 * getHeight() returns the height of the ship.
	 * 
	 * @return
	 */
	int getHeight()
	{
		return height;
	}
	
	/**
	 * setHeight() sets the height of the ship.
	 * 
	 * @param height
	 */
	void setHeight(int height)
	{
		this.height = height;
		data.set("ships." + name + ".height", height);
	}
	
	/**
	 * getFuel() returns the fuel level of the ship.
	 * 
	 * @return
	 */
	int getFuel()
	{
		return fuel;
	}
	
	/**
	 * setFuel() sets the fuel level of the ship.
	 * 
	 * @param fuel
	 */
	void setFuel(int fuel)
	{
		data.set("ships." + name + ".fuel", fuel);
	}
	
	/**
	 * reduceFuel() reduces the fuel level by one.
	 * 
	 */
	void reduceFuel()
	{
		data.set("ships." + name + ".fuel", --fuel);
	}
	
	/**
	 * getName() returns the name of the ship.
	 * 
	 * @return
	 */
	String getName()
	{
		return name;
	}
	
	/**
	 * setName() sets the name of the ship.
	 * To be used only while renaming ships. Setting the name without transferring all data
	 * will result in lost ship data.
	 * 
	 * @return
	 */
	void setName(String newName)
	{
		name = newName;
	}
	
	/**
	 * getRepairItem() returns the item required to repair the ship.
	 * 
	 * @return
	 */
	String getRepairItem()
	{
		return repairItem;
	}
	
	/**
	 * setRepairItem() sets the item required to repair the ship.
	 * 
	 * @param item
	 */
	void setRepairItem(String item)
	{
		this.repairItem = item;
		data.set("ships." + name + ".repairItem", item);
	}
	
	/**
	 * getConsumePart() returns whether the item required to repair the ship
	 * is confumsed upon repair
	 * @return 
	 * 
	 * @return
	 */
	boolean getConsumePart()
	{
		return consumePart;
	}
	
	/**
	 * setConsumePart() sets whether the item required to repair the ship is
	 * consumed upon repair.
	 * 
	 * @param consumePart
	 */
	void setConsumePart(boolean consumePart)
	{
		this.consumePart = consumePart;
		data.set("ships." + name + ".consumePart", consumePart);
	}
	
	/**
	 * getPilots() returns a list of the UUIDs of the players who are permitted to fly the ship.
	 * 
	 * @return
	 */
	List<UUID> getPilots()
	{
		return pilots;
	}
	
	/**
	 * isPilot() checks if the Player provided is allowed to pilot the ship.
	 * 
	 * @param player
	 * @return
	 */
	boolean isPilot(UUID player)
	{
		List <String> pilots = data.getStringList("ships." + name + ".pilots");
		
		for (String p : pilots)
			if (player.toString().equalsIgnoreCase(p))
				return true;
		return false;
	}
	
	/**
	 * addPilot() adds the uuid given to the list of permitted pilots for the ship.
	 * 
	 * @param uuid
	 */
	
	void addPilot(UUID uuid)
	{
		pilots.add(uuid);
		
		List<String> pilotStrings = new ArrayList<String>();
		
		for (UUID u: pilots)
			pilotStrings.add(u.toString());
		
		data.set("ships." + name + ".pilots", pilotStrings);
	}
	
	void removePilot(UUID uuid)
	{
		pilots.remove(uuid);
		
		List<String> pilotStrings = new ArrayList<String>();
		
		for (UUID u: pilots)
			pilotStrings.add(u.toString());
		
		data.set("ships." + name + ".pilots", pilotStrings);
	}
	
	/**
	 * getOwner() returns the UUID of the player who is considered to own the ship.
	 * 
	 * @return
	 */
	UUID getOwner()
	{
		return owner;
	}
	
	void setOwner(UUID uuid)
	{
		owner = uuid;
		data.set("ships." + name + ".owner", uuid.toString());
	}
	
	/**
	 * rename() takes all of the data under the old ship name and transfers it to a new ship name.
	 * 
	 * @param newName
	 */
	void rename(String newName)
	{
		Ship newShip = new Ship(data, newName);
		newShip.setName(newName);
		newShip.setLength(length);
		newShip.setHeight(height);
		newShip.setWidth(width);
		newShip.setBroken(broken);
		newShip.setConsumePart(consumePart);
		newShip.setCurrentDestination(currentDestination.getName());
		newShip.setFuel(fuel);
		if (owner != null)
			newShip.setOwner(owner);
		newShip.setRepairItem(repairItem);
		for (String d: destinations)
			newShip.addDestination(d, getDestination(d).getLocation());
		data.getConfigurationSection("ships.").set(name, null);
		name = newName;
	}
}
