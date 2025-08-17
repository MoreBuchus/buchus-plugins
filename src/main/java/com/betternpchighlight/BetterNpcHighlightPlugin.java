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



import com.betternpchighlight.data.NpcHighlightEntry;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Provides;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.*;
import javax.swing.*;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.callback.Hooks;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.NpcUtil;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginInstantiationException;
import net.runelite.client.plugins.slayer.SlayerPlugin;
import net.runelite.client.plugins.slayer.SlayerPluginService;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.WildcardMatcher;
import net.runelite.client.util.Text;

import org.apache.commons.lang3.StringUtils;
import javax.inject.Inject;
import java.awt.*;
import java.time.Instant;
import java.util.List;

@Slf4j
@PluginDescriptor(
		name = "Better NPC Highlight",
		description = "A more customizable NPC highlight with modern GUI",
		tags = {"npc", "highlight", "indicators", "custom", "gui", "table"}
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
	private ClientThread clientThread;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ColorPickerManager colorPickerManager;


	private NavigationButton panelButton;
	private BetterNpcHighlightPanel panel;

	// Core data
	public ArrayList<NPCInfo> npcList = new ArrayList<>();
	public String currentTask = "";
	public ArrayList<NpcSpawn> npcSpawns = new ArrayList<>();
	public Instant lastTickUpdate;

	// Turbo mode state
	public int turboModeStyle = 0;
	public int turboTileWidth = 0;
	public int turboOutlineWidth = 0;
	public int turboOutlineFeather = 0;
	public ArrayList<Color> turboColors = new ArrayList<>();
	public boolean confirmedWarning = false;

	private final Hooks.RenderableDrawListener drawListener = this::shouldDraw;

	@Provides
	BetterNpcHighlightConfig providesConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BetterNpcHighlightConfig.class);
	}

	protected void startUp()
	{
		clientThread.invokeLater(() -> {
			reset();
			overlayManager.add(overlay);
			overlayManager.add(mapOverlay);
			hooks.registerRenderableDrawListener(drawListener);
			keyManager.registerKeyListener(this);

			// Initialize panel
			panel = new BetterNpcHighlightPanel(colorPickerManager, configManager, this);

            panel.setOnTableChanged(() -> {
                // Save the panel data to config


                // Recreate NPC list for plugin logic
                                recreateList();

                                // Add logging for debugging
                log.info("Panel data changed, config saved.");
            });

			// Create navigation button
			panelButton = NavigationButton.builder()
					.tooltip("Better NPC Highlight")
					.icon(ImageUtil.getResourceStreamFromClass(getClass(), "/icon.png"))
					.priority(5)
					.panel(panel)
					.build();
			clientToolbar.addNavigation(panelButton);
            panel.loadAllCards(configManager, config.CONFIG_GROUP);
            System.out.println("Panel data loaded.");

			if (client.getGameState() == GameState.LOGGED_IN)
			{
				recreateList();
			}
		});
	}

	protected void shutDown()
	{
		clientThread.invokeLater(() -> {
			// Save panel data to config
			if (panel != null) {
				panel.saveAllCards(configManager, config.CONFIG_GROUP);
                System.out.println("Panel data saved.");
			}

			reset();
			overlayManager.remove(overlay);
			overlayManager.remove(mapOverlay);
			hooks.unregisterRenderableDrawListener(drawListener);
			keyManager.unregisterKeyListener(this);

			if (panelButton != null) {
				clientToolbar.removeNavigation(panelButton);
			}
		});
	}

	private void reset()
	{
		npcList.clear();
		currentTask = "";
		npcSpawns.clear();
		turboColors.clear();
		turboModeStyle = 0;
		turboTileWidth = 0;
		turboOutlineWidth = 0;
		turboOutlineFeather = 0;
		confirmedWarning = false;
	}

	/**
	 * Main method to recreate the NPC list from panel data
	 */
	public void recreateList()
	{
		clientThread.invokeLater(() -> {
			if (client.getGameState() == GameState.LOGGED_IN && client.getLocalPlayer() != null
					&& client.getLocalPlayer().getPlayerComposition() != null) {

				npcList.clear();

				if (panel != null) {
					List<NpcHighlightEntry> entries = panel.getNpcHighlightEntries();

					for (NPC npc : client.getNpcs()) {
						NPCInfo info = buildNpcInfoFromPanelEntries(npc, entries);
						if (info != null && (info.hasAnyHighlight() || info.isHideNpc() || info.isDisplayNameAboveNpc() || info.isDrawOverlayBeneathNpc())) {
							npcList.add(info);
						}
					}
				}

				// Handle slayer task separately if enabled
				if (checkSlayerPluginEnabled() && config.slayerHighlight()) {
					addSlayerTaskNpcs();
				}

				currentTask = slayerPluginService.getTask();
			}
		});
	}

	/**
	 * Build NPCInfo from panel entries, allowing for multiple highlights per NPC.
	 * It iterates through all rules and applies each one that matches.
	 */
	private NPCInfo buildNpcInfoFromPanelEntries(NPC npc, List<NpcHighlightEntry> entries) {
		String npcName = npc.getName() != null ? npc.getName().toLowerCase() : "";
		String npcIdStr = String.valueOf(npc.getId());
		NPCInfo info = null; // Lazily create the info object only if a match is found

		for (NpcHighlightEntry entry : entries) {
			if (isEmptyEntry(entry)) {
				continue;
			}

			if (matchesEntry(npcName, npcIdStr, entry)) {
				// If this is the first match for this NPC, create the info object
				if (info == null) {
					info = new NPCInfo(npc);
				}
				// Apply the highlight from this matching entry.
				// We don't break here, allowing multiple rules to apply to the same NPC.
				applyHighlightFromEntry(info, entry);
			}
		}

		return info; // Will be null if no rules matched
	}

	/**
	 * Applies a specific highlight style from a panel entry to an existing NPCInfo object.
	 * This allows for layering multiple highlights on a single NPC.
	 */
	private void applyHighlightFromEntry(NPCInfo info, NpcHighlightEntry entry) {
		HighlightColor highlight = new HighlightColor(true, entry.outlineColor, entry.fillColor, entry.raveOutline, entry.raveFill, entry.raveSpeed, entry.tileStyle, entry.outlineWidth, entry.antiAliasing, entry.outlineFeather);
		switch (entry.tagStyle) {
			case "Tile":
				info.setTile(highlight);
				break;
			case "True Tile":
				info.setTrueTile(highlight);
				break;
			case "SW Tile":
				info.setSwTile(highlight);
				break;
			case "SW True Tile":
				info.setSwTrueTile(highlight);
				break;
			case "Hull":
				info.setHull(highlight);
				break;
			case "Area":
				info.setArea(highlight);
				break;
			case "Outline":
				info.setOutline(highlight);
				break;
			case "Clickbox":
				info.setClickbox(highlight);
				break;
			case "Turbo":
				info.setTurbo(highlight);
				break;
		}
		// Combine boolean flags. If any matching entry has it, it should be true.
		if (entry.hideNpc) {
			info.setHideNpc(true);
		}
		if (entry.drawUnder) {
			info.setDrawOverlayBeneathNpc(true);
		}
		if (entry.displayName) {
			info.setDisplayNameAboveNpc(true);
            info.setDisplayNameColor(entry.displayNameColor);
        }
		if (entry.highlightDead) {
			info.setHighlightDead(true);
		}
        
	}

	private boolean isEmptyEntry(NpcHighlightEntry entry)
	{
		return (entry.nameOrId == null || entry.nameOrId.trim().isEmpty());
	}

	private boolean matchesEntry(String npcName, String npcIdStr, NpcHighlightEntry entry)
	{
		String entryValue = entry.nameOrId.toLowerCase().trim();

		// Try ID match first
		if (StringUtils.isNumeric(entryValue) && npcIdStr.equals(entryValue)) {
			return true;
		}

		// Then try name match with wildcard support
		return !npcName.isEmpty() && WildcardMatcher.matches(entryValue, npcName);
	}

	/**
	 * Add slayer task NPCs to the list
	 */
	private void addSlayerTaskNpcs()
	{
		if (slayerPluginService == null) return;

		for (NPC npc : client.getNpcs()) {
			if (slayerPluginService.getTargets().contains(npc)) {
				// Check if this NPC is already in the list from panel entries
				Optional<NPCInfo> existingInfo = npcList.stream()
						.filter(info -> info.getNpc().getIndex() == npc.getIndex())
						.findFirst();

				if (existingInfo.isPresent()) {
					existingInfo.get().setTask(true);
				} else {
					NPCInfo info = new NPCInfo(npc);
					info.setTask(true);
					npcList.add(info);
				}
			}
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals(config.CONFIG_GROUP)) {
			switch (event.getKey()) {
				case "slayerHighlight":
					enableSlayerPlugin();
					recreateList();
					break;
				case "turboHighlight":
					if (event.getNewValue().equals("true")) {
						if (!confirmedWarning) {
							showEpilepsyWarning();
						} else {
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
		if (event.getGameState() == GameState.LOGIN_SCREEN || event.getGameState() == GameState.HOPPING) {
			npcSpawns.clear();
			npcList.clear();
		}
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		NPC npc = event.getNpc();

		// Handle respawn tracking
		for (NpcSpawn n : npcSpawns) {
			if (npc.getIndex() == n.index && npc.getId() == n.id) {
				if (n.spawnPoint == null && n.diedOnTick != -1) {
					WorldPoint wp = client.isInInstancedRegion() ?
							WorldPoint.fromLocalInstance(client, npc.getLocalLocation()) :
							WorldPoint.fromLocal(client, npc.getLocalLocation());
					if (n.spawnLocations.contains(wp)) {
						n.spawnPoint = wp;
						n.respawnTime = client.getTickCount() - n.diedOnTick + 1;
					} else {
						n.spawnLocations.add(wp);
					}
				}
				n.dead = false;
				break;
			}
		}

		// Check if this NPC should be highlighted
		NPCInfo npcInfo = checkValidNPC(npc);
		if (npcInfo != null) {
            System.out.println("NPC ADDED: " + npcInfo.getNpc().getName());
			npcList.add(npcInfo);
		}
        System.out.println("TEST: " + npcList.size());
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		NPC npc = event.getNpc();
        System.out.println("TEST2: " + npc);

		if (npc.isDead())
		{
			if (npcList.stream().anyMatch(n -> n.getNpc() == npc) && npcSpawns.stream().noneMatch(n -> n.index == npc.getIndex())) {
				npcSpawns.add(new NpcSpawn(npc));
                System.out.println("TEST3: " + npcSpawns.size());
			}
            else {
				for (NpcSpawn n : npcSpawns) {
					if (npc.getIndex() == n.index && npc.getId() == n.id) {
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
		if (npcInfo != null) {
			npcList.add(npcInfo);
		}
	}

	@Subscribe(priority = -1)
	public void onGameTick(GameTick event)
	{
		if (checkSlayerPluginEnabled() && !currentTask.equals(slayerPluginService.getTask())) {
			recreateList();
		}

		lastTickUpdate = Instant.now();

		// Update turbo colors and effects
		turboColors.clear();
		for (int i = 0; i < npcList.size(); i++) {
			turboColors.add(Color.getHSBColor(new Random().nextFloat(), 1.0F, 1.0F));
		}
		turboModeStyle = new Random().nextInt(6);
		turboTileWidth = new Random().nextInt(10) + 1;
		turboOutlineWidth = new Random().nextInt(50) + 1;
		turboOutlineFeather = new Random().nextInt(4);
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		// Only apply if highlightMenuNames is enabled
		if (!config.highlightMenuNames())
		{
			return;
		}

		// Get the MenuAction type
		final MenuAction menuAction = MenuAction.of(event.getType());

		// Check if it's an NPC-related menu action
		// Using a direct comparison for now, as NPC_MENU_ACTIONS is not defined
		// and the original code's logic for deprioritization offset is complex.
		// This covers the common NPC interaction options.
		if (menuAction == MenuAction.EXAMINE_NPC ||
			menuAction == MenuAction.NPC_FIRST_OPTION ||
			menuAction == MenuAction.NPC_SECOND_OPTION ||
			menuAction == MenuAction.NPC_THIRD_OPTION ||
			menuAction == MenuAction.NPC_FOURTH_OPTION ||
			menuAction == MenuAction.NPC_FIFTH_OPTION)
		{
			// Get the NPC
			// Using client.getCachedNPCs() as client.getTopLevelWorldView() might be API specific
			NPC npc = client.getNpcs().stream()
				.filter(n -> n.getIndex() == event.getIdentifier())
				.findFirst()
				.orElse(null);
			if (npc == null)
			{
				return;
			}

			Color color = null;

			// Check for dead NPC color
			if (npcUtil.isDying(npc))
			{
				color = config.deadNpcMenuColor();
			}
			else
			{
				// Get display name color from our plugin's logic
				color = getDisplayNameColorForNpc(npc);
			}

			if (color != null)
			{
				// Apply color to the menu entry
				// Using event.getMenuEntry().setOptionColor() which is the standard way
				// The original code used setTarget with ColorUtil.prependColorTag, which is an older method
				final String target = ColorUtil.prependColorTag(Text.removeTags(event.getMenuEntry().getTarget()), color);
				event.getMenuEntry().setTarget(target);
			}
		}
	}

	/**
	 * Check if an NPC should be highlighted (simplified version)
	 */
	public NPCInfo checkValidNPC(NPC npc)
	{
		if (panel == null) return null;

		List<NpcHighlightEntry> entries = panel.getNpcHighlightEntries();
		NPCInfo info = buildNpcInfoFromPanelEntries(npc, entries);

		// Also check for slayer task
		if (checkSlayerPluginEnabled() && config.slayerHighlight() &&
				slayerPluginService.getTargets().contains(npc)) {
			if (info == null) {
				info = new NPCInfo(npc);
			}
			info.setTask(true);
		}

		if (info != null && (info.hasAnyHighlight() || info.isHideNpc() || info.isDisplayNameAboveNpc() || info.isDrawOverlayBeneathNpc() || info.isTask())) {
			return info;
		}
		return null;
	}

    public int getTurboIndex(int id, String name)
    {
        for (int i = 0; i < npcList.size(); i++)
        {
            NPCInfo info = npcList.get(i);
            NPC npc = info.getNpc();

            if (npc.getId() == id)
            {
                if (name == null || (npc.getName() != null && npc.getName().toLowerCase().equals(name)))
                {
                    if (info.getTurbo() != null && info.getTurbo().isHighlight())
                    {
                        return i;
                    }
                }
            }
            else if (name != null)
            {
                // If ID doesn't match, check name wildcard match
                if (npc.getName() != null && WildcardMatcher.matches(name, npc.getName().toLowerCase()))
                {
                    if (info.getTurbo() != null && info.getTurbo().isHighlight())
                    {
                        return i;
                    }
                }
            }
        }
        return -1;
    }


    public Color getRaveColor(int speed)
	{
		int ticks = speed / 20;
		if (ticks <= 0)
		{
			ticks = 1;
		}
		return Color.getHSBColor((client.getGameCycle() % ticks) / ((float) ticks), 1.0f, 1.0f);
	}

    public Color getDisplayNameColorForNpc(NPC npc) {
        if (panel == null) {
            return null;
        }

        String npcName = npc.getName() != null ? npc.getName().toLowerCase() : "";
        String npcIdStr = String.valueOf(npc.getId());

        // First check if this NPC has a custom display name color in the panel
        List<NpcHighlightEntry> entries = panel.getNpcHighlightEntries();
        for (NpcHighlightEntry entry : entries) {
            if (entry.nameOrId == null || entry.nameOrId.trim().isEmpty()) {
                continue;
            }

            String entryValue = entry.nameOrId.toLowerCase().trim();

            boolean matches = false;
            if (StringUtils.isNumeric(entryValue) && npcIdStr.equals(entryValue)) {
                matches = true;
            } else if (!npcName.isEmpty() && WildcardMatcher.matches(entryValue, npcName)) {
                matches = true;
            }

            if (matches && entry.displayNameColor != null) {
                // Only return explicitly set display name colors
                return entry.displayNameColor;
            }
        }

        // If no custom display name color is set, find the NPC in our list and use its primary highlight color
        NPCInfo info = npcList.stream()
                .filter(i -> i.getNpc().getIndex() == npc.getIndex())
                .findFirst()
                .orElse(null);

        if (info != null && info.getPrimaryHighlight() != null) {
            return info.getPrimaryHighlight().getColor();
        }

        // Final fallback if no highlight color is found
        return Color.CYAN;
    }





    public boolean checkSlayerPluginEnabled()
	{
		final Optional<Plugin> slayerPlugin = pluginManager.getPlugins().stream()
				.filter(p -> p.getName().equals("Slayer")).findFirst();
		return slayerPlugin.isPresent() && pluginManager.isPluginEnabled(slayerPlugin.get());
	}

	public void enableSlayerPlugin()
	{
		try {
			final Optional<Plugin> slayerPlugin = pluginManager.getPlugins().stream()
					.filter(p -> p.getName().equals("Slayer")).findFirst();
			if (slayerPlugin.isPresent() && !pluginManager.isPluginEnabled(slayerPlugin.get()) &&
					config.slayerHighlight()) {
				pluginManager.setPluginEnabled(slayerPlugin.get(), true);
				pluginManager.startPlugin(slayerPlugin.get());
				currentTask = "";
			}
		} catch (PluginInstantiationException ex) {
			log.error("error starting slayer plugin", ex);
		}
	}

	@VisibleForTesting
	boolean shouldDraw(Renderable renderable, boolean drawingUI)
	{
		if (renderable instanceof NPC)
		{
			NPC npc = (NPC) renderable;
			for (NPCInfo npcInfo : npcList)
			{
				if (npcInfo.getNpc().getIndex() == npc.getIndex() && npcInfo.isHideNpc())
				{
					return false;
				}
			}
		}
		return true;
	}

	private void showEpilepsyWarning()
	{
		configManager.setConfiguration(config.CONFIG_GROUP, "turboHighlight", false);
		Object[] options = {"Okay, I accept the risk", "No, this is an affront to my eyes"};
		JLabel label = new JLabel("<html><p>Turning this on will cause any NPCs highlighted with this style to change colors and styles rapidly.</p></html>");

		if (JOptionPane.showOptionDialog(new JFrame(),
				label,
				"EPILEPSY WARNING - Occular Abhorrence",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE,
				null,
				options,
				options[1]) == 0) {
			confirmedWarning = true;
			configManager.setConfiguration(config.CONFIG_GROUP, "turboHighlight", true);
		}
	}

	public BetterNpcHighlightPanel getPanel() {
        return panel;
    }

	// Simplified key listener (keeping minimal functionality)
	public void keyPressed(KeyEvent e) {
		// Simplified - main functionality moved to GUI
	}

	public void keyReleased(KeyEvent e) {
	}

	public void keyTyped(KeyEvent e) {
	}
}
