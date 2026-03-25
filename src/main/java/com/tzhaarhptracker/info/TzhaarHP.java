package com.tzhaarhptracker.info;

import com.google.common.collect.Sets;
import java.util.Set;
import lombok.Getter;
import net.runelite.api.gameval.NpcID;

public enum TzhaarHP
{
	//Fight Caves
	FC_BAT(10, NpcID.TZHAAR_FIGHTCAVE_SWARM_1A, NpcID.TZHAAR_FIGHTCAVE_SWARM_1B),
	FC_BIG_BLOB(20, NpcID.TZHAAR_FIGHTCAVE_SWARM_2A, NpcID.TZHAAR_FIGHTCAVE_SWARM_2B),
	FC_BLOBLETS(10, NpcID.TZHAAR_FIGHTCAVE_SWARM_2SPAWN),
	FC_RANGE(40, NpcID.TZHAAR_FIGHTCAVE_SWARM_3A, NpcID.TZHAAR_FIGHTCAVE_SWARM_3B),
	FC_MELEE(80, NpcID.TZHAAR_FIGHTCAVE_SWARM_4A, NpcID.TZHAAR_FIGHTCAVE_SWARM_4B),
	FC_MAGE(160, NpcID.TZHAAR_FIGHTCAVE_SWARM_5A, NpcID.TZHAAR_FIGHTCAVE_SWARM_5B),
	TZTOK_JAD(250, NpcID.TZHAAR_FIGHTCAVE_SWARM_BOSS),
	FC_JAD_HEALER(60, NpcID.TZHAAR_FIGHTCAVE_SWARM_BOSS_CLERIC),

	//Inferno
	NIBBLER(10, NpcID.INFERNO_NIBBLER),
	BAT(25, NpcID.INFERNO_CREATURE_HARPIE),
	BLOB(40, NpcID.INFERNO_CREATURE_SPLITTER),
	BLOBLETS(15, NpcID.INFERNO_CREATURE_SPLITTER_MAGE, NpcID.INFERNO_CREATURE_SPLITTER_RANGE, NpcID.INFERNO_CREATURE_SPLITTER_MELEE),
	MELEE(75, NpcID.INFERNO_CREATURE_MELEE),
	RANGE(125, NpcID.INFERNO_CREATURE_RANGER, NpcID.INFERNO_RANGER_FINALWAVE),
	MAGE(220, NpcID.INFERNO_CREATURE_MAGER, NpcID.INFERNO_MAGER_FINALWAVE),
	INFERNO_JAD(350, NpcID.INFERNO_JAD, NpcID.INFERNO_JAD_FINALWAVE),
	TZKAL_ZUK(1200, NpcID.INFERNO_TZKALZUK_PLACEHOLDER),
	ZUK_HEALER(75, NpcID.INFERNO_ZUK_HEALER),
	INFERNO_JAD_HEALERS(90, NpcID.INFERNO_JAD_HEALER, NpcID.INFERNO_JAD_HEALER_FINALWAVE),
	PILLAR(255, NpcID.INFERNO_INVISIBLE_3X3) //7710 is just as it is dying
	;

	@Getter
	private final int maxHP;
	@Getter
	private final Set<Integer> ids;

	TzhaarHP(int maxHP, Integer... ids)
	{
		this.maxHP = maxHP;
		this.ids = Sets.newHashSet(ids);
	}

	public static TzhaarHP getNPC(int id)
	{
		for (TzhaarHP npc : values())
		{
			if (npc.ids.stream().anyMatch(i -> i == id))
			{
				return npc;
			}
		}
		return null;
	}

	public static int getMaxHP(int id)
	{
		TzhaarHP npc = getNPC(id);
		if (npc != null)
		{
			return npc.maxHP;
		}
		return 0;
	}

	public static int getRespawnedHP(int id)
	{
		TzhaarHP npc = getNPC(id);
		if (npc != null)
		{
			return (int) Math.ceil((double) npc.maxHP / 2);
		}
		return 0;
	}
}
