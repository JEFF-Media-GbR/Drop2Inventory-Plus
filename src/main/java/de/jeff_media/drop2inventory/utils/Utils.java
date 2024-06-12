package de.jeff_media.drop2inventory.utils;

import com.allatori.annotations.DoNotRename;
import de.jeff_media.drop2inventory.Main;
import de.jeff_media.drop2inventory.config.Config;
import de.jeff_media.drop2inventory.config.Permissions;
import com.jeff_media.jefflib.data.SoundData;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@DoNotRename
public class Utils {

    private static SoundData inventoryFullSound = new SoundData(Sound.ENTITY_ITEM_PICKUP.getKey().toString(), 1, 1, 0.2f, SoundCategory.BLOCKS);
    final Main main;

    public Utils(Main main) {
        this.main = main;
    }

    public static void loadSounds() {
        inventoryFullSound = SoundData.fromConfigurationSection(Main.getInstance().getConfig(), "sound-inv-full-");
    }

    @DoNotRename
    public static void addOrDrop(ItemStack item, Player player, @Nullable Location dropLocation) {
        //main.debug("addOrDrop: " + item.toString() + " -> " + player.getName());

        ItemStack[] items = new ItemStack[1];
        items[0] = item;
        addOrDrop(items, player, dropLocation);
    }

    public enum MessageType {
        ACTIONBAR, TITLE;

        public static MessageType fromString(String str) {
            if(str == null) return ACTIONBAR;
            switch (str.toLowerCase()) {
                case "actionbar":
                case "action_bar":
                case "true":
                    return ACTIONBAR;
                case "title":
                    return TITLE;
                default:
                    return null;
            }
        }
    }

    @DoNotRename
    public static void addOrDrop(ItemStack[] items, Player player, @Nullable Location dropLocation) {
        Main main = Main.getInstance();
        if (main.isDebug()) main.debug("addOrDrop[] " + Arrays.toString(items) + " -> " + player);
        if (main.getConfig().getBoolean(Config.AVOID_HOTBAR)) {
            if (main.isDebug()) main.debug("  avoid-hotbar enabled");
            main.hotbarStuffer.stuffHotbar(player.getInventory());
        } else {
            if (main.isDebug()) main.debug("  avoid-hotbar disabled");
        }
        for (ItemStack item : items) {
            int pickedUpAmount = item.getAmount();
            if (main.isDebug()) main.debug(" addOrDrop#2");
            if (item == null) continue;
            if (item.getType() == Material.AIR) continue;

            if(main.autoSmelter.hasEnabled(player)) {
                if (main.isDebug()) main.debug("Auto smelting " + item.getType().name());
                ItemStack smeltResult = main.autoSmelter.transform(player, item);
                if(smeltResult != null) {
                    if (main.isDebug()) main.debug("  smelted to " + smeltResult.getType().name());
                    item = smeltResult;
                }
            }

            // Try offHand first (md_5 doesnt want that -> https://hub.spigotmc.org/jira/browse/SPIGOT-2436)
            ItemStack offHandItem = player.getInventory().getItemInOffHand();
            if (offHandItem != null && offHandItem.getType() == item.getType()) {
                if (offHandItem.isSimilar(item)) {
                    int spaceLeftInOffHand = offHandItem.getMaxStackSize() - offHandItem.getAmount();
                    if (offHandItem.getAmount() < offHandItem.getMaxStackSize()) {
                        // Enough space left in offhand
                        if (item.getAmount() <= spaceLeftInOffHand) {
                            offHandItem.setAmount(offHandItem.getAmount() + item.getAmount());
                            continue;
                        }
                        int unstorable = item.getAmount() - spaceLeftInOffHand;
                        offHandItem.setAmount(offHandItem.getMaxStackSize());
                        item.setAmount(unstorable);
                    }
                }
            }
            // End offHand first
            HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(item);
            for (ItemStack leftover : leftovers.values()) {
                pickedUpAmount -= leftover.getAmount();
            }
            boolean inventoryFull = false;
            for (ItemStack leftover : leftovers.values()) {
                PDCUtils.add(leftover, Main.IGNORED_DROP_TAG, PersistentDataType.BYTE, (byte) 1);
                if (main.getConfig().getBoolean(Config.DROP_WHEN_INV_FULL)) {
                    player.getWorld().dropItem(dropLocation == null ? player.getLocation() : dropLocation, leftover);
                }
                if (main.isDebug()) main.debug("Inventory full, dropping to world");
                inventoryFull = true;
            }
            MessageType type = MessageType.fromString(main.getConfig().getString(main.getMessages().MSG_INVENTORY_FULL, "actionbar"));
            if (inventoryFull && type != null) {
                switch (type) {
                    case ACTIONBAR:
                        main.getMessages().sendActionBarMessage(player, main.getMessages().MSG_INVENTORY_FULL);
                        break;
                    case TITLE:
                        String[] split = main.getMessages().MSG_INVENTORY_FULL.split("\n");
                        String line1 = split[0];
                        String line2 = split.length > 1 ? split[1] : "";
                        player.sendTitle(line1, line2, 10, 70, 20);
                        break;
                }
            }
            if (inventoryFull && main.getConfig().getBoolean(Config.PLAY_SOUND_WHEN_INVENTORY_IS_FULL)) {
                if (!SoundUtils.getCooldown().hasCooldown(player)) {
                    SoundUtils.getCooldown().setCooldown(player, SoundUtils.SOUND_COOLDOWN, TimeUnit.MILLISECONDS); // TODO change to 100 ms
                    if (main.getConfig().getBoolean(Config.PLAY_SOUND_WHEN_INVENTORY_IS_FULL_GLOBAL)) {
                        inventoryFullSound.playToWorld(player.getLocation());
                    } else {
                        inventoryFullSound.playToPlayer(player);
                    }
                }
            }

            if (main.ingotCondenser.hasEnabled(player)) {
                if (main.isDebug()) main.debug("Auto condensing " + item.getType().name());
                main.ingotCondenser.condense(player.getInventory(), item.getType());
            }

            int statistic = player.getStatistic(Statistic.PICKUP, item.getType());
            int newStatistic = statistic + pickedUpAmount;
            if (newStatistic > statistic && newStatistic > 0) {
                player.setStatistic(Statistic.PICKUP, item.getType(), statistic + pickedUpAmount);
            }
            if(inventoryFull) {
                main.getInvFullCommands().run(player);
            }
        }
        if (main.getConfig().getBoolean(Config.AVOID_HOTBAR)) {
            main.hotbarStuffer.unstuffHotbar(player.getInventory());
        }
        main.getSoundUtils().playPickupSound(player);
    }

    public static boolean hasPermissionForThisTool(@Nullable Material mat, Player p) {
        String matt = mat.name().toLowerCase();
        if (matt.contains("_pickaxe")) {
            return p.hasPermission(Permissions.ALLOW_TOOL_PICKAXE);
        }
        if (matt.contains("_axe")) {
            return p.hasPermission(Permissions.ALLOW_TOOL_AXE);
        }
        if (matt.contains("_hoe")) {
            return p.hasPermission(Permissions.ALLOW_TOOL_HOE);
        }
        if (matt.contains("_sword")) {
            return p.hasPermission(Permissions.ALLOW_TOOL_SWORD);
        }
        if (matt.contains("_shovel")) {
            return p.hasPermission(Permissions.ALLOW_TOOL_SHOVEL);
        } else return p.hasPermission(Permissions.ALLOW_TOOL_HAND);
    }

    public static @Nullable Player getNearestPlayer(Location location) {
        World world = location.getWorld();
        double bestDistance = Double.MAX_VALUE;
        Player nearestPlayer = null;

        for (Player player : world.getPlayers()) {
            double distance = player.getLocation().distanceSquared(location);
            if (distance < bestDistance) {
                bestDistance = distance;
                nearestPlayer = player;
            }
        }

        return nearestPlayer;
    }

    public boolean isBlockEnabled(Material mat) {
        if (main.isDebug()) main.debug("Checking if " + mat.name() + " is enabled...");
        if (main.isDebug()) main.debug("Whitelist: " + main.blocksIsWhitelist);
        if (!main.blocksIsWhitelist) {
            if (main.disabledBlocks.contains(mat)) {
                if (main.isDebug()) main.debug("Its disabled on the blacklist!");
                return false;
            }
            if (main.isDebug()) main.debug("Its enabled!");
            return true;
        }
        if (main.disabledBlocks.contains(mat)) {
            if (main.isDebug()) main.debug("Its enabled on the whitelist!");
            return true;
        }
        if (main.isDebug()) main.debug("Its not on the whitelist!");
        if (main.isDebug()) main.debug("BTW the whitelist contains " + main.disabledBlocks.size() + " blocks");
        return false;
    }

    public boolean isMobEnabled(Entity mob) {
        if (!main.isMobsIsWhitelist()) {
            return !main.disabledMobs.contains(mob.getType().name().toLowerCase());
        }
        return main.disabledMobs.contains(mob.getType().name().toLowerCase());
    }

    public ItemStack getAutoSmeltedIfApplicable(ItemStack item, Player player) {
        boolean forceAutoSmelt = main.getConfig().getBoolean(Config.FORCE_AUTO_SMELT);
        boolean hasPermission = player.hasPermission(Permissions.ALLOW_AUTO_SMELT);

        // TODO
        return null;
    }


}
