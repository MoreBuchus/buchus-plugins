package com.coxadditions.overlay;

import com.coxadditions.CoxAdditionsConfig;
import com.coxadditions.CoxAdditionsPlugin;
import com.coxadditions.RaidsPlayers;
import com.google.common.base.Strings;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.ItemID;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.util.ImageUtil;

public class RaidsPotsStatusOverlay extends Overlay
{
	private final Client client;
	private final CoxAdditionsPlugin plugin;
	private final CoxAdditionsConfig config;

	private final ItemManager itemManager;

	@Inject
	public RaidsPotsStatusOverlay(Client client, CoxAdditionsPlugin plugin, CoxAdditionsConfig config, ItemManager itemManager)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;

		this.itemManager = itemManager;

		setPosition(OverlayPosition.DYNAMIC);
		setPriority(OverlayPriority.HIGH);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (config.raidsPotsStatusOverlay() && plugin.isInRaid())
		{
			for (RaidsPlayers raider : plugin.getPlayersInParty())
			{
				for (Player player : client.getPlayers())
				{
					if (player.getName() != null && player.getName().equals(raider.getPlayer()))
					{
						boolean offset = false;
						if (raider.isOvlActive() && raider.getOvlTicks() >= 0)
						{
							drawTimer(graphics, "OVL", raider.getOvlTicks(), player, false);
							offset = true;
						}

						if (raider.isEnhActive() && raider.getEnhTicks() >= 0)
						{
							drawTimer(graphics, "ENH", raider.getEnhTicks(), player, offset);
						}
						break;
					}
				}
			}

			//Handle local player -> does not require party
			if (client.getLocalPlayer() != null && (plugin.getEnhanceTicks() >= 0 || plugin.getOverloadTicks() >= 0))
			{
				boolean offset = false;
				if (plugin.getOverloadTicks() >= 0)
				{
					drawTimer(graphics, "OVL", plugin.getOverloadTicks(), client.getLocalPlayer(), false);
					offset = true;
				}

				if (plugin.getEnhanceTicks() >= 0)
				{
					drawTimer(graphics, "ENH", plugin.getEnhanceTicks(), client.getLocalPlayer(), offset);
				}
			}
		}
		return null;
	}

	public void drawTimer(Graphics2D graphics, String pot, int ticks, Player player, boolean offset)
	{
		int size = config.raidsPotsIconSize();
		graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, (int) (0.75d * size)));

		int xOffset = 5 - (size / 2);
		int yOffset = offset ? -(size + 3) : 0;

		int zOffset = player.getLogicalHeight();
		switch (config.raidsPotsHeight())
		{
			case CENTER:
				zOffset = player.getLogicalHeight() / 2;
				break;
			case FEET:
				zOffset = 0;
				break;
			case HEAD:
				zOffset = player.getLogicalHeight() + 60;
				break;
			case PRAYER:
				zOffset = player.getLogicalHeight() + 250;
		}

		Point base = Perspective.localToCanvas(client, player.getLocalLocation(), client.getPlane(), zOffset);
		BufferedImage image = pot.equals("OVL") ? itemManager.getImage(ItemID.OVERLOAD_4_20996) : itemManager.getImage(ItemID.PRAYER_ENHANCE_4);
		image = ImageUtil.resizeImage(image, size, size);

		if (base != null)
		{
			graphics.drawImage(image, base.getX() + xOffset, base.getY() - yOffset, null);
			yOffset -= size;

			String text = String.valueOf(ticks);
			if (!Strings.isNullOrEmpty(text))
			{
				int delta = image.getWidth() + 3;
				Point textPoint = new Point(base.getX() + xOffset + delta + 1, base.getY() - yOffset);

				drawTextBackground(graphics, textPoint, text, pot);
				OverlayUtil.renderTextLocation(graphics, textPoint, text, ticks < 25 ? Color.RED : Color.WHITE);
			}
		}
	}

	private void drawTextBackground(Graphics2D graphics, Point textLoc, String text, String pot)
	{
		Color color = pot.equals("OVL") ? Color.BLACK : Integer.parseInt(text) < 25 ? Color.BLACK : new Color(132, 67, 186);
		switch (config.overlayFontBackground())
		{
			case OUTLINE:
			{
				OverlayUtil.renderTextLocation(graphics, new Point(textLoc.getX(), textLoc.getY() + 1), text, color);
				OverlayUtil.renderTextLocation(graphics, new Point(textLoc.getX(), textLoc.getY() - 1), text, color);
				OverlayUtil.renderTextLocation(graphics, new Point(textLoc.getX() + 1, textLoc.getY()), text, color);
				OverlayUtil.renderTextLocation(graphics, new Point(textLoc.getX() - 1, textLoc.getY()), text, color);
				break;
			}
			case SHADOW:
			{
				OverlayUtil.renderTextLocation(graphics, new Point(textLoc.getX() + 1, textLoc.getY() + 1), text, color);
				break;
			}
			default:
				break;
		}
	}
}
