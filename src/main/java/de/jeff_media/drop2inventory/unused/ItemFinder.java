package de.jeff_media.drop2inventory.unused;

import de.jeff_media.drop2inventory.annotations.Unused;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

@Unused
public class ItemFinder {

    private static final Predicate<Entity> ITEM_PREDICATE = entity->entity instanceof Item;
    private static final int ITEM_RADIUS = 5;

    public static Set<Entity> getNearbyItems(Location location) {
        Set<Entity> items = new HashSet<>();
        for (Entity entity : location.getWorld().getNearbyEntities(location, ITEM_RADIUS, ITEM_RADIUS, ITEM_RADIUS, ITEM_PREDICATE)) {
            // TODO: Check if distanceSquared works
            if (entity.getLocation().distanceSquared(location) <= ITEM_RADIUS * ITEM_RADIUS) {
                items.add(entity);
            }
        }
        return items;
    }
}
