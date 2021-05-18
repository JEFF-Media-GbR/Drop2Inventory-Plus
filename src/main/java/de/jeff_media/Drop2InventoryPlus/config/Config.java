package de.jeff_media.Drop2InventoryPlus.config;

import de.jeff_media.Drop2InventoryPlus.Main;
import org.bukkit.configuration.file.FileConfiguration;

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
    public static final String DETECT_LEGACY_DROPS = "detect-unused-drops";
    public static final String DETECT_LEGACY_DROPS_RANGE = "detect-unused-drops-range";
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
    public static final String SOUND_GLOBAL = "sound-global";
    public static final String SOUND_ENABLED = "sound-enabled";
    public static final String SOUND_EFFECT = "sound-effect";
    public static final String SOUND_VOLUME = "sound-volume";
    public static final String SOUND_PITCH = "sound-pitch";
    public static final String CALL_ENTITY_PICKUP_ITEM_EVENT = "call-entity-pickup-item-event";

    public Config() {
        Main main = Main.getInstance();
        FileConfiguration conf = main.getConfig();
        conf.addDefault(ENABLED_BY_DEFAULT, false);
        conf.addDefault(ALWAYS_ENABLED,false);
        conf.addDefault(CHECK_FOR_UPDATES, "true");
        conf.addDefault(UPDATE_CHECK_INTERVAL,4);
        conf.addDefault(SHOW_MESSAGE_WHEN_BREAKING_BLOCK, true);
        conf.addDefault(SHOW_MESSAGE_WHEN_BREAKING_BLOCK_AND_COLLECTION_IS_ENABLED, false);
        conf.addDefault(SHOW_MESSAGE_AGAIN_AFTER_LOGOUT, true);
        conf.addDefault(COLLECT_BLOCK_DROPS, true);
        conf.addDefault(COLLECT_MOB_DROPS, true);
        conf.addDefault(COLLECT_BLOCK_EXP, true);
        conf.addDefault(COLLECT_MOB_EXP, true);
        conf.addDefault(AUTO_CONDENSE,false);
        conf.addDefault(DETECT_LEGACY_DROPS,true);
        conf.addDefault(DETECT_LEGACY_DROPS_RANGE,6.0D);
        conf.addDefault(IGNORE_ITEMS_ON_HOPPERS,true);
        conf.addDefault(IGNORE_ITEMS_ON_HOPPERS_VERTICAL_RANGE,20);
        conf.addDefault(IGNORE_ITEMS_ON_HOPPERS_HORIZONTAL_RANGE, 3);
        conf.addDefault(AVOID_HOTBAR,false);
        conf.addDefault(WARN_WHEN_INVENTORY_IS_FULL,true);
        conf.addDefault(EVENT_PRIO_BLOCKDROPITEMEVENT,"HIGH");
        conf.addDefault(IGNORE_ITEMS_FROM_DISPENSERS, true);
        conf.addDefault(IGNORE_DROPS_FROM_MOBS_KILLED_BY_LAVA, false);
        conf.addDefault(IGNORE_DROPS_FROM_MOBS_KILLED_BY_MAGMA, false);
        conf.addDefault(SOUND_GLOBAL,true);
        conf.addDefault(SOUND_ENABLED,true);
        conf.addDefault(SOUND_EFFECT,"ENTITY_ITEM_PICKUP");
        conf.addDefault(SOUND_VOLUME, 1.0);
        conf.addDefault(SOUND_PITCH, 1.0);
        conf.addDefault(CALL_ENTITY_PICKUP_ITEM_EVENT, true);
    }
}
