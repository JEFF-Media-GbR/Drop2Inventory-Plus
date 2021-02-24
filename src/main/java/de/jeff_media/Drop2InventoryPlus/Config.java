package de.jeff_media.Drop2InventoryPlus;

public class Config {
    public static final String ALWAYS_ENABLED = "always-enabled";
    public static final String ENABLED_BY_DEFAULT = "enabled-by-default";
    public static final String COLLECT_BLOCK_DROPS = "collect-block-drops";
    public static final String COLLECT_BLOCK_EXP = "collect-block-exp";
    public static final String COLLECT_MOB_DROPS = "collect-mob-drops";
    public static final String COLLECT_MOB_EXP = "collect-mob-exp";
    public static final String AUTO_CONDENSE = "auto-condense";
    public static final String SHOW_MESSAGE_WHEN_BREAKING_BLOCK = "show-message-when-breaking-block";
    public static final String SHOW_MESSAGE_WHEN_BREAKING_BLOCK_AND_COLLECTION_IS_ENABLED = "show-message-when-breaking-block-and-collection-is-enabled";
    public static final String SHOW_MESSAGE_AGAIN_AFTER_LOGOUT = "show-message-again-after-logout";
    public static final String PERMISSIONS_PER_TOOL = "permissions-per-tool";
    public static final String DETECT_LEGACY_DROPS = "detect-legacy-drops";
    public static final String DETECT_LEGACY_DROPS_RANGE = "detect-legacy-drops-range";
    public static final String IGNORE_ITEMS_ON_HOPPERS = "ignore-items-on-hoppers";
    public static final String IGNORE_ITEMS_ON_HOPPERS_VERTICAL_RANGE = "ignore-items-on-hoppers-vertical-range";
    public static final String IGNORE_ITEMS_ON_HOPPERS_HORIZONTAL_RANGE = "ignore-items-on-hoppers-horizontal-range";
    public static final String AVOID_HOTBAR = "avoid-hotbar";
    public static final String CHECK_FOR_UPDATES = "check-for-updates";
    public static final String UPDATE_CHECK_INTERVAL = "check-interval";
    public static final String DISABLED_WORLDS = "disabled-worlds";
    public static final String DISABLED_BLOCKS = "disabled-blocks";
    public static final String ENABLED_BLOCKS = "enabled-blocks";
    public static final String DISABLED_MOBS = "disabled-mobs";
    public static final String ENABLED_MOBS = "enabled-mobs";
    public static final String IGNORE_DROPS_FROM_MOBS_KILLED_BY_LAVA = "ignore-drops-from-mobs-killed-by-lava";
    public static final String IGNORE_DROPS_FROM_MOBS_KILLED_BY_MAGMA = "ignore-drops-from-mobs-killed-by-magma";
    public static final String DEBUG = "debug";
    public static final String CONFIG_VERSION = "config-version";
    public static final String WARN_WHEN_INVENTORY_IS_FULL = "warn-when-inventory-is-full";
    public static final String EVENT_PRIO_BLOCKDROPITEMEVENT = "block-drop-item-event-priority";
    public static final String IGNORE_ITEMS_FROM_DISPENSERS = "ignore-items-from-dispensers";
    
    public Config() {
        Main main = Main.getInstance();
        main.getConfig().addDefault(Config.ENABLED_BY_DEFAULT, false);
        main.getConfig().addDefault(Config.ALWAYS_ENABLED,false);
        main.getConfig().addDefault(Config.CHECK_FOR_UPDATES, "true");
        main.getConfig().addDefault(Config.UPDATE_CHECK_INTERVAL,4);
        main.getConfig().addDefault(Config.SHOW_MESSAGE_WHEN_BREAKING_BLOCK, true);
        main.getConfig().addDefault(Config.SHOW_MESSAGE_WHEN_BREAKING_BLOCK_AND_COLLECTION_IS_ENABLED, false);
        main.getConfig().addDefault(Config.SHOW_MESSAGE_AGAIN_AFTER_LOGOUT, true);
        main.getConfig().addDefault(Config.COLLECT_BLOCK_DROPS, true);
        main.getConfig().addDefault(Config.COLLECT_MOB_DROPS, true);
        main.getConfig().addDefault(Config.COLLECT_BLOCK_EXP, true);
        main.getConfig().addDefault(Config.COLLECT_MOB_EXP, true);
        main.getConfig().addDefault(Config.AUTO_CONDENSE,false);
        main.getConfig().addDefault(Config.DETECT_LEGACY_DROPS,true);
        main.getConfig().addDefault(Config.DETECT_LEGACY_DROPS_RANGE,6.0D);
        main.getConfig().addDefault(Config.IGNORE_ITEMS_ON_HOPPERS,true);
        main.getConfig().addDefault(Config.IGNORE_ITEMS_ON_HOPPERS_VERTICAL_RANGE,20);
        main.getConfig().addDefault(Config.IGNORE_ITEMS_ON_HOPPERS_HORIZONTAL_RANGE, 3);
        main.getConfig().addDefault(Config.AVOID_HOTBAR,false);
        main.getConfig().addDefault(Config.WARN_WHEN_INVENTORY_IS_FULL,true);
        main.getConfig().addDefault(Config.EVENT_PRIO_BLOCKDROPITEMEVENT,"HIGH");
        main.getConfig().addDefault(Config.IGNORE_ITEMS_FROM_DISPENSERS, true);
        main.getConfig().addDefault(Config.IGNORE_DROPS_FROM_MOBS_KILLED_BY_LAVA, false);
        main.getConfig().addDefault(Config.IGNORE_DROPS_FROM_MOBS_KILLED_BY_MAGMA, false);
    }
}
