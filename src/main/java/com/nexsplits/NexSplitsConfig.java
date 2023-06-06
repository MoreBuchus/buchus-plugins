/*
 * Copyright (c) 2023, Buchus <http://github.com/MoreBuchus>
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
package com.nexsplits;

import com.nexsplits.config.BackgroundMode;
import com.nexsplits.config.CoughMode;
import com.nexsplits.config.CustomOverlayInfo;
import com.nexsplits.config.FontType;
import com.nexsplits.config.FontWeight;
import com.nexsplits.config.KillTimerMode;
import com.nexsplits.config.PhaseNameTypeMode;
import com.nexsplits.config.TimeStyle;
import java.util.Collections;
import java.util.Set;

import java.awt.*;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(NexSplitsConfig.GROUP)
public interface NexSplitsConfig extends Config
{
	String GROUP = "nexSplits";

	@ConfigSection(
		name = "Timer",
		description = "Configuration for Kill/Phase Timers",
		position = 0,
		closedByDefault = true
	)
	String timerSection = "Timer";

	@ConfigSection(
		name = "Font",
		description = "Configuration for Kill/Phase Timer Font",
		position = 1,
		closedByDefault = true
	)
	String fontSection = "Font";

	//Timer Section
	@ConfigItem(
		keyName = "timerStyle",
		name = "Timer Style",
		description = "Changes how the time is displayed",
		position = 0,
		section = timerSection
	)
	default TimeStyle timerStyle()
	{
		return TimeStyle.VARBIT;
	}

	@ConfigItem(
		keyName = "killTimer",
		name = "Kill Timer",
		description = "Display either an infobox or panel with kill/phase times",
		position = 1,
		section = timerSection
	)
	default KillTimerMode killTimer()
	{
		return KillTimerMode.OFF;
	}

	@ConfigItem(
		keyName = "overlayInfo",
		name = "Display Options",
		description = "Options that can be displayed in the custom overlay",
		position = 2,
		section = timerSection
	)
	default Set<CustomOverlayInfo> overlayInfo()
	{
		return Collections.emptySet();
	}

	@ConfigItem(
		keyName = "phaseNameType",
		name = "Phase Name Type",
		description = "Display phases in timers and messages as either numbers(P1, P2, P3) or name(Smoke, shadow, blood)",
		position = 3,
		section = timerSection
	)
	default PhaseNameTypeMode phaseNameType()
	{
		return PhaseNameTypeMode.NUMBER;
	}

	@ConfigItem(
		keyName = "phaseChatMessages",
		name = "Phase Chat Message",
		description = "Puts message in chatbox for each phase",
		position = 4,
		section = timerSection
	)
	default boolean phaseChatMessages()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showMinionSplit",
		name = "Show Minion Split",
		description = "Shows boss and minion times for each phase",
		position = 5,
		section = timerSection
	)
	default boolean showMinionSplit()
	{
		return false;
	}

	@ConfigItem(
		name = "Time Exporter",
		keyName = "timeExporter",
		description = "Exports Nex times to .txt files in .runelite/nex-splits",
		position = 6,
		section = timerSection
	)
	default boolean timeExporter()
	{
		return false;
	}

	//Font Section
	@ConfigItem(
		name = "Font Type",
		keyName = "fontType",
		description = "",
		position = 0,
		section = fontSection
	)
	default FontType fontType()
	{
		return FontType.REGULAR;
	}

	@ConfigItem(
		name = "Custom Font Name",
		keyName = "fontName",
		description = "Custom font override",
		position = 1,
		section = fontSection
	)
	default String fontName()
	{
		return "";
	}

	@ConfigItem(
		name = "Custom Font Size",
		keyName = "fontsSize",
		description = "",
		position = 2,
		section = fontSection
	)
	default int fontSize()
	{
		return 11;
	}

	@ConfigItem(
		name = "Custom Weight",
		keyName = "fontWeight",
		description = "Sets the custom font weight",
		position = 3,
		section = fontSection
	)
	default FontWeight fontWeight()
	{
		return FontWeight.PLAIN;
	}

	@ConfigItem(
		name = "Background Style",
		keyName = "backgroundStyle",
		description = "Sets the background to the style you select",
		position = 4,
		section = fontSection
	)
	default BackgroundMode backgroundStyle()
	{
		return BackgroundMode.STANDARD;
	}

	@Alpha
	@ConfigItem(
		name = "Background Color",
		keyName = "backgroundColor",
		description = "Sets the overlay color on the custom setting",
		position = 5,
		section = fontSection
	)
	default Color backgroundColor()
	{
		return new Color(23, 23, 23, 156);
	}

	//Misc Section
	@ConfigItem(
		keyName = "replaceCough",
		name = "Replace Cough",
		description = "Replaces *Cough* during smoke phase",
		position = 96
	)
	default CoughMode replaceCough()
	{
		return CoughMode.OFF;
	}
}
