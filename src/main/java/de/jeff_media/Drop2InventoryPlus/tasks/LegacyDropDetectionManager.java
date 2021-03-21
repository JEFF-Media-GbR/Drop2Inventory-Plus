package de.jeff_media.Drop2InventoryPlus.tasks;

import de.jeff_media.Drop2InventoryPlus.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.HashSet;

public class LegacyDropDetectionManager {

    Main main;
    HashSet<Location> locations;

    public LegacyDropDetectionManager() {
        main = Main.getInstance();
        locations = new HashSet<>();
    }

    public void registerIgnoredLocation(Location location) {
        locations.add(location);
        Bukkit.getScheduler().scheduleSyncDelayedTask(main,() -> {
            locations.remove(location);
        },2l);
    }

    public boolean isNearIgnoredLocation(Location location) {
        for(Location location1 : locations) {
            if(location.getWorld().getUID().equals(location1.getWorld().getUID())) {
                if (location.distance(location1) < 1) return true;
            }
        }
        return false;
    }



}
