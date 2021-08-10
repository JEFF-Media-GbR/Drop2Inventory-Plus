package de.jeff_media.drop2inventory.listeners;

import de.jeff_media.drop2inventory.Main;
import de.jeff_media.drop2inventory.config.Config;
import de.jeff_media.drop2inventory.data.DropSubject;
import de.jeff_media.drop2inventory.data.WorldBoundingBox;
import de.jeff_media.drop2inventory.handlers.DropOwnerManager;
import de.jeff_media.drop2inventory.handlers.PermissionChecker;
import de.jeff_media.drop2inventory.hooks.WildChestsHook;
import de.jeff_media.drop2inventory.utils.PDCUtils;
import de.jeff_media.drop2inventory.utils.Utils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collection;

public class RegistrationListener implements Listener {

    private final Main main = Main.getInstance();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHarvestBlock(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        DropOwnerManager.registerSimple(player, event.getClickedBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHarvestEntity(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();
        DropOwnerManager.registerSimple(player, event.getRightClicked().getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void removeHanging(HangingBreakByEntityEvent event) {
        Entity remover = event.getRemover();
        if (!(remover instanceof Player)) return;
        Player player = (Player) remover;
        if (!PermissionChecker.isAllowed(player, new DropSubject(event.getEntity()))) {
            return;
        }
        DropOwnerManager.register(player, event.getEntity().getLocation(), null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void destroyVehicle(VehicleDestroyEvent event) {
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
    public void damageEntity(EntityDamageByEntityEvent event) {
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
    public void blockBreak(BlockBreakEvent event) {
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
    public void killEntity(EntityDeathEvent event) {
        LivingEntity dead = event.getEntity();
        Location location = dead.getLocation();
        Player killer = dead.getKiller();
        if (killer == null) return;
        if (!PermissionChecker.isAllowed(killer, new DropSubject(dead))) return;
        DropOwnerManager.registerSimple(killer, location);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDispense(BlockDispenseEvent event) {
        if (!main.getConfig().getBoolean(Config.IGNORE_ITEMS_FROM_DISPENSERS)) return;
        ItemStack item = event.getItem();
        PDCUtils.add(item, Main.IGNORED_DROP_TAG, PersistentDataType.BYTE, (byte) 1);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExplode(EntityExplodeEvent event) {

        onExplode(event.getLocation().getBlock(), event.blockList());
    }

    private void onExplode(Block block, Collection<Block> destroyedBlocks) {
        if(!main.getConfig().getBoolean(Config.DETECT_EXPLOSION_DROPS)) return;
        Player player = Utils.getNearestPlayer(block.getLocation());
        if (player == null) return;
        if (!PermissionChecker.isAllowed(player, new DropSubject(block))) return;

        int minX, minY, minZ, maxX, maxY, maxZ;
        minX = block.getX() - 2;
        maxX = block.getX() + 2;
        minY = block.getY() - 2;
        maxY = block.getY() + 2;
        minZ = block.getZ() - 2;
        maxZ = block.getZ() + 2;
        for (Block destroyedBlock : destroyedBlocks) {
            int x = destroyedBlock.getX();
            int y = destroyedBlock.getY();
            int z = destroyedBlock.getZ();
            minX = Math.min(x, minX);
            maxX = Math.max(x, maxX);
            minY = Math.min(y, minY);
            maxY = Math.max(y, maxY);
            minZ = Math.min(z, minZ);
            maxZ = Math.max(z, maxZ);
        }

        WorldBoundingBox boundingBox = new WorldBoundingBox(block.getWorld(), 1, minX, maxX, minY, maxY, minZ, maxZ);
        DropOwnerManager.register(player, boundingBox);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExplode(BlockExplodeEvent event) {
        onExplode(event.getBlock(), event.blockList());
    }
}
