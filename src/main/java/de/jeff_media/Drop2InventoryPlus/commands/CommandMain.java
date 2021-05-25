package de.jeff_media.Drop2InventoryPlus.commands;

import de.jeff_media.Drop2InventoryPlus.Main;
import de.jeff_media.Drop2InventoryPlus.config.Config;
import de.jeff_media.Drop2InventoryPlus.config.Messages;
import de.jeff_media.Drop2InventoryPlus.config.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandMain implements CommandExecutor {
	
	final Main main;
	
	public CommandMain(Main main) {
		this.main = main;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String alias, String[] args) {
		
		if(!command.getName().equalsIgnoreCase("drop2inventory")) {
			return false;
		}

		if(args.length>0 && args[0].equalsIgnoreCase(Config.DEBUG)) {
			return CommandDebug.run(main,sender,command,args);
		}

		if(args.length>0 && args[0].equalsIgnoreCase("reload")) {
			return CommandReload.run(main,sender,command,args);
		}

		if(args.length>0 && sender.hasPermission(Permissions.ALLOW_TOGGLE_OTHERS)) {
			Player player = Bukkit.getPlayer(args[0]);
			if(player == null) {
				Messages.sendMessage(sender,ChatColor.RED+"Player "+ChatColor.DARK_RED+args[0]+ChatColor.RED+" not found.");
				return true;
			}
			main.togglePlayerSetting(player);
			if(main.enabled(player)) {
				Messages.sendMessage(sender,"§7Automatic drop collection has been §aenabled§7 for player "+player.getDisplayName());
			} else {
				Messages.sendMessage(sender,"§7Automatic drop collection has been §cdisabled§7 for player "+player.getDisplayName());
			}
			return true;
		}

		if(!(sender instanceof Player)) {
			Messages.sendMessage(sender,"You must be a player to run this command.");
			return true;
		}

		Player p = (Player) sender;

		if(!sender.hasPermission(Permissions.ALLOW_USE)) {
			Messages.sendMessage(sender,main.getCommand("drop2inventory").getPermissionMessage());
			return true;
		}

		if(main.getConfig().getBoolean(Config.ALWAYS_ENABLED)) {
			Messages.sendMessage(sender,ChatColor.RED+"Drop2Inventory cannot be disabled.");
			return true;
		}
		
		main.togglePlayerSetting(p);
		if(main.enabled(p)) {
			Messages.sendMessage(sender,main.messages.MSG_ENABLED);
		} else {
			Messages.sendMessage(sender,main.messages.MSG_DISABLED);
		}
		return true;
		
	}

}