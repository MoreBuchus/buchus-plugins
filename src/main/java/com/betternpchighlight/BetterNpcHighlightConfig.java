/*
 * Copyright (c) 2022, Buchus <http://github.com/MoreBuchus>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.betternpchighlight;

import net.runelite.client.config.*;

import java.awt.*;

@ConfigGroup(BetterNpcHighlightConfig.CONFIG_GROUP)
public interface BetterNpcHighlightConfig extends Config
{
	String CONFIG_GROUP = "BetterNpcHighlight";

	@ConfigSection(
		name = "Tile",
		description = "Tile Plugins",
		position = 0,
		closedByDefault = true
	)
	String tileSection = "tile";

	@ConfigSection(
		name = "True Tile",
		description = "True Tile Plugins",
		position = 1,
		closedByDefault = true
	)
	String trueTileSection = "trueTile";

	@ConfigSection(
		name = "South West Tile",
		description = "South West Tile Plugins",
		position = 2,
		closedByDefault = true
	)
	String swTileSection = "swTile";

	@ConfigSection(
		name = "South West True Tile",
		description = "South West True Tile Plugins",
		position = 3,
		closedByDefault = true
	)
	String swTrueTileSection = "swTrueTile";

	@ConfigSection(
		name = "Hull",
		description = "Hull Plugins",
		position = 4,
		closedByDefault = true
	)
	String hullSection = "hull";

	@ConfigSection(
		name = "Area",
		description = "Area Plugins",
		position = 5,
		closedByDefault = true
	)
	String areaSection = "area";

	@ConfigSection(
		name = "Outline",
		description = "Outline Plugins",
		position = 6,
		closedByDefault = true
	)
	String outlineSection = "outline";

	@ConfigSection(
		name = "Clickbox",
		description = "Clickbox Plugins",
		position = 7,
		closedByDefault = true
	)
	String clickboxSection = "clickbox";

	@ConfigSection(
		name = "<html><font color=#ff0000><i><b>TURBO MODE<b><i>",
		description = "Full send",
		position = 8,
		closedByDefault = true
	)
	String turboSection = "turbo";

	//------------------------------------------------------------//
	// Tile Section
	//------------------------------------------------------------//
	@ConfigItem(
		position = 1,
		keyName = "tileHighlight",
		name = "Tile Highlight",
		description = "Highlights NPCs by tile",
		section = tileSection
	)
	default boolean tileHighlight()
	{
		return false;
	}

	@ConfigItem(
		position = 2,
		keyName = "tileNames",
		name = "Tile Names",
		description = "List of NPCs to highlight by tile",
		section = tileSection
	)
	default String tileNames()
	{
		return "";
	}

	@ConfigItem(
		keyName = "tileNames",
		name = "",
		description = ""
	)
	void setTileNames(String names);

	@ConfigItem(
		position = 3,
		keyName = "tileIds",
		name = "Tile IDs",
		description = "List of NPCs to highlight by tile",
		section = tileSection
	)
	default String tileIds()
	{
		return "";
	}

	@ConfigItem(
		keyName = "tileIds",
		name = "",
		description = ""
	)
	void setTileIds(String ids);

	@Alpha
	@ConfigItem(
		position = 4,
		keyName = "tileColor",
		name = "Highlight Color",
		description = "Sets color of NPC tile highlights",
		section = tileSection
	)
	default Color tileColor()
	{
		return Color.CYAN;
	}

	@Alpha
	@ConfigItem(
		position = 5,
		keyName = "tileFillColor",
		name = "Fill Color",
		description = "Sets the fill color of npc highlights",
		section = tileSection
	)
	default Color tileFillColor()
	{
		return new Color(0, 255, 255, 20);
	}

	@Range(min = 0, max = 50)
	@ConfigItem(
		position = 6,
		keyName = "tileWidth",
		name = "Highlight Width",
		description = "Sets the width of npc highlights",
		section = tileSection
	)
	default double tileWidth()
	{
		return 2;
	}

	@ConfigItem(
		position = 7,
		keyName = "tileAA",
		name = "Anti-Aliasing",
		description = "Turns on anti-aliasing for the tiles. Makes them smoother.",
		section = tileSection
	)
	default boolean tileAA()
	{
		return true;
	}

	//------------------------------------------------------------//
	// True Tile Section
	//------------------------------------------------------------//
	@ConfigItem(
		position = 1,
		keyName = "trueTileHighlight",
		name = "True Tile Highlight",
		description = "Highlights npc's true tile",
		section = trueTileSection
	)
	default boolean trueTileHighlight()
	{
		return false;
	}

	@ConfigItem(
		position = 2,
		keyName = "trueTileNames",
		name = "True Tile Names",
		description = "List of npc's to highlight true tile",
		section = trueTileSection
	)
	default String trueTileNames()
	{
		return "";
	}

	@ConfigItem(
		keyName = "trueTileNames",
		name = "",
		description = ""
	)
	void setTrueTileNames(String names);

	@ConfigItem(
		position = 3,
		keyName = "trueTileIds",
		name = "True Tile IDs",
		description = "List of npc's to highlight true tile",
		section = trueTileSection
	)
	default String trueTileIds()
	{
		return "";
	}

	@ConfigItem(
		keyName = "trueTileIds",
		name = "",
		description = ""
	)
	void setTrueTileIds(String ids);

	@Alpha
	@ConfigItem(
		position = 4,
		keyName = "trueTileColor",
		name = "Highlight Color",
		description = "Sets color of npc highlights",
		section = trueTileSection
	)
	default Color trueTileColor()
	{
		return Color.CYAN;
	}

	@Alpha
	@ConfigItem(
		position = 5,
		keyName = "trueTileFillColor",
		name = "Fill Color",
		description = "Sets the fill color of npc highlights",
		section = trueTileSection
	)
	default Color trueTileFillColor()
	{
		return new Color(0, 255, 255, 20);
	}

	@Range(min = 0, max = 50)
	@ConfigItem(
		position = 6,
		keyName = "trueTileWidth",
		name = "Highlight Width",
		description = "Sets the width of npc highlights",
		section = trueTileSection
	)
	default double trueTileWidth()
	{
		return 2;
	}

	@ConfigItem(
		position = 7,
		keyName = "trueTileAA",
		name = "Anti-Aliasing",
		description = "Turns on anti-aliasing for the tiles. Makes them smoother.",
		section = trueTileSection
	)
	default boolean trueTileAA()
	{
		return true;
	}

	//------------------------------------------------------------//
	// SW Tile Section
	//------------------------------------------------------------//
	@ConfigItem(
		position = 1,
		keyName = "swTileHighlight",
		name = "South West Tile Highlight",
		description = "Highlights npc's south west tile",
		section = swTileSection
	)
	default boolean swTileHighlight()
	{
		return false;
	}

	@ConfigItem(
		position = 2,
		keyName = "swTileNames",
		name = "South West Tile Names",
		description = "List of npc's to highlight south west tile",
		section = swTileSection
	)
	default String swTileNames()
	{
		return "";
	}

	@ConfigItem(
		keyName = "swTileNames",
		name = "",
		description = ""
	)
	void setSwTileNames(String names);

	@ConfigItem(
		position = 3,
		keyName = "swTileIds",
		name = "South West Tile IDs",
		description = "List of npc's to highlight south west tile",
		section = swTileSection
	)
	default String swTileIds()
	{
		return "";
	}

	@ConfigItem(
		keyName = "swTileIds",
		name = "",
		description = ""
	)
	void setSwTileIds(String ids);

	@Alpha
	@ConfigItem(
		position = 4,
		keyName = "swTileColor",
		name = "Highlight Color",
		description = "Sets color of npc highlights",
		section = swTileSection
	)
	default Color swTileColor()
	{
		return Color.CYAN;
	}

	@Alpha
	@ConfigItem(
		position = 5,
		keyName = "swTileFillColor",
		name = "Fill Color",
		description = "Sets the fill color of npc highlights",
		section = swTileSection
	)
	default Color swTileFillColor()
	{
		return new Color(0, 255, 255, 20);
	}

	@Range(min = 0, max = 50)
	@ConfigItem(
		position = 6,
		keyName = "swTileWidth",
		name = "Highlight Width",
		description = "Sets the width of npc highlights",
		section = swTileSection
	)
	default double swTileWidth()
	{
		return 2;
	}

	@ConfigItem(
		position = 7,
		keyName = "swTileAA",
		name = "Anti-Aliasing",
		description = "Turns on anti-aliasing for the tiles. Makes them smoother.",
		section = swTileSection
	)
	default boolean swTileAA()
	{
		return true;
	}

	//------------------------------------------------------------//
	// SW True Tile Section
	//------------------------------------------------------------//
	@ConfigItem(
		position = 1,
		keyName = "swTrueTileHighlight",
		name = "South West True Tile Highlight",
		description = "Enables highlighting NPCs by their south west true tile",
		section = swTrueTileSection
	)
	default boolean swTrueTileHighlight()
	{
		return false;
	}

	@ConfigItem(
		position = 2,
		keyName = "swTrueTileNames",
		name = "South West True Tile Names",
		description = "List of NPCs to highlight by their south west true tile",
		section = swTrueTileSection
	)
	default String swTrueTileNames()
	{
		return "";
	}

	@ConfigItem(
		keyName = "swTrueTileNames",
		name = "",
		description = ""
	)
	void setSwTrueTileNames(String names);

	@ConfigItem(
		position = 3,
		keyName = "swTrueTileIds",
		name = "South West True Tile IDs",
		description = "List of NPCs to highlight by their south west true tile",
		section = swTrueTileSection
	)
	default String swTrueTileIds()
	{
		return "";
	}

	@ConfigItem(
		keyName = "swTrueTileIds",
		name = "",
		description = ""
	)
	void setSwTrueTileIds(String ids);

	@Alpha
	@ConfigItem(
		position = 4,
		keyName = "swTrueTileColor",
		name = "Highlight Color",
		description = "Sets color of npc highlights",
		section = swTrueTileSection
	)
	default Color swTrueTileColor()
	{
		return Color.CYAN;
	}

	@Alpha
	@ConfigItem(
		position = 5,
		keyName = "swTrueTileFillColor",
		name = "Fill Color",
		description = "Sets the fill color of npc highlights",
		section = swTrueTileSection
	)
	default Color swTrueTileFillColor()
	{
		return new Color(0, 255, 255, 20);
	}

	@Range(min = 0, max = 50)
	@ConfigItem(
		position = 6,
		keyName = "swTrueTileWidth",
		name = "Highlight Width",
		description = "Sets the width of npc highlights",
		section = swTrueTileSection
	)
	default double swTrueTileWidth()
	{
		return 2;
	}

	@ConfigItem(
		position = 7,
		keyName = "swTrueTileAA",
		name = "Anti-Aliasing",
		description = "Turns on anti-aliasing for the tiles. Makes them smoother.",
		section = swTrueTileSection
	)
	default boolean swTrueTileAA()
	{
		return true;
	}

	//------------------------------------------------------------//
	// Hull Section
	//------------------------------------------------------------//
	@ConfigItem(
		position = 1,
		keyName = "hullHighlight",
		name = "Hull Highlight",
		description = "Highlight npc's hull",
		section = hullSection
	)
	default boolean hullHighlight()
	{
		return false;
	}

	@ConfigItem(
		position = 2,
		keyName = "hullNames",
		name = "Hull Names",
		description = "List of npc's to highlight hull",
		section = hullSection
	)
	default String hullNames()
	{
		return "";
	}

	@ConfigItem(
		keyName = "hullNames",
		name = "",
		description = ""
	)
	void setHullNames(String names);

	@ConfigItem(
		position = 3,
		keyName = "hullIds",
		name = "Hull IDs",
		description = "List of npc's to highlight hull",
		section = hullSection
	)
	default String hullIds()
	{
		return "";
	}

	@ConfigItem(
		keyName = "hullIds",
		name = "",
		description = ""
	)
	void setHullIds(String ids);

	@Alpha
	@ConfigItem(
		position = 4,
		keyName = "hullColor",
		name = "Highlight Color",
		description = "Sets color of npc highlights",
		section = hullSection
	)
	default Color hullColor()
	{
		return Color.CYAN;
	}

	@Alpha
	@ConfigItem(
		position = 5,
		keyName = "hullFillColor",
		name = "Fill Color",
		description = "Sets the fill color of npc highlights",
		section = hullSection
	)
	default Color hullFillColor()
	{
		return new Color(0, 255, 255, 20);
	}

	@Range(min = 0, max = 50)
	@ConfigItem(
		position = 6,
		keyName = "hullWidth",
		name = "Highlight Width",
		description = "Sets the width of npc highlights",
		section = hullSection
	)
	default double hullWidth()
	{
		return 2;
	}

	@ConfigItem(
		position = 7,
		keyName = "hullAA",
		name = "Anti-Aliasing",
		description = "Turns on anti-aliasing for the tiles. Makes them smoother.",
		section = hullSection
	)
	default boolean hullAA()
	{
		return true;
	}

	//------------------------------------------------------------//
	// Area Section
	//------------------------------------------------------------//
	@ConfigItem(
		position = 1,
		keyName = "areaHighlight",
		name = "Area Highlight",
		description = "Highlights npc's area",
		section = areaSection
	)
	default boolean areaHighlight()
	{
		return false;
	}

	@ConfigItem(
		position = 2,
		keyName = "areaNames",
		name = "Area Names",
		description = "List of npc's to highlight area",
		section = areaSection
	)
	default String areaNames()
	{
		return "";
	}

	@ConfigItem(
		keyName = "areaNames",
		name = "",
		description = ""
	)
	void setAreaNames(String names);

	@ConfigItem(
		position = 3,
		keyName = "areaIds",
		name = "Area IDs",
		description = "List of npc's to highlight area",
		section = areaSection
	)
	default String areaIds()
	{
		return "";
	}

	@ConfigItem(
		keyName = "areaIds",
		name = "",
		description = ""
	)
	void setAreaIds(String ids);

	@Alpha
	@ConfigItem(
		position = 4,
		keyName = "areaColor",
		name = "Highlight Color",
		description = "Sets color of npc highlights",
		section = areaSection
	)
	default Color areaColor()
	{
		return new Color(0, 255, 255, 50);
	}

	//------------------------------------------------------------//
	// Outline Section
	//------------------------------------------------------------//
	@ConfigItem(
		position = 1,
		keyName = "outlineHighlight",
		name = "Outline Highlight",
		description = "Highlights npc's outline",
		section = outlineSection
	)
	default boolean outlineHighlight()
	{
		return false;
	}

	@ConfigItem(
		position = 2,
		keyName = "outlineNames",
		name = "Outline Names",
		description = "List of npc's to highlight outline",
		section = outlineSection
	)
	default String outlineNames()
	{
		return "";
	}

	@ConfigItem(
		keyName = "outlineNames",
		name = "",
		description = ""
	)
	void setOutlineNames(String names);

	@ConfigItem(
		position = 3,
		keyName = "outlineIds",
		name = "Outline IDs",
		description = "List of npc's to highlight outline",
		section = outlineSection
	)
	default String outlineIds()
	{
		return "";
	}

	@ConfigItem(
		keyName = "outlineIds",
		name = "",
		description = ""
	)
	void setOutlineIds(String ids);

	@Alpha
	@ConfigItem(
		position = 4,
		keyName = "outlineColor",
		name = "Highlight Color",
		description = "Sets color of npc highlights",
		section = outlineSection
	)
	default Color outlineColor()
	{
		return Color.CYAN;
	}

	@Range(min = 0, max = 50)
	@ConfigItem(
		position = 5,
		keyName = "outlineWidth",
		name = "Outline Width",
		description = "Sets the width of outline highlights",
		section = outlineSection
	)
	default int outlineWidth()
	{
		return 2;
	}

	@Range(min = 0, max = 5)
	@ConfigItem(
		position = 6,
		keyName = "outlineFeather",
		name = "Outline Feather",
		description = "Sets the feather of the outline highlights",
		section = outlineSection
	)
	default int outlineFeather()
	{
		return 2;
	}

	//------------------------------------------------------------//
	// Clickbox Section
	//------------------------------------------------------------//
	@ConfigItem(
		position = 1,
		keyName = "clickboxHighlight",
		name = "Clickbox Highlight",
		description = "Highlights NPCs by clickbox",
		section = clickboxSection
	)
	default boolean clickboxHighlight()
	{
		return false;
	}

	@ConfigItem(
		position = 2,
		keyName = "clickboxNames",
		name = "Clickbox Names",
		description = "List of NPCs to highlight by clickbox",
		section = clickboxSection
	)
	default String clickboxNames()
	{
		return "";
	}

	@ConfigItem(
		keyName = "clickboxNames",
		name = "",
		description = ""
	)
	void setClickboxNames(String names);

	@ConfigItem(
		position = 3,
		keyName = "clickboxIds",
		name = "Clickbox IDs",
		description = "List of NPCs to highlight by clickbox",
		section = clickboxSection
	)
	default String clickboxIds()
	{
		return "";
	}

	@ConfigItem(
		keyName = "clickboxIds",
		name = "",
		description = ""
	)
	void setClickboxIds(String ids);

	@Alpha
	@ConfigItem(
		position = 4,
		keyName = "clickboxColor",
		name = "Highlight Color",
		description = "Sets color of NPC clickbox highlights",
		section = clickboxSection
	)
	default Color clickboxColor()
	{
		return Color.CYAN;
	}

	@Alpha
	@ConfigItem(
		position = 5,
		keyName = "clickboxFillColor",
		name = "Fill Color",
		description = "Sets the fill color of NPC clickbox highlights",
		section = clickboxSection
	)
	default Color clickboxFillColor()
	{
		return new Color(0, 255, 255, 20);
	}

	//------------------------------------------------------------//
	// Turbo Section
	//------------------------------------------------------------//
	@ConfigItem(
		position = 0,
		name = "<html><p style=\"color:#e03c31\">—————— <b>Epilepsy Warning</b> ——————</p></html>",
		keyName = "epilepsyWarning",
		description = "A warning message will pop up if you try to enable Turbo Highlight. Turbo mode highlights NPCs switching between all styles and colors.",
		section = turboSection
	)
	void epilepsyWarning();

	@ConfigItem(
		position = 1,
		keyName = "turboHighlight",
		name = "Turbo Highlight",
		description = "Highlights NPCs in turbo mode",
		section = turboSection
	)
	default boolean turboHighlight()
	{
		return false;
	}

	@ConfigItem(
		position = 2,
		keyName = "turboNames",
		name = "Turbo Names",
		description = "List of NPCs to do things with",
		section = turboSection
	)
	default String turboNames()
	{
		return "";
	}

	@ConfigItem(
		keyName = "turboNames",
		name = "",
		description = ""
	)
	void setTurboNames(String names);

	@ConfigItem(
		position = 3,
		keyName = "turboIds",
		name = "Turbo IDs",
		description = "Can be used to see ghosts if you really want to",
		section = turboSection
	)
	default String turboIds()
	{
		return "";
	}

	//------------------------------------------------------------//
	// No Section
	//------------------------------------------------------------//
	@ConfigItem(
		position = 9,
		keyName = "tagStyleMode",
		name = "Tag Style",
		description = "Sets which highlight style list the NPC tagged is added too")
	default tagStyleMode tagStyleMode()
	{
		return tagStyleMode.TILE;
	}

	@ConfigItem(
		position = 14,
		keyName = "highlightMenuNames",
		name = "Highlight Menu Names",
		description = "Highlights names in right click menu entry"
	)
	default boolean highlightMenuNames()
	{
		return false;
	}

	@ConfigItem(
		position = 15,
		keyName = "ignoreDeadNpcs",
		name = "Ignore Dead NPCs",
		description = "Doesn't highlight dead NPCs"
	)
	default boolean ignoreDeadNpcs()
	{
		return false;
	}

	@ConfigItem(
		position = 16,
		keyName = "ignoreDeadExclusion",
		name = "Ignore Dead Exclusion List",
		description = "List of NPCs to not remove highlight when dead"
	)
	default String ignoreDeadExclusion()
	{
		return "";
	}

	@ConfigItem(
		position = 17,
		keyName = "deadNpcMenuColor",
		name = "Dead NPC Menu Color",
		description = "Highlights names in right click menu entry when an NPC is dead"
	)
	Color deadNpcMenuColor();

	@ConfigItem(
		position = 18,
		keyName = "respawnTimer",
		name = "Respawn Timer",
		description = "Marks tile and shows timer for when a marker NPC will respawn"
	)
	default respawnTimerMode respawnTimer()
	{
		return respawnTimerMode.OFF;
	}

	@Alpha
	@ConfigItem(
		position = 19,
		keyName = "respawnTimerColor",
		name = "Respawn Time Color",
		description = "Sets the color of the text for Respawn Timer"
	)
	default Color respawnTimerColor()
	{
		return Color.WHITE;
	}

	@Alpha
	@ConfigItem(
		position = 20,
		keyName = "respawnOutlineColor",
		name = "Respawn Outline Color",
		description = "Sets the color of the tile for Respawn Timer"
	)
	default Color respawnOutlineColor()
	{
		return Color.CYAN;
	}

	@Alpha
	@ConfigItem(
		position = 21,
		keyName = "respawnFillColor",
		name = "Respawn Fill Color",
		description = "Sets the fill color of the tile for Respawn Timer"
	)
	default Color respawnFillColor()
	{
		return new Color(0, 255, 255, 20);
	}

	@Range(min = 1, max = 10)
	@ConfigItem(
		position = 22,
		keyName = "respawnTileWidth",
		name = "Respawn Tile Width",
		description = "Sets the width of the tile for Respawn Timer"
	)
	default int respawnTileWidth()
	{
		return 2;
	}

	@ConfigItem(
		position = 23,
		keyName = "displayName",
		name = "Display Name",
		description = "Shows name of NPCs in the list above them"
	)
	default String displayName()
	{
		return "";
	}

	@ConfigItem(
		position = 24,
		keyName = "npcMinimapMode",
		name = "Highlight Minimap",
		description = "Highlights NPC on minimap and/or displays name"
	)
	default npcMinimapMode npcMinimapMode()
	{
		return npcMinimapMode.OFF;
	}

	enum tagStyleMode
	{
		TILE,
		TRUE_TILE,
		SW_TILE,
		SW_TRUE_TILE,
		HULL,
		AREA,
		OUTLINE,
		CLICKBOX,
		TURBO
	}

	enum respawnTimerMode
	{
		OFF,
		TICKS,
		SECONDS
	}

	enum npcMinimapMode
	{
		OFF,
		DOT,
		NAME,
		BOTH
	}
}
