/*
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

import com.google.common.collect.Sets;
import java.util.Set;
import lombok.Getter;
import net.runelite.api.gameval.NpcID;

enum XPModifiers
{
	//Fight Caves -> All have +0% xp mods
	FC_BAT(0, NpcID.TZHAAR_FIGHTCAVE_SWARM_1A, NpcID.TZHAAR_FIGHTCAVE_SWARM_1B),
	FC_BLOB(0, NpcID.TZHAAR_FIGHTCAVE_SWARM_2A, NpcID.TZHAAR_FIGHTCAVE_SWARM_2B, NpcID.TZHAAR_FIGHTCAVE_SWARM_2SPAWN), //All blobs have 0 xp mod
	FC_RANGE(0, NpcID.TZHAAR_FIGHTCAVE_SWARM_3A, NpcID.TZHAAR_FIGHTCAVE_SWARM_3B),
	FC_MELEE(0, NpcID.TZHAAR_FIGHTCAVE_SWARM_4A, NpcID.TZHAAR_FIGHTCAVE_SWARM_4B),
	FC_MAGE(0, NpcID.TZHAAR_FIGHTCAVE_SWARM_5A, NpcID.TZHAAR_FIGHTCAVE_SWARM_5B),
	TZTOK_JAD(0, NpcID.TZHAAR_FIGHTCAVE_SWARM_BOSS),
	FC_JAD_HEALER(0, NpcID.TZHAAR_FIGHTCAVE_SWARM_BOSS_CLERIC),

	//Inferno
	NIBBLER(0, NpcID.INFERNO_NIBBLER),
	BAT(0, NpcID.INFERNO_CREATURE_HARPIE),
	BLOB(2.5, NpcID.INFERNO_CREATURE_SPLITTER),
	BLOBLETS(0, NpcID.INFERNO_CREATURE_SPLITTER_MAGE, NpcID.INFERNO_CREATURE_SPLITTER_RANGE, NpcID.INFERNO_CREATURE_SPLITTER_MELEE),
	MELEE(7.5, NpcID.INFERNO_CREATURE_MELEE),
	RANGE(0, NpcID.INFERNO_CREATURE_RANGER, NpcID.INFERNO_RANGER_FINALWAVE),
	MAGE(0, NpcID.INFERNO_CREATURE_MAGER, NpcID.INFERNO_MAGER_FINALWAVE),
	INFERNO_JAD(0, NpcID.INFERNO_JAD, NpcID.INFERNO_JAD_FINALWAVE),
	TZKAL_ZUK(57.5, NpcID.INFERNO_TZKALZUK_PLACEHOLDER),
	ZUK_HEALER(0, NpcID.INFERNO_ZUK_HEALER),
	INFERNO_JAD_HEALERS(0, NpcID.INFERNO_JAD_HEALER, NpcID.INFERNO_JAD_HEALER_FINALWAVE),
	PILLAR(0, NpcID.INFERNO_INVISIBLE_3X3) //7710 is just as it is dying
	;

	@Getter
	private final double xpMod;
	@Getter
	private final Set<Integer> ids;

	XPModifiers(double xpMod, Integer... ids)
	{
		this.xpMod = xpMod;
		this.ids = Sets.newHashSet(ids);
	}

	static XPModifiers getNPC(int id)
	{
		for (XPModifiers npc : values())
		{
			if (npc.ids.stream().anyMatch(i -> i == id))
			{
				return npc;
			}
		}
		return null;
	}

	static double getXpMod(int id)
	{
		XPModifiers npc = getNPC(id);
		if (npc != null)
		{
			return npc.xpMod;
		}
		return 0;
	}
}
