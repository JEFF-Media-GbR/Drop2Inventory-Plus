package de.jeff_media.Drop2InventoryPlus.commands;

import de.jeff_media.Drop2InventoryPlus.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CommandDebug {

    static boolean run(Main main, CommandSender sender, Command command, String[] args)  {

        if(!sender.hasPermission("drop2inventory.debug")) {
            sender.sendMessage(command.getPermissionMessage());
            return true;
        }

        main.debug = !main.debug;

        if(!main.debug) {
            main.debug(ChatColor.GREEN+"Drop2Inventory DEBUG Mode disabled!",sender);
            return true;
        }

        main.debug(ChatColor.GOLD+"Drop2Inventory DEBUG Mode enabled!",sender);
        main.debug(ChatColor.GRAY+"(You can expect massive console spam until disabled again)",sender);

        try {
            InputStream in = main.getResource("version.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            while (reader.ready()) {
                String line = reader.readLine();
                main.debug(line,sender);
            }
        } catch (IOException ioException) {
            main.debug(ChatColor.RED+"E: Could not detect version information",sender);
            ioException.printStackTrace();
        }


        return true;
    }

}
