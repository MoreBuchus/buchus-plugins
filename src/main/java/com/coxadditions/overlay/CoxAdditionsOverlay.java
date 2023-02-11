/*
 * Copyright (c) 2022, Buchus <http://github.com/MoreBuchus>
 * Copyright (c) 2022, dey0 <http://github.com/dey0> - CC warning
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
package com.coxadditions.overlay;

import com.coxadditions.ChestGroup;
import com.coxadditions.CoxAdditionsConfig;
import com.coxadditions.CoxAdditionsPlugin;
import net.runelite.api.Point;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.components.ProgressPieComponent;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

@Singleton
public class CoxAdditionsOverlay extends Overlay
{
	private final Client client;
	private final CoxAdditionsPlugin plugin;
	private final CoxAdditionsConfig config;
	private final ModelOutlineRenderer modelOutlineRenderer;

	@Inject
	private CoxAdditionsOverlay(final Client client, final CoxAdditionsPlugin plugin, final CoxAdditionsConfig config, final ModelOutlineRenderer modelOutlineRenderer)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		this.modelOutlineRenderer = modelOutlineRenderer;
		setPosition(OverlayPosition.DYNAMIC);
		setPriority(OverlayPriority.HIGH);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (plugin.isInRaid())
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

			if (plugin.room() == InstanceTemplates.RAIDS_THIEVING && client.getLocalPlayer() != null
				&& WorldPoint.fromLocalInstance(client, client.getLocalPlayer().getLocalLocation()).getRegionID() == 13140)
			{
				Tile[][][] tiles = client.getScene().getTiles();
				for (int x = 0; x < Constants.SCENE_SIZE; ++x)
				{
					for (int y = 0; y < Constants.SCENE_SIZE; ++y)
					{
						Tile tile = tiles[client.getPlane()][x][y];
						if (tile != null && tile.getGameObjects() != null)
						{
							checkObjects:
							for (GameObject obj : tile.getGameObjects())
							{
								for (ChestGroup cg : ChestGroup.values())
								{
									if (obj != null && obj.getId() == 29742 && (cg.getRegionX() == obj.getWorldLocation().getRegionX() && cg.getRegionY() == obj.getWorldLocation().getRegionY())
										&& ((config.chestGroupsHighlight().contains(CoxAdditionsConfig.HighlightChestGroups.CHEST_GROUPS_1) && cg.getGroup() < 5)
										|| (config.chestGroupsHighlight().contains(CoxAdditionsConfig.HighlightChestGroups.CHEST_GROUPS_2) && (cg.getGroup() > 4 && cg.getGroup() < 8))
										|| (config.chestGroupsHighlight().contains(CoxAdditionsConfig.HighlightChestGroups.CHEST_GROUPS_3) && cg.getGroup() > 7)))
									{
										switch (config.chestGroupsHighlightStyle())
										{
											case HULL:
												Shape hull = obj.getConvexHull();
												if (hull != null)
												{
													renderPoly(graphics, cg.getColor(), hull, 2);
												}
												break;
											case OUTLINE:
												modelOutlineRenderer.drawOutline(obj, 2, cg.getColor(), 4);
												break;
											case TILE:
												Polygon tilePoly = Perspective.getCanvasTilePoly(client, obj.getLocalLocation());
												renderPoly(graphics, cg.getColor(), tilePoly, 2);
												break;
											case CLICKBOX:
												Shape clickbox = obj.getClickbox();
												if (clickbox != null)
												{
													renderPoly(graphics, cg.getColor(), clickbox, 2);
												}
												break;
										}
										break checkObjects;
									}
								}
							}
						}
					}
				}
			}
		}
		else
		{
			//Made by De0
			if (config.ccWarning())
			{
				int sceneX = 1232 - client.getBaseX(), sceneY = 3573 - client.getBaseY();
				if (sceneX >= 0 && sceneY >= 0 && sceneX < 104 && sceneY < 104)
				{
					Tile tile = client.getScene().getTiles()[0][sceneX][sceneY];
					if (tile.getGameObjects()[0] != null)
					{
						GameObject obj = tile.getGameObjects()[0];
						if (obj.getId() == ObjectID.CHAMBERS_OF_XERIC)
						{
							Color color = null;
							if (client.getFriendsChatManager() == null)
							{
								color = new Color(255, 0, 0, 50);
							}
							else if (client.getVarpValue(VarPlayer.IN_RAID_PARTY) == -1)
							{
								color = new Color(200, 200, 0, 50);
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
