package com.gmail.Rhisereld.HorizonShips;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements CommandExecutor
{
			
	@Override
	public void onEnable()
	{
		getLogger().info("Hello World!");
	}

	@Override
	public void onDisable()
	{
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		if(commandLabel.equalsIgnoreCase("hello"))
		{
			Player player = (Player) sender;
			String playerName = player.getName();
			
			player.sendMessage(ChatColor.GREEN + "Hello " + playerName);
			
			return true;
		}
		
		return false;
	}
}