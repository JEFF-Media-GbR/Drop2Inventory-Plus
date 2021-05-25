package de.jeff_media.Drop2InventoryPlus.unused;

import de.jeff_media.Drop2InventoryPlus.annotations.Unused;
import de.jeff_media.Drop2InventoryPlus.events.Drop2InventoryPickupItemEvent;
import de.jeff_media.Drop2InventoryPlus.legacy.PendingDrop;
import de.jeff_media.Drop2InventoryPlus.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;

@SuppressWarnings("ALL")
@Unused
public class PickUpTask extends BukkitRunnable {

    private final PendingDrop pendingDrop;
    private final int count = 1;

    private PickUpTask(PendingDrop pendingDrop) {
        this.pendingDrop = pendingDrop;
    }

    @Override
    public void run() {
        if (count >= 3) cancel();
        Set<Entity> items = ItemFinder.getNearbyItems(pendingDrop.getLocation());
        items.removeIf((entity)->pendingDrop.getExistingItems().contains(entity));
        for (Entity entity : items) {
            if (entity == null || entity.isDead()) continue;
            Item item = (Item) entity;
            Drop2InventoryPickupItemEvent pickupItemEvent = new Drop2InventoryPickupItemEvent(pendingDrop.getPlayer(), item, 0);
            Bukkit.getPluginManager().callEvent(pickupItemEvent);
            if (pickupItemEvent.isCancelled()) continue;
            Utils.addOrDrop(item.getItemStack(), pendingDrop.getPlayer(), pendingDrop.getLocation());
            item.remove();
        }
    }
}
