package com.gmail.Rhisereld.HorizonShips;

import java.util.ArrayList;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

//Destinations are used to group docks. They do not have a physical location.
//Each dock within a destination is identified by a number.
public class Destination 
{
	String name;
	ArrayList<Dock> docks = new ArrayList<Dock>();
	FileConfiguration data;

	/**
	 * Constructor for destination.
	 * If loadFromFile is false, a new, empty destination will be created.
	 * 
	 * @param data
	 * @param name
	 */
	Destination(FileConfiguration data, String name, boolean loadFromFile) throws IllegalArgumentException
	{
		this.name = name;
		this.data = data;
		
		Set<String> dockNames = null;
		try {dockNames = data.getConfigurationSection("docks.").getKeys(false); }
		catch (NullPointerException e) { }
		
		//New destination
		if (!loadFromFile)
		{
			if (dockNames != null && dockNames.contains(name))
				throw new IllegalArgumentException("A destination already exists by that name.");
			
			data.set("docks." + name + ".exists", true);
			return;
		}
		
		//Existing destination - load docks from data.yml
		//Check that the destination actually exists
		if (dockNames != null && !dockNames.contains(name))
			throw new IllegalArgumentException("That destination does not exist!");
		
		Set<String> docks = null;
		try { docks = data.getConfigurationSection("docks." + name).getKeys(false); }
		catch (NullPointerException e) { return; }
		
		//Add all dock objects on file to the list.
		int ID;
		Dock dock;
		for (String d: docks)
		{
			try { ID = Integer.parseInt(d); }
			catch (NumberFormatException e) { continue; }
			dock = new Dock(data, name, ID);
			if (dock.exists())
				this.docks.set(ID, dock);
		}
	}
	
	/**
	 * numberofDocks() returns the number of docks that this destination currently holds.
	 * 
	 * @return The number of docks that this destination currently holds.
	 */
	int numberOfDocks()
	{
		return docks.size();
	}
	
	/**
	 * addDock() creates a new dock and adds it to the list of docks at this destination.
	 * 
	 * @param location
	 * @param length
	 * @param height
	 * @param width
	 */
	int addDock(Location location, int length, int height, int width)
	{
		Dock dock = new Dock(data, name, location, length, height, width);
		docks.set(dock.getID(), dock);
		return dock.getID();
	}
	
	/**
	 * removeDock() removes the dock that fits the ID given from file.
	 * 
	 * @param ID
	 */
	void removeDock(int ID)
	{
		docks.get(ID).delete();
		docks.remove(ID);
	}
}
