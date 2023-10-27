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

import com.google.common.base.Strings;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Objects;
import javax.inject.Inject;

import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.game.NpcUtil;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;
import static net.runelite.api.NpcID.*;

public class TzhaarHPTrackerOverlay extends Overlay
{
	private final TzhaarHPTrackerPlugin plugin;
	private final TzhaarHPTrackerConfig config;
	private final Client client;
	private final ModelOutlineRenderer modelOutlineRenderer;
	private final NpcUtil npcUtil;

	private static final int ACTOR_OVERHEAD_TEXT_MARGIN = 40;
	private static final int ACTOR_HORIZONTAL_TEXT_MARGIN = 10;

	@Inject
	private TzhaarHPTrackerOverlay(TzhaarHPTrackerPlugin plugin, TzhaarHPTrackerConfig config, Client client,
								   ModelOutlineRenderer modelOutlineRenderer, NpcUtil npcUtil)
	{
		this.plugin = plugin;
		this.config = config;
		this.client = client;
		this.modelOutlineRenderer = modelOutlineRenderer;
		this.npcUtil = npcUtil;
		setPosition(OverlayPosition.DYNAMIC);
		setPriority(OverlayPriority.HIGHEST);
		setLayer(OverlayLayer.UNDER_WIDGETS);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (plugin.isInAllowedCaves())
		{
			if (plugin.getFont() == null)
			{
				plugin.loadFont();
			}

			ArrayList<NPC> stackedNpcs = new ArrayList<>();

			for (TzhaarNPC n : plugin.getNpcs())
			{
				if (shouldHighlight(n.getNpc()))
				{
					Color line = !n.isDead() ? config.highlightAliveColor() : config.highlightDeadColor();
					Color fill = !n.isDead() ? config.fillAliveColor() : config.fillDeadColor();

					if (config.dynamicColor() == TzhaarHPTrackerConfig.DynamicColor.BOTH || config.dynamicColor() == TzhaarHPTrackerConfig.DynamicColor.HIGHLIGHT)
					{
						line = plugin.getDynamicColor(n, true);
						fill = plugin.getDynamicColor(n, false);
					}

					NPCComposition npcComposition = n.getNpc().getTransformedComposition();
					if (npcComposition != null)
					{
						int size = npcComposition.getSize();

						//Only highlights NPCs - not pillars
						if (n.getNpc().getId() != ROCKY_SUPPORT)
						{
							if (config.highlightStyle().contains(TzhaarHPTrackerConfig.HighlightStyle.TILE))
							{
								LocalPoint lp = n.getNpc().getLocalLocation();
								if (lp != null)
								{
									Polygon tilePoly = Perspective.getCanvasTileAreaPoly(client, lp, size);
									if (tilePoly != null)
									{
										switch (config.tileLines())
										{
											case REG:
												renderPoly(graphics, line, fill, tilePoly, config.highlightThiCC());
												break;
											case DASH:
												renderPolygonDashed(graphics, line, fill, tilePoly, config.highlightThiCC(), size);
												break;
											case CORNER:
												renderPolygonCorners(graphics, line, fill, tilePoly, config.highlightThiCC());
												break;
										}
									}
								}
							}

							if (config.highlightStyle().contains(TzhaarHPTrackerConfig.HighlightStyle.TRUE_TILE))
							{
								LocalPoint lp = LocalPoint.fromWorld(client, n.getNpc().getWorldLocation());
								if (lp != null)
								{
									lp = new LocalPoint(lp.getX() + size * 128 / 2 - 64, lp.getY() + size * 128 / 2 - 64);
									Polygon tilePoly = Perspective.getCanvasTileAreaPoly(client, lp, size);
									if (tilePoly != null)
									{
										switch (config.tileLines())
										{
											case REG:
												renderPoly(graphics, line, fill, tilePoly, config.highlightThiCC());
												break;
											case DASH:
												renderPolygonDashed(graphics, line, fill, tilePoly, config.highlightThiCC(), size);
												break;
											case CORNER:
												renderPolygonCorners(graphics, line, fill, tilePoly, config.highlightThiCC());
												break;
										}
									}
								}
							}

							if (config.highlightStyle().contains(TzhaarHPTrackerConfig.HighlightStyle.SW_TILE))
							{
								LocalPoint lp = n.getNpc().getLocalLocation();
								if (lp != null)
								{
									int x = lp.getX() - (size - 1) * 128 / 2;
									int y = lp.getY() - (size - 1) * 128 / 2;
									Polygon tilePoly = Perspective.getCanvasTilePoly(client, new LocalPoint(x, y));
									if (tilePoly != null)
									{
										switch (config.tileLines())
										{
											case REG:
												renderPoly(graphics, line, fill, tilePoly, config.highlightThiCC());
												break;
											case DASH:
												renderPolygonDashed(graphics, line, fill, tilePoly, config.highlightThiCC(), size);
												break;
											case CORNER:
												renderPolygonCorners(graphics, line, fill, tilePoly, config.highlightThiCC());
												break;
										}
									}
								}
							}

							if (config.highlightStyle().contains(TzhaarHPTrackerConfig.HighlightStyle.SW_TRUE_TILE))
							{
								LocalPoint lp = LocalPoint.fromWorld(client, n.getNpc().getWorldLocation());
								if (lp != null)
								{
									Polygon tilePoly = Perspective.getCanvasTilePoly(client, lp);
									switch (config.tileLines())
									{
										case REG:
											renderPoly(graphics, line, fill, tilePoly, config.highlightThiCC());
											break;
										case DASH:
											renderPolygonDashed(graphics, line, fill, tilePoly, config.highlightThiCC(), size);
											break;
										case CORNER:
											renderPolygonCorners(graphics, line, fill, tilePoly, config.highlightThiCC());
											break;
									}
								}
							}

							if (config.highlightStyle().contains(TzhaarHPTrackerConfig.HighlightStyle.HULL))
							{
								Shape hull = n.getNpc().getConvexHull();
								if (hull != null)
								{
									renderPoly(graphics, line, fill, hull, config.highlightThiCC());
								}
							}

							if (config.highlightStyle().contains(TzhaarHPTrackerConfig.HighlightStyle.OUTLINE))
							{
								modelOutlineRenderer.drawOutline(n.getNpc(), (int) config.highlightThiCC(), line, 4);
							}
						}

						if ((config.showHp() != TzhaarHPTrackerConfig.HpLocation.OFF && n.getNpc().getId() != ROCKY_SUPPORT)
							|| (config.showPillarHp() != TzhaarHPTrackerConfig.HpLocation.OFF && n.getNpc().getId() == ROCKY_SUPPORT))
						{
							drawHp(graphics, stackedNpcs, n);
						}
					}
				}
			}
		}
		return null;
	}

	private void renderPoly(Graphics2D g, Color outlineColor, Color fillColor, Shape polygon, double width)
	{
		if (polygon != null)
		{
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, config.antiAlias() ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
			g.setColor(outlineColor);
			g.setStroke(new BasicStroke((float) width));
			g.draw(polygon);
			g.setColor(fillColor);
			g.fill(polygon);
		}
	}

	/**
	 * Draws only the corners of NPC tile highlights - Made by Geheur
	 *
	 * @param graphics
	 * @param outlineColor
	 * @param fillColor
	 * @param poly
	 * @param width
	 */
	private void renderPolygonCorners(Graphics2D graphics, Color outlineColor, Color fillColor, Shape poly, double width)
	{
		if (poly instanceof Polygon)
		{
			Polygon p = (Polygon) poly;
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, config.antiAlias() ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
			graphics.setColor(outlineColor);
			graphics.setStroke(new BasicStroke((float) width));

			int divisor = 7;
			for (int i = 0; i < p.npoints; i++)
			{
				int ptx = p.xpoints[i];
				int pty = p.ypoints[i];
				int prev = (i - 1) < 0 ? 3 : (i - 1);
				int next = (i + 1) > 3 ? 0 : (i + 1);
				int ptxN = ((p.xpoints[next]) - ptx) / divisor + ptx;
				int ptyN = ((p.ypoints[next]) - pty) / divisor + pty;
				int ptxP = ((p.xpoints[prev]) - ptx) / divisor + ptx;
				int ptyP = ((p.ypoints[prev]) - pty) / divisor + pty;
				graphics.drawLine(ptx, pty, ptxN, ptyN);
				graphics.drawLine(ptx, pty, ptxP, ptyP);
			}

			graphics.setColor(fillColor);
			graphics.fill(poly);
		}
	}

	/**
	 * Draws the corners and dashed lines along each side of NPC tile highlights - Made by Geheur
	 *
	 * @param graphics
	 * @param outlineColor
	 * @param fillColor
	 * @param poly
	 * @param width
	 * @param tiles
	 */
	private void renderPolygonDashed(Graphics2D graphics, Color outlineColor, Color fillColor, Shape poly, double width, int tiles)
	{
		if (poly instanceof Polygon)
		{
			Polygon p = (Polygon) poly;
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, config.antiAlias() ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
			graphics.setColor(outlineColor);
			graphics.setStroke(new BasicStroke((float) width));

			int divisor = 7 * tiles;
			for (int i = 0; i < p.npoints; i++)
			{
				int ptx = p.xpoints[i];
				int pty = p.ypoints[i];
				int next = (i + 1) > 3 ? 0 : (i + 1);
				int ptxN = (p.xpoints[next]) - ptx;
				int ptyN = (p.ypoints[next]) - pty;
				float length = (float) Point2D.distance(ptx, pty, ptx + ptxN, pty + ptyN);
				float dashLength = length * 2f / divisor;
				float spaceLength = length * 5f / divisor;
				Stroke s = new BasicStroke((float) width, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10, new float[]{dashLength, spaceLength}, dashLength / 2);
				graphics.setStroke(s);
				graphics.drawLine(ptx, pty, ptx + ptxN, pty + ptyN);
			}

			graphics.setColor(fillColor);
			graphics.fill(poly);
		}
	}

	private void drawHp(Graphics2D graphics, ArrayList<NPC> stackedNpcs, TzhaarNPC n)
	{
		int offset = 0;
		NPC firstStack = null;
		for (NPC npc : stackedNpcs)
		{
			if (npc.getWorldLocation().getX() == n.getNpc().getWorldLocation().getX() && npc.getWorldLocation().getY() == n.getNpc().getWorldLocation().getY())
			{
				if (firstStack == null)
				{
					firstStack = npc;
				}
				offset += graphics.getFontMetrics().getHeight();
			}
		}

		final int zOffset;
		int npcOffset = firstStack != null ? firstStack.getLogicalHeight() : n.getNpc().getLogicalHeight();
		if (n.getNpc().getId() != ROCKY_SUPPORT)
		{
			switch (config.showHp())
			{
				case CENTER:
					zOffset = npcOffset / 2;
					break;
				case FEET:
					zOffset = 0;
					break;
				default:
					//Zuk is too tall for above HP bar to be seen by most players -> Set it to center if HP bar is selected
					zOffset = !Objects.equals(n.getNpc().getName(), "TzKal-Zuk") ? npcOffset + ACTOR_OVERHEAD_TEXT_MARGIN : npcOffset / 2;
			}
		}
		else
		{
			switch (config.showPillarHp())
			{
				case CENTER:
					zOffset = npcOffset / 2;
					break;
				case FEET:
					zOffset = 0;
					break;
				default:
					zOffset = npcOffset + ACTOR_OVERHEAD_TEXT_MARGIN;
			}
		}
		stackedNpcs.add(n.getNpc());
		String hp = n.getHp() < 0 ? "0" : Integer.toString(n.getHp());
		Point textLocation = offset > 0 ? firstStack.getCanvasTextLocation(graphics, hp, zOffset) : n.getNpc().getCanvasTextLocation(graphics, hp, zOffset);

		if (textLocation != null)
		{
			Point offsetLocation = new Point(textLocation.getX(), textLocation.getY() - offset);
			Color color = !n.isDead() ? config.highlightAliveColor() : config.highlightDeadColor();

			if (config.dynamicColor() == TzhaarHPTrackerConfig.DynamicColor.BOTH || config.dynamicColor() == TzhaarHPTrackerConfig.DynamicColor.HP)
			{
				color = plugin.getDynamicColor(n, true);
			}

			if (config.hpFontAlpha() > 0)
			{
				color = new Color(color.getRed(), color.getGreen(), color.getBlue(), config.hpFontAlpha());
			}

			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, config.antiAlias() ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
			Font oldFont = graphics.getFont();
			graphics.setFont(plugin.getFont());
			drawText(graphics, offsetLocation.getX(), offsetLocation.getY(), hp, color);
			graphics.setFont(oldFont);
		}
	}

	public void drawText(Graphics2D graphics, int textX, int textY, String text, Color color)
	{
		if (!Strings.isNullOrEmpty(text))
		{
			graphics.setColor(Color.BLACK);
			drawTextBackground(graphics, new Point(textX, textY), text, color.getAlpha());

			graphics.setColor(color);
			graphics.drawString(text, textX, textY);
		}
	}

	private void drawTextBackground(Graphics2D graphics, Point textLoc, String text, int fontAlpha)
	{
		Color shadow = new Color(0, 0, 0, fontAlpha);
		switch (config.fontBackground())
		{
			case OUTLINE:
			{
				OverlayUtil.renderTextLocation(graphics, new Point(textLoc.getX(), textLoc.getY() + 1), text, shadow);
				OverlayUtil.renderTextLocation(graphics, new Point(textLoc.getX(), textLoc.getY() - 1), text, shadow);
				OverlayUtil.renderTextLocation(graphics, new Point(textLoc.getX() + 1, textLoc.getY()), text, shadow);
				OverlayUtil.renderTextLocation(graphics, new Point(textLoc.getX() - 1, textLoc.getY()), text, shadow);
				break;
			}
			case SHADOW:
			{
				OverlayUtil.renderTextLocation(graphics, new Point(textLoc.getX() + 1, textLoc.getY() + 1), text, shadow);
				break;
			}
			default:
				break;
		}
	}

	private boolean shouldHighlight(NPC n)
	{
		final NPCComposition c = n.getTransformedComposition();
		if (c != null && c.isFollower())
		{
			return false;
		}
		return plugin.getNpcs().stream().anyMatch(npc -> npc.getNpc().getIndex() == n.getIndex());
	}
}
