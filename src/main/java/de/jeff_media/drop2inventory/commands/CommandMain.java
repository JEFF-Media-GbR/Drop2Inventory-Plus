package de.jeff_media.drop2inventory.commands;

import de.jeff_media.drop2inventory.Main;
import de.jeff_media.drop2inventory.config.Config;
import de.jeff_media.drop2inventory.config.Messages;
import de.jeff_media.drop2inventory.config.Permissions;
import de.jeff_media.morepersistentdatatypes.DataType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandMain implements CommandExecutor {
	
	private final Main main;

	
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
		
		if(args.length>0 && args[0].equalsIgnoreCase("autocondense")) {
			return autoCondenseToggle(command, sender);
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
			Messages.sendMessage(sender,main.getMessages().MSG_ENABLED);
		} else {
			Messages.sendMessage(sender,main.getMessages().MSG_DISABLED);
		}
		return true;
		
	}

	private static void noPermission(Command command, CommandSender sender) {
		sender.sendMessage(command.getPermissionMessage() != null ? command.getPermissionMessage() : ChatColor.RED + "You don't have permission to do that.");
	}



	private boolean autoCondenseToggle(Command command, CommandSender sender) {
		// It's globally enabled, players cannot disable it
		if(main.getConfig().getBoolean(Config.FORCE_AUTO_CONDENSE)) {
			if(main.isDebug()) main.debug("AutoCondense is globally enabled, players cannot disable it");
			noPermission(command, sender);
			return true;
		}

		// No permission to toggle
		if(!sender.hasPermission(Permissions.ALLOW_AUTO_CONDENSE)) {
			if(main.isDebug()) main.debug("No permission to toggle");
			noPermission(command, sender);
			return true;
		}

		if(!(sender instanceof Player)) {
			Messages.sendMessage(sender,"You must be a player to run this command.");
			return true;
		}

		Player player = (Player) sender;
		boolean hadEnabled = main.ingotCondenser.hasEnabled(player);
		boolean nowEnabled = !hadEnabled;
		if(nowEnabled) {
			player.getPersistentDataContainer().set(main.ingotCondenser.getAutoCondenseKey(), DataType.BOOLEAN, true);
			Messages.sendMessage(player, main.getMessages().MSG_AUTOCONDENSE_ENABLED);
		} else {
			player.getPersistentDataContainer().remove(main.ingotCondenser.getAutoCondenseKey());
			Messages.sendMessage(player, main.getMessages().MSG_AUTOCONDENSE_DISABLED);
		}
		return true;
	}

}