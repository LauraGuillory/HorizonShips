package com.gmail.Rhisereld.HorizonShips;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ShipEvent 
{
	private String[] EVENTS = {"bumpyRide", "infestation", "breakdown", "fuelleak"};
	private ConfigAccessor config;
	private String chosenEvent;
	
	public ShipEvent(ConfigAccessor config)
	{
		this.config = config;
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
		chosenEvent = "bumpyRide";
	}
	
	public String trigger(Player player, Location location, int length, int width, int height)
	{
		switch(chosenEvent) {
			case "bumpyRide": return triggerBumpyRide(player, location, length, width, height);
			case "infestation": return triggerInfestation(player);
			case "breakdown": return triggerBreakdown(player);
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
	private String triggerInfestation(Player player)
	{
		return chosenEvent;
	}
	
	//TODO
	private String triggerBreakdown(Player player)
	{
		return chosenEvent;
		
	}
	
	//TODO
	private String triggerFuelLeak(Player player)
	{
		return chosenEvent;
		
	}

}
