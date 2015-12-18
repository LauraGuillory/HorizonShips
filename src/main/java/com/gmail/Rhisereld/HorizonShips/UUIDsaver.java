package com.gmail.Rhisereld.HorizonShips;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class UUIDsaver implements Listener
{
	FileConfiguration data;
	
	public UUIDsaver(FileConfiguration data)
	{
		this.data = data;
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onJoin(PlayerJoinEvent event)
	{
		//Add the player's name and UUID to file.
		data.set("uuids." + event.getPlayer().getUniqueId(), event.getPlayer().getName());
	}
}
