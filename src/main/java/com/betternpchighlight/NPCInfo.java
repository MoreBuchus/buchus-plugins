package com.betternpchighlight;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.NPC;

@Getter
@Setter
public class NPCInfo
{
	NPC npc;
	HighlightColor tile;
	HighlightColor trueTile;
	HighlightColor swTile;
	HighlightColor swTrueTile;
	HighlightColor hull;
	HighlightColor area;
	HighlightColor outline;
	HighlightColor clickbox;
	HighlightColor turbo;
	boolean isTask;
	boolean ignoreDead;
	boolean hideNpc;
	boolean drawOverlayBeneathNpc;
	boolean displayNameAboveNpc;

	/**
	 * Main constructor - creates NPCInfo with default (off) highlight states
	 */
	public NPCInfo(NPC npc)
	{
		this.npc = npc;
		// Initialize with default "off" values
		this.tile = new HighlightColor(false, null, null);
		this.trueTile = new HighlightColor(false, null, null);
		this.swTile = new HighlightColor(false, null, null);
		this.swTrueTile = new HighlightColor(false, null, null);
		this.hull = new HighlightColor(false, null, null);
		this.area = new HighlightColor(false, null, null);
		this.outline = new HighlightColor(false, null, null);
		this.clickbox = new HighlightColor(false, null, null);
		this.turbo = new HighlightColor(false, null, null);
		this.isTask = false;
		this.ignoreDead = false;
		this.hideNpc = false;
		this.drawOverlayBeneathNpc = false;
		this.displayNameAboveNpc = false;
	}

	/**
	 * Check if this NPC has any active highlights
	 */
	public boolean hasAnyHighlight()
	{
		return tile.isHighlight() || trueTile.isHighlight() || swTile.isHighlight() ||
				swTrueTile.isHighlight() || hull.isHighlight() || area.isHighlight() ||
				outline.isHighlight() || clickbox.isHighlight() || turbo.isHighlight() ||
				isTask;
	}

	/**
	 * Get the primary highlight type for this NPC (used for priority ordering)
	 */
	public String getPrimaryHighlightType()
	{
		if (isTask) return "Task";
		if (tile.isHighlight()) return "Tile";
		if (trueTile.isHighlight()) return "True Tile";
		if (swTile.isHighlight()) return "SW Tile";
		if (swTrueTile.isHighlight()) return "SW True Tile";
		if (hull.isHighlight()) return "Hull";
		if (area.isHighlight()) return "Area";
		if (outline.isHighlight()) return "Outline";
		if (clickbox.isHighlight()) return "Clickbox";
		if (turbo.isHighlight()) return "Turbo";
		return "None";
	}

	/**
	 * Get the primary highlight color for this NPC
	 */
	public HighlightColor getPrimaryHighlight()
	{
		if (tile.isHighlight()) return tile;
		if (trueTile.isHighlight()) return trueTile;
		if (swTile.isHighlight()) return swTile;
		if (swTrueTile.isHighlight()) return swTrueTile;
		if (hull.isHighlight()) return hull;
		if (area.isHighlight()) return area;
		if (outline.isHighlight()) return outline;
		if (clickbox.isHighlight()) return clickbox;
		if (turbo.isHighlight()) return turbo;
		return null;
	}
}