/*
 * Copyright (c) 2022, Buchus <http://github.com/MoreBuchus>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.partydefencetracker;

import com.google.inject.Provides;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.IndexDataBase;
import net.runelite.api.NPC;
import net.runelite.api.Skill;
import net.runelite.api.SpritePixels;
import net.runelite.api.Varbits;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GraphicChanged;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.party.PartyService;
import net.runelite.client.party.WSClient;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.party.PartyPlugin;
import net.runelite.client.plugins.specialcounter.SpecialCounterUpdate;
import net.runelite.client.plugins.specialcounter.SpecialWeapon;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.util.ColorUtil;

@PluginDescriptor(
	name = "Party Defence Tracker",
	description = "Calculates the defence based off party specs",
	tags = {"party", "defence", "tracker", "boosting", "special", "counter"}
)
@Slf4j
@PluginDependency(PartyPlugin.class)
public class DefenceTrackerPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private PartyService partyService;

	@Inject
	private WSClient wsClient;

	@Inject
	private DefenceTrackerConfig config;

	@Inject
	private SkillIconManager skillIconManager;

	@Inject
	private InfoBoxManager infoBoxManager;

	private String boss = "";
	private int bossIndex = 0;
	private double bossDef = -1;
	private DefenceInfoBox box = null;
	private VulnerabilityInfoBox vulnBox = null;
	private SpritePixels vuln = null;
	private boolean isInCm = false;
	private final ArrayList<String> bossList = new ArrayList<>(Arrays.asList(
		"Abyssal Sire", "Callisto", "Cerberus", "Chaos Elemental", "Corporeal Beast", "General Graardor", "Giant Mole",
		"Kalphite Queen", "King Black Dragon", "K'ril Tsutsaroth", "Sarachnis", "Venenatis", "Vet'ion", "Vet'ion Reborn",
		"The Maiden of Sugadinti", "Pestilent Bloat", "Nylocas Vasilias", "Sotetseg", "Xarpus",
		"Great Olm (Left claw)", "Tekton", "Tekton (enraged)"));

	private boolean hmXarpus = false;
	private static final int MAIDEN_REGION = 12613;
	private static final int BLOAT_REGION = 13125;
	private static final int NYLOCAS_REGION = 13122;
	private static final int SOTETSEG_REGION = 13123;
	private static final int SOTETSEG_MAZE_REGION = 13379;
	private static final int XARPUS_REGION = 12612;
	private boolean bloatDown = false;

	private QueuedNpc queuedNpc = null;

	@Provides
	DefenceTrackerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(DefenceTrackerConfig.class);
	}

	protected void startUp() throws Exception
	{
		reset();
		wsClient.registerMessage(DefenceTrackerUpdate.class);
	}

	protected void shutDown() throws Exception
	{
		reset();
		wsClient.unregisterMessage(DefenceTrackerUpdate.class);
	}

	protected void reset()
	{
		infoBoxManager.removeInfoBox(box);
		infoBoxManager.removeInfoBox(vulnBox);
		boss = "";
		bossIndex = 0;
		bossDef = -1;
		box = null;
		vulnBox = null;
		vuln = null;
		isInCm = config.cm();
		bloatDown = false;
		queuedNpc = null;
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged e)
	{
		isInCm = config.cm();
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged e)
	{
		if (e.getActor() != null && client.getLocalPlayer() != null && e.getActor().getName() != null)
		{
			int animation = e.getActor().getAnimation();
			if (e.getActor().getName().equals(client.getLocalPlayer().getName()))
			{
				if (animation == 1816 && boss.equalsIgnoreCase("sotetseg") && (isInOverWorld() || isInUnderWorld()))
				{
					infoBoxManager.removeInfoBox(box);
					bossDef = 200;
				}
			}
		}

		if (e.getActor() instanceof NPC && e.getActor().getName() != null && e.getActor().getName().equalsIgnoreCase("pestilent bloat"))
		{
			bloatDown = e.getActor().getAnimation() == 8082;
		}
	}

	@Subscribe
	public void onGameTick(GameTick e)
	{
		if (partyService.isInParty())
		{
			for (NPC n : client.getNpcs())
			{
				if (n != null && n.getName() != null && (n.getName().equals(boss) || (n.getName().contains("Tekton") && boss.equals("Tekton")))
					&& (n.isDead() || n.getHealthRatio() == 0))
				{
					partyService.send(new DefenceTrackerUpdate(n.getName(), n.getIndex(), false, false, client.getWorld()));
				}
			}
		}
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned e)
	{
		NPC npc = e.getNpc();
		if (npc.getName() != null && bossList.contains(npc.getName()))
		{
			hmXarpus = npc.getId() >= 10770 && npc.getId() <= 10772;
		}
	}

	@Subscribe
	public void onActorDeath(ActorDeath e)
	{
		if (e.getActor() instanceof NPC && e.getActor().getName() != null && client.getLocalPlayer() != null && partyService.isInParty())
		{
			if (e.getActor().getName().equals(boss) || (e.getActor().getName().contains("Tekton") && boss.equals("Tekton")))
			{
				partyService.send(new DefenceTrackerUpdate(e.getActor().getName(), ((NPC) e.getActor()).getIndex(), false, false, client.getWorld()));
			}
		}
	}

	@Subscribe
	public void onSpecialCounterUpdate(SpecialCounterUpdate e)
	{
		int hit = e.getHit();
		int world = e.getWorld();
		SpecialWeapon weapon = e.getWeapon();
		int index = e.getNpcIndex();
		NPC npc = client.getCachedNPCs()[index];

		clientThread.invoke(() ->
		{
			if ((npc != null && npc.getName() != null && bossList.contains(npc.getName())) || bossIndex == index)
			{
				if (bossIndex != index)
				{
					String bossName = npc.getName();

					if (!boss.equals(bossName) || (bossName.contains("Tekton") && !boss.equals("Tekton")))
					{
						baseDefence(bossName, index);
						calculateQueue(index);
					}

					if (((boss.equals("Tekton") || boss.contains("Great Olm")) && client.getVarbitValue(Varbits.IN_RAID) != 1)
						|| ((boss.contains("The Maiden of Sugadinti") || boss.contains("Pestilent Bloat") || boss.contains("Nylocas Vasilias")
						|| boss.contains("Sotetseg") || boss.contains("Xarpus")) && client.getVarbitValue(Varbits.THEATRE_OF_BLOOD) != 2)
						|| world != client.getWorld())
					{
						return;
					}
				}
				calculateDefence(weapon, hit);
				updateDefInfobox();
			}
			else
			{
				if (queuedNpc == null || queuedNpc.index != index)
				{
					queuedNpc = new QueuedNpc(index);
				}
				queuedNpc.queuedSpecs.add(new QueuedNpc.QueuedSpec(weapon, hit));
			}
		});
	}

	@Subscribe
	public void onDefenceTrackerUpdate(DefenceTrackerUpdate e)
	{
		int world = e.getWorld();
		clientThread.invoke(() ->
		{
			if (!e.isAlive())
			{
				reset();
			}
			else
			{
				if (!boss.equals(e.getBoss()) || (e.getBoss().contains("Tekton") && !boss.equals("Tekton")))
				{
					baseDefence(e.getBoss(), e.getIndex());
					calculateQueue(e.getIndex());
				}

				if (((boss.equals("Tekton") || boss.contains("Great Olm")) && client.getVarbitValue(Varbits.IN_RAID) != 1)
					|| ((boss.contains("The Maiden of Sugadinti") || boss.contains("Pestilent Bloat") || boss.contains("Nylocas Vasilias")
					|| boss.contains("Sotetseg") || boss.contains("Xarpus")) && client.getVarbitValue(Varbits.THEATRE_OF_BLOOD) != 2)
					|| world != client.getWorld())
				{
					return;
				}

				if (config.vulnerability())
				{
					infoBoxManager.removeInfoBox(vulnBox);
					IndexDataBase sprite = client.getIndexSprites();
					vuln = Objects.requireNonNull(client.getSprites(sprite, 56, 0))[0];
					vulnBox = new VulnerabilityInfoBox(vuln.toBufferedImage(), this);
					vulnBox.setTooltip(ColorUtil.wrapWithColorTag(boss, Color.WHITE));
					infoBoxManager.addInfoBox(vulnBox);
				}
				bossDef -= bossDef * .1;

				updateDefInfobox();
			}
		});
	}

	@Subscribe
	private void onVarbitChanged(VarbitChanged e)
	{
		if ((client.getVarbitValue(Varbits.IN_RAID) != 1 && (boss.equals("Tekton") || boss.equals("Great Olm (Left claw)")))
			|| (boss.equals("The Maiden of Sugadinti") && !isInMaiden()) || (boss.equals("Pestilent Bloat") && !isInBloat())
			|| (boss.equals("Nylocas Vasilias") && !isInNylo()) || (boss.equals("Sotetseg") && !isInOverWorld() && !isInUnderWorld())
			|| (boss.equals("Xarpus") && !isInXarpus()))
		{
			reset();
		}
	}

	@Subscribe
	public void onGraphicChanged(GraphicChanged e)
	{
		//85 = splash
		if (e.getActor() instanceof NPC && e.getActor().getName() != null && e.getActor().getGraphic() == 169 && partyService.isInParty())
		{
			if (bossList.contains(e.getActor().getName()))
			{
				partyService.send(new DefenceTrackerUpdate(e.getActor().getName(), ((NPC) e.getActor()).getIndex(), true, false, client.getWorld()));
			}
		}
	}

	private void baseDefence(String bossName, int index)
	{
		boss = bossName;
		bossIndex = index;
		switch (boss)
		{
			case "Abyssal Sire":
			case "General Graardor":
				bossDef = 250;
				break;
			case "Callisto":
				bossDef = 440;
				break;
			case "Cerberus":
				bossDef = 110;
				break;
			case "Chaos Elemental":
			case "K'ril Tsutsaroth":
				bossDef = 270;
				break;
			case "Corporeal Beast":
				bossDef = 310;
				break;
			case "Giant Mole":
			case "The Maiden of Sugadinti":
			case "Sotetseg":
				bossDef = 200;
				break;
			case "Kalphite Queen":
				bossDef = 300;
				break;
			case "King Black Dragon":
				bossDef = 240;
				break;
			case "Sarachnis":
				bossDef = 150;
				break;
			case "Venenatis":
				bossDef = 490;
				break;
			case "Vet'ion":
			case "Vet'ion Reborn":
				bossDef = 395;
				break;
			case "Pestilent Bloat":
				bossDef = 100;
				break;
			case "Nylocas Vasilias":
				bossDef = 50;
				break;
			case "Xarpus":
				if (hmXarpus)
				{
					bossDef = 200;
				}
				else
				{
					bossDef = 250;
				}
				break;
			case "Great Olm (Left claw)":
				bossDef = 175 * (1 + (.01 * (client.getVarbitValue(5424) - 1)));

				if (isInCm)
				{
					bossDef = bossDef * 1.5;
				}
				break;
			case "Tekton":
			case "Tekton (enraged)":
				boss = "Tekton";
				bossDef = 205 * (1 + (.01 * (client.getVarbitValue(5424) - 1)));

				if (isInCm)
				{
					bossDef = bossDef * 1.2;
				}
				break;
		}
	}

	private void calculateDefence(SpecialWeapon weapon, int hit)
	{
		if (weapon == SpecialWeapon.DRAGON_WARHAMMER)
		{
			if (hit == 0)
			{
				if (client.getVarbitValue(Varbits.IN_RAID) == 1 && boss.equals("Tekton"))
				{
					bossDef -= bossDef * .05;
				}
			}
			else
			{
				bossDef -= bossDef * .30;
			}
		}
		else if (weapon == SpecialWeapon.BANDOS_GODSWORD)
		{
			if (hit == 0)
			{
				if (client.getVarbitValue(Varbits.IN_RAID) == 1 && boss.equals("Tekton"))
				{
					bossDef -= 10;
				}
			}
			else
			{
				if (boss.equals("Corporeal Beast") || (isInBloat() && boss.equals("Pestilent Bloat") && !bloatDown))
				{
					bossDef -= hit * 2;
				}
				else
				{
					bossDef -= hit;
				}
			}
		}
		else if ((weapon == SpecialWeapon.ARCLIGHT || weapon == SpecialWeapon.DARKLIGHT) && hit > 0)
		{
			if (boss.equals("K'ril Tsutsaroth"))
			{
				bossDef -= bossDef * .10;
			}
			else
			{
				bossDef -= bossDef * .05;
			}
		}
		else if (weapon == SpecialWeapon.BARRELCHEST_ANCHOR)
		{
			bossDef -= hit * .10;
		}
		else if (weapon == SpecialWeapon.BONE_DAGGER || weapon == SpecialWeapon.DORGESHUUN_CROSSBOW)
		{
			bossDef -= hit;
		}
	}

	private void calculateQueue(int index)
	{
		if (queuedNpc != null)
		{
			if (queuedNpc.index == index)
			{
				for (QueuedNpc.QueuedSpec spec : queuedNpc.queuedSpecs)
				{
					calculateDefence(spec.weapon, spec.hit);
				}
			}
			queuedNpc = null;
		}
	}

	private void updateDefInfobox()
	{
		if (boss.equals("Sotetseg") && bossDef < 100)
		{
			bossDef = 100;
		}
		else if (bossDef < 0)
		{
			bossDef = 0;
		}
		infoBoxManager.removeInfoBox(box);
		box = new DefenceInfoBox(skillIconManager.getSkillImage(Skill.DEFENCE), this, Math.round(bossDef), config);
		box.setTooltip(ColorUtil.wrapWithColorTag(boss, Color.WHITE));
		infoBoxManager.addInfoBox(box);
	}

	private boolean isInMaiden()
	{
		return client.getMapRegions().length > 0 && client.getMapRegions()[0] == MAIDEN_REGION;
	}

	private boolean isInBloat()
	{
		return client.getMapRegions().length > 0 && client.getMapRegions()[0] == BLOAT_REGION;
	}

	private boolean isInNylo()
	{
		return client.getMapRegions().length > 0 && client.getMapRegions()[0] == NYLOCAS_REGION;
	}

	private boolean isInOverWorld()
	{
		return client.getMapRegions().length > 0 && client.getMapRegions()[0] == SOTETSEG_REGION;
	}

	private boolean isInUnderWorld()
	{
		return client.getMapRegions().length > 0 && client.getMapRegions()[0] == SOTETSEG_MAZE_REGION;
	}

	private boolean isInXarpus()
	{
		return client.getMapRegions().length > 0 && client.getMapRegions()[0] == XARPUS_REGION;
	}
}
