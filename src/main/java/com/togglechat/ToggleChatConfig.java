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
package com.togglechat;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Keybind;

@ConfigGroup("ToggleChat")
public interface ToggleChatConfig extends Config
{
	@ConfigItem(
		keyName = "hotKey",
		name = "Chat Toggle",
		description = "Pressing this key will toggle the chatbox",
		position = 0
	)
	default Keybind hotKey()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
		keyName = "defaultTab",
		name = "Default Tab",
		description = "The tab to open to when the hotkey is pressed",
		position = 1
	)
	default TabMode defaultTab()
	{
		return TabMode.ALL;
	}

	@ConfigItem(
		keyName = "removeFlashingTabs",
		name = "Disable Notification Flash",
		description = "For the chat-closed gamers - removes the annoying tab flashing. No blink blink",
		position = 2
	)
	default boolean removeFlashingTabs()
	{
		return false;
	}

	@ConfigSection(
		position = 3,
		name = "Notification Flash Settings",
		description = "Customization for the flash settings.",
		closedByDefault = true
	)
	String flashSection = "flashSection";

	@ConfigItem(
		keyName = "notifyWithOpenChat",
		name = "Notify with chat open",
		description = "Allows the categories to notify if chat box is opened.",
		position = 1,
		section = flashSection
	)
	default boolean notifyWithOpenChat()
	{
		return true;
	}

	@ConfigItem(
		keyName = "gameChat",
		name = "Game Chat",
		description = "Stops game chat from flashing.",
		position = 2,
		section = flashSection
	)
	default boolean gameChat()
	{
		return true;
	}

	@ConfigItem(
		keyName = "publicChat",
		name = "Public Chat",
		description = "Stops public chat from flashing.",
		position = 3,
		section = flashSection
	)
	default boolean publicChat()
	{
		return true;
	}

	@ConfigItem(
		keyName = "privateChat",
		name = "Private Chat",
		description = "Stops private chat from flashing.",
		position = 4,
		section = flashSection
	)
	default boolean privateChat()
	{
		return true;
	}

	@ConfigItem(
		keyName = "channelChat",
		name = "Channel Chat",
		description = "Stops channel chat from flashing.",
		position = 5,
		section = flashSection
	)
	default boolean channelChat()
	{
		return true;
	}

	@ConfigItem(
		keyName = "clanChat",
		name = "Clan Chat",
		description = "Stops clan chat from flashing.",
		position = 6,
		section = flashSection
	)
	default boolean clanChat()
	{
		return true;
	}

	@ConfigItem(
		keyName = "tradeChat",
		name = "Trade Chat",
		description = "Stops trade chat from flashing.",
		position = 7,
		section = flashSection
	)
	default boolean tradeChat()
	{
		return true;
	}
}
