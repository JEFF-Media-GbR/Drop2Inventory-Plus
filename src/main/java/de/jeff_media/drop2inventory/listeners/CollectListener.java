package de.jeff_media.drop2inventory.listeners;

import de.jeff_media.drop2inventory.Main;
import de.jeff_media.drop2inventory.config.Config;
import de.jeff_media.drop2inventory.data.DropSubject;
import de.jeff_media.drop2inventory.handlers.DropOwnerManager;
import de.jeff_media.drop2inventory.handlers.EventManager;
import de.jeff_media.drop2inventory.handlers.PermissionChecker;
import de.jeff_media.drop2inventory.hooks.PluginHooks;
import de.jeff_media.drop2inventory.hooks.WildChestsHook;
import de.jeff_media.drop2inventory.utils.PDCUtils;
import de.jeff_media.drop2inventory.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

/**
 * Registers events that will probably lead to drops, i.e. EntityDeathEvent and BlockBreakEvent, so that those drops
 * can be given to their rightful owner when the ItemSpawnEvent occurs, which doesn't include the cause of the drop
 */
public class CollectListener implements Listener {

    private final Main main = Main.getInstance();

    /**
     * Cancels the actual Item Spawn and gives it to the player that owns this drop
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void collectDrops(ItemSpawnEvent event) {
        Item item = event.getEntity();
        ItemStack itemStack = item.getItemStack();
        if (PDCUtils.has(itemStack, Main.IGNORED_DROP_TAG, PersistentDataType.BYTE)) {
            PDCUtils.remove(itemStack, Main.IGNORED_DROP_TAG);
            return;
        }
        Location location = event.getLocation();
        Player player = DropOwnerManager.getDropOwner(location);

        if(!main.getPluginHooks().mayPickUp(item, player)) return;

        if (player == null) {
            if (main.isDebug()) main.debug("  Don't pick up: no player found");
            return;
        }
        if(main.getHopperDetector().isAboveHopper(location)) {
            if(main.isDebug()) main.debug("  Don't pick up: above hopper");
            return;
        }
        if (!EventManager.mayPickUp(player, item)) {
            if (main.isDebug()) main.debug("  Don't pick up: EntityPickUpItemEvent cancelled");
            return;
        }

        WildChestsHook wildChestsHook = main.getPluginHooks().getWildChestsHook();
        if(wildChestsHook == null || !wildChestsHook.wasStorageChestLocation(location)) {

            // Collect drops normally
            event.setCancelled(true);
            Utils.addOrDrop(itemStack, player, location);
        } else {

            // WildChests storage chest

            Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
                System.out.println("Delayed stack collection");
                Utils.addOrDrop(item.getItemStack(), player, location);
                item.remove();
            }, 30L);

        }

    }

    /**
     * Sets the BlockBreakEvent's getExpToDrop to zero, calls the EntityExpChangeEvent and adds the adjusted XP
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockXP(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if(!PermissionChecker.hasDrop2InvEnabled(player)) return;
        int experience = event.getExpToDrop();
        EventManager.giveAdjustedXP(player, experience);
        event.setExpToDrop(0);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityXP(EntityDeathEvent event) {
        LivingEntity dead = event.getEntity();
        Player killer = dead.getKiller();
        if (killer == null) return;
        if(!PermissionChecker.hasDrop2InvEnabled(killer)) return;
        int experience = event.getDroppedExp();
        EventManager.giveAdjustedXP(killer, experience);
        event.setDroppedExp(0);
    }

}
