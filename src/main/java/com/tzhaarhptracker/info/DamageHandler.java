/*
 * Copyright (c) 2022, l2- <https://github.com/l2->
 * Copyright (c) 2021, InfernoStats <https://github.com/InfernoStats>
 * Copyright (c) 2023, Buchus <http://github.com/MoreBuchus>
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
package com.tzhaarhptracker.info;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.HitsplatID;
import net.runelite.api.MenuAction;
import net.runelite.api.NPC;
import net.runelite.api.Skill;
import net.runelite.api.VarPlayer;
import net.runelite.api.Varbits;
import net.runelite.api.events.FakeXpDrop;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.kit.KitType;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.NpcUtil;
import net.runelite.client.game.SkillIconManager;
import com.tzhaarhptracker.TzhaarHPTrackerConfig;
import com.tzhaarhptracker.TzhaarHPTrackerPlugin;
import com.tzhaarhptracker.attackstyles.WeaponMap;
import com.tzhaarhptracker.InfoHandler;
import com.tzhaarhptracker.TzhaarNPC;
import com.tzhaarhptracker.attackstyles.AttackStyle;
import com.tzhaarhptracker.attackstyles.WeaponStyle;
import com.tzhaarhptracker.attackstyles.WeaponType;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;
import org.apache.commons.lang3.ObjectUtils;
import static net.runelite.api.NpcID.*;

@Slf4j
public class DamageHandler extends InfoHandler
{
	@Inject
	private Client client;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private ClientThread clientThread;
	@Inject
	private NpcUtil npcUtil;
	@Inject
	private SkillIconManager skillIconManager;

	//XP drops
	@Getter
	private static final int[] previous_exp = new int[Skill.values().length];
	@Getter
	private final Map<Skill, Integer> fakeXpMap = new EnumMap<>(Skill.class);

	@Getter
	private Actor lastOpponent;
	@Getter
	private int lastOpponentID = -1;

	private int attackStyleVarbit = -1;
	private int equippedWeaponTypeVarbit = -1;
	private int castingModeVarbit = -1;

	@Getter
	private AttackStyle attackStyle;
	@Getter
	private WeaponStyle weaponStyle;
	private boolean skipTickCheck = false;

	@Getter
	private boolean processedThisTick = false;
	@Getter
	private boolean aoeSpellQueued = false;

	//444 -> healing graphic without purple hitsplats
	private static final int HEALING_GRAPHIC = 444;

	private static final int BARRAGE = 1979;

	@Inject
	protected DamageHandler(TzhaarHPTrackerPlugin plugin, TzhaarHPTrackerConfig config)
	{
		super(plugin, config);
	}

	public void load()
	{
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			clientThread.invokeLater(() ->
			{
				int[] xps = client.getSkillExperiences();
				System.arraycopy(xps, 0, previous_exp, 0, previous_exp.length);

				initAttackStyles();
			});
		}
		else
		{
			Arrays.fill(previous_exp, 0);
		}
	}

	public void unload()
	{

	}

	private void initAttackStyles()
	{
		attackStyleVarbit = client.getVarpValue(VarPlayer.ATTACK_STYLE);
		equippedWeaponTypeVarbit = client.getVarbitValue(Varbits.EQUIPPED_WEAPON_TYPE);
		castingModeVarbit = client.getVarbitValue(Varbits.DEFENSIVE_CASTING_MODE);
		updateAttackStyle(equippedWeaponTypeVarbit, attackStyleVarbit, castingModeVarbit);
	}

	private void updateAttackStyle(int equippedWeaponType, int attackStyleIndex, int castingMode)
	{
		AttackStyle[] attackStyles = WeaponType.getWeaponType(equippedWeaponType).getAttackStyles();
		if (attackStyleIndex < attackStyles.length)
		{
			attackStyle = attackStyles[attackStyleIndex];
			if (attackStyle == null)
			{
				attackStyle = AttackStyle.OTHER;
			}
			else if ((attackStyle == AttackStyle.CASTING) && (castingMode == 1))
			{
				attackStyle = AttackStyle.DEFENSIVE_CASTING;
			}
		}
	}

	@Subscribe
	protected void onGameStateChanged(GameStateChanged e)
	{
		if (e.getGameState() == GameState.LOGIN_SCREEN || e.getGameState() == GameState.HOPPING)
		{
			Arrays.fill(previous_exp, 0);
		}
	}

	@Subscribe
	private void onVarbitChanged(VarbitChanged e)
	{
		if (plugin.isInAllowedCaves())
		{
			int currentAttackStyleVarbit = client.getVarpValue(VarPlayer.ATTACK_STYLE);
			int currentEquippedWeaponTypeVarbit = client.getVarbitValue(Varbits.EQUIPPED_WEAPON_TYPE);
			int currentCastingModeVarbit = client.getVarbitValue(Varbits.DEFENSIVE_CASTING_MODE);

			if (attackStyleVarbit != currentAttackStyleVarbit || equippedWeaponTypeVarbit != currentEquippedWeaponTypeVarbit || castingModeVarbit != currentCastingModeVarbit)
			{
				attackStyleVarbit = currentAttackStyleVarbit;
				equippedWeaponTypeVarbit = currentEquippedWeaponTypeVarbit;
				castingModeVarbit = currentCastingModeVarbit;

				updateAttackStyle(equippedWeaponTypeVarbit, attackStyleVarbit, castingModeVarbit);
			}
		}
	}

	@Subscribe
	private void onHitsplatApplied(HitsplatApplied e)
	{
		if (plugin.isInAllowedCaves())
		{
			Actor actor = e.getActor();
			int hitsplat = e.getHitsplat().getAmount();

			if (actor != null && actor.getName() != null && actor instanceof NPC)
			{
				NPC splatNpc = (NPC) actor;

				for (TzhaarNPC n : plugin.getNpcs())
				{
					if (n.getNpc().equals(splatNpc))
					{
						if (!Objects.equals(n.getNpc().getName(), "TzKal-Zuk") || !zukWidgetActive())
						{
							if (e.getHitsplat().getHitsplatType() == HitsplatID.HEAL)
							{
								n.addHp(hitsplat);
							}
							else
							{
								if (hitsplat != 0)
								{
									if (!n.isHealed())
									{
										n.removeHp(hitsplat);
										//Only set to dead if it has not been set to dead yet
										if (!n.isDead())
										{
											//n.setDead(n.getHp() <= 0);
											handleDead(n, n.getHp() <= 0);
										}
									}

									//Set the death tick on hitsplat when it is predicted to die
									if (n.isDead())
									{
										n.setDeathTick(client.getTickCount());
									}

									//If the npc is not set to dead, but the death tick was tracked -> reset to 0
									if (!n.isDead() && n.getDeathTick() > 0)
									{
										n.setDeathTick(0);
									}

									n.setQueuedDamage(Math.max(0, n.getQueuedDamage() - hitsplat));
								}
							}
						}
						//Track death tick for Zuk if HP widget is active
						else
						{
							//Set the death tick on hitsplat when it is predicted to die
							if (n.isDead())
							{
								n.setDeathTick(client.getTickCount());
							}

							//If the npc is not set to dead, but the death tick was tracked -> reset to 0
							if (!n.isDead() && n.getDeathTick() > 0)
							{
								n.setDeathTick(0);
							}
						}
					}
				}
			}
		}
	}

	@Subscribe
	private void onGameTick(GameTick e)
	{
		if (plugin.isInAllowedCaves())
		{
			processedThisTick = false;

			// Group FakeXP drops and process them every game tick
			for (Map.Entry<Skill, Integer> xp : fakeXpMap.entrySet())
			{
				int hit;
				Actor interacted = Objects.requireNonNull(client.getLocalPlayer()).getInteracting();
				if (interacted instanceof NPC && lastOpponent == null)
				{
					lastOpponent = interacted;
				}

				if (lastOpponent != null)
				{
					switch (xp.getKey())
					{
						case ATTACK:
						case STRENGTH:
						case DEFENCE:
						case RANGED:
							//Long range should be calculated with range only
							hit = calculateHitOnNpc(lastOpponentID, attackStyle == AttackStyle.LONGRANGE ? Skill.RANGED : xp.getKey(), xp.getValue(), attackStyle, weaponStyle);
							processHit(hit, xp.getKey(), attackStyle, weaponStyle, (NPC) lastOpponent);
							break;
						case HITPOINTS:
							if (attackStyle == AttackStyle.CASTING)
							{
								//Only calculate magic damage using hitpoints if it's not defensive casting
								hit = calculateHitOnNpc(lastOpponentID, xp.getKey(), xp.getValue(), attackStyle, weaponStyle);
								processHit(hit, xp.getKey(), attackStyle, weaponStyle, (NPC) lastOpponent);
							}
							break;
					}
				}
			}
			fakeXpMap.clear();

			//Handle HP recalculating and regen
			if (!plugin.getNpcs().isEmpty() && client.getLocalPlayer() != null)
			{
				for (TzhaarNPC n : plugin.getNpcs())
				{
					int currentTick = client.getTickCount();
					int spawnTick = n.getSpawnTick();
					if (currentTick - spawnTick >= 100 && n.getNpc().getId() != ROCKY_SUPPORT)
					{
						if (n.getHp() != n.getMaxHp())
						{
							n.addHp(1);
							n.setSpawnTick(currentTick);
						}
					}

					//Recalculate HP 2 ticks after 1st hitsplat after being set to dead if still alive
					if (n.isDead() && n.getDeathTick() != 0 && client.getTickCount() >= n.getDeathTick() + 2 && !npcUtil.isDying(n.getNpc()))
					{
						n.setDead(false);
						recalcHP(n, n.getNpc().getHealthRatio(), n.getNpc().getHealthScale());
					}
					else
					{
						if (n.getHp() > 0 && npcUtil.isDying(n.getNpc()))
						{
							n.setHp(0);
							handleDead(n, true);
						}
					}

					//Healing graphic without healing hitsplats
					if (n.getNpc().hasSpotAnim(HEALING_GRAPHIC))
					{
						n.setHealed(true);
					}

					//Recalculate HP for NPCs with healing graphic
					//Zuk HP handled by widget
					if (n.isHealed() && !Objects.equals(n.getNpc().getName(), "TzKal-Zuk"))
					{
						recalcHP(n, n.getNpc().getHealthRatio(), n.getNpc().getHealthScale());
					}

					if (Objects.equals(n.getNpc().getName(), "TzKal-Zuk") && zukWidgetActive() && plugin.getCurrentWave().containsKey("inferno")
						&& plugin.getCurrentWave().get("inferno") == 69)
					{
						n.setHp(getZukHPfromWidget());
					}
				}
			}

			if (skipTickCheck)
			{
				skipTickCheck = false;
			}
			else
			{
				if (client.getLocalPlayer() != null && client.getLocalPlayer().getPlayerComposition() != null)
				{
					int equippedWeapon = ObjectUtils.defaultIfNull(client.getLocalPlayer().getPlayerComposition().getEquipmentId(KitType.WEAPON), -1);
					weaponStyle = WeaponMap.StyleMap.get(equippedWeapon);
				}
			}
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked e)
	{
		if (plugin.isInAllowedCaves())
		{
			String target = Text.removeTags(e.getMenuTarget());
			String option = Text.removeTags(e.getMenuOption());

			if (option.equalsIgnoreCase("wield"))
			{
				WeaponStyle newStyle = WeaponMap.StyleMap.get(e.getItemId());
				if (newStyle != null)
				{
					skipTickCheck = true;
					weaponStyle = newStyle;
				}
			}

			switch (e.getMenuAction())
			{
				//Any menu click that targets an NPC
				case WIDGET_TARGET_ON_NPC:
				case NPC_FIRST_OPTION:
				case NPC_SECOND_OPTION:
				case NPC_THIRD_OPTION:
				case NPC_FOURTH_OPTION:
				case NPC_FIFTH_OPTION:
					lastOpponent = e.getMenuEntry().getNpc();
					if (e.getMenuAction() == MenuAction.WIDGET_TARGET_ON_NPC && WidgetInfo.TO_GROUP(client.getSelectedWidget().getId()) == WidgetID.SPELLBOOK_GROUP_ID)
					{
						attackStyle = (client.getVarbitValue(Varbits.DEFENSIVE_CASTING_MODE) == 1 || attackStyle == AttackStyle.DEFENSIVE_CASTING) ?
							AttackStyle.DEFENSIVE_CASTING : AttackStyle.CASTING;

						String[] aoeSpells = {
							"ice barrage", "ice burst", "blood barrage", "blood burst",
							"smoke barrage", "smoke burst", "shadow barrage", "shadow burst"
						};
						for (String spell : aoeSpells)
						{
							if (target.toLowerCase().startsWith(spell + " ->") && e.getMenuEntry().getNpc() != null && e.getMenuEntry().getNpc().getName() != null
								&& (TzhaarHPTrackerPlugin.getINFERNO_NPC().contains(e.getMenuEntry().getNpc().getName().toLowerCase())
								|| TzhaarHPTrackerPlugin.getFIGHT_CAVE_NPC().contains(e.getMenuEntry().getNpc().getName().toLowerCase())))
							{
								aoeSpellQueued = true;
							}
						}
					}
					else
					{
						initAttackStyles();
						aoeSpellQueued = false;
					}
					break;
				//Any menu click which clears an interaction
				case WALK:
				case WIDGET_TARGET_ON_WIDGET:
				case WIDGET_TARGET_ON_GROUND_ITEM:
				case WIDGET_TARGET_ON_PLAYER:
				case GROUND_ITEM_FIRST_OPTION:
				case GROUND_ITEM_SECOND_OPTION:
				case GROUND_ITEM_THIRD_OPTION:
				case GROUND_ITEM_FOURTH_OPTION:
				case GROUND_ITEM_FIFTH_OPTION:
					lastOpponent = null;
					aoeSpellQueued = false;
					break;
				default:
					if (e.isItemOp())
					{
						lastOpponent = null;
						aoeSpellQueued = false;
					}
					break;
			}
		}
	}

	@Subscribe
	private void onInteractingChanged(InteractingChanged e)
	{
		if (plugin.isInAllowedCaves())
		{
			if (e.getSource() == client.getLocalPlayer())
			{
				if (e.getTarget() instanceof NPC)
				{
					NPC npc = (NPC) e.getTarget();
					lastOpponent = npc;
					lastOpponentID = npc.getId();
				}
				else
				{
					lastOpponent = null;
					lastOpponentID = -1;
				}
			}
		}
	}

	@Subscribe
	protected void onStatChanged(StatChanged e)
	{
		int currentXp = e.getXp();
		int previousXp = previous_exp[e.getSkill().ordinal()];
		if (previousXp > 0 && currentXp - previousXp > 0)
		{
			int hit;

			Actor interacted = Objects.requireNonNull(client.getLocalPlayer()).getInteracting();
			if (interacted instanceof NPC && lastOpponent == null)
			{
				lastOpponent = interacted;
			}

			if (plugin.isInAllowedCaves() && lastOpponent != null)
			{
				switch (e.getSkill())
				{
					case ATTACK:
					case STRENGTH:
					case DEFENCE:
					case RANGED:
						//Long range should be calculated with range only
						hit = calculateHitOnNpc(lastOpponentID, attackStyle == AttackStyle.LONGRANGE ? Skill.RANGED : e.getSkill(),
							currentXp - previousXp, attackStyle, weaponStyle);
						processHit(hit, e.getSkill(), attackStyle, weaponStyle, (NPC) lastOpponent);
						break;
					case HITPOINTS:
						if (attackStyle == AttackStyle.CASTING)
						{
							hit = calculateHitOnNpc(lastOpponentID, e.getSkill(), currentXp - previousXp, attackStyle, weaponStyle);
							processHit(hit, e.getSkill(), attackStyle, weaponStyle, (NPC) lastOpponent);
						}
						break;
				}
			}
		}

		previous_exp[e.getSkill().ordinal()] = e.getXp();
	}

	@Subscribe
	protected void onFakeXpDrop(FakeXpDrop e)
	{
		switch (e.getSkill())
		{
			case ATTACK:
			case STRENGTH:
			case DEFENCE:
			case RANGED:
			case HITPOINTS: //HP used instead of magic
				final int currentXp = fakeXpMap.getOrDefault(e.getSkill(), 0);
				fakeXpMap.put(e.getSkill(), currentXp + e.getXp());
				break;
		}
	}

	public int calculateHitOnNpc(int id, Skill skill, int xpDiff, AttackStyle attackStyle, WeaponStyle weaponStyle)
	{
		double modifier = 1.0;

		if (XPModifiers.getNPC(id) != null)
		{
			modifier = (XPModifiers.getXpMod(id) + 100) / 100.0d;
		}

		return calculateHit(skill, xpDiff, attackStyle, weaponStyle, modifier, config.xpMultiplier());
	}

	private int calculateHit(Skill skill, int xpDiff, AttackStyle attackStyle, WeaponStyle weaponStyle, double modifier, double configModifier)
	{
		double damage = 0;

		if (Math.abs(configModifier) < 1e-6)
		{
			configModifier = 1e-6;
		}

		if (modifier < 1e-6)
		{
			return 0;
		}

		switch (skill)
		{
			case ATTACK:
			case STRENGTH:
			case DEFENCE:
				switch (attackStyle)
				{
					case ACCURATE:
					case AGGRESSIVE:
					case DEFENSIVE:
						damage = xpDiff / 4.0D;
						break;
					case CONTROLLED:
						damage = xpDiff / 1.33D;
						break;
					case DEFENSIVE_CASTING:
						damage = xpDiff;
						break;
				}
				break;
			case HITPOINTS:
				if (attackStyle == AttackStyle.CASTING)
				{
					damage = xpDiff / 1.33D;
					break;
				}
				break;
			case RANGED:
				switch (attackStyle)
				{
					case RANGING:
						damage = xpDiff / 4.0D;
						break;
					case LONGRANGE:
						damage = xpDiff / 2.0D;
						break;
				}
				break;
		}

		//Rounding at end more accurate
		return (int) Math.round(damage / modifier / configModifier);
	}

	private void processHit(int damage, Skill skill, AttackStyle attackStyle, WeaponStyle style, NPC interacting)
	{
		if (!processedThisTick && damage > 0 && skill != null)
		{
			processedThisTick = true;
			boolean isAoe = style == WeaponStyle.CHINS || (client.getLocalPlayer().getAnimation() == BARRAGE || aoeSpellQueued);
			checkIfInteractingDead(damage, isAoe, interacting.getIndex(), attackStyle, style);
		}
	}

	private void checkIfInteractingDead(int damage, boolean isAoe, int index, AttackStyle attackStyle, WeaponStyle style)
	{
		clientThread.invokeLater(() -> {
			if (damage != -1)
			{
				TzhaarNPC target = findTargetByIndex(index);
				if (target != null)
				{
					List<TzhaarNPC> clump = getNearbyTzhaarNpcs(target);
					handleTargetDeath(target, damage, isAoe, attackStyle, style, clump);
				}
				aoeSpellQueued = false;
			}
		});
	}

	private TzhaarNPC findTargetByIndex(int index)
	{
		for (TzhaarNPC n : plugin.getNpcs())
		{
			if (n.getNpc().getIndex() == index)
			{
				return n;
			}
		}
		return null;
	}

	private List<TzhaarNPC> getNearbyTzhaarNpcs(TzhaarNPC target)
	{
		List<TzhaarNPC> clump = new ArrayList<>();
		for (TzhaarNPC n : plugin.getNpcs())
		{
			if (!n.isDead() && n.getNpc().getWorldLocation().distanceTo(target.getNpc().getWorldLocation()) <= 1)
			{
				clump.add(n);
			}
		}
		return clump;
	}

	private void handleTargetDeath(TzhaarNPC target, int damage, boolean isAoe, AttackStyle attackStyle, WeaponStyle style, List<TzhaarNPC> clump)
	{
		if (!isAoe || clump.size() == 1 || client.getVarbitValue(Varbits.MULTICOMBAT_AREA) == 0 || (style == WeaponStyle.SCYTHES && attackStyle != AttackStyle.CASTING))
		{
			// Handle normally (clump size = 1) or single combat if AoE
			target.setQueuedDamage(target.getQueuedDamage() + damage);
			handleDead(target, target.getQueuedDamage() >= target.getHp());
		}
		else
		{
			// Handle clump (clump size > 1)
			if (clump.stream().mapToInt(TzhaarNPC::getHp).sum() <= damage)
			{
				clump.forEach(npc -> handleDead(npc, true));
			}
		}
	}

	private void handleDead(TzhaarNPC npc, boolean dead)
	{
		for (TzhaarNPC n : plugin.getNpcs())
		{
			if (n.getNpc().getIndex() == npc.getNpc().getIndex())
			{
				if (dead)
				{
					n.setDead(true);
				}

				boolean isHidden = plugin.getHiddenNPCs().stream().anyMatch(h -> h.getNpc().getIndex() == n.getNpc().getIndex());
				if (n.isDead() && !isHidden)
				{
					plugin.getHiddenNPCs().add(n);
				}

				if (!n.isDead() && isHidden)
				{
					plugin.getHiddenNPCs().remove(n);
				}
			}
		}
	}

	// Copied from Opponent Info
	private void recalcHP(TzhaarNPC n, int lastRatio, int lastHealthScale)
	{
		int health;
		if (lastRatio > 0)
		{
			int minHealth = 1;
			int maxHealth;
			if (lastHealthScale > 1)
			{
				if (lastRatio > 1)
				{
					minHealth = (n.getMaxHp() * (lastRatio - 1) + lastHealthScale - 2) / (lastHealthScale - 1);
				}
				maxHealth = (n.getMaxHp() * lastRatio - 1) / (lastHealthScale - 1);
				if (maxHealth > n.getMaxHp())
				{
					maxHealth = n.getMaxHp();
				}
			}
			else
			{
				maxHealth = n.getMaxHp();
			}

			health = (minHealth + maxHealth + 1) / 2;

			n.setHp(health);
			handleDead(n, health <= 0);

			//If the npc is not set to dead, but the death tick was tracked -> reset to 0
			if (!n.isDead() && n.getDeathTick() > 0)
			{
				n.setDeathTick(0);
			}
		}
	}

	// Set Zuk's hp off of the widget
	private int getZukHPfromWidget()
	{
		Widget widget = client.getWidget(39059465);
		if (widget != null && !widget.isHidden())
		{
			return Integer.parseInt(widget.getText().substring(0, widget.getText().indexOf("/")).trim());
		}
		return -1;
	}

	// Check if the hp widget is active for Zuk
	private boolean zukWidgetActive()
	{
		Widget widget = client.getWidget(39059465);
		return widget != null && !widget.isHidden();
	}
}
