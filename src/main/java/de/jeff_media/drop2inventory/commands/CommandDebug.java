package de.jeff_media.drop2inventory.commands;

import de.jeff_media.drop2inventory.Main;
import de.jeff_media.drop2inventory.config.Messages;
import de.jeff_media.drop2inventory.config.Permissions;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class CommandDebug {

    static boolean run(Main main, CommandSender sender, Command command, String[] args)  {

        if(!sender.hasPermission(Permissions.ALLOW_TOGGLE_DEBUG)) {
            Messages.sendMessage(sender,command.getPermissionMessage());
            return true;
        }

        main.setDebug(!main.isDebug());

        if(!main.isDebug()) {
            sender.sendMessage(ChatColor.GREEN+"Drop2Inventory DEBUG Mode disabled!");
            return true;
        }

        sender.sendMessage(ChatColor.GOLD+"Drop2Inventory v" + main.getDescription().getVersion() + " DEBUG Mode enabled!");
        sender.sendMessage(ChatColor.GRAY+"(You can expect massive console spam until disabled again)");

        return true;
    }

}
