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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.IndexDataBase;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.SpritePixels;
import net.runelite.api.Varbits;
import net.runelite.api.coords.WorldPoint;
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

	private boolean hmXarpus = false;
	private boolean bloatDown = false;

	private QueuedNpc queuedNpc = null;

	Map<String, ArrayList<Integer>> bossRegions = new HashMap<String, ArrayList<Integer>>()
	{{
		put("The Maiden of Sugadinti", new ArrayList<>(Collections.singletonList(12613)));
		put("Pestilent Bloat", new ArrayList<>(Collections.singletonList(13125)));
		put("Nylocas Vasilias", new ArrayList<>(Collections.singletonList(13122)));
		put("Sotetseg", new ArrayList<>(Arrays.asList(13123, 13379)));
		put("Xarpus", new ArrayList<>(Collections.singletonList(12612)));
		put("Zebak", new ArrayList<>(Collections.singletonList(15700)));
		put("Kephri", new ArrayList<>(Collections.singletonList(14164)));
		put("Ba-Ba", new ArrayList<>(Collections.singletonList(15188)));
		put("<col=00ffff>Obelisk</col>", new ArrayList<>(Collections.singletonList(15184)));
		put("Tumeken's Warden", new ArrayList<>(Collections.singletonList(15696)));
		put("Elidinis' Warden", new ArrayList<>(Collections.singletonList(15696)));
	}};

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
		int animation = e.getActor().getAnimation();

		if (e.getActor() instanceof Player && e.getActor() != null && client.getLocalPlayer() != null && e.getActor().getName() != null)
		{
			if (e.getActor().getName().equals(client.getLocalPlayer().getName()))
			{
				if (animation == 1816 && boss.equalsIgnoreCase("sotetseg") && inBossRegion())
				{
					infoBoxManager.removeInfoBox(box);
					bossDef = 200;
				}
			}
		}

		if (e.getActor() instanceof NPC && e.getActor().getName() != null)
		{
			if (e.getActor().getName().equalsIgnoreCase("pestilent bloat"))
			{
				bloatDown = animation == 8082;
			}
			//Enraged Wardens
			else if (animation == 9685 && (boss.equalsIgnoreCase("Tumeken's Warden") || boss.equalsIgnoreCase("Elidinis' Warden")))
			{
				infoBoxManager.removeInfoBox(box);
				bossDef = 60;
			}
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
		if (npc.getName() != null && BossInfo.getBoss(npc.getName()) != null)
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
			if ((npc != null && npc.getName() != null && BossInfo.getBoss(npc.getName()) != null) || bossIndex == index)
			{
				if (bossIndex != index)
				{
					String bossName = npc.getName();

					if (!boss.equals(bossName) || (bossName.contains("Tekton") && !boss.equals("Tekton")))
					{
						baseDefence(bossName, index);
						calculateQueue(index);
					}
				}

				if (inBossRegion() && world == client.getWorld())
				{
					calculateDefence(weapon, hit);
					updateDefInfobox();
				}
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

				if (inBossRegion() && world == client.getWorld())
				{
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
			}
		});
	}

	@Subscribe
	private void onVarbitChanged(VarbitChanged e)
	{
		if (!inBossRegion())
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
			if (BossInfo.getBoss(e.getActor().getName()) != null)
			{
				partyService.send(new DefenceTrackerUpdate(e.getActor().getName(), ((NPC) e.getActor()).getIndex(), true, false, client.getWorld()));
			}
		}
	}

	private void baseDefence(String bossName, int index)
	{
		boss = bossName;
		bossIndex = index;
		bossDef = BossInfo.getBaseDefence(boss);

		if (boss.equals("Xarpus") && hmXarpus)
		{
			bossDef = 200;
		}
		else if (boss.equals("Great Olm (Left claw)") || boss.contains("Tekton"))
		{
			bossDef = bossDef * (1 + (.01 * (client.getVarbitValue(Varbits.RAID_PARTY_SIZE) - 1)));
			if (isInCm)
			{
				bossDef = bossDef * (boss.contains("Tekton") ? 1.2 : 1.5);
			}
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
				if (boss.equals("Corporeal Beast") || (inBossRegion() && boss.equals("Pestilent Bloat") && !bloatDown))
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
			if (boss.equals("K'ril Tsutsaroth") || boss.equals("Abyssal Sire"))
			{
				bossDef -= BossInfo.getBaseDefence(boss) * .10;
			}
			else
			{
				bossDef -= BossInfo.getBaseDefence(boss) * .05;
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

		if (boss.equals("Sotetseg") && bossDef < 100)
		{
			bossDef = 100;
		}
		else if (bossDef < 0)
		{
			bossDef = 0;
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
		infoBoxManager.removeInfoBox(box);
		box = new DefenceInfoBox(skillIconManager.getSkillImage(Skill.DEFENCE), this, Math.round(bossDef), config);
		box.setTooltip(ColorUtil.wrapWithColorTag(boss, Color.WHITE));
		infoBoxManager.addInfoBox(box);
	}

	private boolean inBossRegion()
	{
		if (client.getLocalPlayer() != null && bossRegions.containsKey(boss))
		{
			WorldPoint wp = WorldPoint.fromLocalInstance(client, client.getLocalPlayer().getLocalLocation());
			if (wp != null)
			{
				return bossRegions.get(boss).contains(wp.getRegionID());
			}
		}
		return client.getVarbitValue(Varbits.IN_RAID) == 1 || (!boss.equals("Tekton") && !boss.equals("Great Olm (Left claw)"));
	}
}
