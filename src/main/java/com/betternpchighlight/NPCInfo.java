package com.betternpchighlight;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.NPC;
import net.runelite.client.plugins.slayer.SlayerPluginService;

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

	public NPCInfo(NPC npc, BetterNpcHighlightPlugin plugin, SlayerPluginService slayerPluginService, BetterNpcHighlightConfig config)
	{
		this.npc = npc;
		this.tile = plugin.checkSpecificList(plugin.tileNames, plugin.tileIds, npc, config.tileColor(), config.tileFillColor());
		this.trueTile = plugin.checkSpecificList(plugin.trueTileNames, plugin.trueTileIds, npc, config.trueTileColor(), config.trueTileFillColor());
		this.swTile = plugin.checkSpecificList(plugin.swTileNames, plugin.swTileIds, npc, config.swTileColor(), config.swTileFillColor());
		this.swTrueTile = plugin.checkSpecificList(plugin.swTrueTileNames, plugin.swTrueTileIds, npc, config.swTrueTileColor(), config.swTrueTileFillColor());
		this.hull = plugin.checkSpecificList(plugin.hullNames, plugin.hullIds, npc, config.hullColor(), config.hullFillColor());
		this.area = plugin.checkSpecificList(plugin.areaNames, plugin.areaIds, npc, config.areaColor(), null);
		this.outline = plugin.checkSpecificList(plugin.outlineNames, plugin.outlineIds, npc, config.outlineColor(), null);
		this.clickbox = plugin.checkSpecificList(plugin.clickboxNames, plugin.clickboxIds, npc, config.clickboxColor(), config.clickboxFillColor());
		this.turbo = plugin.checkSpecificList(plugin.turboNames, plugin.turboIds, npc, null, null);
		this.isTask = plugin.checkSlayerPluginEnabled() && slayerPluginService != null && slayerPluginService.getTargets().contains(npc);
		this.ignoreDead = plugin.checkSpecificNameList(plugin.ignoreDeadExclusionList, npc) || plugin.checkSpecificIdList(plugin.ignoreDeadExclusionIDList, npc);
	}
}
