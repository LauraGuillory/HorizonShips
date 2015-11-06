package com.gmail.Rhisereld.HorizonShips;

import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.Rhisereld.HorizonShips.ConfigAccessor;
import com.gmail.Rhisereld.HorizonShips.ShipsCommandExecutor;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;

public class Main extends JavaPlugin implements CommandExecutor
{
	static JavaPlugin plugin;						//Some functions require a reference to the plugin in args.
	WorldEditPlugin worldEditPlugin;
	
	static ConfigAccessor config;						//Configuration file.
	ConfigAccessor data;						//Data file.

	/**
	 * onEnable() is called when the server is started or the plugin is enabled.
	 * It should contain everything that the plugin needs for its initial setup.
	 * 
	 * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
	 */
	@Override
	public void onEnable()
	{
		plugin = this;
		
		//Setup files for configuration and data storage.
    	config = new ConfigAccessor(this, "config.yml");
    	data = new ConfigAccessor(this, "data.yml");
    	
    	//Load configuration
    	config.saveDefaultConfig();
    	
    	//WorldEdit integration for schematics.
        if (!getServer().getPluginManager().isPluginEnabled("WorldEdit")) 
        {
            getLogger().severe(String.format("[%s] - Disabled due to no WorldEdit dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        //TODO HorizonProfessions integration

        //Register commands
    	this.getCommand("ship").setExecutor(new ShipsCommandExecutor(data, plugin));
	}

	/**
     * onDisable() is called when the server shuts down or the plugin is disabled.
     * It should contain all the cleanup and data saving that the plugin needs to do before it is disabled.
     * 
     * @see org.bukkit.plugin.java.JavaPlugin#onDisable()
     */
	@Override
	public void onDisable()
	{
		plugin = null;
		worldEditPlugin = null;
		config = null;
		data = null;
		
		//TODO remove all destinations in the middle of being defined.
	}
}
