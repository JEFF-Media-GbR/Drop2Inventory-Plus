package de.jeff_media.drop2inventory.utils;

import com.jeff_media.jefflib.EnumUtils;
import de.jeff_media.drop2inventory.Main;
import de.jeff_media.drop2inventory.config.Config;
import de.jeff_media.drop2inventory.config.Permissions;
import de.jeff_media.drop2inventory.hooks.EcoItemsHook;
import de.jeff_media.morepersistentdatatypes.DataType;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Locale;

/**
 * Condenses ingots.
 * TODO: Toggleable per player using the Player's PersistentDataContainer
 */
public class IngotCondenser {

    final Main main;
    final HashMap<Material, CondensationMap> condensationMap = new HashMap<>();
    @Getter private final NamespacedKey autoCondenseKey;

    public IngotCondenser(Main main) {
        this.main = main;
        this.autoCondenseKey = new NamespacedKey(main, "has-autocondense-enabled");
        try {
            initFromFile();
        } catch (IOException e) {
            main.getLogger().warning("Could not read condensation map from file");
        }


        for (CondensationMap map : condensationMap.values()) {
            if (main.isDebug())
                main.debug(String.format("%d x %s = %s", map.number, map.item.name(), map.block.name()));
        }

    }

    public boolean hasEnabled(Player player) {
        if(main.isDebug()) {
            System.out.println("IngotCondenser.hasEnabled: " + player.getName());
            System.out.println("Config: " + main.getConfig().getBoolean(Config.AUTO_CONDENSE));
            System.out.println("Permission: " + player.hasPermission(Permissions.ALLOW_AUTO_CONDENSE));
            System.out.println("PDC: " + player.getPersistentDataContainer().has(autoCondenseKey, DataType.BOOLEAN));
        }
        return (main.getConfig().getBoolean(Config.AUTO_CONDENSE) || player.getPersistentDataContainer().has(autoCondenseKey, DataType.BOOLEAN)) && player.hasPermission(Permissions.ALLOW_AUTO_CONDENSE);
    }

    void initFromFile() throws IOException {
        File file = new File(main.getDataFolder(), "condense.yml");
        File oldFile = new File(main.getDataFolder(), "condense.csv");
        File oldOldFile = new File(main.getDataFolder(), "condense.txt");
        if(oldFile.exists()) oldFile.delete();
        if(oldOldFile.exists()) oldOldFile.delete();
        if (!file.exists()) {
            main.saveResource("condense.yml", false);
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        for(String key : yaml.getKeys(false)) {
            Material item = EnumUtils.getIfPresent(Material.class,key.toUpperCase(Locale.ROOT)).orElse(null);
            if(item == null) {
                main.getLogger().warning("Invalid material in condense.yml: " + key);
                continue;
            }
            Material block = EnumUtils.getIfPresent(Material.class, yaml.getString(key + ".result", "").toUpperCase(Locale.ROOT)).orElse(null);
            if(block == null) {
                main.getLogger().warning("Invalid material in condense.yml: " + key);
                continue;
            }
            if(yaml.isInt(key + ".required")) {
                condensationMap.put(item, new CondensationMap(item, yaml.getInt(key + ".required"), block));
            } else {
                main.getLogger().warning("Invalid amount in condense.yml: " + yaml.get(key + ".amount"));
            }
        }
    }

    public void condense(Inventory inv, Material mat) {

        if(main.isDebug()) main.debug("Running autocondense for " + mat.name());

        CondensationMap map = condensationMap.get(mat);

        if (map == null) {
            if(main.isDebug()) main.debug("No condensation map found for " + mat.name());
            return;
        }
        if (main.isDebug()) main.debug("Trying to condense " + mat.name());

        int amount = 0;
        for (ItemStack is : inv.all(map.item).values()) {
            if(EcoItemsHook.isEcoItemsItem(is)) continue;
            amount += is.getAmount();
        }
        if (main.isDebug()) main.debug("  Found " + amount + " times");
        if (amount < map.number) {
            if (main.isDebug()) main.debug("  Returning! Thats not enough");
            return;
        }
        //inv.remove(map.item);
        for(ItemStack item : inv) {
            if(item == null) continue;
            if (item.getType() == map.item) {
                if(EcoItemsHook.isEcoItemsItem(item)) continue;
                inv.remove(item);
            }
        }
        int blocks = amount / map.number;
        int items = amount % map.number;
        inv.addItem(new ItemStack(map.block, blocks));
        inv.addItem(new ItemStack(map.item, items));
    }

    void condense(Inventory inv) {
        for (Material mat : condensationMap.keySet()) {
            condense(inv, mat);
        }
    }

    static class CondensationMap {
        final Material item;
        final int number;
        final Material block;

        CondensationMap(Material mat, int number, Material block) {
            this.item = mat;
            this.number = number;
            this.block = block;
        }
    }


}
