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
import static net.runelite.api.NpcID.*;

enum XPModifiers
{
	//Fight Caves -> All have +0% xp mods
	FC_BAT(0, TZKIH_3116, TZKIH_3117),
	FC_BLOB(0, TZKEK_3118, TZKEK_3119, TZKEK_3120), //All blobs have 0 xp mod
	FC_RANGE(0, TOKXIL_3121, TOKXIL_3122),
	FC_MELEE(0, YTMEJKOT, YTMEJKOT_3124),
	FC_MAGE(0, KETZEK, KETZEK_3126),
	TZTOK_JAD(0, TZTOKJAD),
	FC_JAD_HEALER(0, YTHURKOT),

	//Inferno
	NIBBLER(0, JALNIB),
	BAT(0, JALMEJRAH),
	BLOB(2.5, JALAK),
	BLOBLETS(0, JALAKREKMEJ, JALAKREKXIL, JALAKREKKET),
	MELEE(7.5, JALIMKOT),
	RANGE(0, JALXIL, JALXIL_7702),
	MAGE(0, JALZEK, JALZEK_7703),
	INFERNO_JAD(0, JALTOKJAD, JALTOKJAD_7704),
	TZKAL_ZUK(57.5, TZKALZUK),
	ZUK_HEALER(0, JALMEJJAK),
	INFERNO_JAD_HEALERS(0, YTHURKOT_7701, YTHURKOT_7705),
	PILLAR(0, ROCKY_SUPPORT) //7710 is just as it is dying
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
