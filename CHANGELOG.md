## 4.3.0
- Added "run-commands.yml" file
  - Lets you run commands on certain events, currently only supports "inventory-full"
- Changed default "default-detection-radius" to 1
  - This will fix drops from auto-farms (like cactus farms) being picked up randomly

## 4.0.0
- Added Auto-smelt
  - Works like auto-condense, but for smelting. Supports all cooking recipes, both builtin and added through plugins
  - Granting experience for auto-smelting is toggleable
  - Players can toggle it on/off using /d2i autosmelt
  - The setting "force-auto-smelt" in config.yml is a global option, it works like this:
    - Players will always need the permission "drop2inventory.autosmelt" to use auto-smelt
    - If the global option is set to true, players will have auto-smelt enabled and cannot disable it
    - If the global option is set to false, players will have auto-smelt disabled by default and can toggle it themselves using /d2i autosmelt
- Improved "auto-condense" feature
  - Players can toggle it on/off using /d2i autocondense 
  - The current setting "auto-condense" in config.yml was renamed to "force-autosmelt" and is now a global option, it works like this:
    - Players will always need the permission "drop2inventory.autocondense" to use auto-condense
    - If the global option is set to true, players will have auto-condense enabled and cannot disable it
    - If the global option is set to false, players will have auto-condense disabled by default and can toggle it themselves using /d2i autosmelt
  - Blocks to be condensed can now be configured using condense.yml. By default it includes all "revertible" vanilla recipes, e.g. iron_ingot -> iron_block, but not quartz -> quartz_block
- Auto-smelt and Auto-condense work fine together. For example if you mine a gold_ore, it will be smelted into a gold_ingot, and then condensed into a gold_block in case you already had 8 gold_ingots in your inventory
- Material blacklist, similar to disabled-blocks or disabled-blocks, but for the actual drop type
- Add "enabled by default" for both autosmelt and autocondense
- "inventory-full" message can now be shown as title or actionbar
- Fixed some plugins not being able to prohibit auto-pickup
- Fixed "coins" items from "Coins" plugin being picked up

## 4.1.1
- Fixed plugin not enabling when other plugins add crafting recipes using a "null" MaterialChoice
- Fixed warning about async tasks

## 4.1.0
- Added 1.19.4 support
- Fixed plugin not enabling when other plugins add crafting recipes using a "null" MaterialChoice

## 3.7.1
- Fixed hologram item getting duplicated when creating a new QuickShop shop while having D2I enabled

## 3.7.0
- Fixed custom items from EcoItems getting condensed
- Adjusted obfuscation settings to not use class names anymore that Windows is too stupid to use

## 3.6.0
- Added config option "collect-fishing-drops" to enable/disable collection of fishing drops. Disabled by default because enabling this will get rid of the "fish flying towards player" animation

## 3.5.2
- Fixed dead players being able to collect items

## 3.5.1
- Fixed players with no permission for drop2inventory.use but still having Drop2Inventory enabled being able to collect drops for a second after right-clicking a block

## 3.4.2
- Added a cooldown for the "added to inventory" and "your inventory is full" sounds to prevent sound spamming on explosions

## 3.4.1
- Improved performance

## 3.4.0
- Added config option "superior-skyblock-collect-only-on-own-islands" (false by default). See config.yml for more information

## 3.3.3
- Added compatibility for plugins that try to prevent players picking up items by setting the item's pickup delay to arbitrary long times (e.g. QuickShop)

## 3.3.2
- Fixed exception when other plugins have already stored a negative Statistics value for a certain block type

## 3.3.1
- Improved the way statistics are handled

## 3.3.0
- Fixed ingame statistics not recognizing picked up items
- General performance improvements
- Removed unused code and libraries, reducing file size by 140kb

## 3.2.5
- Fixed bug when mining blocks below Y=0

## 3.2.4
- Full 1.18.2 support
- "ignore-items-on-hoppers" is now disabled by default because it confused many users

## 3.2.2
- Fixed player data not being saved, even when "save-playerdata" was true lol. I should stop releasing updates in the middle of the night.

## 3.2.1
- Fixed "enabled-by-default" not working in certain configurations

## 3.2.0
- Added config option "save-playerdata". It works like this:
```yaml
# When set to true, Drop2Inventory remembers every player's setting after the rejoined.
# If you set this to false, players will always have the default settings after joining again,
# regardless of whether they had enabled or disabled Drop2Inventory before.
save-playerdata: true 
```

## 3.1.1
- D2I will no longer automatically collect XP from blacklisted blocks

## 3.1.0
- Improved Discord Verification

## 3.0.1
- Added safety net for badly coded plugins illegally implementing the player interface, not even considering behaving according to @NotNull annotations...
  - It will print a warning though, so you can ask the authors of those plugins to stop doing that, and use proper implementations instead 

## 2.11.0
- Added config option to not drop items to ground when the player's inventory is full

## 2.10.1
- Fixed typo in default condense.csv config file that prevented the plugin from enabling

## 2.10.0
- Added auto-condense support for raw iron, raw copper and raw gold

## 2.9.6
- Fixed drops being collected that players have dropped using Q

## 2.9.5
- Fixed disabling "collect-block-exp" and "collect-mob-exp" not working

## 2.9.4
- Added support for armor stand drops

## 2.9.3
- Added config option "works-in-creative" to also collect Drops for people in Creative Mode

## 2.9.2
- Removed warning message when the plugin cannot check for updates for any reasons

## 2.9.1
- Fixed "enabled-by-default" not working

## 2.9.0
- Added option to play a sound when the inventory is full

## 2.8.3
- Fixed autofarm drops being collected to players who kill other mobs nearby (BETA)

## 2.8.2
- Fixed EliteMobs visual items were being picked up (no idea why, but EliteMobs decided to just not cancel the EntityPickupItemEvent)

## 2.7.1
- Fixed emeralds not being condensed when using the auto-condense feature

## 2.7.0
- Fixed "Sweeping Edge" enchantment not giving correct amount of XP in certain forks (Paper etc.)

## 2.6.0
- Added support for plugins that listen to totally outdated events
  - E.g. SuperiorSkyblock2, even in 1.17, listens to the PlayerPickupItemEvent, which has been deprecated and replaced more than 4 years ago(!) by the EntityPickupItemEvent

## 2.5.3
- Fixed discord-verification.html file being invalid sometimes

## 2.5.0
- Changed the way blocks drop when your inventory is full
- Added discord-verification.html and notification (only shown to OPs once)

## 2.4.1
- Fixed exception in version 2.4.0

## 2.4.0
- Made event priority configurable for 3rd party plugins that do strange things
- Updated Russian translation

## 2.3.0
- Added support for shearing sheeps, harvesting sweet berry bushes, etc.
- Added "sound-pitch-variant" to config.yml (default 0.2) to make the drop sounds more random like in vanilla

## 2.2.1
- Fixed "%drop2inventory_enabled%" placeholder not working

## 2.2.0
- Fixed certain EliteMobs drops not being collected
- Fixed Chorus Plants from not being collected completely sometimes

## 2.1.2
- Fixed plugin not enabling on 1.17

## 2.1.1
- Fixed XP being automatically collected even when Drop2Inventory is disabled

## 2.1.0
- Added config option "detect-explosion-drops"

## 2.0.2
- Fixed plugin not enabling in old Minecraft versions (below 1.16)

## 2.0.1
- Fixed Config Updater updating the config every time
- Removed forgotten debug messages

## 2.0.0
- FULL support for mcMMO and similar plugins
- Supports ALL drops from ALL plugins!
  - Drop2Inventory Plus was completely rewritten and now uses totoally different mechanics to collect drops. It should be compatible with basically EVERY plugin
  - If somehow some drops still aren't detected, let me know on my discord at https://discord.jeff-media.de and I'll fix it ASAP!
- Improved UpdateChecker and ConfigUpdater
- Added "Pirate Speak" language

## 1.5.0
- You can set messages to an empty String ("") to avoid them from being shown to the player

## 1.4.1
- Added config option "call-entity-pickup-item-event"

## 1.4.0
- Drop2Inventory can now call fake EntityPickupItemEvents to check if other plugins want to cancel a drop picked up by the legacy drop detection
- Fixed exception when other plugins somehow cause a EntityDamageEvent without giving a DamageCause

## 1.3.1
- Raised priority for EntityDeathEvent for better compatibility with other plugins

## 1.3.0
- Added option to play sound effect when picking up drops

## 1.2.1
- Fixed colors not showing when using `/drop2inv <player>`

## 1.2.0
- Added new config options:
  - ignore-drops-from-mobs-killed-by-lava: false
  - ignore-drops-from-mobs-killed-by-magma: false

## 1.1.2
- Improved automatic update checker

## 1.1.1
- Made "Your inventory is full"-message configurable