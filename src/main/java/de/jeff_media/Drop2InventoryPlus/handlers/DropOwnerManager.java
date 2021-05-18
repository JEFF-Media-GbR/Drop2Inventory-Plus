package de.jeff_media.Drop2InventoryPlus.handlers;

import de.jeff_media.Drop2InventoryPlus.Main;
import de.jeff_media.Drop2InventoryPlus.data.WorldBoundingBox;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Manages who the owner is of drops that have not yet been spawned.
 */
public class DropOwnerManager {

    private final static HashMap<WorldBoundingBox, Player> dropLocationMap = new HashMap<>();
    private final static Main main = Main.getInstance();

    static {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(main, ()->{
            for (WorldBoundingBox boundingBox : dropLocationMap.keySet().toArray(new WorldBoundingBox[0])) { //ConcurrentModificationException
                if (boundingBox.isExpired()) {
                    dropLocationMap.remove(boundingBox);
                    System.out.println("BoundingBox expired");
                } else {
                    System.out.println("Collecting for " + boundingBox.getTicksLeft() + " more ticks");
                }
            }
        }, 1, 1);
    }

    public static @Nullable Player getDropOwner(Location location) {
        List<WorldBoundingBox> possibleBoundingBoxes = new ArrayList<>();
        for (WorldBoundingBox boundingBox : dropLocationMap.keySet()) {
            if (boundingBox.contains(location)) {
                possibleBoundingBoxes.add(boundingBox);
            }
        }
        if (possibleBoundingBoxes.size() == 0) return null;
        possibleBoundingBoxes.sort(new WorldBoundingBox.BoundingBoxComparator(location));
        Player player = dropLocationMap.get(possibleBoundingBoxes.get(0));
        return player;
    }

    public static void register(Player player, Location location, @Nullable Block block) {
        WorldBoundingBox boundingBox = WorldBoundingBoxGenerator.getAppropriateBoundingBox(location, block);
        dropLocationMap.put(boundingBox, player);
        System.out.println("BoundingBox registered");
    }


}
