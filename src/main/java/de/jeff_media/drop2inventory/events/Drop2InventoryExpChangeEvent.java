package de.jeff_media.drop2inventory.events;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.jetbrains.annotations.NotNull;

public class Drop2InventoryExpChangeEvent extends PlayerExpChangeEvent {
    public Drop2InventoryExpChangeEvent(@NotNull Player player, int expAmount) {
        super(player, expAmount);
    }
}
