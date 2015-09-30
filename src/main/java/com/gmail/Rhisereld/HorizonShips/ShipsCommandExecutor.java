package com.gmail.Rhisereld.HorizonShips;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.data.DataException;

public class ShipsCommandExecutor implements CommandExecutor 
{
    /**
     * onCommand() is called when a player enters a command recognised by Bukkit to belong to this plugin.
     * After that it is up to the contents of this method to determine what the commands do.
     * 
     * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
	{
		Player player;
		String name = sender.getName();
		String[] arguments;
		
		//All commands that fall under /ships [additional arguments]
		if (commandLabel.equalsIgnoreCase("ship"))
		{
			//ships test - used for testing during development.
			if (args[0].equalsIgnoreCase("test"))
			{
			}
		}
		return false;
	}

}
