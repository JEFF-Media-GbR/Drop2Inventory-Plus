package de.jeff_media.Drop2InventoryPlus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import com.google.common.base.Enums;
import de.jeff_media.Drop2InventoryPlus.commands.CommandMain;
import de.jeff_media.Drop2InventoryPlus.listeners.BlockDropItemListener;
import de.jeff_media.Drop2InventoryPlus.listeners.GenericListener;
import de.jeff_media.Drop2InventoryPlus.listeners.ItemSpawnListener;
import de.jeff_media.PluginUpdateChecker.PluginUpdateChecker;
import org.bstats.bukkit.Metrics;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.java.JavaPlugin;


public class Main extends JavaPlugin {

	final int currentConfigVersion = 117;
	PluginUpdateChecker updateChecker;
	public Messages messages;
	public Utils utils;
	public MendingUtils mendingUtils;
	IngotCondenser ingotCondenser;
	ItemSpawnListener itemSpawnListener;
	HotbarStuffer hotbarStuffer;
	public HashMap<String, PlayerSetting> perPlayerSettings;
	ArrayList<Material> disabledBlocks;
	ArrayList<String> disabledWorlds;
	boolean blocksIsWhitelist = false;
	ArrayList<String> disabledMobs;
	boolean mobsIsWhitelist = false;
	public final int mcVersion = Utils.getMcVersion(Bukkit.getBukkitVersion());
	boolean usingMatchingConfig = true;
	public boolean debug = false;
	public static final String uid = "%%__USER__%%";
	public EventPriority blockDropItemPrio;

	public void reload() {
		createConfig();
		reloadConfig();
		perPlayerSettings = new HashMap<>();
		messages = new Messages(this);
		ingotCondenser = new IngotCondenser(this);

		// Update Checker start
		if(updateChecker != null) {
			updateChecker.stop();
		}
		updateChecker = new PluginUpdateChecker(this,"https://api.jeff-media.de/drop2inventoryplus/drop2inventoryplus-latest-version.txt",
				"https://www.spigotmc.org/resources/drop2inventoryplus.87784/","https://www.spigotmc.org/resources/drop2inventoryplus.87784/updates","https://paypal.me/mfnalex");
		if (getConfig().getString(Config.CHECK_FOR_UPDATES, "true").equalsIgnoreCase("true")) {
			updateChecker.check(getConfig().getInt(Config.UPDATE_CHECK_INTERVAL)*60*60);
		} else if (getConfig().getString(Config.CHECK_FOR_UPDATES, "true").equalsIgnoreCase("on-startup")) {
			updateChecker.check();
		}
		// Update Checker end
		blockDropItemPrio = Enums.getIfPresent(EventPriority.class,getConfig().getString(Config.EVENT_PRIO_BLOCKDROPITEMEVENT).toUpperCase()).or(EventPriority.HIGH);
	}

	public void onEnable() {

		if(Bukkit.getPluginManager().getPlugin("Drop2Inventory")!=null) {
			//Plugin oldPlugin = Bukkit.getPluginManager().getPlugin("Drop2Inventory");
			getLogger().severe("You still have the free version of Drop2Inventory installed.");
			getLogger().severe("Please delete the old plugin and restart your server!");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		if(mcVersion<13) {
			getLogger().severe("Drop2InventoryPlus will not run on 1.12.2 and earlier versions!");
			getLogger().severe("Please update your server to 1.13 or later.");
			return;
		}

		reload();

		itemSpawnListener = new ItemSpawnListener(this);
		CommandMain commandMain = new CommandMain(this);
		hotbarStuffer = new HotbarStuffer(this);

		this.getServer().getPluginManager().registerEvents(new GenericListener(this), this);
		this.getServer().getPluginManager().registerEvents(itemSpawnListener,this);
		this.getServer().getPluginManager().registerEvents(new BlockDropItemListener(this),this);

		utils = new Utils(this);
		mendingUtils = new MendingUtils(this);

		Metrics metrics = new Metrics(this,9970);
		
		this.getCommand("drop2inventory").setExecutor(commandMain);

		if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null){
			new Placeholders(this).register();
		}


	}
	
	@Override
	public void onDisable() {
		for (Player p : getServer().getOnlinePlayers()) {
			unregisterPlayer(p);
		}
	}

	public void createConfig() {

		migrateFromFreeVersion();

		saveDefaultConfig();

		if(getConfig().getBoolean(Config.DEBUG,false)) {
			debug=true;
		}

		if(getConfig().isSet(Config.ENABLED_BLOCKS)) {
			blocksIsWhitelist=true;
		}
		if(getConfig().isSet(Config.ENABLED_MOBS)) {
			mobsIsWhitelist=true;
		}

		disabledBlocks = new ArrayList<>();
		disabledMobs = new ArrayList<>();
		disabledWorlds = new ArrayList<>();
		ArrayList<String> disabledBlocksStrings = (ArrayList<String>) (blocksIsWhitelist ? getConfig().getStringList(Config.ENABLED_BLOCKS) : getConfig().getStringList(Config.DISABLED_BLOCKS));
		for(String s : disabledBlocksStrings) {
			Material m = Material.getMaterial(s.toUpperCase());
			if( m == null) {
				getLogger().warning("Unrecognized material "+s);
			} else {
				disabledBlocks.add(m);
				debug("Adding block to blocks " + (blocksIsWhitelist ? "whitelist" : "blacklist")+": "+m.name());
			}
		}
		for(String s : (mobsIsWhitelist ? getConfig().getStringList(Config.ENABLED_MOBS) : getConfig().getStringList(Config.DISABLED_MOBS))) {
			disabledMobs.add(s.toLowerCase());
			debug("Adding mob to mobs " + (mobsIsWhitelist ? "whitelist" : "blacklist") + ": "+s.toLowerCase());
		}
		for(String s : getConfig().getStringList(Config.DISABLED_WORLDS)) {
			disabledWorlds.add(s.toLowerCase());
			debug("Adding world to worlds blacklist: "+s.toLowerCase());
		}

		if (getConfig().getInt(Config.CONFIG_VERSION, 0) != currentConfigVersion) {
			showOldConfigWarning();

			ConfigUpdater configUpdater = new ConfigUpdater(this);
			configUpdater.updateConfig();
			configUpdater = null;
			usingMatchingConfig = true;
			reloadConfig();
		}

		File playerDataFolder = new File(getDataFolder().getPath() + File.separator + "playerdata");
		if (!playerDataFolder.getAbsoluteFile().exists()) {
			playerDataFolder.mkdir();
		}
		
		// Default settings
		getConfig().addDefault(Config.ENABLED_BY_DEFAULT, false);
		getConfig().addDefault(Config.ALWAYS_ENABLED,false);
		getConfig().addDefault(Config.CHECK_FOR_UPDATES, "true");
		getConfig().addDefault(Config.UPDATE_CHECK_INTERVAL,4);
		getConfig().addDefault(Config.SHOW_MESSAGE_WHEN_BREAKING_BLOCK, true);
		getConfig().addDefault(Config.SHOW_MESSAGE_WHEN_BREAKING_BLOCK_AND_COLLECTION_IS_ENABLED, false);
		getConfig().addDefault(Config.SHOW_MESSAGE_AGAIN_AFTER_LOGOUT, true);
		getConfig().addDefault(Config.COLLECT_BLOCK_DROPS, true);
		getConfig().addDefault(Config.COLLECT_MOB_DROPS, true);
		getConfig().addDefault(Config.COLLECT_BLOCK_EXP, true);
		getConfig().addDefault(Config.COLLECT_MOB_EXP, true);
		getConfig().addDefault(Config.AUTO_CONDENSE,false);
		getConfig().addDefault(Config.DETECT_LEGACY_DROPS,true);
		getConfig().addDefault(Config.DETECT_LEGACY_DROPS_RANGE,6.0D);
		getConfig().addDefault(Config.IGNORE_ITEMS_ON_HOPPERS,true);
		getConfig().addDefault(Config.IGNORE_ITEMS_ON_HOPPERS_VERTICAL_RANGE,20);
		getConfig().addDefault(Config.IGNORE_ITEMS_ON_HOPPERS_HORIZONTAL_RANGE, 3);
		getConfig().addDefault(Config.AVOID_HOTBAR,false);
		getConfig().addDefault(Config.WARN_WHEN_INVENTORY_IS_FULL,true);
		getConfig().addDefault(Config.EVENT_PRIO_BLOCKDROPITEMEVENT,"HIGH");
		getConfig().addDefault(Config.IGNORE_ITEMS_FROM_DISPENSERS, true);
	}

	private void migrateFromFreeVersion() {
		if(!getDataFolder().exists()) {
			File oldFolder = new File(getDataFolder().getPath()+File.separator+".."+File.separator+"Drop2Inventory");
			if(oldFolder.exists()) {
				oldFolder.renameTo(getDataFolder());
			} else {
				getDataFolder().mkdirs();
			}
		}
	}

	public boolean isWorldDisabled(String worldName) {
		return disabledWorlds.contains(worldName.toLowerCase());
	}

	private  void showOldConfigWarning() {
		getLogger().warning("=================================================");
		getLogger().warning("You were using an old config file. Drop2InventoryPlus");
		getLogger().warning("has updated the file to the newest version.");
		getLogger().warning("Your changes have been kept.");
		getLogger().warning("=================================================");
	}
	
	public PlayerSetting getPlayerSetting(Player p) {
		registerPlayer(p);
		return perPlayerSettings.get(p.getUniqueId().toString());
}
	
	public void registerPlayer(Player p) {
		if (!perPlayerSettings.containsKey(p.getUniqueId().toString())) {
			
			File playerFile = new File(getDataFolder() + File.separator + "playerdata",
					p.getUniqueId().toString() + ".yml");
			YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);

			boolean activeForThisPlayer;

			if (!playerFile.exists()) {
				activeForThisPlayer = getConfig().getBoolean(Config.ENABLED_BY_DEFAULT);
			} else {
				activeForThisPlayer = playerConfig.getBoolean("enabled");
			}

			PlayerSetting newSettings = new PlayerSetting(activeForThisPlayer);
			if (!getConfig().getBoolean(Config.SHOW_MESSAGE_AGAIN_AFTER_LOGOUT)) {
				newSettings.hasSeenMessage = playerConfig.getBoolean("hasSeenMessage");
			}
			
			
			perPlayerSettings.put(p.getUniqueId().toString(), newSettings);
		}
}
	
	public void togglePlayerSetting(Player p) {
		registerPlayer(p);
		boolean enabled = perPlayerSettings.get(p.getUniqueId().toString()).enabled;
		perPlayerSettings.get(p.getUniqueId().toString()).enabled = !enabled;
}
	
	public boolean enabled(Player p) {

		if(getConfig().getBoolean(Config.ALWAYS_ENABLED)) return true;
		
		// The following is for all the lazy server admins who use /reload instead of properly restarting their
		// server ;) I am sometimes getting stacktraces although it is clearly stated that /reload is NOT
		// supported. So, here is a quick fix
		if(perPlayerSettings == null) {
			perPlayerSettings = new HashMap<>();
		}
		registerPlayer(p);
		// End of quick fix
		
		return perPlayerSettings.get(p.getUniqueId().toString()).enabled;
}
	
	public void unregisterPlayer(Player p) {
		UUID uniqueId = p.getUniqueId();
		if (perPlayerSettings.containsKey(uniqueId.toString())) {
			PlayerSetting setting = perPlayerSettings.get(p.getUniqueId().toString());
			File playerFile = new File(getDataFolder() + File.separator + "playerdata",
					p.getUniqueId().toString() + ".yml");
			YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
			playerConfig.set("enabled", setting.enabled);
			playerConfig.set("hasSeenMessage", setting.hasSeenMessage);
			try {
				playerConfig.save(playerFile);
			} catch (IOException e) {
				e.printStackTrace();
			}

			perPlayerSettings.remove(uniqueId.toString());
		}
}

	public void debug(String t) {
		if(debug) getLogger().warning("[DEBUG] "+t);
	}
	public void debug(String t, CommandSender sender) {
		if(debug) {
			if (sender instanceof Player) {
				sender.sendMessage(ChatColor.GOLD+"[Drop2Inventory] [DEBUG] " + t);
			}
			debug(ChatColor.stripColor(t));
		}
	}
}
