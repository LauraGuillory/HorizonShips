package com.gmail.Rhisereld.HorizonShips;

import java.io.IOException;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.Rhisereld.HorizonProfessions.ProfessionAPI;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.data.DataException;

@SuppressWarnings("deprecation")
public class HorizonCommandParser implements CommandExecutor 
{
	ProfessionAPI prof;
	FileConfiguration data;
	FileConfiguration config;
	ShipHandler shipHandler;
	JavaPlugin plugin;
	
	HashMap<String, String> confirmCreate = new HashMap<String, String>();	//Used to confirm commands
	HashMap<String, String> confirmDelete = new HashMap<String, String>();
	HashMap<String, String> confirmRemoveDestination = new HashMap<String, String>();
	HashMap<String, String> confirmTransfer = new HashMap<String, String>();
	
    public HorizonCommandParser(ProfessionAPI prof, FileConfiguration data, FileConfiguration config, JavaPlugin plugin) 
    {
    	this.prof = prof;
		this.data = data;
		this.config = config;
		this.plugin = plugin;
		shipHandler = new ShipHandler(prof, data, config, plugin);
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
		//All commands that fall under /ship [additional arguments]
		if (commandLabel.equalsIgnoreCase("ship"))
		{
			//ship
			if (args.length == 0)
				return showCommands(sender);
			
			//ship reload
			if (args[0].equalsIgnoreCase("reload"))
				if (!(sender instanceof Player) || sender.hasPermission("horizonships.reload"))
				{
					config = new ConfigAccessor(plugin, "config.yml").getConfig();
					ShipHandler.updateConfig(config);
					
					sender.sendMessage(ChatColor.YELLOW + "Horizon Ships config reloaded.");
					return true;
				}
				else
				{
					sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
					return false;
				}
			
			//ship create [shipName]
			if (args[0].equalsIgnoreCase("create"))
				if (args.length >= 2)
					return shipCreate(sender, args);
				else
				{
					sender.sendMessage(ChatColor.RED + "Incorrect number of arguments! Correct usage: /ship create [shipName]");
					return false;
				}
			
			//ship delete [shipName]
			if (args[0].equalsIgnoreCase("delete"))
				if (args.length >= 2)
					return shipDelete(sender, args);
				else
				{
					sender.sendMessage(ChatColor.RED + "Incorrect number of arguments! Correct usage: /ship delete [shipName]");
					return false;
				}
			
			//ship add
			if (args[0].equalsIgnoreCase("add"))
			{	
				if (args.length < 2)
				{
					sender.sendMessage(ChatColor.RED + "Incorrect format!");
					return false;
				}
				
				//ship add destination [destinationName]
				if (args[1].equalsIgnoreCase("destination"))
					if (args.length >= 3)
						return addDestination(sender, args[2]);
					else
						sender.sendMessage(ChatColor.RED + "Incorrect number of arguments! Correct usage: /ship "
								+ "add destination [destinationName]");
				
				//ship add dock [destinationName]
				if (args[1].equalsIgnoreCase("dock"))
					if (args.length >= 3)
						return addDock(sender, args[2]);
					else
						sender.sendMessage(ChatColor.RED + "Incorrect number of arguments! Correct usage: /ship add dock "
								+ "[destinationName]");
				
				//ship add pilot [pilotName] [shipName] 
				if (args[1].equalsIgnoreCase("pilot"))
					if (args.length >= 4)
						return addPilot(sender, args);
					else
						sender.sendMessage(ChatColor.RED + "Incorrect number of arguments! Correct usage: /ship add pilot [pilotName] "
								+ "[shipName]");
				
				return false;
			}
			
			//ship remove
			if (args[0].equalsIgnoreCase("remove"))
			{
				if (args.length < 2)
				{
					sender.sendMessage(ChatColor.RED + "Incorrect format!");
					return false;
				}
				
				//ship remove destination [destinationName]
				if (args[1].equalsIgnoreCase("destination"))
					if (args.length >= 3)
						return removeDestination(sender, args[2]);
					else
						sender.sendMessage(ChatColor.RED + "Incorrect number of arguments! Correct usage: /ship "
								+ "remove destination [destinationName]");
				
				//ship remove dock [destinationName] [dockNumber]
				if (args[1].equalsIgnoreCase("dock"))
					if (args.length >= 4)
						return removeDock(sender, args);
					else
						sender.sendMessage(ChatColor.RED + "Incorrect number of arguments! Correct usage: /ship "
								+ "remove dock [destinationName] [dockNumber]");
				
				//ship remove pilot [pilotName] [shipName]
				if (args[1].equalsIgnoreCase("pilot"))
					if (args.length >= 4)
						return removePilot(sender, args);
					else
						sender.sendMessage(ChatColor.RED + "Incorrect number of arguments! Correct usage: /ship remove "
								+ "pilot pilotName] [shipName]");
				
				return false;
			}
			
			//ship list
			if (args[0].equalsIgnoreCase("list"))
			{
				if (args.length < 2)
				{
					sender.sendMessage(ChatColor.RED + "What do you want to list? Possible lists: ships, destinations, docks");
					return false;
				}
				
				//ship list ships
				if (args[1].equalsIgnoreCase("ships"))
					return listShips(sender);
				
				//ship list destinations
				if (args[1].equalsIgnoreCase("destinations"))
					return listDestinations(sender);
				
				//ship list docks [destination]
				if (args[1].equalsIgnoreCase("docks"))
				{
					if (args.length < 3)
					{
						sender.sendMessage(ChatColor.RED + "For which destination? Use /ship list docks [destination].");
						return false;
					}
					
					return listDocks(sender, args[2]);
				}
				
				sender.sendMessage(ChatColor.RED + "What do you want to list? Possible lists: ships, destinations, docks");
				return false;
			}
			
			//ship pilot [destination]
			if (args[0].equalsIgnoreCase("pilot"))
				if (args.length >= 2)
					return pilotShip(sender, args);
				else
				{
					sender.sendMessage(ChatColor.RED + "Incorrect number of arguments! Correct usage: /ship pilot "
							+ "[destinationName]");
					return true;
				}
			
			//ship diagnose
			if (args[0].equalsIgnoreCase("diagnose"))
				if (args.length == 1)
					return diagnoseShip(sender, args);
				else
				{
					sender.sendMessage(ChatColor.RED + "Incorrect number of arguments! Correct usage: /ship diagnose");
					return false;
				}
			
			//ship repair
			if (args[0].equalsIgnoreCase("repair"))
				if (args.length == 1)
					return repairShip(sender, args);
				else
				{
					sender.sendMessage(ChatColor.RED + "Incorrect number of arguments! Correct usage: /ship repair");
					return false;
				}
			
			//ship refuel
			if (args[0].equalsIgnoreCase("refuel"))
				if (args.length == 1)
					return refuelShip(sender, args);
				else
				{
					sender.sendMessage(ChatColor.RED + "Incorrect number of arguments! Correct usage: /ship refuel");
					return false;
				}
			
			//ship setowner [playerName] [shipName]
			if (args[0].equalsIgnoreCase("setowner"))
				if (args.length >= 3)
					return setOwner(sender, args);
				else
				{
					sender.sendMessage(ChatColor.RED + "Incorrect number of arguments! Correct usage: /ship setowner "
							+ "[playerName] [shipName]");
					return false;
				}
			
			//ship transfer [playerName] [shipName]
			if (args[0].equalsIgnoreCase("transfer"))
				if (args.length >= 3)
					return transferShip(sender, args);
				else
				{
					sender.sendMessage(ChatColor.RED + "Incorrect number of arguments! Correct usage: /ship transfer "
							+ "[playerName] [shipName]");
					return false;
				}
			
			//ship rename [shipName] [newShipName]
			if (args[0].equalsIgnoreCase("rename"))
				if (args.length == 3)
					return renameShip(sender, args[1], args[2]);
				else
				{
					sender.sendMessage(ChatColor.RED + "Incorrect number of arguments! Correct usage: /ship rename "
							+ "[shipName] [newShipName]");
					return false;
				}
			
			//ship tp [shipName]
			if (args[0].equalsIgnoreCase("tp"))
				if (args.length >= 2)
					return teleport(sender, args);
				else
				{
					sender.sendMessage(ChatColor.RED + "Incorrect number of arguments! Correct usage: /ship tp "
							+ "[shipName]");
					return false;
				}
			
			//ship force
			if (args[0].equalsIgnoreCase("force"))
				//ship force refuel [shipName]
				if (args[1].equalsIgnoreCase("refuel"))
					if (args.length == 3)
						return forceRefuel(sender, args[2]);
					else
					{
						sender.sendMessage(ChatColor.RED + "Incorrect number of arguments! Correct usage: /ship force refuel "
							+ "[shipName]");
						return false;
					}
				//ship force repair [shipName]
				else if (args[1].equalsIgnoreCase("repair"))
					if (args.length >= 3)
						return forceRepair(sender, args);
					else
					{
						sender.sendMessage(ChatColor.RED + "Incorrect number of arguments! Correct usage: /ship force repair "
								+ "[shipName]");
							return false;
					}
				//ship force break [shipName]
				else if (args[1].equalsIgnoreCase("break"))
					if (args.length >= 3)
						return forceBreak(sender, args);
					else
					{
						sender.sendMessage(ChatColor.RED + "Incorrect number of arguments! Correct usage: /ship force break "
								+ "[shipName]");
							return false;
					}
			
			//ship confirm
			if (args[0].equalsIgnoreCase("confirm"))
			{
				if (args.length < 2)
				{
					sender.sendMessage(ChatColor.RED + "Incorrect format.");
					return false;
				}
				
				//ship confirm create
				if (args[1].equalsIgnoreCase("create"))
					return confirmCreate(sender);

				//ship confirm delete
				if (args[1].equalsIgnoreCase("delete"))
					return confirmDelete(sender);
				
				//ship confirm remove
				if (args[1].equalsIgnoreCase("remove"))
					//ship confirm remove destination
					if (args[2].equalsIgnoreCase("destination"))
						return confirmRemoveDestination(sender);
				
				//ship confirm transfer
				if (args[1].equalsIgnoreCase("transfer"))
					return confirmTransfer(sender);
			}
			
			//ship cancel
			if (args[0].equalsIgnoreCase("cancel"))
				return cancelAction(sender);
			
			//ship [shipName]
			//If not any recognised command, assume ship and request information on it.
			return shipInfo(sender, args);
		}
		
		return false;
	}
	
	/**
	 * shipCreate() performs all the checks necessary for the /ship create command,
	 * then sets aside the action to be carried out when it is confirmed.
	 * 
	 * @param sender
	 * @param args
	 * @return
	 */
	private boolean shipCreate(CommandSender sender, String[] args)
	{
		Player player;
		
		//Check that the sender is a player
		if (sender instanceof Player)
			player = Bukkit.getPlayer(sender.getName());
		else
		{
			sender.sendMessage(ChatColor.RED + "This command cannot be used by the console.");
			return false;
		}
		
		//Check the player has permission
		if (!player.hasPermission("horizonships.admin.ship.create"))
		{
			sender.sendMessage(ChatColor.RED + "You don't have permission to create a ship.");
			return false;
		}

		sender.sendMessage(ChatColor.YELLOW + "A ship will be created using your current WorldEdit selection. Is this correct?"
				+ " Type '/ship confirm create' to confirm.");
		
		//Put together a string of the name.
		StringBuilder shipName = new StringBuilder();
		for (String s: args)
		{
			if (s.equalsIgnoreCase("create"))
				continue;
			shipName.append(s);
			shipName.append(" ");
		}
		shipName.deleteCharAt(shipName.length()-1);
		
		confirmCreate.put(player.getName(), shipName.toString());
		confirmCreateTimeout(sender);
		
		return true;
	}
	
	/**
	 * shipDelete() performs all the checks necessary for the /ship delete command,
	 * then sets aside the action to be carried out when it is confirmed.
	 * 
	 * @param sender
	 * @param args
	 * @return
	 */
	private boolean shipDelete(CommandSender sender, String[] args)
	{
		//Check the player has permission OR is the console
		if (!sender.hasPermission("horizonships.admin.ship.delete") && sender instanceof Player)
		{
			sender.sendMessage(ChatColor.RED + "You don't have permission to delete a ship.");
			return false;
		}
		
		//Put together a string of the name.
		StringBuilder sb = new StringBuilder();
		for (String s: args)
		{
			if (s.equalsIgnoreCase("delete"))
				continue;
			sb.append(s);
			sb.append(" ");
		}
		sb.deleteCharAt(sb.length()-1);
		
		sender.sendMessage(ChatColor.YELLOW + "Are you sure you want to delete the ship " + sb.toString() + "?"
				+ " Type '/ship confirm delete' to confirm.");
		
		confirmDelete.put(sender.getName(), sb.toString());
		confirmDeleteTimeout(sender);
		
		return true;
	}

	/**
	 * addDock() performs all the checks necessary for the /ship add dock command,
	 * then attempts to add a dock to a given destination.
	 * 
	 * @param sender
	 * @param destination
	 * @return
	 */
	private boolean addDock(CommandSender sender, String destination)
	{
		Player player;
		
		//Check that the sender is a player
		if (sender instanceof Player)
			player = Bukkit.getPlayer(sender.getName());
		else
		{
			sender.sendMessage(ChatColor.RED + "This command cannot be used by the console.");
			return false;
		}
		
		//Check that the player has permission
		if (!player.hasPermission("horizonships.admin.dock.add"))
		{
			sender.sendMessage(ChatColor.RED + "You don't have permission to create a destination.");
			return false;
		}
		
		int dockID = -1;
		try { dockID = shipHandler.addDock(player, destination); } 
		catch (IllegalArgumentException e)
		{
			sender.sendMessage(ChatColor.RED + e.getMessage());
			return false;
		}
		sender.sendMessage(ChatColor.YELLOW + "Ship dock created and assigned ID: " + dockID);
		return true;
	}
	
	/**
	 * removeDock() performs all the checks necessary for the /ship remove dock command,
	 * then sets aside the action to be carried out when it is confirmed.
	 * 
	 * @param sender
	 * @param args
	 * @return
	 */
	private boolean removeDock(CommandSender sender, String[] args)
	{
		String dock = args[3];
		String destination = args[2];
		
		//Check the player has permission OR is the console
		if (!sender.hasPermission("horizonships.admin.dock.remove") && sender instanceof Player)
		{
			sender.sendMessage(ChatColor.RED + "You don't have permission to remove a dock.");
			return false;
		}
		
		shipHandler.removeDock(sender, destination, dock);
		sender.sendMessage(ChatColor.YELLOW + "Ship dock with ID: " + dock + " removed.");
		return true;
	}
	
	/**
	 * addDestination() adds a new destination which is used to group docks.
	 * 
	 * @param sender
	 * @param destination
	 * @return
	 */
	private boolean addDestination(CommandSender sender, String destination)
	{
		Player player;
		
		//Check that the sender is a player
		if (sender instanceof Player)
			player = Bukkit.getPlayer(sender.getName());
		else
		{
			sender.sendMessage(ChatColor.RED + "This command cannot be used by the console.");
			return false;
		}
		
		//Check that the player has permission
		if (!player.hasPermission("horizonships.admin.destination.add"))
		{
			sender.sendMessage(ChatColor.RED + "You don't have permission to create a destination.");
			return false;
		}
		
		shipHandler.addDestination(destination);
		sender.sendMessage(ChatColor.YELLOW + "Empty destination " + destination + " created.");
		return true;
	}
	
	/**
	 * removeDestination() performs all the checks necessary for the /ship remove destination command,
	 * then sets aside the action to be carried out when it is confirmed.
	 * 
	 * @param sender
	 * @param args
	 * @return
	 */
	private boolean removeDestination(CommandSender sender, String destination)
	{
		//Check the player has permission OR is the console
		if (!sender.hasPermission("horizonships.admin.destination.remove") && sender instanceof Player)
		{
			sender.sendMessage(ChatColor.RED + "You don't have permission to remove a destination.");
			return false;
		}
		
		sender.sendMessage(ChatColor.YELLOW + "Are you sure you wish to remove the destination " + destination 
						+ "? All docks at this destination will be removed. Type '/ship confirm remove "
						+ "destination' to confirm.");
		confirmRemoveDestination.put(sender.getName(), destination);
		confirmRemoveDestinationTimeout(sender);
		return true;
	}
	
	/**
	 * addPilot() performs all the checks necessary for the /ship add pilot command,
	 * then calls the shipHandler class to carry out the action.
	 * 
	 * @param sender
	 * @param args
	 * @return
	 */
	private boolean addPilot(CommandSender sender, String[] args)
	{
		//Check that the sender has permission to add a pilot OR is the console
		if (!sender.hasPermission("horizonships.pilot.add") && !sender.hasPermission("horizonships.admin.pilot.remove") 
				&& sender instanceof Player)
		{
			sender.sendMessage(ChatColor.RED + "You don't have permission to add a pilot.");
			return false;
		}
		
		//Put together a string of the name.
		StringBuilder sb = new StringBuilder();
		for (int i = 3; i < args.length; i++)
		{
			sb.append(args[i]);
			sb.append(" ");
		}
		sb.deleteCharAt(sb.length()-1);
				
		//Add pilot
		try {
			shipHandler.addPilot(sender, sb.toString(), args[2]);
		} catch (IllegalArgumentException e) {
			sender.sendMessage(ChatColor.RED + e.getMessage());
			return false;
		}
		
		sender.sendMessage(ChatColor.YELLOW + "Pilot added.");
		
		return true;
	}
	
	/**
	 * removePilot() performs all the checks necessary for the /ship remove pilot command,
	 * then calls the shipHandler class to carry out the action.
	 * 
	 * @param sender
	 * @param args
	 * @return
	 */
	private boolean removePilot(CommandSender sender, String[] args)
	{
		//Check that the sender has permission to remove a pilot OR is the console
		if (!sender.hasPermission("horizonships.pilot.remove") && !sender.hasPermission("horizonships.admin.pilot.remove") 
				&& sender instanceof Player)
		{
			sender.sendMessage(ChatColor.RED + "You don't have permission to remove a pilot.");
			return false;
		}
		
		//Put together a string of the name.
		StringBuilder sb = new StringBuilder();
		for (int i = 3; i < args.length; i++)
		{
			sb.append(args[i]);
			sb.append(" ");
		}
		sb.deleteCharAt(sb.length()-1);
		
		//Remove pilot
		try {
			shipHandler.removePilot(sender, sb.toString(), args[2]);
		} catch (IllegalArgumentException e) {
			sender.sendMessage(ChatColor.RED + e.getMessage());
			return false;
		}
		
		sender.sendMessage(ChatColor.YELLOW + "Pilot removed.");
		
		return true;
	}
	
	/**
	 * listShips() performs all the checks necessary for the /ship list ships command,
	 * then calls the shipHandler class to carry out the action.
	 * 
	 * @param sender
	 * @param args
	 * @return
	 */
	private boolean listShips(CommandSender sender)
	{
		//Check that the player has permission OR is the console
		if (!sender.hasPermission("horizonships.list") && sender instanceof Player)
		{
			sender.sendMessage(ChatColor.RED + "You don't have permission to view this.");
			return false;
		}
		
		shipHandler.listShips(sender);
		return true;
	}
	
	/**
	 * listDestinations performs all the checks necessary for the /ship list destinations command,
	 * then calls the shipHandler class to carry out the action.
	 * 
	 * @param sender
	 * @return
	 */
	private boolean listDestinations(CommandSender sender)
	{
		//Check that the player has permission OR is the console
		if (!sender.hasPermission("horizonships.list") && sender instanceof Player)
		{
			sender.sendMessage(ChatColor.RED + "You don't have permission to view this.");
			return false;
		}
		
		shipHandler.listDestinations(sender);
		return true;
	}
	
	private boolean listDocks(CommandSender sender, String destination)
	{
		//Check that the player has permission OR is the console
		if (!sender.hasPermission("horizonships.list") && sender instanceof Player)
		{
			sender.sendMessage(ChatColor.RED + "You don't have permission to view this.");
			return false;
		}
		
		shipHandler.listDocks(sender, destination);
		return true;
	}
	
	/**
	 * pilotShip() performs all the checks necessary for the /ship pilot command,
	 * then calls the shipHandler class to carry out the action.
	 * 
	 * @param sender
	 * @param args
	 * @return
	 */
	private boolean pilotShip(CommandSender sender, String[] args)
	{
		Player player;
		
		//Check that the sender is a player
		if (sender instanceof Player)
			player = Bukkit.getPlayer(sender.getName());
		else
		{
			sender.sendMessage(ChatColor.RED + "This command cannot be used by the console.");
			return false;
		}
		
		//Check that the player has permission
		if (!sender.hasPermission("horizonships.pilot"))
		{
			sender.sendMessage(ChatColor.RED + "You don't have permission to pilot a ship.");
			return false;
		}
		
		String destination = args[1];
		
		for (int i = 2; i < args.length; i++)
			destination = destination + " " + args[i];
		
		try {
			shipHandler.moveShip(player, destination);
		} catch (DataException | IOException e) {
			player.sendMessage(ChatColor.RED + "Couldn't move ship. Please report this to an Adminstrator.");
			e.printStackTrace();
		} catch (MaxChangedBlocksException e) {
			player.sendMessage(ChatColor.RED + "Ship too large!");
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			player.sendMessage(ChatColor.RED + e.getMessage());
		}
		
		return true;
	}
	
	/**
	 * diagnoseShip() performs all the checks necessary for the /ship diagnose command,
	 * then calls the shipHandler class to carry out the action.
	 * 
	 * @param sender
	 * @param args
	 * @return
	 */
	private boolean diagnoseShip(CommandSender sender, String[] args)
	{
		Player player;
		
		//Check that the sender is a player
		if (sender instanceof Player)
			player = Bukkit.getPlayer(sender.getName());
		else
		{
			sender.sendMessage(ChatColor.RED + "This command cannot be used by the console.");
			return false;
		}
		
		//Check that the player has permission
		if (!player.hasPermission("horizonships.diagnose"))
		{
			sender.sendMessage(ChatColor.RED + "You don't have permission to diagnose a ship.");
			return false;
		}
		
		try {
			shipHandler.diagnose(player);
		} catch (IllegalArgumentException e) {
			player.sendMessage(ChatColor.RED + e.getMessage());
		}
		
		return true;
	}
	
	/**
	 * repairShip() performs all the checks necessary for the /ship repair command,
	 * then calls the shipHandler class to carry out the action.
	 * 
	 * @param sender
	 * @param args
	 * @return
	 */
	private boolean repairShip(CommandSender sender, String[] args)
	{
		Player player;
		
		//Check that the sender is a player
		if (sender instanceof Player)
			player = Bukkit.getPlayer(sender.getName());
		else
		{
			sender.sendMessage(ChatColor.RED + "This command cannot be used by the console.");
			return false;
		}
		
		//Check that the player has permission
		if (!player.hasPermission("horizonships.repair"))
		{
			sender.sendMessage("You don't have permission to repair a ship.");
			return false;
		}
		
		try {
			shipHandler.repair(player);
		} catch (IllegalArgumentException e) {
			player.sendMessage(ChatColor.RED + e.getMessage());
			return false;
		}	
		
		return true;
	}
	
	/**
	 * refuelShip() performs checks to ensure that the refuel command is valid, then calls
	 * shipHandler to perform the action of refuelling the ship.
	 * 
	 * @param sender
	 * @param args
	 * @return
	 */
	private boolean refuelShip(CommandSender sender, String[] args)
	{
		Player player;
		
		//Check that the sender is a player
		if (sender instanceof Player)
			player = Bukkit.getPlayer(sender.getName());
		else
		{
			sender.sendMessage(ChatColor.RED + "This command cannot be used by the console.");
			return false;
		}
		
		//Check that the player has permission
		if (!player.hasPermission("horizonships.refuel"))
		{
			sender.sendMessage(ChatColor.RED + "You don't have permission to refuel a ship.");
			return false;
		}
		
		try {
		shipHandler.refuel(player);
		} catch (IllegalArgumentException e) {
			player.sendMessage(ChatColor.RED + e.getMessage());
			return false;
		}
		
		return true;
	}
	
	/**
	 * shipInfo() displays some information about a ship to the sender.
	 * 
	 * @param sender
	 * @param args
	 * @return
	 */
	private boolean shipInfo(CommandSender sender, String[] args)
	{
		//Check that the sender has permission OR is the console
		if (!sender.hasPermission("horizonships.info") && sender instanceof Player)
		{
			sender.sendMessage(ChatColor.RED + "You don't have permission to view ship information.");
			return false;
		}
		
		try {
			shipHandler.shipInfo(sender, args[0]);
		} catch (IllegalArgumentException e) {
			sender.sendMessage(ChatColor.RED + e.getMessage());
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/**
	 * setOwner() performs checks to ensure the setowner command is valid, then calls shipHandler
	 * to perform the action of setting the owner.
	 * It is an administrator-only command.
	 * 
	 * @param sender
	 * @param args
	 * @return
	 */
	private boolean setOwner(CommandSender sender, String[] args)
	{
		//Check that the sender has permission OR is the console
		if (!sender.hasPermission("horizonships.admin.setowner") && sender instanceof Player)
		{
			sender.sendMessage(ChatColor.RED + "You don't have permission to set the owner of a ship.");
			return false;
		}
		
		//Put together a string of the name.
		StringBuilder sb = new StringBuilder();
		for (int i = 3; i < args.length; i++)
		{
			sb.append(args[i]);
			sb.append(" ");
		}
		sb.deleteCharAt(sb.length()-1);

		try {
			shipHandler.setOwner(sender, sb.toString(), args[1]);
		} catch (IllegalArgumentException e) {
			sender.sendMessage(ChatColor.RED + e.getMessage());
			return false;
		}
		
		sender.sendMessage(ChatColor.YELLOW + "New owner set.");
		
		return true;
	}
	
	/**
	 * transferShip() performs checks to ensure that the command is valid, then
	 * sets aside the action to be performed when it is confirmed.
	 * 
	 * @param sender
	 * @param args
	 * @return
	 */
	private boolean transferShip(CommandSender sender, String[] args)
	{
		Player player;
		
		//Check that the sender is a player
		if (sender instanceof Player)
			player = Bukkit.getPlayer(sender.getName());
		else
		{
			sender.sendMessage(ChatColor.RED + "This command cannot be used by the console.");
			return false;
		}
		
		//Check that they have permission
		if (!player.hasPermission("horizonships.transfer"))
		{
			sender.sendMessage(ChatColor.RED + "You don't have permission to transfer a ship to someone else.");
			return false;
		}
		
		//Put together a string of the name.
		StringBuilder sb = new StringBuilder();
		for (int i = 2; i < args.length; i++)
		{
			sb.append(args[i]);
			sb.append(" ");
		}
		sb.deleteCharAt(sb.length()-1);
		
		sender.sendMessage(ChatColor.YELLOW + "Are you sure that you want to transfer ownership of "
							+ sb.toString() + " to " + args[1] + "? You will lose all ownership rights and "
							+ "access to the ship. This action cannot be undone. Type /ship confirm "
							+ "transfer to confirm.");
		confirmTransfer.put(sender.getName(), args[1] + " " + sb.toString());
		confirmTransferTimeout(sender);
		return true;
	}
	
	/**
	 * renameShip() changes the name of the ship.
	 * 
	 * @param sender
	 * @param args
	 * @return
	 */
	private boolean renameShip(CommandSender sender, String oldName, String newName)
	{
		//Check that they have permission.
		if (!sender.hasPermission("horizonships.rename") && !sender.hasPermission("horizonships.admin.rename"))
		{
			sender.sendMessage(ChatColor.RED + "You don't have permission to rename a ship.");
			return false;
		}
		
		//Perform the action.
		ShipHandler shipHandler = new ShipHandler(prof, data, config, plugin);
		try { shipHandler.rename(sender, oldName, newName); }
		catch (IllegalArgumentException e)
		{
			sender.sendMessage(ChatColor.RED + e.getMessage());
			return false;
		} catch (DataException | IOException e) 
		{
			sender.sendMessage(ChatColor.RED + e.getMessage());
			sender.sendMessage(ChatColor.RED + "Couldn't create ship. Please report this to an Adminstrator.");
			e.printStackTrace();
			return false;
		}
		sender.sendMessage(ChatColor.YELLOW + "Ship renamed from " + oldName + " to " + newName);
		return true;
	}
	
	/**
	 * teleport() checks if the sender is a player and has permission and teleports the player
	 * to the current destination of the ship name given.
	 * 
	 * @param sender
	 * @param shipName
	 * @return
	 */
	private boolean teleport(CommandSender sender, String[] args)
	{
		//Check that they have permission and is a player
		if (!sender.hasPermission("horizonships.admin.teleport") || !(sender instanceof Player))
		{
			sender.sendMessage(ChatColor.RED + "You don't have permission to teleport to a ship.");
			return false;
		}
		
		//Put together a string of the name.
		StringBuilder sb = new StringBuilder();
		for (int i = 1; i < args.length; i++)
		{
			sb.append(args[i]);
			sb.append(" ");
		}
		sb.deleteCharAt(sb.length()-1);
		
		//Perform the action
		ShipHandler shipHandler = new ShipHandler(prof, data, config, plugin);
		try { shipHandler.teleport((Player) sender, sb.toString()); }
		catch (IllegalArgumentException e)
		{
			sender.sendMessage(ChatColor.RED + e.getMessage());
			return false;
		}
		sender.sendMessage(ChatColor.YELLOW + "Teleporting to ship: " + sb.toString());
		return true;
	}
	
	/**
	 * forceRefuel() sets the ship's fuel to full without taking any items or requiring the ship's owner.
	 * 
	 * @param sender
	 * @param shipName
	 * @return
	 */
	private boolean forceRefuel(CommandSender sender, String shipName)
	{
		//Check that they have permission
		if (!sender.hasPermission("horizonships.admin.forcerefuel"))
		{
			sender.sendMessage(ChatColor.RED + "You don't have permission to force a ship refuel.");
			return false;
		}
		
		//Perform the action
		try { new ShipHandler(prof, data, config, plugin).forceRefuel(shipName); }
		catch (IllegalArgumentException e)
		{
			sender.sendMessage(ChatColor.RED + e.getMessage());
			return false;
		}
		
		sender.sendMessage(ChatColor.YELLOW + "You forced " + shipName + " to refuel.");
		return true;
	}
	
	/**
	 * forceRepair() forces the ship to repair without taking any items or requiring any skill.
	 * 
	 * @param sender
	 * @param shipName
	 * @return
	 */
	private boolean forceRepair(CommandSender sender, String[] args)
	{
		//Check that they have permission
		if (!sender.hasPermission("horizonships.admin.forcerepair"))
		{
			sender.sendMessage(ChatColor.RED + "You don't have permission to force a ship repair.");
			return false;
		}
		
		//Put together a string of the name.
		StringBuilder sb = new StringBuilder();
		for (int i = 2; i < args.length; i++)
		{
			sb.append(args[i]);
			sb.append(" ");
		}
		sb.deleteCharAt(sb.length()-1);
		
		//Perform the action
		try { new ShipHandler(prof, data, config, plugin).forceRepair(sb.toString()); }
		catch (IllegalArgumentException e)
		{
			sender.sendMessage(ChatColor.RED + e.getMessage());
			return false;
		}
		
		sender.sendMessage(ChatColor.YELLOW + "You forced " + sb.toString() + " to repair.");
		return true;
	}
	
	/**
	 * forceBreak() forces a ship to break down.
	 * 
	 * @param sender
	 * @param shipName
	 * @return
	 */
	private boolean forceBreak(CommandSender sender, String[] args)
	{
		//Check that they have permission
		if (!sender.hasPermission("horizonships.admin.forcebreak"))
		{
			sender.sendMessage(ChatColor.RED + "You don't have permission to force a ship to break.");
			return false;
		}
		
		//Put together a string of the name.
		StringBuilder sb = new StringBuilder();
		for (int i = 2; i < args.length; i++)
		{
			sb.append(args[i]);
			sb.append(" ");
		}
		sb.deleteCharAt(sb.length()-1);
		
		//Perform the action
		try { new ShipHandler(prof, data, config, plugin).forceBreak(sb.toString()); }
		catch (IllegalArgumentException e)
		{
			sender.sendMessage(ChatColor.RED + e.getMessage());
			return false;
		}
		
		sender.sendMessage(ChatColor.YELLOW + "You forced " + sb.toString() + " to break.");
		return true;
	}
	
	/**
	 * confirmCreate() performs the delayed action for the /ship create command.
	 * 
	 * @param sender
	 * @param args
	 * @return
	 */
	private boolean confirmCreate(CommandSender sender)
	{
		Player player;
		String shipName = confirmCreate.get(sender.getName());
		
		//Check that the player actually has a delayed /ship create action.
		if (shipName == null)
		{
			sender.sendMessage(ChatColor.RED + "There is nothing for you to confirm.");
			return false;
		}
		
		confirmCreate.remove(sender.getName());
		player = Bukkit.getPlayer(sender.getName());

		try {	shipHandler.createShip(shipName, player);
				player.sendMessage(ChatColor.YELLOW + "Ship " + shipName + " created!");
		} catch (DataException | IOException e) {
			sender.sendMessage(ChatColor.RED + e.getMessage());
			player.sendMessage(ChatColor.RED + "Couldn't create ship. Please report this to an Adminstrator.");
			e.printStackTrace();
			return false;
		} catch (IllegalArgumentException e) {
			sender.sendMessage(ChatColor.RED + e.getMessage());
			return false;
		}
		
		return true;
	}
	
	/**
	 * confirmDelete() performs the delayed action for the /ship delete command.
	 * 
	 * @param sender
	 * @param args
	 * @return
	 */
	private boolean confirmDelete(CommandSender sender)
	{	
		String shipName = confirmDelete.get(sender.getName());
		
		if (shipName == null)
		{
			sender.sendMessage(ChatColor.RED + "There is nothing for you to confirm.");
			return false;
		}
		
		Bukkit.getLogger().info(shipName);
		
		Player player = Bukkit.getPlayer(sender.getName());
		
		try { shipHandler.deleteShip(sender, shipName); } 
		catch (IllegalArgumentException e)
		{
			sender.sendMessage(ChatColor.RED + e.getMessage());
			confirmDelete.remove(sender.getName());
			return false;
		} 
		catch (IOException e) 
		{
			sender.sendMessage(ChatColor.RED + e.getMessage());
			player.sendMessage(ChatColor.RED + "Couldn't create ship. Please report this to an Adminstrator.");
			confirmDelete.remove(sender.getName());
			e.printStackTrace();
			return false;
		}
		
		confirmDelete.remove(sender.getName());
		sender.sendMessage(ChatColor.YELLOW + "Ship deleted.");
		return true;
	}
	
	/**
	 * confirmRemoveDestination() performs the delayed action for the /ship remove destination command.
	 * 
	 * @param sender
	 * @param args
	 * @return
	 */
	private boolean confirmRemoveDestination(CommandSender sender)
	{
		String name = sender.getName();
		
		if (confirmRemoveDestination.get(name) == null)
		{
			sender.sendMessage(ChatColor.RED + "There is nothing for you to confirm.");
			return false;
		}

		//Remove destination
		String destination = confirmRemoveDestination.get(name);
		confirmRemoveDestination.remove(name);
		try {
			shipHandler.removeDestination(destination);
		} catch (IllegalArgumentException e) {
			sender.sendMessage(ChatColor.RED + e.getMessage());
			return false;
		}

		sender.sendMessage(ChatColor.YELLOW + "Destination removed.");
		return true;
	}

	/**
	 * confirmTransfer() calls shipHandler to perform the action of transferring ownership
	 * of a ship, that has previously been set aside by transferShip()
	 * 
	 * @param sender
	 * @param args
	 * @return
	 */
	private boolean confirmTransfer(CommandSender sender)
	{
		String name = sender.getName();
		
		if (confirmTransfer.get(name) == null)
		{
			sender.sendMessage(ChatColor.RED + "There is nothing for you to confirm.");
			return false;
		}
		
		Player player = Bukkit.getPlayer(name);
		String[] arguments = confirmTransfer.get(name).split(" ");
		confirmTransfer.remove(name);
		
		//Put together a string of the name.
		StringBuilder sb = new StringBuilder();
		for (int i = 1; i < arguments.length; i++)
		{
			sb.append(arguments[i]);
			sb.append(" ");
		}
		sb.deleteCharAt(sb.length()-1);
		
		try {
			shipHandler.transfer(player, sb.toString(), arguments[0]);
		} catch (IllegalArgumentException e) {
			sender.sendMessage(ChatColor.RED + e.getMessage());
			return false;
		}
		sender.sendMessage(ChatColor.YELLOW + "You transfer ownership of " + sb.toString() + " to " + arguments[0] + ".");
		return false;
	}
	
	/**
	 * cancelAction() cancels all pending actions by removing the player's name
	 * from all pending action lists.
	 * 
	 * @param sender
	 * @param args
	 * @return
	 */
	private boolean cancelAction(CommandSender sender)
	{
		String name = sender.getName();
		
		if (confirmCreate.containsKey(name))
		{
			confirmCreate.remove(name);
			sender.sendMessage(ChatColor.YELLOW + "Ship creation cancelled.");
			return true;
		}
		
		if (confirmDelete.containsKey(name))
		{
			confirmDelete.remove(name);
			sender.sendMessage(ChatColor.YELLOW + "Ship deletion cancelled.");
			return true;
		}
		
		if (confirmRemoveDestination.containsKey(name))
		{
			confirmRemoveDestination.remove(name);
			sender.sendMessage(ChatColor.YELLOW + "Destination removal cancelled.");
			return true;
		}
		
		sender.sendMessage(ChatColor.RED + "There is nothing to cancel.");
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
				if (confirmCreate.containsKey(sender.getName()))
				{
					confirmCreate.remove(sender.getName());
					sender.sendMessage(ChatColor.RED + "You timed out.");
				}
			}
		} , 400);
	}

	/**
	 * confirmDeleteTimeout() removes the player from the list of players who have a delete command awaiting
	 * confirmation after 10 seconds.
	 * 
	 * @param sender
	 * @param key
	 */
	private void confirmDeleteTimeout(final CommandSender sender)
	{
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable()
		{
			public void run()
			{
				if (confirmDelete.containsKey(sender.getName()))
				{
					confirmDelete.remove(sender.getName());
					sender.sendMessage(ChatColor.RED + "You timed out.");
				}
			}			
		} , 400);
	}
	
	/**
	 * confirmRemoveDestinationTimeout() removes the player from the list of players who are in the process
	 * of removing a destination.
	 * 
	 * @param sender
	 */
	private void confirmRemoveDestinationTimeout(final CommandSender sender)
	{
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable()
		{
			public void run()
			{
				if (confirmRemoveDestination.containsKey(sender.getName()))
				{
					confirmRemoveDestination.remove(sender.getName());
					sender.sendMessage(ChatColor.RED + "You timed out.");
				}
			}			
		} , 400);
	}
	
	/**
	 * confirmTransferTimeout() removes the player from the list of players who are in the process
	 * of transfering ownership of their ship.
	 * 
	 * @param sender
	 */
	private void confirmTransferTimeout(final CommandSender sender)
	{
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable()
		{
			public void run()
			{
				if (confirmTransfer.containsKey(sender.getName()))
				{
					confirmTransfer.remove(sender.getName());
					sender.sendMessage(ChatColor.RED + "You timed out.");
				}
			}			
		} , 400);
	}
	
	/**
	 * showCommands() displays all available commands to the sender.
	 * Commands are only displayed if the sender has permission to use them.
	 * 
	 * @param sender
	 * @return
	 */
	private boolean showCommands(CommandSender sender)
	{
		Player player = null;
		
		if (sender instanceof Player)
			player = Bukkit.getPlayer(sender.getName()); //Some commands are not available to console - which is not a player
		
		if (player != null)
			sender.sendMessage("-----------<" + ChatColor.GOLD + " Horizon Ships Commands " + ChatColor.WHITE + ">-----------");
		else
			sender.sendMessage("----------------<" + ChatColor.GOLD + " Horizon Ships Commands " + ChatColor.WHITE + ">----------------");
			
		sender.sendMessage(ChatColor.GOLD + "Horizon Ships allows you to maintain and travel in ships!");
		if (sender.hasPermission("horizonships.admin.ship.create") && player != null)
		{
			sender.sendMessage(ChatColor.YELLOW + "/ship create [shipName]");
			sender.sendMessage("Create a new ship at a starter destination, using your current WorldEdit selection.");
		}
		if (sender.hasPermission("horizonships.admin.ship.delete"))
		{
			sender.sendMessage(ChatColor.YELLOW + "/ship delete [shipName]");
			sender.sendMessage("Delete a ship.");
		}
		if (sender.hasPermission("horizonships.admin.destination.add") && player != null)
		{
			sender.sendMessage(ChatColor.YELLOW + "/ship add destination [destinationName]");
			sender.sendMessage("Add a new destination, using your current WorldEdit selection.");
		}
		if (sender.hasPermission("horizonships.admin.destination.remove"))
		{
			sender.sendMessage(ChatColor.YELLOW + "/ship remove destination [destinationName]");
			sender.sendMessage("Remove a destination. Ships currently at this destination will be assigned a temporary "
					+ "dock until they leave.");
		}
		if (sender.hasPermission("horizonships.admin.dock.add") && player != null)
		{
			sender.sendMessage(ChatColor.YELLOW + "/ship add dock [destinationName]");
			sender.sendMessage("Add a new dock at the given destination, using your current WorldEdit selection.");
		}
		if (sender.hasPermission("horizonships.admin.dock.remove"))
		{
			sender.sendMessage(ChatColor.YELLOW + "/ship remove dock [destinationName]");
			sender.sendMessage("Remove a dock. Ships currently at this dock will be assigned a temporary "
					+ "dock until they leave.");
		}
		if (sender.hasPermission("horizonships.admin.forcerefuel"))
		{
			sender.sendMessage(ChatColor.YELLOW + "/ship force refuel [shipName]");
			sender.sendMessage("Force the ship to refuel to full without taking any items.");
		}
		if (sender.hasPermission("horizonships.admin.forcerepair"))
		{
			sender.sendMessage(ChatColor.YELLOW + "/ship force repair [shipName]");
			sender.sendMessage("Force the ship to repair without taking any items.");
		}
		if (sender.hasPermission("horizonships.admin.forcebreak"))
		{
			sender.sendMessage(ChatColor.YELLOW + "/ship force break [shipName]");
			sender.sendMessage("Force the ship to break down.");
		}
		if (sender.hasPermission("horizonships.pilot.add"))
		{
			sender.sendMessage(ChatColor.YELLOW + "/ship add pilot [pilotName] [shipName]");
			sender.sendMessage("Add a pilot to the authentication list of the ship that allows them to pilot it.");
		}
		if (sender.hasPermission("horizonships.pilot.remove"))
		{
			sender.sendMessage(ChatColor.YELLOW + "/ship remove pilot [pilotName] [shipName]");
			sender.sendMessage("Remove a pilot from the authentication list of the ship that allows them to pilot it.");
		}
		if (sender.hasPermission("horizonships.admin.setowner"))
		{
			sender.sendMessage(ChatColor.YELLOW + "/ship setowner [playerName] [shipName]");
			sender.sendMessage("Sets the player as the owner of the ship.");
		}
		if (sender.hasPermission("horizonships.admin.teleport"))
		{
			sender.sendMessage(ChatColor.YELLOW + "/ship tp [shipName]");
			sender.sendMessage("Teleport to the ship's current destination.");
		}
		if (sender.hasPermission("horizonships.list"))
		{
			sender.sendMessage(ChatColor.YELLOW + "/ship list ships/destinations/docks");
			sender.sendMessage("Provides a list of all current ships, destinations or docks.");
		}
		if (sender.hasPermission("horizonships.info"))
		{
			sender.sendMessage(ChatColor.YELLOW + "/ship [shipName]");
			sender.sendMessage("Provides general information about a ship.");
		}
		if (sender.hasPermission("horizonships.pilot") && player != null)
		{
			sender.sendMessage(ChatColor.YELLOW + "/ship pilot [destination]");
			sender.sendMessage("Pilot the ship that you are currently within to a destination of your choice.");
		}
		if (sender.hasPermission("horizonships.diagnose") && player != null)
		{
			sender.sendMessage(ChatColor.YELLOW + "/ship diagnose");
			sender.sendMessage("Examine the ship you are currently within to discover any mechanical defects.");
		}
		if (sender.hasPermission("horizonships.repair") && player != null)
		{
			sender.sendMessage(ChatColor.YELLOW + "/ship repair");
			sender.sendMessage("Use the item in your active hand to repair the ship.");
		}
		if (sender.hasPermission("horizonships.refuel") && player != null)
		{
			sender.sendMessage(ChatColor.YELLOW + "/ship refuel");
			sender.sendMessage("Use the item in your active hand to refuel the ship.");
		}
		if (sender.hasPermission("horizonships.transfer") && player != null)
		{
			sender.sendMessage(ChatColor.YELLOW + "/ship transfer [playerName] [shipName]");
			sender.sendMessage("Transfer ownership of a ship to another player.");
		}	
		if (sender.hasPermission("horizonships.rename"))
		{
			sender.sendMessage(ChatColor.YELLOW + "/ship rename [shipName] [newshipName]");
			sender.sendMessage("Rename a ship.");
		}
		sender.sendMessage(ChatColor.YELLOW + "/ship cancel");
		sender.sendMessage("Cancel any actions that are currently awaiting confirmation.");
		
		return true;
	}
}
