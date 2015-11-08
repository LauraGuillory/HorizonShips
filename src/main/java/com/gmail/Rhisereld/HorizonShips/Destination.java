package com.gmail.Rhisereld.HorizonShips;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class Destination 
{
	ConfigAccessor data;
	String shipName;
	String destinationName;
	Location location;

	public Destination(ConfigAccessor data, String shipName, String destinationName)
	{
		this.data = data;
		this.shipName = shipName;
		this.destinationName = destinationName;
		
		location = new Location(Bukkit.getWorld(data.getConfig().getString("ships." + shipName + ".destinations." + destinationName + ".world")),
								data.getConfig().getInt("ships." + shipName + ".destinations." + destinationName + ".x"),
								data.getConfig().getInt("ships." + shipName + ".destinations." + destinationName + ".y"),
								data.getConfig().getInt("ships." + shipName + ".destinations." + destinationName + ".z"));
	}
	
	public Destination(ConfigAccessor data, String shipName, String destinationName, Location location)
	{
		this.data = data;
		this.shipName = shipName;
		this.destinationName = destinationName;
		this.location = location;
		
		data.getConfig().set("ships." + shipName + ".destinations." + ".world", location.getWorld().getName());
		data.getConfig().set("ships." + shipName + ".destinations." + ".x", location.getBlockX());
		data.getConfig().set("ships." + shipName + ".destinations." + ".y", location.getBlockY());
		data.getConfig().set("ships." + shipName + ".destinations." + ".z", location.getBlockZ());
	}
	
	public String getShipName()
	{
		return shipName;
	}
	
	public String getDestinationName()
	{
		return destinationName;
	}
	
	public Location getLocation()
	{
		return location;
	}
	
	public boolean exists()
	{
		if (location.getWorld() == null)
			return false;
		else 
			return true;
	}
}
