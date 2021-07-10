package de.jeff_media.drop2inventory.hooks;


import com.bgsoftware.wildchests.api.WildChestsAPI;
import com.bgsoftware.wildchests.api.objects.chests.StorageChest;
import de.jeff_media.drop2inventory.Main;
import de.jeff_media.drop2inventory.utils.Utils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigInteger;

public class WildChestsHook implements Listener {

    private final Main main = Main.getInstance();

    private void dropWhenAllowed(BlockBreakEvent event) {

        if(!main.enabled(event.getPlayer())) return;

        StorageChest storageChest = WildChestsAPI.getStorageChest(event.getBlock().getLocation());
        if(storageChest == null) return;

        BigInteger amount = storageChest.getAmount();
        if(amount.intValue() == 0) return;

        ItemStack stack = storageChest.getItemStack();
        stack.setAmount(amount.intValue());
        storageChest.setAmount(BigInteger.ZERO);

        new BukkitRunnable() {
            @Override
            public void run() {
                storageChest.setAmount(amount);
                if(WildChestsAPI.getStorageChest(event.getBlock().getLocation()) == null) {
                    //System.out.println("Now trying to collect");
                    Utils.addOrDrop(stack, event.getPlayer(), event.getBlock().getLocation());
                    storageChest.setAmount(BigInteger.ZERO);
                } else {
                    //System.out.println("There is no storage chest");
                }
            }
        }.runTaskLater(main, 1L);


    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
    public void onStorageChestBreakLowest(BlockBreakEvent event) {
        dropWhenAllowed(event);
    }


}
