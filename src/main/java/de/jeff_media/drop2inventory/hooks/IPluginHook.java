package de.jeff_media.drop2inventory.hooks;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

public interface IPluginHook {

    boolean mayPickUp(Item item, Player player);

}
