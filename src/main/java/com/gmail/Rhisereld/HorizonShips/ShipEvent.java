package com.gmail.Rhisereld.HorizonShips;

import java.util.Random;

import org.bukkit.Bukkit;

public class ShipEvent 
{
	private String[] EVENTS = {"bumpyride", "infestation", "breakdown", "fuel"};
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
		
		chosenEvent = EVENTS[i];
		Bukkit.getServer().getLogger().info(chosenEvent);
	}
	
	public void trigger()
	{
		
	}

}
