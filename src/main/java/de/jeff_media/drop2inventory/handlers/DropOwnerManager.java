package de.jeff_media.drop2inventory.handlers;

import de.jeff_media.drop2inventory.Main;
import de.jeff_media.drop2inventory.data.WorldBoundingBox;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Manages who the owner is of drops that have not yet been spawned.
 */
public class DropOwnerManager {

    private final static HashMap<WorldBoundingBox, Player> dropLocationMap = new HashMap<>();
    private final static Main main = Main.getInstance();

    static {

        /*
          Clear bounding boxes
         */
        Bukkit.getScheduler().scheduleSyncRepeatingTask(main, ()->{
            for (WorldBoundingBox boundingBox : dropLocationMap.keySet().toArray(new WorldBoundingBox[0])) { //ConcurrentModificationException
                if (boundingBox.isExpired()) {
                    dropLocationMap.remove(boundingBox);
                    if(main.debug) main.debug("BoundingBox expired");
                } else {
                    //System.out.println("Collecting for " + boundingBox.getTicksLeft() + " more ticks");
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
        return dropLocationMap.get(possibleBoundingBoxes.get(0));
    }

    public static void register(Player player, Location location, @Nullable Block block) {
        if(main.debug) main.debug("Registering DropOwner " + player.getName() + " for location " + location);
        WorldBoundingBox boundingBox = WorldBoundingBoxGenerator.getAppropriateBoundingBox(location, block, player);
        dropLocationMap.put(boundingBox, player);
        //System.out.println("BoundingBox registered");
    }


}
