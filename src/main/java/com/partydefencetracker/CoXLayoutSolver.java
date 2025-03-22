/*
 * Copyright (c) 2022, l2- <https://github.com/l2->
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
package com.partydefencetracker;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.GameState;
import net.runelite.api.InstanceTemplates;
import net.runelite.api.NullObjectID;
import net.runelite.api.Point;
import net.runelite.api.Tile;
import net.runelite.api.VarPlayer;
import net.runelite.api.Varbits;
import net.runelite.api.WorldView;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.plugins.raids.Raid;
import net.runelite.client.plugins.raids.RaidRoom;

import javax.inject.Inject;
import javax.inject.Singleton;

import static net.runelite.api.Perspective.SCENE_SIZE;

// Mostly copied from the core RaidsPlugin
@Slf4j
@Singleton
public class CoXLayoutSolver
{
	static final int ROOM_MAX_SIZE = 32;
	private static final int LOBBY_PLANE = 3;
	private static final int SECOND_FLOOR_PLANE = 2;
	private static final int ROOMS_PER_PLANE = 8;
	private static final int AMOUNT_OF_ROOMS_PER_X_AXIS_PER_PLANE = 4;
	private static final WorldPoint TEMP_LOCATION = new WorldPoint(3360, 5152, 2);
	private static final String CM_RAID_CODE = "SPCFPC#¤CFP SPC#";

	private final Client client;
	boolean checkInRaid;
	private boolean loggedIn;
	private boolean inRaidChambers;
	private int raidPartyID;
	@Getter
	private Raid raid;

	@Inject
	public CoXLayoutSolver(Client client)
	{
		this.client = client;
	}

	public boolean isCM()
	{
		return raid != null && CM_RAID_CODE.equals(raid.toCode());
	}

	public void onVarbitChanged(VarbitChanged event)
	{
		if (event.getVarpId() == VarPlayer.IN_RAID_PARTY)
		{
			boolean tempInRaid = client.getVarbitValue(Varbits.IN_RAID) == 1;
			if (loggedIn && !tempInRaid)
			{
				raid = null;
			}

			raidPartyID = event.getValue();
		}

		if (event.getVarbitId() == Varbits.IN_RAID)
		{
			boolean tempInRaid = event.getValue() == 1;
			if (tempInRaid && loggedIn)
			{
				checkRaidPresence();
			}

			inRaidChambers = tempInRaid;
		}
	}

	public void onGameTick(GameTick event)
	{
		if (checkInRaid)
		{
			loggedIn = true;
			checkInRaid = false;

			if (inRaidChambers)
			{
				checkRaidPresence();
			}
			else
			{
				if (raidPartyID == -1)
				{
					raid = null;
				}
			}
		}
	}

	public void onGameStateChanged(GameStateChanged event)
	{
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			if (client.getLocalPlayer() == null
				|| client.getLocalPlayer().getWorldLocation().equals(TEMP_LOCATION))
			{
				return;
			}

			checkInRaid = true;
		}
		else if (client.getGameState() == GameState.LOGIN_SCREEN
			|| client.getGameState() == GameState.CONNECTION_LOST)
		{
			loggedIn = false;
		}
		else if (client.getGameState() == GameState.HOPPING)
		{
			raid = null;
		}
	}

	private void checkRaidPresence()
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		inRaidChambers = client.getVarbitValue(Varbits.IN_RAID) == 1;

		if (!inRaidChambers)
		{
			return;
		}

		raid = buildRaid(raid);
	}

	private Raid buildRaid(Raid from)
	{
		WorldView wv = client.getTopLevelWorldView();
		Raid raid = from;

		if (raid == null)
		{
			Point gridBase = findLobbyBase();

			if (gridBase == null)
			{
				return null;
			}

			Integer lobbyIndex = findLobbyIndex(gridBase);

			if (lobbyIndex == null)
			{
				return null;
			}

			raid = new Raid(
				new WorldPoint(wv.getBaseX() + gridBase.getX(), wv.getBaseY() + gridBase.getY(), LOBBY_PLANE),
				lobbyIndex
			);
		}

		int baseX = raid.getLobbyIndex() % AMOUNT_OF_ROOMS_PER_X_AXIS_PER_PLANE;
		int baseY = raid.getLobbyIndex() % ROOMS_PER_PLANE > (AMOUNT_OF_ROOMS_PER_X_AXIS_PER_PLANE - 1) ? 1 : 0;

		for (int i = 0; i < raid.getRooms().length; i++)
		{
			int x = i % AMOUNT_OF_ROOMS_PER_X_AXIS_PER_PLANE;
			int y = i % ROOMS_PER_PLANE > (AMOUNT_OF_ROOMS_PER_X_AXIS_PER_PLANE - 1) ? 1 : 0;
			int plane = i > (ROOMS_PER_PLANE - 1) ? SECOND_FLOOR_PLANE : LOBBY_PLANE;

			x = x - baseX;
			y = y - baseY;

			x = raid.getGridBase().getX() + x * ROOM_MAX_SIZE;
			y = raid.getGridBase().getY() - y * ROOM_MAX_SIZE;

			x = x - wv.getBaseX();
			y = y - wv.getBaseY();

			if (x < (1 - ROOM_MAX_SIZE) || x >= SCENE_SIZE)
			{
				continue;
			}
			else if (x < 1)
			{
				x = 1;
			}

			if (y < 1)
			{
				y = 1;
			}

			Tile tile = wv.getScene().getTiles()[plane][x][y];

			if (tile == null)
			{
				continue;
			}

			RaidRoom room = determineRoom(tile);
			raid.setRoom(room, i);
		}

		return raid;
	}

	private Point findLobbyBase()
	{
		Tile[][] tiles = client.getTopLevelWorldView().getScene().getTiles()[LOBBY_PLANE];

		for (int x = 0; x < SCENE_SIZE; x++)
		{
			for (int y = 0; y < SCENE_SIZE; y++)
			{
				if (tiles[x][y] == null || tiles[x][y].getWallObject() == null)
				{
					continue;
				}

				if (tiles[x][y].getWallObject().getId() == NullObjectID.NULL_12231)
				{
					return tiles[x][y].getSceneLocation();
				}
			}
		}

		return null;
	}

	private RaidRoom determineRoom(Tile base)
	{
		int chunkData = client.getTopLevelWorldView().getInstanceTemplateChunks()[base.getPlane()][(base.getSceneLocation().getX()) / 8][base.getSceneLocation().getY() / 8];
		InstanceTemplates template = InstanceTemplates.findMatch(chunkData);

		if (template == null)
		{
			return RaidRoom.EMPTY;
		}

		switch (template)
		{
			case RAIDS_LOBBY:
			case RAIDS_START:
				return RaidRoom.START;
			case RAIDS_END:
				return RaidRoom.END;
			case RAIDS_SCAVENGERS:
			case RAIDS_SCAVENGERS2:
				return RaidRoom.SCAVENGERS;
			case RAIDS_SHAMANS:
				return RaidRoom.SHAMANS;
			case RAIDS_VASA:
				return RaidRoom.VASA;
			case RAIDS_VANGUARDS:
				return RaidRoom.VANGUARDS;
			case RAIDS_ICE_DEMON:
				return RaidRoom.ICE_DEMON;
			case RAIDS_THIEVING:
				return RaidRoom.THIEVING;
			case RAIDS_FARMING:
			case RAIDS_FARMING2:
				return RaidRoom.FARMING;
			case RAIDS_MUTTADILES:
				return RaidRoom.MUTTADILES;
			case RAIDS_MYSTICS:
				return RaidRoom.MYSTICS;
			case RAIDS_TEKTON:
				return RaidRoom.TEKTON;
			case RAIDS_TIGHTROPE:
				return RaidRoom.TIGHTROPE;
			case RAIDS_GUARDIANS:
				return RaidRoom.GUARDIANS;
			case RAIDS_CRABS:
				return RaidRoom.CRABS;
			case RAIDS_VESPULA:
				return RaidRoom.VESPULA;
			default:
				return RaidRoom.EMPTY;
		}
	}

	private Integer findLobbyIndex(Point gridBase)
	{
		if (Constants.SCENE_SIZE <= gridBase.getX() + ROOM_MAX_SIZE
			|| Constants.SCENE_SIZE <= gridBase.getY() + ROOM_MAX_SIZE)
		{
			return null;
		}
		int x;
		int y;
		Tile[][] tiles = client.getTopLevelWorldView().getScene().getTiles()[LOBBY_PLANE];
		if (tiles[gridBase.getX()][gridBase.getY() + ROOM_MAX_SIZE] == null)
		{
			y = 0;
		}
		else
		{
			y = 1;
		}
		if (tiles[gridBase.getX() + ROOM_MAX_SIZE][gridBase.getY()] == null)
		{
			x = 3;
		}
		else
		{
			for (x = 0; x < 3; x++)
			{
				int sceneX = gridBase.getX() - 1 - ROOM_MAX_SIZE * x;
				if (sceneX < 0 || tiles[sceneX][gridBase.getY()] == null)
				{
					break;
				}
			}
		}

		return x + y * AMOUNT_OF_ROOMS_PER_X_AXIS_PER_PLANE;
	}
}
