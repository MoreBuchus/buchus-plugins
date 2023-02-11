package com.coxadditions.overlay;

import com.coxadditions.CoxAdditionsConfig;
import com.coxadditions.CoxAdditionsPlugin;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.components.LineComponent;

public class InstanceTimerOverlay extends OverlayPanel
{
	private final Client client;
	private final CoxAdditionsPlugin plugin;
	private final CoxAdditionsConfig config;

	@Inject
	public InstanceTimerOverlay(Client client, CoxAdditionsPlugin plugin, CoxAdditionsConfig config)
	{
		super(plugin);
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
	}

	public Dimension render(Graphics2D graphics)
	{
		if (plugin.getPanelFont() == null)
		{
			plugin.loadFont(false);
		}

		panelComponent.getChildren().clear();
		if (config.instanceTimer() == CoxAdditionsConfig.instanceTimerMode.INFOBOX && plugin.isInstanceTimerRunning() && plugin.isInRaid())
		{
			graphics.setFont(plugin.getPanelFont());
			panelComponent.setPreferredSize(new Dimension(graphics.getFontMetrics().stringWidth("Tick:   ") + 15, 0));
			panelComponent.getChildren().add(LineComponent.builder()
				.left("Tick: ")
				.right(String.valueOf(plugin.getInstanceTimer()))
				.build());
		}
		return super.render(graphics);
	}
}

