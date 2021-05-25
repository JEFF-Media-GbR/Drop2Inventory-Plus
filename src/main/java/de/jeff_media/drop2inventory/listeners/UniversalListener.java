package de.jeff_media.drop2inventory.listeners;

import de.jeff_media.drop2inventory.Main;
import de.jeff_media.drop2inventory.config.Config;
import de.jeff_media.drop2inventory.data.DropSubject;
import de.jeff_media.drop2inventory.handlers.DropOwnerManager;
import de.jeff_media.drop2inventory.handlers.EventManager;
import de.jeff_media.drop2inventory.handlers.PermissionChecker;
import de.jeff_media.drop2inventory.utils.Utils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

/**
 * Registers events that will probably lead to drops, i.e. EntityDeathEvent and BlockBreakEvent, so that those drops
 * can be given to their rightful owner when the ItemSpawnEvent occurs, which doesn't include the cause of the drop
 */
public class UniversalListener implements Listener {

    private final Main main = Main.getInstance();

    /**
     * Resets the "has seen message" tag on join
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if(!main.getConfig().getBoolean(Config.SHOW_MESSAGE_AGAIN_AFTER_LOGOUT)) return;

        Player player = event.getPlayer();

        PersistentDataContainer pdc = player.getPersistentDataContainer();
        if(pdc.has(Main.HAS_SEEN_MESSAGE_TAG, PersistentDataType.BYTE)) {
            pdc.remove(Main.HAS_SEEN_MESSAGE_TAG);
        }
    }

    /**
     * Cancels the actual Item Spawn and gives it to the player that owns this drop
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void collectDrops(ItemSpawnEvent event) {
        if(main.debug) main.debug("ItemSpawnEvent: " + event.getEntity().getItemStack() + " @ " + event.getEntity().getLocation());
        Item item = event.getEntity();
        ItemStack itemStack = item.getItemStack();
        Location location = event.getLocation();
        Player player = DropOwnerManager.getDropOwner(location);
        if (player == null) {
            if(main.debug) main.debug("  Don't pick up: no player found");
            return;
        }
        if (!EventManager.mayPickUp(player, item)) {
            if(main.debug) main.debug("  Don't pick up: EntityPickUpItemEvent cancelled");
            return;
        }
        event.setCancelled(true);
        Utils.addOrDrop(itemStack, player, location);
    }

    @EventHandler
    public void debug(EntitySpawnEvent event) {
        if (event.getEntityType() == EntityType.FALLING_BLOCK) {
            //System.out.println("FallingBlockSpawn");
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
        int experience = event.getExpToDrop();
        EventManager.giveAdjustedXP(player, experience);
        event.setExpToDrop(0);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityXP(EntityDeathEvent event) {
        LivingEntity dead = event.getEntity();
        Player killer = dead.getKiller();
        if (killer == null) return;
        int experience = event.getDroppedExp();
        EventManager.giveAdjustedXP(killer, experience);
        event.setDroppedExp(0);
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
        if(main.debug) main.debug("BlockBreak, trying to register DropOwner " + player.getName() + " for Block " + block);
        if (!PermissionChecker.isAllowed(player, new DropSubject(event.getBlock()))) {
            if(main.debug) main.debug("  Cancelling: player is not allowed to be registered as DropOwner");
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
}
