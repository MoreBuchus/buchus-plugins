package com.betternpchighlight.data;

import com.betternpchighlight.BetterNpcHighlightConfig;
import com.betternpchighlight.HighlightInfo;
import com.betternpchighlight.TagStyle;

import java.awt.*;

public class NpcHighlightEntry {
    public String nameOrId;
    public TagStyle tagStyle;
    public Color outlineColor;
    public Color fillColor;
    public boolean hideNpc;
    public boolean drawUnder;
    public boolean displayName;
    public Color displayNameColor;
    public boolean overrideDisplayNameColor;
    public boolean highlightDead;
    public boolean raveOutline;
    public boolean raveFill;
    public int raveSpeed;
    public HighlightInfo.TileStyle tileStyle;
    public double outlineWidth;
    public boolean antiAliasing;
    public int outlineFeather;

    public NpcHighlightEntry(String nameOrId, TagStyle tagStyle, Color outlineColor, Color fillColor,
                             boolean hideNpc, boolean drawUnder, boolean displayName, Color displayNameColor, boolean overrideDisplayNameColor,
                             boolean highlightDead, boolean raveOutline, boolean raveFill, int raveSpeed, HighlightInfo.TileStyle tileStyle,
                             double outlineWidth, boolean antiAliasing, int outlineFeather) {
        this.nameOrId = nameOrId;
        this.tagStyle = tagStyle;
        this.outlineColor = outlineColor;
        this.fillColor = fillColor;
        this.hideNpc = hideNpc;
        this.drawUnder = drawUnder;
        this.displayName = displayName;
        this.displayNameColor = displayNameColor;
        this.overrideDisplayNameColor = overrideDisplayNameColor;
        this.highlightDead = highlightDead;
        this.raveOutline = raveOutline;
        this.raveFill = raveFill;
        this.raveSpeed = raveSpeed;
        this.tileStyle = tileStyle;
        this.outlineWidth = outlineWidth;
        this.antiAliasing = antiAliasing;
        this.outlineFeather = outlineFeather;
    }

    /**
     * Constructor for creating a new entry with default values.
     */
    public NpcHighlightEntry(String nameOrId, TagStyle tagStyle) {
        this(nameOrId,
                tagStyle,
                Color.CYAN, // outlineColor
                new Color(0, 255, 255, 20), // fillColor
                false, // hideNpc
                false, // drawUnder
                false, // displayName
                null,  // displayNameColor
                false, // overrideDisplayNameColor
                false, // highlightDead
                false, // raveOutline
                false, // raveFill
                6000,  // raveSpeed
                HighlightInfo.TileStyle.REGULAR, // tileStyle
                2.0,   // outlineWidth
                true,  // antiAliasing
                2);    // outlineFeather
    }

    /**
     * Constructor for creating a new entry with default values from config.
     */
    public NpcHighlightEntry(String nameOrId, TagStyle tagStyle, BetterNpcHighlightConfig config) {
        this.nameOrId = nameOrId;
        this.tagStyle = tagStyle;
        this.hideNpc = false;
        this.drawUnder = false;
        this.displayName = false;
        this.displayNameColor = null;
        this.overrideDisplayNameColor = false;
        this.highlightDead = false;

        // Set defaults based on the tag style from the config
        if (tagStyle != null) {
            switch (tagStyle) {
                case TILE:
                    this.outlineColor = config.tileColor();
                    this.fillColor = config.tileFillColor();
                    this.raveOutline = config.tileRave();
                    this.raveFill = config.tileRave();
                    this.raveSpeed = config.tileRaveSpeed();
                    this.tileStyle = HighlightInfo.TileStyle.valueOf(config.tileLines().name());
                    this.outlineWidth = config.tileWidth();
                    this.antiAliasing = config.tileAA();
                    this.outlineFeather = 0;
                    break;
                case TRUE_TILE:
                    this.outlineColor = config.trueTileColor();
                    this.fillColor = config.trueTileFillColor();
                    this.raveOutline = config.trueTileRave();
                    this.raveFill = config.trueTileRave();
                    this.raveSpeed = config.trueTileRaveSpeed();
                    this.tileStyle = HighlightInfo.TileStyle.valueOf(config.trueTileLines().name());
                    this.outlineWidth = config.trueTileWidth();
                    this.antiAliasing = config.trueTileAA();
                    this.outlineFeather = 0;
                    break;
                case SW_TILE:
                    this.outlineColor = config.swTileColor();
                    this.fillColor = config.swTileFillColor();
                    this.raveOutline = config.swTileRave();
                    this.raveFill = config.swTileRave();
                    this.raveSpeed = config.swTileRaveSpeed();
                    this.tileStyle = HighlightInfo.TileStyle.valueOf(config.swTileLines().name());
                    this.outlineWidth = config.swTileWidth();
                    this.antiAliasing = config.swTileAA();
                    this.outlineFeather = 0;
                    break;
                case SW_TRUE_TILE:
                    this.outlineColor = config.swTrueTileColor();
                    this.fillColor = config.swTrueTileFillColor();
                    this.raveOutline = config.swTrueTileRave();
                    this.raveFill = config.swTrueTileRave();
                    this.raveSpeed = config.swTrueTileRaveSpeed();
                    this.tileStyle = HighlightInfo.TileStyle.valueOf(config.swTrueTileLines().name());
                    this.outlineWidth = config.swTrueTileWidth();
                    this.antiAliasing = config.swTrueTileAA();
                    this.outlineFeather = 0;
                    break;
                case HULL:
                    this.outlineColor = config.hullColor();
                    this.fillColor = config.hullFillColor();
                    this.raveOutline = config.hullRave();
                    this.raveFill = config.hullRave();
                    this.raveSpeed = config.hullRaveSpeed();
                    this.tileStyle = HighlightInfo.TileStyle.REGULAR;
                    this.outlineWidth = config.hullWidth();
                    this.antiAliasing = config.hullAA();
                    this.outlineFeather = 0;
                    break;
                case AREA:
                    this.outlineColor = config.areaColor();
                    this.fillColor = config.areaColor();
                    this.raveOutline = config.areaRave();
                    this.raveFill = config.areaRave();
                    this.raveSpeed = config.areaRaveSpeed();
                    this.tileStyle = HighlightInfo.TileStyle.REGULAR;
                    this.outlineWidth = 0;
                    this.antiAliasing = false;
                    this.outlineFeather = 0;
                    break;
                case OUTLINE:
                    this.outlineColor = config.outlineColor();
                    this.fillColor = new Color(0, 0, 0, 0);
                    this.raveOutline = config.outlineRave();
                    this.raveFill = false;
                    this.raveSpeed = config.outlineRaveSpeed();
                    this.tileStyle = HighlightInfo.TileStyle.REGULAR;
                    this.outlineWidth = config.outlineWidth();
                    this.antiAliasing = true;
                    this.outlineFeather = config.outlineFeather();
                    break;
                case CLICKBOX:
                    this.outlineColor = config.clickboxColor();
                    this.fillColor = config.clickboxFillColor();
                    this.raveOutline = config.clickboxRave();
                    this.raveFill = config.clickboxRave();
                    this.raveSpeed = config.clickboxRaveSpeed();
                    this.tileStyle = HighlightInfo.TileStyle.REGULAR;
                    this.outlineWidth = config.clickboxWidth();
                    this.antiAliasing = config.clickboxAA();
                    this.outlineFeather = 0;
                    break;
                case TURBO:
                    // Turbo mode gets its colors dynamically, but we can set a fallback.
                    this.outlineColor = Color.CYAN;
                    this.fillColor = new Color(0, 255, 255, 20);
                    this.raveOutline = true;
                    this.raveFill = true;
                    this.raveSpeed = 100; // Fast by default
                    break;
                default:
                    // Fallback for any other styles
                    this.outlineColor = Color.CYAN;
                    this.fillColor = new Color(0, 255, 255, 20);
            }
        }
    }
}
