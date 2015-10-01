package com.gmail.Rhisereld.HorizonShips;

import java.io.IOException;
import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.sk89q.worldedit.data.DataException;

@SuppressWarnings("deprecation")
public class ShipsCommandExecutor implements CommandExecutor 
{
	ConfigAccessor data;
	Ship ship;
	
	HashMap<String, String> confirmCreate = new HashMap<String, String>();	//Used to confirm commands
	
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
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
	{
		String name = sender.getName();
		Player player = Bukkit.getPlayer(name);
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

				sender.sendMessage(ChatColor.YELLOW + "A ship will be created using your current WorldEdit selection. Is this correct?"
						+ " Type '/ship confirm create' to confirm.");
				confirmCreate.put(name, args[1] + " " + args[2]);
				confirmCreateTimeout(sender);
				
				return true;
			}
			
			//ship confirm
			if (args[0].equalsIgnoreCase("confirm"))
			{
				if (args.length != 2)
				{
					sender.sendMessage(ChatColor.RED + "Incorrect format.");
					return true;
				}
				
				//ship confirm create
				if (args[1].equalsIgnoreCase("create"))
				{					
					if (confirmCreate.get(name) == null)
					{
						sender.sendMessage(ChatColor.RED + "There is nothing for you to confirm.");
						return true;
					}
					
					arguments = confirmCreate.get(name).split(" ");
					confirmCreate.remove(name);

					try {
						ship.createShip(arguments[0], player, arguments[1]);
					} catch (DataException | IOException e) {
						player.sendMessage(ChatColor.RED + "Couldn't create ship. Please report this to an Adminstrator.");
						e.printStackTrace();
						return false;
					} catch (NullPointerException e) {
						sender.sendMessage(ChatColor.RED + "No ship selected. Please make a selection using WorldEdit.");
						return false;
					} catch (IllegalArgumentException e) {
						sender.sendMessage(ChatColor.RED + "A ship already exists by that name.");
						return false;
					}

					player.sendMessage(ChatColor.YELLOW + "Ship " + arguments[0] + " created!");
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * confirmCreateTimeout() removes the player from the list of players who have a create command awaiting
	 * confirmation after 10 seconds.
	 * 
	 * @param sender
	 * @param key
	 */
	private void confirmCreateTimeout(final CommandSender sender)
	{
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable()
		{
			public void run() 
			{
				if (confirmCreate.containsKey(sender))
				{
					confirmCreate.remove(sender);
					sender.sendMessage(ChatColor.RED + "You timed out.");
				}
			}			
		} , 200);
	}

}
