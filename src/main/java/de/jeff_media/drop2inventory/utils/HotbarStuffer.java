package de.jeff_media.drop2inventory.utils;

import de.jeff_media.drop2inventory.Main;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * TODO: This could be done way better. It's old but it works.
 */
public class HotbarStuffer {

    final ItemStack filler;
    private final Main main;

    public HotbarStuffer(Main main) {
        this.main=main;
        filler = new ItemStack(Material.DIRT);
        filler.setAmount(64);
        ItemMeta meta = filler.getItemMeta();
        meta.setLore(Arrays.asList("§cDrop2Inventory Hotbar filler","§4If you can see this, please","§4create a bug report."));
        meta.setDisplayName("§cDrop2Inventory Hotbar filler");
        filler.setItemMeta(meta);
    }

    public void stuffHotbar(PlayerInventory inv) {
        boolean free = false;
        for(int i = 9; i < 36; i++) {
            if(inv.getItem(i) == null || inv.getItem(i).getType()==Material.AIR) {
                free = true;
                break;
            }
        }
        if(free) {
            main.debug("Inventory has free slots, filling up hotbar");
            for (int i = 0; i < 9; i++) {
                if (inv.getItem(i) == null || inv.getItem(i).getType() == Material.AIR) {
                    inv.setItem(i, filler);
                }
            }
        } else {
            main.debug("Inventory is full, skipping hotbar filler");
        }
    }

    public void unstuffHotbar(PlayerInventory inv) {
        inv.remove(filler);
    }

}
