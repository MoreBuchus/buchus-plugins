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

import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.game.NpcUtil;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import java.awt.*;
import java.awt.geom.Point2D;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class BetterNpcHighlightOverlay extends Overlay {
    private final Client client;
    private final BetterNpcHighlightPlugin plugin;
    private final BetterNpcHighlightConfig config;
    private final ModelOutlineRenderer modelOutlineRenderer;
    private final NpcUtil npcUtil;

    @Inject
    private BetterNpcHighlightOverlay(Client client, BetterNpcHighlightPlugin plugin, BetterNpcHighlightConfig config,
                                      ModelOutlineRenderer modelOutlineRenderer, NpcUtil npcUtil) {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        this.modelOutlineRenderer = modelOutlineRenderer;
        this.npcUtil = npcUtil;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    public Dimension render(Graphics2D graphics) {
        // Main render loop
        plugin.npcList.forEach(npcInfo -> renderSingleNpc(graphics, npcInfo));

        // Collect NPCs for the "Draw Beneath" feature
        final List<NPCInfo> npcsToDrawBeneath = client.isGpu() && client.getLocalPlayer() != null
                ? getNpcsToDrawBeneath()
                : java.util.Collections.emptyList();
        npcsToDrawBeneath.forEach(nInfo -> removeActor(graphics, nInfo.getNpc()));

        // Debug and Respawn Timer rendering
        if (plugin.isDebugModeEnabled()) {
            for (NPC npc : client.getTopLevelWorldView().npcs()) {
                NPCComposition npcComposition = npc.getTransformedComposition();
                if (npcComposition != null && ((npc.getName() != null && !npc.getName().isEmpty() && !npc.getName().equals("null")) || !isInvisible(npc.getModel()))) {
                    LocalPoint lp = npc.getLocalLocation();
                    if (lp != null) {
                        Polygon tilePoly = Perspective.getCanvasTileAreaPoly(client, lp, npcComposition.getSize());
                        if (tilePoly != null) {
                            renderPoly(graphics, Color.GRAY, new Color(0, 0, 0, 0), 255, 0, tilePoly, config.tileWidth(), true);
                            String text = "N: " + npc.getName() + " | ID: " + npc.getId();
                            Point textLoc = npc.getCanvasTextLocation(graphics, text, npc.getLogicalHeight() + 40);
                            if (textLoc != null) {
                                drawTextBackground(graphics, textLoc, text);
                                OverlayUtil.renderTextLocation(graphics, textLoc, text, Color.WHITE);
                            }
                        }
                    }
                }
            }
        }

        if (config.respawnTimer() != BetterNpcHighlightConfig.respawnTimerMode.OFF) {
            for (NpcSpawn n : plugin.npcSpawns) {
                if (n.spawnPoint != null && n.respawnTime != -1 && n.dead) {
                    final LocalPoint lp = LocalPoint.fromWorld(client, n.spawnPoint.getX(), n.spawnPoint.getY());

                    if (lp != null) {
                        final LocalPoint centerLp = new LocalPoint(lp.getX() + Perspective.LOCAL_TILE_SIZE * (n.size - 1) / 2, lp.getY() + Perspective.LOCAL_TILE_SIZE * (n.size - 1) / 2, client.getTopLevelWorldView());
                        Color outlineColor = config.respawnOutlineColor();
                        Color fillColor = config.respawnFillColor();
                        Color raveColor = Color.WHITE;
                        int width = config.respawnTileWidth();

                        int turboIndex = plugin.getTurboIndex(n.id, n.name != null ? n.name.toLowerCase() : null);
                        if (turboIndex != -1 && turboIndex < plugin.turboColors.size()) {
                            raveColor = plugin.turboColors.get(turboIndex);
                            outlineColor = new Color(raveColor.getRed(), raveColor.getGreen(), raveColor.getBlue(), new Random().nextInt(254) + 1);
                            fillColor = new Color(raveColor.getRed(), raveColor.getGreen(), raveColor.getBlue(), new Random().nextInt(254) + 1);
                            width = plugin.turboTileWidth;
                        }

                        Polygon tilePoly = Perspective.getCanvasTileAreaPoly(client, centerLp, n.size);
                        if (tilePoly != null) {
                            renderPoly(graphics, outlineColor, fillColor, outlineColor.getAlpha(), fillColor.getAlpha(), tilePoly, width, true);
                        }

                        String text;
                        if (config.respawnTimer() == BetterNpcHighlightConfig.respawnTimerMode.SECONDS) {
                            final Instant now = Instant.now();
                            final double baseTick = (n.respawnTime - (client.getTickCount() - n.diedOnTick)) * (Constants.GAME_TICK_LENGTH / 1000.0);
                            final double sinceLast = (now.toEpochMilli() - plugin.lastTickUpdate.toEpochMilli()) / 1000.0;
                            final double timeLeft = Math.max(0, baseTick - sinceLast);
                            text = String.valueOf(timeLeft);
                            if (text.contains(".")) {
                                text = text.substring(0, text.indexOf(".") + 2);
                            }
                        } else {
                            text = String.valueOf(Math.max(0, (n.respawnTime - (client.getTickCount() - n.diedOnTick))));
                        }

                        Point textLoc = Perspective.getCanvasTextLocation(client, graphics, centerLp, text, 0);
                        if (textLoc != null) {
                            drawTextBackground(graphics, textLoc, text);
                            if (raveColor != Color.WHITE) {
                                OverlayUtil.renderTextLocation(graphics, textLoc, text, new Color(raveColor.getRed(), raveColor.getGreen(), raveColor.getBlue(), new Random().nextInt(205) + 50));
                            } else {
                                OverlayUtil.renderTextLocation(graphics, textLoc, text, config.respawnTimerColor());
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private List<NPCInfo> getNpcsToDrawBeneath() {
        final LocalPoint localPlayerLp = client.getLocalPlayer().getLocalLocation();
        if (localPlayerLp == null) {
            return java.util.Collections.emptyList();
        }

        return plugin.npcList.stream()
                .filter(npcInfo -> config.drawBeneathNpcs() || npcInfo.isDrawOverlayBeneathNpc())
                .filter(n -> n.getNpc().getLocalLocation() != null && !n.getNpc().isDead() && !npcUtil.isDying(n.getNpc()))
                .sorted(Comparator.comparingInt(n -> n.getNpc().getLocalLocation().distanceTo(localPlayerLp)))
                .limit(config.drawBeneathLimit())
                .collect(Collectors.toList());
    }

    private void renderSingleNpc(Graphics2D graphics, NPCInfo npcInfo) {
        NPC npc = npcInfo.getNpc();
        NPCComposition npcComposition = npc.getTransformedComposition();

        if (npcComposition == null || (isInvisible(npc.getModel()) && (npc.getName() == null || npc.getName().isEmpty() || "null".equals(npc.getName())))) {
            return;
        }

        boolean showWhileDead = (!npc.isDead() && !npcUtil.isDying(npc)) || !config.ignoreDeadNpcs() || npcInfo.isHighlightDead();
        boolean showNPC = (npcComposition.isFollower() && config.highlightPets()) || (!npcComposition.isFollower() && showWhileDead);

        if (!showNPC || !withinDistanceLimit(npc)) {
            return;
        }

        // Render highlights
        if (npcInfo.hasAnyHighlight()) {
            if (config.slayerHighlight() && npcInfo.isTask()) {
                renderNpcOverlay(graphics, npcInfo, config.taskHighlightStyle().name());
            } else {
                npcInfo.getHighlights().forEach((style, highlight) -> {
                    if (highlight.isHighlight()) {
                        renderNpcOverlay(graphics, npcInfo, style.name());
                    }
                });
            }
        }

        // Render name above NPC
        if (npcInfo.isDisplayNameAboveNpc() && npc.getName() != null) {
            renderName(graphics, npcInfo);
        }
    }

    private void renderName(Graphics2D graphics, NPCInfo npcInfo) {
        NPC npc = npcInfo.getNpc();
        String text = Text.removeTags(npc.getName());
        Point textLoc = npc.getCanvasTextLocation(graphics, text, npc.getLogicalHeight() + 40);

        if (textLoc != null) {
            drawTextBackground(graphics, textLoc, text);
            Color textColor = npcInfo.getDisplayNameColor();
            if (textColor == null) {
                textColor = config.tileColor(); // A default fallback
            }
            OverlayUtil.renderTextLocation(graphics, textLoc, text, textColor);
        }
    }

    private static class RenderStyle {
        final Color line;
        final Color fill;
        final int lineAlpha;
        final int fillAlpha;
        final boolean antiAlias;

        RenderStyle(Color line, Color fill, int lineAlpha, int fillAlpha, boolean antiAlias) {
            this.line = line;
            this.fill = fill;
            this.lineAlpha = lineAlpha;
            this.fillAlpha = fillAlpha;
            this.antiAlias = antiAlias;
        }
    }

    private RenderStyle getRenderStyle(NPCInfo npcInfo, HighlightInfo highlightInfo) {
        boolean isTask = npcInfo.isTask() && config.slayerHighlight();

        Color line = isTask
                ? (config.slayerRave() ? plugin.getRaveColor(config.slayerRaveSpeed()) : config.taskColor())
                : highlightInfo.getColor(); // Get base line color

        if (!isTask && highlightInfo.isRaveOutline() && line != null) {
            Color raveRgb = plugin.getRaveColor(highlightInfo.getRaveSpeed());
            line = new Color(raveRgb.getRed(), raveRgb.getGreen(), raveRgb.getBlue(), line.getAlpha());
        }

        Color fill = isTask
                ? (config.slayerRave() ? plugin.getRaveColor(config.slayerRaveSpeed()) : config.taskFillColor())
                : highlightInfo.getFill(); // Get base fill color

        if (!isTask && highlightInfo.isRaveFill() && fill != null) {
            Color raveRgb = plugin.getRaveColor(highlightInfo.getRaveSpeed());
            fill = new Color(raveRgb.getRed(), raveRgb.getGreen(), raveRgb.getBlue(), fill.getAlpha());
        }

        // Ensure colors are not null to prevent NullPointerException on .getAlpha()
        int lineAlpha = isTask ? config.taskColor().getAlpha() : (line != null ? line.getAlpha() : 255);
        int fillAlpha = isTask ? config.taskFillColor().getAlpha() : (fill != null ? fill.getAlpha() : 255);

        boolean antiAlias = isTask ? config.slayerAA() : highlightInfo.isAntiAliasing();

        return new RenderStyle(line, fill, lineAlpha, fillAlpha, antiAlias);
    }

    /**
     * Create overlays for NPCs to highlight.
     *
     * @param graphics           graphics
     * @param npcInfo            NPCInfo
     * @param highlightStyleName Style to highlight the NPC
     */
    protected void renderNpcOverlay(Graphics2D graphics, NPCInfo npcInfo, String highlightStyleName) {
        if (highlightStyleName.equalsIgnoreCase("None")) {
            return;
        }
        NPC npc = npcInfo.getNpc();

        NPCComposition npcComposition = npc.getTransformedComposition();
        if (npcComposition != null) {
            int size = npcComposition.getSize();
            Polygon tilePoly;
            LocalPoint lp;
            RenderStyle renderStyle;
            boolean isTask = npcInfo.isTask() && config.slayerHighlight();

            switch (highlightStyleName.toUpperCase()) {
                case "TILE":
                    if (!config.tileHighlight()) break;
                    HighlightInfo tileHighlight = npcInfo.getTile();
                    renderStyle = getRenderStyle(npcInfo, tileHighlight);

                    lp = npc.getLocalLocation();
                    if (lp != null) {
                        tilePoly = Perspective.getCanvasTileAreaPoly(client, lp, size);
                        if (tilePoly != null) {
                            switch (tileHighlight.getLineType()) {
                                case REGULAR:
                                    renderPoly(graphics, renderStyle.line, renderStyle.fill, renderStyle.lineAlpha, renderStyle.fillAlpha, tilePoly, tileHighlight.getOutlineWidth(), renderStyle.antiAlias);
                                    break;
                                case DASHED:
                                    renderPolygonDashed(graphics, renderStyle.line, renderStyle.fill, renderStyle.lineAlpha, renderStyle.fillAlpha, tilePoly, tileHighlight.getOutlineWidth(), size, renderStyle.antiAlias);
                                    break;
                                case CORNER:
                                    renderPolygonCorners(graphics, renderStyle.line, renderStyle.fill, renderStyle.lineAlpha, renderStyle.fillAlpha, tilePoly, tileHighlight.getOutlineWidth(), renderStyle.antiAlias);
                                    break;
                            }
                        }
                    }
                    break;
                case "TRUE_TILE":
                    if (!config.trueTileHighlight()) break;
                    HighlightInfo trueTileHighlight = npcInfo.getTrueTile();
                    renderStyle = getRenderStyle(npcInfo, trueTileHighlight);

                    lp = LocalPoint.fromWorld(client, npc.getWorldLocation());
                    if (lp != null) {
                        lp = new LocalPoint(lp.getX() + size * 128 / 2 - 64, lp.getY() + size * 128 / 2 - 64, client.getTopLevelWorldView());
                        tilePoly = Perspective.getCanvasTileAreaPoly(client, lp, size);
                        if (tilePoly != null) {
                            switch (trueTileHighlight.getLineType()) {
                                case REGULAR:
                                    renderPoly(graphics, renderStyle.line, renderStyle.fill, renderStyle.lineAlpha, renderStyle.fillAlpha, tilePoly, trueTileHighlight.getOutlineWidth(), renderStyle.antiAlias);
                                    break;
                                case DASHED:
                                    renderPolygonDashed(graphics, renderStyle.line, renderStyle.fill, renderStyle.lineAlpha, renderStyle.fillAlpha, tilePoly, trueTileHighlight.getOutlineWidth(), size, renderStyle.antiAlias);
                                    break;
                                case CORNER:
                                    renderPolygonCorners(graphics, renderStyle.line, renderStyle.fill, renderStyle.lineAlpha, renderStyle.fillAlpha, tilePoly, trueTileHighlight.getOutlineWidth(), renderStyle.antiAlias);
                                    break;
                            }
                        }
                    }
                    break;
                case "SW_TILE":
                    if (!config.swTileHighlight()) break;
                    HighlightInfo swTileHighlight = npcInfo.getSwTile();
                    renderStyle = getRenderStyle(npcInfo, swTileHighlight);

                    lp = npc.getLocalLocation();
                    if (lp != null) {
                        int x = lp.getX() - (size - 1) * 128 / 2;
                        int y = lp.getY() - (size - 1) * 128 / 2;
                        tilePoly = Perspective.getCanvasTilePoly(client, new LocalPoint(x, y, client.getTopLevelWorldView()));
                        if (tilePoly != null) {
                            switch (swTileHighlight.getLineType()) {
                                case REGULAR:
                                    renderPoly(graphics, renderStyle.line, renderStyle.fill, renderStyle.lineAlpha, renderStyle.fillAlpha, tilePoly, swTileHighlight.getOutlineWidth(), renderStyle.antiAlias);
                                    break;
                                case DASHED:
                                    renderPolygonDashed(graphics, renderStyle.line, renderStyle.fill, renderStyle.lineAlpha, renderStyle.fillAlpha, tilePoly, swTileHighlight.getOutlineWidth(), size, renderStyle.antiAlias);
                                    break;
                                case CORNER:
                                    renderPolygonCorners(graphics, renderStyle.line, renderStyle.fill, renderStyle.lineAlpha, renderStyle.fillAlpha, tilePoly, swTileHighlight.getOutlineWidth(), renderStyle.antiAlias);
                                    break;
                            }
                        }
                    }
                    break;
                case "SW_TRUE_TILE":
                    if (!config.swTrueTileHighlight()) break;
                    HighlightInfo swTrueTileHighlight = npcInfo.getSwTrueTile();
                    renderStyle = getRenderStyle(npcInfo, swTrueTileHighlight);

                    lp = LocalPoint.fromWorld(client, npc.getWorldLocation());
                    if (lp != null) {
                        tilePoly = Perspective.getCanvasTilePoly(client, lp);
                        if (tilePoly != null) {
                            switch (swTrueTileHighlight.getLineType()) {
                                case REGULAR:
                                    renderPoly(graphics, renderStyle.line, renderStyle.fill, renderStyle.lineAlpha, renderStyle.fillAlpha, tilePoly, swTrueTileHighlight.getOutlineWidth(), renderStyle.antiAlias);
                                    break;
                                case DASHED:
                                    renderPolygonDashed(graphics, renderStyle.line, renderStyle.fill, renderStyle.lineAlpha, renderStyle.fillAlpha, tilePoly, swTrueTileHighlight.getOutlineWidth(), size, renderStyle.antiAlias);
                                    break;
                                case CORNER:
                                    renderPolygonCorners(graphics, renderStyle.line, renderStyle.fill, renderStyle.lineAlpha, renderStyle.fillAlpha, tilePoly, swTrueTileHighlight.getOutlineWidth(), renderStyle.antiAlias);
                                    break;
                            }
                        }
                    }
                    break;
                case "HULL":
                    if (!config.hullHighlight()) break;
                    HighlightInfo hullHighlight = npcInfo.getHull();
                    renderStyle = getRenderStyle(npcInfo, hullHighlight);

                    Shape hull = npc.getConvexHull();
                    if (hull != null) {
                        renderPoly(graphics, renderStyle.line, renderStyle.fill, renderStyle.lineAlpha, renderStyle.fillAlpha, hull, hullHighlight.getOutlineWidth(), renderStyle.antiAlias);
                    }
                    break;
                case "OUTLINE":
                    if (!config.outlineHighlight()) break;
                    HighlightInfo outlineHighlight = npcInfo.getOutline();
                    Color line = isTask ? config.slayerRave() ? plugin.getRaveColor(config.slayerRaveSpeed()) : config.taskColor()
                            : outlineHighlight.isRaveOutline() ? plugin.getRaveColor(outlineHighlight.getRaveSpeed()) : outlineHighlight.getColor();

                    modelOutlineRenderer.drawOutline(npc, (int) outlineHighlight.getOutlineWidth(), line, outlineHighlight.getOutlineFeather());
                    break;
                case "AREA":
                    if (!config.areaHighlight()) break;
                    HighlightInfo areaHighlight = npcInfo.getArea();
                    Color color = areaHighlight.getFill() != null ? areaHighlight.getFill() : areaHighlight.getColor();
                    Color fill = isTask ? config.slayerRave() ? plugin.getRaveColor(config.slayerRaveSpeed()) : config.taskFillColor()
                            : areaHighlight.isRaveFill() ? plugin.getRaveColor(areaHighlight.getRaveSpeed()) : color;
                    int fillAlpha = isTask ? config.taskFillColor().getAlpha() : color.getAlpha();

                    Shape area = npc.getConvexHull();
                    if (area != null) {
                        graphics.setColor(fill.getAlpha() == 0 ? new Color(fill.getRed(), fill.getGreen(), fill.getBlue(), 50)
                                : new Color(fill.getRed(), fill.getGreen(), fill.getBlue(), fillAlpha));
                        graphics.fill(area);
                    }
                    break;
                case "CLICKBOX":
                    if (!config.clickboxHighlight()) break;
                    HighlightInfo clickboxHighlight = npcInfo.getClickbox();
                    renderStyle = getRenderStyle(npcInfo, clickboxHighlight);

                    lp = npc.getLocalLocation();
                    if (lp != null) {
                        Shape clickbox = Perspective.getClickbox(client, npc.getWorldView(), npc.getModel(), npc.getCurrentOrientation(), lp.getX(), lp.getY(),
                                Perspective.getTileHeight(client, lp, npc.getWorldLocation().getPlane()));
                        renderClickbox(graphics, clickbox, client.getMouseCanvasPosition(), renderStyle.line, renderStyle.fill, renderStyle.lineAlpha, renderStyle.fillAlpha, renderStyle.line.darker(), clickboxHighlight.getOutlineWidth(), renderStyle.antiAlias);
                    }
                    break;
                case "TURBO":
                    if (!config.turboHighlight()) break;
                    int turboIndex = plugin.npcList.indexOf(npcInfo);
                    if (turboIndex < 0 || turboIndex >= plugin.turboColors.size()) {
                        break;
                    }
                    Color raveColor = plugin.turboColors.get(turboIndex);
                    if (raveColor != null) {
                        Color turboLine = new Color(raveColor.getRed(), raveColor.getGreen(), raveColor.getBlue(), new Random().nextInt(254) + 1);
                        Color turboFill = new Color(raveColor.getRed(), raveColor.getGreen(), raveColor.getBlue(), new Random().nextInt(254) + 1);
                        int tileMode = new Random().nextInt(3);

                        if (plugin.turboModeStyle == 0) {
                            lp = npc.getLocalLocation();
                            if (lp != null) {
                                tilePoly = Perspective.getCanvasTileAreaPoly(client, lp, size);
                                if (tilePoly != null) {
                                    if (tileMode == 0) {
                                        renderPoly(graphics, turboLine, turboFill, turboLine.getAlpha(), turboFill.getAlpha(), tilePoly, plugin.turboTileWidth, true);
                                    } else if (tileMode == 1) {
                                        renderPolygonDashed(graphics, turboLine, turboFill, turboLine.getAlpha(), turboFill.getAlpha(), tilePoly, plugin.turboTileWidth, size, true);
                                    } else {
                                        renderPolygonCorners(graphics, turboLine, turboFill, turboLine.getAlpha(), turboFill.getAlpha(), tilePoly, plugin.turboTileWidth, true);
                                    }
                                }
                            }
                        } else if (plugin.turboModeStyle == 1) {
                            lp = LocalPoint.fromWorld(client, npc.getWorldLocation());
                            if (lp != null) {
                                lp = new LocalPoint(lp.getX() + size * 128 / 2 - 64, lp.getY() + size * 128 / 2 - 64, client.getTopLevelWorldView());
                                tilePoly = Perspective.getCanvasTileAreaPoly(client, lp, size);
                                if (tilePoly != null) {
                                    if (tileMode == 0) {
                                        renderPoly(graphics, turboLine, turboFill, turboLine.getAlpha(), turboFill.getAlpha(), tilePoly, plugin.turboTileWidth, true);
                                    } else if (tileMode == 1) {
                                        renderPolygonDashed(graphics, turboLine, turboFill, turboLine.getAlpha(), turboFill.getAlpha(), tilePoly, plugin.turboTileWidth, size, true);
                                    } else {
                                        renderPolygonCorners(graphics, turboLine, turboFill, turboLine.getAlpha(), turboFill.getAlpha(), tilePoly, plugin.turboTileWidth, true);
                                    }
                                }
                            }
                        } else if (plugin.turboModeStyle == 2) {
                            lp = npc.getLocalLocation();
                            if (lp != null) {
                                int x = lp.getX() - (size - 1) * 128 / 2;
                                int y = lp.getY() - (size - 1) * 128 / 2;
                                tilePoly = Perspective.getCanvasTilePoly(client, new LocalPoint(x, y, client.getTopLevelWorldView()));
                                if (tilePoly != null) {
                                    if (tileMode == 0) {
                                        renderPoly(graphics, turboLine, turboFill, turboLine.getAlpha(), turboFill.getAlpha(), tilePoly, plugin.turboTileWidth, true);
                                    } else if (tileMode == 1) {
                                        renderPolygonDashed(graphics, turboLine, turboFill, turboLine.getAlpha(), turboFill.getAlpha(), tilePoly, plugin.turboTileWidth, size, true);
                                    } else {
                                        renderPolygonCorners(graphics, turboLine, turboFill, turboLine.getAlpha(), turboFill.getAlpha(), tilePoly, plugin.turboTileWidth, true);
                                    }
                                }
                            }
                        } else if (plugin.turboModeStyle == 3) {
                            if (npc.getConvexHull() != null) {
                                renderPoly(graphics, turboLine, turboFill, turboLine.getAlpha(), turboFill.getAlpha(), npc.getConvexHull(), plugin.turboTileWidth, true);
                            }
                        } else if (plugin.turboModeStyle == 4) {
                            if (npc.getConvexHull() != null) {
                                graphics.setColor(turboFill);
                                graphics.fill(npc.getConvexHull());
                            }
                        } else {
                            modelOutlineRenderer.drawOutline(npc, plugin.turboTileWidth, turboLine, plugin.turboOutlineFeather);
                        }
                    }
                    break;
            }
        }
    }

    private void renderPoly(Graphics2D graphics, Color outlineColor, Color fillColor, int lineAlpha, int fillAlpha, Shape polygon, double width, boolean antiAlias) {
        if (polygon != null) {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antiAlias ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
            graphics.setColor(new Color(outlineColor.getRed(), outlineColor.getGreen(), outlineColor.getBlue(), lineAlpha));
            graphics.setStroke(new BasicStroke((float) width));
            graphics.draw(polygon);
            graphics.setColor(new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), fillAlpha));
            graphics.fill(polygon);
        }
    }

    public static void renderClickbox(Graphics2D graphics, Shape area, Point mousePosition, Color line, Color fill, int lineAlpha, int fillAlpha, Color hovered, double width, boolean antiAlias) {
        if (area != null) {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antiAlias ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
            if (area.contains(mousePosition.getX(), mousePosition.getY())) {
                graphics.setColor(new Color(hovered.getRed(), hovered.getGreen(), hovered.getBlue(), lineAlpha));
            } else {
                graphics.setColor(new Color(line.getRed(), line.getGreen(), line.getBlue(), lineAlpha));
            }
            graphics.setStroke(new BasicStroke((float) width));
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
    private static void renderPolygonCorners(Graphics2D graphics, Color outlineColor, Color fillColor, int lineAlpha, int fillAlpha, Shape poly, double width, boolean antiAlias) {
        if (poly instanceof Polygon) {
            Polygon p = (Polygon) poly;
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antiAlias ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
            graphics.setColor(new Color(outlineColor.getRed(), outlineColor.getGreen(), outlineColor.getBlue(), lineAlpha));
            graphics.setStroke(new BasicStroke((float) width));

            int divisor = 7;
            for (int i = 0; i < p.npoints; i++) {
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
                                            double width, int tiles, boolean antiAlias) {
        if (poly instanceof Polygon) {
            Polygon p = (Polygon) poly;
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antiAlias ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
            graphics.setColor(new Color(outlineColor.getRed(), outlineColor.getGreen(), outlineColor.getBlue(), lineAlpha));
            graphics.setStroke(new BasicStroke((float) width));

            int divisor = 7 * tiles;
            for (int i = 0; i < p.npoints; i++) {
                int ptx = p.xpoints[i];
                int pty = p.ypoints[i];
                int next = (i + 1) > 3 ? 0 : (i + 1);
                int ptxN = (p.xpoints[next]) - ptx;
                int ptyN = (p.ypoints[next]) - pty;
                float length = (float) Point2D.distance(ptx, pty, ptx + ptxN, pty + ptyN);
                float dashLength = length * 2f / divisor;
                float spaceLength = length * 5f / divisor;

                if (dashLength == 0 && spaceLength == 0) {
                    continue;
                }

                Stroke s = new BasicStroke((float) width, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10, new float[]{dashLength, spaceLength}, dashLength / 2);
                graphics.setStroke(s);
                graphics.drawLine(ptx, pty, ptx + ptxN, pty + ptyN);
            }

            graphics.setColor(new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), fillAlpha));
            graphics.fill(poly);
        }
    }

    private void drawTextBackground(Graphics2D graphics, Point textLoc, String text) {
        switch (config.fontBackground()) {
            case OUTLINE: {
                OverlayUtil.renderTextLocation(graphics, new Point(textLoc.getX(), textLoc.getY() + 1), text, Color.BLACK);
                OverlayUtil.renderTextLocation(graphics, new Point(textLoc.getX(), textLoc.getY() - 1), text, Color.BLACK);
                OverlayUtil.renderTextLocation(graphics, new Point(textLoc.getX() + 1, textLoc.getY()), text, Color.BLACK);
                OverlayUtil.renderTextLocation(graphics, new Point(textLoc.getX() - 1, textLoc.getY()), text, Color.BLACK);
                break;
            }
            case SHADOW: {
                OverlayUtil.renderTextLocation(graphics, new Point(textLoc.getX() + 1, textLoc.getY() + 1), text, Color.BLACK);
                break;
            }
            default:
                break;
        }
    }

    //Copied from Skretzo
    private static boolean isInvisible(Model model) {
        // If all the values in model.getFaceColors3() are -1 then the model is invisible
        for (int value : model.getFaceColors3()) {
            if (value != -1) {
                return false;
            }
        }
        return true;
    }

    //Made by LeikvollE
    private void removeActor(final Graphics2D graphics, final Actor actor) {
        final int clipX1 = client.getViewportXOffset();
        final int clipY1 = client.getViewportYOffset();
        final int clipX2 = client.getViewportWidth() + clipX1;
        final int clipY2 = client.getViewportHeight() + clipY1;
        Object origAA = graphics.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        Model model = actor.getModel();
        int vCount = model.getVerticesCount();
        float[] x3d = model.getVerticesX();
        float[] y3d = model.getVerticesY();
        float[] z3d = model.getVerticesZ();

        int[] x2d = new int[vCount];
        int[] y2d = new int[vCount];

        int size = 1;
        if (actor instanceof NPC) {
            NPCComposition composition = ((NPC) actor).getTransformedComposition();
            if (composition != null) {
                size = composition.getSize();
            }
        }

        final LocalPoint lp = actor.getLocalLocation();

        final int localX = lp.getX();
        final int localY = lp.getY();
        final int northEastX = lp.getX() + Perspective.LOCAL_TILE_SIZE * (size - 1) / 2;
        final int northEastY = lp.getY() + Perspective.LOCAL_TILE_SIZE * (size - 1) / 2;
        final LocalPoint northEastLp = new LocalPoint(northEastX, northEastY, client.getTopLevelWorldView());
        int localZ = Perspective.getTileHeight(client, northEastLp, client.getTopLevelWorldView().getPlane());
        int rotation = actor.getCurrentOrientation();

        Perspective.modelToCanvas(client, vCount, localX, localY, localZ, rotation, x3d, z3d, y3d, x2d, y2d);

        boolean anyVisible = false;

        for (int i = 0; i < vCount; i++) {
            int x = x2d[i];
            int y = y2d[i];

            boolean visibleX = x >= clipX1 && x < clipX2;
            boolean visibleY = y >= clipY1 && y < clipY2;
            anyVisible |= visibleX && visibleY;
        }

        if (!anyVisible) {
            return;
        }

        int tCount = model.getFaceCount();
        int[] tx = model.getFaceIndices1();
        int[] ty = model.getFaceIndices2();
        int[] tz = model.getFaceIndices3();

        Composite orig = graphics.getComposite();
        graphics.setComposite(AlphaComposite.Clear);
        graphics.setColor(Color.WHITE);
        for (int i = 0; i < tCount; i++) {
            // Cull tris facing away from the camera
            if (getTriDirection(x2d[tx[i]], y2d[tx[i]], x2d[ty[i]], y2d[ty[i]], x2d[tz[i]], y2d[tz[i]]) >= 0) {
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

    private int getTriDirection(int x1, int y1, int x2, int y2, int x3, int y3) {
        int x4 = x2 - x1;
        int y4 = y2 - y1;
        int x5 = x3 - x1;
        int y5 = y3 - y1;
        return x4 * y5 - y4 * x5;
    }

    private boolean withinDistanceLimit(NPC npc) {
        final int maxDistance = config.renderDistance().getDistance();
        return maxDistance == 0 || npc.getWorldArea().distanceTo(client.getLocalPlayer().getWorldArea()) - 1 <= maxDistance;
    }
}
