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
import java.util.List;
import java.util.Optional;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
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

	public ArrayList<NPCInfo> npcList = new ArrayList<>();
	public String currentTask = "";
	//Lists
	public ArrayList<String> tileNames = new ArrayList<>();
	public ArrayList<String> tileIds = new ArrayList<>();
	public ArrayList<String> trueTileNames = new ArrayList<>();
	public ArrayList<String> trueTileIds = new ArrayList<>();
	public ArrayList<String> swTileNames = new ArrayList<>();
	public ArrayList<String> swTileIds = new ArrayList<>();
	public ArrayList<String> swTrueTileNames = new ArrayList<>();
	public ArrayList<String> swTrueTileIds = new ArrayList<>();
	public ArrayList<String> hullNames = new ArrayList<>();
	public ArrayList<String> hullIds = new ArrayList<>();
	public ArrayList<String> areaNames = new ArrayList<>();
	public ArrayList<String> areaIds = new ArrayList<>();
	public ArrayList<String> outlineNames = new ArrayList<>();
	public ArrayList<String> outlineIds = new ArrayList<>();
	public ArrayList<String> clickboxNames = new ArrayList<>();
	public ArrayList<String> clickboxIds = new ArrayList<>();
	public ArrayList<String> turboNames = new ArrayList<>();
	public ArrayList<String> turboIds = new ArrayList<>();
	public ArrayList<Color> turboColors = new ArrayList<>();
	public ArrayList<NpcSpawn> npcSpawns = new ArrayList<>();
	public ArrayList<String> namesToDisplay = new ArrayList<>();
	public ArrayList<String> ignoreDeadExclusionList = new ArrayList<>();
	public ArrayList<String> ignoreDeadExclusionIDList = new ArrayList<>();
	public ArrayList<String> hiddenNames = new ArrayList<>();
	public ArrayList<String> hiddenIds = new ArrayList<>();
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
		splitList(config.tileNames(), tileNames);
		splitList(config.tileIds(), tileIds);
		splitList(config.trueTileNames(), trueTileNames);
		splitList(config.trueTileIds(), trueTileIds);
		splitList(config.swTileNames(), swTileNames);
		splitList(config.swTileIds(), swTileIds);
		splitList(config.swTrueTileNames(), swTrueTileNames);
		splitList(config.swTrueTileIds(), swTrueTileIds);
		splitList(config.hullNames(), hullNames);
		splitList(config.hullIds(), hullIds);
		splitList(config.areaNames(), areaNames);
		splitList(config.areaIds(), areaIds);
		splitList(config.outlineNames(), outlineNames);
		splitList(config.outlineIds(), outlineIds);
		splitList(config.clickboxNames(), clickboxNames);
		splitList(config.clickboxIds(), clickboxIds);
		splitList(config.turboNames(), turboNames);
		splitList(config.turboIds(), turboIds);
		splitList(config.displayName(), namesToDisplay);
		splitList(config.ignoreDeadExclusion(), ignoreDeadExclusionList);
		splitList(config.ignoreDeadExclusionID(), ignoreDeadExclusionIDList);
		splitList(config.entityHiderNames(), hiddenNames);
		splitList(config.entityHiderIds(), hiddenIds);
		hooks.registerRenderableDrawListener(drawListener);
		keyManager.registerKeyListener(this);

		final Optional<Plugin> slayerPlugin = pluginManager.getPlugins().stream().filter(p -> p.getName().equals("Slayer")).findFirst();
		if (slayerPlugin.isPresent() && pluginManager.isPluginEnabled(slayerPlugin.get()) && config.slayerHighlight())
		{
			pluginManager.setPluginEnabled(slayerPlugin.get(), true);
		}

		if (client.getGameState() == GameState.LOGGED_IN)
		{
			recreateList();
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
		npcList.clear();
		currentTask = "";
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

	private void splitList(String configStr, ArrayList<String> strList)
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

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals(config.CONFIG_GROUP))
		{
			switch (event.getKey())
			{
				case "tileNames":
					tileNames.clear();
					splitList(config.tileNames(), tileNames);
					recreateList();
					break;
				case "tileIds":
					tileIds.clear();
					splitList(config.tileIds(), tileIds);
					recreateList();
					break;
				case "trueTileNames":
					trueTileNames.clear();
					splitList(config.trueTileNames(), trueTileNames);
					recreateList();
					break;
				case "trueTileIds":
					trueTileIds.clear();
					splitList(config.trueTileIds(), trueTileIds);
					recreateList();
					break;
				case "swTileNames":
					swTileNames.clear();
					splitList(config.swTileNames(), swTileNames);
					recreateList();
					break;
				case "swTileIds":
					swTileIds.clear();
					splitList(config.swTileIds(), swTileIds);
					recreateList();
					break;
				case "swTrueTileNames":
					swTrueTileNames.clear();
					splitList(config.swTrueTileNames(), swTrueTileNames);
					recreateList();
					break;
				case "swTrueTileIds":
					swTrueTileIds.clear();
					splitList(config.swTrueTileIds(), swTrueTileIds);
					recreateList();
					break;
				case "hullNames":
					hullNames.clear();
					splitList(config.hullNames(), hullNames);
					recreateList();
					break;
				case "hullIds":
					hullIds.clear();
					splitList(config.hullIds(), hullIds);
					recreateList();
					break;
				case "areaNames":
					areaNames.clear();
					splitList(config.areaNames(), areaNames);
					recreateList();
					break;
				case "areaIds":
					areaIds.clear();
					splitList(config.areaIds(), areaIds);
					recreateList();
					break;
				case "outlineNames":
					outlineNames.clear();
					splitList(config.outlineNames(), outlineNames);
					recreateList();
					break;
				case "outlineIds":
					outlineIds.clear();
					splitList(config.outlineIds(), outlineIds);
					recreateList();
					break;
				case "clickboxNames":
					clickboxNames.clear();
					splitList(config.clickboxNames(), clickboxNames);
					recreateList();
					break;
				case "clickboxIds":
					clickboxIds.clear();
					splitList(config.clickboxIds(), clickboxIds);
					recreateList();
					break;
				case "turboNames":
					turboNames.clear();
					splitList(config.turboNames(), turboNames);
					recreateList();
					break;
				case "turboIds":
					turboIds.clear();
					splitList(config.turboIds(), turboIds);
					recreateList();
					break;
				case "displayName":
					namesToDisplay.clear();
					splitList(config.displayName(), namesToDisplay);
					break;
				case "ignoreDeadExclusion":
					ignoreDeadExclusionList.clear();
					splitList(config.ignoreDeadExclusion(), ignoreDeadExclusionList);
					recreateList();
					break;
				case "ignoreDeadExclusionID":
					ignoreDeadExclusionIDList.clear();
					splitList(config.ignoreDeadExclusionID(), ignoreDeadExclusionIDList);
					recreateList();
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
					splitList(config.entityHiderNames(), hiddenNames);
					break;
				case "entityHiderIds":
					hiddenIds.clear();
					splitList(config.entityHiderIds(), hiddenIds);
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
			npcList.clear();
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
			else if (config.highlightMenuNames() && npc.getName() != null)
			{
				for (NPCInfo npcInfo : npcList)
				{
					if (npcInfo.getNpc() == npc)
					{
						color = getSpecificColor(npcInfo);
						break;
					}
				}
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
						option = checkSpecificNameList(tileNames, npc) ? "Untag-Tile" : "Tag-Tile";
					}
					else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.TRUE_TILE)
					{
						option = checkSpecificNameList(trueTileNames, npc) ? "Untag-True-Tile" : "Tag-True-Tile";
					}
					else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.SW_TILE)
					{
						option = checkSpecificNameList(swTileNames, npc) ? "Untag-SW-Tile" : "Tag-SW-Tile";
					}
					else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.SW_TRUE_TILE)
					{
						option = checkSpecificNameList(swTrueTileNames, npc) ? "Untag-SW-True-Tile" : "Tag-SW-True-Tile";
					}
					else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.HULL)
					{
						option = checkSpecificNameList(hullNames, npc) ? "Untag-Hull" : "Tag-Hull";
					}
					else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.AREA)
					{
						option = checkSpecificNameList(areaNames, npc) ? "Untag-Area" : "Tag-Area";
					}
					else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.OUTLINE)
					{
						option = checkSpecificNameList(outlineNames, npc) ? "Untag-Outline" : "Tag-Outline";
					}
					else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.CLICKBOX)
					{
						option = checkSpecificNameList(clickboxNames, npc) ? "Untag-Clickbox" : "Tag-Clickbox";
					}
					else
					{
						option = checkSpecificNameList(turboNames, npc) ? "Untag-Turbo" : "Tag-Turbo";
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

						int idx = -1;
						MenuEntry parent = client.createMenuEntry(idx)
							.setOption(option)
							.setTarget(tagAllEntry)
							.setIdentifier(event.getIdentifier())
							.setParam0(event.getActionParam0())
							.setParam1(event.getActionParam1())
							.setType(config.presetColorAmount() == BetterNpcHighlightConfig.presetColorAmount.ZERO ? MenuAction.RUNELITE : MenuAction.RUNELITE_SUBMENU)
							.onClick(this::tagNPC);

						if (parent != null)
						{
							customColorTag(idx, npc, parent);
						}
					}
				}
			}
		}
	}

	public void tagNPC(MenuEntry event)
	{
		//Submenu if there are more than 1 preset colors selected, RuneLite if no preset colors
		if (event.getType() == MenuAction.RUNELITE_SUBMENU || event.getType() == MenuAction.RUNELITE)
		{
			if ((event.getOption().contains("Tag") || event.getOption().contains("Untag")) && (event.getOption().contains("-Tile")
				|| event.getOption().contains("-True-Tile") || event.getOption().contains("-SW-Tile") || event.getOption().contains("-SW-True-Tile")
				|| event.getOption().contains("-Hull") || event.getOption().contains("-Area") || event.getOption().contains("-Outline")
				|| event.getOption().contains("-Clickbox") || event.getOption().contains("-Turbo")))
			{
				final int id = event.getIdentifier();
				final NPC npc = client.getCachedNPCs()[id];
				boolean tag = event.getOption().contains("Tag");
				if (npc.getName() != null)
				{
					updateListConfig(tag, npc.getName().toLowerCase(), 0);
				}
			}
		}
	}

	private void customColorTag(int idx, NPC npc, MenuEntry parent)
	{
		List<Color> colors = new ArrayList<>();
		// add X amount of preset colors based off of config
		if (config.presetColorAmount() != BetterNpcHighlightConfig.presetColorAmount.ZERO)
		{
			if (config.presetColorAmount() == BetterNpcHighlightConfig.presetColorAmount.ONE)
			{
				colors.add(config.presetColor1());
			}
			else if (config.presetColorAmount() == BetterNpcHighlightConfig.presetColorAmount.TWO)
			{
				colors.add(config.presetColor1());
				colors.add(config.presetColor2());
			}
			else if (config.presetColorAmount() == BetterNpcHighlightConfig.presetColorAmount.THREE)
			{
				colors.add(config.presetColor1());
				colors.add(config.presetColor2());
				colors.add(config.presetColor3());
			}
			else if (config.presetColorAmount() == BetterNpcHighlightConfig.presetColorAmount.FOUR)
			{
				colors.add(config.presetColor1());
				colors.add(config.presetColor2());
				colors.add(config.presetColor3());
				colors.add(config.presetColor4());
			}
			else if (config.presetColorAmount() == BetterNpcHighlightConfig.presetColorAmount.FIVE)
			{
				colors.add(config.presetColor1());
				colors.add(config.presetColor2());
				colors.add(config.presetColor3());
				colors.add(config.presetColor4());
				colors.add(config.presetColor5());
			}

			if (colors.size() > 0)
			{
				int index = 1;
				for (final Color c : colors)
				{
					if (c != null)
					{
						int preset = index;
						client.createMenuEntry(idx--)
							.setOption(ColorUtil.prependColorTag("Preset color " + index, c))
							.setType(MenuAction.RUNELITE)
							.setParent(parent)
							.onClick(e ->
							{
								if (npc.getName() != null)
								{
									updateListConfig(true, npc.getName().toLowerCase(), preset);
								}
							});
						index++;
					}
				}
			}
		}

		for (NPCInfo n : npcList)
		{
			if (n.getNpc() == npc)
			{
				client.createMenuEntry(idx--)
					.setOption("Reset color")
					.setType(MenuAction.RUNELITE)
					.setParent(parent)
					.onClick(e ->
					{
						if (npc.getName() != null)
						{
							updateListConfig(true, npc.getName().toLowerCase(), 0);
						}
					});
				break;
			}
		}
	}

	private void updateListConfig(boolean add, String name, int preset)
	{
		if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.TILE)
		{
			config.setTileNames(configListToString(add, name, tileNames, preset));
		}
		else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.TRUE_TILE)
		{
			config.setTrueTileNames(configListToString(add, name, trueTileNames, preset));
		}
		else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.SW_TILE)
		{
			config.setSwTileNames(configListToString(add, name, swTileNames, preset));
		}
		else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.SW_TRUE_TILE)
		{
			config.setSwTrueTileNames(configListToString(add, name, swTrueTileNames, preset));
		}
		else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.HULL)
		{
			config.setHullNames(configListToString(add, name, hullNames, preset));
		}
		else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.AREA)
		{
			config.setAreaNames(configListToString(add, name, areaNames, preset));
		}
		else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.OUTLINE)
		{
			config.setOutlineNames(configListToString(add, name, outlineNames, preset));
		}
		else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.CLICKBOX)
		{
			config.setClickboxNames(configListToString(add, name, clickboxNames, preset));
		}
		else if (config.tagStyleMode() == BetterNpcHighlightConfig.tagStyleMode.TURBO)
		{
			config.setTurboNames(configListToString(add, name, turboNames, 0));
		}
	}

	private String configListToString(boolean tagOrHide, String name, ArrayList<String> strList, int preset)
	{
		if (tagOrHide)
		{
			boolean foundName = false;
			String newName = preset > 0 ? name + ":" + preset : name;
			for (String str : strList)
			{
				if (str.startsWith(name + ":") || str.equalsIgnoreCase(name))
				{
					strList.set(strList.indexOf(str), newName);
					foundName = true;
				}
			}

			if (!foundName)
			{
				strList.add(newName);
			}
		}
		else
		{
			strList.removeIf(str -> str.toLowerCase().startsWith(name + ":") || str.equalsIgnoreCase(name));
		}
		return Text.toCSV(strList);
	}

	@Subscribe(priority = -1)
	public void onNpcSpawned(NpcSpawned event)
	{
		NPC npc = event.getNpc();

		for (NpcSpawn n : npcSpawns)
		{
			if (npc.getIndex() == n.index && npc.getId() == n.id)
			{
				if (n.spawnPoint == null && n.diedOnTick != -1)
				{
					WorldPoint wp = client.isInInstancedRegion() ? WorldPoint.fromLocalInstance(client, npc.getLocalLocation()) : WorldPoint.fromLocal(client, npc.getLocalLocation());
					if (n.spawnLocations.contains(wp))
					{
						n.spawnPoint = wp;
						n.respawnTime = client.getTickCount() - n.diedOnTick + 1;
					}
					else
					{
						n.spawnLocations.add(wp);
					}
				}
				n.dead = false;
				break;
			}
		}

		NPCInfo npcInfo = checkValidNPC(npc);
		if (npcInfo != null)
		{
			npcList.add(npcInfo);
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		NPC npc = event.getNpc();

		if (npc.isDead())
		{
			if (npcList.stream().anyMatch(n -> n.getNpc() == npc) && npcSpawns.stream().noneMatch(n -> n.index == npc.getIndex()))
			{
				npcSpawns.add(new NpcSpawn(npc));
			}
			else
			{
				for (NpcSpawn n : npcSpawns)
				{
					if (npc.getIndex() == n.index && npc.getId() == n.id)
					{
						n.diedOnTick = client.getTickCount();
						n.dead = true;
						break;
					}
				}
			}
		}

		npcList.removeIf(n -> n.getNpc().getIndex() == npc.getIndex());
	}

	@Subscribe(priority = -1)
	public void onNpcChanged(NpcChanged event)
	{
		NPC npc = event.getNpc();

		npcList.removeIf(n -> n.getNpc().getIndex() == npc.getIndex());

		NPCInfo npcInfo = checkValidNPC(npc);
		if (npcInfo != null)
		{
			npcList.add(npcInfo);
		}
	}

	@Subscribe(priority = -1)
	public void onGameTick(GameTick event)
	{
		if (!currentTask.equals(slayerPluginService.getTask()))
		{
			recreateList();
		}

		lastTickUpdate = Instant.now();
		turboColors.clear();
		for (int i = 0; i < npcList.size(); i++)
		{
			turboColors.add(Color.getHSBColor(new Random().nextFloat(), 1.0F, 1.0F));
		}
		turboModeStyle = new Random().nextInt(6);
		turboTileWidth = new Random().nextInt(10) + 1;
		turboOutlineWidth = new Random().nextInt(50) + 1;
		turboOutlineFeather = new Random().nextInt(4);
	}

	public void recreateList()
	{
		if (client.getGameState() == GameState.LOGGED_IN && client.getLocalPlayer() != null
			&& client.getLocalPlayer().getPlayerComposition() != null)
		{
			npcList.clear();
			for (NPC npc : client.getNpcs())
			{
				NPCInfo npcInfo = checkValidNPC(npc);
				if (npcInfo != null)
				{
					npcList.add(npcInfo);
				}
			}
			currentTask = slayerPluginService.getTask();
		}
	}

	public NPCInfo checkValidNPC(NPC npc)
	{
		NPCInfo n = new NPCInfo(npc, this, slayerPluginService, config);
		if (n.getTile().isHighlight() || n.getTrueTile().isHighlight() || n.getSwTile().isHighlight() || n.getSwTrueTile().isHighlight() || n.getHull().isHighlight()
			|| n.getArea().isHighlight() || n.getOutline().isHighlight() || n.getClickbox().isHighlight() || n.getTurbo().isHighlight() || n.isTask())
		{
			return n;
		}
		return null;
	}

	public HighlightColor checkSpecificList(ArrayList<String> strList, ArrayList<String> idList, NPC npc, Color configColor, Color configFillColor)
	{
		for (String entry : idList)
		{
			int id = -1;
			String preset = "";
			if (entry.contains(":"))
			{
				String[] strArr = entry.split(":");
				if (StringUtils.isNumeric(strArr[0]))
				{
					id = Integer.parseInt(strArr[0]);
				}
				preset = strArr[1];
			}
			else if (StringUtils.isNumeric(entry))
			{
				id = Integer.parseInt(entry);
			}

			if (id == npc.getId())
			{
				return new HighlightColor(true, getHighlightColor(preset, configColor), getHighlightFillColor(preset, configFillColor));
			}
		}

		if (npc.getName() != null)
		{
			String name = npc.getName().toLowerCase();
			for (String entry : strList)
			{
				String nameStr = entry;
				String preset = "";
				if (entry.contains(":"))
				{
					String[] strArr = entry.split(":");
					nameStr = strArr[0];
					preset = strArr[1];
				}

				if (WildcardMatcher.matches(nameStr, name))
				{
					return new HighlightColor(true, getHighlightColor(preset, configColor), getHighlightFillColor(preset, configFillColor));
				}
			}
		}
		return new HighlightColor(false, configColor, configFillColor);
	}

	public boolean checkSpecificNameList(ArrayList<String> strList, NPC npc)
	{
		if (npc.getName() != null)
		{
			String name = npc.getName().toLowerCase();
			for (String entry : strList)
			{
				String nameStr = entry;
				if (entry.contains(":"))
				{
					String[] strArr = entry.split(":");
					nameStr = strArr[0];
				}

				if (WildcardMatcher.matches(nameStr, name))
				{
					return true;
				}
			}
		}
		return false;
	}

	public boolean checkSpecificIdList(ArrayList<String> strList, NPC npc)
	{
		int id = npc.getId();
		for (String entry : strList)
		{
			if (StringUtils.isNumeric(entry) && Integer.parseInt(entry) == id)
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Color of the tag menu (ex. "Tag-Hull")
	 *
	 * @return Color
	 */
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

	/**
	 * Color of the NPC in the list
	 * Used for Minimap dot and displayed names
	 *
	 * @return Color
	 */
	public Color getSpecificColor(NPCInfo n)
	{
		if (n.isTask() && config.slayerHighlight())
		{
			return config.slayerRave() ? getRaveColor(config.slayerRaveSpeed()) : config.taskColor();
		}
		else if (n.getTile().isHighlight())
		{
			return config.tileRave() ? getRaveColor(config.tileRaveSpeed()) : n.getTile().getColor();
		}
		else if (n.getTrueTile().isHighlight())
		{
			return config.trueTileRave() ? getRaveColor(config.trueTileRaveSpeed()) : n.getTrueTile().getColor();
		}
		else if (n.getSwTile().isHighlight())
		{
			return config.swTileRave() ? getRaveColor(config.swTileRaveSpeed()) : n.getSwTile().getColor();
		}
		else if (n.getSwTrueTile().isHighlight())
		{
			return config.swTrueTileRave() ? getRaveColor(config.swTrueTileRaveSpeed()) : n.getSwTrueTile().getColor();
		}
		else if (n.getHull().isHighlight())
		{
			return config.hullRave() ? getRaveColor(config.hullRaveSpeed()) : n.getHull().getColor();
		}
		else if (n.getArea().isHighlight())
		{
			return config.areaRave() ? getRaveColor(config.areaRaveSpeed()) : n.getArea().getColor();
		}
		else if (n.getOutline().isHighlight())
		{
			return config.outlineRave() ? getRaveColor(config.outlineRaveSpeed()) : n.getOutline().getColor();
		}
		else if (n.getClickbox().isHighlight())
		{
			return config.clickboxRave() ? getRaveColor(config.clickboxRaveSpeed()) : n.getClickbox().getColor();
		}
		else
		{
			return getTurboIndex(n.getNpc().getId(), n.getNpc().getName()) != -1 ? turboColors.get(getTurboIndex(n.getNpc().getId(), n.getNpc().getName())) : Color.WHITE;
		}
	}

	/**
	 * Returns color of either the config or a preset if selected
	 *
	 * @return Color
	 */
	public Color getHighlightColor(String preset, Color color)
	{
		switch (preset)
		{
			case "1":
				return config.presetColor1();
			case "2":
				return config.presetColor2();
			case "3":
				return config.presetColor3();
			case "4":
				return config.presetColor4();
			case "5":
				return config.presetColor5();
		}

		return color;
	}

	/**
	 * Returns fill color of either the config or a preset if selected
	 *
	 * @return Color
	 */
	public Color getHighlightFillColor(String preset, Color color)
	{
		switch (preset)
		{
			case "1":
				return config.presetFillColor1();
			case "2":
				return config.presetFillColor2();
			case "3":
				return config.presetFillColor3();
			case "4":
				return config.presetFillColor4();
			case "5":
				return config.presetFillColor5();
		}

		return color;
	}

	public Color getRaveColor(int speed)
	{
		int ticks = speed / 20;
		return Color.getHSBColor((client.getGameCycle() % ticks) / ((float) ticks), 1.0f, 1.0f);
	}

	public int getTurboIndex(int id, String name)
	{
		if (turboIds.contains(String.valueOf(id)))
		{
			return turboIds.indexOf(String.valueOf(id));
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
				return !hiddenIds.contains(String.valueOf(npc.getId())) && (npc.getName() != null && !hiddenNames.contains(npc.getName().toLowerCase()));
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
				config.setEntityHiderIds(configListToString(hide, npcToHide, hiddenIds, 0));
			}
			else
			{
				config.setEntityHiderNames(configListToString(hide, npcToHide, hiddenNames, 0));
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
			int preset = 0;
			if (npcToTag.contains(":"))
			{
				String[] strArr = npcToTag.split(":");
				npcToTag = strArr[0];
				if (StringUtils.isNumeric(strArr[1]))
					preset = Integer.parseInt(strArr[1]);
			}
			boolean tag = text.startsWith(TAG_COMMAND);

			if (!npcToTag.isEmpty())
			{
				if (validateCommand(text, "t ") || validateCommand(text, "tile "))
				{
					if (StringUtils.isNumeric(npcToTag))
					{
						config.setTileIds(configListToString(tag, npcToTag, tileIds, preset));
					}
					else
					{
						config.setTileNames(configListToString(tag, npcToTag, tileNames, preset));
					}
				}
				else if (validateCommand(text, "tt ") || validateCommand(text, "truetile "))
				{
					if (StringUtils.isNumeric(npcToTag))
					{
						config.setTrueTileIds(configListToString(tag, npcToTag, trueTileIds, preset));
					}
					else
					{
						config.setTrueTileNames(configListToString(tag, npcToTag, trueTileNames, preset));
					}
				}
				else if (validateCommand(text, "sw ") || validateCommand(text, "swt ")
					|| validateCommand(text, "southwesttile ") || validateCommand(text, "southwest ")
					|| validateCommand(text, "swtile ") || validateCommand(text, "southwestt "))
				{
					if (StringUtils.isNumeric(npcToTag))
					{
						config.setSwTileIds(configListToString(tag, npcToTag, swTileIds, preset));
					}
					else
					{
						config.setSwTileNames(configListToString(tag, npcToTag, swTileNames, preset));
					}
				}
				else if (validateCommand(text, "swtt ") || validateCommand(text, "swtruetile ")
					|| validateCommand(text, "southwesttruetile ") || validateCommand(text, "southwesttt "))
				{
					if (StringUtils.isNumeric(npcToTag))
					{
						config.setSwTrueTileIds(configListToString(tag, npcToTag, swTrueTileIds, preset));
					}
					else
					{
						config.setSwTrueTileNames(configListToString(tag, npcToTag, swTrueTileNames, preset));
					}
				}
				else if (validateCommand(text, "h ") || validateCommand(text, "hull "))
				{
					if (StringUtils.isNumeric(npcToTag))
					{
						config.setHullIds(configListToString(tag, npcToTag, hullIds, preset));
					}
					else
					{
						config.setHullNames(configListToString(tag, npcToTag, hullNames, preset));
					}
				}
				else if (validateCommand(text, "a ") || validateCommand(text, "area "))
				{
					if (StringUtils.isNumeric(npcToTag))
					{
						config.setAreaIds(configListToString(tag, npcToTag, areaIds, preset));
					}
					else
					{
						config.setAreaNames(configListToString(tag, npcToTag, areaNames, preset));
					}
				}
				else if (validateCommand(text, "o ") || validateCommand(text, "outline "))
				{
					if (StringUtils.isNumeric(npcToTag))
					{
						config.setOutlineIds(configListToString(tag, npcToTag, outlineIds, preset));
					}
					else
					{
						config.setOutlineNames(configListToString(tag, npcToTag, outlineNames, preset));
					}
				}
				else if (validateCommand(text, "c ") || validateCommand(text, "clickbox ") || validateCommand(text, "box "))
				{
					if (StringUtils.isNumeric(npcToTag))
					{
						config.setClickboxIds(configListToString(tag, npcToTag, clickboxIds, preset));
					}
					else
					{
						config.setClickboxNames(configListToString(tag, npcToTag, clickboxNames, preset));
					}
				}
				else if (validateCommand(text, "tu ") || validateCommand(text, "turbo "))
				{
					if (StringUtils.isNumeric(npcToTag))
					{
						config.setTurboIds(configListToString(tag, npcToTag, turboIds, preset));
					}
					else
					{
						config.setTurboNames(configListToString(tag, npcToTag, turboNames, preset));
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
