package com.gmail.Rhisereld.HorizonShips;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.gmail.Rhisereld.HorizonProfessions.ProfessionAPI;

/**
 * ShipEvent handles all things related to the set of events that may occur during a journey.
 * chooseEvent() must be called before trigger() is called.
 */
public class ShipEvent 
{
	private String[] EVENTS = {"bumpyRide", "infestation", "breakdown", "fuelLeak", "none"};
	ProfessionAPI prof;
	private FileConfiguration config;
	private String chosenEvent;
	
	public ShipEvent(ProfessionAPI prof, FileConfiguration config, FileConfiguration data)
	{
		this.prof = prof;
		this.config = config;
	}

	/**
	 * chooseEvent() randomly selects an event from the EVENTS array.
	 */
	public void chooseEvent()
	{
		int total = 0;
		
		for (String e: EVENTS)
			total += config.getInt("events." + e + ".probability");
		
		double randomNum = Math.random() * total;
		double cumulativeProbability = 0;
		for (String e: EVENTS) 
		{
		    cumulativeProbability += config.getInt("events." + e + ".probability");
		    if (cumulativeProbability >= randomNum)
		    {
		        chosenEvent = e;
		        return;
		    }

		}

	}
	
	/**
	 * trigger() calls the methods necessary to trigger the event based on which event has been selected.
	 * 
	 * @param player
	 * @param ship
	 * @param location
	 * @param length
	 * @param width
	 * @param height
	 * @return A string detailing the events of the trip.
	 */
	public String trigger(Player player, Ship ship)
	{
		switch(chosenEvent) {
			case "bumpyRide": return triggerBumpyRide(player, ship);
			case "infestation": return triggerInfestation(player, ship);
			case "breakdown": return triggerBreakdown(player, ship);
			case "fuelLeak": return triggerFuelLeak(player, ship);
			case "none": return triggerNone(player, ship);
			default:	Bukkit.getLogger().severe("Invalid event. Event cancelled.");
						return null;
		}
	}
	
	/**
	 * triggerBumpyRide() completes all of the actions required for the Bumpy Ride event.
	 * - Decides whether an injury occurs based on pilot's skill and a random number
	 * - If yes, randomly selects a player and damages their health.
	 * 
	 * @param player
	 * @param location
	 * @param length
	 * @param width
	 * @param height
	 * @return A string detailing the events of the trip.
	 */
	private String triggerBumpyRide(Player player, Ship ship)
	{
		//Configuration options
		String path = "events.bumpyRide.";
		int damage = config.getInt(path + "damage");
		boolean professionsEnabled = config.getBoolean("professionsEnabled");
		String professionReq = config.getString("professionReqs.pilot.profession");
		
		//Determine if injury occurs or not
		double randomDoub = 0;
		int injuryChance = 0;
		Random rand = new Random();
		//If profession isn't required, injury never happens.
		if (prof != null && professionsEnabled && professionReq != null)
		{
			injuryChance = config.getInt(path + "injuryChance." + prof.getTier(player.getUniqueId(), professionReq));
			randomDoub = rand.nextDouble() * 100;
		}
		
		//No injury
		if (prof == null || professionReq == null || randomDoub > injuryChance)
		{
			return "The ship creaks and shudders, battered with whorls of wind. " + player.getDisplayName() + ChatColor.YELLOW + 
					" expertly manouevres the ship through the atmosphere, and the tremors fade away.";
		}
		//Injury
		else
		{
			//Choose a player to damage
			Collection<? extends Player> onlinePlayers = Bukkit.getServer().getOnlinePlayers();
			List<Player> playersOnShip = new ArrayList<Player>();
			Location location = ship.getCurrentDestination().getLocation();

			for (Player p: onlinePlayers)
				if (p.getWorld().equals(location.getWorld())
						&& p.getLocation().getBlockX() >= location.getBlockX() 
						&& p.getLocation().getBlockX() <= location.getBlockX() + ship.getLength()
						&& p.getLocation().getBlockY() >= location.getBlockY() 
						&& p.getLocation().getBlockY() <= location.getBlockY() + ship.getHeight()
						&& p.getLocation().getBlockZ() >= location.getBlockZ() 
						&& p.getLocation().getBlockZ() <= location.getBlockZ() + ship.getWidth())
					playersOnShip.add(p);
			
			if (playersOnShip.isEmpty())
				throw new IllegalArgumentException("Error with bumpy ride event: no players detected on the ship!");
			
			int randomInt = rand.nextInt(playersOnShip.size());
			
			//Damage player
			double newHealth = playersOnShip.get(randomInt).getHealth() - damage;
			
			if (newHealth < 2)
				playersOnShip.get(randomInt).setHealth(2);
			else
				playersOnShip.get(randomInt).setHealth(newHealth);
			
			//Notify
			return "The ship creaks and shudders, battered with whorls of wind as " + player.getDisplayName() + ChatColor.YELLOW + 
					" struggles to manouevre the ship. Suddenly it lurches to one side, throwing everyone to the floor. " 
			+ playersOnShip.get(randomInt).getDisplayName() + ChatColor.YELLOW + " is injured!";
		}
	}
	
	/**
	 * triggerInfestation() completes all of the actions required for the Infestation event.
	 * - Determines the spawn point (must be air, with a solid block underneath and preferably with a roof overhead)
	 * - Spawns a number of spiders specified in configuration
	 * 
	 * @param player
	 * @param location
	 * @param length
	 * @param width
	 * @param height
	 * @return A string detailing the events of the trip.
	 */
	private String triggerInfestation(Player player, Ship ship)
	{
		//Configuration options
		String path = "events.infestation.";
		int number = config.getInt(path + "number");
		boolean poisonous = config.getBoolean(path + "poisonous");
		
		//Determine spawn point
		//Create list of locations that have a block directly below and somewhere above them.
		List<Location> potentialLocations = new ArrayList<Location>();
		boolean hasBlockAbove;
		Location location = ship.getCurrentDestination().getLocation();
		Location testLoc;
		int yAbove;
		int length = ship.getLength();
		int width = ship.getWidth();
		int height = ship.getHeight();
		
		for (int x = location.getBlockX(); x < location.getX() + length; x++)
			for (int y = location.getBlockY(); y < location.getY() + height; y++)
				for (int z = location.getBlockZ(); z < location.getZ() + width; z++)
				{
					testLoc = new Location(location.getWorld(), x, y, z);
					if (testLoc.getBlock().getType().isSolid())
						if (!testLoc.add(0, 1, 0).getBlock().getType().isSolid())
						{
							hasBlockAbove = false;
							for (yAbove = y + 1; yAbove < location.getBlockY() + height; yAbove++)
								if (testLoc.add(0, 1, 0).getBlock().getType().isSolid())
									hasBlockAbove = true;
							
							testLoc.add(0, -(yAbove - y - 1), 0);

							if (hasBlockAbove)
							{
								if (!testLoc.add(1, 0, 0).getBlock().getType().isSolid()
										&& !testLoc.add(0, 0, 1).getBlock().getType().isSolid()
										&& !testLoc.add(-1, 0, 0).getBlock().getType().isSolid())
									potentialLocations.add(new Location(location.getWorld(), x, y+1, z));
							}
						}
				}
		
		//If no potential locations, look only for locations with a block directly underneath.		
		if (potentialLocations.isEmpty())
			for (int x = location.getBlockX(); x < location.getBlockX() + length; x++)
				for (int y = location.getBlockY(); y < location.getBlockY() + height; y++)
					for (int z = location.getBlockZ(); z < location.getBlockZ() + width; z++)
					{
						testLoc = new Location(location.getWorld(), x, y, z);
						if (testLoc.getBlock().getType().isSolid())
							if (!testLoc.add(0, 1, 0).getBlock().getType().isSolid())
								if (!testLoc.add(1, 0, 0).getBlock().getType().isSolid()
										&& !testLoc.add(0, 0, 1).getBlock().getType().isSolid()
										&& !testLoc.add(-1, 0, 0).getBlock().getType().isSolid())
									potentialLocations.add(new Location(location.getWorld(), x, y+1, z));
					}


		//Randomly choose spawn point. If still no potential locations, spawn on player.
		Location spawnPoint;

		if (potentialLocations.isEmpty())
			spawnPoint = player.getLocation();

		else
		{
			Random rand = new Random();
			int randomNum = rand.nextInt(potentialLocations.size());
			spawnPoint = potentialLocations.get(randomNum);
		}
		
		//Spawn spiders
		spawnPoint.add(1, 0, 1);
		if (poisonous)
			for (int i = 0; i < number; i++)
				spawnPoint.getWorld().spawnEntity(spawnPoint, EntityType.CAVE_SPIDER);
		else
			for (int i = 0; i < number; i++)
				spawnPoint.getWorld().spawnEntity(spawnPoint, EntityType.SPIDER);
		
		return "Something shuffles inside the walls of the ship. Looks like you've picked up some unwelcome passengers, and "
				+ "they've grown to an unprecedented size!";
	}
	
	/**
	 * triggerBreakdown() completes all of the actions required for the Breakdown event.
	 * - Sets the ship status to broken.
	 * - Chooses the item needed for repair
	 * - Sets the item needed to repair
	 * - Sets isConsumed value for that repair
	 * 
	 * @param player
	 * @param ship
	 * @return A string detailing the events of the trip.
	 */
	private String triggerBreakdown(Player player, Ship ship) throws IllegalArgumentException
	{
		//Configuration options
		String path = "events.breakdown.";
		List<String> spareParts = new ArrayList<String>();
		try { spareParts.addAll(config.getConfigurationSection(path + "spareParts").getKeys(false)); }
		catch (NullPointerException e) { }
		List<String> tools = new ArrayList<String>();
		try { tools.addAll(config.getConfigurationSection(path + "tools").getKeys(false)); }
		catch (NullPointerException e) { }
		
		//If there's no possible spare parts or tools this event can't continue
		if (spareParts.isEmpty() && tools.isEmpty())
			return "Breakdown not possible: No spare parts or tools configured. Please contact an Administrator.";
		
		//Set ship to broken
		ship.setBroken(true);
		
		//Choose item needed for repair
		Random rand = new Random();
		int randomNum = rand.nextInt(spareParts.size() + tools.size() - 1);

		//Check that the item is valid - if not, cancel the event.
		boolean consumePart = false;
		String repairItem;
		if (randomNum < spareParts.size())
		{
			consumePart = true;
			repairItem = spareParts.get(randomNum);
		}
		else
			repairItem = tools.get(randomNum - spareParts.size());

		Material repairItemMaterial = Material.matchMaterial(repairItem);
		if (repairItemMaterial == null)
			return "Breakdown not possible: Invalid configuration item: " + repairItem + ". Please contact an Administrator.";
		
		//Set item needed for repair and isConsumed
		ship.setRepairItem(repairItem);
		ship.setConsumePart(consumePart);
		
		return "As you touch down, the ship engine splutters and dies. It's broken down!";
	}
	
	/**
	 * triggerFuelLeak() completes all the actions required for the Fuel Leak event.
	 * - Sets the ship's fuel level to zero.
	 * 
	 * @param player
	 * @param ship
	 * @return A string detailing the events of the trip.
	 */
	private String triggerFuelLeak(Player player, Ship ship)
	{
		ship.setFuel(0);
		return "During the flight, you notice a leak in the fuel line. A little duct tape fixes the problem for now, "
				+ "but the ship barely has enough fuel left to reach its destination.";
	}
	
	/**
	 * triggerNone() is the event where nothing happens. Really.
	 * 
	 * @param player
	 * @param ship
	 * @return
	 */
	private String triggerNone(Player player, Ship ship)
	{
		return "The journey is uneventful, and " + player.getDisplayName() + ChatColor.YELLOW + " touches down at " 
					+ ship.getCurrentDestination().getName() + " without any problems.";
	}
}
