package de.jeff_media.drop2inventory.hooks;

import de.jeff_media.drop2inventory.Main;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class CoinsHook implements IPluginHook {

    final NamespacedKey key;

    CoinsHook() {
        if(Bukkit.getPluginManager().getPlugin("Coins") != null) {
            key = NamespacedKey.fromString("coins:coins-type");
        } else {
            key = null;
        }
    }

    @Override
    public boolean mayPickUp(Item item, Player player) {
        if(key == null) return true;
        ItemStack itemStack = item.getItemStack();
        if(!itemStack.hasItemMeta()) return true;
        PersistentDataContainer pdc = itemStack.getItemMeta().getPersistentDataContainer();
        return !pdc.has(key, PersistentDataType.INTEGER);
    }
}
