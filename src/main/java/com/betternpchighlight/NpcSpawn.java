package com.betternpchighlight;

import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.coords.WorldPoint;

import java.util.ArrayList;

@Getter(AccessLevel.PACKAGE)
public class NpcSpawn
{
	public int index;
	public String name;
	public int id;
	public int size;
	public int diedOnTick;
	public int respawnTime;
	public ArrayList<WorldPoint> spawnLocations;
	public WorldPoint spawnPoint;
	public boolean dead;

	NpcSpawn(NPC npc)
	{
		this.name = npc.getName();
		this.id = npc.getId();
		this.index = npc.getIndex();
		this.spawnLocations = new ArrayList<>();
		this.respawnTime = -1;
		this.diedOnTick = -1;
		this.spawnPoint = null;
		this.dead = true;

		final NPCComposition composition = npc.getTransformedComposition();
		size = composition != null ? composition.getSize() : 1;
	}
}
