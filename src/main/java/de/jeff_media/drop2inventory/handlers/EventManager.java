package de.jeff_media.drop2inventory.handlers;

import de.jeff_media.drop2inventory.Main;
import de.jeff_media.drop2inventory.config.Config;
import de.jeff_media.drop2inventory.events.Drop2InventoryPickupItemEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

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

        if(Main.getInstance().getConfig().getBoolean(Config.IM_USING_OUTDATED_PLUGINS)) {
            try {
                org.bukkit.event.player.PlayerPickupItemEvent outdatedEvent = new org.bukkit.event.player.PlayerPickupItemEvent(player, item, 0);
                Bukkit.getPluginManager().callEvent(outdatedEvent);
                if (outdatedEvent.isCancelled()) return false;
            } catch (Throwable ignored) {

            }
        }

        Drop2InventoryPickupItemEvent event = new Drop2InventoryPickupItemEvent(player, item, 0);
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }
}
