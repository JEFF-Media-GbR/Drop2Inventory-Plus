package de.jeff_media.drop2inventory.legacy;

import de.jeff_media.drop2inventory.annotations.Unused;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Set;

@Unused
public class PendingDrop {

    private final Set<Entity> existingItems;
    private final Location location;
    private final Player player;

    public PendingDrop(Player player, Location location, Set<Entity> existingItems) {
        this.player = player;
        this.location = location;
        this.existingItems = existingItems;
    }

    public Set<Entity> getExistingItems() {
        return existingItems;
    }

    public Location getLocation() {
        return location;
    }

    public Player getPlayer() {
        return player;
    }
}
