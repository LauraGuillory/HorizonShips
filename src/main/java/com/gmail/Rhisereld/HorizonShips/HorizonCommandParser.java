package com.gmail.Rhisereld.HorizonShips;

import java.io.IOException;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.regions.RegionOperationException;

@SuppressWarnings("deprecation")
public class HorizonCommandParser implements CommandExecutor 
{
	ConfigAccessor data;
	ConfigAccessor config;
	ShipHandler shipHandler;
	JavaPlugin plugin;
	
	HashMap<String, String> confirmCreate = new HashMap<String, String>();	//Used to confirm commands
	HashMap<String, String> confirmDelete = new HashMap<String, String>();
	HashMap<String, String> confirmAddDestination = new HashMap<String, String>();
	HashMap<String, String> confirmRemoveDestination = new HashMap<String, String>();
	HashMap<String, String> confirmAdjust = new HashMap<String, String>();
	HashMap<String, String> confirmTransfer = new HashMap<String, String>();
	
    public HorizonCommandParser(ConfigAccessor data, ConfigAccessor config, JavaPlugin plugin) 
    {
		this.data = data;
		this.config = config;
		this.plugin = plugin;
		shipHandler = new ShipHandler(data, config, plugin);
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
			
			//ship test
			if (args[0].equalsIgnoreCase("test"))
			{
				
			}
			
			//ship create [shipName] [destinationName]
			if (args[0].equalsIgnoreCase("create"))
				if (args.length == 3)
					return shipCreate(sender, args);
				else
				{
					sender.sendMessage(ChatColor.RED + "Incorrect number of arguments! Correct usage: /ship create [shipName] [destinationName]");
					return false;
				}
			
			//ship delete [shipName]
			if (args[0].equalsIgnoreCase("delete"))
				if (args.length == 2)
					return shipDelete(sender, args);
				else
				{
					sender.sendMessage(ChatColor.RED + "Incorrect number of arguments! Correct usage: /ship delete [shipName]");
					return false;
				}
			
			//ship add
			if (args[0].equalsIgnoreCase("add"))
			{	
				//ship add destination [shipName] [destinationName]
				if (args.length >= 4 && args[1].equalsIgnoreCase("destination"))
					return addDestination(sender, args);
				
				sender.sendMessage(ChatColor.RED + "Incorrect number of arguments! Correct usage: /ship add destination [shipName] "
									+ "[destinationName]");
				return false;
			}
			
			//ship remove
			if (args[0].equalsIgnoreCase("remove"))
			{
				//ship remove destination [shipName] [destinationName]
				if (args.length >= 4 && args[1].equalsIgnoreCase("destination"))
					return removeDestination(sender, args);
				
				sender.sendMessage(ChatColor.RED + "Incorrect number of arguments! Correct usage: /ship remove destination [shipName] "
									+ "[destinationName]");
				return false;
			}
			
			//ship adjust [north/east/south/west/up/down]
			if (args[0].equalsIgnoreCase("adjust"))
				if (args.length == 2)
					return adjustDestination(sender, args);
				else
				{
					sender.sendMessage(ChatColor.RED + "Incorrect number of arguments! Correct usage: /ship adjust "
							+ "[north/east/south/west/up/down]");
					return false;
				}
			
			//ship list
			if (args[0].equalsIgnoreCase("list"))
				return listShips(sender, args);
			
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
			
			//ship setowner [shipName] [playerName]
			if (args[0].equalsIgnoreCase("setowner"))
				if (args.length == 3)
					return setOwner(sender, args);
				else
				{
					sender.sendMessage(ChatColor.RED + "Incorrect number of arguments! Correct usage: /ship setowner "
							+ "[shipName] [playerName]");
					return false;
				}
			
			//ship transfer [shipName] [playerName]
			if (args[0].equalsIgnoreCase("transfer"))
				if (args.length == 3)
					return transferShip(sender, args);
				else
				{
					sender.sendMessage(ChatColor.RED + "Incorrect number of arguments! Correct usage: /ship transfer "
							+ "[shipName] [playerName]");
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
					return confirmCreate(sender, args);

				//ship confirm delete
				if (args[1].equalsIgnoreCase("delete"))
					return confirmDelete(sender, args);
				
				//ship confirm add destination
				if (args[1].equalsIgnoreCase("add"))
					if (args.length >= 3 && args[2].equalsIgnoreCase("destination"))
						return confirmAddDestination(sender, args);
					else
					{
						sender.sendMessage(ChatColor.RED + "Incorrect format.");
						return true;
					}

				//ship confirm remove destination
				if (args[1].equalsIgnoreCase("remove"))
					if (args.length >= 3 && args[2].equalsIgnoreCase("destination"))
						confirmRemoveDestination(sender, args);
					else
					{
						sender.sendMessage(ChatColor.RED + "Incorrect format.");
						return true;
					}

				//ship confirm adjust
				if (args[1].equalsIgnoreCase("adjust"))
					return confirmAdjust(sender, args);
				
				//ship confirm transfer
				if (args[1].equalsIgnoreCase("transfer"))
					return confirmTransfer(sender, args);
			}
			
			//ship cancel
			if (args[0].equalsIgnoreCase("cancel"))
				return cancelAction(sender, args);
			
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
		confirmCreate.put(player.getName(), args[1] + " " + args[2]);
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
		if (!sender.hasPermission("horizonships.admin.ship.delete") && !(sender instanceof Player))
		{
			sender.sendMessage(ChatColor.RED + "You don't have permission to delete a ship.");
			return false;
		}
		
		sender.sendMessage(ChatColor.YELLOW + "Are you sure you want to delete the ship " + args[1] + "?"
				+ " Type '/ship confirm delete' to confirm.");
		confirmDelete.put(sender.getName(), args[1]);
		confirmDeleteTimeout(sender);
		
		return true;
	}
	
	/**
	 * addDestination() performs all the checks necessary for the /ship add destination command,
	 * then sets aside the action to be carried out when it is confirmed.
	 * 
	 * @param sender
	 * @param args
	 * @return
	 */
	private boolean addDestination(CommandSender sender, String[] args)
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
		
		sender.sendMessage(ChatColor.YELLOW + "The ship will be pasted inside your current WorldEdit selection. Is this correct? "
				+ " Type '/ship confirm add destination' to confirm.");
		confirmAddDestination.put(player.getName(), args[2] + " " + args[3]);
		confirmAddDestinationTimeout(sender);
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
	private boolean removeDestination(CommandSender sender, String[] args)
	{
		//Check the player has permission OR is the console
		if (!sender.hasPermission("horizonships.admin.destination.remove") && !(sender instanceof Player))
		{
			sender.sendMessage(ChatColor.RED + "You don't have permission to remove a destination.");
			return false;
		}
		
		sender.sendMessage(ChatColor.YELLOW + "Are you sure you wish to remove the destination " + args[3] + " from "
				+ args[2] + "? Type '/ship confirm remove destination' to confirm.");
		confirmRemoveDestination.put(sender.getName(), args[2] + " " + args[3]);
		confirmRemoveDestinationTimeout(sender);
		return true;
	}
	
	/**
	 * adjustDestination() performs all the checks necessary for the /ship adjust [direction] command,
	 * then calls the shipHandler class to carry out the action.
	 * 
	 * @param sender
	 * @param args
	 * @return
	 */
	private boolean adjustDestination(CommandSender sender, String[] args)
	{		
		//Check there's something to adjust
		if (!confirmAdjust.containsKey(sender.getName()))
		{
			sender.sendMessage(ChatColor.RED + "You are not currently adjusting a destination!");
			return false;
		}
		
		//Adjust
		Player player = Bukkit.getPlayer(sender.getName()); //Sender must be a player if their name is in the list.
		String [] arguments = confirmAdjust.get(player.getName()).split(" ");
		
		try {
			shipHandler.adjustDestination(player, args[1], arguments[0]);
		} catch (MaxChangedBlocksException e) {
			sender.sendMessage(ChatColor.RED + "Ship too large!");
			return false;
		} catch (DataException | RegionOperationException | IncompleteRegionException | IOException e) {
			sender.sendMessage(ChatColor.RED + e.getMessage());
			sender.sendMessage(ChatColor.RED + "Unable to adjust destination. Please contact an administrator.");
			e.printStackTrace();
			return false;
		} catch (IllegalArgumentException e) {
			sender.sendMessage(ChatColor.RED + e.getMessage());
			return false;
		}
		
		return true;
	}
	
	/**
	 * listShips() performs all the checks necessary for the /ship list command,
	 * then calls the shipHandler class to carry out the action.
	 * 
	 * @param sender
	 * @param args
	 * @return
	 */
	private boolean listShips(CommandSender sender, String[] args)
	{
		//Check that the player has permission OR is the console
		if (!sender.hasPermission("horizonships.list") && !(sender instanceof Player))
		{
			sender.sendMessage(ChatColor.RED + "You don't have permission to view this.");
			return false;
		}
		
		shipHandler.listShips(sender);
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
		if (!sender.hasPermission("horizonships.info") && !(sender instanceof Player))
		{
			sender.sendMessage(ChatColor.RED + "You don't have permission to view this.");
			return false;
		}
		
		try {
			shipHandler.shipInfo(sender, args[0]);
		} catch (IllegalArgumentException e) {
			sender.sendMessage(ChatColor.RED + e.getMessage());
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
		if (!sender.hasPermission("horizonships.admin.setowner") && !(sender instanceof Player))
		{
			sender.sendMessage(ChatColor.RED + "You don't have permission to set the owner of a ship.");
			return false;
		}

		try {
			shipHandler.setOwner(sender, args[1], args[2]);
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
	public boolean transferShip(CommandSender sender, String[] args)
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
		
		sender.sendMessage(ChatColor.YELLOW + "Are you sure that you want to transfer ownership of "
							+ args[1] + " to " + args[2] + "? You will lose all ownership rights and "
							+ "access to the ship. This action cannot be undone. Type /ship confirm "
							+ "transfer to confirm.");
		confirmTransfer.put(sender.getName(), args[1] + " " + args[2]);
		confirmTransferTimeout(sender);
		return true;
	}
	
	/**
	 * confirmCreate() performs the delayed action for the /ship create command.
	 * 
	 * @param sender
	 * @param args
	 * @return
	 */
	private boolean confirmCreate(CommandSender sender, String[] args)
	{
		Player player;
		
		if (confirmCreate.get(sender.getName()) == null)
		{
			sender.sendMessage(ChatColor.RED + "There is nothing for you to confirm.");
			return false;
		}
		
		String[] arguments = confirmCreate.get(sender.getName()).split(" ");
		confirmCreate.remove(sender.getName());
		player = Bukkit.getPlayer(sender.getName());

		try {
			shipHandler.createShip(arguments[0], player, arguments[1]);
			player.sendMessage(ChatColor.YELLOW + "Ship " + arguments[0] + " created!");
		} catch (DataException | IOException e) {
			sender.sendMessage(ChatColor.RED + e.getMessage());
			player.sendMessage(ChatColor.RED + "Couldn't create ship. Please report this to an Adminstrator.");
			e.printStackTrace();
			return false;
		} catch (NullPointerException e) {
			sender.sendMessage(ChatColor.RED + e.getMessage());
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
	private boolean confirmDelete(CommandSender sender, String[] args)
	{	
		if (confirmDelete.get(sender.getName()) == null)
		{
			sender.sendMessage(ChatColor.RED + "There is nothing for you to confirm.");
			return false;
		}
		
		Player player = Bukkit.getPlayer(sender.getName());
		confirmDelete.remove(sender.getName());
		
		try {
		shipHandler.deleteShip(sender, confirmDelete.get(sender.getName()));
		} catch (IllegalArgumentException e) {
			sender.sendMessage(ChatColor.RED + e.getMessage());
			return false;
		} catch (IOException e) {
			sender.sendMessage(ChatColor.RED + e.getMessage());
			player.sendMessage(ChatColor.RED + "Couldn't create ship. Please report this to an Adminstrator.");
			e.printStackTrace();
			return false;
		}
		
		sender.sendMessage(ChatColor.YELLOW + "Ship deleted.");
		return true;
	}
	
	/**
	 * confirmAddDestination() performs the delayed action for the /ship add destination command.
	 * 
	 * @param sender
	 * @param args
	 * @return
	 */
	private boolean confirmAddDestination(CommandSender sender, String[] args)
	{
		Player player;
		String name = sender.getName();
		
		if (confirmAddDestination.get(name) == null)
		{
			sender.sendMessage(ChatColor.RED + "There is nothing for you to confirm.");
			return false;
		}

		//Paste ship
		player = Bukkit.getPlayer(name);
		String[] arguments = confirmAddDestination.get(name).split(" ");
		try {
			shipHandler.testDestination(player, arguments[0], arguments[1]);
		} catch (DataException | IOException e) {
			sender.sendMessage(ChatColor.RED + e.getMessage());
			player.sendMessage(ChatColor.RED + "Couldn't paste ship. Please report this to an Adminstrator.");
			confirmAddDestination.remove(name);
			e.printStackTrace();
			return false;
		} catch (MaxChangedBlocksException e) {
			player.sendMessage(ChatColor.RED + "Ship too large!");
			confirmAddDestination.remove(name);
			e.printStackTrace();
			return false;
		} catch (NullPointerException | IllegalArgumentException e) {
			player.sendMessage(ChatColor.RED + e.getMessage());
			confirmAddDestination.remove(name);
			e.printStackTrace();
			return false;
		}
	
		sender.sendMessage(ChatColor.YELLOW + "Ship pasted for reference. Adjust the destination of the ship using "
			+ "'/ship adjust [north/east/south/west/up/down'. To confirm placement, type "
			+ "'/ship confirm adjust'.");
	
		//Remove confirmation for destination, add confirmation for tweaking
		confirmAdjust.put(name, confirmAddDestination.get(name));
		confirmAdjustTimeout(sender);
		confirmAddDestination.remove(name);
		return true;
	}
	
	/**
	 * confirmRemoveDestination() performs the delayed action for the /ship remove destination command.
	 * 
	 * @param sender
	 * @param args
	 * @return
	 */
	private boolean confirmRemoveDestination(CommandSender sender, String[] args)
	{
		String name = sender.getName();
		
		if (confirmRemoveDestination.get(name) == null)
		{
			sender.sendMessage(ChatColor.RED + "There is nothing for you to confirm.");
			return false;
		}

		//Remove destination
		String[] arguments = confirmRemoveDestination.get(name).split(" ");
		confirmRemoveDestination.remove(name);
		try {
			shipHandler.removeDestination(arguments[0], arguments[1]);
		} catch (IllegalArgumentException e) {
			sender.sendMessage(ChatColor.RED + e.getMessage());
			return false;
		}

		sender.sendMessage(ChatColor.YELLOW + "Destination removed.");
		return true;
	}
	
	/**
	 * confirmAdjust() finalises the adjusted destination.
	 * 
	 * @param sender
	 * @param args
	 * @return
	 */
	private boolean confirmAdjust(CommandSender sender, String[] args)
	{
		String name = sender.getName();
		
		if (confirmAdjust.get(name) == null)
		{
			sender.sendMessage(ChatColor.RED + "There is nothing for you to confirm.");
			return false;
		}

		//Add destination
		Player player = Bukkit.getPlayer(name);
		String[] arguments = confirmAdjust.get(name).split(" ");
		shipHandler.addDestination(player, arguments[0], arguments[1]);
		confirmAdjust.remove(name);

		sender.sendMessage(ChatColor.YELLOW + "Ship destination created.");
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
	private boolean confirmTransfer(CommandSender sender, String[] args)
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
		
		try {
			shipHandler.transfer(player, arguments[0], arguments[1]);
		} catch (IllegalArgumentException e) {
			sender.sendMessage(ChatColor.RED + e.getMessage());
			return false;
		}
		sender.sendMessage(ChatColor.YELLOW + "You transfer ownership of " + arguments[0] + " to " + arguments[1] + ".");
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
	private boolean cancelAction(CommandSender sender, String[] args)
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
		
		if (confirmAddDestination.containsKey(name))
		{
			confirmAddDestination.remove(name);
			sender.sendMessage(ChatColor.YELLOW + "Ship destination cancelled.");
			return true;
		}
		
		if (confirmAdjust.containsKey(name))
		{
			confirmAdjust.remove(name);
			shipHandler.cancelDestination(name);
			sender.sendMessage(ChatColor.YELLOW + "Ship destination cancelled.");
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
	 * confirmAddDestinationTimeout() removes the player from the list of players who are in the process 
	 * of adding a destination and removes the destination being made.
	 * 
	 * @param sender
	 */
	private void confirmAddDestinationTimeout(final CommandSender sender)
	{
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable()
		{
			public void run()
			{
				if (confirmAddDestination.containsKey(sender.getName()))
				{
					confirmAddDestination.remove(sender.getName());
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
	 * confirmAdjustTimeout() removes the player from the list of players who are in the process 
	 * of adding a destination and removes the destination being made.
	 * 
	 * @param sender
	 */
	private void confirmAdjustTimeout(final CommandSender sender)
	{
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable()
		{
			public void run()
			{
				if (confirmAdjust.containsKey(sender.getName()))
				{
					shipHandler.cancelDestination(sender.getName());
					confirmAdjust.remove(sender.getName());
					sender.sendMessage(ChatColor.RED + "You timed out.");
				}
			}			
		} , 6000);
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
		
		if (player == null)
			sender.sendMessage("--------<" + ChatColor.GOLD + " Horizon Ships Commands " + ChatColor.WHITE + ">--------");
		else
			sender.sendMessage("----------------<" + ChatColor.GOLD + " Horizon Ships Commands " + ChatColor.WHITE + ">----------------");
			
		sender.sendMessage(ChatColor.GOLD + "Horizon Ships allows you to maintain and travel in ships!");
		if (sender.hasPermission("horizonships.admin.ship.create") && player != null)
		{
			sender.sendMessage(ChatColor.YELLOW + "/ship create [shipName] [destinationName]");
			sender.sendMessage("Create a new ship at a starter destination, using your current WorldEdit selection.");
		}
		if (sender.hasPermission("horizonships.admin.ship.delete"))
		{
			sender.sendMessage(ChatColor.YELLOW + "/ship delete [shipName]");
			sender.sendMessage("Delete a ship.");
		}
		if (sender.hasPermission("horizonships.admin.destination.add") && player != null)
		{
			sender.sendMessage(ChatColor.YELLOW + "/ship add destination [shipName] [destinationName]");
			sender.sendMessage("Add a destination to the given ship, using your current WorldEdit selection.");
		}
		if (sender.hasPermission("horizonships.admin.destination.remove"))
		{
			sender.sendMessage(ChatColor.YELLOW + "/ship remove [shipName] [destinationName]");
			sender.sendMessage("Remove a destination from the given ship.");
		}
		if (sender.hasPermission("horizonships.admin.setowner"))
		{
			sender.sendMessage(ChatColor.YELLOW + "/ship setowner [shipName] [playerName]");
			sender.sendMessage("Sets the player as the owner of the ship.");
		}
		if (sender.hasPermission("horizonships.list"))
		{
			sender.sendMessage(ChatColor.YELLOW + "/ship list");
			sender.sendMessage("Provides a list of all current ships.");
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
			sender.sendMessage(ChatColor.YELLOW + "/ship diagnose");
			sender.sendMessage("Use the item in your active hand to repair the ship.");
		}
		
		sender.sendMessage(ChatColor.YELLOW + "/ship cancel");
		sender.sendMessage("Cancel any actions that are currently awaiting confirmation.");
		
		return true;
	}
}
