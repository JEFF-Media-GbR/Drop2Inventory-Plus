package de.jeff_media.drop2inventory.hooks;

import com.bgsoftware.wildchests.api.WildChestsAPI;
import com.bgsoftware.wildchests.api.objects.chests.StorageChest;
import com.bgsoftware.wildstacker.api.WildStackerAPI;
import de.jeff_media.drop2inventory.Main;
import de.jeff_media.drop2inventory.data.SimpleLocation;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashSet;

public class WildChestsHook {

    private final Collection<SimpleLocation> locations = new HashSet<>();

    public boolean isStorageChest(Location location) {
        return WildChestsAPI.getStorageChest(location) != null;
    }

    public void registerStorageChestLocation(Location location) {
        SimpleLocation simpleLocation = new SimpleLocation(location);
        locations.add(simpleLocation);
        new BukkitRunnable() {
            @Override
            public void run() {
                locations.remove(simpleLocation);
                System.out.println("WildChests Storage location expired");
            }
        }.runTaskLater(Main.getInstance(), 10L);
    }

    public boolean wasStorageChestLocation(Location location) {
        SimpleLocation simpleLocation = new SimpleLocation(location);
        boolean result = locations.contains(simpleLocation);
        System.out.println(location + " was a Storage chest: " + result);
        System.out.println("All available Storage chests: ");
        for(SimpleLocation location2 : locations) {
            System.out.println(location2);
        }
        return result;
    }

}
