package com.coxadditions.overlay;

import com.coxadditions.CoxAdditionsConfig;
import com.coxadditions.CoxAdditionsPlugin;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.InstanceTemplates;
import net.runelite.api.NPC;
import net.runelite.api.Point;
import net.runelite.api.Varbits;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;

public class CoxHPOverlay extends Overlay
{
	private final Client client;
	private final CoxAdditionsPlugin plugin;
	private final CoxAdditionsConfig config;

	@Inject
	private CoxHPOverlay(final Client client, final CoxAdditionsPlugin plugin, final CoxAdditionsConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setPriority(OverlayPriority.HIGH);
		setLayer(OverlayLayer.UNDER_WIDGETS);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (plugin.getOverlayFont() == null)
		{
			plugin.loadFont(true);
		}

		if (plugin.isInRaid() && client.getLocalPlayer() != null)
		{
			boolean solo = client.getVarbitValue(Varbits.RAID_PARTY_SIZE) == 1;

			if (config.smallMuttaHp() && plugin.isSmallMuttaAlive() && !plugin.getSmallMutta().isDead()
				&& plugin.room() == InstanceTemplates.RAIDS_MUTTADILES)
			{
				NPC smallMutta = plugin.getSmallMutta();
				if (smallMutta.getHealthRatio() > 0 || (plugin.getLastRatio() != 0 && plugin.getLastHealthScale() != 0))
				{
					if (smallMutta.getHealthRatio() > 0)
					{
						plugin.setLastRatio(smallMutta.getHealthRatio());
						plugin.setLastHealthScale(smallMutta.getHealthScale());
					}
					drawHp(graphics, plugin.getSmallMutta(), plugin.getSmallMutta().getLogicalHeight() + 40, plugin.getLastRatio(), plugin.getLastHealthScale(), true);
				}
			}

			if (config.iceDemonHp() && plugin.getIceDemon() != null && !plugin.isIceDemonActive())
			{
				if (plugin.room() == InstanceTemplates.RAIDS_ICE_DEMON)
				{
					if (solo)
					{
						drawHp(graphics, plugin.getIceDemon(), plugin.getIceDemon().getLogicalHeight() + 40, plugin.getIceDemon().getHealthRatio(), plugin.getIceDemon().getHealthScale(), true);
					}
					else
					{
						drawHp(graphics, plugin.getIceDemon(), plugin.getIceDemon().getLogicalHeight() + 40, plugin.getIceDemon().getHealthRatio(), 100, true);
					}
				}
			}

			if (config.olmHandsHealth() == CoxAdditionsConfig.olmHandsHealthMode.OVERLAY && (plugin.getMageHand() != null || plugin.getMeleeHand() != null))
			{
				NPC mageHand = plugin.getMageHand();
				NPC meleeHand = plugin.getMeleeHand();
				if (solo)
				{
					//Hand HP in Solo Raids
					if (mageHand != null && plugin.getMageHandHp() > 0)
					{
						drawHp(graphics, mageHand, -75, plugin.getMageHandHp(), 100, false);
					}
					if (meleeHand != null && plugin.getMeleeHandHp() > 0)
					{
						drawHp(graphics, meleeHand, -75, plugin.getMeleeHandHp(), 100, false);
					}
				}
				else
				{
					//Hand HP in Team Raids
					if (mageHand != null)
					{
						if (mageHand.getHealthRatio() > 0 || (plugin.getMageHandLastRatio() != 0 && plugin.getMageHandLastHealthScale() != 0))
						{
							if (mageHand.getHealthRatio() > 0)
							{
								plugin.setMageHandLastRatio(mageHand.getHealthRatio());
								plugin.setMageHandLastHealthScale(mageHand.getHealthScale());
							}
							drawHp(graphics, mageHand, -75, plugin.getMageHandLastRatio(), plugin.getMageHandLastHealthScale(), true);
						}
					}

					if (meleeHand != null)
					{
						if (meleeHand.getHealthRatio() > 0 || (plugin.getMeleeHandLastRatio() != 0 && plugin.getMeleeHandLastHealthScale() != 0))
						{
							if (plugin.getMeleeHand().getHealthRatio() > 0)
							{
								plugin.setMeleeHandLastRatio(meleeHand.getHealthRatio());
								plugin.setMeleeHandLastHealthScale(meleeHand.getHealthScale());
							}
							drawHp(graphics, meleeHand, -75, plugin.getMeleeHandLastRatio(), plugin.getMeleeHandLastHealthScale(), true);
						}
					}
				}
			}
		}
		return null;
	}

	public Color getHPColor(float hpPercent)
	{
		hpPercent = Math.max(Math.min(100.0F, hpPercent), 0.0F);
		float rMod = 130.0F * hpPercent / 100.0F;
		float gMod = 235.0F * hpPercent / 100.0F;
		float bMod = 125.0F * hpPercent / 100.0F;
		int r = (int) Math.min(255.0F, 255.0F - rMod);
		int g = Math.min(255, (int) (0.0F + gMod));
		int b = Math.min(255, (int) (0.0F + bMod));
		return new Color(r, g, b);
	}

	private void drawHp(Graphics2D graphics, NPC npc, int offset, int ratio, int scale, boolean percent)
	{
		float health = percent ? ((float) ratio / (float) scale * 100) : ratio;
		String text = Float.toString(health);
		text = percent ? text.substring(0, text.indexOf(".")) + "%" : text.substring(0, text.indexOf("."));
		Point point = npc.getCanvasTextLocation(graphics, text, offset);

		if (point != null)
		{
			Font oldFont = graphics.getFont();
			graphics.setFont(plugin.getOverlayFont());
			drawTextBackground(graphics, point, text);
			OverlayUtil.renderTextLocation(graphics, point, text, percent ? getHPColor(health) : getHPColor((float) ratio / 600 * 100));
			graphics.setFont(oldFont);
		}
	}

	private void drawTextBackground(Graphics2D graphics, Point textLoc, String text)
	{
		switch (config.overlayFontBackground())
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
}
