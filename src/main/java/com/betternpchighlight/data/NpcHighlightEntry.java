package com.betternpchighlight.data;

import com.betternpchighlight.HighlightColor;

import java.awt.*;

public class NpcHighlightEntry {
    public String nameOrId;
    public String tagStyle;
    public Color outlineColor;
    public Color fillColor;
    public boolean hideNpc;
    public boolean drawUnder;
    public boolean displayName;
    public Color displayNameColor;
    public boolean highlightDead;
    public boolean raveOutline;
    public boolean raveFill;
    public int raveSpeed;
    public HighlightColor.TileStyle tileStyle;
    // New fields
    public double outlineWidth;
    public boolean antiAliasing;
    public int outlineFeather;

    public NpcHighlightEntry(String nameOrId, String tagStyle, Color outlineColor, Color fillColor,
                             boolean hideNpc, boolean drawUnder, boolean displayName, Color displayNameColor,
                             boolean highlightDead, boolean raveOutline, boolean raveFill, int raveSpeed, HighlightColor.TileStyle tileStyle,
                             double outlineWidth, boolean antiAliasing, int outlineFeather) {
        this.nameOrId = nameOrId;
        this.tagStyle = tagStyle;
        this.outlineColor = outlineColor;
        this.fillColor = fillColor;
        this.hideNpc = hideNpc;
        this.drawUnder = drawUnder;
        this.displayName = displayName;
        this.displayNameColor = displayNameColor;
        this.highlightDead = highlightDead;
        this.raveOutline = raveOutline;
        this.raveFill = raveFill;
        this.raveSpeed = raveSpeed;
        this.tileStyle = tileStyle;
        this.outlineWidth = outlineWidth;
        this.antiAliasing = antiAliasing;
        this.outlineFeather = outlineFeather;
    }
}
