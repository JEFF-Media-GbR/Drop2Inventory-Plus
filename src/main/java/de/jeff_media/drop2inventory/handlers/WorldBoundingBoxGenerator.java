package de.jeff_media.drop2inventory.handlers;

import de.jeff_media.drop2inventory.data.BoundingBoxPrediction;
import de.jeff_media.drop2inventory.data.WorldBoundingBox;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates a WorldBoundingBox of exactly the size and lifetime needed to catch all predicted drops.
 * TODO: Support for Chorus Trees etc.
 */
public class WorldBoundingBoxGenerator {

    private static final Set<Material> unstable = new HashSet<>();
    private static final List<Material> unstableMaterials = Arrays.asList(Material.SUGAR_CANE, Material.CACTUS);
    private static final Tag[] unstableTags = {Tag.BANNERS, Tag.BUTTONS, Tag.CARPETS, Tag.CAMPFIRES, Tag.CORAL_PLANTS, Tag.CROPS, Tag.DOORS, Tag.FLOWERS, Tag.PRESSURE_PLATES, Tag.RAILS, Tag.SAPLINGS, Tag.SIGNS, Tag.SMALL_FLOWERS, Tag.STANDING_SIGNS, Tag.TALL_FLOWERS, Tag.TRAPDOORS};
    private final Block block;
    private final Location location;

    public WorldBoundingBoxGenerator(Location location, Block block) {
        this.location = location;
        this.block = block;
    }

    private static void addUnstable(Tag tag) {
        unstable.addAll(tag.getValues());
    }

    public static WorldBoundingBox getAppropriateBoundingBox(Location location, @Nullable Block block) {

        return new WorldBoundingBoxGenerator(location, block).get();

    }

    public static void init() {
        //Main.getInstance().getLogger().info("Generating Set of unstable Materials...");
        for (Tag tag : unstableTags) {
            addUnstable(tag);
        }
        unstable.addAll(unstableMaterials);

        // DEBUG
        for (Material mat : new ArrayList<>(unstable).stream().sorted(Comparator.comparing(Enum::name)).collect(Collectors.toList())) {
            //System.out.println(mat.name());
        }
    }

    private WorldBoundingBox get() {

        int lifeTime = 2;
        int radius = 1;
        int radiusYMin = 0;
        int radiusYMax = 1;

        if (block != null) {
            Material mat = block.getType();

            if (isUnstable(mat) || isUnstable(block.getRelative(BlockFace.UP).getType())) {
                BoundingBoxPrediction boundingBoxPrediction = getUnstableBlocksAbove(block.getRelative(BlockFace.UP));
                radiusYMax = boundingBoxPrediction.getHeightNeeded();
                lifeTime += boundingBoxPrediction.getLifetimeNeeded();
            }

        }

        if (lifeTime > 20) lifeTime = 20;
        /*Main.getInstance().getLogger().warning("Bounding Box");
        Main.getInstance().getLogger().warning("- LifeTime: " + lifeTime);
        Main.getInstance().getLogger().warning("- radius: " + radius);
        Main.getInstance().getLogger().warning("- Y Min: " + radiusYMin);
        Main.getInstance().getLogger().warning("- Y Max: " + radiusYMax);*/
        return new WorldBoundingBox(location, lifeTime, radius, radiusYMin, radiusYMax);
    }

    private BoundingBoxPrediction getUnstableBlocksAbove(Block block) {
        Block current = block;
        int extraLifetimeNeeded = 0;
        int height = 0;
        int gravityBlocksAbove = 0;
        while (extraLifetimeNeeded < 256) {
            if (isUnstable(current.getType())) {
                //System.out.println(extraLifetimeNeeded + ": " + current + " is unstable");
                extraLifetimeNeeded += 1;
                height += 1;
                gravityBlocksAbove = 0;
            } else if (current.getType().hasGravity()) {
                //System.out.println(extraLifetimeNeeded + ": " + current + " is gravity");
                extraLifetimeNeeded += 2;
                height += 1;
                gravityBlocksAbove += 1;
            } else {
                extraLifetimeNeeded -= gravityBlocksAbove * 2;
                height -= gravityBlocksAbove;
                break;
            }
            current = current.getRelative(BlockFace.UP);
        }
        return new BoundingBoxPrediction(height, extraLifetimeNeeded);
    }

    private boolean isUnstable(Material mat) {
        //System.out.println("isUnstable? " + mat.name());
        if (mat.isAir()) {
            //System.out.println("  no, its air");
            return false;
        }
        if (unstable.contains(mat)) {
            //System.out.println("  yes");
            return true;
        }
        //System.out.println("  no");
        return false;
    }

}
