package de.jeff_media.drop2inventory.listeners;

import de.jeff_media.daddy.Chicken;
import de.jeff_media.drop2inventory.Main;
import de.jeff_media.drop2inventory.config.Config;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class MiscListener implements Listener {

    private final Main main = Main.getInstance();

    {
        Chicken.wing(main);
    }

    /**
     * Resets the "has seen message" tag on join
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();
        if(!main.getConfig().getBoolean(Config.SAVE_PLAYERDATA, true)) {
            resetPlayerData(player);
        }

        if (main.getConfig().getBoolean(Config.SHOW_MESSAGE_AGAIN_AFTER_LOGOUT)) {
            PersistentDataContainer pdc = player.getPersistentDataContainer();
            if (pdc.has(Main.HAS_SEEN_MESSAGE_TAG, PersistentDataType.BYTE)) {
                pdc.remove(Main.HAS_SEEN_MESSAGE_TAG);
            }
        }

        main.applyEnabledByDefault(player);
        main.applyAutoCondenseEnabledByDefault(player);
    }

    private void resetPlayerData(Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        for(NamespacedKey key : pdc.getKeys()) {
            if(key.getNamespace().equals(Main.HAS_SEEN_MESSAGE_TAG.getNamespace())) {
                pdc.remove(key);
            }
        }
    }

}
