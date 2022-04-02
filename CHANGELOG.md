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
- Fixed typo in default condense.txt config file that prevented the plugin from enabling

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