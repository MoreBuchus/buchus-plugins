package com.coxanalytics;

import com.coxanalytics.config.CustomOverlayInfo;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.VarPlayer;
import net.runelite.api.Varbits;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.ComponentConstants;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;

import javax.inject.Inject;
import java.awt.*;
import java.text.DecimalFormat;

public class CoxAnalyticsOverlay extends OverlayPanel
{
	private static final DecimalFormat POINTS_FORMAT = new DecimalFormat("#,###");

	private final Client client;
	private final CoxAnalyticsPlugin plugin;
	private final CoxAnalyticsConfig config;
	private final TooltipManager tooltipManager;

	@Inject
	private CoxAnalyticsOverlay(Client client, CoxAnalyticsPlugin plugin, CoxAnalyticsConfig config, TooltipManager tooltipManager)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		this.tooltipManager = tooltipManager;
		setPosition(OverlayPosition.TOP_LEFT);
		setPriority(OverlayPriority.HIGH);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (plugin.isInCox() && config.replaceWidget())
		{
			int totalPoints = client.getVarbitValue(Varbits.TOTAL_POINTS);
			int personalPoints = client.getVarpValue(VarPlayer.RAIDS_PERSONAL_POINTS);

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
					if (!config.fontName().equals(""))
					{
						graphics.setFont(new Font(config.fontName(), config.fontWeight().getWeight(), config.fontSize()));
					}
					break;
			}

			if (!config.overlayInfo().isEmpty())
			{
				if (config.overlayInfo().contains(CustomOverlayInfo.TOTAL))
				{
					panelComponent.getChildren().add(LineComponent.builder()
						.left("Total:")
						.right(POINTS_FORMAT.format(totalPoints))
						.build());
				}

				if (config.overlayInfo().contains(CustomOverlayInfo.PERSONAL))
				{
					panelComponent.getChildren().add(LineComponent.builder()
						.left(client.getLocalPlayer().getName() + ":")
						.right(POINTS_FORMAT.format(personalPoints))
						.build());
				}

				if (config.overlayInfo().contains(CustomOverlayInfo.ELAPSED))
				{
					panelComponent.getChildren().add(LineComponent.builder()
						.left("Time:")
						.rightColor(!plugin.getOlmTime().equals("") ? Color.GREEN : Color.WHITE)
						.right(plugin.raidTime(plugin.coxTimeVar()))
						.build());
				}

				if (config.overlayInfo().contains(CustomOverlayInfo.FLOOR))
				{
					if (plugin.getUpperTicks() != -1)
					{
						panelComponent.getChildren().add(LineComponent.builder()
							.left("Floor 1: ")
							.right(plugin.getUpperFloorTime())
							.build());
					}

					if (plugin.getMiddleTicks() != -1)
					{
						panelComponent.getChildren().add(LineComponent.builder()
							.left("Floor 2: ")
							.right(plugin.getMiddleFloorTime())
							.build());
					}

					if (plugin.getLowerTicks() != -1)
					{
						if (plugin.getMiddleTicks() != -1)
						{
							panelComponent.getChildren().add(LineComponent.builder()
								.left("Floor 3: ")
								.right(plugin.getLowerFloorTime())
								.build());
						}
						else
						{
							panelComponent.getChildren().add(LineComponent.builder()
								.left("Floor 2: ")
								.right(plugin.getLowerFloorTime())
								.build());
						}
					}

					if (!plugin.getOlmTime().equals(""))
					{
						panelComponent.getChildren().add(LineComponent.builder()
							.left("Olm: ")
							.right(plugin.getOlmTime())
							.build());
					}
				}

				if (config.overlayInfo().contains(CustomOverlayInfo.CURRENT) && plugin.getOlmTime().equals(""))
				{
					panelComponent.getChildren().add(LineComponent.builder()
						.left("Current Split:")
						.rightColor(Color.WHITE)
						.right(plugin.raidTime(plugin.coxTimeVar() - plugin.getSplitTicks()))
						.build());
				}
			}

			final Rectangle bounds = getBounds();
			if (bounds.getX() > 0)
			{
				final Point mousePosition = client.getMouseCanvasPosition();

				if (bounds.contains(mousePosition.getX(), mousePosition.getY()))
				{
					//Does not add the tooltip if upper floor has not been completed
					if (config.splitsTooltip() && plugin.getUpperTicks() != -1)
					{
						tooltipManager.add(new Tooltip(plugin.getFloorTimes()));
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