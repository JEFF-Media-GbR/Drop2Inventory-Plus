package de.jeff_media.drop2inventory.utils;

import de.jeff_media.drop2inventory.Main;
import de.jeff_media.drop2inventory.data.WorldBoundingBox;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;

import java.util.HashSet;
import java.util.Set;

public class ParticleUtils {

    private static Particle particleType = Particle.VILLAGER_HAPPY;
    private static int particleCount = 1;

    public static void draw(Player player, WorldBoundingBox worldBoundingBox) {
        BoundingBox box = worldBoundingBox.getBoundingBox();
        World world = worldBoundingBox.getWorld();
        Set<Location> points = getHollowCube(box.getMin().toLocation(world), box.getMax().toLocation(world), 0.5);
        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                for(Location location : points) {
                    player.spawnParticle(particleType, location, particleCount);
                }
                count++;
                if(count >= 4) cancel();
            }
        }.runTaskTimer(Main.getInstance(), 0, 5);
    }

    public static Set<Location> getHollowCube(Location corner1, Location corner2, double particleDistance) {
        Set<Location> result = new HashSet<>();
        World world = corner1.getWorld();
        double minX = Math.min(corner1.getX(), corner2.getX());
        double minY = Math.min(corner1.getY(), corner2.getY());
        double minZ = Math.min(corner1.getZ(), corner2.getZ());
        double maxX = Math.max(corner1.getX(), corner2.getX());
        double maxY = Math.max(corner1.getY(), corner2.getY());
        double maxZ = Math.max(corner1.getZ(), corner2.getZ());

        for (double x = minX; x <= maxX; x+=particleDistance) {
            for (double y = minY; y <= maxY; y+=particleDistance) {
                for (double z = minZ; z <= maxZ; z+=particleDistance) {
                    int components = 0;
                    if (x == minX || x == maxX) components++;
                    if (y == minY || y == maxY) components++;
                    if (z == minZ || z == maxZ) components++;
                    if (components >= 2) {
                        result.add(new Location(world, x, y, z));
                    }
                }
            }
        }

        return result;
    }
}
