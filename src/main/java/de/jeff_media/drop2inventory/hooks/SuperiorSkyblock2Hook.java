package de.jeff_media.drop2inventory.hooks;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SuperiorSkyblock2Hook {

    public static boolean isInForeignIsland(Player player, Location location) {
        final Island island = SuperiorSkyblockAPI.getIslandAt(location);
        if(island == null) return false;
        final SuperiorPlayer superiorPlayer = SuperiorSkyblockAPI.getPlayer(player);
        if(superiorPlayer == null) return false;
        return !island.getOwner().equals(superiorPlayer) && !island.isCoop(superiorPlayer) && !island.isMember(superiorPlayer);
    }
}
