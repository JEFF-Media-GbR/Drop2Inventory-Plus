package de.jeff_media.drop2inventory.data;

import de.jeff_media.drop2inventory.enums.DropReason;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the subject that caused a drop - which is either a broken block or a killed entity.
 */
public class DropSubject {

    private final DropReason dropReason;
    private final Location location;
    private final Object subject;

    public DropSubject(Block block) {
        this.subject = block;
        this.location = block.getLocation();
        this.dropReason = DropReason.BLOCK_BREAK;
    }

    public DropSubject(Entity entity) {
        this.subject = entity;
        this.location = entity.getLocation();
        this.dropReason = DropReason.ENTITY_KILL;
    }

    public @Nullable Block getBlock() {
        if (dropReason == DropReason.BLOCK_BREAK) {
            return (Block) subject;
        }
        return null;
    }

    public DropReason getDropReason() {
        return dropReason;
    }

    public @Nullable Entity getEntity() {
        if (dropReason == DropReason.ENTITY_KILL) {
            return (Entity) subject;
        }
        return null;
    }

    public Location getLocation() {
        return location;
    }

    public World getWorld() {
        return location.getWorld();
    }

}
