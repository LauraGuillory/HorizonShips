package com.gmail.Rhisereld.HorizonShips;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.MCEditSchematicFormat;

@SuppressWarnings("deprecation")
public class SchematicManager 
{
	private final WorldEditPlugin wep;
	private final WorldEdit worldEdit;
	private final EditSession editSession;
	private CuboidClipboard clipboard;
	private File file;
	
	/**
	 * Constructor: Creates a SchematicManager object that contains an EditSession detailing all the changes that have been made.
	 * 				The object can be retained for purposes of undoing/redoing.
	 * 				Can only apply to one world (create another for a different world).
	 * 
	 * @param world - the world to which this session applies.
	 * @return SchematicManager
	 */
	public SchematicManager(World world)
	{
    	wep = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
		worldEdit = wep.getWorldEdit();
		editSession = worldEdit.getEditSessionFactory().getEditSession(new BukkitWorld(world), 10000000);
	}
	
	/**
	 * saveSchematic() copies the blocks between the two locations given (forming the two opposing apexes of a cuboid)
	 * and saves them to a schematic in \plugins\HorizonShips\schematics\.
	 * 
	 * @param loc1 - the first apex of the cuboid
	 * @param loc2 - the second apex of the cuboid
	 * @param schematicName - the name under which the schematic will be saved.
	 * @throws DataException
	 * @throws IOException
	 */
	public void saveSchematic(Location loc1, Location loc2, String schematicName) throws DataException, IOException
	{
		file = new File("plugins\\HorizonShips\\schematics\\" + schematicName + ".schematic");
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
	 * loadSchematic() loads the schematic found in \plugins\HorizonShips\schematics\ and pastes them at the
	 * origin provided.
	 * 
	 * @param schematicName - the name of the schematic to be loaded.
	 * @param l - the location at which to paste the schematic into the world.
	 * @throws MaxChangedBlocksException
	 * @throws DataException
	 * @throws IOException
	 */
	public void loadSchematic(String schematicName, Location l) throws MaxChangedBlocksException, DataException, IOException
	{
		file = new File("plugins\\HorizonShips\\schematics\\" + schematicName + ".schematic");
		Vector origin = new Vector(l.getX(), l.getY(), l.getZ());
		
		editSession.enableQueue();
		
		//Load schematic into clipboard.
		MCEditSchematicFormat.MCEDIT.load(file).paste(editSession, origin, true, true);
		editSession.flushQueue();
	}
	
	/**
	 * getMin() returns a Vector containing the minimum point of the cuboid specified by two locations.
	 * @param l1
	 * @param l2
	 * @return
	 */
	private Vector getMin(Location l1, Location l2) 
	{
		return new Vector(Math.min(l1.getBlockX(), l2.getBlockX()),
		                  Math.min(l1.getBlockY(), l2.getBlockY()),
		                  Math.min(l1.getBlockZ(), l2.getBlockZ()));
	}

	/**
	 * getMax() returns a Vector containing the maximum point of the cuboid specified by two locations.
	 * @param l1
	 * @param l2
	 * @return
	 */
	private Vector getMax(Location l1, Location l2) 
	{
		return new Vector(Math.max(l1.getBlockX(), l2.getBlockX()),
		                  Math.max(l1.getBlockY(), l2.getBlockY()),
		                  Math.max(l1.getBlockZ(), l2.getBlockZ()));
	}
}
