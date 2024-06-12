package de.jeff_media.drop2inventory;

import com.allatori.annotations.DoNotRename;
import com.jeff_media.cesspool.config.CommandList;
import de.jeff_media.daddy.Daddy_Stepsister;
import de.jeff_media.drop2inventory.commands.CommandMain;
import de.jeff_media.drop2inventory.commands.CommandMainTabCompleter;
import de.jeff_media.drop2inventory.config.Config;
import de.jeff_media.drop2inventory.config.ConfigUpdater;
import de.jeff_media.drop2inventory.config.Messages;
import de.jeff_media.drop2inventory.handlers.HopperDetector;
import de.jeff_media.drop2inventory.handlers.WorldBoundingBoxGenerator;
import de.jeff_media.drop2inventory.hooks.Placeholders;
import de.jeff_media.drop2inventory.hooks.PluginHooks;
import de.jeff_media.drop2inventory.listeners.CollectListener;
import de.jeff_media.drop2inventory.listeners.MiscListener;
import de.jeff_media.drop2inventory.listeners.RegistrationListener;
import de.jeff_media.drop2inventory.utils.AutoSmelter;
import de.jeff_media.drop2inventory.utils.HotbarStuffer;
import de.jeff_media.drop2inventory.utils.IngotCondenser;
import de.jeff_media.drop2inventory.utils.SoundUtils;
import de.jeff_media.drop2inventory.utils.Utils;
import de.jeff_media.morepersistentdatatypes.DataType;
import de.jeff_media.updatechecker.UpdateChecker;
import de.jeff_media.updatechecker.UserAgentBuilder;
import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import javax.xml.stream.events.Namespace;
import java.io.*;
import java.util.ArrayList;
import java.util.function.BiConsumer;

@DoNotRename
public class Main extends JavaPlugin {

    public static final String uid = "%%__USER__%%";
    private static Main instance;
    public EventPriority blockDropItemPrio;
    @DoNotRename
    public boolean blocksIsWhitelist = false;
    @Getter private boolean debug = false;
    @DoNotRename
    public ArrayList<Material> disabledBlocks;
    @DoNotRename
    public ArrayList<String> disabledMobs;
    @DoNotRename
    public ArrayList<String> disabledWorlds;
    public HotbarStuffer hotbarStuffer;
    public IngotCondenser ingotCondenser;
    public AutoSmelter autoSmelter;
    @Getter private Messages messages;
    @DoNotRename
    @Getter private boolean mobsIsWhitelist = false;
    @Getter private SoundUtils soundUtils;
    @Getter private Utils utils;
    private UpdateChecker updateChecker;
    boolean usingMatchingConfig = true;
    @Getter private HopperDetector hopperDetector;
    @Getter private PluginHooks pluginHooks;
    @Getter private CommandList invFullCommands = CommandList.EMPTY;

    public static NamespacedKey HAS_DROP_COLLECTION_ENABLED_TAG;
    public static NamespacedKey HAS_SEEN_MESSAGE_TAG;
    public static NamespacedKey IGNORED_DROP_TAG;
    private boolean showedWeirdPluginWarning = false;

    @DoNotRename
    public static Main getInstance() {
        return instance;
    }

    public void applyEnabledByDefault(Player player) {
        if(!getConfig().getBoolean(Config.ENABLED_BY_DEFAULT)) return;
        PersistentDataContainer pdc = player.getPersistentDataContainer();

        NamespacedKey applied = new NamespacedKey(this,"enabledbydefault_applied");
        if(pdc.has(applied, DataType.BOOLEAN)) {
            return;
        }

        pdc.set(applied, DataType.BOOLEAN, true);
        pdc.set(HAS_DROP_COLLECTION_ENABLED_TAG, PersistentDataType.BYTE, (byte) 1);
    }

    public void applyAutoCondenseEnabledByDefault(Player player) {
        if(!getConfig().getBoolean(Config.AUTO_CONDENSE_ENABLED_BY_DEFAULT)) return;
        PersistentDataContainer pdc = player.getPersistentDataContainer();

        NamespacedKey applied = new NamespacedKey(this,"autocondense_enabledbydefault_applied");
        if(pdc.has(applied, DataType.BOOLEAN)) {
            return;
        }

        pdc.set(applied, DataType.BOOLEAN, true);
        pdc.set(ingotCondenser.getAutoCondenseKey(), DataType.BOOLEAN, true);
    }

    public void applyAutoSmeltEnabledByDefault(Player player) {
        if(!getConfig().getBoolean(Config.AUTO_SMELT_ENABLED_BY_DEFAULT)) return;
        PersistentDataContainer pdc = player.getPersistentDataContainer();

        NamespacedKey applied = new NamespacedKey(this,"autosmelt_enabledbydefault_applied");
        if(pdc.has(applied, DataType.BOOLEAN)) {
            return;
        }

        pdc.set(applied, DataType.BOOLEAN, true);
        pdc.set(autoSmelter.getAutoSmeltKey(), DataType.BOOLEAN, true);
    }

    public void createConfig() {

        migrateFromFreeVersion();

        saveDefaultConfig();

        if (getConfig().getBoolean(Config.DEBUG, false)) {
            setDebug(true);
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

        if (getConfig().getLong(Config.CONFIG_VERSION, 0) < getNewConfigVersion()) {
            debug("Your config version: " + getConfig().getLong(Config.CONFIG_VERSION));
            debug("Newest config version: " + getNewConfigVersion());
            showOldConfigWarning();

            ConfigUpdater configUpdater = new ConfigUpdater(this);
            configUpdater.updateConfig();
            usingMatchingConfig = true;
            reloadConfig();
        }

    }

    public void debug(String t) {
        if (isDebug()) getLogger().warning("[DEBUG] " + t);
    }

    public void debug(String t, CommandSender sender) {
        if (isDebug()) {
            if (sender instanceof Player) {
                Messages.sendMessage(sender, ChatColor.GOLD + "[Drop2Inventory] [DEBUG] " + t);
            }
            debug(ChatColor.stripColor(t));
        }
    }

    @Nullable
    public PersistentDataContainer getPdcAndCheckForStupidPlugins(Player p) {
        PersistentDataContainer pdc = p.getPersistentDataContainer();
        if(pdc == null) { // Do not remove. Stupid plugins like "WarpSystem" implement their own shitty Player class, ignoring @NotNull annotated stuff.
            Plugin weirdPlugin = getProvidingPlugin(p.getClass());
            if(weirdPlugin != null && !showedWeirdPluginWarning) {
                showedWeirdPluginWarning = true;
                String weirdPluginName = weirdPlugin.getName() + " " + weirdPlugin.getDescription().getVersion();
                String weirdClass = p.getClass().getName();
                getLogger().warning("");
                getLogger().warning("Oh no - you have some weird plugin that implements the Player interface using a custom class,");
                getLogger().warning("which is not allowed according to the Spigot API documentation. Furthermore, this custom");
                getLogger().warning("implementation is corrupted, as it returns \"null\" for a method annotated with \"@NotNull\".");
                getLogger().severe("The problem was caused by the following plugin: " + weirdPluginName + " (Class: " + weirdClass + ")");
                Bukkit.getScheduler().runTaskLater(this, () -> showedWeirdPluginWarning = false, 19);
            }
            return null;
        }
        return pdc;
    }

    @DoNotRename
    public boolean enabled(Player p) {

        if (getConfig().getBoolean(Config.ALWAYS_ENABLED)) return true;

        PersistentDataContainer pdc = getPdcAndCheckForStupidPlugins(p);
        if(pdc == null) return false;

        byte value = pdc.getOrDefault(HAS_DROP_COLLECTION_ENABLED_TAG, PersistentDataType.BYTE, (byte) 0);
        return value == (byte) 1 ? true : false;
    }

    @DoNotRename
    public boolean hasSeenMessage(Player p) {
        PersistentDataContainer pdc = getPdcAndCheckForStupidPlugins(p);
        if(pdc == null) return false;
        byte value = pdc.getOrDefault(HAS_SEEN_MESSAGE_TAG, PersistentDataType.BYTE, (byte) 0);
        return value == (byte) 1;
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

    private static long getNewConfigVersion() {
        final InputStream in = Main.getInstance().getClass().getResourceAsStream("/config-version.txt");
        final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        try {
            return Long.parseLong(reader.readLine());
        } catch (final IOException ioException) {
            ioException.printStackTrace();
            return 0;
        }
    }

    public void onLoad() {
        instance = this;
    }

    public void onEnable() {

        Daddy_Stepsister.init(this);
        if(Daddy_Stepsister.allows(null)) {
            Daddy_Stepsister.createVerificationFile();
        }

        HAS_DROP_COLLECTION_ENABLED_TAG = new NamespacedKey(this, "dropcollectionenabled");
        HAS_SEEN_MESSAGE_TAG = new NamespacedKey(this, "hasseenmessage");
        IGNORED_DROP_TAG = new NamespacedKey(this, "ignoreddrop");

        WorldBoundingBoxGenerator.init();

        if (Bukkit.getPluginManager().getPlugin("Drop2Inventory") != null) {
            //Plugin oldPlugin = Bukkit.getPluginManager().getPlugin("Drop2Inventory");
            getLogger().severe("You still have the free version of Drop2Inventory installed.");
            getLogger().severe("Please delete the old plugin and restart your server!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        try {
            Class.forName("org.bukkit.persistence.PersistentDataContainer");
        } catch (Exception e) {
            getLogger().severe("drop2inventory will not run on 1.13 and earlier versions!");
            getLogger().severe("Please update your server to 1.14.1 or later.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        reload();

        CommandMain commandMain = new CommandMain(this);
        hotbarStuffer = new HotbarStuffer(this);

        getServer().getPluginManager().registerEvents(new CollectListener(), this);
        getServer().getPluginManager().registerEvents(new RegistrationListener(), this);
        getServer().getPluginManager().registerEvents(new MiscListener(), this);

        utils = new Utils(this);
        pluginHooks = new PluginHooks();
        //mendingUtils = new MendingUtils(this);

        Metrics metrics = new Metrics(this, 9970);

        this.getCommand("drop2inventory").setExecutor(commandMain);
        this.getCommand("drop2inventory").setTabCompleter(new CommandMainTabCompleter());

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new Placeholders(this).register();
        }


    }

    public void reload() {
        reloadConfig();
        createConfig();
        soundUtils = new SoundUtils();
        messages = new Messages(this);
        ingotCondenser = new IngotCondenser(this);
        autoSmelter = new AutoSmelter(this);
        hopperDetector = new HopperDetector();

        // Update Checker start
        // TODO: Switch to my new Update Checker -> https://github.com/JEFF-Media-GbR/Spigot-UpdateChecker
        if (updateChecker != null) {
            updateChecker.stop();
        }
        updateChecker = UpdateChecker.init(this, "https://api.jeff-media.de/drop2inventoryplus/drop2inventoryplus-latest-version.txt")
                .setDownloadLink(87784)
                .setChangelogLink(87784)
                .setDonationLink("https://paypal.me/mfnalex")
                .setUserAgent(UserAgentBuilder.getDefaultUserAgent().addSpigotUserId())
                .onFail((commandSenders, exception) -> { });
        if (getConfig().getString(Config.CHECK_FOR_UPDATES, "true").equalsIgnoreCase("true")) {
            updateChecker.checkEveryXHours(getConfig().getDouble(Config.UPDATE_CHECK_INTERVAL)).checkNow();
        } else if (getConfig().getString(Config.CHECK_FOR_UPDATES, "true").equalsIgnoreCase("on-startup")) {
            updateChecker.checkNow();
        }
        Utils.loadSounds();

        loadRunCommands();

        // Update Checker end
        //blockDropItemPrio = Enums.getIfPresent(EventPriority.class, getConfig().getString(Config.EVENT_PRIO_BLOCKDROPITEMEVENT).toUpperCase()).or(EventPriority.HIGH);
    }

    private void loadRunCommands() {
        com.jeff_media.jefflib.data.Config commandConfig = new com.jeff_media.jefflib.data.Config("run-commands.yml");
        invFullCommands = getCommandList(commandConfig, "inventory-full");
    }

    private CommandList getCommandList(ConfigurationSection config, String name) {
        if(config.isConfigurationSection(name)) {
            boolean enabled = config.getBoolean(name + ".enabled", false);
            if(enabled && config.isSet(name + ".commands")) {
                return CommandList.of(config, name + ".commands");
            }
        }
        return CommandList.EMPTY;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    private void showOldConfigWarning() {
        getLogger().warning("=================================================");
        getLogger().warning("You were using an old config file. Drop2Inventory");
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
        PersistentDataContainer pdc = getPdcAndCheckForStupidPlugins(p);
        if(pdc == null) return;
        pdc.set(HAS_SEEN_MESSAGE_TAG, PersistentDataType.BYTE, (byte) 1);
    }


}
