package com.partydefencetracker;

import net.runelite.client.ui.overlay.infobox.InfoBox;
import java.awt.*;
import java.awt.image.BufferedImage;

public class RedKerisInfoBox extends InfoBox
{
	private DefenceTrackerPlugin plugin;

	RedKerisInfoBox(BufferedImage image, DefenceTrackerPlugin plugin)
	{
		super(image, plugin);
		this.plugin = plugin;
	}

	@Override
	public String getText()
	{
		return String.valueOf(plugin.getRedKerisTicks());
	}

	@Override
	public Color getTextColor()
	{
		return plugin.getRedKerisTicks() <= 3 ? Color.RED : Color.WHITE;
	}
}
