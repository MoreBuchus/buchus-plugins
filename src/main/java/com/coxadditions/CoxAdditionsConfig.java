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
package com.coxadditions;

import java.util.Collections;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.client.config.*;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

@ConfigGroup("CoxAdditions")
public interface CoxAdditionsConfig extends Config
{
	@ConfigSection(
		name = "Olm",
		description = "Olm Plugins",
		position = 0,
		closedByDefault = true
	)
	String olmSection = "olm";

	@ConfigSection(
		name = "Rooms",
		description = "Cox Room Options",
		position = 1,
		closedByDefault = true
	)
	String roomSection = "rooms";

	@ConfigSection(
		name = "Prep",
		description = "Cox Prep Options",
		position = 2,
		closedByDefault = true
	)
	String prepSection = "prep";

	@ConfigSection(
		name = "Font",
		description = "Font Options",
		position = 3,
		closedByDefault = true
	)
	String fontSection = "font";

	//Olm Section
	@ConfigItem(
		name = "Olm Side Highlight",
		keyName = "olmSide",
		description = "Highlights a tile indicating which side olm will spawn on - disappears when he pops up",
		position = 0,
		section = olmSection
	)
	default olmSideMode olmSide()
	{
		return olmSideMode.CENTER_TILE;
	}

	@ConfigItem(
		name = "Olm Side Color Mode",
		keyName = "olmSideColorMode",
		description = "Color = Olm Side Color, Phase = Color of the Olm phase (final phase is Olm Side Color)",
		position = 1,
		section = olmSection
	)
	default olmSideColorMode olmSideColorMode()
	{
		return olmSideColorMode.COLOR;
	}

	@Alpha
	@ConfigItem(
		name = "Olm Side Color",
		keyName = "olmSideColor",
		description = "Configures the color of the Olm side highlight",
		position = 2,
		section = olmSection
	)
	default Color olmSideColor()
	{
		return Color.RED;
	}

	@ConfigItem(
		name = "Olm Hands Health",
		keyName = "olmHandsHealth",
		description = "Puts an overlay on Olm's hands showing their current HP",
		position = 3,
		section = olmSection
	)
	default olmHandsHealthMode olmHandsHealth()
	{
		return olmHandsHealthMode.OFF;
	}

	@ConfigItem(
		name = "Replace Olm Orbs",
		keyName = "replaceOrbs",
		description = "Replaces Olm orbs with Warden attacks.",
		position = 4,
		section = olmSection
	)
	default boolean replaceOrbs()
	{
		return false;
	}

	@ConfigItem(
		name = "—————— Phase ——————",
		keyName = "olm divider",
		description = "",
		position = 5,
		section = olmSection
	)
	void olmDivider();

	@ConfigItem(
		name = "Olm Phase Highlight",
		keyName = "olmPhaseHighlight",
		description = "Highlights Olm head the color of the phase indicated in the chat box (Red = Flame, Green = Acid, Purple = Crystal)",
		position = 6,
		section = olmSection
	)
	default boolean olmPhaseHighlight()
	{
		return false;
	}

	@ConfigItem(
		name = "Show Phase Panel",
		keyName = "olmPhasePanel",
		description = "Displays Olm phase in an infobox",
		position = 7,
		section = olmSection
	)
	default boolean olmPhasePanel()
	{
		return false;
	}

	@Alpha
	@ConfigItem(
		name = "Final Phase Color",
		keyName = "olmHighlightColor",
		description = "Configures the color of the Olm phase highlight",
		position = 8,
		section = olmSection
	)
	default Color olmHighlightColor()
	{
		return Color.CYAN;
	}

	@Range(max = 255)
	@ConfigItem(
		name = "Olm Phase Fill Opacity",
		keyName = "olmHighlightFill",
		description = "Highlight fill opacity for Olm phase highlight",
		position = 9,
		section = olmSection
	)
	default int olmHighlightFill()
	{
		return 30;
	}

	@Range(min = 1, max = 5)
	@ConfigItem(
		name = "Olm Outline Width",
		keyName = "olmThiCC",
		description = "Outline width for Olm phase highlight",
		position = 10,
		section = olmSection
	)
	default double olmWidth()
	{
		return 2;
	}

	@ConfigItem(
		name = "Olm Line Type",
		keyName = "olmLines",
		description = "Sets the tile outline to regular, dashed, or corners only for Olm",
		position = 11,
		section = olmSection
	)
	default lineType olmLines()
	{
		return lineType.REG;
	}

	//Room Section
	@ConfigItem(
		name = "Instance Timer",
		keyName = "instanceTimer",
		description = "Instance timer for starting a raid.",
		position = 0,
		section = roomSection
	)
	default instanceTimerMode instanceTimer()
	{
		return instanceTimerMode.OVERHEAD;
	}

	@ConfigItem(
		name = "Left Click Leave CC",
		keyName = "leftClickLeave",
		description = "Left click leave corp simulator",
		position = 1,
		section = roomSection
	)
	default boolean leftClickLeave()
	{
		return false;
	}

	@ConfigItem(
		name = "CC Warning",
		keyName = "ccWarning",
		description = "Highlights the entrance to CoX. Red = not in a CC, Yellow = in a CC, but no party made.",
		position = 2,
		section = roomSection
	)
	default boolean ccWarning()
	{
		return false;
	}

	@ConfigItem(
		name = "Hotkey",
		keyName = "hotkey",
		description = "Configures the hotkey used for hotkey configs in Cox Additions",
		position = 3,
		section = roomSection
	)
	default Keybind hotkey()
	{
		return new Keybind(KeyEvent.VK_CONTROL, InputEvent.CTRL_DOWN_MASK);
	}

	@ConfigItem(
		name = "Hotkey Swap Private Chest",
		keyName = "hotkeySwapBank",
		description = "Switches your CoX chest from shared to private when holding the hotkey",
		position = 4,
		section = roomSection
	)
	default boolean hotkeySwapBank()
	{
		return false;
	}

	@ConfigItem(
		name = "Detailed Prayer Enhance",
		keyName = "detailedPrayerEnhance",
		description = "Displays a detailed prayer enhance timer in CoX",
		position = 5,
		section = roomSection
	)
	default enhanceMode detailedPrayerEnhance()
	{
		return enhanceMode.OFF;
	}

	@ConfigItem(
		name = "Anti-Aliasing",
		keyName = "antiAlias",
		description = "Turns on anti-aliasing for all overlays. Makes them smoother.",
		position = 6,
		section = roomSection
	)
	default boolean antiAlias()
	{
		return true;
	}

	@ConfigItem(
		name = "—————— True Tile ——————",
		keyName = "room divider",
		description = "",
		position = 7,
		section = roomSection
	)
	void roomDivider();

	@ConfigItem(
		name = "True Location List",
		keyName = "tlList",
		description = "NPC's in this list will be highlighted with true location. ONLY works with Cox bosses",
		position = 8,
		section = roomSection
	)
	default String tlList()
	{
		return "";
	}

	@Range(min = 1, max = 5)
	@ConfigItem(
		name = "True Location Width",
		keyName = "tlThiCC",
		description = "Outline width for true location highlight",
		position = 9,
		section = roomSection
	)
	default double tlThiCC()
	{
		return 2;
	}

	@Alpha
	@ConfigItem(
		name = "True Location Color",
		keyName = "tlColor",
		description = "Highlight color for true location",
		position = 10,
		section = roomSection
	)
	default Color tlColor()
	{
		return new Color(207, 138, 253, 255);
	}

	@Alpha
	@ConfigItem(
		name = "True Location Fill Color",
		keyName = "tlFillColor",
		description = "Fill color for true location",
		position = 11,
		section = roomSection
	)
	default Color tlFillColor()
	{
		return new Color(207, 138, 253, 30);
	}

	@ConfigItem(
		name = "True Tile Line Type",
		keyName = "tileLines",
		description = "Sets the true tile outline to regular, dashed, or corners only",
		position = 12,
		section = roomSection
	)
	default lineType tileLines()
	{
		return lineType.REG;
	}

	@ConfigItem(
		name = "—————— Puzzle Rooms ——————",
		keyName = "puzzle divider",
		description = "",
		position = 13,
		section = roomSection
	)
	void puzzleDivider();

	@ConfigItem(
		keyName = "chestGroupsHighlight",
		name = "Highlight Grub Chests",
		description = "CoX CM ONLY - Highlights groups of 4 chests",
		position = 14,
		section = roomSection
	)
	default Set<HighlightChestGroups> chestGroupsHighlight()
	{
		return Collections.emptySet();
	}

	@ConfigItem(
		keyName = "chestGroupsHighlightStyle",
		name = "Chest Highlight Style",
		description = "Selects the highlight style for 'Highlight Grub Chests'",
		position = 15,
		section = roomSection
	)
	default GrubChestStyle chestGroupsHighlightStyle()
	{
		return GrubChestStyle.HULL;
	}

	@ConfigItem(
		keyName = "grubsInfobox",
		name = "Grubs Counter",
		description = "Displays an infobox showing the total amount of cavern grubs collected. Works with party",
		position = 16,
		section = roomSection
	)
	default grubsMode grubsInfobox()
	{
		return grubsMode.OFF;
	}

	@ConfigItem(
		keyName = "grubsAmount",
		name = "Grubs Amount",
		description = "Set to the amount of grubs you/your team want to collect. The cavern grub counter's text will turn green when this number is reached.",
		position = 17,
		section = roomSection
	)
	default int grubsAmount()
	{
		return 30;
	}

	@ConfigItem(
		name = "Hotkey Swap Smash",
		keyName = "hotkeySwapSmash",
		description = "Switches attack and smash when holding the hotkey",
		position = 18,
		section = roomSection
	)
	default boolean hotkeySwapSmash()
	{
		return false;
	}

	@ConfigItem(
		keyName = "iceDemonHp",
		name = "Ice Demon HP",
		description = "Displays Ice Demon HP percent while lighting kindling",
		position = 19,
		section = roomSection
	)
	default boolean iceDemonHp()
	{
		return true;
	}

	@ConfigItem(
		keyName = "swapCoXKeystone",
		name = "Left Click Drop Keystone",
		description = "swaps use with drop for the keystone crystal at tightrope",
		position = 20,
		section = roomSection
	)
	default boolean swapCoXKeystone()
	{
		return false;
	}

	@ConfigItem(
		name = "—————— Combat Rooms ——————",
		keyName = "combat divider",
		description = "",
		position = 21,
		section = roomSection
	)
	void combatDivider();

	@ConfigItem(
		name = "Small Muttadile HP",
		keyName = "smallMuttaHp",
		description = "Displays the health percentage of small Muttadile while meat tree is alive",
		position = 22,
		section = roomSection
	)
	default boolean smallMuttaHp()
	{
		return true;
	}

	@ConfigItem(
		name = "Vanguard HP Infobox",
		keyName = "vangInfobox",
		description = "Displays the hp left on each vanguard",
		position = 23,
		section = roomSection
	)
	default boolean vangInfobox()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showPanel",
		name = "Vanguard Overloads Overlay",
		description = "Shows how many Overloads have been received from Vanguards. Works with party.",
		position = 24,
		section = roomSection
	)
	default boolean showPanel()
	{
		return true;
	}

	@ConfigItem(
		keyName = "overloadChatMessage",
		name = "Overload Dropped Chat Message",
		description = "Prints a chat message when a player receives an Overload from Vanguards. Works with party.",
		position = 25,
		section = roomSection
	)
	default boolean overloadChatMessage()
	{
		return true;
	}

	//Prep Section
	@ConfigItem(
		name = "CoX Herb Timer",
		keyName = "coxHerbTimer",
		description = "Displays a timer for herb growth",
		position = 1,
		section = prepSection)
	default CoXHerbTimerMode coxHerbTimer()
	{
		return CoXHerbTimerMode.OFF;
	}

	@Alpha
	@ConfigItem(
		name = "CoX Herb Timer Color",
		keyName = "coxHerbTimerColor",
		description = "Sets color of CoX herb timer",
		position = 2,
		section = prepSection)
	default Color coxHerbTimerColor()
	{
		return Color.YELLOW;
	}

	@Range(min = 10, max = 30)
	@ConfigItem(
		name = "CoX Herb Timer Size",
		keyName = "coxHerbTimerSize",
		description = "Sets the size of the CoX herb timer",
		position = 3,
		section = prepSection)
	default int coxHerbTimerSize()
	{
		return 20;
	}

	@ConfigItem(
		name = "—————— Prep ——————",
		keyName = "prep divider",
		description = "",
		position = 4,
		section = prepSection
	)
	void prepDivider();

	@Range(min = 0)
	@ConfigItem(
		keyName = "brews",
		name = "Xeric's Aids",
		description = "How many Xeric's Aids your team wants to make",
		position = 5,
		section = prepSection
	)
	default int brews() {
		return 0;
	}

	@Range(min = 0)
	@ConfigItem(
		keyName = "revites",
		name = "Revites",
		description = "How many Revites your team wants to make",
		position = 6,
		section = prepSection
	)
	default int revites() {
		return 0;
	}

	@Range(min = 0)
	@ConfigItem(
		keyName = "enhances",
		name = "Prayer Enhances",
		description = "How many Prayer Enhances your team wants to make",
		position = 7,
		section = prepSection
	)
	default int enhances() {
		return 0;
	}

	@Range(min = 0)
	@ConfigItem(
		keyName = "overloads",
		name = "Overloads",
		description = "How many Overloads your team wants to make",
		position = 8,
		section = prepSection
	)
	default int overloads() {
		return 0;
	}

	@Range(min = 0)
	@ConfigItem(
		keyName = "extraNox",
		name = "Extra Nox",
		description = "How many extra Noxifer you want to pick",
		position = 9,
		section = prepSection
	)
	default int extraNox() {
		return 0;
	}

	@ConfigItem(
		keyName = "showSecondaries",
		name = "Show Secondaries",
		description = "Toggle whether or not to show secondaries overlay in scavs",
		position = 10,
		section = prepSection
	)
	default boolean showSecondaries() {
		return false;
	}

	@ConfigItem(
		keyName = "showPots",
		name = "Show Pots Made",
		description = "Toggle whether or not to show how many pots you have made in prep",
		position = 11,
		section = prepSection
	)
	default boolean showPots() {
		return false;
	}

	@ConfigItem(
		name = "—————— Chest ——————",
		keyName = "chest divider",
		description = "",
		position = 12,
		section = prepSection
	)
	void bankDivider();

	@ConfigItem(
		name = "Highlight Chest Mode",
		keyName = "highlightChest",
		description = "Highlight items in your private chest based off the lists",
		position = 13,
		section = prepSection)
	default HighlightChestMode highlightChest()
	{
		return HighlightChestMode.OFF;
	}

	@ConfigItem(
		name = "Highlight Private Chest Items 1",
		keyName = "highlightChestItems",
		description = "Highlights items in the list in the storage chest. Can be names or ids.",
		position = 14,
		section = prepSection)
	default String highlightChestItems()
	{
		return "";
	}

	@Alpha
	@ConfigItem(
		name = "Chest Items Color 1",
		keyName = "highlightChestItemsColor",
		description = "Sets color of highlight chest items",
		position = 15,
		section = prepSection)
	default Color highlightChestItemsColor()
	{
		return Color.WHITE;
	}

	@ConfigItem(
		name = "Highlight Private Chest Items 2",
		keyName = "highlightChestItems2",
		description = "Highlights items in the list in the storage chest. Can be names or ids.",
		position = 16,
		section = prepSection)
	default String highlightChestItems2()
	{
		return "";
	}

	@Alpha
	@ConfigItem(
		name = "Chest Items Color 2",
		keyName = "highlightChestItemsColor2",
		description = "Sets color of highlight chest items",
		position = 17,
		section = prepSection)
	default Color highlightChestItemsColor2()
	{
		return Color.WHITE;
	}

	//Font Section
	@ConfigItem(
		name = "Overlay Font Type",
		keyName = "overlayFontType",
		description = "",
		position = 0,
		section = fontSection
	)
	default FontType overlayFontType()
	{
		return FontType.BOLD;
	}

	@ConfigItem(
		name = "Overlay Font Name",
		keyName = "overlayFontName",
		description = "Custom font override for overlays",
		position = 1,
		section = fontSection
	)
	default String overlayFontName()
	{
		return "";
	}

	@ConfigItem(
		name = "Overlay Font Size",
		keyName = "overlayFontSize",
		description = "",
		position = 2,
		section = fontSection
	)
	default int overlayFontSize()
	{
		return 11;
	}

	@ConfigItem(
		name = "Overlay Font Weight",
		keyName = "overlayFontWeight",
		description = "Sets the custom font weight for overlays",
		position = 3,
		section = fontSection
	)
	default FontWeight overlayFontWeight()
	{
		return FontWeight.PLAIN;
	}

	@ConfigItem(
		name = "—————— Panel ——————",
		keyName = "panel divider",
		description = "",
		position = 4,
		section = fontSection
	)
	void panelDivider();

	@ConfigItem(
		name = "Panel Font Type",
		keyName = "panelFontType",
		description = "",
		position = 5,
		section = fontSection
	)
	default FontType panelFontType()
	{
		return FontType.REGULAR;
	}

	@ConfigItem(
		name = "Panel Font Name",
		keyName = "panelFontName",
		description = "Custom font override for panels/infoboxes",
		position = 6,
		section = fontSection
	)
	default String panelFontName()
	{
		return "";
	}

	@ConfigItem(
		name = "Panel Font Size",
		keyName = "panelFontSize",
		description = "",
		position = 7,
		section = fontSection
	)
	default int panelFontSize()
	{
		return 11;
	}

	@ConfigItem(
		name = "Panel Font Weight",
		keyName = "panelFontWeight",
		description = "Sets the custom font weight for panels/infoboxes",
		position = 8,
		section = fontSection
	)
	default FontWeight panelFontWeight()
	{
		return FontWeight.PLAIN;
	}

	//Enums
	enum CoXHerbTimerMode
	{
		OFF,
		TEXT,
		PIE
	}

	enum HighlightChestMode
	{
		OFF,
		UNDERLINE,
		OUTLINE
	}

	enum instanceTimerMode
	{
		OFF,
		OVERHEAD,
		INFOBOX
	}

	enum enhanceMode
	{
		OFF,
		TICKS,
		REGEN_CYCLE
	}

	enum olmSideMode
	{
		OFF,
		CENTER_TILE,
		SIZE
	}

	enum olmSideColorMode
	{
		COLOR,
		PHASE
	}

	enum olmHandsHealthMode
	{
		OFF,
		INFOBOX,
		OVERLAY
	}

	enum GrubChestStyle
	{
		HULL,
		OUTLINE,
		TILE,
		CLICKBOX,
		CORNERS
	}

	@Getter
	@RequiredArgsConstructor
	enum HighlightChestGroups
	{
		CHEST_GROUPS_1("Groups 1-4"),
		CHEST_GROUPS_2("Groups 5-7"),
		CHEST_GROUPS_3("Groups 8-10");

		@Getter
		private final String group;

		@Override
		public String toString()
		{
			return group;
		}
	}

	@Getter
	@RequiredArgsConstructor
	enum grubsMode
	{
		OFF("Off"),
		THIEVING("Thieving"),
		BOTH("Thieving/Prep");

		private final String name;

		@Override
		public String toString()
		{
			return name;
		}
	}

	@Getter
	@RequiredArgsConstructor
	enum lineType
	{
		REG("Regular"),
		DASH("Dashed"),
		CORNER("Corners");

		@Getter
		private final String group;

		@Override
		public String toString()
		{
			return group;
		}
	}

	@Getter
	@RequiredArgsConstructor
	enum FontType
	{
		SMALL("RS Small"),
		REGULAR("RS Regular"),
		BOLD("RS Bold"),
		CUSTOM("Custom");

		private final String name;

		@Override
		public String toString()
		{
			return name;
		}
	}

	enum FontWeight
	{
		PLAIN(0),
		BOLD(1),
		ITALIC(2);

		@Getter
		private final int weight;

		FontWeight(int i)
		{
			weight = i;
		}
	}
}
