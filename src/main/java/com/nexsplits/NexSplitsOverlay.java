/*
 * Copyright (c) 2023, Buchus <http://github.com/MoreBuchus>
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
package com.nexsplits;

import com.nexsplits.config.CustomOverlayInfo;
import com.nexsplits.config.KillTimerMode;
import com.nexsplits.config.PhaseNameTypeMode;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.components.ComponentConstants;
import net.runelite.client.ui.overlay.components.LineComponent;

import javax.inject.Inject;
import java.awt.*;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;

public class NexSplitsOverlay extends OverlayPanel
{
	private final NexSplitsPlugin plugin;

	private final NexSplitsConfig config;

	private final Client client;

	private final TooltipManager tooltipManager;

	@Inject
	public NexSplitsOverlay(NexSplitsPlugin plugin, NexSplitsConfig config, Client client, TooltipManager tooltipManager)
	{
		super(plugin);
		this.plugin = plugin;
		this.config = config;
		this.client = client;
		this.tooltipManager = tooltipManager;
	}

	public Dimension render(Graphics2D graphics)
	{
		panelComponent.getChildren().clear();

		switch (config.fontType())
		{
			case SMALL:
				graphics.setFont(FontManager.getRunescapeSmallFont());
				break;
			case REGULAR:
				graphics.setFont(FontManager.getRunescapeFont());
				break;
			case BOLD:
				graphics.setFont(FontManager.getRunescapeBoldFont());
				break;
			case CUSTOM:
				if (!config.fontName().isEmpty())
				{
					graphics.setFont(new Font(config.fontName(), config.fontWeight().getWeight(), config.fontSize()));
				}
				break;
		}

		if ((config.killTimer() == KillTimerMode.OVERLAY || config.killTimer() == KillTimerMode.BOTH) && plugin.inNexRegion() && plugin.getStartTick() > -1)
		{
			if (!config.overlayInfo().isEmpty())
			{
				if (config.overlayInfo().contains(CustomOverlayInfo.PLAYERS))
				{
					panelComponent.getChildren().add(LineComponent.builder()
						.left("Players:")
						.right(String.valueOf(plugin.getPlayerCount()))
						.build());
				}

				if (config.overlayInfo().contains(CustomOverlayInfo.ELAPSED))
				{
					panelComponent.getChildren().add(LineComponent.builder()
						.left("Time:")
						.rightColor(plugin.isChickenDead() ? Color.GREEN : Color.WHITE)
						.right(plugin.isChickenDead() ? plugin.getTime(plugin.getP5Tick() - plugin.getStartTick())
							: plugin.getTime(client.getTickCount() - plugin.getStartTick()))
						.build());
				}

				if (config.overlayInfo().contains(CustomOverlayInfo.PHASE) && plugin.getP1Tick() > -1)
				{
					if (plugin.getP1Tick() != -1)
					{
						String phaseText = config.phaseNameType() == PhaseNameTypeMode.NUMBER ? "P1: " : "Smoke: ";
						String bossText = "";
						if (config.showMinionSplit() && plugin.getP1Boss() > -1)
						{
							bossText = " (" + plugin.getTime(plugin.getP1Boss() - plugin.getStartTick()) + ")";
						}
						panelComponent.getChildren().add(LineComponent.builder()
							.left(phaseText)
							.right(plugin.getTime(plugin.getP1Tick() - plugin.getStartTick()) + bossText)
							.build());
					}

					if (plugin.getP2Tick() != -1)
					{
						String phaseText = config.phaseNameType() == PhaseNameTypeMode.NUMBER ? "P2: " : "Shadow: ";
						String bossText = "";
						if (config.showMinionSplit() && plugin.getP2Boss() > -1)
						{
							bossText = " (" + plugin.getTime(plugin.getP2Boss() - plugin.getP1Tick()) + ")";
						}

						panelComponent.getChildren().add(LineComponent.builder()
							.left(phaseText)
							.right(plugin.getTime(plugin.getP2Tick() - plugin.getP1Tick()) + bossText)
							.build());
					}

					if (plugin.getP3Tick() != -1)
					{
						String phaseText = config.phaseNameType() == PhaseNameTypeMode.NUMBER ? "P3: " : "Blood: ";
						String bossText = "";
						if (config.showMinionSplit() && plugin.getP3Boss() > -1)
						{
							bossText = " (" + plugin.getTime(plugin.getP3Boss() - plugin.getP2Tick()) + ")";
						}

						panelComponent.getChildren().add(LineComponent.builder()
							.left(phaseText)
							.right(plugin.getTime(plugin.getP3Tick() - plugin.getP2Tick()) + bossText)
							.build());
					}

					if (plugin.getP4Tick() != -1)
					{
						String phaseText = config.phaseNameType() == PhaseNameTypeMode.NUMBER ? "P4: " : "Ice: ";
						String bossText = "";
						if (config.showMinionSplit() && plugin.getP4Boss() > -1)
						{
							bossText = " (" + plugin.getTime(plugin.getP4Boss() - plugin.getP3Tick()) + ")";
						}

						panelComponent.getChildren().add(LineComponent.builder()
							.left(phaseText)
							.right(plugin.getTime(plugin.getP4Tick() - plugin.getP3Tick()) + bossText)
							.build());
					}

					if (plugin.getP5Tick() != -1)
					{
						String phaseText = config.phaseNameType() == PhaseNameTypeMode.NUMBER ? "P5: " : "Zaros: ";

						panelComponent.getChildren().add(LineComponent.builder()
							.left(phaseText)
							.right(plugin.getTime(plugin.getP5Tick() - plugin.getP4Tick()))
							.build());
					}
				}

				if (config.overlayInfo().contains(CustomOverlayInfo.CURRENT) && !plugin.isChickenDead() && plugin.getSplitTicks() > -1)
				{
					panelComponent.getChildren().add(LineComponent.builder()
						.left("Current Split:")
						.rightColor(Color.WHITE)
						.right(plugin.getTime(client.getTickCount() - plugin.getSplitTicks()))
						.build());
				}
			}

			final Rectangle bounds = getBounds();
			if (bounds.getX() > 0)
			{
				boolean timeEnabled = config.overlayInfo().contains(CustomOverlayInfo.PHASE) || config.overlayInfo().contains(CustomOverlayInfo.CURRENT)
					|| config.overlayInfo().contains(CustomOverlayInfo.ELAPSED);
				final Point mousePosition = client.getMouseCanvasPosition();

				if (bounds.contains(mousePosition.getX(), mousePosition.getY()))
				{
					if (timeEnabled && plugin.getStartTick() != -1)
					{
						tooltipManager.add(new Tooltip(plugin.getPhaseTimes()));
					}
				}
			}
		}

		switch (config.backgroundStyle())
		{
			case HIDE:
				panelComponent.setBackgroundColor(null);
				break;
			case STANDARD:
				panelComponent.setBackgroundColor(ComponentConstants.STANDARD_BACKGROUND_COLOR);
				break;
			case CUSTOM:
				panelComponent.setBackgroundColor(config.backgroundColor());
				break;
		}
		return super.render(graphics);
	}
}
