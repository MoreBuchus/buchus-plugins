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

import com.google.common.collect.ArrayListMultimap;
import com.google.inject.Provides;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.function.Predicate;

@PluginDescriptor(
	name = "CoX Additions",
	description = "Additional plugins for the Chambers of Xeric",
	tags = {"xeric", "olm", "chambers", "cox", "buchus"},
	enabledByDefault = false
)
@Slf4j
public class CoxAdditionsPlugin extends Plugin implements KeyListener
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private CoxAdditionsConfig config;

	@Inject
	private ConfigManager configManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private CoxAdditionsOverlay overlay;

	@Inject
	private OlmSideOverlay olmSideOverlay;

	@Inject
	private CoxItemOverlay itemOverlay;

	@Inject
	private InstanceTimerOverlay instanceTimerOverlay;

	@Inject
	private VanguardInfoBox vanguardInfobox;

	@Inject
	private InfoBoxManager infoBoxManager;

	@Inject
	private ItemManager itemManager;

	@Inject
	private EventBus eventBus;

	@Inject
	private KeyManager keyManager;

	@Getter(AccessLevel.PACKAGE)
	private final ArrayListMultimap<String, Integer> optionIndexes = ArrayListMultimap.create();

	@Getter(AccessLevel.PACKAGE)
	private GameObject coxHerb1;
	@Getter(AccessLevel.PACKAGE)
	private int coxHerbTimer1;
	@Getter(AccessLevel.PACKAGE)
	private GameObject coxHerb2;
	@Getter(AccessLevel.PACKAGE)
	private int coxHerbTimer2;

	@Getter(AccessLevel.PACKAGE)
	private boolean enhanceSipped;
	@Getter(AccessLevel.PACKAGE)
	private int enhanceTicks = -1;
	@Getter(AccessLevel.PACKAGE)
	private int totalEnhCycles = 0;
	@Getter(AccessLevel.PACKAGE)
	private EnhanceInfobox enhanceInfobox;

	@Getter(AccessLevel.PACKAGE)
	private int instanceTimer = 3;
	@Getter(AccessLevel.PACKAGE)
	private boolean isInstanceTimerRunning = false;

	@Getter(AccessLevel.PACKAGE)
	private LocalPoint olmTile = null;

	@Getter(AccessLevel.PACKAGE)
	private final List<String> tlList = new ArrayList<>();
	@Getter(AccessLevel.PACKAGE)
	private final List<String> bossList = Arrays.asList(
		"tekton", "jewelled crab", "scavenger beast", "ice demon", "lizardman shaman", "vanguard",
		"vespula", "deathly ranger", "deathly mage", "vasa nistirio", "skeletal mystic", "muttadile");

	//Vanguards
	@Getter(AccessLevel.PACKAGE)
	private final int MAGE = 7529;
	@Getter(AccessLevel.PACKAGE)
	private final int RANGE = 7528;
	@Getter(AccessLevel.PACKAGE)
	private final int MELEE = 7527;
	@Getter(AccessLevel.PACKAGE)
	private final int DOWN = 7526;
	@Getter(AccessLevel.PACKAGE)
	private final ArrayList<Integer> ids = new ArrayList<>();
	@Getter(AccessLevel.PACKAGE)
	private NPC ranger;
	@Getter(AccessLevel.PACKAGE)
	private NPC mager;
	@Getter(AccessLevel.PACKAGE)
	private NPC meleer;
	@Getter(AccessLevel.PACKAGE)
	private boolean inRaid;
	@Getter(AccessLevel.PACKAGE)
	private boolean magerFound;
	@Getter(AccessLevel.PACKAGE)
	private boolean rangerFound;
	@Getter(AccessLevel.PACKAGE)
	private boolean meleeFound;
	@Getter(AccessLevel.PACKAGE)
	private int mageHP = -1;
	@Getter(AccessLevel.PACKAGE)
	private int rangeHP = -1;
	@Getter(AccessLevel.PACKAGE)
	private int meleeHP = -1;
	@Getter(AccessLevel.PACKAGE)
	private float percent;
	@Getter(AccessLevel.PACKAGE)
	private boolean inVangs;

	@Getter(AccessLevel.PACKAGE)
	private final ArrayList<Integer> chestHighlightIdList = new ArrayList<>();
	@Getter(AccessLevel.PACKAGE)
	private final ArrayList<Integer> chestHighlightIdList2 = new ArrayList<>();

	@Getter(AccessLevel.PACKAGE)
	private boolean hotkeyHeld;

	@Provides
	CoxAdditionsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CoxAdditionsConfig.class);
	}

	private void reset()
	{
		coxHerb1 = null;
		coxHerbTimer1 = 16;
		coxHerb2 = null;
		coxHerbTimer2 = 16;

		olmTile = null;

		ids.add(MAGE);
		ids.add(RANGE);
		ids.add(MELEE);
		ids.add(DOWN);
		inRaid = false;
		inVangs = false;
		meleeFound = false;
		rangerFound = false;
		magerFound = false;
	}

	@Override
	protected void startUp()
	{
		reset();

		tlList.clear();
		for (String str : config.tlList().split(","))
		{
			if (!str.trim().equals(""))
			{
				tlList.add(str.trim().toLowerCase());
			}
		}

		chestHighlightIdList.clear();
		for (String str : config.highlightChestItems().split(","))
		{
			if (!str.trim().equals(""))
			{
				try
				{
					chestHighlightIdList.add(Integer.valueOf(str.trim()));
				}
				catch (Exception ex)
				{
					System.out.println(ex.getMessage());
				}
			}
		}

		chestHighlightIdList2.clear();
		for (String str : config.highlightChestItems2().split(","))
		{
			if (!str.trim().equals(""))
			{
				try
				{
					chestHighlightIdList2.add(Integer.valueOf(str.trim()));
				}
				catch (Exception ex)
				{
					System.out.println(ex.getMessage());
				}
			}
		}

		enhanceSipped = false;
		enhanceTicks = -1;
		totalEnhCycles = 0;
		if (enhanceInfobox != null)
		{
			infoBoxManager.removeInfoBox(enhanceInfobox);
			enhanceInfobox = null;
		}

		keyManager.registerKeyListener(this);

		overlayManager.add(overlay);
		overlayManager.add(olmSideOverlay);
		overlayManager.add(vanguardInfobox);
		overlayManager.add(itemOverlay);
		overlayManager.add(instanceTimerOverlay);
	}

	@Override
	protected void shutDown()
	{
		reset();
		eventBus.unregister(this);
		keyManager.unregisterKeyListener(this);
		overlayManager.remove(overlay);
		overlayManager.remove(olmSideOverlay);
		overlayManager.remove(vanguardInfobox);
		overlayManager.remove(itemOverlay);
		overlayManager.remove(instanceTimerOverlay);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged e)
	{
		if (e.getGroup().equals("CoxAdditions"))
		{
			switch (e.getKey())
			{
				case "tlList":
					tlList.clear();
					for (String str : config.tlList().split(","))
					{
						if (!str.trim().equals(""))
						{
							tlList.add(str.trim().toLowerCase());
						}
					}
					break;
				case "highlightChestItems":
					chestHighlightIdList.clear();
					for (String str : config.highlightChestItems().split(","))
					{
						if (!str.trim().equals(""))
						{
							try
							{
								chestHighlightIdList.add(Integer.valueOf(str.trim()));
							}
							catch (Exception ex)
							{
								System.out.println(ex.getMessage());
							}
						}
					}
					break;
				case "highlightChestItems2":
					chestHighlightIdList2.clear();
					for (String str : config.highlightChestItems2().split(","))
					{
						if (!str.trim().equals(""))
						{
							try
							{
								chestHighlightIdList2.add(Integer.valueOf(str.trim()));
							}
							catch (Exception ex)
							{
								System.out.println(ex.getMessage());
							}
						}
					}
					break;
				case "detailedPrayerEnhance":
					if (config.detailedPrayerEnhance() == CoxAdditionsConfig.enhanceMode.OFF && enhanceInfobox != null)
					{
						removeInfobox();
					}
					else
					{
						addInfobox();
					}
					break;
			}
		}
	}

	@Subscribe
	private void onChatMessage(ChatMessage e)
	{
		String msg = Text.standardize(e.getMessageNode().getValue());

		if (msg.equalsIgnoreCase("you have been kicked from the channel.") || msg.contains("decided to start the raid without you. sorry.")
			|| msg.equalsIgnoreCase("you are no longer eligible to lead the party.") || msg.equalsIgnoreCase("the raid has begun!"))
		{
			instanceTimer = 5;
			isInstanceTimerRunning = false;
		}
		else if (msg.equalsIgnoreCase("inviting party...") || msg.equalsIgnoreCase("your party has entered the dungeons! come and join them now."))
		{
			instanceTimer = 5;
			isInstanceTimerRunning = true;
		}
	}

	@Subscribe
	private void onGameTick(GameTick e)
	{
		if (inRaid)
		{
			if (coxHerb1 != null || coxHerb2 != null)
			{
				if (coxHerb1 != null)
				{
					if (coxHerbTimer1 != 0)
					{
						coxHerbTimer1--;
					}
					else
					{
						coxHerb1 = null;
					}
				}

				if (coxHerb2 != null)
				{
					if (coxHerbTimer2 != 0)
					{
						coxHerbTimer2--;
					}
					else
					{
						coxHerb2 = null;
					}
				}
			}

			List<NPC> npcs = client.getNpcs();
			inVangs = false;
			for (NPC npc : npcs)
			{
				if (!ids.contains(npc.getId()))
				{
					continue;
				}
				inVangs = true;
				int currentId = npc.getId();
				switch (currentId)
				{
					case MAGE:
						percent = (float) npc.getHealthRatio() / npc.getHealthScale() * 100;
						mageHP = (int) percent;
						mager = npc;
						break;
					case RANGE:
						percent = (float) npc.getHealthRatio() / npc.getHealthScale() * 100;
						rangeHP = (int) percent;
						ranger = npc;
						break;
					case MELEE:
						percent = (float) npc.getHealthRatio() / npc.getHealthScale() * 100;
						meleeHP = (int) percent;
						meleer = npc;
						break;
					case DOWN:
						break;
					default:
				}
			}

			if (client.getVarbitValue(5417) > 0)
			{
				addInfobox();
			}
			else if (client.getVarbitValue(5417) == 0)
			{
				removeInfobox();
			}
			enhanceTicks--;
		}

		if (isInstanceTimerRunning)
		{
			instanceTimer--;
			if (instanceTimer < 0)
			{
				instanceTimer = 3;
			}
		}
	}

	@Subscribe
	private void onGameObjectSpawned(GameObjectSpawned e)
	{
		GameObject obj = e.getGameObject();
		if (inRaid)
		{
			if (obj.getId() >= 29997 && obj.getId() <= 29999)
			{
				if (coxHerb1 == null)
				{
					coxHerb1 = obj;
					coxHerbTimer1 = 16;
				}
				else
				{
					coxHerb2 = obj;
					coxHerbTimer2 = 16;
				}
			}
			else if (obj.getId() >= 30000 && obj.getId() <= 30008)
			{
				if (coxHerb1 == null)
				{
					coxHerb1 = obj;
					coxHerbTimer1 = 16;
				}
				else
				{
					coxHerb2 = obj;
					coxHerbTimer2 = 16;
				}
			}
		}
	}

	@Subscribe
	private void onGameObjectDespawned(GameObjectDespawned e)
	{
		GameObject obj = e.getGameObject();
		if (inRaid)
		{
			if (coxHerb1 != null || coxHerb2 != null)
			{
				if (obj.getId() >= 29997 && obj.getId() <= 30008)
				{
					if (coxHerb1 != null)
					{
						if (obj.getId() == coxHerb1.getId())
						{
							coxHerb1 = null;
						}
						else
						{
							coxHerb2 = null;
						}
					}
					else
					{
						coxHerb2 = null;
					}
				}
			}
		}
	}

	@Subscribe
	private void onNpcSpawned(NpcSpawned e)
	{
		if (inRaid && e.getNpc() != null)
		{
			NPC npc = e.getNpc();
			int id = npc.getId();
			String name = npc.getName();

			if (name != null)
			{
				if (name.equalsIgnoreCase("great olm"))
				{
					if (id == 7551)
					{
						olmTile = npc.getLocalLocation();
					}
					else if (id == 7554)
					{
						olmTile = null;
					}
				}
			}
		}
	}

	@Subscribe
	private void onNpcDespawned(NpcDespawned e)
	{
		if (inRaid)
		{
			NPC npc = e.getNpc();
			int id = npc.getId();
			String name = npc.getName();

			if (name != null)
			{
				if (name.equalsIgnoreCase("great olm"))
				{
					if (id == 7551)
					{
						olmTile = null;
					}
				}
			}
		}
	}

	@Subscribe
	private void onNpcChanged(NpcChanged e)
	{
		if (inRaid)
		{
			NPC npc = e.getNpc();
			int id = npc.getId();

			if (id == 7554)
			{
				olmTile = null;
			}
		}
	}

	private final Predicate<MenuEntry> filterMenuEntries = entry ->
	{
		if (inRaid)
		{
			String option = Text.standardize(entry.getOption()).toLowerCase();

			if (config.leftClickLeave() && option.contains("leave") && entry.getType().getId() == 1007)
			{
				entry.setType(MenuAction.CC_OP);
			}
		}
		return true;
	};

	private void swapMenuEntry(int index, MenuEntry menuEntry)
	{
		String option = Text.removeTags(menuEntry.getOption()).toLowerCase();
		String target = Text.removeTags(menuEntry.getTarget()).toLowerCase();

		if (inRaid)
		{
			if (config.swapCoXKeystone() && target.equals("keystone crystal") && option.equals("use"))
			{
				swap("drop", option, target, index, false);
			}

			if (config.hotkeySwapSmash() && target.contains("jewelled crab") && option.contains("attack"))
			{
				if (hotkeyHeld)
				{
					swap("smash", option, target, index);
				}
			}

			if (config.hotkeySwapBank() && target.contains("storage unit"))
			{
				if (hotkeyHeld)
				{
					if (option.contains("shared"))
					{
						swap("private", option, target, index);
					}
				}
			}
		}
	}

	@Subscribe
	public void onClientTick(ClientTick e)
	{
		if (client.getGameState() == GameState.LOGGED_IN && !client.isMenuOpen())
		{
			MenuEntry[] menuEntries = client.getMenuEntries();
			int idx = 0;
			optionIndexes.clear();

			for (MenuEntry entry : menuEntries)
			{
				String option = Text.removeTags(entry.getOption()).toLowerCase();
				optionIndexes.put(option, idx++);
			}

			idx = 0;
			for (MenuEntry entry : menuEntries)
			{
				swapMenuEntry(idx++, entry);
			}
		}
		client.setMenuEntries(updateMenuEntries(client.getMenuEntries()));
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged e)
	{
		inRaid = client.getVarbitValue(Varbits.IN_RAID) == 1;

		if (inRaid)
		{
			if (client.getVarbitValue(5417) > 0 && totalEnhCycles == 0)
			{
				totalEnhCycles = client.getVarbitValue(5417);
				enhanceTicks = client.getVarbitValue(5417) * 6;
				addInfobox();
			}
			else if (client.getVarbitValue(5417) < totalEnhCycles || client.getVarbitValue(5417) > totalEnhCycles)
			{
				enhanceTicks = client.getVarbitValue(5417) * 6;
				totalEnhCycles = client.getVarbitValue(5417);
				addInfobox(); //Makes infobox persist after log out
			}
		}
		else
		{
			coxHerb1 = null;
			coxHerbTimer1 = 16;
			coxHerb2 = null;
			coxHerbTimer2 = 16;

			inVangs = false;
			meleeFound = false;
			rangerFound = false;
			magerFound = false;

			enhanceSipped = false;
			enhanceTicks = -1;
			totalEnhCycles = 0;
			if (enhanceInfobox != null)
			{
				infoBoxManager.removeInfoBox(enhanceInfobox);
				enhanceInfobox = null;
			}
		}
	}

	private void swap(String optionA, String optionB, String target, int index)
	{
		swap(optionA, optionB, target, index, true);
	}

	private void swapContains(String optionA, String optionB, String target, int index)
	{
		swap(optionA, optionB, target, index, false);
	}

	private void swap(String optionA, String optionB, String target, int index, boolean strict)
	{
		MenuEntry[] menuEntries = client.getMenuEntries();
		int thisIndex = findIndex(menuEntries, index, optionB, target, strict);
		int optionIdx;

		if (target.contains("*"))
		{
			optionIdx = findIndex(menuEntries, thisIndex, optionA, target.replace("*", ""), strict);
		}
		else
		{
			optionIdx = findIndex(menuEntries, thisIndex, optionA, target, strict);
		}

		if (thisIndex >= 0 && optionIdx >= 0)
		{
			swap(optionIndexes, menuEntries, optionIdx, thisIndex);
		}
	}

	private int findIndex(MenuEntry[] entries, int limit, String option, String target, boolean strict)
	{
		if (strict)
		{
			List<Integer> indexes = optionIndexes.get(option);
			for (int i = indexes.size() - 1; i >= 0; i--)
			{
				int idx = indexes.get(i);
				MenuEntry entry = entries[idx];
				String entryTarget = Text.removeTags(entry.getTarget()).toLowerCase();
				if (idx <= limit && entryTarget.equals(target))
				{
					return idx;
				}
			}
		}
		else
		{
			for (int i = limit; i >= 0; i--)
			{
				MenuEntry entry = entries[i];
				String entryOption = Text.removeTags(entry.getOption()).toLowerCase();
				String entryTarget = Text.removeTags(entry.getTarget()).toLowerCase();
				if (entryOption.contains(option.toLowerCase()) && entryTarget.equals(target))
				{
					return i;
				}
			}
		}
		return -1;
	}

	private void swap(ArrayListMultimap<String, Integer> optionIndexes, MenuEntry[] entries, int index1, int index2)
	{
		MenuEntry entry1 = entries[index1],
			entry2 = entries[index2];
		entries[index1] = entry2;
		entries[index2] = entry1;

		if (entry1.isItemOp() && entry1.getType() == MenuAction.CC_OP_LOW_PRIORITY)
		{
			entry1.setType(MenuAction.CC_OP);
		}

		if (entry2.isItemOp() && entry2.getType() == MenuAction.CC_OP_LOW_PRIORITY)
		{
			entry2.setType(MenuAction.CC_OP);
		}

		client.setMenuEntries(entries);
		optionIndexes.clear();
		int idx = 0;
		for (MenuEntry menuEntry : entries)
		{
			String option = Text.removeTags(menuEntry.getOption()).toLowerCase();
			optionIndexes.put(option, idx++);
		}
	}

	private MenuEntry[] updateMenuEntries(MenuEntry[] menuEntries)
	{
		return Arrays.stream(menuEntries)
			.filter(filterMenuEntries).sorted((o1, o2) -> 0)
			.toArray(MenuEntry[]::new);
	}

	@Override
	public void keyTyped(KeyEvent e)
	{
	}

	public void keyPressed(KeyEvent e)
	{
		if (config.hotkey().matches(e))
		{
			hotkeyHeld = true;
		}
	}

	public void keyReleased(KeyEvent e)
	{
		if (config.hotkey().matches(e))
		{
			hotkeyHeld = false;
		}
	}

	public void addInfobox()
	{
		enhanceSipped = true;
		if (enhanceInfobox == null && config.detailedPrayerEnhance() != CoxAdditionsConfig.enhanceMode.OFF)
		{
			enhanceInfobox = new EnhanceInfobox(client, this, config);
			int img = ItemID.PRAYER_ENHANCE_4_20972;
			enhanceInfobox.setImage(itemManager.getImage(img));
			infoBoxManager.addInfoBox(enhanceInfobox);
		}
	}

	public void removeInfobox()
	{
		totalEnhCycles = 0;
		enhanceSipped = false;
		infoBoxManager.removeInfoBox(enhanceInfobox);
		enhanceInfobox = null;
	}
}
