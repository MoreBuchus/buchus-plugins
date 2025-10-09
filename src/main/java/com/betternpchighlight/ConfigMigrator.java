package com.betternpchighlight;

import com.betternpchighlight.data.CardDTO;
import com.betternpchighlight.data.DataManager;
import com.betternpchighlight.data.GroupDTO;
import com.betternpchighlight.data.StyleDTO;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import net.runelite.client.config.ConfigManager;

public final class ConfigMigrator {

    private final BetterNpcHighlightConfig config;
    private final ConfigManager configManager;
    private final Gson gson;
    private final List<String> ignoreDeadExclusionNames;
    private final List<String> ignoreDeadExclusionIds;
    private final List<String> drawBeneathListNames;
    private final List<String> displayNameNames;
    private final List<String> entityHiderNames;
    private final List<String> entityHiderIds;

    public ConfigMigrator(BetterNpcHighlightConfig config, ConfigManager configManager, Gson gson) {
        this.config = config;
        this.configManager = configManager;
        this.gson = gson;
        this.ignoreDeadExclusionNames = parseConfigList(config.ignoreDeadExclusion());
        this.ignoreDeadExclusionIds = parseConfigList(config.ignoreDeadExclusionID());
        this.drawBeneathListNames = parseConfigList(config.drawBeneathList());
        this.displayNameNames = parseConfigList(config.displayName());
        this.entityHiderNames = parseConfigList(config.entityHiderNames());
        this.entityHiderIds = parseConfigList(config.entityHiderIds());
    }

    public void migrate() {
        // Only migrate if the new 'groups' config doesn't exist
        if (configManager.getConfiguration(BetterNpcHighlightConfig.CONFIG_GROUP, "groups") != null) {
            return;
        }

        // 1. Collect all unique NPC names/IDs from all old config fields.
        HashSet<String> allNpcIdentifiers = new HashSet<>();
        addIdentifiers(allNpcIdentifiers, config.tileNames());
        addIdentifiers(allNpcIdentifiers, config.tileIds());
        addIdentifiers(allNpcIdentifiers, config.trueTileNames());
        addIdentifiers(allNpcIdentifiers, config.trueTileIds());
        addIdentifiers(allNpcIdentifiers, config.swTileNames());
        addIdentifiers(allNpcIdentifiers, config.swTileIds());
        addIdentifiers(allNpcIdentifiers, config.swTrueTileNames());
        addIdentifiers(allNpcIdentifiers, config.swTrueTileIds());
        addIdentifiers(allNpcIdentifiers, config.hullNames());
        addIdentifiers(allNpcIdentifiers, config.hullIds());
        addIdentifiers(allNpcIdentifiers, config.areaNames());
        addIdentifiers(allNpcIdentifiers, config.areaIds());
        addIdentifiers(allNpcIdentifiers, config.outlineNames());
        addIdentifiers(allNpcIdentifiers, config.outlineIds());
        addIdentifiers(allNpcIdentifiers, config.clickboxNames());
        addIdentifiers(allNpcIdentifiers, config.clickboxIds());
        addIdentifiers(allNpcIdentifiers, config.turboNames());
        addIdentifiers(allNpcIdentifiers, config.turboIds());
        allNpcIdentifiers.addAll(ignoreDeadExclusionNames);
        allNpcIdentifiers.addAll(ignoreDeadExclusionIds);
        allNpcIdentifiers.addAll(drawBeneathListNames);
        allNpcIdentifiers.addAll(displayNameNames);
        allNpcIdentifiers.addAll(entityHiderNames);
        allNpcIdentifiers.addAll(entityHiderIds);

        // 2. For each unique NPC, build its full CardDTO.
        List<CardDTO> migratedCards = new ArrayList<>();
        for (String identifier : allNpcIdentifiers) {
            if (identifier.trim().isEmpty()) continue;

            CardDTO card = buildCardForIdentifier(identifier);
            migratedCards.add(card);
        }

        // Create a default group and add all migrated cards to it
        GroupDTO defaultGroup = DataManager.DEFAULT_GROUP;
        defaultGroup.cards.addAll(migratedCards);

        List<GroupDTO> groupsToSave = new ArrayList<>();
        groupsToSave.add(defaultGroup);

        String groupsJson = gson.toJson(groupsToSave);

        // Save the new group structure to the 'groups' key
        configManager.setConfiguration(BetterNpcHighlightConfig.CONFIG_GROUP, "groups", groupsJson);
    }

    private void addIdentifiers(HashSet<String> set, String configString) {
        if (configString == null || configString.trim().isEmpty()) {
            return;
        }
        // We don't need to parse highlights here, just get the names/IDs
        Arrays.stream(configString.toLowerCase().split(","))
                .map(s -> s.split(":")[0].trim())
                .forEach(set::add);
    }

    private CardDTO buildCardForIdentifier(String identifier) {
        CardDTO card = new CardDTO();
        card.uuid = UUID.randomUUID().toString();
        card.name = identifier;

        // Apply boolean flags
        card.highlightDead = ignoreDeadExclusionNames.contains(identifier) || ignoreDeadExclusionIds.contains(identifier);
        card.drawUnder = drawBeneathListNames.contains(identifier);
        card.displayName = displayNameNames.contains(identifier);
        card.hideNpc = entityHiderNames.contains(identifier) || entityHiderIds.contains(identifier);

        // Check and apply each visual style
        addStyleIfPresent(card, identifier, TagStyle.TILE, config.tileNames(), config.tileIds(), config.tileColor(), config.tileFillColor(), config.tileWidth(), config.tileAA(), config.tileRave(), config.tileRaveSpeed(), config.tileLines(), 0);
        addStyleIfPresent(card, identifier, TagStyle.TRUE_TILE, config.trueTileNames(), config.trueTileIds(), config.trueTileColor(), config.trueTileFillColor(), config.trueTileWidth(), config.trueTileAA(), config.trueTileRave(), config.trueTileRaveSpeed(), config.trueTileLines(), 0);
        addStyleIfPresent(card, identifier, TagStyle.SW_TILE, config.swTileNames(), config.swTileIds(), config.swTileColor(), config.swTileFillColor(), config.swTileWidth(), config.swTileAA(), config.swTileRave(), config.swTileRaveSpeed(), config.swTileLines(), 0);
        addStyleIfPresent(card, identifier, TagStyle.SW_TRUE_TILE, config.swTrueTileNames(), config.swTrueTileIds(), config.swTrueTileColor(), config.swTrueTileFillColor(), config.swTrueTileWidth(), config.swTrueTileAA(), config.swTrueTileRave(), config.swTrueTileRaveSpeed(), config.swTrueTileLines(), 0);
        addStyleIfPresent(card, identifier, TagStyle.HULL, config.hullNames(), config.hullIds(), config.hullColor(), config.hullFillColor(), config.hullWidth(), config.hullAA(), config.hullRave(), config.hullRaveSpeed(), null, 0);
        addStyleIfPresent(card, identifier, TagStyle.AREA, config.areaNames(), config.areaIds(), config.areaColor(), null, 0, false, config.areaRave(), config.areaRaveSpeed(), null, 0);
        addStyleIfPresent(card, identifier, TagStyle.OUTLINE, config.outlineNames(), config.outlineIds(), config.outlineColor(), null, config.outlineWidth(), true, config.outlineRave(), config.outlineRaveSpeed(), null, config.outlineFeather());
        addStyleIfPresent(card, identifier, TagStyle.CLICKBOX, config.clickboxNames(), config.clickboxIds(), config.clickboxColor(), config.clickboxFillColor(), config.clickboxWidth(), config.clickboxAA(), config.clickboxRave(), config.clickboxRaveSpeed(), null, 0);
        addStyleIfPresent(card, identifier, TagStyle.TURBO, config.turboNames(), config.turboIds(), null, null, 0, false, false, 0, null, 0);

        return card;
    }

    private void addStyleIfPresent(CardDTO card, String identifier, TagStyle style, String names, String ids, java.awt.Color outlineColor, java.awt.Color fillColor, double width, boolean antiAliasing, boolean rave, int raveSpeed, BetterNpcHighlightConfig.lineType lineType, int feather) {
        int preset = 0;
        boolean found = false;

        List<String> nameList = parseConfigList(names);
        for (String nameEntry : nameList) {
            String[] parts = nameEntry.split(":");
            if (parts[0].equals(identifier)) {
                found = true;
                if (parts.length > 1) {
                    try {
                        preset = Integer.parseInt(parts[1]);
                    } catch (NumberFormatException ignored) {
                    }
                }
                break;
            }
        }

        if (!found) {
            List<String> idList = parseConfigList(ids);
            for (String idEntry : idList) {
                String[] parts = idEntry.split(":");
                if (parts[0].equals(identifier)) {
                    found = true;
                    if (parts.length > 1) {
                        try {
                            preset = Integer.parseInt(parts[1]);
                        } catch (NumberFormatException ignored) {
                        }
                    }
                    break;
                }
            }
        }

        if (found) {
            StyleDTO styleDTO = new StyleDTO();
            styleDTO.tagStyle = style.toString();

            java.awt.Color finalOutlineColor = getPresetColor(preset, false);
            if (finalOutlineColor == null) finalOutlineColor = outlineColor;

            java.awt.Color finalFillColor = getPresetColor(preset, true);
            if (finalFillColor == null) finalFillColor = fillColor;

            if (finalOutlineColor != null) styleDTO.outlineColor = finalOutlineColor.getRGB();
            if (finalFillColor != null) styleDTO.fillColor = finalFillColor.getRGB();

            styleDTO.raveOutline = rave;
            styleDTO.raveFill = rave;
            styleDTO.raveSpeed = raveSpeed;
            styleDTO.tileStyle = toTileStyle(lineType).name();
            styleDTO.outlineWidth = width;
            styleDTO.antiAliasing = antiAliasing;
            styleDTO.outlineFeather = feather;
            card.styles.add(styleDTO);
        }
    }

    private java.awt.Color getPresetColor(int preset, boolean isFill) {
        switch (preset) {
            case 1:
                return isFill ? config.presetFillColor1() : config.presetColor1();
            case 2:
                return isFill ? config.presetFillColor2() : config.presetColor2();
            case 3:
                return isFill ? config.presetFillColor3() : config.presetColor3();
            case 4:
                return isFill ? config.presetFillColor4() : config.presetColor4();
            case 5:
                return isFill ? config.presetFillColor5() : config.presetColor5();
            default:
                return null;
        }
    }

    private HighlightInfo.TileStyle toTileStyle(BetterNpcHighlightConfig.lineType lineType) {
        if (lineType == null) {
            return HighlightInfo.TileStyle.REGULAR;
        }
        switch (lineType) {
            case DASHED:
                return HighlightInfo.TileStyle.DASHED;
            case CORNER:
                return HighlightInfo.TileStyle.CORNER;
            default:
                return HighlightInfo.TileStyle.REGULAR;
        }
    }

    private List<String> parseConfigList(String configString) {
        if (configString == null || configString.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(configString.toLowerCase().split(","))
                .map(String::trim).collect(Collectors.toList());
    }
}