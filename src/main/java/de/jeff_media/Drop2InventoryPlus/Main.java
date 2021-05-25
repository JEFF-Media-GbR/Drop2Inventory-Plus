package de.jeff_media.Drop2InventoryPlus;

import com.google.common.base.Enums;
import de.jeff_media.Drop2InventoryPlus.commands.CommandMain;
import de.jeff_media.Drop2InventoryPlus.config.Config;
import de.jeff_media.Drop2InventoryPlus.config.ConfigUpdater;
import de.jeff_media.Drop2InventoryPlus.config.Messages;
import de.jeff_media.Drop2InventoryPlus.data.PlayerSetting;
import de.jeff_media.Drop2InventoryPlus.handlers.WorldBoundingBoxGenerator;
import de.jeff_media.Drop2InventoryPlus.hooks.Placeholders;
import de.jeff_media.Drop2InventoryPlus.listeners.UniversalListener;
import de.jeff_media.Drop2InventoryPlus.utils.HotbarStuffer;
import de.jeff_media.Drop2InventoryPlus.utils.IngotCondenser;
import de.jeff_media.Drop2InventoryPlus.utils.SoundUtils;
import de.jeff_media.Drop2InventoryPlus.utils.Utils;
import de.jeff_media.PluginUpdateChecker.PluginUpdateChecker;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import javax.naming.Name;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;


public class Main extends JavaPlugin {

    public static final String uid = "%%__USER__%%";
    private static Main instance;
    public final int mcVersion = Utils.getMcVersion(Bukkit.getBukkitVersion());
    final int currentConfigVersion = 121;
    public EventPriority blockDropItemPrio;
    public boolean blocksIsWhitelist = false;
    public boolean debug = false;
    public ArrayList<Material> disabledBlocks;
    public ArrayList<String> disabledMobs;
    public ArrayList<String> disabledWorlds;
    public HotbarStuffer hotbarStuffer;
    public IngotCondenser ingotCondenser;
    //public MendingUtils mendingUtils;
    public Messages messages;
    public boolean mobsIsWhitelist = false;
    public SoundUtils soundUtils;
    public Utils utils;
    PluginUpdateChecker updateChecker;
    boolean usingMatchingConfig = true;

    public static NamespacedKey HAS_DROP_COLLECTION_ENABLED_TAG;
    public static NamespacedKey HAS_SEEN_MESSAGE_TAG;

    public static Main getInstance() {
        return instance;
    }

    public void createConfig() {

        migrateFromFreeVersion();

        saveDefaultConfig();

        if (getConfig().getBoolean(Config.DEBUG, false)) {
            debug = true;
        }

        if (getConfig().isSet(Config.ENABLED_BLOCKS)) {
            blocksIsWhitelist = true;
        }
        if (getConfig().isSet(Config.ENABLED_MOBS)) {
            mobsIsWhitelist = true;
        }

        disabledBlocks = new ArrayList<>();
        disabledMobs = new ArrayList<>();
        disabledWorlds = new ArrayList<>();
        ArrayList<String> disabledBlocksStrings = (ArrayList<String>) (blocksIsWhitelist ? getConfig().getStringList(Config.ENABLED_BLOCKS) : getConfig().getStringList(Config.DISABLED_BLOCKS));
        for (String s : disabledBlocksStrings) {
            Material m = Material.getMaterial(s.toUpperCase());
            if (m == null) {
                getLogger().warning("Unrecognized material " + s);
            } else {
                disabledBlocks.add(m);
                debug("Adding block to blocks " + (blocksIsWhitelist ? "whitelist" : "blacklist") + ": " + m.name());
            }
        }
        for (String s : (mobsIsWhitelist ? getConfig().getStringList(Config.ENABLED_MOBS) : getConfig().getStringList(Config.DISABLED_MOBS))) {
            disabledMobs.add(s.toLowerCase());
            debug("Adding mob to mobs " + (mobsIsWhitelist ? "whitelist" : "blacklist") + ": " + s.toLowerCase());
        }
        for (String s : getConfig().getStringList(Config.DISABLED_WORLDS)) {
            disabledWorlds.add(s.toLowerCase());
            debug("Adding world to worlds blacklist: " + s.toLowerCase());
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

    }

    public void debug(String t) {
        if (debug) getLogger().warning("[DEBUG] " + t);
    }

    public void debug(String t, CommandSender sender) {
        if (debug) {
            if (sender instanceof Player) {
                Messages.sendMessage(sender, ChatColor.GOLD + "[Drop2Inventory] [DEBUG] " + t);
            }
            debug(ChatColor.stripColor(t));
        }
    }

    public boolean enabled(Player p) {

        if (getConfig().getBoolean(Config.ALWAYS_ENABLED)) return true;

        PersistentDataContainer pdc = p.getPersistentDataContainer();
        byte value = pdc.getOrDefault(HAS_DROP_COLLECTION_ENABLED_TAG, PersistentDataType.BYTE, (byte) 0);
        return value == (byte) 1 ? true : false;
    }

    public boolean hasSeenMessage(Player p) {
        PersistentDataContainer pdc = p.getPersistentDataContainer();
        byte value = pdc.getOrDefault(HAS_SEEN_MESSAGE_TAG, PersistentDataType.BYTE, (byte) 0);
        return value == (byte) 1 ? true : false;
    }


    public boolean isWorldDisabled(String worldName) {
        return disabledWorlds.contains(worldName.toLowerCase());
    }

    private void migrateFromFreeVersion() {
        if (!getDataFolder().exists()) {
            File oldFolder = new File(getDataFolder().getPath() + File.separator + ".." + File.separator + "Drop2Inventory");
            if (oldFolder.exists()) {
                oldFolder.renameTo(getDataFolder());
            } else {
                getDataFolder().mkdirs();
            }
        }
    }

    public void onEnable() {

        instance = this;
        HAS_DROP_COLLECTION_ENABLED_TAG = new NamespacedKey(this, "dropcollectionenabled");
        HAS_SEEN_MESSAGE_TAG = new NamespacedKey(this, "hasseenmessage");

        WorldBoundingBoxGenerator.init();

        if (Bukkit.getPluginManager().getPlugin("Drop2Inventory") != null) {
            //Plugin oldPlugin = Bukkit.getPluginManager().getPlugin("Drop2Inventory");
            getLogger().severe("You still have the free version of Drop2Inventory installed.");
            getLogger().severe("Please delete the old plugin and restart your server!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        if (mcVersion < 13) {
            getLogger().severe("Drop2InventoryPlus will not run on 1.12.2 and earlier versions!");
            getLogger().severe("Please update your server to 1.13 or later.");
            return;
        }

        reload();

        CommandMain commandMain = new CommandMain(this);
        hotbarStuffer = new HotbarStuffer(this);

        //this.getServer().getPluginManager().registerEvents(new GenericListener(this), this);
        //this.getServer().getPluginManager().registerEvents(legacyDropDetectionListener,this);
        //this.getServer().getPluginManager().registerEvents(new BlockDropItemListener(this),this);
        getServer().getPluginManager().registerEvents(new UniversalListener(), this);

        utils = new Utils(this);
        //mendingUtils = new MendingUtils(this);

        Metrics metrics = new Metrics(this, 9970);

        this.getCommand("drop2inventory").setExecutor(commandMain);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new Placeholders(this).register();
        }


    }

    public void reload() {
        createConfig();
        reloadConfig();
        soundUtils = new SoundUtils();
        messages = new Messages(this);
        ingotCondenser = new IngotCondenser(this);

        // Update Checker start
        // TODO: Switch to my new Update Checker -> https://github.com/JEFF-Media-GbR/Spigot-UpdateChecker
        if (updateChecker != null) {
            updateChecker.stop();
        }
        updateChecker = new PluginUpdateChecker(this, "https://api.jeff-media.de/drop2inventoryplus/drop2inventoryplus-latest-version.txt",
                "https://www.spigotmc.org/resources/drop2inventoryplus.87784/", "https://www.spigotmc.org/resources/drop2inventoryplus.87784/updates", "https://paypal.me/mfnalex");
        if (getConfig().getString(Config.CHECK_FOR_UPDATES, "true").equalsIgnoreCase("true")) {
            updateChecker.check((long) getConfig().getInt(Config.UPDATE_CHECK_INTERVAL) * 60 * 60);
        } else if (getConfig().getString(Config.CHECK_FOR_UPDATES, "true").equalsIgnoreCase("on-startup")) {
            updateChecker.check();
        }
        // Update Checker end
        blockDropItemPrio = Enums.getIfPresent(EventPriority.class, getConfig().getString(Config.EVENT_PRIO_BLOCKDROPITEMEVENT).toUpperCase()).or(EventPriority.HIGH);
    }

    private void showOldConfigWarning() {
        getLogger().warning("=================================================");
        getLogger().warning("You were using an old config file. Drop2InventoryPlus");
        getLogger().warning("has updated the file to the newest version.");
        getLogger().warning("Your changes have been kept.");
        getLogger().warning("=================================================");
    }

    public void togglePlayerSetting(Player p) {
        boolean enabled = !enabled(p);
        PersistentDataContainer pdc = p.getPersistentDataContainer();
        if(enabled) {
            pdc.set(HAS_DROP_COLLECTION_ENABLED_TAG, PersistentDataType.BYTE, (byte) 1);
        } else {
            pdc.remove(HAS_DROP_COLLECTION_ENABLED_TAG);
        }
    }

    public void setHasSeenMessage(Player p) {
        PersistentDataContainer pdc = p.getPersistentDataContainer();
        pdc.set(HAS_SEEN_MESSAGE_TAG, PersistentDataType.BYTE, (byte) 1);
    }


}
