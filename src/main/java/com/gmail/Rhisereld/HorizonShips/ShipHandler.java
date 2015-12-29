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
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.gmail.Rhisereld.HorizonProfessions.ProfessionAPI;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.regions.RegionOperationException;

@SuppressWarnings("deprecation")
public class ShipHandler 
{	
	ProfessionAPI prof;
	FileConfiguration data;
	static FileConfiguration config;
	Plugin plugin;
	
	HashMap<String, SchematicManager> schemManagers = new HashMap<String, SchematicManager>();
	
	public ShipHandler(ProfessionAPI prof, FileConfiguration data, FileConfiguration config, Plugin plugin) 
	{
		this.prof = prof;
		this.data = data;
		this.plugin = plugin;
		ShipHandler.config = config;
	}
	
	public static void updateConfig(FileConfiguration config)
	{
		ShipHandler.config = config;
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
	public void createShip(String shipName, Player player, String destinationName) throws DataException, IOException, NullPointerException, IllegalArgumentException
	{
		//Check a ship doesn't already exist by that name.
		if (data.contains("ships."))
		{
			Set<String> ships;
			try { ships = data.getConfigurationSection("ships").getKeys(false); }
			catch (NullPointerException e)
			{ return; }
			for (String sh : ships)
				if (sh.equalsIgnoreCase(shipName))
					throw new IllegalArgumentException("A ship already exists by that name.");
		}
				
		SchematicManager sm = new SchematicManager(player.getWorld());
		Selection s = sm.getPlayerSelection(player);
		
		new Ship(data, shipName, destinationName, s);
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
		Set<String> ships = new HashSet<String>();
		try { ships = data.getConfigurationSection("ships").getKeys(false); }
		catch (NullPointerException e)
		{ }
		boolean shipFound = false;
		
		//Check that the ship exists
		for (String s : ships)
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
		//Check that the ship exists
		Ship ship = new Ship(data, shipName);
		if (ship.getName() == null)
			throw new IllegalArgumentException("Ship not found.");

		//Check that there isn't already a destination by that name.
		if (ship.getDestination(destinationName) == null)
			throw new IllegalArgumentException("This ship already has a destination by that name.");
		
		//Check that the destination doesn't collide with any other destinations.
		Set<String> shipStrings;
		ConfigurationSection configSection = data.getConfigurationSection("ships");
		Location currentLocation = ship.getCurrentDestination().getLocation();	
		SchematicManager sm = new SchematicManager(currentLocation.getWorld());
		sm = new SchematicManager(player.getWorld());
		Selection s = sm.getPlayerSelection(player);
		if (configSection != null)
		{
			shipStrings = configSection.getKeys(false);
			Set<String> destinations;
			
			for (String ss: shipStrings)
			{
				Ship checkShip = new Ship(data, ss);
				destinations = checkShip.getAllDestinations();
				for (String d: destinations)
				{
					Location min = new Destination(data, ss, d).getLocation();
					Location max = new Location(min.getWorld(), min.getX() + ship.getLength(), min.getY() + ship.getHeight(), min.getZ() 
												+ ship.getWidth());
					
					Location newMin = s.getMinimumPoint();
					Location newMax = new Location(newMin.getWorld(), newMin.getX() + checkShip.getLength(), newMin.getY() + checkShip.getHeight(), 
													newMin.getZ() + checkShip.getWidth());
					
					//Check if the regions intersect.
					if (min.getWorld().equals(newMin.getWorld())
							&& (newMax.getX() >= min.getX() && newMax.getX() <= max.getX() || newMin.getX() >= min.getX() && newMin.getX() <= max.getX())
							&& (newMax.getY() >= min.getY() && newMax.getY() <= max.getY() || newMin.getY() >= min.getY() && newMax.getY() <= max.getY())
							&& (newMax.getZ() >= min.getZ() && newMax.getZ() <= max.getZ() || newMin.getZ() >= min.getZ() && newMax.getZ() <= max.getZ()))
						throw new IllegalArgumentException("Collision detected: There is already another destination here!");
				}
			}
		}

		//Save schematic from current destination	
		Location loc2 = new Location(currentLocation.getWorld(), 
				currentLocation.getBlockX() + ship.getLength(), 
				currentLocation.getBlockY() + ship.getHeight(), 
				currentLocation.getBlockZ() + ship.getWidth());

		sm.saveSchematic(currentLocation, loc2, ship.getName() + "\\ship");	
		
		//Test schematic at new destination
		sm.loadSchematic(s, ship.getName() + "\\ship");
		
		//Players currently testing a destination
		schemManagers.put(player.getName(), sm);
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
		sm.loadSchematic(s, shipName + "\\ship");
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
		Set<String> ships;
		try { ships = data.getConfigurationSection("ships").getKeys(false); }
		catch (NullPointerException e)
		{ 
			sender.sendMessage(ChatColor.YELLOW + "Ships currently saved: None");
			return;
		}
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
		//Ensure that the player has the tier requirement
		UUID uuid = player.getUniqueId();
		String professionReq = config.getString("professionReqs.pilot.profession");
		int tierReq = config.getInt("professionReqs.pilot.tier");
		
		if (prof != null && professionReq != null && prof.isValidProfession(professionReq) 
				&& !prof.hasTier(uuid, professionReq, tierReq))
			throw new IllegalArgumentException("You cannot pilot a ship because you are not " + getDeterminer(prof.getTierName(tierReq)) 
					+ " " + prof.getTierName(tierReq) + " " + professionReq + ".");
		
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
		{
			Set<String> destinations = ship.getAllDestinations();
			String message = "That destination does not exist. Valid destinations: ";
			
			if (destinations.isEmpty())
				message += "None";
			else
				for (String d: destinations)
					message += d + " ";
			
			throw new IllegalArgumentException(message);
		}

		//Save schematic at current location
		SchematicManager sm = new SchematicManager(player.getWorld());
		Location currentLocation = ship.getCurrentDestination().getLocation();
		Location loc2 = new Location(currentLocation.getWorld(), 
				currentLocation.getBlockX() + ship.getLength(), 
				currentLocation.getBlockY() + ship.getHeight(), 
				currentLocation.getBlockZ() + ship.getWidth());
		
		sm.saveSchematic(currentLocation, loc2, ship.getName() + "\\ship");

		//Paste schematic at new location
		Destination newDestination = ship.getDestination(destination);
		sm = new SchematicManager(newDestination.getLocation().getWorld());		//Each schematic manager may only apply to one world.
		sm.loadSchematic(newDestination.getLocation(), ship.getName() + "\\ship");

		//Teleport all players from old to new location
		teleportPlayers(ship, newDestination.getLocation());
		
		//Teleport all other entities from old to new location
		teleportEntities(ship, newDestination.getLocation());

		//Erase old location
		sm.eraseArea(currentLocation, loc2);
		
		//Award experience for flying.
		if (prof != null && professionReq != null && prof.isValidProfession(professionReq))
			prof.addExperience(uuid, professionReq, config.getInt("professionReqs." + professionReq + ".exp"));
		
		//No event if destination is "DeepSpace"
		if (destination.equalsIgnoreCase("DeepSpace"))
		{
			//Change current destination but do nothing else.
			ship.setCurrentDestination(destination);
			return;
		}

		//Reduce fuel by one
		ship.reduceFuel();
		
		//Event trigger
		ShipEvent shipEvent = new ShipEvent(prof, config, data);
		String message = null;
		try {
		shipEvent.chooseEvent();
		message = shipEvent.trigger(player, ship);
		} catch (IllegalArgumentException e)
		{
			player.sendMessage(ChatColor.RED + "There was an error with ship events. Please contact an Administrator.");
			e.printStackTrace();
		}
		Set<Player> playersToNotify = getPlayersInsideRegion(ship);
		for (Player p: playersToNotify)
			p.sendMessage(ChatColor.YELLOW + message);
		
		//Change current destination
		ship.setCurrentDestination(destination);
	}
	
	/**
	 * diagnose() reveals to the player which item is required to fix a broken ship.
	 * 
	 * @param player
	 */
	public void diagnose(Player player) throws IllegalArgumentException
	{
		//Only novice pilots can diagnose a ship.
		String professionReq = config.getString("professionReqs.repair.profession");
		if (prof != null && config.getBoolean("professionsEnabled") && professionReq != null)
		{
			int tierReq = config.getInt("professionReqs.repair.tier");
			
			if (!prof.hasTier(player.getUniqueId(), professionReq, tierReq))
				throw new IllegalArgumentException("You cannot diagnose a ship because you are not " + getDeterminer(prof.getTierName(tierReq)) 
						+ " " + prof.getTierName(tierReq) + " " + professionReq + ".");
		}
		
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
			repairItem = config.getString("events.breakdown.spareParts." + repairItem + ".name", repairItem);
		else
			repairItem = config.getString("events.breakdown.tools." + repairItem + ".name", repairItem);
		
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
		//Only novice pilots can repair a ship.
		UUID uuid = player.getUniqueId();
		String professionReq = config.getString("professionReqs.repair.profession");
		boolean professionEnabled = config.getBoolean("professionsEnabled");
		if (prof != null && professionEnabled && professionReq != null)
		{
			int tierReq = config.getInt("professionReqs.repair.tier");
			
			if (!prof.hasTier(uuid, professionReq, tierReq))
				throw new IllegalArgumentException("You cannot repair a ship because you are not " + getDeterminer(prof.getTierName(tierReq)) 
						+ " " + prof.getTierName(tierReq) + " " + professionReq + ".");
		}
		
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
		int dataValue;
		
		if (ship.getConsumePart())
			dataValue = config.getInt("events.breakdown.spareParts." + repairItem + ".data value");
		else
			dataValue = config.getInt("events.breakdown.tools." + repairItem + ".data value");
		
		//Check player is holding the correct item.
		if(!player.getInventory().getItemInHand().getType().equals(repairItemMaterial)
				|| !(player.getInventory().getItemInHand().getDurability() == dataValue))
			throw new IllegalArgumentException("You don't have the correct item.");
		
		//If professions are enabled, there is a chance to fumble and destroy the item
		if (prof != null && professionEnabled)
		{
			int tier = prof.getTier(uuid, professionReq);
			double successRate = config.getDouble("professionReqs.repair.successRate." + tier) / 100;
			double randomNum = Math.random();
			if (randomNum > successRate)
			{
				//Destroy the item
				int numItemsInHand = player.getItemInHand().getAmount();
				if (numItemsInHand <= 1)
					player.setItemInHand(null);
				else
					player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
				
				//Send message
				String itemName;
				if (ship.getConsumePart())
					itemName = config.getString("events.breakdown.spareParts." + repairItem + ".name");
				else
					itemName = config.getString("events.breakdown.tools." + repairItem + ".name");
				
				player.sendMessage(ChatColor.YELLOW + "You try to repair the ship, but you fumble and destroy your " 
						+ itemName + "!");
				return;
			}
		}

		
		
		//If they are and the item is consumable, remove one of that item
		if (ship.getConsumePart())
		{
			int numItemsInHand = player.getItemInHand().getAmount();
			if (numItemsInHand <= 1)
				player.setItemInHand(null);
			else
				player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
		}
		
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
		int maxTank = config.getInt("refuel.maxtank");
		if (ship.getFuel() >= maxTank)
			throw new IllegalArgumentException("The ship's tank is already full.");
		
		//Get required items
		Set<String> refuelItemStrings;
		try { refuelItemStrings = config.getConfigurationSection("refuel").getKeys(false); }
		catch (NullPointerException e)
		{ throw new IllegalArgumentException("No items are configured to refuel. Please contact an Administrator."); }
		
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
		int fills = config.getInt("refuel." + refuelItemString + ".fills");
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
			pilotsString = pilotsString.concat(getName(p) + ", ");
		
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
			owner = getName(ship.getOwner());
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
			sender.sendMessage(ChatColor.YELLOW + "PERMITTED PILOTS: " + ChatColor.WHITE + pilotsString);
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
		UUID uuid = getUUID(owner);
		if (uuid == null)
			throw new IllegalArgumentException("That player does not exist!");
			
		ship.setOwner(uuid);		
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
		UUID uuid = getUUID(newOwner);
		if (uuid == null)
			throw new IllegalArgumentException("That player does not exist!");
		
		ship.setOwner(uuid);
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
		
		if (!sender.hasPermission("horizonships.admin.pilot.add") && player != null && !player.getUniqueId().equals(ship.getOwner()))
			throw new IllegalArgumentException("That ship does not belong to you.");
		
		//Make sure the pilot isn't already a pilot
		List<UUID> pilots = ship.getPilots();
		
		UUID pilotUUID = getUUID(pilot);
		if (pilotUUID == null)
			throw new IllegalArgumentException("That player does not exist!");
		
		for (UUID p: pilots)
			if (p.equals(pilotUUID))
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
		
		if (!sender.hasPermission("horizonships.admin.pilot.remove") && player != null && !player.getUniqueId().equals(ship.getOwner()))
			throw new IllegalArgumentException("That ship does not belong to you.");
		
		//Check that the pilot is currently a pilot of the ship
		List<UUID> pilots = ship.getPilots();
		boolean pilotMatch = false;
		
		UUID pilotUUID = getUUID(pilot);
		if (pilotUUID == null)
			throw new IllegalArgumentException("That player does not exist!");
		
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
		Set<String> shipStrings;
		try { shipStrings = data.getConfigurationSection("ships").getKeys(false); }
		catch (NullPointerException e)
		{ return null; }
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
	 * teleportPlayers() teleports all players within the ship region to newLocation. The position of all players teleported is offset by 
	 * their offset at the original location.
	 * 
	 * @param ship
	 * @param newLocation
	 */
	private void teleportPlayers(final Ship ship, final Location newLocation)
	{
		final Set<Player> playersInside = getPlayersInsideRegion(ship);
		
		for (final Player p: playersInside)
		{
			final Location playerLocation = p.getLocation();
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() 
			{
				Location oldLocation = ship.getCurrentDestination().getLocation();
			
				public void run() 
				{ 
					//Determine new player location based on existing offset to ship location.			
					p.teleport(new Location(newLocation.getWorld(),
						playerLocation.getX() - oldLocation.getX() + newLocation.getX(), 
						playerLocation.getY() - oldLocation.getY() + newLocation.getY(),
						playerLocation.getZ() - oldLocation.getZ() + newLocation.getZ(),
						playerLocation.getYaw(),
						playerLocation.getPitch()));
				}
			}, 20);
		}
	}
	
	/**
	 * teleportEntities() teleports all entities within the ship region to newLocation. The position of all entities teleported is offset by
	 * their offset at the original location.
	 * 
	 * @param ship
	 * @param newLocation
	 */
	private void teleportEntities(Ship ship, Location newLocation)
	{
		Location oldLocation = ship.getCurrentDestination().getLocation();
		Set<Entity> entitiesInside = getEntitiesInsideRegion(ship);

		for (Entity e: entitiesInside)		
			if (e.getType().isSpawnable())
			{
				//Determine new entity location based on existing offset to ship location.
				newLocation.getWorld().spawnEntity(new Location(e.getLocation().getWorld(), 
						e.getLocation().getX() - oldLocation.getX() + newLocation.getX(), 
						e.getLocation().getY() - oldLocation.getY() + newLocation.getY(), 
						e.getLocation().getZ() - oldLocation.getZ() + newLocation.getZ()), e.getType());
				e.remove();
			}
	}
	
	/**
	 * getPlayersInsideRegion() returns a set containing all the players whose locations are
	 * within the ship region.
	 * 
	 * @param ship
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
	
	/**
	 * getEntitiesInsideRegion() returns a set containing all the entities whose locations are
	 * within the ship region.
	 * 
	 * @param ship
	 * @return
	 */
	private Set<Entity> getEntitiesInsideRegion(Ship ship)
	{
		List<Entity> entities = ship.getCurrentDestination().getLocation().getWorld().getEntities();
		Set<Entity> entitiesInside = new HashSet<Entity>();
		Location location = ship.getCurrentDestination().getLocation();
		
		for (Entity e: entities)
			if (e.getWorld().equals(location.getWorld())
					&& e.getLocation().getBlockX() >= location.getBlockX() && e.getLocation().getBlockX() <= location.getBlockX() + ship.getLength()
					&& e.getLocation().getBlockY() >= location.getBlockY() && e.getLocation().getBlockY() <= location.getBlockY() + ship.getHeight()
					&& e.getLocation().getBlockZ() >= location.getBlockZ() && e.getLocation().getBlockZ() <= location.getBlockZ() + ship.getWidth())
				entitiesInside.add(e);

		return entitiesInside;
	}
	
	/** 
	 * getDeterminer() returns the determiner that should occur before a noun.
	 * @param string - the noun.
	 * @return - "an" if the noun begins with a vowel, "a" otherwise.
	 */
	private String getDeterminer(String string)
	{
		if (string.charAt(0) == 'a' || string.charAt(0) == 'e' || string.charAt(0) == 'i' || string.charAt(0) == 'o'
				|| string.charAt(0) == 'u')
			return "an";
		else
			return "a";
	}
	
	/**
	 * getUUID() searches the data file for a name and returns the UUID if found.
	 * 
	 * @param name
	 * @return
	 */
	public UUID getUUID(String name)
	{
		Set<String> uuids;
		try { uuids = data.getConfigurationSection("uuids.").getKeys(false); }
		catch (NullPointerException e)
		{ return null; }
		
		for (String u: uuids)
			if (data.getString("uuids." + u).equalsIgnoreCase(name))
				return UUID.fromString(u);
		
		return null;
	}
	
	/**
	 * getName() returns the name of a given UUID if saved.
	 * 
	 * @param uuid
	 * @return
	 */
	public String getName(UUID uuid)
	{
		return data.getString("uuids." + uuid);
	}
}
