package com.gmail.Rhisereld.HorizonShips;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;

import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.data.DataException;

@SuppressWarnings("deprecation")
public class Ship 
{
	private ConfigAccessor data;
	private String name;
	private Destination currentDestination;
	private Set<String> destinations;
	private int fuel;
	private boolean broken;
	private Material partRequired;
	private boolean consumePart;
	private List<String> pilots;
	private int length;
	private int width;
	private int height;
	
	public Ship(ConfigAccessor data, String name)
	{
		String path = "ships." + name + ".";
		
		this.data = data;
		this.name = name;
		this.currentDestination = new Destination(data, name, data.getConfig().getString(path + "currentDestination"));
		this.destinations = data.getConfig().getConfigurationSection(path + "destinations.").getKeys(false);
		this.fuel = data.getConfig().getInt(path + "fuel");
		this.broken = data.getConfig().getBoolean(path + "broken");
		this.partRequired = Material.getMaterial(data.getConfig().getString(path + "partRequired"));
		this.consumePart = data.getConfig().getBoolean(path + consumePart);
		this.pilots = data.getConfig().getStringList(path + "pilots");
	}
	
	public Ship(ConfigAccessor data, String name, String destinationName, Selection selection, Location location, int length, int width, int height) throws DataException, IOException
	{
		this.data = data;
		this.name = name;
		this.currentDestination = new Destination(data, name, destinationName, location);
		this.destinations.add(currentDestination.getDestinationName());
		this.fuel = 10;
		this.broken = false;
		this.pilots = new ArrayList<String>();
		this.length = length;
		this.width = width;
		this.height = height;
		
		data.getConfig().set("ships." + name + ".currentDestination", destinationName);
		data.getConfig().set("ships." + name + ".fuel", 0);
		data.getConfig().set("ships." + name + ".broken", broken);
		data.getConfig().set("ships." + name + ".pilots", pilots);
		data.getConfig().set("ships." + name + ".length", length);
		data.getConfig().set("ships." + name + ".width", width);
		data.getConfig().set("ships." + name + ".height", height);
		data.saveConfig();
		
		SchematicManager sm = new SchematicManager(location.getWorld());
		sm.saveSchematic(selection, "ship", name + "\\");
	}
	
	public void deleteShip()
	{
		data.getConfig().getConfigurationSection("ships.").set(name, null);
		data.saveConfig();
		
		//Delete ship schematic
		SchematicManager sm = new SchematicManager(null);
		sm.deleteSchematic(name, "ship");
	}
	
	public Destination getDestination(String destinationName)
	{
		return new Destination(data, name, destinationName);
	}
	
	public void addDestination(String destinationName, Location location)
	{
		new Destination(data, name, destinationName, location);
	}
	
	public Destination getCurrentDestination()
	{
		return currentDestination;
	}
	
	public void setCurrentDestination(String destination)
	{
		currentDestination = new Destination(data, name, destination);
	}
	
	public boolean isPilot(UUID player)
	{
		List <String> pilots = data.getConfig().getStringList("ships." + name + ".pilots");
		
		for (String p : pilots)
			if (player.toString().equalsIgnoreCase(p))
				return true;
		return false;
	}
	
	public boolean isBroken()
	{
		return broken;
	}
	
	public void setBroken(boolean broken)
	{
		data.getConfig().set("ships." + name + ".broken", broken);
		data.saveConfig();
	}
	
	public int getFuel()
	{
		return fuel;
	}

	
	public int getLength()
	{
		return length;
	}
	
	public int getWidth()
	{
		return width;
	}
	
	public int getHeight()
	{
		return height;
	}
	
	public void setFuel(int fuel)
	{
		data.getConfig().set("ships." + name + ".fuel", fuel);
		data.saveConfig();
	}
	
	public void reduceFuel()
	{
		data.getConfig().set("ships." + name + ".fuel",fuel - 1);
		data.saveConfig();
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setRepairItem(String item)
	{
		data.getConfig().set("ships." + name + ".repairItem", item);
		data.saveConfig();
	}
	
	public void setConsumePart(boolean consumePart)
	{
		data.getConfig().set("ships." + name + ".consumePart", consumePart);
		data.saveConfig();
	}
}
