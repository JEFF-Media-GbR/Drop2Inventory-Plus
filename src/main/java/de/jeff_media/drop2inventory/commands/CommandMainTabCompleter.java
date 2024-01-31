package de.jeff_media.drop2inventory.commands;

import de.jeff_media.drop2inventory.Main;
import de.jeff_media.drop2inventory.config.Config;
import de.jeff_media.drop2inventory.config.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.HumanEntity;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CommandMainTabCompleter implements TabCompleter {

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> commands = new ArrayList<>();

        if(strings.length == 2) {
            if(strings[0].equalsIgnoreCase("autocondense") || strings[0].equalsIgnoreCase("autosmelt")) {
                commands.add("on");
                commands.add("off");
                return StringUtil.copyPartialMatches(strings[1], commands, new ArrayList<>());
            }
        }

        if(commandSender.hasPermission(Permissions.ALLOW_RELOAD)) {
            commands.add("reload");
        }
        if(commandSender.hasPermission(Permissions.ALLOW_AUTO_CONDENSE) && !Main.getInstance().getConfig().getBoolean(Config.FORCE_AUTO_CONDENSE)) {
            commands.add("autocondense");
        }
        if(commandSender.hasPermission(Permissions.ALLOW_AUTO_SMELT) && !Main.getInstance().getConfig().getBoolean(Config.FORCE_AUTO_SMELT)) {
            commands.add("autosmelt");
        }
        if(commandSender.hasPermission(Permissions.ALLOW_TOGGLE_DEBUG)) {
            commands.add("debug");
        }
        if(commandSender.hasPermission(Permissions.ALLOW_TOGGLE_OTHERS)) {
            commands.addAll(Bukkit.getOnlinePlayers().stream().filter(player -> player != commandSender).map(HumanEntity::getName).collect(Collectors.toList()));
        }
        if(commandSender.hasPermission(Permissions.ALLOW_USE)) {
            commands.add("on");
            commands.add("off");
        }
        return StringUtil.copyPartialMatches(strings[0], commands, new ArrayList<>());
    }
}
