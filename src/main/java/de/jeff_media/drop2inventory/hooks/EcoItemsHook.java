package de.jeff_media.drop2inventory.hooks;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class EcoItemsHook {

    private static final NamespacedKey ECOITEMS_ITEM_KEY = NamespacedKey.fromString("ecoitems:item");

    public static boolean isEcoItemsItem(ItemStack item) {
        if(item.hasItemMeta()) {
            return item.getItemMeta().getPersistentDataContainer().has(ECOITEMS_ITEM_KEY, PersistentDataType.STRING);
        }
        return false;
    }

}
