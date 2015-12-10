package com.gmail.Rhisereld.HorizonShips;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
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
		
		//Check that the ship exists
		Ship ship = new Ship(data, shipName);
		if (ship.getName() == null)
			throw new IllegalArgumentException("Ship not found.");
		
		//Check that there isn't already a destination by that name.
		if (ship.getDestination(destinationName) == null)
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
	 * removeDestination() removes the destination and all its information from the ship given.
	 * 
	 * @param shipName
	 * @param destinationName
	 */
	public void removeDestination(String shipName, String destinationName) throws IllegalArgumentException
	{
		//Check that the ship exists
		Ship ship = new Ship(data, shipName);
		if (ship.getName() == null)
			throw new IllegalArgumentException("Ship not found.");
		
		//Check that the destination exists
		Destination destination = ship.getDestination(destinationName);
		if (destination.getName() == null)
			throw new IllegalArgumentException("That destination does not exist!");
		
		//Check that the ship isn't currently at that destination
		if (destinationName.equalsIgnoreCase(ship.getCurrentDestination().getName()))
			throw new IllegalArgumentException("You are currently at that destination! You have to leave it first.");
		
		ship.removeDestination(destinationName);
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
			list = list.concat(ship + ", ");
		
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
	{
		//Determine the ship the player is trying to pilot.
		Ship ship = findCurrentShip(player);
		if (ship == null)
			throw new IllegalArgumentException("You are not inside a ship.");
		
		//Make sure they are not trying to fly to the current destination
		if (ship.getCurrentDestination().getName().equalsIgnoreCase(destination))
			throw new IllegalArgumentException("You are already at that destination!");
		
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
		if (ship.getDestination(destination).getName() == null)
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
	
	/**
	 * repair() ensures the player has the correct item to repair the ship, and if it does,
	 * it removes the item from the player and sets the ship's broken status to false.
	 * 
	 * @param player
	 */
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
	 * refuel() checks that the player has the correct item to refuel the ship, and if they do,
	 * it removes the item from the player and sets the ship's fuel level.
	 * 
	 * @param player
	 * @throws IllegalArgumentException
	 */
	public void refuel(Player player) throws IllegalArgumentException
	{
		//Determine the ship the player is trying to refuel
		Ship ship = findCurrentShip(player);
		if (ship == null)
			throw new IllegalArgumentException("You are not inside a ship.");
		
		//Check if the ship tank is full
		int maxTank = config.getConfig().getInt("refuel.maxtank");
		if (ship.getFuel() >= maxTank)
			throw new IllegalArgumentException("The ship's tank is already full.");
		
		//Get required items
		Set<String> refuelItemStrings = config.getConfig().getConfigurationSection("refuel").getKeys(false);
		
		//Check player is holding the correct item
		String itemInHand = player.getItemInHand().getType().name();
		String refuelItemString = null;
		for (String r: refuelItemStrings)
			if (itemInHand.equalsIgnoreCase(r))
				refuelItemString = r;
		
		if (refuelItemString == null)
			throw new IllegalArgumentException("You do not have the correct item.");
		
		//Remove the correct number of the item.
		int numItemsInHand = player.getItemInHand().getAmount();
		int fills = config.getConfig().getInt("refuel." + refuelItemString + ".fills");
		int numToUse;
		
		if ((10 - ship.getFuel()) < (numItemsInHand * fills))
			numToUse = (maxTank - ship.getFuel()) / fills;
		else
			numToUse = player.getItemInHand().getAmount();
		
		int newNum = player.getItemInHand().getAmount() - numToUse;
		
		if (newNum <= 0)
			player.setItemInHand(null);
		else
			player.getItemInHand().setAmount(newNum);
		
		//Raise the fuel level that amount.
		ship.setFuel(ship.getFuel() + numToUse * fills);
		
		//Notify
		player.sendMessage(ChatColor.YELLOW + "You refilled the ship by " + numToUse * fills + " units.");
	}
	
	/**
	 * shipInfo() displays some information about the given ship to the player.
	 * 
	 * @param sender
	 * @param shipName
	 * @throws IllegalArgumentException
	 */
	public void shipInfo(CommandSender sender, String shipName) throws IllegalArgumentException
	{
		//Get the ship
		Ship ship = new Ship(data, shipName);
		
		//Check the ship actually exists
		if (ship.getName() == null)
			throw new IllegalArgumentException("Ship not found.");
		
		//Check that the person checking is the owner, a permitted pilot, an admin, or the console
		Player player = null;
		if (sender instanceof Player)
			player = Bukkit.getPlayer(sender.getName());
		
		if (player != null && ship.getOwner() != null &&!ship.isPilot(player.getUniqueId())  
				&& !ship.getOwner().equals(player.getUniqueId()) && !player.hasPermission("horizonships.admin.info"))
				throw new IllegalArgumentException("You don't have permission to view that ship.");

		//Organise some information
		//Destinations
		Set<String> destinations = ship.getAllDestinations();
		String destinationString = "";
		
		for (String d: destinations)
			destinationString = destinationString.concat(d + ", ");
		
		if (destinations.size() == 0)
			destinationString = destinationString.concat("None");
		else if (destinationString.length() > 0)
			destinationString = destinationString.substring(0, destinationString.length() - 2);
		
		//Condition
		String condition;
		if (ship.isBroken())
			condition = "Broken down";
		else
			condition = "Good";
		
		//Permitted pilots
		List<UUID> pilots = ship.getPilots();
		String pilotsString = "";
		
		for (UUID p: pilots)
			pilotsString = pilotsString.concat(Bukkit.getOfflinePlayer(p).getName() + ", ");
		
		if (pilots.size() == 0)
			pilotsString = pilotsString.concat("None");
		else if (pilotsString.length() > 0)
			pilotsString = pilotsString.substring(0, pilotsString.length() - 2);
		
		//Current destination
		String currentDestination = ship.getCurrentDestination().getName();
		
		//Dimensions
		String dimensions = ship.getLength() + "x" + ship.getHeight() + "x" + ship.getWidth();
		
		//Owner
		Player ownerPlayer = Bukkit.getPlayer(ship.getOwner());
		String owner;
		if (ship.getOwner() == null)
			owner = "None";
		else if (ownerPlayer == null)
			owner = Bukkit.getOfflinePlayer(ship.getOwner()).getName();
		else
			owner = ownerPlayer.getName();
		
		if (owner == null) //Sometimes player names can't be retrieved if the player has never joined the server.
			owner = ship.getOwner().toString(); //At this point, give up and provide the UUID instead.
		
		//Colour status
		ChatColor conditionStatus;
		if (ship.isBroken())
			conditionStatus = ChatColor.RED;
		else
			conditionStatus = ChatColor.GREEN;
		
		ChatColor fuelStatus;
		if (ship.getFuel() == 0)
			fuelStatus = ChatColor.RED;
		else
			fuelStatus = ChatColor.GREEN;
		
		//Displaying
		if (sender instanceof Player)
		{
			sender.sendMessage("-----------<" + ChatColor.GOLD + " Ship " + ChatColor.WHITE + "- " + ChatColor.GOLD + shipName + " " 
								+ ChatColor.WHITE + ">-----------");
			sender.sendMessage(ChatColor.YELLOW + "OWNER: " + ChatColor.WHITE + owner);
			sender.sendMessage(ChatColor.YELLOW + "DESTINATIONS: " + ChatColor.WHITE + destinationString);
			sender.sendMessage(ChatColor.YELLOW + "CURRENT LOCATION: " + ChatColor.WHITE + currentDestination);
			sender.sendMessage(ChatColor.YELLOW + "DIMENSIONS: " + ChatColor.WHITE + dimensions);
			sender.sendMessage(ChatColor.YELLOW + "PPERMITTED PILOTS: " + ChatColor.WHITE + pilotsString);
			sender.sendMessage(ChatColor.YELLOW + "MECHANICAL CONDITION: " + conditionStatus + condition);
			sender.sendMessage(ChatColor.YELLOW + "FUEL: " + fuelStatus + 
					Integer.toString(ship.getFuel()));
		}
		else
		{
			sender.sendMessage("---------------<" + ChatColor.GOLD + " Ship " + ChatColor.WHITE + "- " + ChatColor.GOLD + shipName + " " 
								+ ChatColor.WHITE + ">---------------");
			sender.sendMessage(ChatColor.YELLOW + "Owner:                 " + ChatColor.WHITE + owner);
			sender.sendMessage(ChatColor.YELLOW + "Destinations:          " + ChatColor.WHITE + destinationString);
			sender.sendMessage(ChatColor.YELLOW + "Current location:      " + ChatColor.WHITE + currentDestination);
			sender.sendMessage(ChatColor.YELLOW + "Dimensions:            " + ChatColor.WHITE + dimensions);
			sender.sendMessage(ChatColor.YELLOW + "Permitted pilots:      " + ChatColor.WHITE + pilotsString);
			sender.sendMessage(ChatColor.YELLOW + "Mechanical condition:  " + conditionStatus + condition);
			sender.sendMessage(ChatColor.YELLOW + "Fuel:                  " + fuelStatus + Integer.toString(ship.getFuel()));
		}

	}
	
	/**
	 * setOwner() sets the owner of the ship.
	 * 
	 * @param sender
	 * @param shipName
	 * @param owner
	 * @throws IllegalArgumentException
	 */
	public void setOwner(CommandSender sender, String shipName, String owner) throws IllegalArgumentException
	{
		//Get the ship
		Ship ship = new Ship(data, shipName);
		
		//Ensure the ship actually exists
		if (ship.getName() == null)
			throw new IllegalArgumentException("Ship not found.");
		
		//Set the new owner
		Player player = Bukkit.getPlayer(owner);
		if (player == null)
		{
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(owner);
			
			if (offlinePlayer == null)
				throw new IllegalArgumentException("Player does not exist.");
			
			ship.setOwner(offlinePlayer.getUniqueId());
		}
		else
			ship.setOwner(player.getUniqueId());		
	}
	
	/**
	 * transfer() sets the new owner of a ship, but also ensures that the player
	 * performing the command owns the ship.
	 * 
	 * @param owner
	 * @param shipName
	 * @param newOwner
	 */
	public void transfer(Player owner, String shipName, String newOwner)
	{
		//Get the ship
		Ship ship = new Ship(data, shipName);
		
		//Ensure the ship actually exists
		if (ship.getName() == null)
			throw new IllegalArgumentException("Ship not found.");
		
		//Ensure the player is the owner of the ship
		if (!owner.getUniqueId().equals(ship.getOwner()))
			throw new IllegalArgumentException("You are not the owner of that ship.");
		
		//Set the new owner
		Player player = Bukkit.getPlayer(newOwner);
		if (player == null)
		{
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(newOwner);
			
			if (offlinePlayer == null)
				throw new IllegalArgumentException("Player does not exist.");
			
			ship.setOwner(offlinePlayer.getUniqueId());
		}
		else
			ship.setOwner(player.getUniqueId());
	}
	
	/**
	 * addPilot() ensures that the sender is an owner of the ship or an administrator,
	 * then adds the pilot given to the list of permitted pilots for the ship given.
	 * 
	 * @param sender
	 * @param shipName
	 * @param pilot
	 * @throws IllegalArgumentException
	 */
	public void addPilot(CommandSender sender, String shipName, String pilot) throws IllegalArgumentException
	{
		//Make sure the ship exists
		Ship ship = new Ship(data, shipName);
		if (ship.getName() == null)
			throw new IllegalArgumentException("Ship not found.");
		
		//Check that the sender is an administrator OR owns the ship
		Player player = Bukkit.getPlayer(sender.getName());
		
		if (!sender.hasPermission("horizonships.admin.pilot.add") && player != null && player.getUniqueId().equals(ship.getOwner()))
			throw new IllegalArgumentException("That ship does not belong to you.");
		
		//Make sure the pilot isn't already a pilot
		List<UUID> pilots = ship.getPilots();
		Player pilotPlayer = Bukkit.getPlayer(pilot);
		UUID pilotUUID;
		boolean pilotMatch = false;
		
		if (pilotPlayer == null)
			pilotUUID = Bukkit.getOfflinePlayer(pilot).getUniqueId();
		else
			pilotUUID = pilotPlayer.getUniqueId();
		
		for (UUID u: pilots)
			if (pilotUUID.equals(u))
				pilotMatch = true;
		
		if (pilotMatch)
			throw new IllegalArgumentException("That player is already a pilot of that ship.");
		
		ship.addPilot(pilotUUID);
	}
	
	/**
	 * removePilot() ensures that the sender is an owner of theship or an administrator,
	 * then removes the pilot given from the list of permitted pilots for the ship given.
	 * 
	 * @param sender
	 * @param shipName
	 * @param pilot
	 * @throws IllegalArgumentException
	 */
	public void removePilot(CommandSender sender, String shipName, String pilot) throws IllegalArgumentException
	{
		//Make sure the ship exists
		Ship ship = new Ship(data, shipName);
		if (ship.getName() == null)
			throw new IllegalArgumentException("Ship not found.");
		
		//Check that the sender is an administrator OR owns the ship
		Player player = Bukkit.getPlayer(sender.getName());
		
		if (!sender.hasPermission("horizonships.admin.pilot.remove") && player != null && player.getUniqueId().equals(ship.getOwner()))
			throw new IllegalArgumentException("That ship does not belong to you.");
		
		//Check that the pilot is currently a pilot of the ship
		List<UUID> pilots = ship.getPilots();
		Player pilotPlayer = Bukkit.getPlayer(pilot);
		UUID pilotUUID;
		boolean pilotMatch = false;
		
		if (pilotPlayer == null)
			pilotUUID = Bukkit.getOfflinePlayer(pilot).getUniqueId();
		else
			pilotUUID = pilotPlayer.getUniqueId();
		
		for (UUID u: pilots)
			if (pilotUUID.equals(u))
				pilotMatch = true;
		
		if (!pilotMatch)
			throw new IllegalArgumentException("That player is not a pilot of that ship.");
		
		ship.removePilot(pilotUUID);
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
