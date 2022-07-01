package com.betternpchighlight;

import net.runelite.api.Point;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.game.NpcUtil;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

import javax.inject.Inject;
import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Random;

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
		setPriority(OverlayPriority.HIGH);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	public Dimension render(Graphics2D graphics)
	{
		for (NPC npc : client.getNpcs())
		{
			boolean showWhileDead = !npcUtil.isDying(npc) || !config.ignoreDeadNpcs()
				|| plugin.checkSpecificList(plugin.ignoreDeadExclusionList, new ArrayList<>(), npc);

			if (npc.getHealthRatio() != 0 || showWhileDead)
			{
				Color outlineColor = Color.CYAN;
				Color fillColor = new Color(0, 255, 255, 20);

				NPCComposition npcComposition = npc.getTransformedComposition();
				if (npcComposition != null)
				{
					int size = npcComposition.getSize();
					if (config.tileHighlight() && plugin.checkSpecificList(plugin.tileNames, plugin.tileIds, npc))
					{
						outlineColor = config.tileColor();
						fillColor = config.tileFillColor();

						LocalPoint lp = npc.getLocalLocation();
						if (lp != null)
						{
							Polygon tilePoly = Perspective.getCanvasTileAreaPoly(client, lp, size);
							if (tilePoly != null)
							{
								renderPoly(graphics, outlineColor, fillColor, tilePoly, config.tileWidth(), config.tileAA());
							}
						}
					}

					if (config.trueTileHighlight() && plugin.checkSpecificList(plugin.trueTileNames, plugin.trueTileIds, npc))
					{
						outlineColor = config.trueTileColor();
						fillColor = config.trueTileFillColor();

						LocalPoint lp = LocalPoint.fromWorld(client, npc.getWorldLocation());
						if (lp != null)
						{
							lp = new LocalPoint(lp.getX() + size * 128 / 2 - 64, lp.getY() + size * 128 / 2 - 64);
							Polygon tilePoly = Perspective.getCanvasTileAreaPoly(client, lp, size);
							if (tilePoly != null)
							{
								renderPoly(graphics, outlineColor, fillColor, tilePoly, config.trueTileWidth(), config.trueTileAA());
							}
						}
					}

					if (config.swTileHighlight() && plugin.checkSpecificList(plugin.swTileNames, plugin.swTileIds, npc))
					{
						outlineColor = config.swTileColor();
						fillColor = config.swTileFillColor();

						LocalPoint lp = npc.getLocalLocation();
						if (lp != null)
						{
							int x = lp.getX() - (size - 1) * 128 / 2;
							int y = lp.getY() - (size - 1) * 128 / 2;
							Polygon tilePoly = Perspective.getCanvasTilePoly(client, new LocalPoint(x, y));
							if (tilePoly != null)
							{
								renderPoly(graphics, outlineColor, fillColor, tilePoly, config.swTileWidth(), config.swTileAA());
							}
						}
					}

					if (config.swTrueTileHighlight() && plugin.checkSpecificList(plugin.swTrueTileNames, plugin.swTrueTileIds, npc))
					{
						outlineColor = config.swTrueTileColor();
						fillColor = config.swTrueTileFillColor();

						LocalPoint lp = LocalPoint.fromWorld(client, npc.getWorldLocation());
						if (lp != null)
						{
							Polygon tilePoly = Perspective.getCanvasTilePoly(client, lp);
							renderPoly(graphics, outlineColor, fillColor, tilePoly, config.swTrueTileWidth(), config.swTrueTileAA());
						}
					}

					if (config.hullHighlight() && plugin.checkSpecificList(plugin.hullNames, plugin.hullIds, npc))
					{
						outlineColor = config.hullColor();
						fillColor = config.hullFillColor();

						Shape objectClickbox = npc.getConvexHull();
						if (objectClickbox != null)
						{
							renderPoly(graphics, outlineColor, fillColor, objectClickbox, config.hullWidth(), config.hullAA());
						}
					}

					if (config.areaHighlight() && plugin.checkSpecificList(plugin.areaNames, plugin.areaIds, npc))
					{
						fillColor = config.areaColor();

						graphics.setColor(fillColor);
						graphics.fill(npc.getConvexHull());
					}

					if (config.outlineHighlight() && plugin.checkSpecificList(plugin.outlineNames, plugin.outlineIds, npc))
					{
						outlineColor = config.outlineColor();

						modelOutlineRenderer.drawOutline(npc, config.outlineWidth(), outlineColor, config.outlineFeather());
					}

					if (config.clickboxHighlight() && plugin.checkSpecificList(plugin.clickboxNames, plugin.clickboxIds, npc))
					{
						outlineColor = config.clickboxColor();
						fillColor = config.clickboxFillColor();

						LocalPoint lp = npc.getLocalLocation();
						if (lp != null)
						{
							Shape clickbox = Perspective.getClickbox(client, npc.getModel(), npc.getCurrentOrientation(), lp.getX(), lp.getY(),
								Perspective.getTileHeight(client, lp, npc.getWorldLocation().getPlane()));
							OverlayUtil.renderHoverableArea(graphics, clickbox, client.getMouseCanvasPosition(), fillColor, outlineColor, outlineColor.darker());
						}
					}

					if (config.turboHighlight() && plugin.checkSpecificList(plugin.turboNames, plugin.turboIds, npc))
					{
						Color raveColor = plugin.turboIds.contains(npc.getId()) ? plugin.turboColors.get(plugin.turboIds.indexOf(npc.getId()) + plugin.turboNames.size()) : Color.WHITE;
						outlineColor = new Color(raveColor.getRed(), raveColor.getGreen(), raveColor.getBlue(), new Random().nextInt(254) + 1);
						fillColor = new Color(raveColor.getRed(), raveColor.getGreen(), raveColor.getBlue(), new Random().nextInt(254) + 1);
						if (raveColor == Color.WHITE)
						{
							if (npc.getName() != null)
							{
								String name = npc.getName().toLowerCase();
								int index = 0;
								for (String str : plugin.turboNames)
								{
									if (str.equalsIgnoreCase(name) || (str.contains("*")
										&& ((str.startsWith("*") && str.endsWith("*") && name.contains(str.replace("*", "")))
										|| (str.startsWith("*") && name.endsWith(str.replace("*", ""))) || name.startsWith(str.replace("*", "")))))
									{
										raveColor = plugin.turboColors.get(index);
										outlineColor = new Color(raveColor.getRed(), raveColor.getGreen(), raveColor.getBlue(), new Random().nextInt(254) + 1);
										fillColor = new Color(raveColor.getRed(), raveColor.getGreen(), raveColor.getBlue(), new Random().nextInt(254) + 1);
										break;
									}
									index++;
								}
							}
						}

						if (plugin.turboModeStyle == 0)
						{
							LocalPoint lp = npc.getLocalLocation();
							if (lp != null)
							{
								Polygon tilePoly = Perspective.getCanvasTileAreaPoly(client, lp, size);
								if (tilePoly != null)
								{
									renderPoly(graphics, outlineColor, fillColor, tilePoly, plugin.turboTileWidth, true);
								}
							}
						}
						else if (plugin.turboModeStyle == 1)
						{
							LocalPoint lp = LocalPoint.fromWorld(client, npc.getWorldLocation());
							if (lp != null)
							{
								lp = new LocalPoint(lp.getX() + size * 128 / 2 - 64, lp.getY() + size * 128 / 2 - 64);
								Polygon tilePoly = Perspective.getCanvasTileAreaPoly(client, lp, size);
								if (tilePoly != null)
								{
									renderPoly(graphics, outlineColor, fillColor, tilePoly, plugin.turboTileWidth, true);
								}
							}
						}
						else if (plugin.turboModeStyle == 2)
						{
							LocalPoint lp = npc.getLocalLocation();
							if (lp != null)
							{
								int x = lp.getX() - (size - 1) * 128 / 2;
								int y = lp.getY() - (size - 1) * 128 / 2;
								Polygon tilePoly = Perspective.getCanvasTilePoly(client, new LocalPoint(x, y));
								if (tilePoly != null)
								{
									renderPoly(graphics, outlineColor, fillColor, tilePoly, plugin.turboTileWidth, true);
								}
							}
						}
						else if (plugin.turboModeStyle == 3)
						{
							Shape objectClickbox = npc.getConvexHull();
							if (objectClickbox != null)
							{
								renderPoly(graphics, outlineColor, fillColor, objectClickbox, plugin.turboTileWidth, true);
							}
						}
						else if (plugin.turboModeStyle == 4)
						{
							graphics.setColor(fillColor);
							graphics.fill(npc.getConvexHull());
						}
						else
						{
							modelOutlineRenderer.drawOutline(npc, plugin.turboTileWidth, outlineColor, plugin.turboOutlineFeather);
						}
					}

					if (plugin.namesToDisplay.size() > 0 && npc.getName() != null && plugin.namesToDisplay.contains(npc.getName().toLowerCase()))
					{
						String text = npc.getName();
						Point textLoc = npc.getCanvasTextLocation(graphics, text, npc.getLogicalHeight() + 20);
						if (textLoc != null)
						{
							Point pointShadow = new Point(textLoc.getX() + 1, textLoc.getY() + 1);
							OverlayUtil.renderTextLocation(graphics, pointShadow, text, Color.BLACK);
							OverlayUtil.renderTextLocation(graphics, textLoc, text, outlineColor);
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
							renderPoly(graphics, outlineColor, fillColor, tilePoly, width, true);
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

						Point textLoc = Perspective.getCanvasTextLocation(client, graphics, lp, text, 0);
						if (textLoc != null)
						{
							Point pointShadow = new Point(textLoc.getX() + 1, textLoc.getY() + 1);
							OverlayUtil.renderTextLocation(graphics, pointShadow, text, Color.BLACK);
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

	private void renderPoly(Graphics2D graphics, Color outlineColor, Color fillColor, Shape polygon, double width, boolean antiAlias)
	{
		if (polygon != null)
		{
			if (antiAlias)
			{
				graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			}
			else
			{
				graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			}
			graphics.setColor(outlineColor);
			graphics.setStroke(new BasicStroke((float) width));
			graphics.draw(polygon);
			graphics.setColor(fillColor);
			graphics.fill(polygon);
		}
	}
}
