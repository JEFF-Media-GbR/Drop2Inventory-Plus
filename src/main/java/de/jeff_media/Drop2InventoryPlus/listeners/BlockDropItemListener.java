package de.jeff_media.Drop2InventoryPlus.listeners;

import de.jeff_media.Drop2InventoryPlus.*;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class BlockDropItemListener implements @NotNull Listener {

    final Main main;

    public BlockDropItemListener(Main main) {
        this.main = main;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockDropItemMonitor(BlockDropItemEvent event) {
        if(main.blockDropItemPrio == EventPriority.MONITOR) {
            onBlockDropItem(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockDropItemHighest(BlockDropItemEvent event) {
        if(main.blockDropItemPrio == EventPriority.HIGHEST) {
            onBlockDropItem(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockDropItemHigh(BlockDropItemEvent event) {
        if(main.blockDropItemPrio == EventPriority.HIGH) {
            onBlockDropItem(event);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockDropItemNormal(BlockDropItemEvent event) {
        if(main.blockDropItemPrio == EventPriority.NORMAL) {
            onBlockDropItem(event);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockDropItemLow(BlockDropItemEvent event) {
        if(main.blockDropItemPrio == EventPriority.LOW) {
            onBlockDropItem(event);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockDropItemLowest(BlockDropItemEvent event) {
        if(main.blockDropItemPrio == EventPriority.LOWEST) {
            onBlockDropItem(event);
        }
    }

    public void onBlockDropItem(BlockDropItemEvent event) {
        main.debug("###BlockDropItemEvent "+main.blockDropItemPrio.name());
        List<Item> items = event.getItems();

        if(main.debug) {
            for (Item item : event.getItems()) {
                main.debug(item.getItemStack().toString());
            }
        }

        Player player = event.getPlayer();
        World world = event.getPlayer().getLocation().getWorld();

        if (event.isCancelled()) {
            main.debug("R: cancelled");
            return;
        }

        if(main.isWorldDisabled(world.getName())) {
            main.debug("R: World "+world.getName()+" disabled");
            return;
        }

        if (!player.hasPermission(Permissions.ALLOW_USE)) {
            main.debug("R: No Permission");
            return;
        }

        if(main.getConfig().getBoolean(Config.PERMISSIONS_PER_TOOL,false) && !(Utils.hasPermissionForThisTool(player.getInventory().getItemInMainHand().getType(),player))) {
            main.debug("R: No Permission for tool");
            return;
        }

        main.registerPlayer(event.getPlayer());

        if (player.getGameMode() == GameMode.CREATIVE) {
            main.debug("R: Creative");
            return;
        }

        // disabled block?
        if (!main.utils.isBlockEnabled(event.getBlockState().getType())) {
            main.debug("R: Block disabled");
            return;
        }

        if (!main.getConfig().getBoolean(Config.COLLECT_BLOCK_DROPS)) {
            main.debug("R: Block Drop Collection disabled");
            return;
        }


        PlayerSetting setting = main.perPlayerSettings.get(player.getUniqueId().toString());

        if (!main.enabled(player)) {
            if (!setting.hasSeenMessage) {
                setting.hasSeenMessage = true;
                if (main.getConfig().getBoolean(Config.SHOW_MESSAGE_WHEN_BREAKING_BLOCK)) {
                    Messages.sendMessage(player,main.messages.MSG_HINT_ENABLE);
                }
            }
            main.debug("R: Player has Drop2Inv disabled");
            return;
        }
        if (!setting.hasSeenMessage) {
            setting.hasSeenMessage = true;
            if (main.getConfig().getBoolean(Config.SHOW_MESSAGE_WHEN_BREAKING_BLOCK_AND_COLLECTION_IS_ENABLED)) {
                Messages.sendMessage(player,main.messages.MSG_HINT_DISABLE);
            }
        }

        main.debug("Collecting drops");

        for(Item item : items) {
            main.debug("Drop detected: "+item.getItemStack());
            main.utils.addOrDrop(item.getItemStack(),event.getPlayer(),event.getBlock().getLocation());
        }

        event.setCancelled(true);
    }
}
