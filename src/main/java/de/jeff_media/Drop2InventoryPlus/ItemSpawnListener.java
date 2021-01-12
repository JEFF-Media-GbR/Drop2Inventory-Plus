package de.jeff_media.Drop2InventoryPlus;

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
import java.util.Collections;
import java.util.UUID;
import java.util.function.Predicate;

public class ItemSpawnListener implements @NotNull Listener {

    Main main;
    ArrayList<UUID> drops;

    ItemSpawnListener(Main main) {
        this.main=main;
        drops = new ArrayList<UUID>();
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

        if(!main.getConfig().getBoolean("detect-legacy-drops")) {
            main.debug("detect-legacy-drops = false");
            return;
        }
        main.debug("detecting legacy drop...");
        if(e.getEntity() == null) return;
        if(e.getEntity().getItemStack()==null) return;
        ItemStack is = e.getEntity().getItemStack();
        if(is.getType() == Material.AIR) return;
        if(is.getAmount() == 0) return;
        Player p;
        p = getNearestPlayer(e.getLocation());

        if(p==null) return;
        main.debug("Nearest player: "+p.getName());

        if(isInvFull(p)) {
            main.debug("Skipping collection because inv is full");
            return;
        }

        // Fix for /reload
        main.registerPlayer(p);

        if (p.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        // disabled block?
        if (!main.utils.isBlockEnabled(is.getType())) {
            return;
        }

        if (!main.getConfig().getBoolean("collect-block-drops")) {
            return;
        }


        PlayerSetting setting = main.perPlayerSettings.get(p.getUniqueId().toString());

        if (!main.enabled(p)) {
            if (!setting.hasSeenMessage) {
                setting.hasSeenMessage = true;
                if (main.getConfig().getBoolean("show-message-when-breaking-block")) {
                    p.sendMessage(main.messages.MSG_COMMANDMESSAGE);
                }
            }
            return;
        }
        if (!setting.hasSeenMessage) {
            setting.hasSeenMessage = true;
            if (main.getConfig().getBoolean("show-message-when-breaking-block-and-collection-is-enabled")) {
                p.sendMessage(main.messages.MSG_COMMANDMESSAGE2);
            }
        }

        main.debug("Player "+p.getName()+" collected legacy drop "+is);

        main.utils.addOrDrop(is,p);
        e.getEntity().remove();
    }

    private boolean isInvFull(Player p) {
        for(ItemStack i : p.getInventory().getStorageContents()) {
            if(i == null || i.getAmount()==0 || i.getType()==Material.AIR) return false;
        }
        return true;
    }

    @Nullable
    private Player getNearestPlayer(Location location) throws NoSuchMethodError {

        ArrayList<Player> players = new ArrayList<Player>();
        for(Entity e : location.getWorld().getNearbyEntities(location, 6, 6, 6, new Predicate<Entity>() {
            @Override
            public boolean test(Entity entity) {
                return entity instanceof Player && !((Player) entity).isDead();
            }
        })) {
            players.add((Player) e);
        }

        Collections.sort(players, (o1, o2) -> {
            if(o1.getLocation().distance(location) > o2.getLocation().distance(location)) {
                return 1;
            }
            if(o1.getLocation().distance(location) < o2.getLocation().distance(location)) {
                return -1;
            }
            return 0;
        });

        if(players.size()>0) return players.get(0);
        return null;
    }

}
