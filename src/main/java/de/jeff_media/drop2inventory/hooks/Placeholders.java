package de.jeff_media.drop2inventory.hooks;

import de.jeff_media.drop2inventory.Main;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class Placeholders extends PlaceholderExpansion {

    private final Main main;

    public Placeholders(Main main) {
        this.main=main;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "drop2inventory";
    }

    @Override
    public @NotNull String getAuthor() {
        return "mfnalex";
    }

    @Override
    public @NotNull String getVersion() {
        return "GENERIC";
    }

    @Override
    public boolean canRegister(){
        return true;
    }

    /**
     * This is the method called when a placeholder with our identifier
     * is found and needs a value.
     * <br>We specify the value identifier in this method.
     * <br>Since version 2.9.1 can you use OfflinePlayers in your requests.
     *
     * @param  offlinePlayer
     *         A {@link org.bukkit.OfflinePlayer OfflinePlayer}.
     * @param  identifier
     *         A String containing the identifier/value.
     *
     * @return Possibly-null String of the requested identifier.
     */
    @Override
    public String onRequest(OfflinePlayer offlinePlayer, @NotNull String identifier){

        UUID uuid = offlinePlayer.getUniqueId();
        Player player = Bukkit.getPlayer(offlinePlayer.getUniqueId());
        if(player == null) return null;


        //noinspection SwitchStatementWithTooFewBranches
        switch (identifier) {

            case "enabled":
                return main.enabled(player) ? "true" : "false";

            case "smelting_enabled":
                return main.autoSmelter.hasEnabled(player) ? "true" : "false";

            case "condense_enabled":
                return main.ingotCondenser.hasEnabled(player) ? "true" : "false";

        }

        return null;
    }
}
