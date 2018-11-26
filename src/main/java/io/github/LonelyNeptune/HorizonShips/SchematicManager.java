package io.github.LonelyNeptune.HorizonShips;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.MCEditSchematicFormat;

class SchematicManager
{
	private final WorldEditPlugin wep;
	private final EditSession editSession;
	/**
	 * Constructor: Creates a SchematicManager object that contains an EditSession detailing all the changes that have
	 * 				been made. The object can be retained for purposes of undoing/redoing. Can only apply to one world
	 * 				(create another for a different world).
	 * 
	 * @param world - the world to which this session applies.
	 */
	SchematicManager(World world)
	{
		wep = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
		WorldEdit worldEdit = wep.getWorldEdit();
		editSession = worldEdit.getEditSessionFactory().getEditSession(new BukkitWorld(world), 10000000);
	}
	
	/**
	 * saveSchematic() copies the blocks between the two locations given (forming the two opposing apexes of a cuboid)
	 * and saves them to a schematic in \plugins\HorizonShips\schematics\.
	 * 
	 * @param loc1 - the first apex of the cuboid
	 * @param loc2 - the second apex of the cuboid
	 * @param path - the path under which the schematic will be saved.
	 * @throws DataException
	 * @throws IOException
	 */
	void saveSchematic(Location loc1, Location loc2, String path) throws DataException, IOException
	{
		File file;
		CuboidClipboard clipboard;
		
		file = new File("plugins" + File.separator + "HorizonShips"  + File.separator + "schematics"  + File.separator
				+ path + ".schematic");
		file.getParentFile().mkdirs(); //Ensure the directory exists.
		
		Vector min = getMin(loc1, loc2);
		Vector max = getMax(loc1, loc2);
		
		editSession.enableQueue();
		clipboard = new CuboidClipboard(max.subtract(min).add(new Vector(1, 1, 1)), min);
		clipboard.copy(editSession);
		MCEditSchematicFormat.MCEDIT.save(clipboard, file);
		editSession.flushQueue();		
	}
	
	/**
	 * saveSchematic() copies the blocks found inside the selection given and saves them to a schematic in
	 * \plugins\HorizonShips\schematics\.
	 * 
	 * @param selection - the selection of blocks to be saved
	 * @param path - the path under which the selection will be saved.
	 * @throws DataException
	 * @throws IOException
	 */
	void saveSchematic(Selection selection, String path) throws DataException, IOException
	{
		File file;
		CuboidClipboard clipboard;
		
		file = new File("plugins" + File.separator + "HorizonShips"  + File.separator + "schematics"  + File.separator
				+ path + ".schematic");
		file.getParentFile().mkdirs(); //Ensure the directory exists.
		
		Vector min = loc2Vector(selection.getMinimumPoint());
		Vector max = loc2Vector(selection.getMaximumPoint());
		
		editSession.enableQueue();
		clipboard = new CuboidClipboard(max.subtract(min).add(new Vector(1, 1, 1)), min);
		clipboard.copy(editSession);
		MCEditSchematicFormat.MCEDIT.save(clipboard, file);
		editSession.flushQueue();		
	}

	/**
	 * loadSchematic() loads the schematic found in \plugins\HorizonShips\schematics\ and pastes them at the
	 * origin provided.
	 *
	 * @param l - the location at which to paste the schematic into the world.
	 * @param path - the name of the schematic to be loaded.
	 * @throws MaxChangedBlocksException
	 * @throws DataException
	 * @throws IOException
	 */
	void loadSchematic(Location l, String path) throws MaxChangedBlocksException, DataException, IOException
	{
		File file;
		
		file = new File("plugins" + File.separator + "HorizonShips"  + File.separator + "schematics"  + File.separator
				+ path + ".schematic");
		Vector origin = new Vector(l.getX(), l.getY(), l.getZ());
		
		editSession.enableQueue();
		
		//Load schematic into clipboard.
		MCEditSchematicFormat.MCEDIT.load(file).paste(editSession, origin, true, true);
		editSession.flushQueue();
	}
	
	/**
	 * loadSchematic() loads the schematic found in \plugins\HorizonShips\schematics\ and pastes them inside
	 * the selection provided.
	 *
	 * @param s - the area selection to load the schematic into
	 * @param path - the path of the schematic to load
	 * @throws MaxChangedBlocksException
	 * @throws DataException
	 * @throws IOException
	 */
	void loadSchematic(Selection s, String path) throws MaxChangedBlocksException, DataException, IOException
	{
		File file;
		
		file = new File("plugins" + File.separator + "HorizonShips"  + File.separator + "schematics"  + File.separator
				+ path + ".schematic");
		Vector origin = s.getNativeMinimumPoint();
		
		editSession.enableQueue();

		//Load schematic into clipboard.
		MCEditSchematicFormat.MCEDIT.load(file).paste(editSession, origin, true, true);
		editSession.flushQueue();
	}
	
	// deleteSchematic() removes the schematic file and its parent directory.
	void deleteSchematic(String path)
	{
		File file = new File("plugins" + File.separator + "HorizonShips"  + File.separator + "schematics"
				+ File.separator + path + ".schematic");
		file.delete();
		
		//Delete directory
		file = new File("plugins" + File.separator + "HorizonShips"  + File.separator + "schematics"
				+ File.separator + "ship");
		file.delete();	
	}

	// getMin() returns a Vector containing the minimum point of the cuboid specified by two locations.
	private Vector getMin(Location l1, Location l2) 
	{
		return new Vector(Math.min(l1.getBlockX(), l2.getBlockX()),
		                  Math.min(l1.getBlockY(), l2.getBlockY()),
		                  Math.min(l1.getBlockZ(), l2.getBlockZ()));
	}

	// getMax() returns a Vector containing the maximum point of the cuboid specified by two locations.
	private Vector getMax(Location l1, Location l2) 
	{
		return new Vector(Math.max(l1.getBlockX(), l2.getBlockX()),
		                  Math.max(l1.getBlockY(), l2.getBlockY()),
		                  Math.max(l1.getBlockZ(), l2.getBlockZ()));
	}
	
	private Location getMinLocation(Location l1, Location l2)
	{
		return new Location(l1.getWorld(), Math.min(l1.getBlockX(), l2.getBlockX()),
		                  Math.min(l1.getBlockY(), l2.getBlockY()),
		                  Math.min(l1.getBlockZ(), l2.getBlockZ()));
	}
	
	private Location getMaxLocation(Location l1, Location l2)
	{
		return new Location(l1.getWorld(), Math.max(l1.getBlockX(), l2.getBlockX()),
                Math.max(l1.getBlockY(), l2.getBlockY()),
                Math.max(l1.getBlockZ(), l2.getBlockZ()));
	}
	
	/**
	 * loc2Vector() takes a location and returns a vector
	 * @param l - location
	 * @return vector
	 */
	private Vector loc2Vector(Location l)
	{
		return new Vector(l.getX(), l.getY(), l.getZ());
	}
	
	/**
	 * getPlayerSelection() returns the current selection of the player.
	 * If the player hasw no selection, throws NullPointerException.
	 * @return selection
	 */
	Selection getPlayerSelection(Player player)
	{	
		Selection s = wep.getSelection(player);
		if (s == null)
			throw new NullPointerException("No selection made.");
		return s;
	}
	
	// eraseArea() sets all of the blocks inside the region defined by two locations to air.
	void eraseArea(Location loc1, Location loc2)
	{
		Location min = getMinLocation(loc1, loc2);
		Location max = getMaxLocation(loc1, loc2);
		List<Material> droppables = new ArrayList<>();
		droppables.add(Material.TRAP_DOOR);
		droppables.add(Material.IRON_TRAPDOOR);
		droppables.add(Material.REDSTONE_TORCH_ON);
		droppables.add(Material.REDSTONE_TORCH_OFF);
		droppables.add(Material.TORCH);
		droppables.add(Material.STONE_BUTTON);
		droppables.add(Material.WOOD_BUTTON);
		droppables.add(Material.DARK_OAK_DOOR);
		droppables.add(Material.WOOD_DOOR);
		droppables.add(Material.WOODEN_DOOR);
		droppables.add(Material.ACACIA_DOOR);
		droppables.add(Material.JUNGLE_DOOR);
		droppables.add(Material.BIRCH_DOOR);
		droppables.add(Material.IRON_DOOR);
		droppables.add(Material.SPRUCE_DOOR);

		//Clean the area of items that are likely to drop items.
		for (int x = max.getBlockX(); x >= min.getBlockX(); x--)
			for (int y = max.getBlockY(); y >= min.getBlockY(); y--)
				for (int z = max.getBlockZ(); z >= min.getBlockZ(); z--)
				{
					Block block = loc1.getWorld().getBlockAt(x, y, z);
					if (block.getState() instanceof InventoryHolder)
					{
						InventoryHolder inventoryHolder = (InventoryHolder) block.getState();
						inventoryHolder.getInventory().clear();
					}
					
					if (droppables.contains(block.getType()))
						block.setType(Material.AIR);
				}
		
		//Erase what's left.
		for (int x = max.getBlockX(); x >= min.getBlockX(); x--)
			for (int y = max.getBlockY(); y >= min.getBlockY(); y--)
				for (int z = max.getBlockZ(); z >= min.getBlockZ(); z--)
					loc1.getWorld().getBlockAt(x, y, z).setType(Material.AIR);
	}
}
