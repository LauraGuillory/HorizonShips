package com.gmail.Rhisereld.HorizonShips;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.fusesource.jansi.Ansi.Color;

import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.data.DataException;

public class ShipsCommandExecutor implements CommandExecutor 
{
	ConfigAccessor data;
	Ship ship;
	
    public ShipsCommandExecutor(ConfigAccessor data) 
    {
		this.data = data;
		ship = new Ship(data);
	}

	/**
     * onCommand() is called when a player enters a command recognised by Bukkit to belong to this plugin.
     * After that it is up to the contents of this method to determine what the commands do.
     * 
     * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
	{
		Player player;
		String name = sender.getName();
		String[] arguments;
		
		//All commands that fall under /ship [additional arguments]
		if (commandLabel.equalsIgnoreCase("ship"))
		{
			//ship test - used for testing during development.
			if (args[0].equalsIgnoreCase("test"))
			{
			}
			
			//ship create [shipName] [destinationName]
			if (args[0].equalsIgnoreCase("create"))
			{
				player = Bukkit.getPlayer(name);
				SchematicManager sm = new SchematicManager(player.getWorld());
				
				//Check for correct number of arguments.
				if (args.length < 3)
				{
					player.sendMessage(ChatColor.RED + "Too few arguments! Correct usage: /ship create [shipName] [destinationName]");
					return false;
				}
				if (args.length > 3)
				{
					sender.sendMessage(ChatColor.RED + "Too many arguments! Correct usage: /ship create [shipName] [destinationName]");
					return false;
				}
				
				try {
					ship.createShip(args[1], player, args[2]);
				} catch (DataException | IOException e) {
					player.sendMessage(Color.RED + "Couldn't create ship. Please report this to an Adminstrator.");
					e.printStackTrace();
					return false;
				} catch (NullPointerException e) {
					sender.sendMessage(ChatColor.RED + "No ship selected. Please make a selection using WorldEdit.");
					return false;
				} catch (IllegalArgumentException e) {
					sender.sendMessage(ChatColor.RED + "A ship already exists by that name.");
					return false;
				}
				
				return true;
			}
		}
		return false;
	}

}
