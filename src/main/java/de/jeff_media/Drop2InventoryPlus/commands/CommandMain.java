package de.jeff_media.Drop2InventoryPlus.commands;

import de.jeff_media.Drop2InventoryPlus.Config;
import de.jeff_media.Drop2InventoryPlus.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandMain implements CommandExecutor {
	
	Main main;
	
	public CommandMain(Main main) {
		this.main = main;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		
		if(!command.getName().equalsIgnoreCase("drop2inventory")) {
			return false;
		}

		if(args.length>0 && args[0].equalsIgnoreCase(Config.DEBUG)) {
			return CommandDebug.run(main,sender,command,args);
		}

		if(args.length>0 && args[0].equalsIgnoreCase("reload")) {
			return CommandReload.run(main,sender,command,args);
		}

		if(args.length>0 && sender.hasPermission("drop2inventory.others")) {
			Player player = Bukkit.getPlayer(args[0]);
			if(player == null) {
				sender.sendMessage(ChatColor.RED+"Player "+ChatColor.DARK_RED+args[0]+ChatColor.RED+" not found.");
				return true;
			}
			main.getPlayerSetting(player).enabled=!main.getPlayerSetting(player).enabled;
			if(main.getPlayerSetting(player).enabled) {
				sender.sendMessage("&7Automatic drop collection has been &aenabled&7 for player "+player.getDisplayName());
			} else {
				sender.sendMessage("&7Automatic drop collection has been &cdisabled&7 for player "+player.getDisplayName());
			}
			return true;
		}

		if(!(sender instanceof Player)) {
			sender.sendMessage("You must be a player to run this command.");
			return true;
		}

		Player p = (Player) sender;

		if(!sender.hasPermission("drop2inventory.use")) {
			sender.sendMessage(main.getCommand("drop2inventory").getPermissionMessage());
			return true;
		}

		if(main.getConfig().getBoolean(Config.ALWAYS_ENABLED)) {
			sender.sendMessage(ChatColor.RED+"Drop2Inventory cannot be disabled.");
			return true;
		}
		
		main.togglePlayerSetting(p);
		if(main.getPlayerSetting(p).enabled) {
			sender.sendMessage(main.messages.MSG_ENABLED);
		} else {
			sender.sendMessage(main.messages.MSG_DISABLED);
		}
		return true;
		
	}

}