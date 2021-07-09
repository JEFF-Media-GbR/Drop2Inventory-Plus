package de.jeff_media.drop2inventory.hooks;

import de.jeff_media.drop2inventory.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

public class PluginHooks {

    private final Main main = Main.getInstance();

    private final IPluginHook[] hooks = {
            new EliteMobsHook()
    };

    public PluginHooks() {
        if(Bukkit.getPluginManager().getPlugin("WildChests") != null) {
            Bukkit.getPluginManager().registerEvents(new WildChestsHook(), main);
        }
    }


    public boolean mayPickUp(Item item, Player player) {
        for(IPluginHook hook : hooks) {
            if(!hook.mayPickUp(item, player)) {
                if(main.isDebug()) main.debug("Plugin hook prevents pickup: " + hook.getClass().getName());
                return false;
            }
        }
        return true;
    }

}
