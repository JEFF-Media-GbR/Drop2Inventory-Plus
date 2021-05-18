package de.jeff_media.Drop2InventoryPlus.data;

/**
 * Represents all settings a player can have in Drop2Inventory.
 * TODO: Use PersistentDataContainer instead.
 */
public class PlayerSetting {
	public boolean enabled;
	public boolean hasSeenMessage = false;

	public PlayerSetting(boolean enabled) {
		this.enabled = enabled;
	}
}