package com.gmail.Rhisereld.HorizonShips;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.data.DataException;

/**
 * This class contains all the information pertaining to a ship. It is controlled by a ShipHandler.
 */
@SuppressWarnings("deprecation")
public class Ship 
{
	private FileConfiguration data;
	private String name;
	private Dock dock;
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
		this.data = data;
		this.name = name;		
		
		dock = new Dock(data, data.getString("ships." + name + ".dock.destination"), data.getInt("ships." + name + ".dock.id"));
		if (dock.getLocation().getWorld() == null) //Ship does not exist.
			return;
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
	public Ship(FileConfiguration data, String name, Selection selection) throws DataException, IOException
	{
		this.data = data;
		
		Location min = selection.getMinimumPoint();
		Location max = selection.getMaximumPoint();
		setName(name);
		setFuel(10);
		setBroken(false);
		setLength(max.getBlockX() - min.getBlockX());
		setWidth(max.getBlockZ() - min.getBlockZ());
		setHeight(max.getBlockY() - min.getBlockY());
		setDock(new Dock(data, "temp", selection.getMinimumPoint(), length, height, width));
		dock.updateShipName(name);
		
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
	 * getDock() returns the dock that the ship is currently inhabiting.
	 * 
	 * @return
	 */
	Dock getDock()
	{
		return dock;
	}
	
	/**
	 * setDock() sets the dock that the ship is currently inhabiting.
	 * 
	 * @param dock
	 */
	void setDock(Dock dock)
	{
		//Keep the ship name updated on the dock so the ship belonging to it can easily be found.
		if (this.dock != null)
			this.dock.updateShipName(null);
		dock.updateShipName(name);
		
		//If the old dock was temporary it should be removed.
		if (this.dock != null && this.dock.getDestination().equalsIgnoreCase("temp"))
		{
			new Destination(data, "temp", true).docks.remove(this.dock.getID());
			this.dock.delete();
		}
			
		//Set the dock
		this.dock = dock;
		data.set("ships." + name + ".dock.destination", dock.getDestination());
		data.set("ships." + name + ".dock.ID", dock.getID());
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
		this.broken = broken;
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
		this.fuel = fuel;
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
		newShip.setDock(dock);
		newShip.setFuel(fuel);
		if (owner != null)
			newShip.setOwner(owner);
		for (UUID p: pilots)
			newShip.addPilot(p);
		newShip.setRepairItem(repairItem);
		data.getConfigurationSection("ships.").set(name, null);
		name = newName;
	}
}
