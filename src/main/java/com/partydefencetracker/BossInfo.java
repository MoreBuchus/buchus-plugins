package com.partydefencetracker;

import lombok.Getter;

enum BossInfo
{
	ABYSSAL_PORTAL("Abyssal portal", 176, 176),
	ABYSSAL_SIRE("Abyssal Sire", 250, 200),
	AKKHA("Akkha", 80, 100),
	AKKHAS_SHADOW("Akkha's Shadow", 30, 100),
	ALCHEMICAL_HYDRA("Alchemical Hydra", 100, 260),
	ARTIO("Artio", 150, 90),
	BA_BA("Ba-Ba", 20, 100),
	CALLISTO("Callisto", 225, 140),
	CALVARION("Calvar'ion", 225, 178),
	CERBERUS("Cerberus", 110, 220),
	CHAOS_ELEMENTAL("Chaos Elemental", 270, 270),
	COMMANDER_ZILYANA("Commander Zilyana", 300, 300),
	CORE("<col=00ffff>Core</col>", 0, 0),
	CORPOREAL_BEAST("Corporeal Beast", 310, 350),
	DAGANNOTH_PRIME("Dagannoth Prime", 255, 255),
	DAGANNOTH_REX("Dagannoth Rex", 255, 0),
	DAGANNOTH_SUPREME("Dagannoth Supreme", 128, 244),
	DEATHLY_MAGE("Deathly mage", 155, 210),
	DEATHLY_RANGER("Deathly ranger", 155, 155),
	ELIDINIS_WARDEN("Elidinis' Warden", 30, 190),
	GENERAL_GRAARDOR("General Graardor", 250, 150),
	GIANT_MOLE("Giant Mole", 200, 200),
	GREAT_OLM("Great Olm", 150, 250),
	GREAT_OLM_LEFT_CLAW("Great Olm (Left claw)", 175, 175),
	GREAT_OLM_RIGHT_CLAW("Great Olm (Right claw)", 175, 87),
	ICE_DEMON("Ice Demon", 160, 390),
	KALPHITE_QUEEN("Kalphite Queen", 300, 150),
	KEPHRI("Kephri", 20, 125),
	KING_BLACK_DRAGON("King Black Dragon", 240, 240),
	KREE_ARRA("Kree'arra", 260, 200),
	KRIL_TSUTSAROTH("K'ril Tsutsaroth", 270, 200),
	LIZARDMAN_SHAMAN("Lizardman shaman", 210, 130),
	NEX("Nex", 260, 230),
	NYLOCAS_VASILIAS("Nylocas Vasilias", 50, 50),
	OBELISK("<col=00ffff>Obelisk</col>", 40, 100),
	PESTILENT_BLOAT("Pestilent Bloat", 100, 150),
	PHANTOM_MUSPAH("Phantom Muspah", 200, 150), //180 shielded
	SARACHNIS("Sarachnis", 150, 150),
	SCORPIA("Scorpia", 180, 1),
	SKELETAL_MYSTIC("Skeletal Mystic", 187, 140),
	SKOTIZO("Skotizo", 200, 280),
	SOTETSEG("Sotetseg", 200, 250),
	SPINDEL("Spindel", 225, 235),
	TEKTON("Tekton", 205, 205),
	TEKTON_ENRAGED("Tekton (enraged)", 205, 205),
	THE_MAIDEN_OF_SUGADINTI("The Maiden of Sugadinti", 200, 350),
	TUMEKENS_WARDEN("Tumeken's Warden", 30, 190),
	TZKAL_ZUK("TzKal-Zuk", 260, 150),
	TZTOK_JAD("TzTok-Jad", 480, 480),
	VASA("Vasa Nistirio", 175, 230),
	VENENATIS("Venenatis", 321, 300),
	VETION("Vet'ion", 395, 300),
	VORKATH("Vorkath", 214, 150),
	XARPUS("Xarpus", 250, 220),
	YAMA("Yama", 225, 250),
	ZEBAK("Zebak", 20, 100),
	ZULRAH("Zulrah", 300, 300);

	@Getter
	private final String name;
	@Getter
	private final double baseDef;
	@Getter
	private final double baseMagic;

	BossInfo(String name, double baseDef, double baseMagic)
	{
		this.name = name;
		this.baseDef = baseDef;
		this.baseMagic = baseMagic;
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

	static double getBaseMagic(String bossName)
	{
		BossInfo boss = getBoss(bossName);
		if(boss != null)
		{
			return boss.baseMagic;
		}
		return 0;
	}
}
