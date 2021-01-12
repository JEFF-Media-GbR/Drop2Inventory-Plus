package de.jeff_media.Drop2InventoryPlus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import de.jeff_media.Drop2InventoryPlus.commands.CommandMain;
import de.jeff_media.PluginProtect.PluginProtect;
import de.jeff_media.PluginUpdateChecker.PluginUpdateChecker;
import org.bstats.bukkit.Metrics;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;


public class Main extends JavaPlugin {

	int currentConfigVersion = 115;

	PluginUpdateChecker updateChecker;
	public Messages messages;
	public Utils utils;
	MendingUtils mendingUtils;
	IngotCondenser ingotCondenser;
	ItemSpawnListener itemSpawnListener;
	HotbarStuffer hotbarStuffer;

	HashMap<String, PlayerSetting> perPlayerSettings;

	ArrayList<Material> disabledBlocks;
	ArrayList<String> disabledWorlds;

	boolean blocksIsWhitelist = false;

	ArrayList<String> disabledMobs;

	boolean mobsIsWhitelist = false;
	boolean autoCondense = false;



	final int mcVersion = Utils.getMcVersion(Bukkit.getBukkitVersion());
	boolean usingMatchingConfig = true;
	boolean enabledByDefault = false;
	boolean showMessageWhenBreakingBlock = true;
	boolean showMessageWhenBreakingBlockAndCollectionIsDisabled = false;
	boolean showMessageAgainAfterLogout=true;

	private static final int updateCheckInterval = 86400;

	public boolean debug = false;

	public static String uid = "%%__USER__%%";

	public boolean reload() {
		createConfig();
		reloadConfig();
		perPlayerSettings = new HashMap<String, PlayerSetting>();
		messages = new Messages(this);
		ingotCondenser = new IngotCondenser(this);

		enabledByDefault = getConfig().getBoolean("enabled-by-default");
		showMessageWhenBreakingBlock = getConfig().getBoolean("show-message-when-breaking-block");
		showMessageWhenBreakingBlockAndCollectionIsDisabled = getConfig().getBoolean("show-message-when-breaking-block-and-collection-is-disabled");
		showMessageAgainAfterLogout = getConfig().getBoolean("show-message-again-after-logout");
		autoCondense = getConfig().getBoolean("auto-condense");

		// Update Checker start
		if(updateChecker != null) {
			updateChecker.stop();
		}
		updateChecker = new PluginUpdateChecker(this,"https://api.jeff-media.de/drop2inventoryplus/drop2inventoryplus-latest-version.txt",
				null,null,"https://paypal.me/mfnalex");
		if (getConfig().getString("check-for-updates", "true").equalsIgnoreCase("true")) {
			updateChecker.check((long) updateCheckInterval);
		} else if (getConfig().getString("check-for-updates", "true").equalsIgnoreCase("on-startup")) {
			updateChecker.check();
		}
		// Update Checker end
		return false;
	}

	public void onEnable() {

		PluginProtect pp = new PluginProtect(this,"https://api.jeff-media.de/vfy.php",uid);
		pp.check();

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
		


		this.getServer().getPluginManager().registerEvents(new Listener(this), this);
		this.getServer().getPluginManager().registerEvents(itemSpawnListener,this);
		this.getServer().getPluginManager().registerEvents(new DropListener(this),this);

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

		if(getConfig().getBoolean("debug",false)) {
			debug=true;
		}

		if(getConfig().isSet("enabled-blocks")) {
			blocksIsWhitelist=true;
		}
		if(getConfig().isSet("enabled-mobs")) {
			mobsIsWhitelist=true;
		}

		disabledBlocks = new ArrayList<>();
		disabledMobs = new ArrayList<>();
		disabledWorlds = new ArrayList<>();
		ArrayList<String> disabledBlocksStrings = (ArrayList<String>) (blocksIsWhitelist ? getConfig().getStringList("enabled-blocks") : getConfig().getStringList("disabled-blocks"));
		for(String s : disabledBlocksStrings) {
			Material m = Material.getMaterial(s.toUpperCase());
			if( m == null) {
				getLogger().warning("Unrecognized material "+s);
			} else {
				disabledBlocks.add(m);
				debug("Adding block to blocks " + (blocksIsWhitelist ? "whitelist" : "blacklist")+": "+m.name());
			}
		}
		for(String s : (mobsIsWhitelist ? getConfig().getStringList("enabled-mobs") : getConfig().getStringList("disabled-mobs"))) {
			disabledMobs.add(s.toLowerCase());
			debug("Adding mob to mobs " + (mobsIsWhitelist ? "whitelist" : "blacklist") + ": "+s.toLowerCase());
		}
		for(String s : getConfig().getStringList("disabled-worlds")) {
			disabledWorlds.add(s.toLowerCase());
			debug("Adding world to worlds blacklist: "+s.toLowerCase());
		}

		if (getConfig().getInt("config-version", 0) != currentConfigVersion) {
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
		getConfig().addDefault("enabled-by-default", false);
		getConfig().addDefault("always-enabled",false);
		getConfig().addDefault("check-for-updates", "true");
		getConfig().addDefault("show-message-when-breaking-block", true);
		getConfig().addDefault("show-message-when-breaking-block-and-collection-is-enabled", false);
		getConfig().addDefault("show-message-again-after-logout", true);
		getConfig().addDefault("collect-block-drops", true);
		getConfig().addDefault("collect-mob-drops", true);
		getConfig().addDefault("collect-block-exp", true);
		getConfig().addDefault("collect-mob-exp", true);
		getConfig().addDefault("auto-condense",false);
		getConfig().addDefault("detect-legacy-drops",true);
		getConfig().addDefault("avoid-hotbar",false);
		getConfig().addDefault("ignore-items-on-hoppers",true);
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

	protected boolean isWorldDisabled(String worldName) {
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
				activeForThisPlayer = enabledByDefault;
			} else {
				activeForThisPlayer = playerConfig.getBoolean("enabled");
			}

			PlayerSetting newSettings = new PlayerSetting(activeForThisPlayer);
			if (!getConfig().getBoolean("show-message-again-after-logout")) {
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
	
	boolean enabled(Player p) {

		if(getConfig().getBoolean("always-enabled")) return true;
		
		// The following is for all the lazy server admins who use /reload instead of properly restarting their
		// server ;) I am sometimes getting stacktraces although it is clearly stated that /reload is NOT
		// supported. So, here is a quick fix
		if(perPlayerSettings == null) {
			perPlayerSettings = new HashMap<String, PlayerSetting>();
		}
		registerPlayer(p);
		// End of quick fix
		
		return perPlayerSettings.get(p.getUniqueId().toString()).enabled;
}
	
	void unregisterPlayer(Player p) {
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
