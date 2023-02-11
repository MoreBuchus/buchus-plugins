package com.coxadditions;

import java.awt.Color;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.infobox.InfoBox;
import net.runelite.client.ui.overlay.infobox.InfoBoxPriority;

public class GrubsInfobox extends InfoBox
{
	private final CoxAdditionsPlugin plugin;
	private final CoxAdditionsConfig config;

	@Inject
	public GrubsInfobox(Client client, CoxAdditionsPlugin plugin, CoxAdditionsConfig config)
	{
		super(null, plugin);
		this.plugin = plugin;
		this.config = config;
		setPriority(InfoBoxPriority.MED);
	}

	@Override
	public String getText()
	{
		return Integer.toString(plugin.getTotalGrubs());
	}

	@Override
	public Color getTextColor()
	{
		if (plugin.getTotalGrubs() >= config.grubsAmount())
		{
			return new Color(26, 204, 6);
		}

		return Color.WHITE;
	}

	@Override
	public String getTooltip()
	{
		return plugin.getTotalGrubs() + " Cavern Grubs";
	}
}
