package de.jeff_media.drop2inventory.hooks;

import de.jeff_media.drop2inventory.Main;
import de.jeff_media.drop2inventory.utils.PDCUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public class EliteMobsHook implements IPluginHook {

    private static final NamespacedKey SOULBOUND_KEY;
    private static final String CURRENCY_LORE = "EliteMobsCurrencyItem";
    private final Main main = Main.getInstance();

    static {
        Plugin eliteMobsPlugin = Bukkit.getPluginManager().getPlugin("EliteMobs");
        if(eliteMobsPlugin != null) {
            SOULBOUND_KEY = new NamespacedKey(eliteMobsPlugin, "soulbind");
        } else {
            SOULBOUND_KEY = null;
        }
    }

    @Override
    public boolean mayPickUp(Item item, Player player) {
        ItemStack itemStack = item.getItemStack();
        if(!itemStack.hasItemMeta()) return true;
        ItemMeta meta = itemStack.getItemMeta();
        if(isCurrencyItem(meta)) {
            if(main.isDebug()) main.debug("Item pickup forbidden because it's a EliteMobs currency item");
            return false;
        }
        if(isPickupForbiddenBySoulbind(itemStack, player)) {
            if(main.isDebug()) main.debug("Item pickup forbidden because it's EliteMobs soulbound but not to this player");
            return false;
        }
        if(isVisualItem(itemStack)) {
            return false;
        }
        return true;
    }

    private boolean isVisualItem(ItemStack itemStack) {
        if(!itemStack.hasItemMeta()) return false;
        if(!itemStack.getItemMeta().hasLore()) return false;
        if(itemStack.getItemMeta().getLore().size()==0) return false;
        return itemStack.getItemMeta().getLore().get(0).equals("visualItem");
    }

    private boolean isPickupForbiddenBySoulbind(ItemStack itemStack, Player player) {
        if(SOULBOUND_KEY == null) return false;
        if(!PDCUtils.has(itemStack,SOULBOUND_KEY, PersistentDataType.STRING)) return false;
        String soulbindUuidString = itemStack.getItemMeta().getPersistentDataContainer().get(SOULBOUND_KEY,PersistentDataType.STRING);
        UUID soulbindUuid = UUID.fromString(soulbindUuidString);
        if(player.getUniqueId().equals(soulbindUuid)) return false;
        return true;
    }

    private boolean isCurrencyItem(ItemMeta meta) {
        if(!meta.hasLore()) return false;
        if(meta.getLore().size()==0) return false;
        return meta.getLore().get(0).equals(CURRENCY_LORE);
    }
}
