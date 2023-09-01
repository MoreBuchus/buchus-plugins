package com.betternpchighlight;

import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.Point;
import net.runelite.client.plugins.slayer.SlayerPluginService;
import net.runelite.client.ui.overlay.*;
import javax.inject.Inject;
import java.awt.*;
import net.runelite.client.util.Text;

public class BetterNpcMinimapOverlay extends Overlay
{
	private final Client client;

	private final BetterNpcHighlightPlugin plugin;

	private final BetterNpcHighlightConfig config;

	@Inject
	private BetterNpcMinimapOverlay(Client client, BetterNpcHighlightPlugin plugin, BetterNpcHighlightConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		for (NPCInfo npcInfo : plugin.npcList)
		{
			NPC npc = npcInfo.getNpc();
			if (npc.getName() != null && config.npcMinimapMode() != BetterNpcHighlightConfig.npcMinimapMode.OFF)
			{
				Color color = plugin.getSpecificColor(npcInfo);

				NPCComposition npcComposition = npc.getTransformedComposition();
				if (color != null && npcComposition != null && npcComposition.isInteractible())
				{
					Point minimapLocation = npc.getMinimapLocation();
					if (minimapLocation != null)
					{
						if (config.npcMinimapMode() == BetterNpcHighlightConfig.npcMinimapMode.DOT || config.npcMinimapMode() == BetterNpcHighlightConfig.npcMinimapMode.BOTH)
						{
							OverlayUtil.renderMinimapLocation(graphics, minimapLocation, color);
						}

						if (config.npcMinimapMode() == BetterNpcHighlightConfig.npcMinimapMode.NAME || config.npcMinimapMode() == BetterNpcHighlightConfig.npcMinimapMode.BOTH)
						{
							OverlayUtil.renderTextLocation(graphics, minimapLocation, Text.removeTags(npc.getName()), color);
						}
					}
				}
			}
		}
		return null;
	}
}
