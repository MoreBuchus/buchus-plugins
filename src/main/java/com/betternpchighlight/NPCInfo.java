package com.betternpchighlight;

import java.awt.Color;
import java.util.EnumMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.NPC;

@Getter
@Setter
public class NPCInfo {
    NPC npc;
    HighlightInfo tile;
    HighlightInfo trueTile;
    HighlightInfo swTile;
    HighlightInfo swTrueTile;
    HighlightInfo hull;
    HighlightInfo area;
    HighlightInfo outline;
    HighlightInfo clickbox;
    HighlightInfo turbo;
    boolean isTask;
    boolean highlightDead;
    boolean hideNpc;
    boolean drawOverlayBeneathNpc;
    boolean displayNameAboveNpc;
    private Color displayNameColor;

    /**
     * Main constructor - creates NPCInfo with default (off) highlight states
     */
    public NPCInfo(NPC npc) {
        this.npc = npc;
        this.tile = new HighlightInfo(false, null, null);
        this.trueTile = new HighlightInfo(false, null, null);
        this.swTile = new HighlightInfo(false, null, null);
        this.swTrueTile = new HighlightInfo(false, null, null);
        this.hull = new HighlightInfo(false, null, null);
        this.area = new HighlightInfo(false, null, null);
        this.outline = new HighlightInfo(false, null, null);
        this.clickbox = new HighlightInfo(false, null, null);
        this.turbo = new HighlightInfo(false, null, null);
        this.isTask = false;
        this.highlightDead = false;
        this.hideNpc = false;
        this.drawOverlayBeneathNpc = false;
        this.displayNameAboveNpc = false;
    }

    /**
     * Check if this NPC has any active highlights
     */
    public boolean hasAnyHighlight() {
        return tile.isHighlight() || trueTile.isHighlight() || swTile.isHighlight() ||
                swTrueTile.isHighlight() || hull.isHighlight() || area.isHighlight() ||
                outline.isHighlight() || clickbox.isHighlight() || turbo.isHighlight() ||
                isTask;
    }

    /**
     * Returns a map of all highlight styles and their corresponding info.
     * This is used for rendering in the overlay.
     *
     * @return A map of TagStyle to HighlightInfo.
     */
    public Map<TagStyle, HighlightInfo> getHighlights() {
        final Map<TagStyle, HighlightInfo> highlights = new EnumMap<>(TagStyle.class);
        highlights.put(TagStyle.TILE, tile);
        highlights.put(TagStyle.TRUE_TILE, trueTile);
        highlights.put(TagStyle.SW_TILE, swTile);
        highlights.put(TagStyle.SW_TRUE_TILE, swTrueTile);
        highlights.put(TagStyle.HULL, hull);
        highlights.put(TagStyle.AREA, area);
        highlights.put(TagStyle.OUTLINE, outline);
        highlights.put(TagStyle.CLICKBOX, clickbox);
        highlights.put(TagStyle.TURBO, turbo);
        return highlights;
    }
}