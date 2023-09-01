/*
 * Copyright (c) 2022, Buchus <http://github.com/MoreBuchus>
 * Copyright (c) 2023, geheur <http://github.com/geheur>
 * Copyright (c) 2021, LeikvollE <http://github.com/LeikvollE>
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
package com.betternpchighlight;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;
import net.runelite.api.Point;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.game.NpcUtil;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

import javax.inject.Inject;
import java.awt.*;
import java.time.Instant;
import java.util.Random;
import net.runelite.client.util.Text;
import net.runelite.client.util.WildcardMatcher;

public class BetterNpcHighlightOverlay extends Overlay
{
	private final Client client;

	private final BetterNpcHighlightPlugin plugin;

	private final BetterNpcHighlightConfig config;

	private final ModelOutlineRenderer modelOutlineRenderer;

	private final NpcUtil npcUtil;

	@Inject
	private BetterNpcHighlightOverlay(Client client, BetterNpcHighlightPlugin plugin, BetterNpcHighlightConfig config,
									  ModelOutlineRenderer modelOutlineRenderer, NpcUtil npcUtil)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		this.modelOutlineRenderer = modelOutlineRenderer;
		this.npcUtil = npcUtil;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	public Dimension render(Graphics2D graphics)
	{
		for (NPCInfo npcInfo : plugin.npcList)
		{
			NPC npc = npcInfo.getNpc();
			NPCComposition npcComposition = npc.getTransformedComposition();
			if (npcComposition != null && ((npc.getName() != null && !npc.getName().equals("") && !npc.getName().equals("null")) || !isInvisible(npc.getModel())))
			{
				boolean showWhileDead = (!npc.isDead() && !npcUtil.isDying(npc)) || !config.ignoreDeadNpcs() || npcInfo.isIgnoreDead();
				boolean showNPC = (npcComposition.isFollower() && config.highlightPets()) || (!npcComposition.isFollower() && showWhileDead);

				if (showNPC && withinDistanceLimit(npc))
				{
					if (config.slayerHighlight() && npcInfo.isTask())
					{
						for (BetterNpcHighlightConfig.tagStyleMode mode : BetterNpcHighlightConfig.tagStyleMode.values())
						{
							if (config.taskHighlightStyle().contains(mode))
							{
								renderNpcOverlay(graphics, npcInfo, mode.getKey());
							}
						}
					}
					else
					{
						if (config.tileHighlight() && npcInfo.getTile().isHighlight())
						{
							renderNpcOverlay(graphics, npcInfo, "tile");
						}

						if (config.trueTileHighlight() && npcInfo.getTrueTile().isHighlight())
						{
							renderNpcOverlay(graphics, npcInfo, "trueTile");
						}

						if (config.swTileHighlight() && npcInfo.getSwTile().isHighlight())
						{
							renderNpcOverlay(graphics, npcInfo, "swTile");
						}

						if (config.swTrueTileHighlight() && npcInfo.getSwTrueTile().isHighlight())
						{
							renderNpcOverlay(graphics, npcInfo, "swTrueTile");
						}

						if (config.hullHighlight() && npcInfo.getHull().isHighlight())
						{
							renderNpcOverlay(graphics, npcInfo, "hull");
						}

						if (config.areaHighlight() && npcInfo.getArea().isHighlight())
						{
							renderNpcOverlay(graphics, npcInfo, "area");
						}

						if (config.outlineHighlight() && npcInfo.getOutline().isHighlight())
						{
							renderNpcOverlay(graphics, npcInfo, "outline");
						}

						if (config.clickboxHighlight() && npcInfo.getClickbox().isHighlight())
						{
							renderNpcOverlay(graphics, npcInfo, "clickbox");
						}

						if (config.turboHighlight() && npcInfo.getTurbo().isHighlight())
						{
							renderNpcOverlay(graphics, npcInfo, "turbo");
						}
					}

					if (plugin.namesToDisplay.size() > 0 && npc.getName() != null)
					{
						for (String str : plugin.namesToDisplay)
						{
							if (WildcardMatcher.matches(str, npc.getName().toLowerCase()))
							{
								String text = Text.removeTags(npc.getName());
								Point textLoc = npc.getCanvasTextLocation(graphics, text, npc.getLogicalHeight() + 40);
								if (textLoc != null)
								{
									drawTextBackground(graphics, textLoc, text);
									OverlayUtil.renderTextLocation(graphics, textLoc, text, plugin.getSpecificColor(npcInfo));
									break;
								}
							}
						}
					}
				}
			}
		}

		if (config.drawBeneath() && client.isGpu() && client.getLocalPlayer() != null)
		{
			// Limits the number of npcs drawn below overlays, ranks the NPCs by distance to player.
			LocalPoint lp = LocalPoint.fromWorld(client, client.getLocalPlayer().getWorldLocation());
			if (lp != null)
			{
				ArrayList<NPCInfo> closestNPCs = plugin.npcList;
				if (!plugin.beneathNPCs.isEmpty())
				{
					closestNPCs = new ArrayList<>();
					for (NPCInfo npcInfo : plugin.npcList)
					{
						for (String str : plugin.beneathNPCs)
						{
							if (npcInfo.getNpc().getName() != null && WildcardMatcher.matches(str, npcInfo.getNpc().getName().toLowerCase()))
							{
								closestNPCs.add(npcInfo);
								break;
							}
						}
					}
				}
				closestNPCs
					.stream()
					.sorted(Comparator.comparingInt(n -> n.getNpc().getLocalLocation().distanceTo(lp)))
					.limit(config.drawBeneathLimit())
					.collect(Collectors.toList())
					.forEach(nInfo -> removeActor(graphics, nInfo.getNpc()));
			}
		}

		if (config.debugNPC())
		{
			for (NPC npc : client.getNpcs())
			{
				NPCComposition npcComposition = npc.getTransformedComposition();
				//Do not show debug info for NPCs with invisible models
				if (npcComposition != null && ((npc.getName() != null && !npc.getName().equals("") && !npc.getName().equals("null")) || !isInvisible(npc.getModel())))
				{
					LocalPoint lp = npc.getLocalLocation();
					if (lp != null)
					{
						Polygon tilePoly = Perspective.getCanvasTileAreaPoly(client, lp, npcComposition.getSize());
						if (tilePoly != null)
						{
							renderPoly(graphics, Color.GRAY, new Color(0, 0, 0, 0), 255, 0, tilePoly, config.tileWidth(), true);
							String text = "N: " + npc.getName() + " | ID: " + npc.getId();
							Point textLoc = npc.getCanvasTextLocation(graphics, text, npc.getLogicalHeight() + 40);
							if (textLoc != null)
							{
								drawTextBackground(graphics, textLoc, text);
								OverlayUtil.renderTextLocation(graphics, textLoc, text, Color.WHITE);
							}
						}
					}
				}
			}
		}

		if (config.respawnTimer() != BetterNpcHighlightConfig.respawnTimerMode.OFF)
		{
			for (NpcSpawn n : plugin.npcSpawns)
			{
				if (n.spawnPoint != null && n.respawnTime != -1 && n.dead)
				{
					final LocalPoint lp = LocalPoint.fromWorld(client, n.spawnPoint.getX(), n.spawnPoint.getY());

					if (lp != null)
					{
						final LocalPoint centerLp = new LocalPoint(lp.getX() + Perspective.LOCAL_TILE_SIZE * (n.size - 1) / 2, lp.getY() + Perspective.LOCAL_TILE_SIZE * (n.size - 1) / 2);
						Color outlineColor = config.respawnOutlineColor();
						Color fillColor = config.respawnFillColor();
						Color raveColor = Color.WHITE;
						int width = config.respawnTileWidth();
						if (plugin.getTurboIndex(n.id, n.name.toLowerCase()) != -1)
						{
							raveColor = plugin.turboColors.get(plugin.getTurboIndex(n.id, n.name.toLowerCase()));
							outlineColor = new Color(raveColor.getRed(), raveColor.getGreen(), raveColor.getBlue(), new Random().nextInt(254) + 1);
							fillColor = new Color(raveColor.getRed(), raveColor.getGreen(), raveColor.getBlue(), new Random().nextInt(254) + 1);
							width = plugin.turboTileWidth;
						}

						Polygon tilePoly = Perspective.getCanvasTileAreaPoly(client, centerLp, n.size);
						if (tilePoly != null)
						{
							renderPoly(graphics, outlineColor, fillColor, outlineColor.getAlpha(), fillColor.getAlpha(), tilePoly, width, true);
						}

						String text;
						if (config.respawnTimer() == BetterNpcHighlightConfig.respawnTimerMode.SECONDS)
						{
							final Instant now = Instant.now();
							final double baseTick = (n.respawnTime - (client.getTickCount() - n.diedOnTick)) * (Constants.GAME_TICK_LENGTH / 1000.0);
							final double sinceLast = (now.toEpochMilli() - plugin.lastTickUpdate.toEpochMilli()) / 1000.0;
							final double timeLeft = Math.max(0, baseTick - sinceLast);
							text = String.valueOf(timeLeft);
							if (text.contains("."))
							{
								text = text.substring(0, text.indexOf(".") + 2);
							}
						}
						else
						{
							text = String.valueOf(Math.max(0, (n.respawnTime - (client.getTickCount() - n.diedOnTick))));
						}

						Point textLoc = Perspective.getCanvasTextLocation(client, graphics, centerLp, text, 0);
						if (textLoc != null)
						{
							drawTextBackground(graphics, textLoc, text);
							if (raveColor != Color.WHITE)
							{
								OverlayUtil.renderTextLocation(graphics, textLoc, text, new Color(raveColor.getRed(), raveColor.getGreen(), raveColor.getBlue(), new Random().nextInt(205) + 50));
							}
							else
							{
								OverlayUtil.renderTextLocation(graphics, textLoc, text, config.respawnTimerColor());
							}
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Create overlays for NPCs to highlight.
	 *
	 * @param graphics  graphics
	 * @param npcInfo   NPCInfo
	 * @param highlight Style to highlight the NPC
	 */
	protected void renderNpcOverlay(Graphics2D graphics, NPCInfo npcInfo, String highlight)
	{
		NPC npc = npcInfo.getNpc();
		NPCComposition npcComposition = npc.getTransformedComposition();
		if (npcComposition != null)
		{
			int size = npcComposition.getSize();
			Polygon tilePoly;
			LocalPoint lp;
			Color line;
			Color fill;
			int lineAlpha;
			int fillAlpha;
			boolean antialias;
			boolean isTask = npcInfo.isTask() && config.slayerHighlight();

			switch (highlight)
			{
				case "hull":
					line = isTask ? config.slayerRave() ? plugin.getRaveColor(config.slayerRaveSpeed()) : config.taskColor()
						: config.hullRave() ? plugin.getRaveColor(config.hullRaveSpeed()) : npcInfo.getHull().getColor();
					fill = isTask ? config.slayerRave() ? plugin.getRaveColor(config.slayerRaveSpeed()) : config.taskFillColor()
						: config.hullRave() ? plugin.getRaveColor(config.hullRaveSpeed()) : npcInfo.getHull().getFill();
					lineAlpha = isTask ? config.taskColor().getAlpha() : npcInfo.getHull().getColor().getAlpha();
					fillAlpha = isTask ? config.taskFillColor().getAlpha() : npcInfo.getHull().getFill().getAlpha();
					antialias = isTask ? config.slayerAA() : config.hullAA();

					Shape hull = npc.getConvexHull();
					if (hull != null)
					{
						renderPoly(graphics, line, fill, lineAlpha, fillAlpha, hull, config.hullWidth(), antialias);
					}
					break;
				case "tile":
					line = isTask ? config.slayerRave() ? plugin.getRaveColor(config.slayerRaveSpeed()) : config.taskColor()
						: config.tileRave() ? plugin.getRaveColor(config.tileRaveSpeed()) : npcInfo.getTile().getColor();
					fill = isTask ? config.slayerRave() ? plugin.getRaveColor(config.slayerRaveSpeed()) : config.taskFillColor()
						: config.tileRave() ? plugin.getRaveColor(config.tileRaveSpeed()) : npcInfo.getTile().getFill();
					lineAlpha = isTask ? config.taskColor().getAlpha() : npcInfo.getTile().getColor().getAlpha();
					fillAlpha = isTask ? config.taskFillColor().getAlpha() : npcInfo.getTile().getFill().getAlpha();
					antialias = isTask ? config.slayerAA() : config.tileAA();

					lp = npc.getLocalLocation();
					if (lp != null)
					{
						tilePoly = Perspective.getCanvasTileAreaPoly(client, lp, size);
						if (tilePoly != null)
						{
							switch (config.tileLines())
							{
								case REG:
									renderPoly(graphics, line, fill, lineAlpha, fillAlpha, tilePoly, config.tileWidth(), antialias);
									break;
								case DASH:
									renderPolygonDashed(graphics, line, fill, lineAlpha, fillAlpha, tilePoly, config.tileWidth(), size, antialias);
									break;
								case CORNER:
									renderPolygonCorners(graphics, line, fill, lineAlpha, fillAlpha, tilePoly, config.tileWidth(), antialias);
									break;
							}
						}
					}
					break;
				case "trueTile":
					line = isTask ? config.slayerRave() ? plugin.getRaveColor(config.slayerRaveSpeed()) : config.taskColor()
						: config.trueTileRave() ? plugin.getRaveColor(config.trueTileRaveSpeed()) : npcInfo.getTrueTile().getColor();
					fill = isTask ? config.slayerRave() ? plugin.getRaveColor(config.slayerRaveSpeed()) : config.taskFillColor()
						: config.trueTileRave() ? plugin.getRaveColor(config.trueTileRaveSpeed()) : npcInfo.getTrueTile().getFill();
					lineAlpha = isTask ? config.taskColor().getAlpha() : npcInfo.getTrueTile().getColor().getAlpha();
					fillAlpha = isTask ? config.taskFillColor().getAlpha() : npcInfo.getTrueTile().getFill().getAlpha();
					antialias = isTask ? config.slayerAA() : config.trueTileAA();

					lp = LocalPoint.fromWorld(client, npc.getWorldLocation());
					if (lp != null)
					{
						lp = new LocalPoint(lp.getX() + size * 128 / 2 - 64, lp.getY() + size * 128 / 2 - 64);
						tilePoly = Perspective.getCanvasTileAreaPoly(client, lp, size);
						if (tilePoly != null)
						{
							switch (config.trueTileLines())
							{
								case REG:
									renderPoly(graphics, line, fill, lineAlpha, fillAlpha, tilePoly, config.trueTileWidth(), antialias);
									break;
								case DASH:
									renderPolygonDashed(graphics, line, fill, lineAlpha, fillAlpha, tilePoly, config.trueTileWidth(), size, antialias);
									break;
								case CORNER:
									renderPolygonCorners(graphics, line, fill, lineAlpha, fillAlpha, tilePoly, config.trueTileWidth(), antialias);
									break;
							}
						}
					}
					break;
				case "swTile":
					line = isTask ? config.slayerRave() ? plugin.getRaveColor(config.slayerRaveSpeed()) : config.taskColor()
						: config.swTileRave() ? plugin.getRaveColor(config.swTileRaveSpeed()) : npcInfo.getSwTile().getColor();
					fill = isTask ? config.slayerRave() ? plugin.getRaveColor(config.slayerRaveSpeed()) : config.taskFillColor()
						: config.swTileRave() ? plugin.getRaveColor(config.swTileRaveSpeed()) : npcInfo.getSwTile().getFill();
					lineAlpha = isTask ? config.taskColor().getAlpha() : npcInfo.getSwTile().getColor().getAlpha();
					fillAlpha = isTask ? config.taskFillColor().getAlpha() : npcInfo.getSwTile().getFill().getAlpha();
					antialias = isTask ? config.slayerAA() : config.swTileAA();

					lp = npc.getLocalLocation();
					if (lp != null)
					{
						int x = lp.getX() - (size - 1) * 128 / 2;
						int y = lp.getY() - (size - 1) * 128 / 2;
						tilePoly = Perspective.getCanvasTilePoly(client, new LocalPoint(x, y));
						if (tilePoly != null)
						{
							switch (config.swTileLines())
							{
								case REG:
									renderPoly(graphics, line, fill, lineAlpha, fillAlpha, tilePoly, config.swTileWidth(), antialias);
									break;
								case DASH:
									renderPolygonDashed(graphics, line, fill, lineAlpha, fillAlpha, tilePoly, config.swTileWidth(), size, antialias);
									break;
								case CORNER:
									renderPolygonCorners(graphics, line, fill, lineAlpha, fillAlpha, tilePoly, config.swTileWidth(), antialias);
									break;
							}
						}
					}
					break;
				case "swTrueTile":
					line = isTask ? config.slayerRave() ? plugin.getRaveColor(config.slayerRaveSpeed()) : config.taskColor()
						: config.swTrueTileRave() ? plugin.getRaveColor(config.swTrueTileRaveSpeed()) : npcInfo.getSwTrueTile().getColor();
					fill = isTask ? config.slayerRave() ? plugin.getRaveColor(config.slayerRaveSpeed()) : config.taskFillColor()
						: config.swTrueTileRave() ? plugin.getRaveColor(config.swTrueTileRaveSpeed()) : npcInfo.getSwTrueTile().getFill();
					lineAlpha = isTask ? config.taskColor().getAlpha() : npcInfo.getSwTrueTile().getColor().getAlpha();
					fillAlpha = isTask ? config.taskFillColor().getAlpha() : npcInfo.getSwTrueTile().getFill().getAlpha();
					antialias = isTask ? config.slayerAA() : config.swTrueTileAA();

					lp = LocalPoint.fromWorld(client, npc.getWorldLocation());
					if (lp != null)
					{
						tilePoly = Perspective.getCanvasTilePoly(client, lp);
						if (tilePoly != null)
						{
							switch (config.swTrueTileLines())
							{
								case REG:
									renderPoly(graphics, line, fill, lineAlpha, fillAlpha, tilePoly, config.swTrueTileWidth(), antialias);
									break;
								case DASH:
									renderPolygonDashed(graphics, line, fill, lineAlpha, fillAlpha, tilePoly, config.swTrueTileWidth(), size, antialias);
									break;
								case CORNER:
									renderPolygonCorners(graphics, line, fill, lineAlpha, fillAlpha, tilePoly, config.swTrueTileWidth(), antialias);
									break;
							}
						}
					}
					break;
				case "outline":
					line = isTask ? config.slayerRave() ? plugin.getRaveColor(config.slayerRaveSpeed()) : config.taskColor()
						: config.outlineRave() ? plugin.getRaveColor(config.outlineRaveSpeed()) : npcInfo.getOutline().getColor();

					modelOutlineRenderer.drawOutline(npc, config.outlineWidth(), line, config.outlineFeather());
					break;
				case "area":
					Color color = npcInfo.getArea().getFill() != null ? npcInfo.getArea().getFill() : npcInfo.getArea().getColor();
					fill = isTask ? config.slayerRave() ? plugin.getRaveColor(config.slayerRaveSpeed()) : config.taskFillColor()
						: config.areaRave() ? plugin.getRaveColor(config.areaRaveSpeed()) : color;
					fillAlpha = isTask ? config.taskFillColor().getAlpha() : color.getAlpha();

					Shape area = npc.getConvexHull();
					if (area != null)
					{
						graphics.setColor(fill.getAlpha() == 0 ? new Color(fill.getRed(), fill.getGreen(), fill.getGreen(), 50)
							: new Color(fill.getRed(), fill.getGreen(), fill.getBlue(), fillAlpha));
						graphics.fill(area);
					}
					break;
				case "clickbox":
					line = isTask ? config.slayerRave() ? plugin.getRaveColor(config.slayerRaveSpeed()) : config.taskColor()
						: config.clickboxRave() ? plugin.getRaveColor(config.clickboxRaveSpeed()) : npcInfo.getClickbox().getColor();
					fill = isTask ? config.slayerRave() ? plugin.getRaveColor(config.slayerRaveSpeed()) : config.taskFillColor()
						: config.clickboxRave() ? plugin.getRaveColor(config.clickboxRaveSpeed()) : npcInfo.getClickbox().getFill();
					lineAlpha = isTask ? config.taskColor().getAlpha() : npcInfo.getClickbox().getColor().getAlpha();
					fillAlpha = isTask ? config.taskFillColor().getAlpha() : npcInfo.getClickbox().getFill().getAlpha();

					lp = npc.getLocalLocation();
					if (lp != null)
					{
						Shape clickbox = Perspective.getClickbox(client, npc.getModel(), npc.getCurrentOrientation(), lp.getX(), lp.getY(),
							Perspective.getTileHeight(client, lp, npc.getWorldLocation().getPlane()));
						renderClickbox(graphics, clickbox, client.getMouseCanvasPosition(), line, fill, lineAlpha, fillAlpha, line.darker(), config.clickboxAA());
					}
					break;
				case "turbo":
					Color raveColor = plugin.turboColors.get(plugin.npcList.indexOf(npcInfo));
					if (raveColor != null)
					{
						line = new Color(raveColor.getRed(), raveColor.getGreen(), raveColor.getBlue(), new Random().nextInt(254) + 1);
						fill = new Color(raveColor.getRed(), raveColor.getGreen(), raveColor.getBlue(), new Random().nextInt(254) + 1);
						int tileMode = new Random().nextInt(3);

						if (plugin.turboModeStyle == 0)
						{
							lp = npc.getLocalLocation();
							if (lp != null)
							{
								tilePoly = Perspective.getCanvasTileAreaPoly(client, lp, size);
								if (tilePoly != null)
								{
									if (tileMode == 0)
									{
										renderPoly(graphics, line, fill, line.getAlpha(), fill.getAlpha(), tilePoly, plugin.turboTileWidth, true);
									}
									else if (tileMode == 1)
									{
										renderPolygonDashed(graphics, line, fill, line.getAlpha(), fill.getAlpha(), tilePoly, plugin.turboTileWidth, size,true);
									}
									else
									{
										renderPolygonCorners(graphics, line, fill, line.getAlpha(), fill.getAlpha(), tilePoly, plugin.turboTileWidth, true);
									}
								}
							}
						}
						else if (plugin.turboModeStyle == 1)
						{
							lp = LocalPoint.fromWorld(client, npc.getWorldLocation());
							if (lp != null)
							{
								lp = new LocalPoint(lp.getX() + size * 128 / 2 - 64, lp.getY() + size * 128 / 2 - 64);
								tilePoly = Perspective.getCanvasTileAreaPoly(client, lp, size);
								if (tilePoly != null)
								{
									if (tileMode == 0)
									{
										renderPoly(graphics, line, fill, line.getAlpha(), fill.getAlpha(), tilePoly, plugin.turboTileWidth, true);
									}
									else if (tileMode == 1)
									{
										renderPolygonDashed(graphics, line, fill, line.getAlpha(), fill.getAlpha(), tilePoly, plugin.turboTileWidth, size,true);
									}
									else
									{
										renderPolygonCorners(graphics, line, fill, line.getAlpha(), fill.getAlpha(), tilePoly, plugin.turboTileWidth, true);
									}
								}
							}
						}
						else if (plugin.turboModeStyle == 2)
						{
							lp = npc.getLocalLocation();
							if (lp != null)
							{
								int x = lp.getX() - (size - 1) * 128 / 2;
								int y = lp.getY() - (size - 1) * 128 / 2;
								tilePoly = Perspective.getCanvasTilePoly(client, new LocalPoint(x, y));
								if (tilePoly != null)
								{
									if (tileMode == 0)
									{
										renderPoly(graphics, line, fill, line.getAlpha(), fill.getAlpha(), tilePoly, plugin.turboTileWidth, true);
									}
									else if (tileMode == 1)
									{
										renderPolygonDashed(graphics, line, fill, line.getAlpha(), fill.getAlpha(), tilePoly, plugin.turboTileWidth, size,true);
									}
									else
									{
										renderPolygonCorners(graphics, line, fill, line.getAlpha(), fill.getAlpha(), tilePoly, plugin.turboTileWidth, true);
									}
								}
							}
						}
						else if (plugin.turboModeStyle == 3)
						{
							if (npc.getConvexHull() != null)
							{
								renderPoly(graphics, line, fill, line.getAlpha(), fill.getAlpha(), npc.getConvexHull(), plugin.turboTileWidth, true);
							}
						}
						else if (plugin.turboModeStyle == 4)
						{
							if (npc.getConvexHull() != null)
							{
								graphics.setColor(fill);
								graphics.fill(npc.getConvexHull());
							}
						}
						else
						{
							modelOutlineRenderer.drawOutline(npc, plugin.turboTileWidth, line, plugin.turboOutlineFeather);
						}
					}
					break;
			}
		}
	}

	private void renderPoly(Graphics2D graphics, Color outlineColor, Color fillColor, int lineAlpha, int fillAlpha, Shape polygon, double width, boolean antiAlias)
	{
		if (polygon != null)
		{
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antiAlias ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
			graphics.setColor(new Color(outlineColor.getRed(), outlineColor.getGreen(), outlineColor.getBlue(), lineAlpha));
			graphics.setStroke(new BasicStroke((float) width));
			graphics.draw(polygon);
			graphics.setColor(new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), fillAlpha));
			graphics.fill(polygon);
		}
	}

	public static void renderClickbox(Graphics2D graphics, Shape area, Point mousePosition, Color line, Color fill, int lineAlpha, int fillAlpha, Color hovered, boolean antiAlias)
	{
		if (area != null)
		{
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antiAlias ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
			if (area.contains(mousePosition.getX(), mousePosition.getY()))
			{
				graphics.setColor(new Color(hovered.getRed(), hovered.getGreen(), hovered.getBlue(), lineAlpha));
			}
			else
			{
				graphics.setColor(new Color(line.getRed(), line.getGreen(), line.getBlue(), lineAlpha));
			}
			graphics.draw(area);
			graphics.setColor(new Color(fill.getRed(), fill.getGreen(), fill.getBlue(), fillAlpha));
			graphics.fill(area);
		}
	}

	/**
	 * Draws only the corners of NPC tile highlights - Made by Geheur
	 *
	 * @param graphics
	 * @param outlineColor
	 * @param fillColor
	 * @param lineAlpha
	 * @param fillAlpha
	 * @param poly
	 * @param width
	 * @param antiAlias
	 */
	private static void renderPolygonCorners(Graphics2D graphics, Color outlineColor, Color fillColor, int lineAlpha, int fillAlpha, Shape poly, double width, boolean antiAlias)
	{
		if (poly instanceof Polygon)
		{
			Polygon p = (Polygon) poly;
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antiAlias ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
			graphics.setColor(new Color(outlineColor.getRed(), outlineColor.getGreen(), outlineColor.getBlue(), lineAlpha));
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

			graphics.setColor(new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), fillAlpha));
			graphics.fill(poly);
		}
	}

	/**
	 * Draws the corners and dashed lines along each side of NPC tile highlights - Made by Geheur
	 *
	 * @param graphics
	 * @param outlineColor
	 * @param fillColor
	 * @param lineAlpha
	 * @param fillAlpha
	 * @param poly
	 * @param width
	 * @param tiles
	 * @param antiAlias
	 */
	private static void renderPolygonDashed(Graphics2D graphics, Color outlineColor, Color fillColor, int lineAlpha, int fillAlpha, Shape poly,
											double width, int tiles, boolean antiAlias)
	{
		if (poly instanceof Polygon)
		{
			Polygon p = (Polygon) poly;
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antiAlias ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
			graphics.setColor(new Color(outlineColor.getRed(), outlineColor.getGreen(), outlineColor.getBlue(), lineAlpha));
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

			graphics.setColor(new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), fillAlpha));
			graphics.fill(poly);
		}
	}

	private void drawTextBackground(Graphics2D graphics, Point textLoc, String text)
	{
		switch (config.fontBackground())
		{
			case OUTLINE:
			{
				OverlayUtil.renderTextLocation(graphics, new Point(textLoc.getX(), textLoc.getY() + 1), text, Color.BLACK);
				OverlayUtil.renderTextLocation(graphics, new Point(textLoc.getX(), textLoc.getY() - 1), text, Color.BLACK);
				OverlayUtil.renderTextLocation(graphics, new Point(textLoc.getX() + 1, textLoc.getY()), text, Color.BLACK);
				OverlayUtil.renderTextLocation(graphics, new Point(textLoc.getX() - 1, textLoc.getY()), text, Color.BLACK);
				break;
			}
			case SHADOW:
			{
				OverlayUtil.renderTextLocation(graphics, new Point(textLoc.getX() + 1, textLoc.getY() + 1), text, Color.BLACK);
				break;
			}
			default:
				break;
		}
	}

	//Copied from Skretzo
	private static boolean isInvisible(Model model)
	{
		// If all the values in model.getFaceColors3() are -1 then the model is invisible
		for (int value : model.getFaceColors3())
		{
			if (value != -1)
			{
				return false;
			}
		}
		return true;
	}

	//Made by LeikvollE
	private void removeActor(final Graphics2D graphics, final Actor actor)
	{
		final int clipX1 = client.getViewportXOffset();
		final int clipY1 = client.getViewportYOffset();
		final int clipX2 = client.getViewportWidth() + clipX1;
		final int clipY2 = client.getViewportHeight() + clipY1;
		Object origAA = graphics.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		Model model = actor.getModel();
		int vCount = model.getVerticesCount();
		int[] x3d = model.getVerticesX();
		int[] y3d = model.getVerticesY();
		int[] z3d = model.getVerticesZ();

		int[] x2d = new int[vCount];
		int[] y2d = new int[vCount];

		int size = 1;
		if (actor instanceof NPC)
		{
			NPCComposition composition = ((NPC) actor).getTransformedComposition();
			if (composition != null)
			{
				size = composition.getSize();
			}
		}

		final LocalPoint lp = actor.getLocalLocation();

		final int localX = lp.getX();
		final int localY = lp.getY();
		final int northEastX = lp.getX() + Perspective.LOCAL_TILE_SIZE * (size - 1) / 2;
		final int northEastY = lp.getY() + Perspective.LOCAL_TILE_SIZE * (size - 1) / 2;
		final LocalPoint northEastLp = new LocalPoint(northEastX, northEastY);
		int localZ = Perspective.getTileHeight(client, northEastLp, client.getPlane());
		int rotation = actor.getCurrentOrientation();

		Perspective.modelToCanvas(client, vCount, localX, localY, localZ, rotation, x3d, z3d, y3d, x2d, y2d);

		boolean anyVisible = false;

		for (int i = 0; i < vCount; i++)
		{
			int x = x2d[i];
			int y = y2d[i];

			boolean visibleX = x >= clipX1 && x < clipX2;
			boolean visibleY = y >= clipY1 && y < clipY2;
			anyVisible |= visibleX && visibleY;
		}

		if (!anyVisible)
		{
			return;
		}

		int tCount = model.getFaceCount();
		int[] tx = model.getFaceIndices1();
		int[] ty = model.getFaceIndices2();
		int[] tz = model.getFaceIndices3();

		Composite orig = graphics.getComposite();
		graphics.setComposite(AlphaComposite.Clear);
		graphics.setColor(Color.WHITE);
		for (int i = 0; i < tCount; i++)
		{
			// Cull tris facing away from the camera
			if (getTriDirection(x2d[tx[i]], y2d[tx[i]], x2d[ty[i]], y2d[ty[i]], x2d[tz[i]], y2d[tz[i]]) >= 0)
			{
				continue;
			}
			Polygon p = new Polygon(
				new int[]{x2d[tx[i]], x2d[ty[i]], x2d[tz[i]]},
				new int[]{y2d[tx[i]], y2d[ty[i]], y2d[tz[i]]},
				3);
			graphics.fill(p);

		}
		graphics.setComposite(orig);
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, origAA);
	}

	private int getTriDirection(int x1, int y1, int x2, int y2, int x3, int y3)
	{
		int x4 = x2 - x1;
		int y4 = y2 - y1;
		int x5 = x3 - x1;
		int y5 = y3 - y1;
		return x4 * y5 - y4 * x5;
	}

	private boolean withinDistanceLimit(NPC npc)
	{
		final int maxDistance = config.renderDistance().getDistance();
		return maxDistance == 0 || npc.getWorldArea().distanceTo(client.getLocalPlayer().getWorldArea()) - 1 <= maxDistance;
	}
}
