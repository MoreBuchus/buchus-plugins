package com.coxadditions.party;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.runelite.client.party.messages.PartyMemberMessage;

@Value
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PartyRaidsPotsUpdate extends PartyMemberMessage
{
	int ticks;
	String pot;
}