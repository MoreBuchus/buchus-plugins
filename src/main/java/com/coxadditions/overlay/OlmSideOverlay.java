package com.coxadditions.overlay;

import com.coxadditions.CoxAdditionsConfig;
import com.coxadditions.CoxAdditionsPlugin;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

public class OlmSideOverlay extends Overlay
{
	private final Client client;
	private final CoxAdditionsConfig config;
	private final CoxAdditionsPlugin plugin;

	@Inject
	public OlmSideOverlay(Client client, CoxAdditionsConfig config, CoxAdditionsPlugin plugin)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setPriority(OverlayPriority.LOW);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	public Dimension render(Graphics2D graphics)
	{
		if (config.olmSide() != CoxAdditionsConfig.olmSideMode.OFF && plugin.getOlmTile() != null && client.getLocalPlayer() != null)
		{
			Color color = config.olmSideColor();
			if (config.olmSideColorMode() == CoxAdditionsConfig.olmSideColorMode.PHASE)
			{
				switch (plugin.getOlmPhase())
				{
					case "Crystal":
						color = Color.MAGENTA;
						break;
					case "Acid":
						color = Color.GREEN;
						break;
					case "Flame":
						color = Color.RED;
						break;
				}
			}
			drawTile(graphics, plugin.getOlmTile(), color);
		}
		return null;
	}

	protected void drawTile(Graphics2D graphics, LocalPoint lp, Color color)
	{
		if (WorldPoint.fromLocal(client, lp).distanceTo(client.getLocalPlayer().getWorldLocation()) < 32)
		{
			Polygon poly = config.olmSide() == CoxAdditionsConfig.olmSideMode.CENTER_TILE ? Perspective.getCanvasTilePoly(client, lp) : Perspective.getCanvasTileAreaPoly(client, lp, 5);
			if (poly != null)
			{
				graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 255));
				graphics.setStroke(new BasicStroke(1));
				graphics.draw(poly);
				graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 10));
				graphics.fill(poly);
			}
		}
	}
}
