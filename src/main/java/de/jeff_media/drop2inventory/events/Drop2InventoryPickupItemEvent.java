package de.jeff_media.drop2inventory.events;

import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.jetbrains.annotations.NotNull;

public class Drop2InventoryPickupItemEvent extends EntityPickupItemEvent {
    public Drop2InventoryPickupItemEvent(@NotNull LivingEntity entity, @NotNull Item item, int remaining) {
        super(entity, item, remaining);
    }
}
