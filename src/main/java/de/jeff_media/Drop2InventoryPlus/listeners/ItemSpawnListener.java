package de.jeff_media.Drop2InventoryPlus.listeners;

import de.jeff_media.Drop2InventoryPlus.Config;
import de.jeff_media.Drop2InventoryPlus.Main;
import de.jeff_media.Drop2InventoryPlus.PlayerSetting;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.UUID;

public class ItemSpawnListener implements @NotNull Listener {

    private final Main main;
    public final ArrayList<UUID> drops;

    public ItemSpawnListener(Main main) {
        this.main=main;
        drops = new ArrayList<>();
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onItemDrop(PlayerDropItemEvent e) {
        main.debug("###PlayerDropItemEvent");
        drops.add(e.getItemDrop().getUniqueId());
    }

    @EventHandler(priority= EventPriority.HIGHEST)
    public void onItemSpawn(ItemSpawnEvent e) {
        main.debug("###ItemSpawnEvent");

        if(drops.contains(e.getEntity().getUniqueId())) {
            drops.remove(e.getEntity().getUniqueId());
            return;
        }

        if(main.isWorldDisabled(e.getLocation().getWorld().getName())) {
            return;
        }

        if(!main.getConfig().getBoolean(Config.DETECT_LEGACY_DROPS)) {
            main.debug("detect-legacy-drops = false");
            return;
        }
        main.debug("detecting legacy drop...");
        if(e.getEntity() == null) return;
        if(e.getEntity().getItemStack()==null) return;
        ItemStack is = e.getEntity().getItemStack();
        if(is.getType() == Material.AIR) return;
        if(is.getAmount() == 0) return;

        // ignore-items-on-hoppers
        if(main.getConfig().getBoolean("ignore-items-on-hoppers")) {
            if(isAboveHopper(e.getLocation())) {
                return;
            }
        }

        Player player;
        player = getNearestPlayer(e.getLocation());

        if(player==null) return;
        main.debug("Nearest player: "+player.getName());

        if(isInvFull(player)) {
            main.debug("Skipping collection because inv is full");
            main.messages.sendActionBarMessage(player, main.messages.MSG_INVENTORY_FULL);
            return;
        }

        // Fix for /reload
        main.registerPlayer(player);

        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        // disabled block?
        if (!main.utils.isBlockEnabled(is.getType())) {
            return;
        }

        if (!main.getConfig().getBoolean(Config.COLLECT_BLOCK_DROPS)) {
            return;
        }


        PlayerSetting setting = main.perPlayerSettings.get(player.getUniqueId().toString());

        if (!main.enabled(player)) {
            if (!setting.hasSeenMessage) {
                setting.hasSeenMessage = true;
                if (main.getConfig().getBoolean(Config.SHOW_MESSAGE_WHEN_BREAKING_BLOCK)) {
                    player.sendMessage(main.messages.MSG_HINT_ENABLE);
                }
            }
            return;
        }
        if (!setting.hasSeenMessage) {
            setting.hasSeenMessage = true;
            if (main.getConfig().getBoolean(Config.SHOW_MESSAGE_WHEN_BREAKING_BLOCK_AND_COLLECTION_IS_ENABLED)) {
                player.sendMessage(main.messages.MSG_HINT_DISABLE);
            }
        }

        main.debug("Player "+player.getName()+" collected legacy drop "+is);

        main.utils.addOrDrop(is,player,e.getLocation());
        e.getEntity().remove();
    }

    private boolean isAboveHopper(Location location) {
        for(int i = 0; i <= 7; i++) {
            if(location.getBlock().getRelative(0,i,0).getType()==Material.HOPPER) {
                main.debug("ItemSpawn above Hopper detected");
                return true;
            }
        }
        return false;
    }

    private boolean isInvFull(Player p) {
        for(ItemStack i : p.getInventory().getStorageContents()) {
            if(i == null || i.getAmount()==0 || i.getType()==Material.AIR) return false;
        }
        return true;
    }

    @Nullable
    private Player getNearestPlayer(Location location) throws NoSuchMethodError {

        ArrayList<Player> players = new ArrayList<>();
        double detectionRange = main.getConfig().getDouble(Config.DETECT_LEGACY_DROPS_RANGE);
        for(Entity e : location.getWorld().getNearbyEntities(location, detectionRange, detectionRange, detectionRange, entity -> entity instanceof Player && !entity.isDead())) {
            players.add((Player) e);
        }

        players.sort(Comparator.comparingDouble(o -> o.getLocation().distance(location)));

        if(players.size()>0) return players.get(0);
        return null;
    }

}
