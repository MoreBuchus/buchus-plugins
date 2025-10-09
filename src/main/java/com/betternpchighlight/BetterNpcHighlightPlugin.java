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

//todo: fix tag commands, add debug button

import com.betternpchighlight.data.DataManager;
import com.betternpchighlight.data.GroupDTO;
import com.betternpchighlight.data.NpcHighlightEntry;
import com.betternpchighlight.ui.BetterNpcHighlightPanel;
import com.betternpchighlight.ui.NpcCard;
import com.betternpchighlight.ui.NpcCardGroupPanel;
import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.google.inject.Provides;

import java.util.*;
import javax.swing.*;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.vars.InputType;
import net.runelite.client.callback.ClientThread;
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
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@PluginDescriptor(
        name = "Better NPC Highlight",
        description = "A more customizable NPC highlight with modern GUI",
        tags = {"npc", "highlight", "indicators", "custom", "gui", "table"}
)
@PluginDependency(SlayerPlugin.class)
public class BetterNpcHighlightPlugin extends Plugin implements KeyListener, BetterNpcHighlightPanel.DataChangedListener {
    @Inject
    private Client client;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private BetterNpcHighlightOverlay overlay;

    @Getter
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

    @Inject
    private ClientThread clientThread;

    @Inject
    private ClientToolbar clientToolbar;

    @Inject
    private ColorPickerManager colorPickerManager;

    @Inject
    private Gson gson;

    private NavigationButton panelButton;
    private BetterNpcHighlightPanel panel;
    private DataManager dataManager;

    private final Random random = new Random();
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

    private static final String HIDE_COMMAND = "!hide";
    private static final String UNHIDE_COMMAND = "!unhide";
    private static final String TAG_COMMAND = "!tag";
    private static final String UNTAG_COMMAND = "!untag";

    @Provides
    BetterNpcHighlightConfig providesConfig(ConfigManager configManager) {
        return configManager.getConfig(BetterNpcHighlightConfig.class);
    }

    protected void startUp() {
        clientThread.invokeLater(() -> {
            reset();
            new ConfigMigrator(config, configManager, gson).migrate();
            overlayManager.add(overlay);
            overlayManager.add(mapOverlay);
            hooks.registerRenderableDrawListener(drawListener);
            keyManager.registerKeyListener(this);
            dataManager = new DataManager(configManager);
            panel = new BetterNpcHighlightPanel(colorPickerManager, configManager, this, dataManager);
            panel.setDataChangedListener(this);
            panelButton = NavigationButton.builder()
                    .tooltip("Better NPC Highlight")
                    .icon(ImageUtil.loadImageResource(getClass(), "/panel_icon.png"))
                    .priority(5)
                    .panel(panel)
                    .build();
            clientToolbar.addNavigation(panelButton);
            panel.loadAllGroups(BetterNpcHighlightConfig.CONFIG_GROUP);
            if (client.getGameState() == GameState.LOGGED_IN) {
                recreateList();
            }
        });
    }

    protected void shutDown() {
        clientThread.invokeLater(() -> {
            reset();
            if (panel != null) {
                panel.saveAllGroups(BetterNpcHighlightConfig.CONFIG_GROUP);
            }
            overlayManager.remove(overlay);
            overlayManager.remove(mapOverlay);
            hooks.unregisterRenderableDrawListener(drawListener);
            keyManager.unregisterKeyListener(this);

            if (panelButton != null) {
                clientToolbar.removeNavigation(panelButton);
            }
        });
    }

    private void reset() {
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
    public void recreateList() {
        clientThread.invokeLater(() -> {
            if (client.getGameState() == GameState.LOGGED_IN && client.getLocalPlayer() != null
                    && client.getLocalPlayer().getPlayerComposition() != null) {

                npcList.clear();
                npcSpawns.clear();

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
                // Always apply toggles from any matching entry.
                applyTogglesFromEntry(info, entry);

                // Apply the visual highlight from this matching entry, but only if it's not "None".
                // We don't break here, allowing multiple rules to apply to the same NPC,
                // layering highlights and toggles.
                if (entry.tagStyle != null) {
                    applyHighlightFromEntry(info, entry);
                }
            }
        }

        return info;
    }

    /**
     * Applies a specific highlight style from a panel entry to an existing NPCInfo object.
     * This allows for layering multiple highlights on a single NPC.
     */
    private void applyHighlightFromEntry(NPCInfo info, NpcHighlightEntry entry) {
        HighlightInfo highlight = new HighlightInfo(true, entry.outlineColor, entry.fillColor, entry.raveOutline, entry.raveFill, entry.raveSpeed, entry.tileStyle, entry.outlineWidth, entry.antiAliasing, entry.outlineFeather);
        switch (entry.tagStyle) {
            case TILE:
                info.setTile(highlight);
                break;
            case TRUE_TILE:
                info.setTrueTile(highlight);
                break;
            case SW_TILE:
                info.setSwTile(highlight);
                break;
            case SW_TRUE_TILE:
                info.setSwTrueTile(highlight);
                break;
            case HULL:
                info.setHull(highlight);
                break;
            case AREA:
                info.setArea(highlight);
                break;
            case OUTLINE:
                info.setOutline(highlight);
                break;
            case CLICKBOX:
                info.setClickbox(highlight);
                break;
            case TURBO:
                info.setTurbo(highlight);
                break;
        }
    }

    /**
     * Applies boolean toggles from a panel entry to an existing NPCInfo object.
     */
    private void applyTogglesFromEntry(NPCInfo info, NpcHighlightEntry entry) {
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

    private boolean isEmptyEntry(NpcHighlightEntry entry) {
        return (entry.nameOrId == null || entry.nameOrId.trim().isEmpty());
    }

    private boolean matchesEntry(String npcName, String npcIdStr, NpcHighlightEntry entry) {
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
    private void addSlayerTaskNpcs() {
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
    public void onConfigChanged(ConfigChanged event) {
        if (event.getGroup().equals(BetterNpcHighlightConfig.CONFIG_GROUP)) {
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
    public void onGameStateChanged(GameStateChanged event) {
        if (event.getGameState() == GameState.LOGIN_SCREEN || event.getGameState() == GameState.HOPPING) {
            npcSpawns.clear();
            npcList.clear();
        }
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned event) {
        NPC npc = event.getNpc();

        // Handle respawn tracking
        for (NpcSpawn n : npcSpawns) {
            if (npc.getIndex() == n.index && npc.getId() == n.id) {
                if (n.spawnPoint == null && n.diedOnTick != -1) {
                    n.spawnPoint = client.isInInstancedRegion() ?
                            WorldPoint.fromLocalInstance(client, npc.getLocalLocation()) :
                            WorldPoint.fromLocal(client, npc.getLocalLocation());
                    n.respawnTime = client.getTickCount() - n.diedOnTick + 1;
                    n.spawnLocations.add(n.spawnPoint);
                }
                n.dead = false;
                break;
            }
        }

        // Check if this NPC should be highlighted
        NPCInfo npcInfo = checkValidNPC(npc);
        if (npcInfo != null) {
            npcList.add(npcInfo);
        }
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned event) {
        NPC npc = event.getNpc();

        if (npc.isDead()) {
            if (npcList.stream().anyMatch(n -> n.getNpc() == npc) && npcSpawns.stream().noneMatch(n -> n.index == npc.getIndex())) {
                npcSpawns.add(new NpcSpawn(npc));
            } else {
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
    public void onNpcChanged(NpcChanged event) {
        NPC npc = event.getNpc();

        npcList.removeIf(n -> n.getNpc().getIndex() == npc.getIndex());

        NPCInfo npcInfo = checkValidNPC(npc);
        if (npcInfo != null) {
            npcList.add(npcInfo);
        }
    }

    @Subscribe(priority = -1)
    public void onGameTick(GameTick event) {
        if (checkSlayerPluginEnabled() && !currentTask.equals(slayerPluginService.getTask())) {
            recreateList();
        }

        lastTickUpdate = Instant.now();

        // Update turbo colors and effects
        turboColors.clear();
        for (int i = 0; i < npcList.size(); i++) {
            turboColors.add(Color.getHSBColor(random.nextFloat(), 1.0F, 1.0F));
        }
        turboModeStyle = random.nextInt(6);
        turboTileWidth = random.nextInt(10) + 1;
        turboOutlineWidth = random.nextInt(50) + 1;
        turboOutlineFeather = random.nextInt(4);
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event) {
        final NPC npc = event.getMenuEntry().getNpc();
        if (npc == null) {
            return;
        }

        // Highlight existing menu entries
        if (config.highlightMenuNames()) {
            Color color;
            if (npcUtil.isDying(npc)) {
                color = config.deadNpcMenuColor();
            } else {
                color = getDisplayNameColorForNpc(npc);
            }

            if (color != null) {
                MenuEntry[] menuEntries = client.getMenuEntries();
                final MenuEntry menuEntry = menuEntries[menuEntries.length - 1];
                final String target = ColorUtil.prependColorTag(Text.removeTags(event.getTarget()), color);
                menuEntry.setTarget(target);
                client.setMenuEntries(menuEntries);
            }
        }

        // Add "Tag" / "Untag" options on shift-click
        if (event.getType() == MenuAction.EXAMINE_NPC.getId() && client.isKeyPressed(KeyCode.KC_SHIFT)) {
            addTagMenuOptions(event, npc);
        }
    }

    private void addTagMenuOptions(MenuEntryAdded event, NPC npc) {
        if (panel == null) {
            return;
        }

        List<NpcHighlightEntry> entries = panel.getNpcHighlightEntries();

        TagStyle style = getTagStyleFromConfig();

        if (style == null) return;

        boolean isTagged = isNpcTaggedWithStyle(npc, style, entries);
        String option = isTagged ? "Untag-" + style.getName() : "Tag-" + style.getName();

        client.createMenuEntry(-1)
                .setOption(option)
                .setTarget(event.getTarget())
                .setType(MenuAction.RUNELITE)
                .onClick(e -> {
                    if (isTagged) {
                        removeNpcFromStyle(npc, style);
                    } else {
                        addNpcToStyle(npc, style);
                    }
                });
    }

    private boolean isNpcTaggedWithStyle(NPC npc, TagStyle style, List<NpcHighlightEntry> entries) {
        String npcName = npc.getName() != null ? npc.getName().toLowerCase() : "";
        String npcIdStr = String.valueOf(npc.getId());

        for (NpcHighlightEntry entry : entries) {
            if (entry.tagStyle != style) {
                continue;
            }

            if (matchesEntry(npcName, npcIdStr, entry)) {
                return true;
            }
        }
        return false;
    }

    public void addNpcToStyle(NPC npc, TagStyle style) {
        if (panel == null || npc == null || npc.getName() == null) {
            return;
        }

        String npcName = npc.getName();

        // First, try to find an existing card for this NPC
        for (NpcCardGroupPanel group : panel.getGroups()) {
            for (NpcCard card : group.getCards()) {
                if (npcName.equalsIgnoreCase(card.getNameText())) {
                    card.addTagStyle(style);
                    panel.triggerDataChanged();
                    return;
                }
            }
        }

        // If no card exists, create a new one in the default group
        panel.addNewCardToDefaultGroup(card -> {
            card.setNameText(npcName);
            card.addTagStyle(style);
        }, false);
    }

    private TagStyle getTagStyleFromConfig() {
        BetterNpcHighlightConfig.DefaultHighlightStyle defaultStyle = config.tagStyleMode();
        if (defaultStyle == BetterNpcHighlightConfig.DefaultHighlightStyle.NONE) {
            return null;
        }
        try {
            return TagStyle.valueOf(defaultStyle.name());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public void removeNpcFromStyle(NPC npc, TagStyle style) {
        if (panel == null || npc == null || npc.getName() == null) {
            return;
        }

        String npcName = npc.getName();
        List<NpcCard> cardsToRemove = new ArrayList<>();
        List<NpcCardGroupPanel> groupsToUpdate = new ArrayList<>();

        for (NpcCardGroupPanel group : panel.getGroups()) {
            boolean groupChanged = false;
            for (NpcCard card : group.getCards()) {
                if (npcName.equalsIgnoreCase(card.getNameText())) {
                    card.removeTagStyle(style);
                    if (card.getAllTagStyles().isEmpty()) {
                        cardsToRemove.add(card);
                        groupChanged = true;
                    }
                }
            }
            if (groupChanged) {
                groupsToUpdate.add(group);
            }
        }

        for (NpcCardGroupPanel group : groupsToUpdate) {
            cardsToRemove.forEach(group::removeCard);
        }

        panel.triggerDataChanged();
    }

    /**
     * Check if an NPC should be highlighted (simplified version)
     */
    public NPCInfo checkValidNPC(NPC npc) {
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

    public int getTurboIndex(int id, String name) {
        for (int i = 0; i < npcList.size(); i++) {
            NPCInfo info = npcList.get(i);
            NPC npc = info.getNpc();

            if (npc.getId() == id) {
                if (name == null || (npc.getName() != null && npc.getName().toLowerCase().equals(name))) {
                    if (info.getTurbo() != null && info.getTurbo().isHighlight()) {
                        return i;
                    }
                }
            } else if (name != null) {
                // If ID doesn't match, check name wildcard match
                if (npc.getName() != null && WildcardMatcher.matches(name, npc.getName().toLowerCase())) {
                    if (info.getTurbo() != null && info.getTurbo().isHighlight()) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    public Color getRaveColor(int speed) {
        int ticks = speed / 20;
        if (ticks <= 0) {
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

        // Find the first matching entry for this NPC in the panel's configuration
        List<NpcHighlightEntry> entries = panel.getNpcHighlightEntries();
        for (NpcHighlightEntry entry : entries) {
            if (entry.nameOrId == null || entry.nameOrId.trim().isEmpty()) {
                continue;
            }

            if (matchesEntry(npcName, npcIdStr, entry)) {
                // Prioritize the explicit display name color if it's set.
                if (entry.overrideDisplayNameColor && entry.displayNameColor != null) {
                    return entry.displayNameColor;
                }
                // Fallback to the outline color of the first active highlight style.
                if (entry.tagStyle != null) {
                    return entry.outlineColor;
                }
            }
        }

        // If the NPC is not found in any panel entries, return null.
        return null;
    }

    public boolean checkSlayerPluginEnabled() {
        final Optional<Plugin> slayerPlugin = pluginManager.getPlugins().stream()
                .filter(p -> p.getName().equals("Slayer")).findFirst();
        return slayerPlugin.isPresent() && pluginManager.isPluginEnabled(slayerPlugin.get());
    }

    public void enableSlayerPlugin() {
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
    boolean shouldDraw(Renderable renderable, boolean drawingUI) {
        if (renderable instanceof NPC) {
            NPC npc = (NPC) renderable;
            for (NPCInfo npcInfo : npcList) {
                if (npcInfo.getNpc().getIndex() == npc.getIndex() && npcInfo.isHideNpc()) {
                    return false;
                }
            }
        }
        return true;
    }

    private void showEpilepsyWarning() {
        configManager.setConfiguration(BetterNpcHighlightConfig.CONFIG_GROUP, "turboHighlight", false);
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
            configManager.setConfiguration(BetterNpcHighlightConfig.CONFIG_GROUP, "turboHighlight", true);
        }
    }

    public void exportGroupsToFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export cards");
        fileChooser.setSelectedFile(new java.io.File("npc_highlight_groups.json"));

        FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON file (*.json)", "json");
        fileChooser.setFileFilter(filter);
        fileChooser.setAcceptAllFileFilterUsed(false);

        int userSelection = fileChooser.showSaveDialog(panel);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();

            // Ensure file ends with .json
            if (!file.getName().toLowerCase().endsWith(".json")) {
                file = new java.io.File(file.getAbsolutePath() + ".json");
            }

            if (file.exists()) {
                int overwrite = JOptionPane.showConfirmDialog(
                        panel,
                        "File already exists. Do you wish to overwrite it?",
                        "Confirm Overwrite",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );
                if (overwrite != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            try (FileWriter writer = new FileWriter(file)) {
                List<GroupDTO> groupDTOs = new ArrayList<>();
                for (NpcCardGroupPanel group : panel.getGroups()) {
                    groupDTOs.add(dataManager.convertGroupToDto(group));
                }
                gson.toJson(groupDTOs, writer);
                JOptionPane.showMessageDialog(panel, "Export successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(panel, "Error exporting groups: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void importGroupsFromFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Import cards");

        FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON file (*.json)", "json");
        fileChooser.setFileFilter(filter);
        fileChooser.setAcceptAllFileFilterUsed(false);

        int userSelection = fileChooser.showOpenDialog(panel);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            try (FileReader reader = new FileReader(fileChooser.getSelectedFile())) {
                GroupDTO[] importedGroups = gson.fromJson(reader, GroupDTO[].class);

                if (importedGroups == null || importedGroups.length == 0) {
                    JOptionPane.showMessageDialog(
                            panel,
                            "The selected file contains no groups.",
                            "Import failed",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }

                final Set<String> allCardNames = panel.getGroups().stream()
                        .flatMap(group -> group.getCards().stream())
                        .map(NpcCard::getNameText)
                        .filter(name -> name != null && !name.trim().isEmpty())
                        .collect(Collectors.toSet());

                final Map<String, NpcCardGroupPanel> existingGroupsMap = panel.getGroups().stream()
                        .collect(Collectors.toMap(NpcCardGroupPanel::getGroupName, g -> g, (g1, g2) -> g1));

                int totalImportedGroups = 0;
                int totalImportedCards = 0;

                for (GroupDTO importedGroupDto : importedGroups) {
                    // Basic validation: skip invalid group
                    if (importedGroupDto.name == null || importedGroupDto.name.trim().isEmpty()) {
                        continue;
                    }

                    NpcCardGroupPanel targetGroup = existingGroupsMap.get(importedGroupDto.getName());
                    boolean isNewGroup = targetGroup == null;

                    NpcCardGroupPanel tempImportedGroup = dataManager.convertGroupFromDto(importedGroupDto, panel);
                    List<NpcCard> cardsToAdd = new ArrayList<>();

                    for (NpcCard importedCard : tempImportedGroup.getCards()) {
                        String cardName = importedCard.getNameText();
                        if (cardName != null && !cardName.trim().isEmpty() && allCardNames.add(cardName)) {
                            cardsToAdd.add(importedCard);
                        }
                    }

                    if (!cardsToAdd.isEmpty()) {
                        if (isNewGroup) {
                            tempImportedGroup.clearCards();
                            cardsToAdd.forEach(tempImportedGroup::addCard);
                            panel.getGroups().add(tempImportedGroup);
                            totalImportedGroups++;
                        } else {
                            cardsToAdd.forEach(targetGroup::addCard);
                        }
                        totalImportedCards += cardsToAdd.size();
                    }
                }

                panel.sortAndRebuildGroups();
                panel.triggerDataChanged();

                if (totalImportedCards == 0) {
                    JOptionPane.showMessageDialog(
                            panel,
                            "No new cards were imported (all duplicates).",
                            "Import finished",
                            JOptionPane.WARNING_MESSAGE
                    );
                } else {
                    StringBuilder message = new StringBuilder("Import successful!\n");
                    if (totalImportedGroups > 0) {
                        message.append("Groups added: ").append(totalImportedGroups).append("\n");
                    }
                    message.append("Cards added: ").append(totalImportedCards);

                    JOptionPane.showMessageDialog(
                            panel,
                            message.toString(),
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }
            } catch (com.google.gson.JsonSyntaxException ex) {
                JOptionPane.showMessageDialog(panel, "Invalid JSON format: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(panel, "Error importing groups: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "An unexpected error occurred while importing: " + ex.getMessage(), "Import failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void keyPressed(KeyEvent e) {
        //Enter is pressed
        if (e.getKeyCode() == 10) {
            int inputType = client.getVarcIntValue(VarClientInt.INPUT_TYPE);
            if (inputType == InputType.PRIVATE_MESSAGE.getType() || inputType == InputType.NONE.getType()) {
                int var;
                if (inputType == InputType.PRIVATE_MESSAGE.getType()) {
                    var = VarClientStr.INPUT_TEXT;
                } else {
                    var = VarClientStr.CHATBOX_TYPED_TEXT;
                }

                if (client.getVarcStrValue(var) != null && !client.getVarcStrValue(var).isEmpty()) {
                    String text = client.getVarcStrValue(var).toLowerCase();
                    if (config.entityHiderCommands() && (text.startsWith(HIDE_COMMAND) || text.startsWith(UNHIDE_COMMAND))) {
                        hideNPCCommand(text, var);
                    } else if (config.tagCommands() && (text.startsWith(TAG_COMMAND) || text.startsWith(UNTAG_COMMAND))) {
                        tagNPCCommand(text, var);
                    }
                }
            }
        }
    }

    private void hideNPCCommand(String text, int var) {
        String npcToHide = text.replace(text.startsWith(HIDE_COMMAND) ? HIDE_COMMAND : UNHIDE_COMMAND, "").trim();
        boolean hide = text.startsWith(HIDE_COMMAND);

        updateNpcHide(npcToHide, hide);

        //Set typed text to nothing
        clientThread.invokeLater(() -> client.setVarcStrValue(var, ""));
    }

    private void tagNPCCommand(String text, int var) {
        if (text.trim().equals(TAG_COMMAND) || text.trim().equals(UNTAG_COMMAND)) {
            printMessage("Please enter a tag abbreviation followed by a valid NPC name or ID!");
            clientThread.invokeLater(() -> client.setVarcStrValue(var, ""));
            return;
        }

        String command = text.startsWith(TAG_COMMAND) ? TAG_COMMAND : UNTAG_COMMAND;
        boolean isTagging = command.equals(TAG_COMMAND);
        String withoutCommand = text.substring(command.length()).trim();
        String[] parts = withoutCommand.split("\\s+", 2);

        if (parts.length < 2) {
            printMessage("Invalid format. Use: " + command + " <style> <npc_name_or_id>");
            clientThread.invokeLater(() -> client.setVarcStrValue(var, ""));
            return;
        }

        String styleAbbr = parts[0].toLowerCase();
        String npcIdentifier = parts[1].trim();
        int preset = 0; // 0 means no preset

        if (npcIdentifier.contains(":")) {
            String[] npcParts = npcIdentifier.split(":", 2);
            if (npcParts.length == 2 && StringUtils.isNumeric(npcParts[1])) {
                npcIdentifier = npcParts[0].trim();
                try {
                    preset = Integer.parseInt(npcParts[1]);
                    if (preset < 1 || preset > 5) {
                        printMessage("Invalid preset number. Must be between 1 and 5.");
                        clientThread.invokeLater(() -> client.setVarcStrValue(var, ""));
                        return;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        TagStyle style = TagStyle.fromAbbreviation(styleAbbr);
        if (style == null) {
            printMessage("Unknown tag style: " + styleAbbr);
            clientThread.invokeLater(() -> client.setVarcStrValue(var, ""));
            return;
        }

        updateNpcTagStyle(npcIdentifier, style, isTagging, preset);

        //Set typed text to nothing
        clientThread.invokeLater(() -> client.setVarcStrValue(var, ""));
    }

    private void updateNpcHide(String npcIdentifier, boolean hide) {
        if (panel == null || npcIdentifier == null || npcIdentifier.trim().isEmpty()) {
            printMessage("Please enter a valid NPC name or ID!");
            return;
        }

        // Find an existing card
        for (NpcCardGroupPanel group : panel.getGroups()) {
            for (NpcCard card : group.getCards()) {
                if (npcIdentifier.equalsIgnoreCase(card.getNameText())) {
                    card.setHideNpc(hide);
                    panel.triggerDataChanged();
                    printMessage((hide ? "Hiding " : "Unhiding ") + card.getNameText());
                    return;
                }
            }
        }

        // If not found and we are hiding, create a new card
        if (hide) {
            panel.addNewCardToDefaultGroup(card -> {
                card.setNameText(npcIdentifier);
                card.setHideNpc(true);
            }, false);
            printMessage("Hiding " + npcIdentifier);
        } else {
            printMessage(npcIdentifier + " is not currently hidden.");
        }
    }

    private void updateNpcTagStyle(String npcIdentifier, TagStyle style, boolean add, int preset) {
        if (panel == null || npcIdentifier == null || npcIdentifier.trim().isEmpty()) {
            printMessage("Please enter a valid NPC name or ID!");
            return;
        }

        // Find an existing card
        for (NpcCardGroupPanel group : panel.getGroups()) {
            for (NpcCard card : group.getCards()) {
                if (npcIdentifier.equalsIgnoreCase(card.getNameText())) {
                    if (add) {
                        card.addTagStyle(style, getPresetOutlineColor(preset), getPresetFillColor(preset));
                        printMessage("Tagged " + card.getNameText() + " with " + style.getName());
                    } else {
                        card.removeTagStyle(style);
                        printMessage("Untagged " + card.getNameText() + " from " + style.getName());
                    }
                    panel.triggerDataChanged();
                    return;
                }
            }
        }

        // If not found and we are adding a tag, create a new card
        if (add) {
            panel.addCardFromCommand(card -> {
                card.setNameText(npcIdentifier);
                card.addTagStyle(style, getPresetOutlineColor(preset), getPresetFillColor(preset));
            });
            printMessage("Tagged " + npcIdentifier + " with " + style.getName());
        } else {
            printMessage(npcIdentifier + " is not currently tagged with " + style.getName());
        }
    }

    private Color getPresetOutlineColor(int preset) {
        if (preset <= 0) return null;
        switch (preset) {
            case 1:
                return config.presetColor1();
            case 2:
                return config.presetColor2();
            case 3:
                return config.presetColor3();
            case 4:
                return config.presetColor4();
            case 5:
                return config.presetColor5();
            default:
                return null;
        }
    }

    private Color getPresetFillColor(int preset) {
        if (preset <= 0) return null;
        switch (preset) {
            case 1:
                return config.presetFillColor1();
            case 2:
                return config.presetFillColor2();
            case 3:
                return config.presetFillColor3();
            case 4:
                return config.presetFillColor4();
            case 5:
                return config.presetFillColor5();
            default:
                return null;
        }
    }

    public void printMessage(String msg) {
        final ChatMessageBuilder message = new ChatMessageBuilder()
                .append(ChatColorType.HIGHLIGHT)
                .append(msg);

        chatMessageManager.queue(QueuedMessage.builder()
                .type(ChatMessageType.CONSOLE)
                .runeLiteFormattedMessage(message.build())
                .build());
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void onDataChanged() {
        recreateList();
        panel.requestSave(BetterNpcHighlightConfig.CONFIG_GROUP);
    }
}
