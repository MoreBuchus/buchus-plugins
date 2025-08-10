package com.betternpchighlight;

import net.runelite.client.config.ConfigManager;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to migrate from legacy config-based system to new panel system
 */
public class ConfigMigrator {

    private static final String CONFIG_GROUP = "betternpchighlight";

    /**
     * Migrate legacy config entries to the new panel format
     */
    public static boolean migrateLegacyConfig(ConfigManager configManager, BetterNpcHighlightPanel panel) {
        List<BetterNpcHighlightPanel.NpcHighlightEntry> migratedEntries = new ArrayList<>();
        boolean foundLegacyData = false;

        // Check if migration has already been done
        String migrationFlag = configManager.getConfiguration(CONFIG_GROUP, "migrationCompleted");
        if ("true".equals(migrationFlag)) {
            return false; // Already migrated
        }

        // Migrate each highlight type
        foundLegacyData |= migrateLegacyEntries(configManager, "tileNames", "tileIds", "Tile",
                Color.CYAN, new Color(0,255,255,20), migratedEntries);
        foundLegacyData |= migrateLegacyEntries(configManager, "trueTileNames", "trueTileIds", "True Tile",
                Color.CYAN, new Color(0,255,255,20), migratedEntries);
        foundLegacyData |= migrateLegacyEntries(configManager, "swTileNames", "swTileIds", "SW Tile",
                Color.CYAN, new Color(0,255,255,20), migratedEntries);
        foundLegacyData |= migrateLegacyEntries(configManager, "swTrueTileNames", "swTrueTileIds", "SW True Tile",
                Color.CYAN, new Color(0,255,255,20), migratedEntries);
        foundLegacyData |= migrateLegacyEntries(configManager, "hullNames", "hullIds", "Hull",
                Color.CYAN, new Color(0,255,255,20), migratedEntries);
        foundLegacyData |= migrateLegacyEntries(configManager, "areaNames", "areaIds", "Area",
                Color.CYAN, null, migratedEntries);
        foundLegacyData |= migrateLegacyEntries(configManager, "outlineNames", "outlineIds", "Outline",
                Color.CYAN, null, migratedEntries);
        foundLegacyData |= migrateLegacyEntries(configManager, "clickboxNames", "clickboxIds", "Clickbox",
                Color.CYAN, new Color(0,255,255,20), migratedEntries);
        foundLegacyData |= migrateLegacyEntries(configManager, "turboNames", "turboIds", "Turbo",
                Color.CYAN, new Color(0,255,255,20), migratedEntries);

        if (foundLegacyData && !migratedEntries.isEmpty()) {
            // Apply migrated entries to panel
            panel.clearAndLoadEntries(migratedEntries);

            // Save to new format
            panel.saveAllCards(configManager, CONFIG_GROUP);

            // Mark migration as complete
            configManager.setConfiguration(CONFIG_GROUP, "migrationCompleted", "true");

            // Optionally clear legacy config entries to clean up
            //clearLegacyConfigEntries(configManager);

            return true;
        }

        return false;
    }

    private static boolean migrateLegacyEntries(ConfigManager configManager, String namesKey, String idsKey,
                                                String tagStyle, Color defaultOutline, Color defaultFill,
                                                List<BetterNpcHighlightPanel.NpcHighlightEntry> migratedEntries) {
        boolean foundData = false;

        // Migrate names
        String namesConfig = configManager.getConfiguration(CONFIG_GROUP, namesKey);
        if (namesConfig != null && !namesConfig.trim().isEmpty()) {
            foundData = true;
            String[] names = namesConfig.split(",");
            for (String name : names) {
                if (!name.trim().isEmpty()) {
                    Color outlineColor = defaultOutline;
                    Color fillColor = defaultFill != null ? defaultFill : new Color(0, 0, 0, 0);

                    // Handle preset colors (name:preset format)
                    String cleanName = name.trim();
                    if (name.contains(":")) {
                        String[] parts = name.split(":");
                        cleanName = parts[0].trim();
                        try {
                            int preset = Integer.parseInt(parts[1].trim());
                            outlineColor = getPresetColor(configManager, preset, defaultOutline);
                            fillColor = getPresetFillColor(configManager, preset, defaultFill);
                        } catch (NumberFormatException e) {
                            // Invalid preset, use defaults
                        }
                    }

                    migratedEntries.add(new BetterNpcHighlightPanel.NpcHighlightEntry(
                            cleanName,
                            tagStyle,
                            outlineColor,
                            fillColor,
                            false,  // hideNpc default
                            false,  // drawUnder default
                            false,  // displayName default
                            Color.CYAN, // default displayNameColor
                            false   // highlightDead default
                    ));

                }
            }
        }

        // Migrate IDs
        String idsConfig = configManager.getConfiguration(CONFIG_GROUP, idsKey);
        if (idsConfig != null && !idsConfig.trim().isEmpty()) {
            foundData = true;
            String[] ids = idsConfig.split(",");
            for (String id : ids) {
                if (!id.trim().isEmpty()) {
                    Color outlineColor = defaultOutline;
                    Color fillColor = defaultFill != null ? defaultFill : new Color(0, 0, 0, 0);

                    // Handle preset colors (id:preset format)
                    String cleanId = id.trim();
                    if (id.contains(":")) {
                        String[] parts = id.split(":");
                        cleanId = parts[0].trim();
                        try {
                            int preset = Integer.parseInt(parts[1].trim());
                            outlineColor = getPresetColor(configManager, preset, defaultOutline);
                            fillColor = getPresetFillColor(configManager, preset, defaultFill);
                        } catch (NumberFormatException e) {
                            // Invalid preset, use defaults
                        }
                    }

                    migratedEntries.add(new BetterNpcHighlightPanel.NpcHighlightEntry(
                            cleanId,
                            tagStyle,
                            outlineColor,
                            fillColor,
                            false,
                            false,
                            false,
                            Color.CYAN,
                            false
                    ));

                }
            }
        }

        return foundData;
    }

    private static Color getPresetColor(ConfigManager configManager, int preset, Color defaultColor) {
        switch (preset) {
            case 1:
                return getColorFromConfig(configManager, "presetColor1", defaultColor);
            case 2:
                return getColorFromConfig(configManager, "presetColor2", defaultColor);
            case 3:
                return getColorFromConfig(configManager, "presetColor3", defaultColor);
            case 4:
                return getColorFromConfig(configManager, "presetColor4", defaultColor);
            case 5:
                return getColorFromConfig(configManager, "presetColor5", defaultColor);
            default:
                return defaultColor;
        }
    }

    private static Color getPresetFillColor(ConfigManager configManager, int preset, Color defaultColor) {
        switch (preset) {
            case 1:
                return getColorFromConfig(configManager, "presetFillColor1", defaultColor);
            case 2:
                return getColorFromConfig(configManager, "presetFillColor2", defaultColor);
            case 3:
                return getColorFromConfig(configManager, "presetFillColor3", defaultColor);
            case 4:
                return getColorFromConfig(configManager, "presetFillColor4", defaultColor);
            case 5:
                return getColorFromConfig(configManager, "presetFillColor5", defaultColor);
            default:
                return defaultColor;
        }
    }

    private static Color getColorFromConfig(ConfigManager configManager, String key, Color defaultColor) {
        try {
            String colorStr = configManager.getConfiguration(CONFIG_GROUP, key);
            if (colorStr != null && !colorStr.isEmpty()) {
                return Color.decode(colorStr);
            }
        } catch (Exception e) {
            // Invalid color format, use default
        }
        return defaultColor;
    }

    private static void clearLegacyConfigEntries(ConfigManager configManager) {
        // List of legacy config keys to clear
        String[] legacyKeys = {
                "tileNames", "tileIds", "trueTileNames", "trueTileIds",
                "swTileNames", "swTileIds", "swTrueTileNames", "swTrueTileIds",
                "hullNames", "hullIds", "areaNames", "areaIds",
                "outlineNames", "outlineIds", "clickboxNames", "clickboxIds",
                "turboNames", "turboIds"
        };

        for (String key : legacyKeys) {
            configManager.unsetConfiguration(CONFIG_GROUP, key);
        }
    }

    /**
     * Check if legacy configuration exists
     */
    public static boolean hasLegacyConfig(ConfigManager configManager) {
        String migrationFlag = configManager.getConfiguration(CONFIG_GROUP, "migrationCompleted");
        if ("true".equals(migrationFlag)) {
            return false;
        }

        // Check for any legacy config entries
        String[] legacyKeys = {
                "tileNames", "tileIds", "trueTileNames", "trueTileIds",
                "swTileNames", "swTileIds", "swTrueTileNames", "swTrueTileIds",
                "hullNames", "hullIds", "areaNames", "areaIds",
                "outlineNames", "outlineIds", "clickboxNames", "clickboxIds",
                "turboNames", "turboIds"
        };

        for (String key : legacyKeys) {
            String value = configManager.getConfiguration(CONFIG_GROUP, key);
            if (value != null && !value.trim().isEmpty()) {
                return true;
            }
        }

        return false;
    }
}