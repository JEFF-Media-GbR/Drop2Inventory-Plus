package de.jeff_media.drop2inventory.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class PDCUtils {

    public static boolean has(ItemStack item, NamespacedKey key, PersistentDataType type) {
        if(item == null) return false;
        if(!item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(key, type);

    }

    public static void remove(ItemStack item, NamespacedKey key) {
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.remove(key);
        item.setItemMeta(meta);
    }

    public static void add(ItemStack item, NamespacedKey key, PersistentDataType type, Object value) {
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(key, type, value);
        item.setItemMeta(meta);
    }
}
