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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Provides;
import java.awt.event.KeyEvent;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.vars.InputType;
import net.runelite.client.callback.Hooks;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.NpcUtil;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.slayer.SlayerPlugin;
import net.runelite.client.plugins.slayer.SlayerPluginService;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.Text;
import net.runelite.client.util.WildcardMatcher;
import org.apache.commons.lang3.StringUtils;
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
	tags = {"npc", "highlight", "indicators", "respawn", "hide", "entity", "custom", "id", "name"}
)
@PluginDependency(SlayerPlugin.class)
public class BetterNpcHighlightPlugin extends Plugin implements KeyListener
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

	@Inject
	private PluginManager pluginManager;

	@Inject
	private Hooks hooks;

	@Inject
	private SlayerPluginService slayerPluginService;

	@Inject
	private KeyManager keyManager;

	@Inject
	private ChatMessageManager chatMessageManager;

	private static final Set<MenuAction> NPC_MENU_ACTIONS = ImmutableSet.of(MenuAction.NPC_FIRST_OPTION, MenuAction.NPC_SECOND_OPTION,
		MenuAction.NPC_THIRD_OPTION, MenuAction.NPC_FOURTH_OPTION, MenuAction.NPC_FIFTH_OPTION, MenuAction.WIDGET_TARGET_ON_NPC,
		MenuAction.ITEM_USE_ON_NPC);

	public ArrayList<String> tileNames = new ArrayList<>();
	public ArrayList<Integer> tileIds = new ArrayList<>();
	public ArrayList<String> trueTileNames = new ArrayList<>();
	public ArrayList<Integer> trueTileIds = new ArrayList<>();
	public ArrayList<String> swTileNames = new ArrayList<>();
	public ArrayList<Integer> swTileIds = new ArrayList<>();
	public ArrayList<String> swTrueTileNames = new ArrayList<>();
	public ArrayList<Integer> swTrueTileIds = new ArrayList<>();
	public ArrayList<String> hullNames = new ArrayList<>();
	public ArrayList<Integer> hullIds = new ArrayList<>();
	public ArrayList<String> areaNames = new ArrayList<>();
	public ArrayList<Integer> areaIds = new ArrayList<>();
	public ArrayList<String> outlineNames = new ArrayList<>();
	public ArrayList<Integer> outlineIds = new ArrayList<>();
	public ArrayList<String> clickboxNames = new ArrayList<>();
	public ArrayList<Integer> clickboxIds = new ArrayList<>();
	public ArrayList<String> turboNames = new ArrayList<>();
	public ArrayList<Integer> turboIds = new ArrayList<>();
	public ArrayList<Color> turboColors = new ArrayList<>();
	public ArrayList<NpcSpawn> npcSpawns = new ArrayList<>();
	public ArrayList<String> namesToDisplay = new ArrayList<>();
	public ArrayList<String> ignoreDeadExclusionList = new ArrayList<>();
	public ArrayList<Integer> ignoreDeadExclusionIDList = new ArrayList<>();
	public ArrayList<String> hiddenNames = new ArrayList<>();
	public ArrayList<Integer> hiddenIds = new ArrayList<>();
	public Instant lastTickUpdate;
	public int turboModeStyle = 0;
	public int turboTileWidth = 0;
	public int turboOutlineWidth = 0;
	public int turboOutlineFeather = 0;
	public boolean confirmedWarning = false;

	private final Hooks.RenderableDrawListener drawListener = this::shouldDraw;

	private static final String HIDE_COMMAND = "!hide";
	private static final String UNHIDE_COMMAND = "!unhide";
	private static final String TAG_COMMAND = "!tag";
	private static final String UNTAG_COMMAND = "!untag";

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
		splitIdList(config.ignoreDeadExclusionID(), ignoreDeadExclusionIDList);
		splitNameList(config.entityHiderNames(), hiddenNames);
		splitIdList(config.entityHiderIds(), hiddenIds);
		hooks.registerRenderableDrawListener(drawListener);
		keyManager.registerKeyListener(this);

		final Optional<Plugin> slayerPlugin = pluginManager.getPlugins().stream().filter(p -> p.getName().equals("Slayer")).findFirst();
		if (slayerPlugin.isPresent() && pluginManager.isPluginEnabled(slayerPlugin.get()) && config.slayerHighlight())
		{
			pluginManager.setPluginEnabled(slayerPlugin.get(), true);
		}
	}

	protected void shutDown()
	{
		reset();
		overlayManager.remove(overlay);
		overlayManager.remove(mapOverlay);
		hooks.unregisterRenderableDrawListener(drawListener);
		keyManager.unregisterKeyListener(this);
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
		turboNames.clear();
		turboIds.clear();
		hiddenNames.clear();
		hiddenIds.clear();
		ignoreDeadExclusionList.clear();
		ignoreDeadExclusionIDList.clear();
		namesToDisplay.clear();
		npcSpawns.clear();
		turboModeStyle = 0;
		turboTileWidth = 0;
		turboOutlineWidth = 0;
		turboOutlineFeather = 0;
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
				case "ignoreDeadExclusionID":
					ignoreDeadExclusionIDList.clear();
					splitIdList(config.ignoreDeadExclusionID(), ignoreDeadExclusionIDList);
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
				case "entityHiderNames":
					hiddenNames.clear();
					splitNameList(config.entityHiderNames(), hiddenNames);
					break;
				case "entityHiderIds":
					hiddenIds.clear();
					splitIdList(config.entityHiderIds(), hiddenIds);
					break;
				case "slayerHighlight":
					final Optional<Plugin> slayerPlugin = pluginManager.getPlugins().stream().filter(p -> p.getName().equals("Slayer")).findFirst();
					if (slayerPlugin.isPresent() && pluginManager.isPluginEnabled(slayerPlugin.get()) && config.slayerHighlight())
					{
						pluginManager.setPluginEnabled(slayerPlugin.get(), true);
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
			else if (config.highlightMenuNames() && npc.getName() != null && (checkAllLists(npc) ||
				(config.slayerHighlight() && slayerPluginService.getTargets().contains(npc))))
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
						option = tileNames.contains(npc.getName().toLowerCase()) ? "Untag-Tile" : "Tag-Tile";
					}
					else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.TRUE_TILE)
					{
						option = trueTileNames.contains(npc.getName().toLowerCase()) ? "Untag-True-Tile" : "Tag-True-Tile";
					}
					else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.SW_TILE)
					{
						option = swTileNames.contains(npc.getName().toLowerCase()) ? "Untag-SW-Tile" : "Tag-SW-Tile";
					}
					else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.SW_TRUE_TILE)
					{
						option = swTrueTileNames.contains(npc.getName().toLowerCase()) ? "Untag-SW-True-Tile" : "Tag-SW-True-Tile";
					}
					else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.HULL)
					{
						option = hullNames.contains(npc.getName().toLowerCase()) ? "Untag-Hull" : "Tag-Hull";
					}
					else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.AREA)
					{
						option = areaNames.contains(npc.getName().toLowerCase()) ? "Untag-Area" : "Tag-Area";
					}
					else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.OUTLINE)
					{
						option = outlineNames.contains(npc.getName().toLowerCase()) ? "Untag-Outline" : "Tag-Outline";
					}
					else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.CLICKBOX)
					{
						option = clickboxNames.contains(npc.getName().toLowerCase()) ? "Untag-Clickbox" : "Tag-Clickbox";
					}
					else
					{
						option = turboNames.contains(npc.getName().toLowerCase()) ? "Untag-Turbo" : "Tag-Turbo";
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
						String tagAllEntry;
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
				boolean tag = event.getMenuOption().contains("Tag");
				if (npc.getName() != null)
				{
					if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.TILE)
					{
						config.setTileNames(configListToString(tag, npc.getName().toLowerCase(), tileNames));
					}
					else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.TRUE_TILE)
					{
						config.setTrueTileNames(configListToString(tag, npc.getName().toLowerCase(), trueTileNames));
					}
					else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.SW_TILE)
					{
						config.setSwTileNames(configListToString(tag, npc.getName().toLowerCase(), swTileNames));
					}
					else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.SW_TRUE_TILE)
					{
						config.setSwTrueTileNames(configListToString(tag, npc.getName().toLowerCase(), swTrueTileNames));
					}
					else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.HULL)
					{
						config.setHullNames(configListToString(tag, npc.getName().toLowerCase(), hullNames));
					}
					else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.AREA)
					{
						config.setAreaNames(configListToString(tag, npc.getName().toLowerCase(), areaNames));
					}
					else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.OUTLINE)
					{
						config.setOutlineNames(configListToString(tag, npc.getName().toLowerCase(), outlineNames));
					}
					else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.CLICKBOX)
					{
						config.setClickboxNames(configListToString(tag, npc.getName().toLowerCase(), clickboxNames));
					}
					else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.TURBO)
					{
						config.setTurboNames(configListToString(tag, npc.getName().toLowerCase(), turboNames));
					}
				}
				event.consume();
			}
		}
	}

	private String configListToString(boolean tagOrHide, String name, ArrayList<String> strList)
	{
		if (tagOrHide)
		{
			if (!strList.contains(name))
			{
				strList.add(name);
			}
		}
		else
		{
			strList.remove(name);
		}
		return Text.toCSV(strList);
	}

	private String configListToString(boolean tagOrHide, int id, ArrayList<Integer> intList)
	{
		if (tagOrHide)
		{
			if (!intList.contains(id))
			{
				intList.add(id);
			}
		}
		else
		{
			intList.remove((Integer) id);
		}
		return intList.stream().map(String::valueOf).collect(Collectors.joining(","));
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
				if (WildcardMatcher.matches(str, name))
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
					if (WildcardMatcher.matches(str, name))
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
		if (slayerPluginService.getTargets().contains(npc) && config.slayerHighlight())
		{
			return config.slayerRave() ? getRaveColor(config.slayerRaveSpeed()) : config.taskColor();
		}
		else if (checkSpecificList(tileNames, tileIds, npc))
		{
			return config.tileRave() ? getRaveColor(config.tileRaveSpeed()) : config.tileColor();
		}
		else if (checkSpecificList(trueTileNames, trueTileIds, npc))
		{
			return config.trueTileRave() ? getRaveColor(config.trueTileRaveSpeed()) : config.trueTileColor();
		}
		else if (checkSpecificList(swTileNames, swTileIds, npc))
		{
			return config.swTileRave() ? getRaveColor(config.swTileRaveSpeed()) : config.swTileColor();
		}
		else if (checkSpecificList(swTrueTileNames, swTrueTileIds, npc))
		{
			return config.swTrueTileRave() ? getRaveColor(config.swTrueTileRaveSpeed()) : config.swTrueTileColor();
		}
		else if (checkSpecificList(hullNames, hullIds, npc))
		{
			return config.hullRave() ? getRaveColor(config.hullRaveSpeed()) : config.hullColor();
		}
		else if (checkSpecificList(areaNames, areaIds, npc))
		{
			return config.areaRave() ? getRaveColor(config.areaRaveSpeed()) : config.areaColor();
		}
		else if (checkSpecificList(outlineNames, outlineIds, npc))
		{
			return config.outlineRave() ? getRaveColor(config.outlineRaveSpeed()) : config.outlineColor();
		}
		else if (checkSpecificList(clickboxNames, clickboxIds, npc))
		{
			return config.clickboxRave() ? getRaveColor(config.clickboxRaveSpeed()) : config.clickboxColor();
		}
		else
		{
			return getTurboIndex(npc.getId(), npc.getName()) != -1 ? turboColors.get(getTurboIndex(npc.getId(), npc.getName())) : Color.WHITE;
		}
	}

	public Color getRaveColor(int speed)
	{
		int ticks = speed / 20;
		return Color.getHSBColor((client.getGameCycle() % ticks) / ((float) ticks), 1.0f, 1.0f);
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
				if (WildcardMatcher.matches(str, name))
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

	@VisibleForTesting
	boolean shouldDraw(Renderable renderable, boolean drawingUI)
	{
		if (renderable instanceof NPC)
		{
			NPC npc = (NPC) renderable;

			if (config.entityHiderToggle())
			{
				return !hiddenIds.contains(npc.getId()) && (npc.getName() != null && !hiddenNames.contains(npc.getName().toLowerCase()));
			}
		}
		return true;
	}

	public void keyPressed(KeyEvent e)
	{
		//Enter is pressed
		if (e.getKeyCode() == 10)
		{
			int inputType = client.getVarcIntValue(VarClientInt.INPUT_TYPE);
			if (inputType == InputType.PRIVATE_MESSAGE.getType() || inputType == InputType.NONE.getType())
			{
				int var;
				if (inputType == InputType.PRIVATE_MESSAGE.getType())
				{
					var = VarClientStr.INPUT_TEXT;
				}
				else
				{
					var = VarClientStr.CHATBOX_TYPED_TEXT;
				}

				if (client.getVarcStrValue(var) != null && !client.getVarcStrValue(var).isEmpty())
				{
					String text = client.getVarcStrValue(var).toLowerCase();
					if (config.entityHiderCommands() && (text.startsWith(HIDE_COMMAND) || text.startsWith(UNHIDE_COMMAND)))
					{
						hideNPCCommand(text, var);
					}
					else if (config.tagCommands() && (text.startsWith(TAG_COMMAND) || text.startsWith(UNTAG_COMMAND)))
					{
						tagNPCCommand(text, var);
					}
				}
			}
		}
	}

	private void hideNPCCommand(String text, int var)
	{
		String npcToHide = text.replace(text.startsWith(HIDE_COMMAND) ? HIDE_COMMAND : UNHIDE_COMMAND, "").trim();
		boolean hide = text.startsWith(HIDE_COMMAND);

		if (!npcToHide.isEmpty())
		{
			if (StringUtils.isNumeric(npcToHide))
			{
				config.setEntityHiderIds(configListToString(hide, Integer.parseInt(npcToHide), hiddenIds));
			}
			else
			{
				config.setEntityHiderNames(configListToString(hide, npcToHide, hiddenNames));
			}
		}
		else
		{
			printMessage("Please enter a valid NPC name or ID!");
		}

		//Set typed text to nothing
		client.setVarcStrValue(var, "");
	}

	private void tagNPCCommand(String text, int var)
	{
		if (text.trim().equals(TAG_COMMAND) || text.trim().equals(UNTAG_COMMAND))
		{
			printMessage("Please enter a tag abbreviation followed by a valid NPC name or ID!");
		}
		else if (text.contains(TAG_COMMAND + " ") || text.contains(UNTAG_COMMAND + " "))
		{
			printMessage("Please enter a valid tag abbreviation!");
		}
		else if (!text.trim().contains(" "))
		{
			printMessage("Please enter a valid NPC name or ID!");
		}
		else
		{
			String npcToTag = text.substring(text.indexOf(" ") + 1).toLowerCase().trim();
			boolean tag = text.startsWith(TAG_COMMAND);

			if (!npcToTag.isEmpty())
			{
				if (validateCommand(text, "t ") || validateCommand(text, "tile "))
				{
					if (StringUtils.isNumeric(npcToTag))
					{
						config.setTileIds(configListToString(tag, Integer.parseInt(npcToTag), tileIds));
					}
					else
					{
						config.setTileNames(configListToString(tag, npcToTag, tileNames));
					}
				}
				else if (validateCommand(text, "tt ") || validateCommand(text, "truetile "))
				{
					if (StringUtils.isNumeric(npcToTag))
					{
						config.setTrueTileIds(configListToString(tag, Integer.parseInt(npcToTag), trueTileIds));
					}
					else
					{
						config.setTrueTileNames(configListToString(tag, npcToTag, trueTileNames));
					}
				}
				else if (validateCommand(text, "sw ") || validateCommand(text, "swt ")
					|| validateCommand(text, "southwesttile ") || validateCommand(text, "southwest ")
					|| validateCommand(text, "swtile ") || validateCommand(text, "southwestt "))
				{
					if (StringUtils.isNumeric(npcToTag))
					{
						config.setSwTileIds(configListToString(tag, Integer.parseInt(npcToTag), swTileIds));
					}
					else
					{
						config.setSwTileNames(configListToString(tag, npcToTag, swTileNames));
					}
				}
				else if (validateCommand(text, "swtt ") || validateCommand(text, "swtruetile ")
					|| validateCommand(text, "southwesttruetile ") || validateCommand(text, "southwesttt "))
				{
					if (StringUtils.isNumeric(npcToTag))
					{
						config.setSwTrueTileIds(configListToString(tag, Integer.parseInt(npcToTag), swTrueTileIds));
					}
					else
					{
						config.setSwTrueTileNames(configListToString(tag, npcToTag, swTrueTileNames));
					}
				}
				else if (validateCommand(text, "h ") || validateCommand(text, "hull "))
				{
					if (StringUtils.isNumeric(npcToTag))
					{
						config.setHullIds(configListToString(tag, Integer.parseInt(npcToTag), hullIds));
					}
					else
					{
						config.setHullNames(configListToString(tag, npcToTag, hullNames));
					}
				}
				else if (validateCommand(text, "a ") || validateCommand(text, "area "))
				{
					if (StringUtils.isNumeric(npcToTag))
					{
						config.setAreaIds(configListToString(tag, Integer.parseInt(npcToTag), areaIds));
					}
					else
					{
						config.setAreaNames(configListToString(tag, npcToTag, areaNames));
					}
				}
				else if (validateCommand(text, "o ") || validateCommand(text, "outline "))
				{
					if (StringUtils.isNumeric(npcToTag))
					{
						config.setOutlineIds(configListToString(tag, Integer.parseInt(npcToTag), outlineIds));
					}
					else
					{
						config.setOutlineNames(configListToString(tag, npcToTag, outlineNames));
					}
				}
				else if (validateCommand(text, "c ") || validateCommand(text, "clickbox ") || validateCommand(text, "box "))
				{
					if (StringUtils.isNumeric(npcToTag))
					{
						config.setClickboxIds(configListToString(tag, Integer.parseInt(npcToTag), clickboxIds));
					}
					else
					{
						config.setClickboxNames(configListToString(tag, npcToTag, clickboxNames));
					}
				}
				else if (validateCommand(text, "tu ") || validateCommand(text, "turbo "))
				{
					if (StringUtils.isNumeric(npcToTag))
					{
						config.setTurboIds(configListToString(tag, Integer.parseInt(npcToTag), turboIds));
					}
					else
					{
						config.setTurboNames(configListToString(tag, npcToTag, turboNames));
					}
				}
			}
		}
		//Set typed text to nothing
		client.setVarcStrValue(var, "");
	}

	public boolean validateCommand(String command, String type)
	{
		return command.startsWith(TAG_COMMAND + type) || command.startsWith(UNTAG_COMMAND + type);
	}

	public void printMessage(String msg)
	{
		final ChatMessageBuilder message = new ChatMessageBuilder()
			.append(ChatColorType.HIGHLIGHT)
			.append(msg);

		chatMessageManager.queue(QueuedMessage.builder()
			.type(ChatMessageType.CONSOLE)
			.runeLiteFormattedMessage(message.build())
			.build());
	}

	public void keyReleased(KeyEvent e)
	{
	}

	public void keyTyped(KeyEvent e)
	{
	}
}
