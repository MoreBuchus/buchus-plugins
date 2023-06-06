package com.nexsplits;

import com.nexsplits.config.KillTimerMode;
import com.nexsplits.config.PhaseNameTypeMode;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.infobox.InfoBox;
import net.runelite.client.ui.overlay.infobox.InfoBoxPriority;

import java.awt.*;
import java.awt.image.BufferedImage;

@Getter
public class NexTimerBox extends InfoBox
{
	private final NexSplitsConfig config;
	private final NexSplitsPlugin plugin;
	private final Client client;

	NexTimerBox(BufferedImage image, NexSplitsConfig config, NexSplitsPlugin plugin, Client client)
	{
		super(image, plugin);
		this.config = config;
		this.plugin = plugin;
		this.client = client;
		setPriority(InfoBoxPriority.LOW);
	}

	@Override
	public String getText()
	{
		return plugin.isChickenDead() ? plugin.getTime(plugin.getP5Tick() - plugin.getStartTick())
			: plugin.getTime(client.getTickCount() - plugin.getStartTick());
	}

	@Override
	public Color getTextColor()
	{
		return plugin.isChickenDead() ? Color.GREEN : Color.WHITE;
	}

	@Override
	public String getTooltip()
	{
		return plugin.getPhaseTimes();
	}

	@Override
	public boolean render()
	{
		return (config.killTimer() == KillTimerMode.INFOBOX || config.killTimer() == KillTimerMode.BOTH) && plugin.inNexRegion();
	}
}
