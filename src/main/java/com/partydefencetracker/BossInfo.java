package com.partydefencetracker;

import lombok.Getter;

enum BossInfo
{
	ABYSSAL_PORTAL("Abyssal portal", 176),
	ABYSSAL_SIRE("Abyssal Sire", 250),
	AKKHA("Akkha", 80),
	AKKHAS_SHADOW("Akkha's Shadow", 30),
	ALCHEMICAL_HYDRA("Alchemical Hydra", 100),
	ARTIO("Artio", 150),
	BA_BA("Ba-Ba", 20),
	CALLISTO("Callisto", 225),
	CALVARION("Calvar'ion", 225),
	CERBERUS("Cerberus", 110),
	CHAOS_ELEMENTAL("Chaos Elemental", 270),
	COMMANDER_ZILYANA("Commander Zilyana", 300),
	CORE("<col=00ffff>Core</col>", 0),
	CORPOREAL_BEAST("Corporeal Beast", 310),
	DAGANNOTH_PRIME("Dagannoth Prime", 255),
	DAGANNOTH_REX("Dagannoth Rex", 255),
	DAGANNOTH_SUPREME("Dagannoth Supreme", 128),
	DEATHLY_MAGE("Deathly mage", 155),
	DEATHLY_RANGER("Deathly ranger", 155),
	ELIDINIS_WARDEN("Elidinis' Warden", 30),
	GENERAL_GRAARDOR("General Graardor", 250),
	GIANT_MOLE("Giant Mole", 200),
	GREAT_OLM("Great Olm", 150),
	GREAT_OLM_LEFT_CLAW("Great Olm (Left claw)", 175),
	GREAT_OLM_RIGHT_CLAW("Great Olm (Right claw)", 175),
	ICE_DEMON("Ice Demon", 160),
	KALPHITE_QUEEN("Kalphite Queen", 300),
	KEPHRI("Kephri", 20),
	KING_BLACK_DRAGON("King Black Dragon", 240),
	KREE_ARRA("Kree'arra", 260),
	KRIL_TSUTSAROTH("K'ril Tsutsaroth", 270),
	LIZARDMAN_SHAMAN("Lizardman shaman", 140),
	NEX("Nex", 260),
	NYLOCAS_VASILIAS("Nylocas Vasilias", 50),
	OBELISK("<col=00ffff>Obelisk</col>", 40),
	PESTILENT_BLOAT("Pestilent Bloat", 100),
	PHANTOM_MUSPAH("Phantom Muspah", 200),
	SARACHNIS("Sarachnis", 150),
	SCORPIA("Scorpia", 180),
	SKELETAL_MYSTIC("Skeletal Mystic", 187),
	SKOTIZO("Skotizo", 200),
	SOTETSEG("Sotetseg", 200),
	SPINDEL("Spindel", 225),
	TEKTON("Tekton", 205),
	TEKTON_ENRAGED("Tekton (enraged)", 205),
	THE_MAIDEN_OF_SUGADINTI("The Maiden of Sugadinti", 200),
	TUMEKENS_WARDEN("Tumeken's Warden", 30),
	TZKAL_ZUK("TzKal-Zuk", 260),
	TZTOK_JAD("TzTok-Jad", 480),
	VASA("Vasa Nistirio", 175),
	VENENATIS("Venenatis", 321),
	VETION("Vet'ion", 395),
	VORKATH("Vorkath", 214),
	XARPUS("Xarpus", 250),
	ZEBAK("Zebak", 20),
	ZULRAH("Zulrah", 300);

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
