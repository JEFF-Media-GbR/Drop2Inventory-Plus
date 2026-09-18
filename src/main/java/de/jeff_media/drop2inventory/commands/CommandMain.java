package de.jeff_media.drop2inventory.commands;

import de.jeff_media.drop2inventory.Main;
import de.jeff_media.drop2inventory.config.Config;
import de.jeff_media.drop2inventory.config.Messages;
import de.jeff_media.drop2inventory.config.Permissions;
import com.jeff_media.morepersistentdatatypes.DataType;
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

        Boolean onOff = null;
        if (args.length > 1) {
            if (args[1].equalsIgnoreCase("on")) {
                onOff = true;
            }
            else if (args[1].equalsIgnoreCase("off")) {
                onOff = false;
            }
        }

        if (args.length > 0 && args[0].equalsIgnoreCase(Config.DEBUG)) {
            return CommandDebug.run(main, sender, command, args);
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            return CommandReload.run(main, sender, command, args);
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("autocondense")) {
            return autoCondenseToggle(command, sender, onOff);
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("autosmelt")) {
            return autoSmeltToggle(command, sender, onOff);
        }

        onOff = null;
        if (args.length > 0 && args[0].equalsIgnoreCase("on")) {
            onOff = true;
        }
        else if (args.length > 0 && args[0].equalsIgnoreCase("off")) {
            onOff = false;
        }
        else if (args.length > 0 && sender.hasPermission(Permissions.ALLOW_TOGGLE_OTHERS)) {
            Player player = Bukkit.getPlayer(args[0]);
            if (player == null) {
                Messages.sendMessage(sender,
                        ChatColor.RED + "Player " + ChatColor.DARK_RED + args[0] + ChatColor.RED + " not found.");
                return true;
            }
            main.togglePlayerSetting(player);
            if (main.enabled(player)) {
                Messages.sendMessage(sender,
                        "§7Automatic drop collection has been §aenabled§7 for player " + player.getDisplayName());
            }
            else {
                Messages.sendMessage(sender,
                        "§7Automatic drop collection has been §cdisabled§7 for player " + player.getDisplayName());
            }
            return true;
        }

        if (!(sender instanceof Player)) {
            Messages.sendMessage(sender, "You must be a player to run this command.");
            return true;
        }

        Player p = (Player) sender;

        if (!sender.hasPermission(Permissions.ALLOW_USE)) {
            //Messages.sendMessage(sender,main.getCommand("drop2inventory").getPermissionMessage());
            Messages.sendMessage(sender, main.getMessages().MSG_NO_PERMS);
            return true;
        }

        if (main.getConfig().getBoolean(Config.ALWAYS_ENABLED)) {
            Messages.sendMessage(sender, ChatColor.RED + "Drop2Inventory cannot be disabled.");
            return true;
        }

		if(onOff == null || main.enabled(p) != onOff) {
			main.togglePlayerSetting(p);
		}
        if (main.enabled(p)) {
            Messages.sendMessage(sender, main.getMessages().MSG_ENABLED);
        }
        else {
            Messages.sendMessage(sender, main.getMessages().MSG_DISABLED);
        }
        return true;

    }

    private void noPermission(Command command, CommandSender sender) {
        sender.sendMessage(/*command.getPermissionMessage() != null ? command.getPermissionMessage() : ChatColor.RED + "You don't have permission to do that."*/
                main.getMessages().MSG_NO_PERMS);
    }

    private boolean autoCondenseToggle(Command command, CommandSender sender, Boolean onOff) {
        // It's globally enabled, players cannot disable it
        if (main.getConfig().getBoolean(Config.FORCE_AUTO_CONDENSE)) {
			if (main.isDebug()) {
				main.debug("AutoCondense is globally enabled, players cannot disable it");
			}
            noPermission(command, sender);
            return true;
        }

        // No permission to toggle
        if (!sender.hasPermission(Permissions.ALLOW_AUTO_CONDENSE)) {
			if (main.isDebug()) {
				main.debug("No permission to toggle");
			}
            noPermission(command, sender);
            return true;
        }

        if (!(sender instanceof Player)) {
            Messages.sendMessage(sender, "You must be a player to run this command.");
            return true;
        }

        Player player = (Player) sender;
        boolean hadEnabled = main.ingotCondenser.hasEnabled(player);
        boolean nowEnabled = onOff != null ? onOff : !hadEnabled;
        if (nowEnabled) {
            player.getPersistentDataContainer().set(main.ingotCondenser.getAutoCondenseKey(), DataType.BOOLEAN, true);
            Messages.sendMessage(player, main.getMessages().MSG_AUTOCONDENSE_ENABLED);
        }
        else {
            player.getPersistentDataContainer().remove(main.ingotCondenser.getAutoCondenseKey());
            Messages.sendMessage(player, main.getMessages().MSG_AUTOCONDENSE_DISABLED);
        }
        return true;
    }

    private boolean autoSmeltToggle(Command command, CommandSender sender, Boolean onOff) {
        // It's globally enabled, players cannot disable it
        if (main.getConfig().getBoolean(Config.FORCE_AUTO_SMELT)) {
			if (main.isDebug()) {
				main.debug("AutoSmelt is globally enabled, players cannot disable it");
			}
            noPermission(command, sender);
            return true;
        }

        // No permission to toggle
        if (!sender.hasPermission(Permissions.ALLOW_AUTO_SMELT)) {
			if (main.isDebug()) {
				main.debug("No permission to toggle");
			}
            noPermission(command, sender);
            return true;
        }

        if (!(sender instanceof Player)) {
            Messages.sendMessage(sender, "You must be a player to run this command.");
            return true;
        }

        Player player = (Player) sender;
        boolean hadEnabled = main.autoSmelter.hasEnabled(player);
        boolean nowEnabled = onOff != null ? onOff : !hadEnabled;
        if (nowEnabled) {
            player.getPersistentDataContainer().set(main.autoSmelter.getAutoSmeltKey(), DataType.BOOLEAN, true);
            Messages.sendMessage(player, main.getMessages().MSG_AUTOSMELT_ENABLED);
        }
        else {
            player.getPersistentDataContainer().remove(main.autoSmelter.getAutoSmeltKey());
            Messages.sendMessage(player, main.getMessages().MSG_AUTOSMELT_DISABLED);
        }
        return true;
    }

}