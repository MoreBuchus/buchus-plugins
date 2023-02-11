package com.coxadditions.overlay;

import com.coxadditions.CoxAdditionsConfig;
import com.coxadditions.CoxAdditionsPlugin;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.InstanceTemplates;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;

public class VangPotsOverlay extends OverlayPanel
{
	private final Client client;

	private final CoxAdditionsConfig config;

	private final CoxAdditionsPlugin plugin;

	@Inject
	private VangPotsOverlay(Client client, CoxAdditionsPlugin plugin, CoxAdditionsConfig config)
	{
		super(plugin);
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.TOP_LEFT);
		setPriority(OverlayPriority.MED);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	public Dimension render(Graphics2D graphics)
	{
		if (plugin.getPanelFont() == null)
		{
			plugin.loadFont(false);
		}

		if (config.showPanel() && plugin.getOverloadsDropped() > 0 && plugin.isInRaid() && client.getLocalPlayer() != null)
		{
			boolean overlayRooms = (plugin.room() == InstanceTemplates.RAIDS_VANGUARDS || plugin.isInPrep());
			if (plugin.room() != null && overlayRooms)
			{
				graphics.setFont(plugin.getPanelFont());
				panelComponent.getChildren().clear();
				panelComponent.setPreferredSize(new Dimension(graphics.getFontMetrics().stringWidth("Overloads: ") + 20, 0));
				panelComponent.getChildren().add(LineComponent.builder()
					.leftColor(Color.WHITE)
					.left("Overloads: ")
					.right(String.valueOf(plugin.getOverloadsDropped()))
					.build());
			}
		}
		return super.render(graphics);
	}
}
