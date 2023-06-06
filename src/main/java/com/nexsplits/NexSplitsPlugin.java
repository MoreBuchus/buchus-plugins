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

import com.google.inject.Provides;
import com.nexsplits.config.CoughMode;
import com.nexsplits.config.KillTimerMode;
import com.nexsplits.config.PhaseNameTypeMode;
import com.nexsplits.config.TimeStyle;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.RuneLite;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.image.BufferedImage;
import java.util.*;
import net.runelite.client.util.Text;

@PluginDescriptor(
	name = "Nex Splits",
	description = "Detailed time tracking for Nex",
	tags = {"nex", "godwars", "dungeon", "gwd", "splits", "torva", "zaros", "zaryte", "ancient", "time", "timer"}
)
@Slf4j
@Singleton
public class NexSplitsPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private NexSplitsConfig config;

	@Inject
	private NexSplitsOverlay overlay;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private InfoBoxManager infoBoxManager;

	@Inject
	private ItemManager itemManager;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ChatMessageManager chatMessageManager;

	private static final int[] nexRegions = {11345, 11601, 11857};
	private final ArrayList<Integer> nexIds = new ArrayList<>(Arrays.asList(NpcID.NEX, NpcID.NEX_11279, NpcID.NEX_11280, NpcID.NEX_11281, NpcID.NEX_11282));

	@Getter
	private boolean chickenDead = false;
	@Getter
	private int playerCount = 0;

	private NexTimerBox timerBox;

	@Getter
	private int splitTicks = 0;
	@Getter
	private String exportSplits = "";
	private static final String KC_MESSAGE = "Your Nex kill count is:";

	@Getter
	private int startTick = -1;
	@Getter
	private int p1Tick = -1;
	@Getter
	private int p1Boss = -1;
	@Getter
	private int p2Tick = -1;
	@Getter
	private int p2Boss = -1;
	@Getter
	private int p3Tick = -1;
	@Getter
	private int p3Boss = -1;
	@Getter
	private int p4Tick = -1;
	@Getter
	private int p4Boss = -1;
	@Getter
	private int p5Tick = -1;

	@Getter
	private static final File TIMES_DIR = new File(RuneLite.RUNELITE_DIR.getPath() + File.separator + "nex-splits");

	@Provides
	NexSplitsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(NexSplitsConfig.class);
	}

	@Override
	protected void startUp()
	{
		reset();
		resetTimes();
		overlayManager.add(overlay);

		if (!TIMES_DIR.exists())
		{
			TIMES_DIR.mkdirs();
		}
	}

	@Override
	protected void shutDown()
	{
		reset();
		resetTimes();
		overlayManager.remove(overlay);
		infoBoxManager.removeInfoBox(timerBox);
	}

	private void reset()
	{
		chickenDead = false;
		playerCount = 0;
		infoBoxManager.removeInfoBox(timerBox);
	}

	private void resetTimes()
	{
		startTick = -1;
		p1Tick = -1;
		p1Boss = -1;
		p2Tick = -1;
		p2Boss = -1;
		p3Tick = -1;
		p3Boss = -1;
		p4Tick = -1;
		p4Boss = -1;
		p5Tick = -1;
		splitTicks = 0;
		exportSplits = "";
	}

	@Subscribe
	private void onConfigChanged(ConfigChanged e)
	{
		if (e.getGroup().equals(NexSplitsConfig.GROUP))
		{
			if (e.getKey().equals("killTimer"))
			{
				if ((config.killTimer() == KillTimerMode.INFOBOX || config.killTimer() == KillTimerMode.BOTH) && inNexRegion() && startTick > -1)
				{
					infoBoxManager.removeInfoBox(timerBox);
					BufferedImage image = itemManager.getImage(ItemID.NEXLING);
					timerBox = new NexTimerBox(image, config, this, client);
					infoBoxManager.addInfoBox(timerBox);
				}
				else
				{
					infoBoxManager.removeInfoBox(timerBox);
				}
			}
		}
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged e)
	{
		if (client.getLocalPlayer() != null && !inNexRegion())
		{
			resetTimes();
			infoBoxManager.removeInfoBox(timerBox);
		}
	}

	@Subscribe(priority = -10.0f)
	public void onOverheadTextChanged(OverheadTextChanged e)
	{
		if (config.replaceCough() != CoughMode.OFF && inNexRegion())
		{
			if (e.getActor() instanceof Player)
			{
				String msg = "";
				if (e.getActor().getOverheadText().equalsIgnoreCase("*Cough*"))
				{
					switch (config.replaceCough())
					{
						case OLM:
							msg = new Random().nextInt(2) == 0 ? "Burn with me!" : "I will burn with you!";
							break;
						case CAT:
							msg = "*Meow*";
							break;
					}
					e.getActor().setOverheadText(msg);
				}
			}
			else if (e.getActor().getName() != null && e.getActor().getName().equals("Nex")
				&& e.getOverheadText().contains("Let the virus flow through you!"))
			{
				e.getActor().setOverheadText(config.replaceCough() == CoughMode.OLM ? "Let the burn flow through you!" : "Let the meow flow through you!");
			}
		}
	}

	@Subscribe
	private void onChatMessage(ChatMessage e) throws Exception
	{
		String text = e.getMessage();
		String strippedText = Text.removeTags(e.getMessage());

		if (e.getType() == ChatMessageType.GAMEMESSAGE)
		{
			if (strippedText.startsWith(KC_MESSAGE))
			{
				String trimKcMsg = strippedText.substring(text.indexOf(":")).replace(".", "");
				if (strippedText.startsWith(KC_MESSAGE))
				{
					exportSplits += "Nex KC" + trimKcMsg + " | Players: " + playerCount + "<br>";
					chickenDead = true;
				}
			}
			else if (strippedText.startsWith("Fight duration:"))
			{
				exportSplits += strippedText;
				if (config.timeExporter())
				{
					exportTimes();
				}
			}
			//Nex chatting
			else if (text.contains("Nex: <col=9090ff>") || text.contains("Nex: <col=0000ff>"))
			{
				//Minion phases
				if ((text.contains("Fumus") || text.contains("Umbra") || text.contains("Cruor") || text.contains("Glacies")) && text.contains(", don't fail me!"))
				{
					if (text.contains("Fumus"))
					{
						p1Boss = client.getTickCount();
						splitTicks = p1Boss;
						if (config.phaseChatMessages() && config.showMinionSplit())
						{
							String phaseText = config.phaseNameType() == PhaseNameTypeMode.NUMBER ? "P1" : "Smoke Phase";
							printTime(phaseText, getTime(p1Boss - startTick), false);
							exportSplits += phaseText + " Boss: " + getTime(p1Boss - startTick) + "<br>";
						}
					}
					else if (text.contains("Umbra"))
					{
						p2Boss = client.getTickCount();
						splitTicks = p2Boss;
						if (config.phaseChatMessages() && config.showMinionSplit())
						{
							String phaseText = config.phaseNameType() == PhaseNameTypeMode.NUMBER ? "P2" : "Shadow Phase";
							printTime(phaseText, getTime(p2Boss - p1Tick), false);
							exportSplits += phaseText + " Boss: " + getTime(p2Boss - p1Tick) + "<br>";
						}
					}
					else if (text.contains("Cruor"))
					{
						p3Boss = client.getTickCount();
						splitTicks = p3Boss;
						if (config.phaseChatMessages() && config.showMinionSplit())
						{
							String phaseText = config.phaseNameType() == PhaseNameTypeMode.NUMBER ? "P3" : "Blood Phase";
							printTime(phaseText, getTime(p3Boss - p2Tick), false);
							exportSplits += phaseText + " Boss: " + getTime(p3Boss - p2Tick) + "<br>";
						}
					}
					else if (text.contains("Glacies"))
					{
						p4Boss = client.getTickCount();
						splitTicks = p4Boss;
						if (config.phaseChatMessages() && config.showMinionSplit())
						{
							String phaseText = config.phaseNameType() == PhaseNameTypeMode.NUMBER ? "P4" : "Ice Phase";
							printTime(phaseText, getTime(p4Boss - p3Tick), false);
							exportSplits += phaseText + " Boss: " + getTime(p4Boss - p3Tick) + "<br>";
						}
					}
				}
				//Kill Start
				else if (text.contains("Fill my soul with smoke!"))
				{
					resetTimes();
					chickenDead = false;
					playerCount = client.getPlayers().size();
					if (startTick == -1)
					{
						startTick = client.getTickCount();
						splitTicks = startTick;
						if (config.killTimer() == KillTimerMode.INFOBOX || config.killTimer() == KillTimerMode.BOTH)
						{
							infoBoxManager.removeInfoBox(timerBox);
							BufferedImage image = itemManager.getImage(ItemID.NEXLING);
							timerBox = new NexTimerBox(image, config, this, client);
							infoBoxManager.addInfoBox(timerBox);
						}
					}
				}
				else if (text.contains("Darken my shadow!"))
				{
					p1Tick = client.getTickCount();
					splitTicks = p1Tick;
					if (config.phaseChatMessages())
					{
						String phaseText = config.phaseNameType() == PhaseNameTypeMode.NUMBER ? "P1" : "Smoke Phase";
						printTime(phaseText, getTime(p1Tick - startTick), true);
						exportSplits += phaseText + ": " + getTime(p1Tick - startTick) + "<br>";
					}
				}
				else if (text.contains("Flood my lungs with blood!"))
				{
					p2Tick = client.getTickCount();
					splitTicks = p2Tick;
					if (config.phaseChatMessages())
					{
						String phaseText = config.phaseNameType() == PhaseNameTypeMode.NUMBER ? "P2" : "Shadow Phase";
						printTime(phaseText, getTime(p2Tick - p1Tick), true);
						exportSplits += phaseText + ": " + getTime(p2Tick - p1Tick) + "<br>";
					}
				}
				else if (text.contains("Infuse me with the power of ice!"))
				{
					p3Tick = client.getTickCount();
					splitTicks = p3Tick;
					if (config.phaseChatMessages())
					{
						String phaseText = config.phaseNameType() == PhaseNameTypeMode.NUMBER ? "P3" : "Blood Phase";
						printTime(phaseText, getTime(p3Tick - p2Tick), true);
						exportSplits += phaseText + ": " + getTime(p3Tick - p2Tick) + "<br>";
					}
				}
				else if (text.contains("NOW, THE POWER OF ZAROS!"))
				{
					p4Tick = client.getTickCount();
					splitTicks = p4Tick;
					if (config.phaseChatMessages())
					{
						String phaseText = config.phaseNameType() == PhaseNameTypeMode.NUMBER ? "P4" : "Ice Phase";
						printTime(phaseText, getTime(p4Tick - p3Tick), true);
						exportSplits += phaseText + ": " + getTime(p4Tick - p3Tick) + "<br>";
					}
				}
				//Kill Finished
				else if (text.contains("Taste my wrath!"))
				{
					p5Tick = client.getTickCount();
					splitTicks = p5Tick;
					if (config.phaseChatMessages())
					{
						String phaseText = config.phaseNameType() == PhaseNameTypeMode.NUMBER ? "P5" : "Zaros Phase";
						printTime(phaseText, getTime(p5Tick - p4Tick), true);
						exportSplits += phaseText + ": " + getTime(p5Tick - p4Tick) + "<br>";
					}
				}
			}
		}
	}

	private void printTime(String phase, String time, boolean isPhase)
	{
		final ChatMessageBuilder chatMessageBuilder;
		if (isPhase)
		{
			chatMessageBuilder = new ChatMessageBuilder()
				.append(ChatColorType.NORMAL)
				.append("Nex " + phase + " Complete! Duration: ")
				.append(ChatColorType.HIGHLIGHT)
				.append(time);
		}
		else
		{
			chatMessageBuilder = new ChatMessageBuilder()
				.append(ChatColorType.NORMAL)
				.append("Nex " + phase + " Boss Complete! Duration: ")
				.append(ChatColorType.HIGHLIGHT)
				.append(time);
		}

		chatMessageManager.queue(QueuedMessage.builder()
			.type(ChatMessageType.CONSOLE)
			.runeLiteFormattedMessage(chatMessageBuilder.build())
			.build());
	}

	public String getPhaseTimes()
	{
		StringBuilder sb = new StringBuilder();
		String phaseText;
		String bossText = "";
		if (p1Tick > -1)
		{
			phaseText = config.phaseNameType() == PhaseNameTypeMode.NUMBER ? "P1: " : "Smoke: ";
			if (config.showMinionSplit() && p1Boss > -1)
			{
				bossText = " (" + getTime(p1Boss - startTick) + ")";
			}
			sb.append(phaseText).append(getTime(p1Tick - startTick)).append(bossText).append("</br>");

			if (p2Tick > -1)
			{
				phaseText = config.phaseNameType() == PhaseNameTypeMode.NUMBER ? "P2: " : "Shadow: ";
				if (config.showMinionSplit() && p2Boss > -1)
				{
					bossText = " (" + getTime(p2Boss - p1Tick) + ")";
				}
				sb.append(phaseText).append(getTime(p2Tick - p1Tick)).append(bossText).append("</br>");

				if (p3Tick > -1)
				{
					phaseText = config.phaseNameType() == PhaseNameTypeMode.NUMBER ? "P3: " : "Blood: ";
					if (config.showMinionSplit() && p3Boss > -1)
					{
						bossText = " (" + getTime(p3Boss - p2Tick) + ")";
					}
					sb.append(phaseText).append(getTime(p3Tick - p2Tick)).append(bossText).append("</br>");

					if (p4Tick > -1)
					{
						phaseText = config.phaseNameType() == PhaseNameTypeMode.NUMBER ? "P4: " : "Ice: ";
						if (config.showMinionSplit() && p4Boss > -1)
						{
							bossText = " (" + getTime(p4Boss - p3Tick) + ")";
						}
						sb.append(phaseText).append(getTime(p4Tick - p3Tick)).append(bossText).append("</br>");

						if (p5Tick > -1)
						{
							phaseText = config.phaseNameType() == PhaseNameTypeMode.NUMBER ? "P5: " : "Zaros: ";
							sb.append(phaseText).append(getTime(p5Tick - p4Tick)).append("</br>");
						}
					}
				}
			}
		}
		return sb.toString();
	}

	public String getTime(int ticks)
	{
		TimeStyle setting = config.timerStyle();
		boolean ingame_setting = client.getVarbitValue(11866) == 1;
		if (setting == TimeStyle.TICKS || (setting == TimeStyle.VARBIT && ingame_setting))
		{
			return to_mmss_precise(ticks);
		}

		return to_mmss(ticks);
	}

	public static String to_mmss(int ticks)
	{
		int m = ticks / 100;
		int s = (ticks - m * 100) * 6 / 10;
		return m + (s < 10 ? ":0" : ":") + s;
	}

	public static String to_mmss_precise(int ticks)
	{
		int min = ticks / 100;
		int tmp = (ticks - min * 100) * 6;
		int sec = tmp / 10;
		int sec_tenth = tmp - sec * 10;
		return min + (sec < 10 ? ":0" : ":") + sec + "." + sec_tenth;
	}

	private void exportTimes() throws Exception
	{
		String fileName = TIMES_DIR + "\\" + client.getLocalPlayer().getName() + "_NexTimes.txt";

		FileWriter writer = new FileWriter(fileName, true);
		try
		{
			writer.write(exportSplits.replace("<br>", "\r\n") + "\r\n" +
				"------------------------------------------------------------------------------------------------\r\n" +
				"------------------------------------------------------------------------------------------------\r\n");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		writer.close();
	}

	public boolean inNexRegion()
	{
		LocalPoint player = client.getLocalPlayer().getLocalLocation();
		return client.isInInstancedRegion() && Arrays.stream(nexRegions).anyMatch(r -> WorldPoint.fromLocalInstance(client, player).getRegionID() == r);
	}
}
