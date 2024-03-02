package com.partydefencetracker;

import java.awt.Color;
import java.awt.image.BufferedImage;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.overlay.infobox.InfoBox;
import javax.inject.Inject;

@ToString
public class DefenceInfoBox extends InfoBox
{
    @Inject
    private final DefenceTrackerConfig config;

    @Getter
    @Setter
    private long count;

    public DefenceInfoBox(BufferedImage image, Plugin plugin, long count, DefenceTrackerConfig config)
    {
        super(image, plugin);
        this.count = count;
        this.config = config;
    }

    @Override
    public String getText()
    {
        return Long.toString(getCount());
    }

    @Override
    public Color getTextColor()
    {
		if (config.disableIBColor())
		{
			return Color.WHITE;
		}
		else
		{
			if (count == 0)
			{
				return config.cappedDefColor();
			}
			else if (count >= 1 && count <= config.lowDef())
			{
				return config.lowDefColor();
			}
			else
			{
				return config.highDefColor();
			}
		}
    }
}