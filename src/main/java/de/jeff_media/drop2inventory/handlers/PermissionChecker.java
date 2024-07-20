package de.jeff_media.drop2inventory.handlers;

import de.jeff_media.drop2inventory.Main;
import de.jeff_media.drop2inventory.config.Config;
import de.jeff_media.drop2inventory.config.Messages;
import de.jeff_media.drop2inventory.config.Permissions;
import de.jeff_media.drop2inventory.data.DropSubject;
import de.jeff_media.drop2inventory.enums.DropReason;
import de.jeff_media.drop2inventory.utils.Utils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
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

    public static boolean hasDrop2InvEnabled(Player player) {

        if (!main.enabled(player)) {
            if (!main.hasSeenMessage(player)) {
                main.setHasSeenMessage(player);
                if (main.getConfig().getBoolean(Config.SHOW_MESSAGE_WHEN_BREAKING_BLOCK)) {
                    Messages.sendMessage(player, main.getMessages().MSG_HINT_ENABLE);
                }
            }
            return false;
        }
        if (!main.hasSeenMessage(player)) {
            main.setHasSeenMessage(player);
            if (main.getConfig().getBoolean(Config.SHOW_MESSAGE_WHEN_BREAKING_BLOCK_AND_COLLECTION_IS_ENABLED)) {
                Messages.sendMessage(player, main.getMessages().MSG_HINT_DISABLE);
            }
        }
        return true;
    }

    public static boolean isAllowed(Player player, DropSubject dropSubject) {
        return isAllowed(player, dropSubject, false);
    }

    public static boolean isAllowed(Player player, DropSubject dropSubject, boolean ignoreKiller) {
        World world = dropSubject.getWorld();
        DropReason dropReason = dropSubject.getDropReason();

        // Permission
        if (!player.hasPermission(Permissions.ALLOW_USE)) {
            if (main.isDebug()) main.debug("No permission for drop2inv");
            return false;
        }

        // Drop2Inventory enabled?
        if (!hasDrop2InvEnabled(player)) {
            if (main.isDebug()) main.debug("Player has drop2inv disabled");
            return false;
        }


        // Disabled worlds
        if (main.isWorldDisabled(world.getName())) {
            if (main.isDebug()) main.debug("World is disabled: " + world.getName());
            return false;
        }

        // GameMode
        if (player.getGameMode() == GameMode.CREATIVE && !main.getConfig().getBoolean(Config.WORKS_IN_CREATIVE)) {
            if (main.isDebug()) main.debug("Gamemode is creative");
            return false;
        }

        if (dropReason == DropReason.BLOCK_BREAK) {
            if (!isAllowedForBlock(player, dropSubject.getBlock())) {
                if (main.isDebug()) main.debug("block specific checks failed - aborting");
                return false;
            }
        } else if (dropReason == DropReason.ENTITY_KILL) {
            if (!isAllowedForEntity(player, dropSubject.getEntity(), ignoreKiller)) {
                if (main.isDebug()) main.debug("entity specific checks failed - aborting");
                return false;
            }
        }
        if (main.isDebug()) main.debug("drop collection is allowed");
        return true;
    }

    private static boolean isAllowedForBlock(Player player, Block block) {

        // Block drops enabled
        if (!main.getConfig().getBoolean(Config.COLLECT_BLOCK_DROPS)) {
            if (main.isDebug()) main.debug("Block drops disabled");
            return false;
        }

        // Block Blacklist
        if (!main.getUtils().isBlockEnabled(block.getType())) {
            if (main.isDebug()) main.debug("Block is not whitelisted / is blacklisted: " + block.getType());
            return false;
        }

        // Per Tool permissions
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        Material itemInMainHandType = itemInMainHand == null ? null : itemInMainHand.getType();
        if (main.getConfig().getBoolean(Config.PERMISSIONS_PER_TOOL, false)
                && !(Utils.hasPermissionForThisTool(itemInMainHandType, player))) {
            if (main.isDebug()) main.debug("No permission for this tool: " + itemInMainHandType);
            return false;
        }

        return true;
    }

    private static boolean isAllowedForEntity(Player player, Entity entity, boolean ignoreKiller) {

        /*if (!(entity instanceof LivingEntity))
            return false; // TODO: Can this ever happen? I think not. Only "living" Entities can "die"*/
        //LivingEntity victim = (LivingEntity) entity;


        if (entity.getLastDamageCause() == null || entity.getLastDamageCause().getCause() == null) {
            if (main.isDebug()) main.debug("Could not get last Damage Cause");
        } else {
            if (main.isDebug()) main.debug(entity.getLastDamageCause().getCause().name());
            if (main.getConfig().getBoolean(Config.IGNORE_DROPS_FROM_MOBS_KILLED_BY_LAVA)) {
                if (entity.getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.LAVA) {
                    if (main.isDebug()) main.debug("ignore drops from mobs killed by lava: true");
                    // TODO
                    //main.legacyDropDetectionManager.registerIgnoredLocation(event.getEntity().getLocation());
                    return false;
                }
            }

            if (main.getConfig().getBoolean(Config.IGNORE_DROPS_FROM_MOBS_KILLED_BY_LAVA)) {
                if (entity.getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.HOT_FLOOR) {
                    if (main.isDebug()) main.debug("ignore drops from mobs killed by magma: true");
                    // TODO
                    //main.legacyDropDetectionManager.registerIgnoredLocation(event.getEntity().getLocation());
                    return false;
                }
            }
        }


        if (entity instanceof LivingEntity) {
            LivingEntity victim = (LivingEntity) entity;

            if (victim.getKiller() == null && victim.getType() != EntityType.ARMOR_STAND && !ignoreKiller) {
                if (main.isDebug()) main.debug("R: Killer is null");
                return false;
            }

        }

        if (!main.getUtils().isMobEnabled(entity)) {
            if (main.isDebug()) main.debug("Mob is disabled: " + entity.getType().name());
            return false;
        }

        if (!main.getConfig().getBoolean(Config.COLLECT_MOB_DROPS) && !(entity instanceof Player)) {
            if (main.isDebug()) main.debug("Collect mob drops is disabled");
            return false;
        }

        if (!main.getConfig().getBoolean(Config.COLLECT_PLAYER_DROPS) && (entity instanceof Player)) {
            if (main.isDebug()) main.debug("Collect player drops is disabled");
            return false;
        }

        return true;
    }
}
