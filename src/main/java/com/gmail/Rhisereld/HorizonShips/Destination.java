package com.gmail.Rhisereld.HorizonShips;

import java.util.ArrayList;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

//Destinations are used to group docks. They do not have a physical location.
//Each dock within a destination is identified by a number.
public class Destination 
{
	private String name;
	private ArrayList<Dock> docks = new ArrayList<Dock>();
	private FileConfiguration data;

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
		if (!data.getBoolean("docks." + name + ".exists"))
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
				if (this.docks.size() > ID)
					this.docks.set(ID, dock);
				else
					this.docks.add(dock);
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
	Dock addDock(Location location, int length, int height, int width) throws IllegalArgumentException
	{
		Dock dock = new Dock(data, name, location, length, height, width);
		if (this.docks.size() > dock.getID())
			this.docks.set(dock.getID(), dock);
		else
			this.docks.add(dock);
		return dock;
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
	
	/**
	 * getDock() returns the dock at this destination with the ID given.
	 * 
	 * @param ID
	 * @return
	 */
	Dock getDock(int ID)
	{
		return docks.get(ID);
	}
	
	/**
	 * getName() returns the name of the destination.
	 * 
	 * @return
	 */
	String getName()
	{
		return name;
	}
	
	/**
	 * getDocks() returns all docks in this destination.
	 * 
	 * @return
	 */
	ArrayList<Dock> getDocks()
	{
		return docks;
	}
	
	/**
	 * delete() removes the destination and all contained docks from file.
	 * The instance should not continue to be used after this is called.
	 */
	void delete()
	{
		data.getConfigurationSection("docks").set(name, null);
	}
}
