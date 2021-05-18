package de.jeff_media.Drop2InventoryPlus.handlers;

import de.jeff_media.Drop2InventoryPlus.Main;
import de.jeff_media.Drop2InventoryPlus.config.Config;
import de.jeff_media.Drop2InventoryPlus.config.Messages;
import de.jeff_media.Drop2InventoryPlus.config.Permissions;
import de.jeff_media.Drop2InventoryPlus.data.DropSubject;
import de.jeff_media.Drop2InventoryPlus.data.PlayerSetting;
import de.jeff_media.Drop2InventoryPlus.enums.DropReason;
import de.jeff_media.Drop2InventoryPlus.utils.Utils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Checks whether a player can use Drop2Inventory in the current situation based on the block broken or entity killed,
 * the world this happened, the permissions etc.
 */
public class PermissionChecker {

    private static final Main main = Main.getInstance();

    private static boolean hasDrop2InvEnabled(Player player) {
        PlayerSetting setting = main.perPlayerSettings.get(player.getUniqueId().toString());

        if (!main.enabled(player)) {
            if (!setting.hasSeenMessage) {
                setting.hasSeenMessage = true;
                if (main.getConfig().getBoolean(Config.SHOW_MESSAGE_WHEN_BREAKING_BLOCK)) {
                    Messages.sendMessage(player, main.messages.MSG_HINT_ENABLE);
                }
            }
            return false;
        }
        if (!setting.hasSeenMessage) {
            setting.hasSeenMessage = true;
            if (main.getConfig().getBoolean(Config.SHOW_MESSAGE_WHEN_BREAKING_BLOCK_AND_COLLECTION_IS_ENABLED)) {
                Messages.sendMessage(player, main.messages.MSG_HINT_DISABLE);
            }
        }
        return true;
    }

    public static boolean isAllowed(Player player, DropSubject dropSubject) {
        World world = dropSubject.getWorld();
        DropReason dropReason = dropSubject.getDropReason();

        main.registerPlayer(player);

        // Permission
        if (!player.hasPermission(Permissions.ALLOW_USE)) {
            main.debug("No permission for drop2inv");
            return false;
        }

        // Drop2Inventory enabled?
        if (!hasDrop2InvEnabled(player)) {
            main.debug("Player has drop2inv disabled");
            return false;
        }


        // Disabled worlds
        if (main.isWorldDisabled(world.getName())) {
            main.debug("World is disabled: " + world.getName());
            return false;
        }

        // GameMode
        if (player.getGameMode() == GameMode.CREATIVE) {
            main.debug("Gamemode is creative");
            return false;
        }

        if (dropReason == DropReason.BLOCK_BREAK) {
            if (!isAllowedForBlock(player, dropSubject.getBlock())) {
                main.debug("block specific checks failed - aborting");
                return false;
            }
        } else if (dropReason == DropReason.ENTITY_KILL) {
            if (!isAllowedForEntity(player, dropSubject.getEntity())) {
                main.debug("entity specific checks failed - aborting");
                return false;
            }
        }

        main.debug("drop collection is allowed");
        return true;
    }

    private static boolean isAllowedForBlock(Player player, Block block) {

        // Block drops enabled
        if (!main.getConfig().getBoolean(Config.COLLECT_BLOCK_DROPS)) {
            main.debug("Block drops disabled");
            return false;
        }

        // Block Blacklist
        if (!main.utils.isBlockEnabled(block.getType())) {
            main.debug("Block is not whitelisted / is blacklisted: " + block.getType());
            return false;
        }

        // Per Tool permissions
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        Material itemInMainHandType = itemInMainHand == null ? null : itemInMainHand.getType();
        if (main.getConfig().getBoolean(Config.PERMISSIONS_PER_TOOL, false)
                && !(Utils.hasPermissionForThisTool(itemInMainHandType, player))) {
            main.debug("No permission for this tool: " + itemInMainHandType);
            return false;
        }

        return true;
    }

    private static boolean isAllowedForEntity(Player player, Entity entity) {

        if (!(entity instanceof LivingEntity))
            return false; // TODO: Can this ever happen? I think not. Only "living" Entities can "die"
        LivingEntity victim = (LivingEntity) entity;

        if (victim.getLastDamageCause() == null || victim.getLastDamageCause().getCause() == null) {
            main.debug("Could not get last Damage Cause");
        } else {
            main.debug(victim.getLastDamageCause().getCause().name());
            if (main.getConfig().getBoolean(Config.IGNORE_DROPS_FROM_MOBS_KILLED_BY_LAVA)) {
                if (victim.getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.LAVA) {
                    main.debug("ignore drops from mobs killed by lava: true");
                    // TODO
                    //main.legacyDropDetectionManager.registerIgnoredLocation(event.getEntity().getLocation());
                    return false;
                }
            }

            if (main.getConfig().getBoolean(Config.IGNORE_DROPS_FROM_MOBS_KILLED_BY_LAVA)) {
                if (victim.getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.HOT_FLOOR) {
                    main.debug("ignore drops from mobs killed by magma: true");
                    // TODO
                    //main.legacyDropDetectionManager.registerIgnoredLocation(event.getEntity().getLocation());
                    return false;
                }
            }
        }


        if (victim.getKiller() == null) {
            main.debug("R: Killer is null");
            return false;
        }

        if (!main.utils.isMobEnabled(victim)) {
            main.debug("Mob is disabled: " + victim.getType().name());
            return false;
        }
        if (!main.getConfig().getBoolean(Config.COLLECT_MOB_DROPS)) {
            main.debug("Collect mob drops is disabled");
            return false;
        }

        return true;
    }
}
