package com.gmail.Rhisereld.HorizonShips;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class ShipEvent 
{
	private String[] EVENTS = {"bumpyRide", "infestation", "breakdown", "fuelleak"};
	private ConfigAccessor config;
	private ConfigAccessor data;
	private String chosenEvent;
	
	public ShipEvent(ConfigAccessor config, ConfigAccessor data)
	{
		this.config = config;
		this.data = data;
	}

	public void chooseEvent()
	{
		Random rand = new Random();
		int totalSum = 0;
		int randomNum;
		int sum = 0;
		int i = 0;
		
		for (String e: EVENTS)
			totalSum += config.getConfig().getInt("events." + e + ".probability");
		
		randomNum = rand.nextInt(totalSum);
		
		while (sum < randomNum)
			sum += config.getConfig().getInt("events." + EVENTS[i++] + ".probability");
		
		//chosenEvent = EVENTS[i];
		chosenEvent = "breakdown";
	}
	
	public String trigger(Player player, String ship, Location location, int length, int width, int height)
	{
		switch(chosenEvent) {
			case "bumpyRide": return triggerBumpyRide(player, location, length, width, height);
			case "infestation": return triggerInfestation(player, location, length, width, height);
			case "breakdown": return triggerBreakdown(player, ship);
			case "fuelleak": return triggerFuelLeak(player);
			default:	Bukkit.getLogger().severe("Invalid event. Event cancelled.");
						return null;
		}
	}
	
	//TODO
	private String triggerBumpyRide(Player player, Location location, int length, int width, int height)
	{
		//Configuration options
		String path = "events.bumpyRide.";
		int damage = config.getConfig().getInt(path + "damage");
		String profession = config.getConfig().getString(path + "profession");
		Set<String> tiers = config.getConfig().getConfigurationSection(path + "injuryChance").getKeys(false);
		List<Double> injuryChances = new ArrayList<Double>();
		
		for (String t: tiers)
			injuryChances.add(config.getConfig().getDouble(path + "injuryChance." + t));
		
		//TODO: Determine pilot's skill
		int pilotSkill = 1;
		
		//Determine if injury occurs or not
		Random rand = new Random();
		double randomDoub = rand.nextDouble();
		
		//If not, notify and exit
		if (randomDoub < injuryChances.get(pilotSkill-1))
		{
			return "The ship creaks and shudders, battered with whorls of wind. " + player.getDisplayName() + " expertly manouevres"
					 + " the ship through the storm, and the tremors fade away.";
		}
		//If yes,
		else 
		{
			//Choose a player to damage
			Collection<? extends Player> onlinePlayers = Bukkit.getServer().getOnlinePlayers();
			List<Player> playersOnShip = new ArrayList<Player>();

			for (Player p: onlinePlayers)
				if (p.getWorld().equals(location.getWorld())
						&& p.getLocation().getX() >= location.getX() && p.getLocation().getX() <= location.getX() + length
						&& p.getLocation().getY() >= location.getY() && p.getLocation().getY() <= location.getY() + height
						&& p.getLocation().getZ() >= location.getZ() && p.getLocation().getZ() <= location.getZ() + width)
					playersOnShip.add(p);
			
			int randomInt = rand.nextInt(playersOnShip.size());
			
			//Damage player
			playersOnShip.get(randomInt).setHealth(playersOnShip.get(randomInt).getHealth() - damage);
			
			//Notify
			return "The ship creaks and shudders, battered with whorls of wind as " + player.getDisplayName() + " struggles to manouevre"
			 + " the ship. Suddenly it lurches to one side, throwing everyone to the floor. " + playersOnShip.get(randomInt).getDisplayName()
			 + " is injured!";
		}
	}
	
	//TODO
	private String triggerInfestation(Player player, Location location, int length, int width, int height)
	{
		//Configuration options
		String path = "events.infestation.";
		int number = config.getConfig().getInt(path + "number");
		boolean poisonous = config.getConfig().getBoolean(path + "poisonous");
		
		//Determine spawn point
		//Create list of locations that have a block directly below and somewhere above them.
		List<Location> potentialLocations = new ArrayList<Location>();
		boolean hasBlockAbove;
		Location testLoc;
		int yAbove;
		
		for (int x = location.getBlockX(); x < location.getX() + length; x++)
			for (int y = location.getBlockY(); y < location.getY() + height; y++)
				for (int z = location.getBlockZ(); z < location.getZ() + length; z++)
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
		{
			player.sendMessage("backup selection");
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
			player.sendMessage(spawnPoint.getBlockX() + " " + spawnPoint.getBlockY() + " " + spawnPoint.getBlockZ());
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
	
	//TODO
	private String triggerBreakdown(Player player, String ship)
	{
		//Configuration options
		String path = "events.breakdown.";
		List<String> spareParts = new ArrayList<String>();
		spareParts.addAll(config.getConfig().getConfigurationSection(path + "spareParts").getKeys(false));
		List<String> tools = new ArrayList<String>();
		tools.addAll(config.getConfig().getConfigurationSection(path + "tools").getKeys(false));
		
		//Set ship to broken
		data.getConfig().set("ships." + ship + ".broken", true);
		
		//Choose item needed for repair
		Random rand = new Random();
		int randomNum = rand.nextInt(spareParts.size() + tools.size());
		
		//Set item needed for repair and isConsumed
		boolean consumePart = false;
		String repairItem;
		if (randomNum < spareParts.size())
		{
			consumePart = true;
			repairItem = spareParts.get(randomNum);
		}
		else
			repairItem = tools.get(randomNum - spareParts.size());
		
		data.getConfig().set("ships." + ship + ".consumePart", consumePart);
		data.getConfig().set("ships." + ship + ".repairItem", repairItem);
		data.saveConfig();
		
		return "As you touch down, the ship engine splutters and dies. It's broken down!";
	}
	
	//TODO
	private String triggerFuelLeak(Player player)
	{
		return null;
		
	}

}
