package com.coxadditions.overlay;

import com.coxadditions.CoxAdditionsConfig;
import com.coxadditions.CoxAdditionsPlugin;
import net.runelite.api.Point;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.components.ProgressPieComponent;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;

@Singleton
public class CoxAdditionsOverlay extends Overlay
{
	private final Client client;
	private final CoxAdditionsPlugin plugin;
	private final CoxAdditionsConfig config;

	@Inject
	private CoxAdditionsOverlay(final Client client, final CoxAdditionsPlugin plugin, final CoxAdditionsConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setPriority(OverlayPriority.HIGH);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (client.getVarbitValue(Varbits.IN_RAID) == 1)
		{
			if (plugin.getOverlayFont() == null)
			{
				plugin.loadFont(true);
			}

			if (config.coxHerbTimer() != CoxAdditionsConfig.CoXHerbTimerMode.OFF && (plugin.getCoxHerb1() != null || plugin.getCoxHerb2() != null))
			{
				if (config.coxHerbTimer() == CoxAdditionsConfig.CoXHerbTimerMode.TEXT)
				{
					graphics.setFont(plugin.getOverlayFont());
					if (plugin.getCoxHerb1() != null)
					{
						GameObject herb = plugin.getCoxHerb1();
						String text = Integer.toString(plugin.getCoxHerbTimer1());
						Point textLoc = herb.getCanvasTextLocation(graphics, text, 50);
						Point pointShadow = new Point(textLoc.getX() + 1, textLoc.getY() + 1);
						Font oldFont = graphics.getFont();
						graphics.setFont(plugin.getOverlayFont());
						OverlayUtil.renderTextLocation(graphics, pointShadow, text, Color.BLACK);
						OverlayUtil.renderTextLocation(graphics, textLoc, text, config.coxHerbTimerColor());
						graphics.setFont(oldFont);
					}

					if (plugin.getCoxHerb2() != null)
					{
						GameObject herb = plugin.getCoxHerb2();
						String text = Integer.toString(plugin.getCoxHerbTimer2());
						Point textLoc = herb.getCanvasTextLocation(graphics, text, 50);
						Point pointShadow = new Point(textLoc.getX() + 1, textLoc.getY() + 1);
						Font oldFont = graphics.getFont();
						graphics.setFont(plugin.getOverlayFont());
						OverlayUtil.renderTextLocation(graphics, pointShadow, text, Color.BLACK);
						OverlayUtil.renderTextLocation(graphics, textLoc, text, config.coxHerbTimerColor());
						graphics.setFont(oldFont);
					}
				}
				else if (config.coxHerbTimer() == CoxAdditionsConfig.CoXHerbTimerMode.PIE)
				{
					if (plugin.getCoxHerb1() != null)
					{
						final Point position = plugin.getCoxHerb1().getCanvasLocation(100);
						final ProgressPieComponent progressPie = new ProgressPieComponent();
						progressPie.setDiameter(config.coxHerbTimerSize());
						Color colorFill = new Color(config.coxHerbTimerColor().getRed(), config.coxHerbTimerColor().getGreen(), config.coxHerbTimerColor().getBlue(), 100);
						progressPie.setFill(colorFill);
						progressPie.setBorderColor(config.coxHerbTimerColor());
						progressPie.setPosition(position);
						int ticks = 16 - plugin.getCoxHerbTimer1();
						double progress = 1 - (ticks / 16.0);
						progressPie.setProgress(progress);
						progressPie.render(graphics);
					}

					if (plugin.getCoxHerb2() != null)
					{
						final Point position = plugin.getCoxHerb2().getCanvasLocation(100);
						final ProgressPieComponent progressPie = new ProgressPieComponent();
						progressPie.setDiameter(config.coxHerbTimerSize());
						Color colorFill = new Color(config.coxHerbTimerColor().getRed(), config.coxHerbTimerColor().getGreen(), config.coxHerbTimerColor().getBlue(), 100);
						progressPie.setFill(colorFill);
						progressPie.setBorderColor(config.coxHerbTimerColor());
						progressPie.setPosition(position);
						int ticks = 16 - plugin.getCoxHerbTimer2();
						double progress = 1 - (ticks / 16.0);
						progressPie.setProgress(progress);
						progressPie.render(graphics);
					}
				}
			}

			if (config.instanceTimer() == CoxAdditionsConfig.instanceTimerMode.OVERHEAD && plugin.isInstanceTimerRunning())
			{
				Player player = client.getLocalPlayer();
				if (player != null)
				{
					Point point = player.getCanvasTextLocation(graphics, "#", player.getLogicalHeight() + 60);
					if (point != null)
					{
						graphics.setFont(plugin.getOverlayFont());
						OverlayUtil.renderTextLocation(graphics, point, String.valueOf(plugin.getInstanceTimer()), Color.CYAN);
					}
				}
			}

			if (!config.tlList().equals(""))
			{
				for (NPC npc : client.getNpcs())
				{
					if (npc.getName() != null && npc.getId() != 8203)
					{
						String bossName = "";

						if (npc.getName().toLowerCase().contains("tekton"))
						{
							bossName = "tekton";
						}
						else if (npc.getName().toLowerCase().contains("jewelled crab"))
						{
							bossName = "jewelled crab";
						}
						else
						{
							bossName = npc.getName().toLowerCase();
						}

						if (plugin.getTlList().contains(bossName))
						{
							NPCComposition comp = npc.getComposition();
							int size = comp.getSize();
							LocalPoint lp = LocalPoint.fromWorld(client, npc.getWorldLocation());
							if (lp != null)
							{
								lp = new LocalPoint(lp.getX() + size * 128 / 2 - 64, lp.getY() + size * 128 / 2 - 64);
								Polygon tilePoly = Perspective.getCanvasTileAreaPoly(client, lp, size);
								renderPoly(graphics, config.tlColor(), tilePoly, config.tlThiCC());
							}
						}
					}
				}
			}

			if (config.olmPhaseHighlight() && plugin.isOlmSpawned() && plugin.getOlmHead() != null)
			{
				NPCComposition comp = plugin.getOlmHead().getComposition();
				int size = comp.getSize();
				LocalPoint lp = plugin.getOlmHead().getLocalLocation();
				if (lp != null)
				{
					Polygon tilePoly = Perspective.getCanvasTileAreaPoly(client, lp, size);
					if (tilePoly != null)
					{
						Color color = config.olmHighlightColor();
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
						renderPoly(graphics, color, tilePoly, config.olmWidth());
					}
				}
			}
		}
		return null;
	}

	private void renderPoly(Graphics2D graphics, Color color, Shape polygon, double width)
	{
		if (polygon != null)
		{
			graphics.setColor(color);
			graphics.setStroke(new BasicStroke((float) width));
			graphics.draw(polygon);
			graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 0));
			graphics.fill(polygon);
		}
	}
}
