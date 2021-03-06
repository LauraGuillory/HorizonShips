package io.github.LonelyNeptune.HorizonShips;

import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

class Dock
{
	private FileConfiguration data;
	private int id;
	private String destination;
	private Location location;
	private int length;
	private int height;
	private int width;
	private String ship;
	
	// Constructor for fetching a dock from file.
	Dock(FileConfiguration data, String destination, int id) throws IllegalArgumentException
	{
		this.data = data;
		
		if (data.getString("docks." + destination + "." + id + ".world") == null)
			throw new IllegalArgumentException("That dock does not exist.");
		
		this.destination = destination;
		this.id = id;
		this.location = new Location(Bukkit.getWorld(data.getString("docks." + destination + "." + id + ".world")),
										data.getInt("docks." + destination + "." + id + ".x"),
										data.getInt("docks." + destination + "." + id + ".y"),
										data.getInt("docks." + destination + "." + id + ".z"));
		this.length = data.getInt("docks." + destination + "." + id + ".length");
		this.height = data.getInt("docks." + destination + "." + id + ".height");
		this.width = data.getInt("docks." + destination + "." + id + ".width");
		this.ship = data.getString("docks." + destination + "." + id + ".ship");
	}
	
	// Constructor for creating a new dock and adding it to the data file.
	Dock(FileConfiguration data, String destination, Location location, int length, int height, int width)
	{
		this.data = data;
		this.destination = destination;
		this.id = 0;
		
		//Determines the ID number of the new dock. 
		//If there are no docks the new ID will be 0.
		//If any of the docks no longer exist between 0 and i-1 where i is the number of docks,
		//the new dock will take that number.
		//Otherwise, the new ID will be i.
		Set<String> docks = null;
		try { docks = data.getConfigurationSection("docks." + destination).getKeys(false); }
		catch (NullPointerException e) { this.id = 0; }
		
		if (docks != null)
			for (int i = 0; i < docks.size(); i++)
			{
				try { new Dock(data, destination, i); }
				catch (IllegalArgumentException e) { this.id = i; }
			}
		
		setLocation(location);
		setLength(length);
		setHeight(height);
		setWidth(width);
	}
	
	// getID() returns the ID of the dock.
	int getID()
	{
		return id;
	}
	
	// getDestination() returns the destination of the dock.
	String getDestination()
	{
		return destination;
	}
	
	// getLocation() returns the location at the origin of the dock.
	Location getLocation()
	{
		return location;
	}
	
	// setLocation() sets the new location of the origin of the dock.
	private void setLocation(Location location)
	{
		this.location = location;
		data.set("docks." + destination + "." + id + ".world", location.getWorld().getName());
		data.set("docks." + destination + "." + id + ".x", location.getBlockX());
		data.set("docks." + destination + "." + id + ".y", location.getBlockY());
		data.set("docks." + destination + "." + id + ".z", location.getBlockZ());
	}
	
	// getLength() returns the length of the docking space.
	int getLength()
	{
		return length;
	}
	
	// setLength() sets the new length of the docking space.
	private void setLength(int length)
	{
		this.length = length;
		data.set("docks." + destination + "." + id + ".length", length);
	}
	
	// getHeight() returns the height of the docking space.
	int getHeight()
	{
		return height;
	}
	
	// setHeight() sets the new height of the docking space.
	private void setHeight(int height)
	{
		this.height = height;
		data.set("docks." + destination + "." + id + ".height", height);
	}
	
	// getWidth() returns the width of the docking space.
	int getWidth()
	{
		return width;
	}
	
	// setWidth() sets the new width of the docking space.
	private void setWidth(int width)
	{
		this.width = width;
		data.set("docks." + destination + "." + id + ".width", width);
	}
	
	// exists() returns true if the dock was present in the data file. (Relies on the "world" field returning null.)
	boolean exists()
	{
		return location != null;
	}
	
	// delete() removes all information pertaining to this dock. The instance should not continue to be used after this
	// is called.
	void delete()
	{
		data.getConfigurationSection("docks." + destination).set(Integer.toString(this.id), null);
	}
	
	// updateShipName() updates the stored name of the ship that may be inhabiting this location. Should be set to null
	// if there is no ship.
	void updateShipName(String shipName)
	{
		ship = shipName;
		data.set("docks." + destination + "." + id + ".ship", ship);
	}
	
	// getShip() returns the name of the ship that may be inhabiting this location. Should return null if there is no
	// ship.
	String getShip()
	{
		return ship;
	}
}
