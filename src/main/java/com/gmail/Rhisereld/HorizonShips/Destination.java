package com.gmail.Rhisereld.HorizonShips;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * This class contains all the information pertaining to a single destination.
 * A ship always has one or more destinations.
 */
public class Destination 
{
	private FileConfiguration data;
	private String shipName;
	private String destinationName;
	private Location location;

	/**
	 * Constructor for fetching an existing destination from the data file.
	 * 
	 * @param data
	 * @param shipName
	 * @param destinationName
	 */
	public Destination(FileConfiguration data, String shipName, String destinationName)
	{
		if (!data.contains("ships." + shipName + ".destinations"))
			return;
		
		Set<String> destinations;
		try { destinations = data.getConfigurationSection("ships." + shipName + ".destinations").getKeys(false); }
		catch (NullPointerException e)
		{ return; }
		
		boolean foundDestination = false;
		
		for (String d: destinations)
			if (d.equalsIgnoreCase(destinationName))
			{
				destinationName = d;	//configuration fetching is case sensitive - use string from config for case insensitivity
				foundDestination = true;
			}
		
		if (!foundDestination)
			return;
		
		this.shipName = shipName;
		this.destinationName = destinationName;
		this.data = data;
		location = new Location(Bukkit.getWorld(data.getString("ships." + shipName + ".destinations." + destinationName + ".world")),
								data.getInt("ships." + shipName + ".destinations." + destinationName + ".x"),
								data.getInt("ships." + shipName + ".destinations." + destinationName + ".y"),
								data.getInt("ships." + shipName + ".destinations." + destinationName + ".z"));
	}
	
	/**
	 * Constructor for creating an entirely new destination and adding it to the data file.
	 * 
	 * @param data
	 * @param shipName
	 * @param destinationName
	 * @param location
	 */
	public Destination(FileConfiguration data, String shipName, String destinationName, Location location)
	{
		this.shipName = shipName;
		this.destinationName = destinationName;
		this.location = location;
		this.data = data;
		
		data.set("ships." + shipName + ".destinations." + destinationName + ".world", location.getWorld().getName());
		data.set("ships." + shipName + ".destinations." + destinationName + ".x", location.getBlockX());
		data.set("ships." + shipName + ".destinations." + destinationName + ".y", location.getBlockY());
		data.set("ships." + shipName + ".destinations." + destinationName + ".z", location.getBlockZ());
	}
	
	/**
	 * delete() removes all information pertaining to this destination.
	 * The instance should not continue to be used after this is called.
	 * 
	 */
	void delete()
	{
		data.getConfigurationSection("ships." + shipName + ".destinations.").set(destinationName, null);
	}
	
	/**
	 * getShipName() returns the name of the ship to which this destination pertains to.
	 * 
	 * @return
	 */
	String getShipName()
	{
		return shipName;
	}
	
	/**
	 * getDestinationName() returns the name of the destination.
	 * 
	 * @return
	 */
	String getName()
	{
		return destinationName;
	}
	
	/**
	 * getLocation() returns the location of the destination.
	 * 
	 * @return
	 */
	Location getLocation()
	{
		return location;
	}
	
	/**
	 * exists() returns true if the destination was present in the data value.
	 * (Relies on the "world" field in the data value returning null.)
	 * 
	 * @return
	 */
	boolean exists()
	{
		if (location.getWorld() == null)
			return false;
		else 
			return true;
	}
}
