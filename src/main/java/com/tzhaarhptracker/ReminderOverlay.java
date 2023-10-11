/*
 * Copyright (c) 2023, Buchus <http://github.com/MoreBuchus>
 * Copyright (c) 2023, geheur <http://github.com/geheur>
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
package com.tzhaarhptracker;

import java.awt.*;
import java.util.Collection;
import javax.inject.Inject;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

public class ReminderOverlay extends Overlay
{
	private final TzhaarHPTrackerPlugin plugin;
	private final TzhaarHPTrackerConfig config;
	private final Client client;
	private final ModelOutlineRenderer modelOutlineRenderer;

	@Inject
	private ReminderOverlay(TzhaarHPTrackerPlugin plugin, TzhaarHPTrackerConfig config, Client client, ModelOutlineRenderer modelOutlineRenderer)
	{
		this.plugin = plugin;
		this.config = config;
		this.client = client;
		this.modelOutlineRenderer = modelOutlineRenderer;
		setPosition(OverlayPosition.DYNAMIC);
		setPriority(OverlayPriority.HIGHEST);
		setLayer(OverlayLayer.UNDER_WIDGETS);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (plugin.isInAllowedBanks() && config.spellbookWarning() != TzhaarHPTrackerConfig.spellbookWarningMode.OFF)
		{
			if (config.spellbookWarning() == TzhaarHPTrackerConfig.spellbookWarningMode.OVERLAY || config.spellbookWarning() == TzhaarHPTrackerConfig.spellbookWarningMode.BOTH)
			{
				Collection<GameObject> gameObjects = plugin.getCaveEntrances();
				for (GameObject obj : gameObjects)
				{
					Color color = null;
					if ((!config.spellbookCheck().contains(TzhaarHPTrackerConfig.spellbook.NORMAL) && plugin.getSpellbookType().equals("NORMAL"))
						|| (!config.spellbookCheck().contains(TzhaarHPTrackerConfig.spellbook.ANCIENT) && plugin.getSpellbookType().equals("ANCIENT"))
						|| (!config.spellbookCheck().contains(TzhaarHPTrackerConfig.spellbook.LUNAR) && plugin.getSpellbookType().equals("LUNAR"))
						|| (!config.spellbookCheck().contains(TzhaarHPTrackerConfig.spellbook.ARCEUUS) && plugin.getSpellbookType().equals("ARCEUUS")))
					{
						color = config.spellbookColor();
					}

					Shape clickbox = obj.getClickbox();
					if (color != null && clickbox != null)
					{
						Point mousePos = client.getMouseCanvasPosition();
						if (mousePos != null && clickbox.contains(mousePos.getX(), mousePos.getY()))
						{
							color = color.darker();
						}
						graphics.setColor(color);
						graphics.fill(clickbox);
						graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue()));
						graphics.draw(clickbox);
					}
				}
			}
		}
		return null;
	}
}