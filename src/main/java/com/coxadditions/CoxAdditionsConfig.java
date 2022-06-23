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

import net.runelite.client.config.*;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

@ConfigGroup("CoxAdditions")
public interface CoxAdditionsConfig extends Config
{
	@ConfigSection(
		name = "Rooms",
		description = "Cox Room Options",
		position = 1
	)
	String roomSection = "rooms";

	@ConfigSection(
		name = "Prep",
		description = "Cox Prep Options",
		position = 2
	)
	String prepSection = "prep";

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
		name = "Hotkey",
		keyName = "hotkey",
		description = "Configures the hotkey used for hotkey configs in Cox Additions",
		position = 2,
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
		position = 3,
		section = roomSection
	)
	default boolean hotkeySwapBank()
	{
		return false;
	}

	@ConfigItem(
		name = "Hotkey Swap Smash",
		keyName = "hotkeySwapSmash",
		description = "Switches attack and smash when holding the hotkey",
		position = 4,
		section = roomSection
	)
	default boolean hotkeySwapSmash()
	{
		return false;
	}

	@ConfigItem(
		keyName = "swapCoXKeystone",
		name = "Left Click Drop Keystone",
		description = "swaps use with drop for the keystone crystal at tightrope",
		position = 5,
		section = roomSection
	)
	default boolean swapCoXKeystone()
	{
		return false;
	}

	@ConfigItem(
		name = "Detailed Prayer Enhance",
		keyName = "detailedPrayerEnhance",
		description = "Displays a detailed prayer enhance timer in CoX",
		position = 6,
		section = roomSection
	)
	default enhanceMode detailedPrayerEnhance()
	{
		return enhanceMode.OFF;
	}

	@ConfigItem(
		name = "Vanguard HP Infobox",
		keyName = "vangInfobox",
		description = "Displays the hp left on each vanguard",
		position = 7,
		section = roomSection
	)
	default boolean vangInfobox()
	{
		return true;
	}

	@ConfigItem(
		name = "Olm Side Highlight",
		keyName = "olmSide",
		description = "Highlights a tile indicating which side olm will spawn on - disappears when he pops up",
		position = 8,
		section = roomSection
	)
	default boolean olmSide()
	{
		return false;
	}

	@Alpha
	@ConfigItem(
		name = "Olm Side Color",
		keyName = "olmSideColor",
		description = "Configures the color of the Olm side highlight",
		position = 9,
		section = roomSection
	)
	default Color olmSideColor()
	{
		return Color.RED;
	}

	@ConfigItem(
		name = "True Location List",
		keyName = "tlList",
		description = "NPC's in this list will be highlighted with true location. ONLY works with Cox bosses",
		position = 10,
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
		position = 11,
		section = roomSection
	)
	default double tlThiCC()
	{
		return 2;
	}

	@ConfigItem(
		name = "True Location Color",
		keyName = "tlColor",
		description = "Highlight color for true location",
		position = 12,
		section = roomSection
	)
	default Color tlColor()
	{
		return new Color(207, 138, 253);
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
		name = "Highlight Chest Mode",
		keyName = "highlightChest",
		description = "Highlight items in your private chest based off the lists",
		position = 4,
		section = prepSection)
	default HighlightChestMode highlightChest()
	{
		return HighlightChestMode.OFF;
	}

	@ConfigItem(
		name = "Highlight Private Chest Items 1",
		keyName = "highlightChestItems",
		description = "Highlights items in the list in the storage chest. Must be ids.",
		position = 5,
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
		position = 6,
		section = prepSection)
	default Color highlightChestItemsColor()
	{
		return Color.WHITE;
	}

	@ConfigItem(
		name = "Highlight Private Chest Items 2",
		keyName = "highlightChestItems2",
		description = "Highlights items in the list in the storage chest. Must be ids.",
		position = 7,
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
		position = 8,
		section = prepSection)
	default Color highlightChestItemsColor2()
	{
		return Color.WHITE;
	}

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
}
