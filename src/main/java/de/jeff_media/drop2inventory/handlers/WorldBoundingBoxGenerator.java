package de.jeff_media.drop2inventory.handlers;

import com.google.common.base.Enums;
import de.jeff_media.drop2inventory.Main;
import de.jeff_media.drop2inventory.config.Config;
import de.jeff_media.drop2inventory.config.Permissions;
import de.jeff_media.drop2inventory.data.BoundingBoxPrediction;
import de.jeff_media.drop2inventory.data.WorldBoundingBox;
import de.jeff_media.drop2inventory.utils.ParticleUtils;
import de.jeff_media.jefflib.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
import java.util.function.Predicate;

/**
 * Generates a WorldBoundingBox of exactly the size and lifetime needed to catch all predicted drops.
 * TODO: Support for Chorus Trees etc.
 */
public class WorldBoundingBoxGenerator {

    private static final Set<Material> unstable = new HashSet<>();
    private final Block block;
    private final Location location;
    private final Player player;
    private final Main main = Main.getInstance();
    private static final Predicate<Entity> HANGING_PREDICATE = entity->entity instanceof Hanging;

    public WorldBoundingBoxGenerator(Location location, Block block, Player player) {
        this.location = location;
        this.block = block;
        this.player = player;
    }

    public static WorldBoundingBox getAppropriateBoundingBox(Location location, @Nullable Block block, Player player) {
        return new WorldBoundingBoxGenerator(location, block, player).get();
    }

    public static void init() {
        Main main = Main.getInstance();
        InputStream stream = main.getResource("unstable-materials.yml");
        Reader reader = new InputStreamReader(stream);
        YamlConfiguration unstableYaml = YamlConfiguration.loadConfiguration(reader);
        for(String type : unstableYaml.getStringList("materials")) {
            Material material = Enums.getIfPresent(Material.class, type).orNull();
            if(material != null) {
                unstable.add(material);
            }
        }
    }

    private WorldBoundingBox get() {

        int lifeTime = 3; // TODO: Adjust this, It was originally set to 2
        int radius = main.getConfig().getInt(Config.DEFAULT_BOUNDING_BOX_RADIUS); // TODO: This was normally set to 1
        int radiusYMin = main.getConfig().getInt(Config.DEFAULT_BOUNDING_BOX_RADIUS); // TODO: This was normally set to 0
        int radiusYMax = main.getConfig().getInt(Config.DEFAULT_BOUNDING_BOX_RADIUS); // TODO: This was normally set to 1 or 2

        if (block != null) {
            Material mat = block.getType();

            if (isUnstable(mat) || isUnstable(block.getRelative(BlockFace.UP).getType())) {
                BoundingBoxPrediction boundingBoxPrediction = getUnstableBlocksAbove(block.getRelative(BlockFace.UP));
                radiusYMax = boundingBoxPrediction.getHeightNeeded();
                lifeTime += boundingBoxPrediction.getLifetimeNeeded();
            }

        }

        if (lifeTime > 20) lifeTime = 20;

        // Check for hanging entities
        if(block != null && hasHangingsAttached(block)) {
            /*Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
                NMSManager.applyBlockPhysics(block);
            }, 1l);*/
            lifeTime += 200;
        }

        WorldBoundingBox result = new WorldBoundingBox(location, lifeTime, radius, radiusYMin, radiusYMax);
        if(player.hasPermission(Permissions.ALLOW_TOGGLE_DEBUG) && Main.getInstance().isDebug()) {
            ParticleUtils.draw(player, result);
        }
        if(main.isDebug()) {
            main.debug("Created WorldBoundingBox: " + result);
        }
        return result;
    }

    private boolean hasHangingsAttached(Block block) {
        Collection<Entity> nearbyHangings = block.getWorld().getNearbyEntities(block.getLocation().add(0.5,0.5,0.5),1,1,1, HANGING_PREDICATE);
        for(Entity entity : nearbyHangings) {
            BlockFace attachedFace = ((Hanging) entity).getAttachedFace();
            if(entity.getLocation().getBlock().getRelative(attachedFace).equals(block)) {
                return true;
            }
        }
        return false;
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
