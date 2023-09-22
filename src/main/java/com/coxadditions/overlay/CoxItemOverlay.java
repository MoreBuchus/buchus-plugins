package com.coxadditions.overlay;

import com.coxadditions.CoxAdditionsConfig;
import com.coxadditions.CoxAdditionsPlugin;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.inject.Inject;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import net.runelite.client.util.Text;
import net.runelite.client.util.WildcardMatcher;

public class CoxItemOverlay extends WidgetItemOverlay
{
	private final ItemManager itemManager;

	private final CoxAdditionsPlugin plugin;

	private final CoxAdditionsConfig config;

	@Inject
	public CoxItemOverlay(ItemManager itemManager, CoxAdditionsPlugin plugin, CoxAdditionsConfig config)
	{
		this.itemManager = itemManager;
		this.plugin = plugin;
		this.config = config;
		showOnInterfaces(271, 551);
	}

	public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem itemWidget)
	{
		if (plugin.isInRaid() && config.highlightChest() != CoxAdditionsConfig.HighlightChestMode.OFF
			&& ((!config.highlightChestItems().equals("") && plugin.getChestHighlightIdList().size() > 0)
			|| (!config.highlightChestItems2().equals("") && plugin.getChestHighlightIdList2().size() > 0)))
		{
			if (plugin.getChestHighlightIdList().contains(String.valueOf(itemId)) || itemNameInList(plugin.getChestHighlightIdList(), itemWidget))
			{
				if (config.highlightChest() == CoxAdditionsConfig.HighlightChestMode.UNDERLINE)
				{
					underlineItem(graphics, itemWidget, config.highlightChestItemsColor());
				}
				else if (config.highlightChest() == CoxAdditionsConfig.HighlightChestMode.OUTLINE)
				{
					highlightItem(graphics, itemId, itemWidget, config.highlightChestItemsColor());
				}
			}
			else if (plugin.getChestHighlightIdList2().contains(String.valueOf(itemId)) || itemNameInList(plugin.getChestHighlightIdList2(), itemWidget))
			{
				if (config.highlightChest() == CoxAdditionsConfig.HighlightChestMode.UNDERLINE)
				{
					underlineItem(graphics, itemWidget, config.highlightChestItemsColor2());
				}
				else if (config.highlightChest() == CoxAdditionsConfig.HighlightChestMode.OUTLINE)
				{
					highlightItem(graphics, itemId, itemWidget, config.highlightChestItemsColor2());
				}
			}
		}
	}

	private void highlightItem(Graphics2D graphics, int itemId, WidgetItem itemWidget, Color color)
	{
		Rectangle bounds = itemWidget.getCanvasBounds();
		BufferedImage outline = itemManager.getItemOutline(itemId, itemWidget.getQuantity(), color);
		graphics.drawImage(outline, (int) bounds.getX(), (int) bounds.getY(), null);
	}

	private void underlineItem(Graphics2D graphics, WidgetItem itemWidget, Color color)
	{
		Rectangle bounds = itemWidget.getCanvasBounds();
		int heightOffSet = (int) bounds.getY() + (int) bounds.getHeight() + 2;
		graphics.setColor(color);
		graphics.drawLine((int) bounds.getX(), heightOffSet, (int) bounds.getX() + (int) bounds.getWidth(), heightOffSet);
	}

	private boolean itemNameInList(ArrayList<String> list, WidgetItem item)
	{
		String itemName = Text.removeTags(item.getWidget().getName().replace('\u00A0', ' ').trim());
		return list.stream().anyMatch(i -> WildcardMatcher.matches(i, itemName) || i.equalsIgnoreCase(itemName));
	}
}
