package de.jeff_media.drop2inventory.hooks;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class QuickShopHook implements IPluginHook{

    private boolean isQuickShopHologram(ItemStack item) {
        if(item == null) return false;
        if(!item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if(meta == null) return false;
        if(!meta.hasLore()) return false;
        List<String> lore = meta.getLore();
        if(lore == null) return false;
        for(String line : lore) {
            if(line.contains("itemStackString")) return true;
        }
        return false;
    }

    @Override
    public boolean mayPickUp(Item item, Player player) {
        return !isQuickShopHologram(item.getItemStack());
    }
}
