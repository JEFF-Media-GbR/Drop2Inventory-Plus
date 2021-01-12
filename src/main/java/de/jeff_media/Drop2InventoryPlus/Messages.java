package de.jeff_media.Drop2InventoryPlus;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Messages {

	Main plugin;

	final public String
			MSG_ENABLED,
			MSG_DISABLED,
			MSG_HINT_ENABLE,
			MSG_HINT_DISABLE,
			MSG_INVENTORY_FULL;

	Messages(Main plugin) {
		this.plugin = plugin;

		MSG_ENABLED = getMsg("enabled",
				"&7Automatic drop collection has been &aenabled&7.&r");

		MSG_DISABLED = getMsg("disabled",
				"&7Automatic drop collection has been &cdisabled&7.&r");

		MSG_HINT_ENABLE = getMsg("when-breaking-block",
				"&7Hint: Type &6/drop2inventory&7 or &6/drop2inv&7 to enable automatic drop collection.");

		MSG_HINT_DISABLE = getMsg("when-breaking-block2",
				"&7Hint: Type &6/drop2inventory&7 or &6/drop2inv&7 to disable automatic drop collection.");

		MSG_INVENTORY_FULL = getMsg("inventory-full",
				"&cYour inventory is full.");


	}

	private String getMsg(String path, String defaultText) {
		return ChatColor.translateAlternateColorCodes('&',plugin.getConfig().getString("message-"+path,defaultText));
	}

    public void sendActionBarMessage(Player player, String message) {
		player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }
}