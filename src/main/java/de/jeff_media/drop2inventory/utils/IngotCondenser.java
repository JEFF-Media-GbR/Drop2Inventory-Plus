package de.jeff_media.drop2inventory.utils;

import de.jeff_media.drop2inventory.Main;
import de.jeff_media.drop2inventory.hooks.EcoItemsHook;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * Condenses ingots.
 * TODO: Toggleable per player using the Player's PersistentDataContainer
 */
public class IngotCondenser {

    final Main main;
    final HashMap<Material, CondensationMap> condensationMap = new HashMap<>();

    public IngotCondenser(Main main) {
        this.main = main;
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

    void initFromFile() throws IOException {
        InputStream in = main.getResource("condense.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        while (reader.ready()) {
            String line = reader.readLine();
            String[] parts = line.split(",");
            Material item = Material.getMaterial(parts[0].toUpperCase());
            Material block = Material.getMaterial(parts[2].toUpperCase());
            int number = Integer.parseInt(parts[1]);
            if (item == null) {
                //main.getLogger().info("Skipping unknown material "+parts[0]);
                continue;
            }
            if (block == null) {
                //main.getLogger().info("Skipping unknown material "+parts[2]);
                continue;
            }
            condensationMap.put(item, new CondensationMap(item, number, block));
        }
    }

    public void condense(Inventory inv, Material mat) {

        CondensationMap map = condensationMap.get(mat);

        if (map == null) return;
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
