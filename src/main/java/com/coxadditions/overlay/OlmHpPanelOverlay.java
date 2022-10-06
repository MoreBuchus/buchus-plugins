package com.coxadditions.overlay;

import com.coxadditions.CoxAdditionsConfig;
import com.coxadditions.CoxAdditionsPlugin;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Varbits;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

public class OlmHpPanelOverlay extends OverlayPanel
{
	private final Client client;

	private final CoxAdditionsPlugin plugin;

	private final CoxAdditionsConfig config;

	@Inject
	private OlmHpPanelOverlay(Client client, CoxAdditionsPlugin plugin, CoxAdditionsConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
	}

	public Dimension render(Graphics2D graphics)
	{
		if (plugin.getPanelFont() == null)
		{
			plugin.loadFont(false);
		}

		if (config.olmHandsHealth() == CoxAdditionsConfig.olmHandsHealthMode.INFOBOX && (plugin.getMageHand() != null || plugin.getMeleeHand() != null))
		{
			graphics.setFont(plugin.getPanelFont());
			panelComponent.getChildren().clear();
			NPC mageHand = plugin.getMageHand();
			NPC meleeHand = plugin.getMeleeHand();
			panelComponent.getChildren().add(TitleComponent.builder()
				.color(Color.PINK)
				.text("Olm Hands HP")
				.build());

			//Solo Hand HP
			if (client.getVarbitValue(Varbits.RAID_PARTY_SIZE) == 1)
			{
				if (mageHand != null && plugin.getMageHandHp() >= 0)
				{
					String mageText = String.valueOf(plugin.getMageHandHp());
					Color mageColor = Color.WHITE;
					if (plugin.getMageHandHp() < 100)
					{
						mageColor = Color.RED;
					}
					panelComponent.getChildren().add(LineComponent.builder()
						.leftColor(Color.CYAN)
						.left("Mage Hand:")
						.rightColor(mageColor)
						.right(mageText)
						.build());
				}

				if (meleeHand != null && plugin.getMeleeHandHp() >= 0)
				{
					String meleeText = String.valueOf(plugin.getMeleeHandHp());
					Color meleeColor = Color.WHITE;
					if (plugin.getMeleeHandHp() < 100)
					{
						meleeColor = Color.RED;
					}
					panelComponent.getChildren().add(LineComponent.builder()
						.leftColor(Color.RED)
						.left("Melee Hand:")
						.rightColor(meleeColor)
						.right(meleeText)
						.build());
				}
			}
			else
			{
				//Team Hands HP
				if (mageHand != null)
				{
					Color mageColor = Color.WHITE;
					String mageText = "";
					if (mageHand.getHealthRatio() > 0 || (plugin.getMageHandLastRatio() != 0 && plugin.getMageHandLastHealthScale() != 0))
					{
						if (mageHand.getHealthRatio() > 0)
						{
							plugin.setMageHandLastRatio(mageHand.getHealthRatio());
							plugin.setMageHandLastHealthScale(mageHand.getHealthScale());
						}

						float floatRatioMage = ((float) plugin.getMageHandLastRatio() / (float) plugin.getMageHandLastHealthScale() * 100);
						if (floatRatioMage <= 20)
						{
							mageColor = Color.RED;
						}
						mageText = Float.toString(floatRatioMage);
						mageText = mageText.substring(0, mageText.indexOf("."));
					}
					panelComponent.getChildren().add(LineComponent.builder()
						.leftColor(Color.CYAN)
						.left("Mage Hand:")
						.rightColor(mageColor)
						.right(mageText + "%")
						.build());
				}
				if (meleeHand != null)
				{
					Color meleeColor = Color.WHITE;
					String meleeText = "";
					if (meleeHand.getHealthRatio() > 0 || (plugin.getMeleeHandLastRatio() != 0 && plugin.getMeleeHandLastHealthScale() != 0))
					{
						if (plugin.getMeleeHand().getHealthRatio() > 0)
						{
							plugin.setMeleeHandLastRatio(meleeHand.getHealthRatio());
							plugin.setMeleeHandLastHealthScale(meleeHand.getHealthScale());
						}

						float floatRatioMelee = ((float) plugin.getMeleeHandLastRatio() / (float) plugin.getMeleeHandLastHealthScale() * 100);
						if (floatRatioMelee <= 20)
						{
							meleeColor = Color.RED;
						}
						meleeText = Float.toString(floatRatioMelee);
						meleeText = meleeText.substring(0, meleeText.indexOf("."));
					}
					panelComponent.getChildren().add(LineComponent.builder()
						.leftColor(Color.RED)
						.left("Melee Hand:")
						.rightColor(meleeColor)
						.right(meleeText + "%")
						.build());
				}
			}
		}
		return super.render(graphics);
	}
}
