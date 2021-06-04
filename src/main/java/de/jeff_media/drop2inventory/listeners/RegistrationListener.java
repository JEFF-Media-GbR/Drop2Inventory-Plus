package de.jeff_media.drop2inventory.listeners;

import de.jeff_media.drop2inventory.Main;
import de.jeff_media.drop2inventory.config.Config;
import de.jeff_media.drop2inventory.data.DropSubject;
import de.jeff_media.drop2inventory.handlers.DropOwnerManager;
import de.jeff_media.drop2inventory.handlers.PermissionChecker;
import de.jeff_media.drop2inventory.utils.PDCUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class RegistrationListener implements Listener {

    private final Main main = Main.getInstance();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void registerDropOwner(HangingBreakByEntityEvent event) {
        Entity remover = event.getRemover();
        if (!(remover instanceof Player)) return;
        Player player = (Player) remover;
        if (!PermissionChecker.isAllowed(player, new DropSubject(event.getEntity()))) {
            return;
        }
        DropOwnerManager.register(player, event.getEntity().getLocation(), null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void registerDropOwner(VehicleDestroyEvent event) {
        Entity attacker = event.getAttacker();
        if (!(attacker instanceof Player)) return;
        Player player = (Player) attacker;
        if (!PermissionChecker.isAllowed(player, new DropSubject(event.getVehicle()))) {
            return;
        }
        DropOwnerManager.register(player, event.getVehicle().getLocation(), null);
    }

    /*
    This is mainly used for item frames and their contents
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void registerDropOwner(EntityDamageByEntityEvent event) {
        if (!(event instanceof Hanging) && event.getEntityType() != EntityType.ITEM_FRAME) return;
        if (event.getDamager().getType() != EntityType.PLAYER) return;
        Player player = (Player) event.getDamager();
        if (!PermissionChecker.isAllowed(player, new DropSubject(event.getEntity()))) {
            return;
        }
        DropOwnerManager.register(player, event.getEntity().getLocation(), null);
    }

    /**
     * Registers drop ownership for block drops
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    // TODO: ignoreCancelled true or false? It's currently false to detect custom blockbreaks
    // TODO: Currently using LOWEST to detect custom drops that are done within an event
    public void registerDropOwner(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location location = block.getLocation();
        if (!PermissionChecker.isAllowed(player, new DropSubject(event.getBlock()))) {
            return;
        }
        DropOwnerManager.register(player, location, block);
    }

    /**
     * Registers drop ownership for entity drops
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void registerDropOwner(EntityDeathEvent event) {
        LivingEntity dead = event.getEntity();
        Location location = dead.getLocation();
        Player killer = dead.getKiller();
        if (killer == null) return;
        if (!PermissionChecker.isAllowed(killer, new DropSubject(dead))) return;
        DropOwnerManager.register(killer, location, null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDispense(BlockDispenseEvent event) {
        if(!main.getConfig().getBoolean(Config.IGNORE_ITEMS_FROM_DISPENSERS)) return;
        ItemStack item = event.getItem();
        PDCUtils.add(item, Main.IGNORED_DROP_TAG, PersistentDataType.BYTE, (byte) 1);
    }

}
