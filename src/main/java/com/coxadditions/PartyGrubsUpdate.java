package com.coxadditions;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.runelite.client.party.messages.PartyMemberMessage;

@Value
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PartyGrubsUpdate extends PartyMemberMessage
{
	String player;
	int world;
	int grubs;
}