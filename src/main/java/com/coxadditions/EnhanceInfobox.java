package com.coxadditions;

import net.runelite.api.Client;
import net.runelite.client.ui.overlay.infobox.InfoBox;
import net.runelite.client.ui.overlay.infobox.InfoBoxPriority;

import javax.inject.Inject;
import java.awt.*;

public class EnhanceInfobox extends InfoBox
{
	private final CoxAdditionsPlugin plugin;
	private final CoxAdditionsConfig config;

	@Inject
	public EnhanceInfobox(Client client, CoxAdditionsPlugin plugin, CoxAdditionsConfig config)
	{
		super(null, plugin);
		this.plugin = plugin;
		this.config = config;
		setPriority(InfoBoxPriority.MED);
	}

	@Override
	public String getText()
	{
		String enhanceTime = "";
		if (config.detailedPrayerEnhance() != CoxAdditionsConfig.enhanceMode.OFF)
		{
			if (config.detailedPrayerEnhance() == CoxAdditionsConfig.enhanceMode.REGEN_CYCLE)
			{
				enhanceTime = String.valueOf(plugin.getEnhanceTicks() % 6 + 1);
			}
			else
			{
				enhanceTime = String.valueOf(plugin.getEnhanceTicks());
			}
		}

		return enhanceTime;
	}

	@Override
	public Color getTextColor()
	{
		if (plugin.getEnhanceTicks() % 6 == 5)
		{
			return new Color(26, 204, 6);
		}

		return Color.WHITE;
	}

	@Override
	public String getTooltip()
	{
		return "Prayer Enhance";
	}
}
