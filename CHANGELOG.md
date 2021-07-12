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