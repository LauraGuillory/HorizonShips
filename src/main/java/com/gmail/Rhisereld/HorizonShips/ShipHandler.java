package com.gmail.Rhisereld.HorizonShips;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.regions.RegionOperationException;

@SuppressWarnings("deprecation")
public class ShipHandler 
{	
	ConfigAccessor data;
	ConfigAccessor config;
	Plugin plugin;
	
	HashMap<String, SchematicManager> schemManagers = new HashMap<String, SchematicManager>();
	
	public ShipHandler(ConfigAccessor data, ConfigAccessor config, Plugin plugin) 
	{
		this.data = data;
		this.plugin = plugin;
		this.config = config;
	}

	/**
	 * createShip() creates a new ship to be recorded in the data.yml file. It will have a name, current destination,
	 * a list of destinations (containing only the current destination), and a list of pilots (empty).
	 * It will have a fuel level of zero and will not be broken.
	 * 
	 * @param shipName
	 * @param player
	 * @param destinationName
	 * @throws DataException
	 * @throws IOException
	 * @throws NullPointerException
	 * @throws IllegalArgumentException
	 */
	@SuppressWarnings("unused")
	public void createShip(String shipName, Player player, String destinationName) throws DataException, IOException, NullPointerException, IllegalArgumentException
	{
		//Check a ship doesn't already exist by that name.
		if (data.getConfig().contains("ships."))
		{
			Set<String> shipNames = data.getConfig().getConfigurationSection("ships.").getKeys(false);
			for (String sh : shipNames)
				if (sh.equalsIgnoreCase(shipName))
					throw new IllegalArgumentException("A ship already exists by that name.");
		}
				
		SchematicManager sm = new SchematicManager(player.getWorld());
		Selection s = sm.getPlayerSelection(player);
		
		Ship ship = new Ship(data, shipName, destinationName, s);
	}

	/**
	 * deleteShip() removes all saved information on the given ship.
	 * 
	 * @param sender
	 * @param shipName
	 * @throws IllegalArgumentException
	 */
	public void deleteShip(CommandSender sender, String shipName) throws IllegalArgumentException, IOException
	{
		Set<String> shipNames = data.getConfig().getConfigurationSection("ships.").getKeys(false);
		boolean shipFound = false;
		
		//Check that the ship exists
		for (String s : shipNames)
			if (s.equalsIgnoreCase(shipName))
				shipFound = true;
		if (!shipFound)
			throw new IllegalArgumentException("Ship not found.");
		
		//Check that the person has permission
		if (!sender.hasPermission("horizonships.admin.delete") && sender instanceof Player)
			throw new IllegalArgumentException("You don't have permission to delete that ship.");
		
		//Delete all information on the ship.
		Ship ship = new Ship(data, shipName);
		ship.deleteShip();
	}

	/**
	 * testDestination() pastes the named ship inside the current WorldEdit selection of the player for visualisation purposes.
	 * Checks that there isn't already a destination by that name.
	 * Checks that the player has permission.
	 * 
	 * @param sm
	 * @param player
	 * @param shipName
	 * @param destinationName
	 * @throws MaxChangedBlocksException
	 * @throws DataException
	 * @throws IOException
	 */
	public void testDestination(Player player, String shipName, String destinationName) throws MaxChangedBlocksException, DataException, IOException, NullPointerException, IllegalArgumentException
	{
		Selection s;
		player.getWorld();
		new SchematicManager(player.getWorld());
		SchematicManager sm = new SchematicManager(player.getWorld());
		schemManagers.put(player.getName(), sm);

		//Check that the player has permission.
		if (!player.hasPermission("horizonShips.admin.destination"))
			throw new IllegalArgumentException("You don't have permission to create a destination!");
		
		//Check that there isn't already a destination by that name.
		if (data.getConfig().getConfigurationSection("ships." + shipName + ".destinations").getKeys(false).contains(shipName))
			throw new IllegalArgumentException("This ship already has a destination by that name.");

		s = sm.getPlayerSelection(player);
		sm.loadSchematic(s, shipName + "\\", "ship");
	}
	
	/**
	 * adjustDestination() undoes the previous changes made stored in the SchematicManager object, and pastes the ship
	 * in the new location one block in the direction given.
	 * Checks that the direction is valid.
	 * 
	 * @param sm
	 * @param player
	 * @param direction
	 * @param shipName
	 * @throws MaxChangedBlocksException
	 * @throws DataException
	 * @throws IOException
	 * @throws RegionOperationException
	 * @throws IncompleteRegionException
	 */
	public void adjustDestination(Player player, String direction, String shipName) throws MaxChangedBlocksException, DataException, IOException, RegionOperationException, IncompleteRegionException
	{
		Vector dir;
		SchematicManager sm = schemManagers.get(player.getName());
		Selection s = sm.getPlayerSelection(player);
		
		switch (direction)
		{
			case "north": 	dir = new Vector(0, 0, -1);
							break;
			case "south": 	dir = new Vector(0, 0, 1);
							break;
			case "east": 	dir = new Vector(1, 0, 0);
							break;
			case "west": 	dir = new Vector(-1, 0, 0);
							break;
			case "up": 		dir = new Vector(0, 1, 0);
							break;
			case "down": 	dir = new Vector(0, -1, 0);
							break;
			default:		throw new IllegalArgumentException("That is not a valid direction.");		
		}
		
		//Undo previous paste.
		sm.undoSession();
		
		//Shift 1 block in direction specified.
		sm.shiftSelection(player, s, dir);
		
		//Paste
		sm.loadSchematic(s, shipName + "\\", "ship");
	}
	
	/**
	 * cancelDestination() removes the ship previously pasted for visualisation purposes.
	 * 
	 * @param sm
	 */
	public void cancelDestination(String name)
	{
		SchematicManager sm = schemManagers.get(name);
		schemManagers.remove(name);
		sm.undoSession();
	}
	
	/**
	 * addDestination() officially adds the destination by storing its location and name. It also removes the 
	 * ship previously pasted for visualisation purposes.
	 * 
	 * @param sm
	 * @param player
	 * @param shipName
	 * @param destinationName
	 */
	public void addDestination(Player player, String shipName, String destinationName)
	{
		SchematicManager sm = schemManagers.get(player.getName());

		Ship ship = new Ship(data, shipName);
		ship.addDestination(destinationName, sm.getPlayerSelection(player).getMinimumPoint());
		
		//Undo paste.
		sm.undoSession();
	}
	
	/**
	 * listShips() sends the sender a list of names of all the existing ships.
	 * 
	 * @param sender
	 */
	public void listShips(CommandSender sender)
	{
		Set<String> ships = data.getConfig().getConfigurationSection("ships.").getKeys(false);
		String list = "Ships currently saved: ";
		
		for (String ship: ships)
		{
			list = list.concat(ship + ", ");
		}
		
		if (ships.size() == 0)
			list = list.concat("None.");
		else if (list.length() > 0)
		{
			list = list.substring(0, list.length() - 2);
			list = list.concat(".");
		}
		
		sender.sendMessage(ChatColor.YELLOW + list);
	}
	
	/**
	 * moveShip() handles all the actions required when a player pilots a ship from one destination to another.
	 * It saves the schematic at current location, pastes the schematic at the new location, teleports all the players within the
	 * ship's region to the new location, erases the old ship, and reduces the ship fuel by one.
	 * 
	 * @param player
	 * @param destination
	 * @throws DataException
	 * @throws IOException
	 * @throws MaxChangedBlocksException
	 * @throws IllegalArgumentException
	 */
	public void moveShip(Player player, String destination) throws DataException, IOException, MaxChangedBlocksException, IllegalArgumentException
	{		//TODO: should not allow current destination
		//Determine the ship the player is trying to pilot.
		Ship ship = findCurrentShip(player);
		if (ship == null)
			throw new IllegalArgumentException("You are not inside a ship.");
		
		//Make sure the player is a permitted pilot
		if (!ship.isPilot(player.getUniqueId()) && !player.hasPermission("horizonships.admin.ship.move"))
			throw new IllegalArgumentException("You don't have permission to pilot this ship.");
		
		//Make sure the ship isn't broken
		if (ship.isBroken())
			throw new IllegalArgumentException("The ship has broken down.");
		
		//Make sure the ship has enough fuel
		if (ship.getFuel() == 0)
			throw new IllegalArgumentException("The ship is out of fuel.");
		
		//Make sure the destination is valid.		
		if (ship.getDestination(destination) == null)
			throw new IllegalArgumentException("That destination does not exist.");
		
		//Save schematic at current location
		SchematicManager sm = new SchematicManager(player.getWorld());
		Location currentLocation = ship.getCurrentDestination().getLocation();
		Location loc2 = new Location(currentLocation.getWorld(), 
				currentLocation.getBlockX() + ship.getLength(), 
				currentLocation.getBlockY() + ship.getHeight(), 
				currentLocation.getBlockZ() + ship.getWidth());
		
		sm.saveSchematic(currentLocation, loc2, ship.getName() + "\\", "ship");

		//Paste schematic at new location
		Destination newDestination = ship.getDestination(destination);
		sm = new SchematicManager(newDestination.getLocation().getWorld());		//Each schematic manager may only apply to one world.
		sm.loadSchematic(newDestination.getLocation(), ship.getName() + "\\", "ship");

		//Teleport all players from old to new location
		teleportPlayers(ship, newDestination.getLocation());

		//Erase old location
		sm.eraseArea(currentLocation, loc2);

		//Reduce fuel by one
		ship.reduceFuel();

		//Change current destination
		ship.setCurrentDestination(destination);
		
		//Event trigger
		ShipEvent shipEvent = new ShipEvent(config, data);
		shipEvent.chooseEvent();
		String message = shipEvent.trigger(player, ship);
		Set<Player> playersToNotify = getPlayersInsideRegion(ship);
		for (Player p: playersToNotify)
			p.sendMessage(ChatColor.YELLOW + message);
	}
	
	/**
	 * diagnose() reveals to the player which item is required to fix a broken ship.
	 * 
	 * @param player
	 */
	public void diagnose(Player player) throws IllegalArgumentException
	{
		//Determine the ship the player is trying to diagnose.
		Ship ship = findCurrentShip(player);
		if (ship == null)
			throw new IllegalArgumentException("You are not inside a ship.");
		
		//Check that the ship is actually broken down.
		if (!ship.isBroken())
			throw new IllegalArgumentException("The ship is functioning perfectly.");
		
		//Get required item
		String repairItem = ship.getRepairItem();
		
		//Use custom name for item.
		Boolean consumePart = ship.getConsumePart();
		if (consumePart)
			repairItem = config.getConfig().getString("events.breakdown.spareParts." + repairItem + ".name", repairItem);
		else
			repairItem = config.getConfig().getString("events.breakdown.tools." + repairItem + ".name", repairItem);
		
		String article;
		Set<Character> vowels = new HashSet<Character>(Arrays.asList('a', 'e', 'i', 'o', 'u'));

		if(vowels.contains(Character.toLowerCase(repairItem.charAt(0)))) 
			article = "an ";
		else
			article = "a ";

		//Notify
		player.sendMessage(ChatColor.YELLOW + "To repair this ship, you need " + article + repairItem + ".");
	}
	
	public void repair(Player player)
	{
		//Determine the ship the player is trying to repair.
		Ship ship = findCurrentShip(player);
		if (ship == null)
			throw new IllegalArgumentException("You are not inside a ship.");
				
		//Check that the ship is actually broken down.
		if (!ship.isBroken())
			throw new IllegalArgumentException("The ship is already fully functional.");
				
		//Get required item
		String repairItem = ship.getRepairItem();
		Material repairItemMaterial = Material.matchMaterial(repairItem);
		
		//Check player is holding the correct item.
		if(!player.getInventory().getItemInHand().getType().equals(repairItemMaterial))
			throw new IllegalArgumentException("You don't have the correct item.");
		
		//If they are, remove one of that item.
		int numItemsInHand = player.getItemInHand().getAmount();
		if (numItemsInHand <= 1)
			player.setItemInHand(null);
		else
			player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
		
		//Repair the ship
		ship.setBroken(false);
		
		//Notify
		player.sendMessage(ChatColor.YELLOW + "You repaired the ship.");
	}
	
	/**
	 * Searches through the locations of all the ships to determine if a player is inside one, and if so,
	 * returns the name of that ship.
	 * 
	 * @param player
	 * @return
	 */
	private Ship findCurrentShip(Player player)
	{
		Set<String> shipStrings = data.getConfig().getConfigurationSection("ships.").getKeys(false);
		Set<Ship> ships = new HashSet<Ship>();
		
		for (String ss: shipStrings)
			ships.add(new Ship(data, ss));
				
		for (Ship s: ships)
		{
			Set<Player> playersInside = getPlayersInsideRegion(s);
			for (Player p: playersInside)
				if (p.equals(player))
					return s;
		}

		return null;
	}
	
	/**
	 * teleportPlayers() teleports all players within the region defined by oldLocation and the length, width, and height
	 * to newLocation. The position of all players teleported is offset by their offset at the original location.
	 * 
	 * @param oldLocation
	 * @param newLocation
	 * @param length
	 * @param width
	 * @param height
	 */
	private void teleportPlayers(Ship ship, Location newLocation)
	{
		Location oldLocation = ship.getCurrentDestination().getLocation();
		Set<Player> playersInside = getPlayersInsideRegion(ship);
		
		for (Player p: playersInside)
			//Determine new player location based on existing offset to ship location.			
			p.teleport(new Location(newLocation.getWorld(),
				p.getLocation().getX() - oldLocation.getX() + newLocation.getX(), 
				p.getLocation().getY() - oldLocation.getY() + newLocation.getY(),
				p.getLocation().getZ() - oldLocation.getZ() + newLocation.getZ(),
				p.getLocation().getYaw(),
				p.getLocation().getPitch()));
	}
	
	/**
	 * getPlayersInsideRegion() returns a set containing all the players whose locations are
	 * within the region defined by a location, length, width and height.
	 * 
	 * @param location
	 * @param length
	 * @param width
	 * @param height
	 * @return
	 */
	private Set<Player> getPlayersInsideRegion(Ship ship)
	{
		Collection<? extends Player> onlinePlayers = Bukkit.getServer().getOnlinePlayers();
		Set<Player> playersInside = new HashSet<Player>();
		Location location = ship.getCurrentDestination().getLocation();

		for (Player p: onlinePlayers)
			if (p.getWorld().equals(location.getWorld())
					&& p.getLocation().getBlockX() >= location.getBlockX() && p.getLocation().getBlockX() <= location.getBlockX() + ship.getLength()
					&& p.getLocation().getBlockY() >= location.getBlockY() && p.getLocation().getBlockY() <= location.getBlockY() + ship.getHeight()
					&& p.getLocation().getBlockZ() >= location.getBlockZ() && p.getLocation().getBlockZ() <= location.getBlockZ() + ship.getWidth())
				playersInside.add(p);

		return playersInside;
	}
}
