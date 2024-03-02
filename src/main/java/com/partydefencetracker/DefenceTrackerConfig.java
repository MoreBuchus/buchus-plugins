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
package com.partydefencetracker;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("defencetracker")
public interface DefenceTrackerConfig extends Config
{
	@ConfigItem(
			name = "Low Defence Threshold",
			keyName = "lowDef",
			description = "Sets when you want the defence to appear as low defence",
			position = 1
	)
	default int lowDef()
	{
		return 10;
	}

	@ConfigItem(
		name = "High Defence Color",
		keyName = "highDefColor",
		description = "Color of the infobox text when the defence is above the low defence threshold",
		position = 2
	)
	default Color highDefColor()
	{
		return Color.WHITE;
	}

	@ConfigItem(
		name = "Low Defence Color",
		keyName = "lowDefColor",
		description = "Color of the infobox text when the defence is beneath the low defence threshold",
		position = 3
	)
	default Color lowDefColor()
	{
		return Color.YELLOW;
	}

	@ConfigItem(
		name = "Capped Defence Color",
		keyName = "cappedDefColor",
		description = "Color of the infobox text when the defence is at the lowest possible amount for that NPC",
		position = 4
	)
	default Color cappedDefColor()
	{
		return Color.GREEN;
	}

	@ConfigItem(
		keyName = "disableIBColor",
		name = "Disable Infobox Text Color",
		description = "Defence infobox text will always be set to White",
		position = 5
	)
	default boolean disableIBColor()
	{
		return false;
	}

	@ConfigItem(
			keyName = "vulnerability",
			name = "Show Vulnerability",
			description = "Displays an infobox when you successfully land vulnerability",
			position = 6
	)
	default boolean vulnerability()
	{
		return true;
	}

	@ConfigItem(
		keyName = "redKeris",
		name = "Show Red Keris",
		description = "Displays an infobox when you successfully land a Red Keris (Corruption) special attack",
		position = 7
	)
	default boolean redKeris()
	{
		return true;
	}
}
