package com.partydefencetracker;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.runelite.api.NPC;
import net.runelite.client.party.messages.PartyMemberMessage;

@Value
@EqualsAndHashCode(callSuper = true)
public class DefenceTrackerUpdate extends PartyMemberMessage
{
	String boss;
	int index;
	boolean alive;
	boolean bossSpawned;
	int world;

	public DefenceTrackerUpdate(String boss, int index, boolean alive, boolean bossSpawned, int world)
	{
		this.boss = boss;
		this.index = index;
		this.alive = alive;
		this.bossSpawned = bossSpawned;
		this.world = world;
	}
}
