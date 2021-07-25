package de.jeff_media.drop2inventory.utils;

import de.jeff_media.drop2inventory.Main;
import de.jeff_media.drop2inventory.config.Config;
import de.jeff_media.drop2inventory.config.Permissions;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    final Main main;

    public Utils(Main main) {
        this.main = main;
    }

    public boolean isBlockEnabled(Material mat) {
        main.debug("Checking if " + mat.name() + " is enabled...");
        main.debug("Whitelist: " + main.blocksIsWhitelist);
        if (!main.blocksIsWhitelist) {
            if (main.disabledBlocks.contains(mat)) {
                main.debug("Its disabled on the blacklist!");
                return false;
            }
            main.debug("Its enabled!");
            return true;
        }
        if (main.disabledBlocks.contains(mat)) {
            main.debug("Its enabled on the whitelist!");
            return true;
        }
        main.debug("Its not on the whitelist!");
        main.debug("BTW the whitelist contains " + main.disabledBlocks.size() + " blocks");
        return false;
    }

    public boolean isMobEnabled(LivingEntity mob) {
        if (!main.isMobsIsWhitelist()) {
            return !main.disabledMobs.contains(mob.getType().name().toLowerCase());
        }
        return main.disabledMobs.contains(mob.getType().name().toLowerCase());
    }

    public static void addOrDrop(ItemStack item, Player player, @Nullable Location dropLocation) {
        //main.debug("addOrDrop: " + item.toString() + " -> " + player.getName());

        ItemStack[] items = new ItemStack[1];
        items[0] = item;
        addOrDrop(items, player, dropLocation);
    }

    public static void addOrDrop(ItemStack[] items, Player player, @Nullable Location dropLocation) {
        Main main = Main.getInstance();
        main.debug("addOrDrop[] " + Arrays.toString(items) + " -> " + player);
        if (main.getConfig().getBoolean(Config.AVOID_HOTBAR)) {
            main.debug("  avoid-hotbar enabled");
            main.hotbarStuffer.stuffHotbar(player.getInventory());
        } else {
            main.debug("  avoid-hotbar disabled");
        }
        for (ItemStack item : items) {
            main.debug(" addOrDrop#2");
            if (item == null) continue;
            if (item.getType() == Material.AIR) continue;
            // Try offHand first (md_5 doesnt want that -> https://hub.spigotmc.org/jira/browse/SPIGOT-2436)
            ItemStack offHandItem = player.getInventory().getItemInOffHand();
            if (offHandItem != null && offHandItem.getType() == item.getType()) {
                if (offHandItem.isSimilar(item)) {
                    int spaceLeftInOffHand = offHandItem.getMaxStackSize() - offHandItem.getAmount();
                    if (offHandItem.getAmount() < offHandItem.getMaxStackSize()) {
                        // Enough space left in offhand
                        if (item.getAmount() <= spaceLeftInOffHand) {
                            offHandItem.setAmount(offHandItem.getAmount() + item.getAmount());
                            continue;
                        }
                        int unstorable = item.getAmount() - spaceLeftInOffHand;
                        offHandItem.setAmount(offHandItem.getMaxStackSize());
                        item.setAmount(unstorable);
                    }
                }
            }
            // End offHand first
            HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(item);
            boolean inventoryFull = false;
            for (ItemStack leftover : leftovers.values()) {
                PDCUtils.add(leftover, Main.IGNORED_DROP_TAG, PersistentDataType.BYTE, (byte) 1);
                player.getWorld().dropItem(dropLocation == null ? player.getLocation() : dropLocation, leftover);
                main.debug("Inventory full, dropping to world");
                inventoryFull = true;
            }
            if (inventoryFull && main.getConfig().getBoolean(Config.WARN_WHEN_INVENTORY_IS_FULL)) {
                main.getMessages().sendActionBarMessage(player, main.getMessages().MSG_INVENTORY_FULL);
            }
            if (main.getConfig().getBoolean(Config.AUTO_CONDENSE)
                    && player.hasPermission(Permissions.ALLOW_AUTO_CONDENSE)) {
                main.debug("Auto condensing " + item.getType().name());
                main.ingotCondenser.condense(player.getInventory(), item.getType());
            }
        }
        if (main.getConfig().getBoolean(Config.AVOID_HOTBAR)) {
            main.hotbarStuffer.unstuffHotbar(player.getInventory());
        }
        main.getSoundUtils().playPickupSound(player);
    }

    public static boolean hasPermissionForThisTool(@Nullable Material mat, Player p) {
        String matt = mat.name().toLowerCase();
        if (matt.contains("_pickaxe")) {
            return p.hasPermission(Permissions.ALLOW_TOOL_PICKAXE);
        }
        if (matt.contains("_axe")) {
            return p.hasPermission(Permissions.ALLOW_TOOL_AXE);
        }
        if (matt.contains("_hoe")) {
            return p.hasPermission(Permissions.ALLOW_TOOL_HOE);
        }
        if (matt.contains("_sword")) {
            return p.hasPermission(Permissions.ALLOW_TOOL_SWORD);
        }
        if (matt.contains("_shovel")) {
            return p.hasPermission(Permissions.ALLOW_TOOL_SHOVEL);
        } else return p.hasPermission(Permissions.ALLOW_TOOL_HAND);
    }

    public static void renameFileInPluginDir(Main plugin, String oldName, String newName) {
        File oldFile = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + oldName);
        File newFile = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + newName);
        oldFile.getAbsoluteFile().renameTo(newFile.getAbsoluteFile());
    }

    public static @Nullable Player getNearestPlayer(Location location) {
        World world = location.getWorld();
        double bestDistance = Double.MAX_VALUE;
        Player nearestPlayer = null;

        for(Player player : world.getPlayers()) {
            double distance = player.getLocation().distanceSquared(location);
            if(distance < bestDistance) {
                bestDistance = distance;
                nearestPlayer = player;
            }
        }

        return nearestPlayer;
    }


}
