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

import java.util.Collections;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.client.config.*;

import java.awt.*;

@ConfigGroup(BetterNpcHighlightConfig.CONFIG_GROUP)
public interface BetterNpcHighlightConfig extends Config {
    String CONFIG_GROUP = "BetterNpcHighlight";

    @ConfigSection(
            name = "Tile",
            description = "Default Tile Settings",
            position = 0,
            closedByDefault = true
    )
    String tileSection = "tile";

    @ConfigSection(
            name = "True Tile",
            description = "Default True Tile Settings",
            position = 1,
            closedByDefault = true
    )
    String trueTileSection = "trueTile";

    @ConfigSection(
            name = "South West Tile",
            description = "Default South West Tile Settings",
            position = 2,
            closedByDefault = true
    )
    String swTileSection = "swTile";

    @ConfigSection(
            name = "South West True Tile",
            description = "Default South West True Tile Settings",
            position = 3,
            closedByDefault = true
    )
    String swTrueTileSection = "swTrueTile";

    @ConfigSection(
            name = "Hull",
            description = "Default Hull Settings",
            position = 4,
            closedByDefault = true
    )
    String hullSection = "hull";

    @ConfigSection(
            name = "Area",
            description = "Default Area Settings",
            position = 5,
            closedByDefault = true
    )
    String areaSection = "area";

    @ConfigSection(
            name = "Outline",
            description = "Default Outline Settings",
            position = 6,
            closedByDefault = true
    )
    String outlineSection = "outline";

    @ConfigSection(
            name = "Clickbox",
            description = "Default Clickbox Settings",
            position = 7,
            closedByDefault = true
    )
    String clickboxSection = "clickbox";

    @ConfigSection(
            name = "TURBO MODE",
            description = "Full send",
            position = 8,
            closedByDefault = true
    )
    String turboSection = "turbo";

    @ConfigSection(
            name = "Slayer",
            description = "Slayer Highlight Settings",
            position = 9,
            closedByDefault = true
    )
    String slayerSection = "slayer";

    @ConfigSection(
            name = "Entity Hider",
            description = "Entity Hider Settings",
            position = 10,
            closedByDefault = true
    )
    String entityHiderSection = "entityHider";

    @ConfigSection(
            name = "Presets",
            description = "Color Presets",
            position = 11,
            closedByDefault = true
    )
    String presetsSection = "presets";

    @ConfigSection(
            name = "Instructions",
            description = "Instructions for various features",
            position = 12,
            closedByDefault = true
    )
    String instructionsSection = "instructions";

    //------------------------------------------------------------//
    // Tile Section
    //------------------------------------------------------------//
    @ConfigItem(
            position = 1,
            keyName = "tileHighlight",
            name = "Tile Highlight",
            description = "Highlights NPCs by tile",
            section = tileSection
    )
    default boolean tileHighlight() {
        return false;
    }

    @Alpha
    @ConfigItem(
            position = 4,
            keyName = "tileColor",
            name = "Highlight Color",
            description = "Sets color of NPC tile highlights",
            section = tileSection
    )
    default Color tileColor() {
        return Color.CYAN;
    }

    @Alpha
    @ConfigItem(
            position = 5,
            keyName = "tileFillColor",
            name = "Fill Color",
            description = "Sets the fill color of npc highlights",
            section = tileSection
    )
    default Color tileFillColor() {
        return new Color(0, 255, 255, 20);
    }

    @Range(min = 0, max = 50)
    @ConfigItem(
            position = 6,
            keyName = "tileWidth",
            name = "Highlight Width",
            description = "Sets the width of npc highlights",
            section = tileSection
    )
    default double tileWidth() {
        return 2;
    }

    @ConfigItem(
            position = 7,
            keyName = "tileAA",
            name = "Anti-Aliasing",
            description = "Turns on anti-aliasing for tile overlays. Makes them smoother.",
            section = tileSection
    )
    default boolean tileAA() {
        return true;
    }

    @ConfigItem(
            position = 8,
            keyName = "tileRave",
            name = "Enable Rave Mode",
            description = "Sets all tile overlays to Rave Mode",
            section = tileSection
    )
    default boolean tileRave() {
        return false;
    }

    @ConfigItem(
            position = 9,
            keyName = "tileRaveSpeed",
            name = "Rave Speed",
            description = "Sets the speed the overlays rave at",
            section = tileSection
    )
    @Units(Units.MILLISECONDS)
    default int tileRaveSpeed() {
        return 6000;
    }

    @ConfigItem(
            position = 10,
            keyName = "tileLines",
            name = "Tile Line Type",
            description = "Sets the tile outline to regular, dashed, or corners only",
            section = tileSection
    )
    default lineType tileLines() {
        return lineType.REGULAR;
    }

    //------------------------------------------------------------//
    // True Tile Section
    //------------------------------------------------------------//
    @ConfigItem(
            position = 1,
            keyName = "trueTileHighlight",
            name = "True Tile Highlight",
            description = "Highlights npc's true tile",
            section = trueTileSection
    )
    default boolean trueTileHighlight() {
        return false;
    }

    @Alpha
    @ConfigItem(
            position = 4,
            keyName = "trueTileColor",
            name = "Highlight Color",
            description = "Sets color of npc highlights",
            section = trueTileSection
    )
    default Color trueTileColor() {
        return Color.CYAN;
    }

    @Alpha
    @ConfigItem(
            position = 5,
            keyName = "trueTileFillColor",
            name = "Fill Color",
            description = "Sets the fill color of npc highlights",
            section = trueTileSection
    )
    default Color trueTileFillColor() {
        return new Color(0, 255, 255, 20);
    }

    @Range(min = 0, max = 50)
    @ConfigItem(
            position = 6,
            keyName = "trueTileWidth",
            name = "Highlight Width",
            description = "Sets the width of npc highlights",
            section = trueTileSection
    )
    default double trueTileWidth() {
        return 2;
    }

    @ConfigItem(
            position = 7,
            keyName = "trueTileAA",
            name = "Anti-Aliasing",
            description = "Turns on anti-aliasing for true tile overlays. Makes them smoother.",
            section = trueTileSection
    )
    default boolean trueTileAA() {
        return true;
    }

    @ConfigItem(
            position = 8,
            keyName = "trueTileRave",
            name = "Enable Rave Mode",
            description = "Sets all true tile overlays to Rave Mode",
            section = trueTileSection
    )
    default boolean trueTileRave() {
        return false;
    }

    @ConfigItem(
            position = 9,
            keyName = "trueTileRaveSpeed",
            name = "Rave Speed",
            description = "Sets the speed the overlays rave at",
            section = trueTileSection
    )
    @Units(Units.MILLISECONDS)
    default int trueTileRaveSpeed() {
        return 6000;
    }

    @ConfigItem(
            position = 10,
            keyName = "trueTileLines",
            name = "True Tile Line Type",
            description = "Sets the true tile outline to regular, dashed, or corners only",
            section = trueTileSection
    )
    default lineType trueTileLines() {
        return lineType.REGULAR;
    }

    //------------------------------------------------------------//
    // SW Tile Section
    //------------------------------------------------------------//
    @ConfigItem(
            position = 1,
            keyName = "swTileHighlight",
            name = "South West Tile Highlight",
            description = "Highlights npc's south west tile",
            section = swTileSection
    )
    default boolean swTileHighlight() {
        return false;
    }

    @Alpha
    @ConfigItem(
            position = 4,
            keyName = "swTileColor",
            name = "Highlight Color",
            description = "Sets color of npc highlights",
            section = swTileSection
    )
    default Color swTileColor() {
        return Color.CYAN;
    }

    @Alpha
    @ConfigItem(
            position = 5,
            keyName = "swTileFillColor",
            name = "Fill Color",
            description = "Sets the fill color of npc highlights",
            section = swTileSection
    )
    default Color swTileFillColor() {
        return new Color(0, 255, 255, 20);
    }

    @Range(min = 0, max = 50)
    @ConfigItem(
            position = 6,
            keyName = "swTileWidth",
            name = "Highlight Width",
            description = "Sets the width of npc highlights",
            section = swTileSection
    )
    default double swTileWidth() {
        return 2;
    }

    @ConfigItem(
            position = 7,
            keyName = "swTileAA",
            name = "Anti-Aliasing",
            description = "Turns on anti-aliasing for the sw tile overlays. Makes them smoother.",
            section = swTileSection
    )
    default boolean swTileAA() {
        return true;
    }

    @ConfigItem(
            position = 8,
            keyName = "swTileRave",
            name = "Enable Rave Mode",
            description = "Sets all sw tile overlays to Rave Mode",
            section = swTileSection
    )
    default boolean swTileRave() {
        return false;
    }

    @ConfigItem(
            position = 9,
            keyName = "swTileRaveSpeed",
            name = "Rave Speed",
            description = "Sets the speed the overlays rave at",
            section = swTileSection
    )
    @Units(Units.MILLISECONDS)
    default int swTileRaveSpeed() {
        return 6000;
    }

    @ConfigItem(
            position = 10,
            keyName = "swTileLines",
            name = "South West Tile Line Type",
            description = "Sets the sw tile outline to regular, dashed, or corners only",
            section = swTileSection
    )
    default lineType swTileLines() {
        return lineType.REGULAR;
    }

    //------------------------------------------------------------//
    // SW True Tile Section
    //------------------------------------------------------------//
    @ConfigItem(
            position = 1,
            keyName = "swTrueTileHighlight",
            name = "South West True Tile Highlight",
            description = "Enables highlighting NPCs by their south west true tile",
            section = swTrueTileSection
    )
    default boolean swTrueTileHighlight() {
        return false;
    }

    @Alpha
    @ConfigItem(
            position = 4,
            keyName = "swTrueTileColor",
            name = "Highlight Color",
            description = "Sets color of npc highlights",
            section = swTrueTileSection
    )
    default Color swTrueTileColor() {
        return Color.CYAN;
    }

    @Alpha
    @ConfigItem(
            position = 5,
            keyName = "swTrueTileFillColor",
            name = "Fill Color",
            description = "Sets the fill color of npc highlights",
            section = swTrueTileSection
    )
    default Color swTrueTileFillColor() {
        return new Color(0, 255, 255, 20);
    }

    @Range(min = 0, max = 50)
    @ConfigItem(
            position = 6,
            keyName = "swTrueTileWidth",
            name = "Highlight Width",
            description = "Sets the width of npc highlights",
            section = swTrueTileSection
    )
    default double swTrueTileWidth() {
        return 2;
    }

    @ConfigItem(
            position = 7,
            keyName = "swTrueTileAA",
            name = "Anti-Aliasing",
            description = "Turns on anti-aliasing for the sw true tile overlays. Makes them smoother.",
            section = swTrueTileSection
    )
    default boolean swTrueTileAA() {
        return true;
    }

    @ConfigItem(
            position = 8,
            keyName = "swTrueTileRave",
            name = "Enable Rave Mode",
            description = "Sets all sw true tile overlays to Rave Mode",
            section = swTrueTileSection
    )
    default boolean swTrueTileRave() {
        return false;
    }

    @ConfigItem(
            position = 9,
            keyName = "swTrueTileRaveSpeed",
            name = "Rave Speed",
            description = "Sets the speed the overlays rave at",
            section = swTrueTileSection
    )
    @Units(Units.MILLISECONDS)
    default int swTrueTileRaveSpeed() {
        return 6000;
    }

    @ConfigItem(
            position = 10,
            keyName = "swTrueTileLines",
            name = "South West True Tile Line Type",
            description = "Sets the sw true tile outline to regular, dashed, or corners only",
            section = swTrueTileSection
    )
    default lineType swTrueTileLines() {
        return lineType.REGULAR;
    }

    //------------------------------------------------------------//
    // Hull Section
    //------------------------------------------------------------//
    @ConfigItem(
            position = 1,
            keyName = "hullHighlight",
            name = "Hull Highlight",
            description = "Highlight npc's hull",
            section = hullSection
    )
    default boolean hullHighlight() {
        return false;
    }

    @Alpha
    @ConfigItem(
            position = 4,
            keyName = "hullColor",
            name = "Highlight Color",
            description = "Sets color of npc highlights",
            section = hullSection
    )
    default Color hullColor() {
        return Color.CYAN;
    }

    @Alpha
    @ConfigItem(
            position = 5,
            keyName = "hullFillColor",
            name = "Fill Color",
            description = "Sets the fill color of npc highlights",
            section = hullSection
    )
    default Color hullFillColor() {
        return new Color(0, 255, 255, 20);
    }

    @Range(min = 0, max = 50)
    @ConfigItem(
            position = 6,
            keyName = "hullWidth",
            name = "Highlight Width",
            description = "Sets the width of npc highlights",
            section = hullSection
    )
    default double hullWidth() {
        return 2;
    }

    @ConfigItem(
            position = 7,
            keyName = "hullAA",
            name = "Anti-Aliasing",
            description = "Turns on anti-aliasing for hull overlays. Makes them smoother.",
            section = hullSection
    )
    default boolean hullAA() {
        return true;
    }

    @ConfigItem(
            position = 8,
            keyName = "hullRave",
            name = "Enable Rave Mode",
            description = "Sets all hull overlays to Rave Mode",
            section = hullSection
    )
    default boolean hullRave() {
        return false;
    }

    @ConfigItem(
            position = 9,
            keyName = "hullRaveSpeed",
            name = "Rave Speed",
            description = "Sets the speed the overlays rave at",
            section = hullSection
    )
    @Units(Units.MILLISECONDS)
    default int hullRaveSpeed() {
        return 6000;
    }

    //------------------------------------------------------------//
    // Area Section
    //------------------------------------------------------------//
    @ConfigItem(
            position = 1,
            keyName = "areaHighlight",
            name = "Area Highlight",
            description = "Highlights npc's area",
            section = areaSection
    )
    default boolean areaHighlight() {
        return false;
    }

    @Alpha
    @ConfigItem(
            position = 4,
            keyName = "areaColor",
            name = "Highlight Color",
            description = "Sets color of npc highlights",
            section = areaSection
    )
    default Color areaColor() {
        return new Color(0, 255, 255, 50);
    }

    @ConfigItem(
            position = 5,
            keyName = "areaRave",
            name = "Enable Rave Mode",
            description = "Sets all area overlays to Rave Mode",
            section = areaSection
    )
    default boolean areaRave() {
        return false;
    }

    @ConfigItem(
            position = 6,
            keyName = "areaRaveSpeed",
            name = "Rave Speed",
            description = "Sets the speed the overlays rave at",
            section = areaSection
    )
    @Units(Units.MILLISECONDS)
    default int areaRaveSpeed() {
        return 6000;
    }

    //------------------------------------------------------------//
    // Outline Section
    //------------------------------------------------------------//
    @ConfigItem(
            position = 1,
            keyName = "outlineHighlight",
            name = "Outline Highlight",
            description = "Highlights npc's outline",
            section = outlineSection
    )
    default boolean outlineHighlight() {
        return false;
    }

    @Alpha
    @ConfigItem(
            position = 4,
            keyName = "outlineColor",
            name = "Highlight Color",
            description = "Sets color of npc highlights",
            section = outlineSection
    )
    default Color outlineColor() {
        return Color.CYAN;
    }

    @Range(min = 0, max = 50)
    @ConfigItem(
            position = 5,
            keyName = "outlineWidth",
            name = "Outline Width",
            description = "Sets the width of outline highlights",
            section = outlineSection
    )
    default int outlineWidth() {
        return 2;
    }

    @Range(min = 0, max = 5)
    @ConfigItem(
            position = 6,
            keyName = "outlineFeather",
            name = "Outline Feather",
            description = "Sets the feather of the outline highlights",
            section = outlineSection
    )
    default int outlineFeather() {
        return 2;
    }

    @ConfigItem(
            position = 7,
            keyName = "outlineRave",
            name = "Enable Rave Mode",
            description = "Sets all outline overlays to Rave Mode",
            section = outlineSection
    )
    default boolean outlineRave() {
        return false;
    }

    @ConfigItem(
            position = 8,
            keyName = "outlineRaveSpeed",
            name = "Rave Speed",
            description = "Sets the speed the overlays rave at",
            section = outlineSection
    )
    @Units(Units.MILLISECONDS)
    default int outlineRaveSpeed() {
        return 6000;
    }

    //------------------------------------------------------------//
    // Clickbox Section
    //------------------------------------------------------------//
    @ConfigItem(
            position = 1,
            keyName = "clickboxHighlight",
            name = "Clickbox Highlight",
            description = "Highlights NPCs by clickbox",
            section = clickboxSection
    )
    default boolean clickboxHighlight() {
        return false;
    }

    @Alpha
    @ConfigItem(
            position = 4,
            keyName = "clickboxColor",
            name = "Highlight Color",
            description = "Sets color of NPC clickbox highlights",
            section = clickboxSection
    )
    default Color clickboxColor() {
        return Color.CYAN;
    }

    @Alpha
    @ConfigItem(
            position = 5,
            keyName = "clickboxFillColor",
            name = "Fill Color",
            description = "Sets the fill color of NPC clickbox highlights",
            section = clickboxSection
    )
    default Color clickboxFillColor() {
        return new Color(0, 255, 255, 20);
    }

    @Range(min = 0, max = 50)
    @ConfigItem(
            position = 6,
            keyName = "clickboxWidth",
            name = "Highlight Width",
            description = "Sets the width of NPC clickbox highlights",
            section = clickboxSection
    )
    default double clickboxWidth() {
        return 1;
    }

    @ConfigItem(
            position = 7,
            keyName = "clickboxAA",
            name = "Anti-Aliasing",
            description = "Turns on anti-aliasing for the clickboxes. Makes them smoother.",
            section = clickboxSection
    )
    default boolean clickboxAA() {
        return true;
    }

    @ConfigItem(
            position = 8,
            keyName = "clickboxRave",
            name = "Enable Rave Mode",
            description = "Sets all clickbox overlays to Rave Mode",
            section = clickboxSection
    )
    default boolean clickboxRave() {
        return false;
    }

    @ConfigItem(
            position = 9,
            keyName = "clickboxRaveSpeed",
            name = "Rave Speed",
            description = "Sets the speed the overlays rave at",
            section = clickboxSection
    )
    @Units(Units.MILLISECONDS)
    default int clickboxRaveSpeed() {
        return 6000;
    }

    //------------------------------------------------------------//
    // Turbo Section
    //------------------------------------------------------------//
    @ConfigItem(
            position = 0,
            name = "—————— Epilepsy Warning ——————",
            keyName = "epilepsyWarning",
            description = "A warning message will pop up if you try to enable Turbo Highlight. Turbo mode highlights NPCs switching between all styles and colors.",
            section = turboSection
    )
    void epilepsyWarning();

    @ConfigItem(
            position = 1,
            keyName = "turboHighlight",
            name = "Turbo Highlight",
            description = "Highlights NPCs in turbo mode",
            section = turboSection
    )
    default boolean turboHighlight() {
        return false;
    }

    //------------------------------------------------------------//
    // Slayer Section
    //------------------------------------------------------------//
    @ConfigItem(
            position = 1,
            keyName = "slayerHighlight",
            name = "Slayer Task Highlight",
            description = "Highlights NPCs that are assigned as your slayer task <br>Uses the 'Slayer' plugin. Keep it on!",
            section = slayerSection
    )
    default boolean slayerHighlight() {
        return false;
    }

    @ConfigItem(
            position = 2,
            keyName = "taskHighlightStyle",
            name = "Slayer Tag Style",
            description = "Picks the highlight style you want for NPCs on your slayer task",
            section = slayerSection
    )
    default TagStyle taskHighlightStyle() {
        return TagStyle.TILE;
    }

    @Alpha
    @ConfigItem(
            position = 4,
            keyName = "taskColor",
            name = "Highlight Color",
            description = "Sets color of slayer task npc highlights",
            section = slayerSection
    )
    default Color taskColor() {
        return new Color(224, 60, 49, 255);
    }

    @Alpha
    @ConfigItem(
            position = 5,
            keyName = "taskFillColor",
            name = "Fill Color",
            description = "Sets the fill color of slayer task npc highlights",
            section = slayerSection
    )
    default Color taskFillColor() {
        return new Color(224, 60, 49, 20);
    }

    @ConfigItem(
            position = 6,
            keyName = "slayerAA",
            name = "Anti-Aliasing",
            description = "Turns on anti-aliasing for the slayer highlights. Makes them smoother.",
            section = slayerSection
    )
    default boolean slayerAA() {
        return true;
    }

    @ConfigItem(
            position = 7,
            keyName = "slayerRave",
            name = "Enable Rave Mode",
            description = "Sets all slayer overlays to Rave Mode",
            section = slayerSection
    )
    default boolean slayerRave() {
        return false;
    }

    @ConfigItem(
            position = 8,
            keyName = "slayerRaveSpeed",
            name = "Rave Speed",
            description = "Sets the speed the overlays rave at",
            section = slayerSection
    )
    @Units(Units.MILLISECONDS)
    default int slayerRaveSpeed() {
        return 6000;
    }

    //------------------------------------------------------------//
    // Entity Hider Section
    //------------------------------------------------------------//
    @ConfigItem(
            position = 1,
            keyName = "entityHiderToggle",
            name = "Entity Hider",
            description = "Enables hiding of specific NPCs",
            section = entityHiderSection
    )
    default boolean entityHiderToggle() {
        return false;
    }

    @ConfigItem(
            position = 2,
            keyName = "entityHiderCommands",
            name = "Entity Hider Commands",
            description = "Enables the use of commands to add/remove NPCs to the Names/IDs list <br>Read the guide in Instructions section",
            section = entityHiderSection
    )
    default boolean entityHiderCommands() {
        return true;
    }

    //------------------------------------------------------------//
    // Presets Section
    //------------------------------------------------------------//
    @ConfigItem(
            position = 1,
            keyName = "presetColorAmount",
            name = "Preset Colors Amount",
            description = "The amount of preset colors you want in the Tag sub-menu",
            section = presetsSection
    )
    default presetColorAmount presetColorAmount() {
        return presetColorAmount.ZERO;
    }

    @Alpha
    @ConfigItem(
            position = 2,
            keyName = "presetColor1",
            name = "Preset Color 1",
            description = "Sets color for the first preset color",
            section = presetsSection
    )
    default Color presetColor1() {
        return new Color(224, 60, 49, 255);
    }

    @Alpha
    @ConfigItem(
            position = 3,
            keyName = "presetFillColor1",
            name = "Preset Fill Color 1",
            description = "Sets the fill color for the first preset color",
            section = presetsSection
    )
    default Color presetFillColor1() {
        return new Color(224, 60, 49, 20);
    }

    @Alpha
    @ConfigItem(
            position = 4,
            keyName = "presetColor2",
            name = "Preset Color 2",
            description = "Sets color for the second preset color",
            section = presetsSection
    )
    default Color presetColor2() {
        return new Color(37, 197, 79, 255);
    }

    @Alpha
    @ConfigItem(
            position = 5,
            keyName = "presetFillColor2",
            name = "Preset Fill Color 2",
            description = "Sets the fill color for the second preset color",
            section = presetsSection
    )
    default Color presetFillColor2() {
        return new Color(37, 197, 79, 20);
    }

    @Alpha
    @ConfigItem(
            position = 6,
            keyName = "presetColor3",
            name = "Preset Color 3",
            description = "Sets color for the third preset color",
            section = presetsSection
    )
    default Color presetColor3() {
        return new Color(207, 138, 253, 255);
    }

    @Alpha
    @ConfigItem(
            position = 7,
            keyName = "presetFillColor3",
            name = "Preset Fill Color 3",
            description = "Sets the fill color for the third preset color",
            section = presetsSection
    )
    default Color presetFillColor3() {
        return new Color(207, 138, 253, 20);
    }

    @Alpha
    @ConfigItem(
            position = 8,
            keyName = "presetColor4",
            name = "Preset Color 4",
            description = "Sets color for the fourth preset color",
            section = presetsSection
    )
    default Color presetColor4() {
        return new Color(38, 255, 169, 255);
    }

    @Alpha
    @ConfigItem(
            position = 9,
            keyName = "presetFillColor4",
            name = "Preset Fill Color 4",
            description = "Sets the fill color for the fourth preset color",
            section = presetsSection
    )
    default Color presetFillColor4() {
        return new Color(38, 255, 169, 20);
    }

    @Alpha
    @ConfigItem(
            position = 10,
            keyName = "presetColor5",
            name = "Preset Color 5",
            description = "Sets color for the fifth preset color",
            section = presetsSection
    )
    default Color presetColor5() {
        return new Color(0, 150, 200, 255);
    }

    @Alpha
    @ConfigItem(
            position = 11,
            keyName = "presetFillColor5",
            name = "Preset Fill Color 5",
            description = "Sets the fill color for the fifth preset color",
            section = presetsSection
    )
    default Color presetFillColor5() {
        return new Color(0, 150, 200, 20);
    }

    //------------------------------------------------------------//
    // Instructions Section
    //------------------------------------------------------------//
    @ConfigItem(
            position = 0,
            keyName = "tagLegend",
            name = "Tag/Untag Legend",
            description = "Legend for highlights to use with the !tag and !untag commands",
            section = instructionsSection
    )
    default String tagLegend() {
        return "### Valid Style Format:\n\n" +
                "Tile = \"t\", \"tile\" \n" +
                "True Tile = \"tt\", \"truetile\" \n" +
                "SW Tile = \"sw\", \"swt\", \"swtile\", \"southwesttile\", \"southwest\", \"southwestt\" \n" +
                "SW True Tile = \"swtt\", \"swtruetile\", \"southwesttruetile\", \"southwesttt\" \n" +
                "Hull = \"h\", \"hull\" \n" +
                "Area = \"a\", \"area\" \n" +
                "Outline = \"o\", \"outline\" \n" +
                "Clickbox = \"c\", \"clickbox\", \"box\" \n" +
                "Turbo = \"tu\", \"turbo\" \n";
    }

    @ConfigItem(
            position = 1,
            keyName = "tagInstructions",
            name = "Tag/Untag Instructions",
            description = "Instructions to use the !tag and !untag commands",
            section = instructionsSection
    )
    default String tagInstructions() {
        return "### Tag Format:\n\n" +
                "!tag[style] [npc name]\n" +
                "!tag[style] [npc id]\n" +
                "!untag[style] [npc name]\n" +
                "!untag[style] [npc id]\n" +
                "---------------------\n" +
                "### Example:\n\n" +
                "\"!tagswt cow\" -> This would add \"cow\" to the SW True Tile names list\n\n" +
                "\"!untago 1234\" -> This would remove \"1234\" from the Outline IDs list\n";
    }

    @ConfigItem(
            position = 2,
            keyName = "presetInstructions",
            name = "Preset Instructions",
            description = "Instructions to use presets with the !tag and !untag commands",
            section = instructionsSection
    )
    default String presetInstructions() {
        return "### Preset Tag Format:\n\n" +
                "!tag[style] [npc name]:[preset #]\n" +
                "!tag[style] [npc id]:[preset #]\n" +
                "!untag[style] [npc name]:[preset #]\n" +
                "!untag[style] [npc id]:[preset #]\n" +
                "---------------------\n" +
                "### Example:\n\n" +
                "\"!tagswt cow:2\" -> This would add \"cow\" to the SW True Tile names list with preset color 2\n" +
                "# Tagging an NPC that already has a preset changes the preset to the new one\n\n" +
                "Untagging is the same, regardless of preset\n";
    }

    @ConfigItem(
            position = 3,
            keyName = "hideInstructions",
            name = "Hide/Unhide Instructions",
            description = "Instructions to use the !hide and !unhide commands",
            section = instructionsSection
    )
    default String hideInstructions() {
        return "### Format:\n\n" +
                "!hide [npc name]\n" +
                "!hide [npc id]\n" +
                "!unhide [npc name]\n" +
                "!unhide [npc id]\n" +
                "---------------------\n" +
                "### Example:\n\n" +
                "\"!hide cow\" -> This would add \"cow\" to the Entity Hider names list\n\n" +
                "\"!unhide 1234\" -> This would remove \"1234\" from the Entity Hider IDs list\n";
    }

    //------------------------------------------------------------//
    // No Section
    //------------------------------------------------------------//
    @ConfigItem(
            position = 13,
            keyName = "tagStyleMode",
            name = "Default Tag Style",
            description = "Sets the default highlight style for new cards and for the 'Tag' right-click option.")
    default DefaultHighlightStyle tagStyleMode() {
        return DefaultHighlightStyle.TILE;
    }

    @ConfigItem(
            position = 14,
            keyName = "tagCommands",
            name = "Tag Commands",
            description = "Enables the use of commands to add/remove NPCs to the Names/IDs list <br>Read the guide in Instructions section"
    )
    default boolean tagCommands() {
        return true;
    }

    @ConfigItem(
            position = 15,
            keyName = "highlightMenuNames",
            name = "Highlight Menu Names",
            description = "Highlights names in right click menu entry"
    )
    default boolean highlightMenuNames() {
        return false;
    }

    @ConfigItem(
            position = 16,
            keyName = "ignoreDeadNpcs",
            name = "Ignore Dead NPCs",
            description = "Doesn't highlight dead NPCs"
    )
    default boolean ignoreDeadNpcs() {
        return false;
    }

    @ConfigItem(
            position = 17,
            keyName = "drawBeneathNpcs",
            name = "Draw Overlay Beneath NPCs",
            description = "Enable drawing the overlay beneath all NPCs"
    )
    default boolean drawBeneathNpcs() {
        return false;
    }

    @Range(max = 20)
    @ConfigItem(
            position = 18,
            keyName = "drawBeneathLimit",
            name = "Draw Beneath Limit",
            description = "Sets the amount of NPCs to have the overlay draw beneath. The higher the number, the more it affects FPS"
    )
    default int drawBeneathLimit() {
        return 10;
    }

    @ConfigItem(
            position = 19,
            keyName = "renderDistance",
            name = "Render Distance",
            description = "Limits overlays to be drawn to within the chosen distance from the local player. <br>Short = 7 tiles, Medium = 11 tiles"
    )
    default renderDistance renderDistance() {
        return renderDistance.NONE;
    }

    @ConfigItem(
            position = 20,
            keyName = "highlightPets",
            name = "Highlight pets",
            description = "Highlights followers/pets that are in any of your lists"
    )
    default boolean highlightPets() {
        return false;
    }

    @ConfigItem(
            position = 21,
            keyName = "deadNpcMenuNames",
            name = "Highlight Dead NPC Menu Names",
            description = "Highlights dead NPC names in right click menu"
    )
    default boolean deadNpcMenuNames() {
        return false;
    }

    @ConfigItem(
            position = 24,
            keyName = "deadNpcMenuColor",
            name = "Dead NPC Menu Color",
            description = "Sets the color of dead NPC names in right click menu"
    )
    Color deadNpcMenuColor();

    @ConfigItem(
            position = 25,
            keyName = "respawnTimer",
            name = "Respawn Timer",
            description = "Marks tile and shows timer for when a marker NPC will respawn"
    )
    default respawnTimerMode respawnTimer() {
        return respawnTimerMode.OFF;
    }

    @Alpha
    @ConfigItem(
            position = 26,
            keyName = "respawnTimerColor",
            name = "Respawn Time Color",
            description = "Sets the color of the text for Respawn Timer"
    )
    default Color respawnTimerColor() {
        return Color.WHITE;
    }

    @Alpha
    @ConfigItem(
            position = 27,
            keyName = "respawnOutlineColor",
            name = "Respawn Outline Color",
            description = "Sets the color of the tile for Respawn Timer"
    )
    default Color respawnOutlineColor() {
        return Color.CYAN;
    }

    @Alpha
    @ConfigItem(
            position = 28,
            keyName = "respawnFillColor",
            name = "Respawn Fill Color",
            description = "Sets the fill color of the tile for Respawn Timer"
    )
    default Color respawnFillColor() {
        return new Color(0, 255, 255, 20);
    }

    @Range(min = 1, max = 10)
    @ConfigItem(
            position = 29,
            keyName = "respawnTileWidth",
            name = "Respawn Tile Width",
            description = "Sets the width of the tile for Respawn Timer"
    )
    default int respawnTileWidth() {
        return 2;
    }

    @ConfigItem(
            position = 31,
            keyName = "fontBackground",
            name = "Font Background",
            description = "Puts an outline, shadow, or nothing behind font overlays"
    )
    default background fontBackground() {
        return background.SHADOW;
    }

    @ConfigItem(
            position = 32,
            keyName = "npcMinimapMode",
            name = "Highlight Minimap",
            description = "Highlights NPC on minimap and/or displays name"
    )
    default npcMinimapMode npcMinimapMode() {
        return npcMinimapMode.OFF;
    }

    //------------------------------------------------------------//
    // Enums
    //------------------------------------------------------------//
    @Getter
    enum DefaultHighlightStyle {
        NONE,
        TILE,
        TRUE_TILE,
        SW_TILE,
        SW_TRUE_TILE,
        HULL,
        AREA,
        OUTLINE,
        CLICKBOX,
        TURBO
    }

    @Getter
    @RequiredArgsConstructor
    enum lineType {
        REGULAR("Regular"),
        DASHED("Dashed"),
        CORNER("Corners"),
        ;

        @Getter
        private final String group;

        @Override
        public String toString() {
            return group;
        }
    }

    @Getter
    @RequiredArgsConstructor
    enum presetColorAmount {
        ZERO("None"),
        ONE("One"),
        TWO("Two"),
        THREE("Three"),
        FOUR("Four"),
        FIVE("Five"),
        ;

        @Getter
        private final String group;

        @Override
        public String toString() {
            return group;
        }
    }

    @Getter
    @RequiredArgsConstructor
    enum background {
        OFF("None"),
        SHADOW("Shadow"),
        OUTLINE("Outline"),
        ;

        @Getter
        private final String group;

        @Override
        public String toString() {
            return group;
        }
    }

    @Getter
    @AllArgsConstructor
    enum renderDistance {
        SHORT("Short", 7),
        MED("Medium", 11),
        NONE("No Limit", 0); //14 = max distance

        private final String group;
        private final int distance;

        @Override
        public String toString() {
            return group;
        }
    }

    enum respawnTimerMode {
        OFF,
        TICKS,
        SECONDS
    }

    enum npcMinimapMode {
        OFF,
        DOT,
        NAME,
        BOTH
    }
}
