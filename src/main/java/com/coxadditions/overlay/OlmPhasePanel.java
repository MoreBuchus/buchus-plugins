package com.coxadditions.overlay;

import com.coxadditions.CoxAdditionsConfig;
import com.coxadditions.CoxAdditionsPlugin;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Varbits;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.components.TitleComponent;

public class OlmPhasePanel extends OverlayPanel
{
	private final Client client;
	private final CoxAdditionsPlugin plugin;
	private final CoxAdditionsConfig config;

	@Inject
	public OlmPhasePanel(Client client, CoxAdditionsPlugin plugin, CoxAdditionsConfig config)
	{
		super(plugin);
		this.plugin = plugin;
		this.config = config;
		this.client = client;
	}

	public Dimension render(Graphics2D graphics)
	{
		if (plugin.getPanelFont() == null)
		{
			plugin.loadFont(false);
		}

		panelComponent.getChildren().clear();
		if (config.olmPhasePanel() && this.client.getVarbitValue(Varbits.IN_RAID) == 1 && !plugin.getOlmPhase().equals(""))
		{
			graphics.setFont(plugin.getPanelFont());
			Color color = plugin.getOlmPhase().equals("Acid") ? Color.GREEN : plugin.getOlmPhase().equals("Flame") ? Color.RED : Color.MAGENTA;
			panelComponent.setPreferredSize(new Dimension(graphics.getFontMetrics().stringWidth(plugin.getOlmPhase()) + 3, 0));
			panelComponent.getChildren().add(TitleComponent.builder()
				.color(color)
				.text(plugin.getOlmPhase())
				.build());
		}
		return super.render(graphics);
	}
}
