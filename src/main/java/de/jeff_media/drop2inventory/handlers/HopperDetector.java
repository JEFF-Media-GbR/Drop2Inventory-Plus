package de.jeff_media.drop2inventory.handlers;

import de.jeff_media.drop2inventory.Main;
import de.jeff_media.drop2inventory.config.Config;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class HopperDetector {

    private final Main main = Main.getInstance();
    private final boolean hopperDetection = main.getConfig().getBoolean(Config.IGNORE_ITEMS_ON_HOPPERS);
    private final int vRange = main.getConfig().getInt(Config.IGNORE_ITEMS_ON_HOPPERS_VERTICAL_RANGE);
    private final int hRange = main.getConfig().getInt(Config.IGNORE_ITEMS_ON_HOPPERS_HORIZONTAL_RANGE);


    public boolean isAboveHopper(Location location) {

        if(!hopperDetection) return false;

        //main.debug("Checking if "+itemStack.toString()+" is above a hopper at "+location.toString());

        for(int y = 0; y <= vRange; y++) {
            for(int x = -hRange; x <= hRange; x++) {
                for(int z = -hRange; z <= hRange; z++) {
                    Block current = location.getBlock().getRelative(x, -y, z);
                    if (current.getType() == Material.HOPPER) {
                        if(main.isDebug()) main.debug("ItemSpawn above Hopper detected");
                        return true;
                    }
                }
            }
        }
        main.debug("No hopper nearby.");
        return false;
    }



}
