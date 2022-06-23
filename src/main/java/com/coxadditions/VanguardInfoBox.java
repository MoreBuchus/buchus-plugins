package com.coxadditions;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;

import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

public class VanguardInfoBox extends Overlay
{
	private final CoxAdditionsPlugin plugin;
	private final CoxAdditionsConfig config;
	private final PanelComponent panelComponent = new PanelComponent();

	@Inject
	VanguardInfoBox(Client client, CoxAdditionsPlugin plugin, CoxAdditionsConfig config)
	{
		super(plugin);
		setPosition(OverlayPosition.TOP_LEFT);
		setPriority(OverlayPriority.HIGH);
		this.plugin = plugin;
		this.config = config;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!plugin.isInVangs())
		{
			return null;
		}
		panelComponent.getChildren().clear();

		if (config.vangInfobox())
		{
			panelComponent.getChildren().add(TitleComponent.builder()
				.text("Vanguards")
				.color(Color.pink)
				.build());
			panelComponent.getChildren().add(LineComponent.builder()
				.left("Range")
				.right(Integer.toString(plugin.getRangeHP()))
				.leftColor(Color.green)
				.build());
			panelComponent.getChildren().add(LineComponent.builder()
				.left("Mage")
				.right(Integer.toString(plugin.getMageHP()))
				.leftColor(Color.blue)
				.build());
			panelComponent.getChildren().add(LineComponent.builder()
				.left("Melee")
				.right(Integer.toString(plugin.getMeleeHP()))
				.leftColor(Color.red)
				.build());
			return panelComponent.render(graphics);
		}
		return null;
	}
}
