package de.jeff_media.drop2inventory.commands;

import de.jeff_media.drop2inventory.Main;
import de.jeff_media.drop2inventory.config.Messages;
import de.jeff_media.drop2inventory.config.Permissions;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class CommandReload {

    static boolean run(Main main, CommandSender sender, Command command, String[] args)  {

        if(!sender.hasPermission(Permissions.ALLOW_RELOAD)) {
            Messages.sendMessage(sender,command.getPermissionMessage());
            return true;
        }

        main.reload();
        Messages.sendMessage(sender,ChatColor.GREEN+"Drop2InventoryPlus has been reloaded.");
        return true;
    }

}
