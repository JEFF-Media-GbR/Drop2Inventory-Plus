package de.jeff_media.Drop2InventoryPlus.commands;

import de.jeff_media.Drop2InventoryPlus.Main;
import de.jeff_media.Drop2InventoryPlus.Permissions;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class CommandReload {

    static boolean run(Main main, CommandSender sender, Command command, String[] args)  {

        if(!sender.hasPermission(Permissions.ALLOW_RELOAD)) {
            sender.sendMessage(command.getPermissionMessage());
            return true;
        }

        main.reload();
        sender.sendMessage(ChatColor.GREEN+"Drop2InventoryPlus has been reloaded.");
        return true;
    }

}
