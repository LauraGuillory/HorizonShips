package com.gmail.Rhisereld.HorizonShips;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

public class Dock 
{
	private FileConfiguration data;
	private String name;
	private Location location;
	private int length;
	private int height;
	private int width;
	
	/**
	 * Constructor for fetching a dock from file.
	 * 
	 * @param data
	 * @param name
	 */
	Dock(FileConfiguration data, String name)
	{
		this.name = name;
		this.location = new Location(Bukkit.getWorld(data.getString("docks." + name + ".world")),
										data.getInt("docks." + name + ".x"),
										data.getInt("docks." + name + ".y"),
										data.getInt("docks." + name + ".z"));
		this.length = data.getInt("docks." + name + ".length");
		this.height = data.getInt("docks." + name + ".height");
		this.width = data.getInt("docks." + name + ".width");
	}
	
	/**
	 * Constructor for creating a new dock and adding it to the data file.
	 * 
	 * @param data
	 * @param location
	 * @param length
	 * @param height
	 * @param width
	 */
	Dock(FileConfiguration data, Location location, int length, int height, int width)
	{
		this.location = location;
		this.length = length;
		this.height = height;
		this.width = width;
	}
	
	/**
	 * getName() returns the name of the dock.
	 * 
	 * @param name
	 * @return
	 */
	String getName()
	{
		return name;
	}

	/**
	 * setName() changes the name of the dock.
	 * 
	 * @param name
	 */
	void setName(String name)
	{
		data.set("docks." + name + ".world", location.getWorld());
		data.set("docks." + name + ".x", location.getX());
		data.set("docks." + name + ".y", location.getY());
		data.set("docks." + name + ".z", location.getZ());
		data.set("docks." + name + ".length", length);
		data.set("docks." + name + ".height", height);
		data.set("docks." + name + ".width", width);
		
		data.getConfigurationSection("docks.").set(this.name, null);
		this.name = name;
	}
	
	/**
	 * getLocation() returns the location at the origin of the dock.
	 * 
	 * @return
	 */
	Location getLocation()
	{
		return location;
	}
	
	/**
	 * setLocation() sets the new location of the origin of the dock.
	 * 
	 * @param location
	 */
	void setLocation(Location location)
	{
		this.location = location;
	}
	
	/**
	 * getLength() returns the length of the docking space.
	 * 
	 * @return
	 */
	int getLength()
	{
		return length;
	}
	
	/**
	 * setLength() sets the new length of the docking space.
	 * 
	 * @param length
	 */
	void setLength(int length)
	{
		this.length = length;
	}
	
	/**
	 * getHeight() returns the height of the docking space.
	 * 
	 * @return
	 */
	int getHeight()
	{
		return height;
	}
	
	/**
	 * setHeight() sets the new height of the docking space.
	 * 
	 * @param height
	 */
	void setHeight(int height)
	{
		this.height = height;
	}
	
	/**
	 * getWidth() returns the width of the docking space.
	 * 
	 * @return
	 */
	int getWidth()
	{
		return width;
	}
	
	/**
	 * setWidth() sets the new width of the docking space.
	 * 
	 * @param width
	 */
	void setWidth(int width)
	{
		this.width = width;
	}
	
	/**
	 * exists() returns true if the dock was present in the data file.
	 * (Relies on the "world" field returning null.)
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
	
	/**
	 * delete() removes all information pertaining to this dock.
	 * The instance should not continue to be used after this is called.
	 * 
	 */
	void delete()
	{
		data.getConfigurationSection("docks.").set(this.name, null);
	}
}
