package com.partydefencetracker;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.runelite.client.party.messages.PartyMemberMessage;

@Value
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DefenceTrackerUpdate extends PartyMemberMessage
{
	String boss;
	int index;
	boolean alive;
	int world;
	String weapon;
}
