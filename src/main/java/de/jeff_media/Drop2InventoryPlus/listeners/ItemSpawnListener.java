package de.jeff_media.Drop2InventoryPlus.listeners;

import de.jeff_media.Drop2InventoryPlus.Config;
import de.jeff_media.Drop2InventoryPlus.Main;
import de.jeff_media.Drop2InventoryPlus.PlayerSetting;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
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
    public void onItemDrop(PlayerDropItemEvent playerDropItemEvent) {
        main.debug("###PlayerDropItemEvent");
        drops.add(playerDropItemEvent.getItemDrop().getUniqueId());
    }

    @EventHandler(priority= EventPriority.HIGHEST)
    public void onItemSpawn(ItemSpawnEvent itemSpawnEvent) {
        main.debug("###ItemSpawnEvent");
        main.debug(itemSpawnEvent.getEntity().getItemStack().toString());

        if(drops.contains(itemSpawnEvent.getEntity().getUniqueId())) {
            drops.remove(itemSpawnEvent.getEntity().getUniqueId());
            return;
        }

        if(main.isWorldDisabled(itemSpawnEvent.getLocation().getWorld().getName())) {
            return;
        }

        if(!main.getConfig().getBoolean(Config.DETECT_LEGACY_DROPS)) {
            main.debug("detect-legacy-drops = false");
            return;
        }
        main.debug("detecting legacy drop...");
        if(itemSpawnEvent.getEntity() == null) return;
        if(itemSpawnEvent.getEntity().getItemStack()==null) return;
        ItemStack is = itemSpawnEvent.getEntity().getItemStack();
        if(is.getType() == Material.AIR) return;
        if(is.getAmount() == 0) return;

        Player player;
        player = getNearestPlayer(itemSpawnEvent.getLocation());

        if(player==null) {
            main.debug("R: No player nearby.");
            return;
        }
        main.debug("Nearest player: "+player.getName());

        if(isInvFull(player)) {
            main.debug("Skipping collection because inv is full");
            main.messages.sendActionBarMessage(player, main.messages.MSG_INVENTORY_FULL);
            return;
        }

        // Fix for /reload
        main.registerPlayer(player);

        if (player.getGameMode() == GameMode.CREATIVE) {
            main.debug("R: Creative");
            return;
        }

        // disabled block?
        if (!main.utils.isBlockEnabled(is.getType())) {
            main.debug("R: Block disabled");
            return;
        }

        if (!main.getConfig().getBoolean(Config.COLLECT_BLOCK_DROPS)) {
            main.debug("R: Collect Block Drops disabled");
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

        if(main.getConfig().getBoolean(Config.IGNORE_ITEMS_FROM_DISPENSERS)) {
            if(isNearbyDispenser(itemSpawnEvent.getEntity())) {
                main.debug("Ignoring this item because nearby dispenser was found.");
                return;
            }
        }

        if(main.getConfig().getBoolean(Config.IGNORE_ITEMS_ON_HOPPERS)) {
            if(isAboveHopper(itemSpawnEvent.getLocation(),is)) {
                main.debug("Ignoring this item because nearby hopper was found.");
                return;
            }
        }

        main.debug("Player "+player.getName()+" collected legacy drop "+is);

        main.utils.addOrDrop(is,player,itemSpawnEvent.getLocation());
        itemSpawnEvent.getEntity().remove();
    }

    private boolean isAboveHopper(Location location, ItemStack itemStack) {

        main.debug("Checking if "+itemStack.toString()+" is above a hopper at "+location.toString());

        int vRange = main.getConfig().getInt(Config.IGNORE_ITEMS_ON_HOPPERS_VERTICAL_RANGE);
        int hRange = main.getConfig().getInt(Config.IGNORE_ITEMS_ON_HOPPERS_HORIZONTAL_RANGE);

        for(int y = 0; y <= vRange; y++) {
            for(int x = -hRange; x <= hRange; x++) {
                for(int z = -hRange; z <= hRange; z++) {
                    Block current = location.getBlock().getRelative(x, -y, z);
                    //main.debug("  Hopper Check: " + current.getType() + " @ " + current.getLocation());
                    if (current.getType() == Material.HOPPER) {
                        main.debug("ItemSpawn above Hopper detected");
                        return true;
                    }
                }
            }
        }
        main.debug("No hopper nearby.");
        return false;
    }

    private boolean isDispenserOrSimilar(Block block) {
        return block.getType()==Material.DISPENSER || block.getType()==Material.DROPPER;
    }

    private boolean isNearbyDispenser(Item item) {
        Location location = item.getLocation();
        for(BlockFace face : BlockFace.values()) {
            Block current = location.getBlock().getRelative(face);
            if(isDispenserOrSimilar(current)) {
                return true;
            }
        }
        return false;
    }

    private boolean isInvFull(Player player) {
        for(ItemStack i : player.getInventory().getStorageContents()) {
            if(i == null || i.getAmount()==0 || i.getType()==Material.AIR) return false;
        }
        return true;
    }

    @Nullable
    private Player getNearestPlayer(Location location) {

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
