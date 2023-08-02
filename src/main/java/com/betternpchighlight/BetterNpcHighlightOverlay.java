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
import java.util.Random;
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

				if (showNPC)
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
								String text = npc.getName();
								Point textLoc = npc.getCanvasTextLocation(graphics, text, npc.getLogicalHeight() + 20);
								if (textLoc != null)
								{
									Point pointShadow = new Point(textLoc.getX() + 1, textLoc.getY() + 1);
									OverlayUtil.renderTextLocation(graphics, pointShadow, text, Color.BLACK);
									OverlayUtil.renderTextLocation(graphics, textLoc, text, plugin.getSpecificColor(npcInfo));
									break;
								}
							}
						}
					}
				}
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
						Point point = npc.getCanvasTextLocation(graphics, "N: " + npc.getName() + " | ID: " + npc.getId(), npc.getLogicalHeight() + 40);
						Polygon tilePoly = Perspective.getCanvasTileAreaPoly(client, lp, npcComposition.getSize());
						if (tilePoly != null)
						{
							renderPoly(graphics, Color.GRAY, new Color(0, 0, 0, 0), 255, 0, tilePoly, config.tileWidth(), true);
							OverlayUtil.renderTextLocation(graphics, point, "N: " + npc.getName() + " | ID: " + npc.getId(), Color.WHITE);
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

	/**
	 * Create overlays for NPCs to highlight.
	 *
	 * @param graphics graphics
	 * @param npcInfo NPCInfo
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

			switch (highlight)
			{
				case "hull":
					line = npcInfo.isTask() ? config.slayerRave() ? plugin.getRaveColor(config.slayerRaveSpeed()) : config.taskColor()
						: config.hullRave() ? plugin.getRaveColor(config.hullRaveSpeed()) : npcInfo.getHull().getColor();
					fill = npcInfo.isTask() ? config.slayerRave() ? plugin.getRaveColor(config.slayerRaveSpeed()) : config.taskColor()
						: config.hullRave() ? plugin.getRaveColor(config.hullRaveSpeed()) : npcInfo.getHull().getFill();
					lineAlpha = npcInfo.isTask() ? config.taskColor().getAlpha() : npcInfo.getHull().getColor().getAlpha();
					fillAlpha = npcInfo.isTask() ? config.taskFillColor().getAlpha() : npcInfo.getHull().getFill().getAlpha();
					antialias = npcInfo.isTask() ? config.slayerAA() : config.hullAA();

					Shape hull = npc.getConvexHull();
					if (hull != null)
					{
						renderPoly(graphics, line, fill, lineAlpha, fillAlpha, hull, config.hullWidth(), antialias);
					}
					break;
				case "tile":
					line = npcInfo.isTask() ? config.slayerRave() ? plugin.getRaveColor(config.slayerRaveSpeed()) : config.taskColor()
						: config.tileRave() ? plugin.getRaveColor(config.tileRaveSpeed()) : npcInfo.getTile().getColor();
					fill = npcInfo.isTask() ? config.slayerRave() ? plugin.getRaveColor(config.slayerRaveSpeed()) : config.taskColor()
						: config.tileRave() ? plugin.getRaveColor(config.tileRaveSpeed()) : npcInfo.getTile().getFill();
					lineAlpha = npcInfo.isTask() ? config.taskColor().getAlpha() : npcInfo.getTile().getColor().getAlpha();
					fillAlpha = npcInfo.isTask() ? config.taskFillColor().getAlpha() : npcInfo.getTile().getFill().getAlpha();
					antialias = npcInfo.isTask() ? config.slayerAA() : config.tileAA();

					lp = npc.getLocalLocation();
					if (lp != null)
					{
						tilePoly = Perspective.getCanvasTileAreaPoly(client, lp, size);
						if (tilePoly != null)
						{
							renderPoly(graphics, line, fill, lineAlpha, fillAlpha, tilePoly, config.tileWidth(), antialias);
						}
					}
					break;
				case "trueTile":
					line = npcInfo.isTask() ? config.slayerRave() ? plugin.getRaveColor(config.slayerRaveSpeed()) : config.taskColor()
						: config.trueTileRave() ? plugin.getRaveColor(config.trueTileRaveSpeed()) : npcInfo.getTrueTile().getColor();
					fill = npcInfo.isTask() ? config.slayerRave() ? plugin.getRaveColor(config.slayerRaveSpeed()) : config.taskColor()
						: config.trueTileRave() ? plugin.getRaveColor(config.trueTileRaveSpeed()) : npcInfo.getTrueTile().getFill();
					lineAlpha = npcInfo.isTask() ? config.taskColor().getAlpha() : npcInfo.getTrueTile().getColor().getAlpha();
					fillAlpha = npcInfo.isTask() ? config.taskFillColor().getAlpha() : npcInfo.getTrueTile().getFill().getAlpha();
					antialias = npcInfo.isTask() ? config.slayerAA() : config.trueTileAA();

					lp = LocalPoint.fromWorld(client, npc.getWorldLocation());
					if (lp != null)
					{
						lp = new LocalPoint(lp.getX() + size * 128 / 2 - 64, lp.getY() + size * 128 / 2 - 64);
						tilePoly = Perspective.getCanvasTileAreaPoly(client, lp, size);
						if (tilePoly != null)
						{
							renderPoly(graphics, line, fill, lineAlpha, fillAlpha, tilePoly, config.trueTileWidth(), antialias);
						}
					}
					break;
				case "swTile":
					line = npcInfo.isTask() ? config.slayerRave() ? plugin.getRaveColor(config.slayerRaveSpeed()) : config.taskColor()
						: config.swTileRave() ? plugin.getRaveColor(config.swTileRaveSpeed()) : npcInfo.getSwTile().getColor();
					fill = npcInfo.isTask() ? config.slayerRave() ? plugin.getRaveColor(config.slayerRaveSpeed()) : config.taskColor()
						: config.swTileRave() ? plugin.getRaveColor(config.swTileRaveSpeed()) : npcInfo.getSwTile().getFill();
					lineAlpha = npcInfo.isTask() ? config.taskColor().getAlpha() : npcInfo.getSwTile().getColor().getAlpha();
					fillAlpha = npcInfo.isTask() ? config.taskFillColor().getAlpha() : npcInfo.getSwTile().getFill().getAlpha();
					antialias = npcInfo.isTask() ? config.slayerAA() : config.swTileAA();

					lp = npc.getLocalLocation();
					if (lp != null)
					{
						int x = lp.getX() - (size - 1) * 128 / 2;
						int y = lp.getY() - (size - 1) * 128 / 2;
						tilePoly = Perspective.getCanvasTilePoly(client, new LocalPoint(x, y));
						if (tilePoly != null)
						{
							renderPoly(graphics, line, fill, lineAlpha, fillAlpha, tilePoly, config.swTileWidth(), antialias);
						}
					}
					break;
				case "swTrueTile":
					line = npcInfo.isTask() ? config.slayerRave() ? plugin.getRaveColor(config.slayerRaveSpeed()) : config.taskColor()
						: config.swTrueTileRave() ? plugin.getRaveColor(config.swTrueTileRaveSpeed()) : npcInfo.getSwTrueTile().getColor();
					fill = npcInfo.isTask() ? config.slayerRave() ? plugin.getRaveColor(config.slayerRaveSpeed()) : config.taskColor()
						: config.swTrueTileRave() ? plugin.getRaveColor(config.swTrueTileRaveSpeed()) : npcInfo.getSwTrueTile().getFill();
					lineAlpha = npcInfo.isTask() ? config.taskColor().getAlpha() : npcInfo.getSwTrueTile().getColor().getAlpha();
					fillAlpha = npcInfo.isTask() ? config.taskFillColor().getAlpha() : npcInfo.getSwTrueTile().getFill().getAlpha();
					antialias = npcInfo.isTask() ? config.slayerAA() : config.swTrueTileAA();

					lp = LocalPoint.fromWorld(client, npc.getWorldLocation());
					if (lp != null)
					{
						tilePoly = Perspective.getCanvasTilePoly(client, lp);
						if (tilePoly != null)
						{
							renderPoly(graphics, line, fill, lineAlpha, fillAlpha, tilePoly, config.swTrueTileWidth(), antialias);
						}
					}
					break;
				case "outline":
					line = npcInfo.isTask() ? config.slayerRave() ? plugin.getRaveColor(config.slayerRaveSpeed()) : config.taskColor()
						: config.outlineRave() ? plugin.getRaveColor(config.outlineRaveSpeed()) : npcInfo.getOutline().getColor();

					modelOutlineRenderer.drawOutline(npc, config.outlineWidth(), line, config.outlineFeather());
					break;
				case "area":
					Color color = npcInfo.getArea().getFill() != null ? npcInfo.getArea().getFill() : npcInfo.getArea().getColor();
					fill = npcInfo.isTask() ? config.slayerRave() ? plugin.getRaveColor(config.slayerRaveSpeed()) : config.taskColor()
						: config.areaRave() ? plugin.getRaveColor(config.areaRaveSpeed()) : color;
					fillAlpha = npcInfo.isTask() ? config.taskColor().getAlpha() : color.getAlpha();

					Shape area = npc.getConvexHull();
					if (area != null)
					{
						graphics.setColor(fill.getAlpha() == 0 ? new Color(fill.getRed(), fill.getGreen(), fill.getGreen(), 50)
							: new Color(fill.getRed(), fill.getGreen(), fill.getBlue(), fillAlpha));
						graphics.fill(area);
					}
					break;
				case "clickbox":
					line = npcInfo.isTask() ? config.slayerRave() ? plugin.getRaveColor(config.slayerRaveSpeed()) : config.taskColor()
						: config.clickboxRave() ? plugin.getRaveColor(config.clickboxRaveSpeed()) : npcInfo.getClickbox().getColor();
					fill = npcInfo.isTask() ? config.slayerRave() ? plugin.getRaveColor(config.slayerRaveSpeed()) : config.taskColor()
						: config.clickboxRave() ? plugin.getRaveColor(config.clickboxRaveSpeed()) : npcInfo.getClickbox().getFill();
					lineAlpha = npcInfo.isTask() ? config.taskColor().getAlpha() : npcInfo.getClickbox().getColor().getAlpha();
					fillAlpha = npcInfo.isTask() ? config.taskFillColor().getAlpha() : npcInfo.getClickbox().getFill().getAlpha();

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

						if (plugin.turboModeStyle == 0)
						{
							lp = npc.getLocalLocation();
							if (lp != null)
							{
								tilePoly = Perspective.getCanvasTileAreaPoly(client, lp, size);
								if (tilePoly != null)
								{
									renderPoly(graphics, line, fill, line.getAlpha(), fill.getAlpha(), tilePoly, plugin.turboTileWidth, true);
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
									renderPoly(graphics, line, fill, line.getAlpha(), fill.getAlpha(), tilePoly, plugin.turboTileWidth, true);
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
									renderPoly(graphics, line, fill, line.getAlpha(), fill.getAlpha(), tilePoly, plugin.turboTileWidth, true);
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
}
