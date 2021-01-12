package de.jeff_media.Drop2InventoryPlus;

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


public class DropListener implements @NotNull Listener {

    Main main;

    DropListener(Main main) {
        this.main = main;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemDrop(BlockDropItemEvent event) {
        main.debug("");
        main.debug("###BlockDropItemEvent");
        List<Item> items = event.getItems();
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

        if (!player.hasPermission("drop2inventory.use")) {
            main.debug("R: No Permission");
            return;
        }

        if(main.getConfig().getBoolean("permissions-per-tool",false) && !(Utils.hasPermissionForThisTool(player.getInventory().getItemInMainHand().getType(),player))) {
            main.debug("R: No Permission for tool");
            return;
        }

        main.registerPlayer(event.getPlayer());

        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        // disabled block?
        if (!main.utils.isBlockEnabled(event.getBlockState().getType())) {
            return;
        }

        if (!main.getConfig().getBoolean("collect-block-drops")) {
            return;
        }


        PlayerSetting setting = main.perPlayerSettings.get(player.getUniqueId().toString());

        if (!main.enabled(player)) {
            if (!setting.hasSeenMessage) {
                setting.hasSeenMessage = true;
                if (main.getConfig().getBoolean("show-message-when-breaking-block")) {
                    player.sendMessage(main.messages.MSG_COMMANDMESSAGE);
                }
            }
            return;
        }
        if (!setting.hasSeenMessage) {
            setting.hasSeenMessage = true;
            if (main.getConfig().getBoolean("show-message-when-breaking-block-and-collection-is-enabled")) {
                player.sendMessage(main.messages.MSG_COMMANDMESSAGE2);
            }
        }

        for(Item item : items) {
            main.debug("Drop detected: "+item.getItemStack());
            main.utils.addOrDrop(item.getItemStack(),event.getPlayer());
        }

        event.setCancelled(true);
    }
}
