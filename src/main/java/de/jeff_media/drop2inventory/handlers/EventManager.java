package de.jeff_media.drop2inventory.handlers;

import de.jeff_media.drop2inventory.Main;
import de.jeff_media.drop2inventory.config.Config;
import de.jeff_media.drop2inventory.events.Drop2InventoryPickupItemEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class EventManager {

    private static final Main main = Main.getInstance();
    private static final Map<Player, ExperienceOrb> pendingXpDrops = new HashMap<>();

    // TODO: This isn't event related anymore, move to another class
    public static void giveAdjustedXP(Player player, int originalExperience) {
        if (originalExperience == 0) return;
        //player.giveExp(getExperienceToGive(player, originalExperience));

        if(pendingXpDrops.containsKey(player)) {
            ExperienceOrb orb = pendingXpDrops.get(player);
            if(orb.isValid() && !orb.isDead()) {
                orb.setExperience(orb.getExperience() + originalExperience);
                return;
            }
        }
        ExperienceOrb orb = player.getWorld().spawn(player.getLocation(), ExperienceOrb.class);
        orb.setExperience(originalExperience);
        orb.setVelocity(player.getVelocity());

        pendingXpDrops.put(player,orb);

        Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
            pendingXpDrops.remove(player);
        },1);
    }

    public static boolean mayPickUp(Player player, Item item) {

        if(item.getPickupDelay() >= Short.MAX_VALUE) {
            return false;
        }

        if(Main.getInstance().getConfig().getBoolean(Config.IM_USING_OUTDATED_PLUGINS)) {
            try {
                org.bukkit.event.player.PlayerPickupItemEvent outdatedEvent = new org.bukkit.event.player.PlayerPickupItemEvent(player, item, 0);
                Bukkit.getPluginManager().callEvent(outdatedEvent);
                if (outdatedEvent.isCancelled()) return false;
            } catch (Throwable ignored) {

            }
        }

        Drop2InventoryPickupItemEvent event = new Drop2InventoryPickupItemEvent(player, item, 0);
        if(stackContainsNossrParty()) return true;
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    private static boolean stackContainsNossrParty() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            if(element.getClassName().contains("com.gmail.nossr50.party")) {
                return true;
            }
        }
    }
}
