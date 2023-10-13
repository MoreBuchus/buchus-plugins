# Tzhaar HP Tracker
Detailed HP tracking for all Tzhaar NPCs.

### Features:
* Tracks HP for both Fight Caves and Inferno NPCs
* Custom font options
* 3 HP overlay locations:
  * Above the HP bar
  * Center of the NPC 
  * Bottom of the NPC
* Custom highlighting options for Tzhaar NPCs
* Recolor menu for alive + dead Tzhaar NPCs
* Predicted hit features:
  * Option to hide NPCs if the predicted XP drop will kill the NPC - Similar to Nylo Death Indicators on the plugin hub
  * Overlay to display alive, dead, and predicted dead Tzhaar NPCs
  * Lag protection:
    * Will unhide hidden NPCs if your ping spikes above the set amount in the config
  * Custom XP Modifier:
    * For players on temporary game modes with boosted XP
* Reminder features:
  * Can select allowed spellbooks
  * 3 Options:
    * Remove enter for wrong spellbook
    * Highlight the entrance to the Fight Caves and Inferno if the player is on the wrong spellbook
    * Both

### Third Party Compliance
* This plugin is compliant with [Jagex's third-party client terms of service](https://secure.runescape.com/m=news/third-party-client-guidelines?oldschool=1).
* This plugin tracks current Tzhaar NPC HP and predicts hits. It also recolors menus and highlights NPCs based on HP. 
* Plugins that also use the above features are: 
  * Opponent Information 
  * Monster HP Percentage 
  * Custom XP Drops 
  * Nylo Death Indicators
  * NPC Indicators
  * Monster Menu HP
  
| RULE | COMPLIANT | RATIONALE |
| :------ | :------: | :------: |
| Anything that automatically indicates where to stand, or not to stand. This applies to only automatic indicators, and not tiles which have been manually marked. |  ✔️ | This plugin does not indicate any tile to stand on |
| Indicates the time where a boss mechanic may start or end |  ✔️| No mechanics are revealed by HP tracking in the Fight Caves or the Inferno. Zuk and Jad HP thresholds do not have any extra indicators |
| Adds additional visual or audio indicators of a boss mechanic except in cases where this is a manually triggered external helper. |  ✔️| No mechanics are revealed by HP tracking in the Fight Caves or the Inferno. Zuk and Jad HP thresholds do not have any extra indicators |
| Helps you to know when to "flinch" your opponent |  ✔️| No attack timers are tracked with this plugin |
| Indicates where projectiles will land |  ✔️| No projectiles are tracked with this plugin |
| Makes it easier to target 3D entities with a spell by removing some options |  ✔️| The only time options are removed is before you can enter the Fight Caves or Inferno if you are not on an allowed spellbook |
| Indicates how long an opponent is frozen for |  ✔️| Freeze timers are not tracked with this plugin |
| Indicates which prayer to use in any combat situation |  ✔️| There are no indicators to aid in prayer flicking |
