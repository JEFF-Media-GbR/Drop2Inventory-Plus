package de.jeff_media.drop2inventory.handlers;

import de.jeff_media.drop2inventory.events.Drop2InventoryPickupItemEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

public class EventManager {

    // TODO: This isn't event related anymore, move to another class
    public static void giveAdjustedXP(Player player, int originalExperience) {
        if (originalExperience == 0) return;
        //player.giveExp(getExperienceToGive(player, originalExperience));
        ExperienceOrb orb = player.getWorld().spawn(player.getLocation(), ExperienceOrb.class);
        //orb.setExperience(getExperienceToGive(player, originalExperience));
        orb.setExperience(originalExperience);
        orb.setVelocity(player.getVelocity());
    }

    public static boolean mayPickUp(Player player, Item item) {
        Drop2InventoryPickupItemEvent event = new Drop2InventoryPickupItemEvent(player, item, 0);
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }
}
