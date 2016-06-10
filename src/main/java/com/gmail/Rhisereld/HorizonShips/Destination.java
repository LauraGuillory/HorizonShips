package com.gmail.Rhisereld.HorizonShips;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

//Destinations are used to group docks. They do not have a physical location.
//Each dock within a destination is identified by a number.
public class Destination 
{
	private String name;
	private Set<Integer> docks = new HashSet<Integer>();
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
		
		Set<String> dockList;
		try { dockList = data.getConfigurationSection("docks." + name).getKeys(false); }
		catch (NullPointerException e) { return; }
		
		//Add all dock objects on file to the list.
		int ID;
		Dock dock;
		for (String d: dockList)
		{
			try { ID = Integer.parseInt(d); }
			catch (NumberFormatException e) { continue; }
			dock = new Dock(data, name, ID);
			if (dock.exists())
					this.docks.add(dock.getID());
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
		docks.add(dock.getID());
		return dock;
	}
	
	/**
	 * removeDock() removes the dock that fits the ID given from file.
	 * 
	 * @param ID
	 */
	void removeDock(int ID) throws IllegalArgumentException
	{
		if (docks.contains(ID))
		{
			Dock dock = new Dock(data, name, ID);
			dock.delete();
			docks.remove(ID);
		}
		else
			throw new IllegalArgumentException("That dock does not exist!");
	}
	
	/**
	 * getDock() returns the dock at this destination with the ID given.
	 * 
	 * @param ID
	 * @return
	 */
	Dock getDock(int ID)
	{
		return new Dock(data, name, ID);
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
		ArrayList<Dock> dockList = new ArrayList<Dock>();
		
		for (Integer d: docks)
			dockList.add(new Dock(data, name, d));
		return dockList;
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
