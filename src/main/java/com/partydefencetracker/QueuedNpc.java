package com.partydefencetracker;

import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.client.plugins.specialcounter.SpecialWeapon;

import java.util.ArrayList;

@Getter(AccessLevel.PACKAGE)
public class QueuedNpc
{
	public int index;
	public ArrayList<QueuedSpec> queuedSpecs;

	QueuedNpc(int index)
	{
		this.index = index;
		this.queuedSpecs = new ArrayList<>();
	}

	public static class QueuedSpec
	{
		public SpecialWeapon weapon;
		public int hit;

		QueuedSpec(SpecialWeapon weapon, int hit)
		{
			this.weapon = weapon;
			this.hit = hit;
		}
	}
}