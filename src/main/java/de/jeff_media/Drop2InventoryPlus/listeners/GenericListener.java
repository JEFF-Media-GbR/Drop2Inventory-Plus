package de.jeff_media.Drop2InventoryPlus.listeners;

import de.jeff_media.Drop2InventoryPlus.*;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GenericListener implements org.bukkit.event.Listener {

    private final Main main;
    Random random = new Random();
    boolean onlyDamaged;
    PlantUtils plantUtils = new PlantUtils();

    public GenericListener(Main main) {
        this.main = main;
        boolean onlyDamaged = main.mcVersion >= 16 ? true : false;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        main.debug("###PlayerJoinEvent");
        main.registerPlayer(event.getPlayer());


    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        main.debug("###PlayerQuitEvent");
        main.unregisterPlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(EntityDeathEvent event) {
        main.debug("###EntityDeathEvent");

        LivingEntity victim = event.getEntity();
        if (victim.getKiller() == null) {
            main.debug("Return: victim.getKiller = null");
            return;
        }

        if(main.isWorldDisabled(victim.getWorld().getName())) {
            return;
        }

        if(!(victim.getKiller() instanceof Player)) {
            main.debug("Return: victim.getKiller ! instanceof player");
            return;
        }

        Player killer = victim.getKiller();

        if (!killer.hasPermission("drop2inventory.use")) {
            main.debug("Return: victim.getKiller ! permission drop2inventory.use");
            return;
        }

        // Fix for /reload
        main.registerPlayer(victim.getKiller());

        if (!main.enabled(killer)) {
            main.debug("victim.getKiller ! drop2Inv enabled");
            return;
        }

        // Mobs drop stuff in Creative mode
        //if (victim.getKiller().getGameMode() == GameMode.CREATIVE) {
        //    return;
        //}

        if (!main.utils.isMobEnabled(victim)) {
            main.debug("not enabled for victim type "+victim.getType().name());
            return;
        }


        if (main.getConfig().getBoolean(Config.COLLECT_MOB_EXP)) {
            int exp = event.getDroppedExp();

            if(MendingUtils.hasMending(killer.getInventory().getItemInMainHand(),false)) {
                exp = main.mendingUtils.tryMending(killer.getInventory(), exp, onlyDamaged);
            }

            event.setDroppedExp(0);
            victim.getKiller().giveExp(exp);
        }
        if (!main.getConfig().getBoolean(Config.COLLECT_MOB_DROPS)) {
            return;
        }

        //victim.getKiller().sendMessage("You have killed victim "+victim.getName());

        List<ItemStack> drops = event.getDrops();
        main.debug("Dropping contents for victim kill to player inv");
        main.utils.addOrDrop(drops.toArray(new ItemStack[0]),victim.getKiller(),victim.getLocation());
        event.getDrops().clear();
    }

    @EventHandler(priority = EventPriority.MONITOR) // Monitor because some plugins like BannerBoard are too stupid to listen on LOWEST when they only want to cancel events regarding their own stuff...
    public void onItemFrameRemoveItem(EntityDamageByEntityEvent event) {
        main.debug("###EntityDamageByEntityEvent");
        if(!(event.getDamager() instanceof Player)) return;
        Player p = (Player) event.getDamager();
        if(event.isCancelled()) {
            main.debug("EntityDamageByEntityEvent is cancelled");
            return;
        }
        main.debug("EntityDamageByEntityEvent is NOT cancelled");
        if(!isDrop2InvEnabled(p, main.getPlayerSetting(p))) return;

        if(main.isWorldDisabled(p.getWorld().getName())) {
            return;
        }

        if(event.getEntity() instanceof ItemFrame) {
            ItemFrame frame = (ItemFrame) event.getEntity();
            ItemStack content = frame.getItem();
            if(content != null && content.getType()!=Material.AIR) {
                main.debug("The frame contained "+content.toString());
                main.utils.addOrDrop(content,p,frame.getLocation());
            } else {
                return;
            }
            frame.setItem(null);
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR) // Monitor because some plugins like BannerBoard are too stupid to listen on LOWEST when they only want to cancel events regarding their own stuff...
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        main.debug("###HangingBreakByEntityEvent");
        if(!(event.getRemover() instanceof Player)) {
            return;
        }
        Player p = (Player) event.getRemover();
        Location dropLocation = event.getEntity().getLocation();
        if(main.isWorldDisabled(p.getName())) {
            return;
        }
        if(event.isCancelled()) return;
        if(!isDrop2InvEnabled(p, main.getPlayerSetting(p))) return;
        main.debug("Player removed a Hanging");
        if(event.getEntity() instanceof ItemFrame) {
            main.debug("It was an Item frame.");
            ItemFrame frame = (ItemFrame) event.getEntity();
            ItemStack content = frame.getItem();
            if(content != null) {
                main.debug("The frame contained "+content.toString());
                main.utils.addOrDrop(content,p,dropLocation);
            }
            main.utils.addOrDrop(new ItemStack(Material.ITEM_FRAME),p,dropLocation);
            event.getEntity().remove();
            event.setCancelled(true);
        }

        if(event.getEntity() instanceof Painting) {
            main.utils.addOrDrop(new ItemStack(Material.PAINTING),p,dropLocation);
            event.getEntity().remove();
        }

    }




    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        main.debug("###BlockBreakEvent");
        //System.out.println("BlockBreakEvent "+event.getBlock().getType().name());


        // TODO: Drop shulker box to inv but keep contents
		/*if (event.getBlock().getType().name().toLowerCase().endsWith("shulker_box")) {
			return;
		}*/

        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();



        // disabled block?
        if (!main.utils.isBlockEnabled(event.getBlock().getType())) {
            return;
        }

        if(main.isWorldDisabled(player.getWorld().getName())) {
            return;
        }






        PlayerSetting setting = main.perPlayerSettings.get(player.getUniqueId().toString());

        if (!isDrop2InvEnabled(player, setting)) return;

        if (main.enabled(player) && main.getConfig().getBoolean(Config.COLLECT_BLOCK_EXP)) {
            int experience = event.getExpToDrop();
            if(MendingUtils.hasMending(event.getPlayer().getInventory().getItemInMainHand(),false)) {
                experience = main.mendingUtils.tryMending(event.getPlayer().getInventory(), experience,onlyDamaged);
            }
            event.getPlayer().giveExp(experience);
            event.setExpToDrop(0);
        }

        Location dropLocation = event.getBlock().getLocation();

        if(plantUtils.isPlant(event.getBlock())) {
            event.setDropItems(false);
            ArrayList<Block> plant = PlantUtils.getPlant(event.getBlock());
            int extraAmount = plant.size();
            ItemStack plantItems = new ItemStack(PlantUtils.getPlantDrop(event.getBlock().getType()), extraAmount);
            main.utils.addOrDrop(plantItems,event.getPlayer(),dropLocation);
            PlantUtils.destroyPlant(plant);
        } else if(PlantUtils.isChorusTree(event.getBlock())) {
            // Note:
            // Chorus flower only drop themselves when broken directly,
            // but not when they drop because the chorus plant is broken
            ArrayList<Block> chorusTree = new ArrayList<Block>();
            event.setDropItems(false);
             PlantUtils.getChorusTree(event.getBlock(),chorusTree);
            int extraAmountChorusPlant = PlantUtils.getAmountInList(chorusTree,Material.CHORUS_PLANT);
            int extraAmountChorusFruit = 0;

            for(int i = 0; i < extraAmountChorusPlant; i++) {
                if(random.nextInt(100)>=50) {
                    extraAmountChorusFruit++;
                }
            }

            ItemStack flowerDrops = new ItemStack(Material.CHORUS_FRUIT, extraAmountChorusFruit);
            main.utils.addOrDrop(flowerDrops,event.getPlayer(),dropLocation);
            PlantUtils.destroyPlant(chorusTree);
        } else if(event.getBlock().getState() instanceof Furnace) {

            FurnaceInventory finv = ((Furnace) event.getBlock().getState()).getInventory();

            if(finv.getFuel()!=null) {
                main.utils.addOrDrop(finv.getFuel(),event.getPlayer(),dropLocation);
                finv.setFuel(null);
            }
            if(finv.getSmelting()!=null) {
                main.utils.addOrDrop(finv.getSmelting(),event.getPlayer(),dropLocation);
                finv.setSmelting(null);
            }
            if(finv.getResult()!=null) {
                main.utils.addOrDrop(finv.getResult(),event.getPlayer(),dropLocation);
                finv.setResult(null);
            }


        }



        //plugin.dropHandler.drop2inventory(event);
    }

    private boolean isDrop2InvEnabled(Player player, PlayerSetting setting) {

        if(main.getConfig().getBoolean(Config.ALWAYS_ENABLED)) return true;

        if (!player.hasPermission("drop2inventory.use")) {
            return false;
        }

        // Fix for /reload
        main.registerPlayer(player);

        if (player.getGameMode() == GameMode.CREATIVE) {
            return false;
        }



        if (!main.getConfig().getBoolean(Config.COLLECT_BLOCK_DROPS)) {
            return false;
        }
        if (!main.enabled(player)) {
            if (!setting.hasSeenMessage) {
                setting.hasSeenMessage = true;
                if (main.getConfig().getBoolean(Config.SHOW_MESSAGE_WHEN_BREAKING_BLOCK)) {
                    player.sendMessage(main.messages.MSG_HINT_ENABLE);
                }
            }
            return false;
        } else {
            if (!setting.hasSeenMessage) {
                setting.hasSeenMessage = true;
                if (main.getConfig().getBoolean(Config.SHOW_MESSAGE_WHEN_BREAKING_BLOCK_AND_COLLECTION_IS_ENABLED)) {
                    player.sendMessage(main.messages.MSG_HINT_DISABLE);
                }
            }
        }
        return true;
    }

}
