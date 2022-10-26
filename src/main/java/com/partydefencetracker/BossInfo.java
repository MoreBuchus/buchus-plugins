package com.partydefencetracker;

import lombok.Getter;

enum BossInfo
{
	ABYSSAL_SIRE("Abyssal Sire", 250),
	BA_BA("Ba-Ba", 20),
	CALLISTO("Callisto", 440),
	CERBERUS("Cerberus", 110),
	CHAOS_ELEMENTAL("Chaos Elemental", 270),
	CORPOREAL_BEAST("Corporeal Beast", 310),
	ELIDINIS_WARDEN("Elidinis' Warden", 30),
	GENERAL_GRAARDOR("General Graardor", 250),
	GIANT_MOLE("Giant Mole", 200),
	GREAT_OLM("Great Olm (Left claw)", 175),
	KALPHITE_QUEEN("Kalphite Queen", 300),
	KEPHRI("Kephri", 20),
	KING_BLACK_DRAGON("King Black Dragon", 240),
	KRIL_TSUTSAROTH("K'ril Tsutsaroth", 270),
	NYLOCAS_VASILIAS("Nylocas Vasilias", 50),
	OBELISK("<col=00ffff>Obelisk</col>", 40),
	PESTILENT_BLOAT("Pestilent Bloat", 100),
	SARACHNIS("Sarachnis", 150),
	SOTETSEG("Sotetseg", 200),
	TEKTON("Tekton", 205),
	TEKTON_ENRAGED("Tekton (enraged)", 205),
	THE_MAIDEN_OF_SUGADINTI("The Maiden of Sugadinti", 200),
	TUMEKENS_WARDEN("Tumeken's Warden", 30),
	VENENATIS("Venenatis", 490),
	VETION("Vet'ion", 395),
	VETION_REBORN("Vet'ion Reborn", 395),
	XARPUS("Xarpus", 250),
	ZEBAK("Zebak", 20);

	@Getter
	private final String name;
	@Getter
	private final double baseDef;

	BossInfo(String name, double baseDef)
	{
		this.name = name;
		this.baseDef = baseDef;
	}

	static BossInfo getBoss(String bossName)
	{
		for (BossInfo boss : values())
		{
			if (boss.name.contains(bossName))
			{
				return boss;
			}
		}
		return null;
	}

	static double getBaseDefence(String bossName)
	{
		BossInfo boss = getBoss(bossName);
		if (boss != null)
		{
			return boss.baseDef;
		}
		return 0;
	}
}
