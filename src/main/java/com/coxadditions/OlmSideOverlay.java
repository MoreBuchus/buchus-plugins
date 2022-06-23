package com.coxadditions;

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
		if (config.olmSide() && plugin.getOlmTile() != null)
		{
			LocalPoint lp = plugin.getOlmTile();
			if (lp != null)
			{
				WorldPoint wp = WorldPoint.fromLocal(client, lp);
				drawTile(graphics, wp, config.olmSideColor());
			}
		}
		return null;
	}

	protected void drawTile(Graphics2D graphics, WorldPoint point, Color color)
	{
		if (client.getLocalPlayer() != null)
		{
			WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();
			if (point.distanceTo(playerLocation) < 32)
			{
				LocalPoint lp = LocalPoint.fromWorld(client, point);
				if (lp != null)
				{
					Polygon poly = Perspective.getCanvasTilePoly(client, lp);
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
	}
}
