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

import com.google.common.collect.ImmutableSet;
import com.google.inject.Provides;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.NpcUtil;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.Text;
import javax.inject.Inject;
import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Set;

import static net.runelite.api.MenuAction.MENU_ACTION_DEPRIORITIZE_OFFSET;

@Slf4j
@PluginDescriptor(
	name = "Better NPC Highlight",
	description = "A more customizable NPC highlight",
	tags = {"npc", "highlight", "indicators", "respawn"}
)
public class BetterNpcHighlightPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private BetterNpcHighlightOverlay overlay;

	@Inject
	private BetterNpcHighlightConfig config;

	@Inject
	private BetterNpcMinimapOverlay mapOverlay;

	@Inject
	private NpcUtil npcUtil;

	@Inject
	private ConfigManager configManager;

	private static final Set<MenuAction> NPC_MENU_ACTIONS = ImmutableSet.of(MenuAction.NPC_FIRST_OPTION, MenuAction.NPC_SECOND_OPTION,
		MenuAction.NPC_THIRD_OPTION, MenuAction.NPC_FOURTH_OPTION, MenuAction.NPC_FIFTH_OPTION, MenuAction.WIDGET_TARGET_ON_NPC,
		MenuAction.ITEM_USE_ON_NPC);

	public ArrayList<String> tileNames = new ArrayList<String>();
	public ArrayList<Integer> tileIds = new ArrayList<Integer>();
	public ArrayList<String> trueTileNames = new ArrayList<String>();
	public ArrayList<Integer> trueTileIds = new ArrayList<Integer>();
	public ArrayList<String> swTileNames = new ArrayList<String>();
	public ArrayList<Integer> swTileIds = new ArrayList<Integer>();
	public ArrayList<String> swTrueTileNames = new ArrayList<String>();
	public ArrayList<Integer> swTrueTileIds = new ArrayList<Integer>();
	public ArrayList<String> hullNames = new ArrayList<String>();
	public ArrayList<Integer> hullIds = new ArrayList<Integer>();
	public ArrayList<String> areaNames = new ArrayList<String>();
	public ArrayList<Integer> areaIds = new ArrayList<Integer>();
	public ArrayList<String> outlineNames = new ArrayList<String>();
	public ArrayList<Integer> outlineIds = new ArrayList<Integer>();
	public ArrayList<String> clickboxNames = new ArrayList<String>();
	public ArrayList<Integer> clickboxIds = new ArrayList<Integer>();
	public ArrayList<String> turboNames = new ArrayList<String>();
	public ArrayList<Integer> turboIds = new ArrayList<Integer>();
	public ArrayList<Color> turboColors = new ArrayList<Color>();
	public ArrayList<NpcSpawn> npcSpawns = new ArrayList<NpcSpawn>();
	public ArrayList<String> namesToDisplay = new ArrayList<String>();
	public ArrayList<String> ignoreDeadExclusionList = new ArrayList<String>();
	public Instant lastTickUpdate;
	public int turboModeStyle = 0;
	public int turboTileWidth = 0;
	public int turboOutlineWidth = 0;
	public int turboOutlineFeather = 0;
	public boolean confirmedWarning = false;

	@Provides
	BetterNpcHighlightConfig providesConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BetterNpcHighlightConfig.class);
	}

	protected void startUp()
	{
		reset();
		overlayManager.add(overlay);
		overlayManager.add(mapOverlay);
		splitNameList(config.tileNames(), tileNames);
		splitIdList(config.tileIds(), tileIds);
		splitNameList(config.trueTileNames(), trueTileNames);
		splitIdList(config.trueTileIds(), trueTileIds);
		splitNameList(config.swTileNames(), swTileNames);
		splitIdList(config.swTileIds(), swTileIds);
		splitNameList(config.swTrueTileNames(), swTrueTileNames);
		splitIdList(config.swTrueTileIds(), swTrueTileIds);
		splitNameList(config.hullNames(), hullNames);
		splitIdList(config.hullIds(), hullIds);
		splitNameList(config.areaNames(), areaNames);
		splitIdList(config.areaIds(), areaIds);
		splitNameList(config.outlineNames(), outlineNames);
		splitIdList(config.outlineIds(), outlineIds);
		splitNameList(config.clickboxNames(), clickboxNames);
		splitIdList(config.clickboxIds(), clickboxIds);
		splitNameList(config.turboNames(), turboNames);
		splitIdList(config.turboIds(), turboIds);
		splitNameList(config.displayName(), namesToDisplay);
		splitNameList(config.ignoreDeadExclusion(), ignoreDeadExclusionList);
	}

	protected void shutDown()
	{
		reset();
		overlayManager.remove(overlay);
		overlayManager.remove(mapOverlay);
	}

	private void reset()
	{
		tileNames.clear();
		tileIds.clear();
		trueTileNames.clear();
		trueTileIds.clear();
		swTileNames.clear();
		swTileIds.clear();
		swTrueTileNames.clear();
		swTrueTileIds.clear();
		hullNames.clear();
		hullIds.clear();
		areaNames.clear();
		areaIds.clear();
		outlineNames.clear();
		outlineIds.clear();
		clickboxNames.clear();
		clickboxIds.clear();
		npcSpawns.clear();
		turboModeStyle = 0;
		turboTileWidth = 0;
		turboOutlineWidth = 0;
		turboOutlineFeather = 0;
		namesToDisplay.clear();
		ignoreDeadExclusionList.clear();
		confirmedWarning = false;
	}

	private void splitNameList(String configStr, ArrayList<String> strList)
	{
		if (!configStr.equals(""))
		{
			for (String str : configStr.split(","))
			{
				if (!str.trim().equals(""))
				{
					strList.add(str.trim().toLowerCase());
				}
			}
		}
	}

	private void splitIdList(String configStr, ArrayList<Integer> idList)
	{
		if (!configStr.equals(""))
		{
			for (String str : configStr.split(","))
			{
				if (!str.trim().equals(""))
				{
					try
					{
						idList.add(Integer.parseInt(str.trim()));
					}
					catch (Exception ex)
					{
						log.info("npc Highlight: " + ex.getMessage());
					}
				}
			}
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals(config.CONFIG_GROUP))
		{
			switch (event.getKey())
			{
				case "tileNames":
					tileNames.clear();
					splitNameList(config.tileNames(), tileNames);
					break;
				case "tileIds":
					tileIds.clear();
					splitIdList(config.tileIds(), tileIds);
					break;
				case "trueTileNames":
					trueTileNames.clear();
					splitNameList(config.trueTileNames(), trueTileNames);
					break;
				case "trueTileIds":
					trueTileIds.clear();
					splitIdList(config.trueTileIds(), trueTileIds);
					break;
				case "swTileNames":
					swTileNames.clear();
					splitNameList(config.swTileNames(), swTileNames);
					break;
				case "swTileIds":
					swTileIds.clear();
					splitIdList(config.swTileIds(), swTileIds);
					break;
				case "swTrueTileNames":
					swTrueTileNames.clear();
					splitNameList(config.swTrueTileNames(), swTrueTileNames);
					break;
				case "swTrueTileIds":
					swTrueTileIds.clear();
					splitIdList(config.swTrueTileIds(), swTrueTileIds);
					break;
				case "hullNames":
					hullNames.clear();
					splitNameList(config.hullNames(), hullNames);
					break;
				case "hullIds":
					hullIds.clear();
					splitIdList(config.hullIds(), hullIds);
					break;
				case "areaNames":
					areaNames.clear();
					splitNameList(config.areaNames(), areaNames);
					break;
				case "areaIds":
					areaIds.clear();
					splitIdList(config.areaIds(), areaIds);
					break;
				case "outlineNames":
					outlineNames.clear();
					splitNameList(config.outlineNames(), outlineNames);
					break;
				case "outlineIds":
					outlineIds.clear();
					splitIdList(config.outlineIds(), outlineIds);
					break;
				case "clickboxNames":
					clickboxNames.clear();
					splitNameList(config.clickboxNames(), clickboxNames);
					break;
				case "clickboxIds":
					clickboxIds.clear();
					splitIdList(config.clickboxIds(), clickboxIds);
					break;
				case "turboNames":
					turboNames.clear();
					splitNameList(config.turboNames(), turboNames);
					break;
				case "turboIds":
					turboIds.clear();
					splitIdList(config.turboIds(), turboIds);
					break;
				case "displayName":
					namesToDisplay.clear();
					splitNameList(config.displayName(), namesToDisplay);
					break;
				case "ignoreDeadExclusion":
					ignoreDeadExclusionList.clear();
					splitNameList(config.ignoreDeadExclusion(), ignoreDeadExclusionList);
					break;
				case "turboHighlight":
					if (event.getNewValue().equals("true"))
					{
						if (!confirmedWarning)
						{
							showEpilepsyWarning();
						}
						else
						{
							confirmedWarning = false;
						}
					}
					break;
			}
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGIN_SCREEN || event.getGameState() == GameState.HOPPING)
		{
			npcSpawns.clear();
		}
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		int type = event.getType();
		if (type >= MENU_ACTION_DEPRIORITIZE_OFFSET)
		{
			type -= MENU_ACTION_DEPRIORITIZE_OFFSET;
		}

		final MenuAction menuAction = MenuAction.of(type);
		if (NPC_MENU_ACTIONS.contains(menuAction))
		{
			NPC npc = client.getCachedNPCs()[event.getIdentifier()];

			Color color = null;
			if (npcUtil.isDying(npc))
			{
				color = config.deadNpcMenuColor();
			}
			else if (config.highlightMenuNames() && npc.getName() != null && checkAllLists(npc))
			{
				color = getSpecificColor(npc);
			}

			if (color != null)
			{
				MenuEntry[] menuEntries = client.getMenuEntries();
				final MenuEntry menuEntry = menuEntries[menuEntries.length - 1];
				final String target = ColorUtil.prependColorTag(Text.removeTags(event.getTarget()), color);
				menuEntry.setTarget(target);
				client.setMenuEntries(menuEntries);
			}
		}
		else if (menuAction == MenuAction.EXAMINE_NPC)
		{
			final int id = event.getIdentifier();
			final NPC npc = client.getCachedNPCs()[id];

			if (npc != null)
			{
				String option;
				if (npc.getName() != null)
				{
					if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.TILE)
					{
						if (tileNames.contains(npc.getName().toLowerCase()))
						{
							option = "Untag-Tile";
						}
						else
						{
							option = "Tag-Tile";
						}
					}
					else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.TRUE_TILE)
					{
						if (trueTileNames.contains(npc.getName().toLowerCase()))
						{
							option = "Untag-True-Tile";
						}
						else
						{
							option = "Tag-True-Tile";
						}
					}
					else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.SW_TILE)
					{
						if (swTileNames.contains(npc.getName().toLowerCase()))
						{
							option = "Untag-SW-Tile";
						}
						else
						{
							option = "Tag-SW-Tile";
						}
					}
					else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.SW_TRUE_TILE)
					{
						if (swTrueTileNames.contains(npc.getName().toLowerCase()))
						{
							option = "Untag-SW-True-Tile";
						}
						else
						{
							option = "Tag-SW-True-Tile";
						}
					}
					else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.HULL)
					{
						if (hullNames.contains(npc.getName().toLowerCase()))
						{
							option = "Untag-Hull";
						}
						else
						{
							option = "Tag-Hull";
						}
					}
					else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.AREA)
					{
						if (areaNames.contains(npc.getName().toLowerCase()))
						{
							option = "Untag-Area";
						}
						else
						{
							option = "Tag-Area";
						}
					}
					else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.OUTLINE)
					{
						if (outlineNames.contains(npc.getName().toLowerCase()))
						{
							option = "Untag-Outline";
						}
						else
						{
							option = "Tag-Outline";
						}
					}
					else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.CLICKBOX)
					{
						if (clickboxNames.contains(npc.getName().toLowerCase()))
						{
							option = "Untag-Clickbox";
						}
						else
						{
							option = "Tag-Clickbox";
						}
					}
					else
					{
						if (turboNames.contains(npc.getName().toLowerCase()))
						{
							option = "Untag-Turbo";
						}
						else
						{
							option = "Tag-Turbo";
						}
					}

					if (option.contains("Untag-") && (config.highlightMenuNames() || (npc.isDead() && config.deadNpcMenuColor() != null)))
					{
						MenuEntry[] menuEntries = client.getMenuEntries();
						final MenuEntry menuEntry = menuEntries[menuEntries.length - 1];
						String target;
						if (option.contains("Turbo"))
						{
							target = ColorUtil.prependColorTag(Text.removeTags(event.getTarget()), Color.getHSBColor(new Random().nextFloat(), 1.0F, 1.0F));
						}
						else
						{
							Color color = npc.isDead() ? config.deadNpcMenuColor() : getTagColor();
							target = ColorUtil.prependColorTag(Text.removeTags(event.getTarget()), color);
						}
						menuEntry.setTarget(target);
						client.setMenuEntries(menuEntries);
					}

					if (client.isKeyPressed(KeyCode.KC_SHIFT))
					{
						String tagAllEntry = "";
						if (config.highlightMenuNames() || (npc.isDead() && config.deadNpcMenuColor() != null))
						{
							String colorCode;
							if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.TURBO)
							{
								if (turboColors.size() == 0 && turboNames.contains(npc.getName().toLowerCase()))
								{
									colorCode = Integer.toHexString(turboColors.get(turboNames.indexOf(npc.getName().toLowerCase())).getRGB());
								}
								else
								{
									colorCode = Integer.toHexString(Color.getHSBColor(new Random().nextFloat(), 1.0F, 1.0F).getRGB());
								}
							}
							else
							{
								colorCode = npc.isDead() ? Integer.toHexString(config.deadNpcMenuColor().getRGB()) : Integer.toHexString(getTagColor().getRGB());
							}
							tagAllEntry = "<col=" + colorCode.substring(2) + ">" + Text.removeTags(event.getTarget());
						}
						else
						{
							tagAllEntry = event.getTarget();
						}

						client.createMenuEntry(-1)
							.setOption(option)
							.setTarget(tagAllEntry)
							.setIdentifier(event.getIdentifier())
							.setParam0(event.getActionParam0())
							.setParam1(event.getActionParam1())
							.setType(MenuAction.RUNELITE);
					}
				}
			}
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (event.getMenuAction() == MenuAction.RUNELITE)
		{
			if ((event.getMenuOption().contains("Tag") || event.getMenuOption().contains("Untag")) && (event.getMenuOption().contains("-Tile")
				|| event.getMenuOption().contains("-True-Tile") || event.getMenuOption().contains("-SW-Tile") || event.getMenuOption().contains("-SW-True-Tile")
				|| event.getMenuOption().contains("-Hull") || event.getMenuOption().contains("-Area") || event.getMenuOption().contains("-Outline")
				|| event.getMenuOption().contains("-Clickbox") || event.getMenuOption().contains("-Turbo")))
			{
				final int id = event.getId();
				final NPC npc = client.getCachedNPCs()[id];

				ArrayList<String> listToChange = new ArrayList<>();
				if (npc.getName() != null)
				{
					if (event.getMenuOption().contains("Untag"))
					{
						if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.TILE)
						{
							tileNames.remove(npc.getName().toLowerCase());
							listToChange = tileNames;
						}
						else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.TRUE_TILE)
						{
							trueTileNames.remove(npc.getName().toLowerCase());
							listToChange = trueTileNames;
						}
						else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.SW_TILE)
						{
							swTileNames.remove(npc.getName().toLowerCase());
							listToChange = swTileNames;
						}
						else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.SW_TRUE_TILE)
						{
							swTrueTileNames.remove(npc.getName().toLowerCase());
							listToChange = swTrueTileNames;
						}
						else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.HULL)
						{
							hullNames.remove(npc.getName().toLowerCase());
							listToChange = hullNames;
						}
						else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.AREA)
						{
							areaNames.remove(npc.getName().toLowerCase());
							listToChange = areaNames;
						}
						else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.OUTLINE)
						{
							outlineNames.remove(npc.getName().toLowerCase());
							listToChange = outlineNames;
						}
						else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.CLICKBOX)
						{
							clickboxNames.remove(npc.getName().toLowerCase());
							listToChange = clickboxNames;
						}
						else
						{
							turboNames.remove(npc.getName().toLowerCase());
							listToChange = turboNames;
						}
					}
					else
					{
						if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.TILE)
						{
							tileNames.add(npc.getName());
							listToChange = tileNames;
						}
						else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.TRUE_TILE)
						{
							trueTileNames.add(npc.getName());
							listToChange = trueTileNames;
						}
						else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.SW_TILE)
						{
							swTileNames.add(npc.getName());
							listToChange = swTileNames;
						}
						else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.SW_TRUE_TILE)
						{
							swTrueTileNames.add(npc.getName());
							listToChange = swTrueTileNames;
						}
						else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.HULL)
						{
							hullNames.add(npc.getName());
							listToChange = hullNames;
						}
						else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.AREA)
						{
							areaNames.add(npc.getName());
							listToChange = areaNames;
						}
						else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.OUTLINE)
						{
							outlineNames.add(npc.getName());
							listToChange = outlineNames;
						}
						else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.CLICKBOX)
						{
							clickboxNames.add(npc.getName());
							listToChange = clickboxNames;
						}
						else
						{
							turboNames.add(npc.getName());
							listToChange = turboNames;
						}
					}
				}

				if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.TILE)
				{
					config.setTileNames(Text.toCSV(listToChange));
				}
				else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.TRUE_TILE)
				{
					config.setTrueTileNames(Text.toCSV(listToChange));
				}
				else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.SW_TILE)
				{
					config.setSwTileNames(Text.toCSV(listToChange));
				}
				else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.SW_TRUE_TILE)
				{
					config.setSwTrueTileNames(Text.toCSV(listToChange));
				}
				else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.HULL)
				{
					config.setHullNames(Text.toCSV(listToChange));
				}
				else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.AREA)
				{
					config.setAreaNames(Text.toCSV(listToChange));
				}
				else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.OUTLINE)
				{
					config.setOutlineNames(Text.toCSV(listToChange));
				}
				else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.CLICKBOX)
				{
					config.setClickboxNames(Text.toCSV(listToChange));
				}
				else
				{
					config.setTurboNames(Text.toCSV(listToChange));
				}
				event.consume();
			}
		}
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		for (NpcSpawn n : npcSpawns)
		{
			if (event.getNpc().getIndex() == n.index && event.getNpc().getId() == n.id)
			{
				if (n.spawnPoint == null && n.diedOnTick != -1)
				{
					if (n.spawnLocations.contains(event.getNpc().getWorldLocation()))
					{
						n.spawnPoint = event.getNpc().getWorldLocation();
						n.respawnTime = client.getTickCount() - n.diedOnTick + 1;
					}
					else
					{
						n.spawnLocations.add(event.getNpc().getWorldLocation());
					}
				}
				n.dead = false;
				break;
			}
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		if (event.getNpc().isDead())
		{
			for (NpcSpawn n : npcSpawns)
			{
				if (event.getNpc().getIndex() == n.index && event.getNpc().getId() == n.id)
				{
					n.diedOnTick = client.getTickCount();
					n.dead = true;
					return;
				}
			}

			if (checkAllLists(event.getNpc()))
			{
				npcSpawns.add(new NpcSpawn(event.getNpc()));
			}
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		lastTickUpdate = Instant.now();
		turboColors.clear();
		for (int i = 0; i < turboNames.size() + turboIds.size(); i++)
		{
			turboColors.add(Color.getHSBColor(new Random().nextFloat(), 1.0F, 1.0F));
		}
		turboModeStyle = new Random().nextInt(6);
		turboTileWidth = new Random().nextInt(10) + 1;
		turboOutlineWidth = new Random().nextInt(50) + 1;
		turboOutlineFeather = new Random().nextInt(4);
	}

	public boolean checkSpecificList(ArrayList<String> strList, ArrayList<Integer> intList, NPC npc)
	{
		if (intList.contains(npc.getId()))
		{
			return true;
		}
		else if (npc.getName() != null)
		{
			String name = npc.getName().toLowerCase();
			for (String str : strList)
			{
				if (str.equalsIgnoreCase(name) || (str.contains("*")
					&& ((str.startsWith("*") && str.endsWith("*") && name.contains(str.replace("*", "")))
					|| (str.startsWith("*") && name.endsWith(str.replace("*", ""))) || name.startsWith(str.replace("*", "")))))
				{
					return true;
				}
			}
		}
		return false;
	}

	public boolean checkAllLists(NPC npc)
	{
		int id = npc.getId();
		if (tileIds.contains(id) || trueTileIds.contains(id) || swTileIds.contains(id) || swTrueTileIds.contains(id) || hullIds.contains(id)
			|| areaIds.contains(id) || outlineIds.contains(id) || clickboxIds.contains(id) || turboIds.contains(id))
		{
			return true;
		}
		else if (npc.getName() != null)
		{
			String name = npc.getName().toLowerCase();
			for (ArrayList<String> strList : new ArrayList<>(Arrays.asList(
				tileNames, trueTileNames, swTileNames, swTrueTileNames, hullNames, areaNames, outlineNames, clickboxNames, turboNames)))
			{
				for (String str : strList)
				{
					if (str.equalsIgnoreCase(name) || (str.contains("*")
						&& ((str.startsWith("*") && str.endsWith("*") && name.contains(str.replace("*", "")))
						|| (str.startsWith("*") && name.endsWith(str.replace("*", ""))) || name.startsWith(str.replace("*", "")))))
					{
						return true;
					}
				}
			}
		}
		return false;
	}

	public Color getTagColor()
	{
		if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.TILE)
		{
			return config.tileColor();
		}
		else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.TRUE_TILE)
		{
			return config.trueTileColor();
		}
		else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.SW_TILE)
		{
			return config.swTileColor();
		}
		else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.SW_TRUE_TILE)
		{
			return config.swTrueTileColor();
		}
		else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.HULL)
		{
			return config.hullColor();
		}
		else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.AREA)
		{
			return config.areaColor();
		}
		else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.OUTLINE)
		{
			return config.outlineColor();
		}
		else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.CLICKBOX)
		{
			return config.clickboxColor();
		}
		else
		{
			return Color.getHSBColor(new Random().nextFloat(), 1.0F, 1.0F);
		}
	}

	public Color getSpecificColor(NPC npc)
	{
		if (checkSpecificList(tileNames, tileIds, npc))
		{
			return config.tileColor();
		}
		else if (checkSpecificList(trueTileNames, trueTileIds, npc))
		{
			return config.trueTileColor();
		}
		else if (checkSpecificList(swTileNames, swTileIds, npc))
		{
			return config.swTileColor();
		}
		else if (checkSpecificList(swTrueTileNames, swTrueTileIds, npc))
		{
			return config.swTrueTileColor();
		}
		else if (checkSpecificList(hullNames, hullIds, npc))
		{
			return config.hullColor();
		}
		else if (checkSpecificList(areaNames, areaIds, npc))
		{
			return config.areaColor();
		}
		else if (checkSpecificList(outlineNames, outlineIds, npc))
		{
			return config.outlineColor();
		}
		else if (checkSpecificList(clickboxNames, clickboxIds, npc))
		{
			return config.clickboxColor();
		}
		else
		{
			return getTurboIndex(npc.getId(), npc.getName()) != -1 ? turboColors.get(getTurboIndex(npc.getId(), npc.getName())) : Color.WHITE;
		}
	}

	public int getTurboIndex(int id, String name)
	{
		if (turboIds.contains(id))
		{
			return turboIds.indexOf(id);
		}
		else if (name != null)
		{
			int index = turboIds.size() - 1;
			for (String str : turboNames)
			{
				if (str.equalsIgnoreCase(name) || (str.contains("*")
					&& ((str.startsWith("*") && str.endsWith("*") && name.contains(str.replace("*", "")))
					|| (str.startsWith("*") && name.endsWith(str.replace("*", ""))) || name.startsWith(str.replace("*", "")))))
				{
					return index;
				}
				index++;
			}
		}
		return -1;
	}

	private void showEpilepsyWarning()
	{
		configManager.setConfiguration(config.CONFIG_GROUP, "turboHighlight", false);
		Font font = (Font) UIManager.get("OptionPane.buttonFont");
		Object[] options = {"Okay, I accept the risk", "No, this is an affront to my eyes"};
		JLabel label = new JLabel("<html><p>Turning this on will cause any NPCs highlighted with this style to change colors and styles rapidly.</p></html>");

		if (JOptionPane.showOptionDialog(new JFrame(),
			label,
			"EPILEPSY WARNING - Occular Abhorrence",
			JOptionPane.YES_NO_OPTION,
			JOptionPane.WARNING_MESSAGE,
			null,
			options,
			options[1]) == 0)
		{
			confirmedWarning = true;
			configManager.setConfiguration(config.CONFIG_GROUP, "turboHighlight", true);
		}
		UIManager.put("OptionPane.buttonFont", font);
	}
}
