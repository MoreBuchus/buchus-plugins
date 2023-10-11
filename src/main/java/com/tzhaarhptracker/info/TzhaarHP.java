package com.tzhaarhptracker.info;

import com.google.common.collect.Sets;
import java.util.Set;
import lombok.Getter;
import static net.runelite.api.NpcID.*;

public enum TzhaarHP
{
	//Fight Caves
	FC_BAT(10, TZKIH_3116, TZKIH_3117),
	FC_BIG_BLOB(20, TZKEK_3118, TZKEK_3119),
	FC_BLOBLETS(10, TZKEK_3120),
	FC_RANGE(40, TOKXIL_3121, TOKXIL_3122),
	FC_MELEE(80, YTMEJKOT, YTMEJKOT_3124),
	FC_MAGE(160, KETZEK, KETZEK_3126),
	TZTOK_JAD(250, TZTOKJAD),
	FC_JAD_HEALER(60, YTHURKOT),

	//Inferno
	NIBBLER(10, JALNIB),
	BAT(25, JALMEJRAH),
	BLOB(40, JALAK),
	BLOBLETS(15, JALAKREKMEJ, JALAKREKXIL, JALAKREKKET),
	MELEE(75, JALIMKOT),
	RANGE(125, JALXIL, JALXIL_7702),
	MAGE(220, JALZEK, JALZEK_7703),
	INFERNO_JAD(350, JALTOKJAD, JALTOKJAD_7704),
	TZKAL_ZUK(1200, TZKALZUK),
	ZUK_HEALER(75, JALMEJJAK),
	INFERNO_JAD_HEALERS(90, YTHURKOT_7701, YTHURKOT_7705),
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
