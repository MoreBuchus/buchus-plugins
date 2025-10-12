package com.betternpchighlight;

import com.betternpchighlight.data.CardDTO;
import com.betternpchighlight.data.DataManager;
import com.betternpchighlight.data.GroupDTO;
import com.betternpchighlight.data.StyleDTO;
import com.google.gson.Gson;
import net.runelite.client.config.ConfigManager;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public final class ConfigMigrator {

    private final ConfigManager configManager;
    private final Gson gson;
    private final List<String> ignoreDeadExclusionNames;
    private final List<String> ignoreDeadExclusionIds;
    private final List<String> drawBeneathListNames;
    private final List<String> displayNameNames;
    private final List<String> entityHiderNames;
    private final List<String> entityHiderIds;

    public ConfigMigrator(ConfigManager configManager, Gson gson) {
        this.configManager = configManager;
        this.gson = gson;
        this.ignoreDeadExclusionNames = parseConfigList(getString("ignoreDeadExclusion"));
        this.ignoreDeadExclusionIds = parseConfigList(getString("ignoreDeadExclusionID"));
        this.drawBeneathListNames = parseConfigList(getString("drawBeneathList"));
        this.displayNameNames = parseConfigList(getString("displayName"));
        this.entityHiderNames = parseConfigList(getString("entityHiderNames"));
        this.entityHiderIds = parseConfigList(getString("entityHiderIds"));
    }

    public void migrate() {
        // Only migrate if the new 'groups' config doesn't exist
        if (configManager.getConfiguration(BetterNpcHighlightConfig.CONFIG_GROUP, "groups") != null) {
            return;
        }

        // 1. Collect all unique NPC names/IDs from all old config fields.
        HashSet<String> allNpcIdentifiers = new HashSet<>();
        addIdentifiers(allNpcIdentifiers, getString("tileNames"));
        addIdentifiers(allNpcIdentifiers, getString("tileIds"));
        addIdentifiers(allNpcIdentifiers, getString("trueTileNames"));
        addIdentifiers(allNpcIdentifiers, getString("trueTileIds"));
        addIdentifiers(allNpcIdentifiers, getString("swTileNames"));
        addIdentifiers(allNpcIdentifiers, getString("swTileIds"));
        addIdentifiers(allNpcIdentifiers, getString("swTrueTileNames"));
        addIdentifiers(allNpcIdentifiers, getString("swTrueTileIds"));
        addIdentifiers(allNpcIdentifiers, getString("hullNames"));
        addIdentifiers(allNpcIdentifiers, getString("hullIds"));
        addIdentifiers(allNpcIdentifiers, getString("areaNames"));
        addIdentifiers(allNpcIdentifiers, getString("areaIds"));
        addIdentifiers(allNpcIdentifiers, getString("outlineNames"));
        addIdentifiers(allNpcIdentifiers, getString("outlineIds"));
        addIdentifiers(allNpcIdentifiers, getString("clickboxNames"));
        addIdentifiers(allNpcIdentifiers, getString("clickboxIds"));
        addIdentifiers(allNpcIdentifiers, getString("turboNames"));
        addIdentifiers(allNpcIdentifiers, getString("turboIds"));
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
        addStyleIfPresent(card, identifier, TagStyle.TILE, getString("tileNames"), getString("tileIds"), getColor("tileColor"), getColor("tileFillColor"), getDouble("tileWidth"), getBoolean("tileAA"), getBoolean("tileRave"), getInt("tileRaveSpeed"), getLineType("tileLines"), 0);
        addStyleIfPresent(card, identifier, TagStyle.TRUE_TILE, getString("trueTileNames"), getString("trueTileIds"), getColor("trueTileColor"), getColor("trueTileFillColor"), getDouble("trueTileWidth"), getBoolean("trueTileAA"), getBoolean("trueTileRave"), getInt("trueTileRaveSpeed"), getLineType("trueTileLines"), 0);
        addStyleIfPresent(card, identifier, TagStyle.SW_TILE, getString("swTileNames"), getString("swTileIds"), getColor("swTileColor"), getColor("swTileFillColor"), getDouble("swTileWidth"), getBoolean("swTileAA"), getBoolean("swTileRave"), getInt("swTileRaveSpeed"), getLineType("swTileLines"), 0);
        addStyleIfPresent(card, identifier, TagStyle.SW_TRUE_TILE, getString("swTrueTileNames"), getString("swTrueTileIds"), getColor("swTrueTileColor"), getColor("swTrueTileFillColor"), getDouble("swTrueTileWidth"), getBoolean("swTrueTileAA"), getBoolean("swTrueTileRave"), getInt("swTrueTileRaveSpeed"), getLineType("swTrueTileLines"), 0);
        addStyleIfPresent(card, identifier, TagStyle.HULL, getString("hullNames"), getString("hullIds"), getColor("hullColor"), getColor("hullFillColor"), getDouble("hullWidth"), getBoolean("hullAA"), getBoolean("hullRave"), getInt("hullRaveSpeed"), null, 0);
        addStyleIfPresent(card, identifier, TagStyle.AREA, getString("areaNames"), getString("areaIds"), null, getColor("areaColor"), 0, false, getBoolean("areaRave"), getInt("areaRaveSpeed"), null, 0);
        addStyleIfPresent(card, identifier, TagStyle.OUTLINE, getString("outlineNames"), getString("outlineIds"), getColor("outlineColor"), null, getInt("outlineWidth"), true, getBoolean("outlineRave"), getInt("outlineRaveSpeed"), null, getInt("outlineFeather"));
        addStyleIfPresent(card, identifier, TagStyle.CLICKBOX, getString("clickboxNames"), getString("clickboxIds"), getColor("clickboxColor"), getColor("clickboxFillColor"), getDouble("clickboxWidth"), getBoolean("clickboxAA"), getBoolean("clickboxRave"), getInt("clickboxRaveSpeed"), null, 0);
        addStyleIfPresent(card, identifier, TagStyle.TURBO, getString("turboNames"), getString("turboIds"), null, null, 0, false, false, 0, null, 0);

        return card;
    }

    private void addStyleIfPresent(CardDTO card, String identifier, TagStyle style, String names, String ids, Color outlineColor, Color fillColor, double width, boolean antiAliasing, boolean rave, int raveSpeed, BetterNpcHighlightConfig.lineType lineType, int feather) {
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

            Color finalOutlineColor = getPresetColor(preset, false);
            if (finalOutlineColor == null) finalOutlineColor = outlineColor;

            Color finalFillColor = getPresetColor(preset, true);
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

    private Color getPresetColor(int preset, boolean isFill) {
        switch (preset) {
            case 1:
                return isFill ? getColor("presetFillColor1") : getColor("presetColor1");
            case 2:
                return isFill ? getColor("presetFillColor2") : getColor("presetColor2");
            case 3:
                return isFill ? getColor("presetFillColor3") : getColor("presetColor3");
            case 4:
                return isFill ? getColor("presetFillColor4") : getColor("presetColor4");
            case 5:
                return isFill ? getColor("presetFillColor5") : getColor("presetColor5");
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

    // Helper methods to get raw config values
    private String getString(String key) {
        return configManager.getConfiguration(BetterNpcHighlightConfig.CONFIG_GROUP, key);
    }

    private boolean getBoolean(String key) {
        String value = getString(key);
        return "true".equalsIgnoreCase(value);
    }

    private int getInt(String key) {
        String value = getString(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ignored) {}
        }
        return 0;
    }

    private double getDouble(String key) {
        String value = getString(key);
        if (value != null) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException ignored) {}
        }
        return 0.0;
    }

    private Color getColor(String key) {
        return configManager.getConfiguration(BetterNpcHighlightConfig.CONFIG_GROUP, key, Color.class);
    }

    private BetterNpcHighlightConfig.lineType getLineType(String key) {
        String value = getString(key);
        if (value != null) {
            try {
                return BetterNpcHighlightConfig.lineType.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }
        return BetterNpcHighlightConfig.lineType.REGULAR;
    }
}