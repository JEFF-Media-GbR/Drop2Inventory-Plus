package de.jeff_media.Drop2InventoryPlus;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.File;

public class Utils {

    Main main;

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

    boolean isMobEnabled(LivingEntity mob) {
        if (!main.mobsIsWhitelist) {
            if (main.disabledMobs.contains(mob.getType().name().toLowerCase()))
                return false;
            return true;
        }
        if (main.disabledMobs.contains(mob.getType().name().toLowerCase()))
            return true;
        return false;
    }

    void addOrDrop(ItemStack item, Player player) {
        main.debug("addOrDrop: " + item.toString() + " -> " + player.getName());

        ItemStack[] items = new ItemStack[1];
        items[0] = item;
        addOrDrop(items, player);
		/*if(plugin.autoCondense) {
			plugin.debug("Auto condensing "+item.getType().name());
			plugin.ingotCondenser.condense(player.getInventory(), item.getType());
		}*/

    }

    static boolean hasPermissionForThisTool(Material mat, Player p) {
        String matt = mat.name().toLowerCase();
        if (matt.contains("_pickaxe")) {
            return p.hasPermission("drop2inventory.tool.pickaxe");
        }
        if (matt.contains("_axe")) {
            return p.hasPermission("drop2inventory.tool.axe");
        }
        if (matt.contains("_hoe")) {
            return p.hasPermission("drop2inventory.tool.hoe");
        }
        if (matt.contains("_sword")) {
            return p.hasPermission("drop2inventory.tool.sword");
        }
        if (matt.contains("_shovel")) {
            return p.hasPermission("drop2inventory.tool.shovel");
        } else return p.hasPermission("drop2inventory.tool.hand");
    }

    void addOrDrop(ItemStack[] items, Player player) {
        if (main.getConfig().getBoolean("avoid-hotbar")) {
            main.debug("  avoid-hotbar enabled");
            main.hotbarStuffer.stuffHotbar(player.getInventory());
        } else {
            main.debug("  avoid-hotbar disabled");
        }
        for (ItemStack item : items) {
            main.debug(" addOrDrop#2");
            if (item == null) continue;
            if (item.getType() == Material.AIR) continue;
            HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(item);
            for (ItemStack leftover : leftovers.values()) {
                Item drop = player.getWorld().dropItemNaturally(player.getLocation(), leftover);
                main.itemSpawnListener.drops.add(drop.getUniqueId());
            }
            if (main.autoCondense) {
                main.debug("Auto condensing " + item.getType().name());
                main.ingotCondenser.condense(player.getInventory(), item.getType());
            }
        }
        if (main.getConfig().getBoolean("avoid-hotbar")) {
            main.hotbarStuffer.unstuffHotbar(player.getInventory());
        }
    }

    ItemStack getItemInMainHand(Player p) {
        if (main.mcVersion < 9) {
            return p.getInventory().getItemInHand();
        }
        return p.getInventory().getItemInMainHand();
    }

    ItemStack getItemInMainHand(PlayerInventory inv) {
        if (main.mcVersion < 9) {
            return inv.getItemInHand();
        }
        return inv.getItemInMainHand();
    }

    // Returns 16 for 1.16, etc.
    static int getMcVersion(String bukkitVersionString) {
        Pattern p = Pattern.compile("^1\\.(\\d*)\\.");
        Matcher m = p.matcher((bukkitVersionString));
        int version = -1;
        while (m.find()) {
            if (NumberUtils.isNumber(m.group(1)))
                version = Integer.parseInt(m.group(1));
        }
        return version;
    }

    static void renameFileInPluginDir(Main plugin, String oldName, String newName) {
        File oldFile = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + oldName);
        File newFile = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + newName);
        oldFile.getAbsoluteFile().renameTo(newFile.getAbsoluteFile());
    }
}
