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
package com.tzhaarhptracker;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Provides;
import com.tzhaarhptracker.info.TzhaarHP;
import java.awt.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.callback.Hooks;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.NPCManager;
import net.runelite.client.game.NpcUtil;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import com.tzhaarhptracker.info.DamageHandler;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.Text;
import org.apache.commons.lang3.ArrayUtils;

@Slf4j
@PluginDescriptor(
	name = "Tzhaar HP Tracker",
	description = "Marks Tzhaar NPCs and shows their current HP remaining",
	tags = {"inferno", "fight", "cave", "tzhaar", "jad", "zuk", "hp", "tracking", "dead", "npc", "indicator"}
)
public class TzhaarHPTrackerPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private TzhaarHPTrackerConfig config;

	@Inject
	private NPCManager npcManager;

	@Inject
	private TzhaarHPTrackerOverlay overlay;

	@Inject
	private ReminderOverlay reminderOverlay;

	@Inject
	private ClientThread clientThread;

	@Inject
	private NpcUtil npcUtil;

	@Inject
	private Hooks hooks;

	@Inject
	private DamageHandler handleDamage;

	@Getter
	private InfoHandler[] infoHandlers = null;

	@Inject
	private EventBus eventBus;

	private static final Set<MenuAction> NPC_MENU_ACTIONS = ImmutableSet.of(
		MenuAction.NPC_FIRST_OPTION, MenuAction.NPC_SECOND_OPTION, MenuAction.NPC_THIRD_OPTION, MenuAction.NPC_FOURTH_OPTION,
		MenuAction.NPC_FIFTH_OPTION, MenuAction.WIDGET_TARGET_ON_NPC, MenuAction.ITEM_USE_ON_NPC
	);

	@Getter
	private final ArrayList<TzhaarNPC> npcs = new ArrayList<>();

	@Getter
	private final ArrayList<TzhaarNPC> hiddenNPCs = new ArrayList<>();

	@Getter
	private static final Set<String> FIGHT_CAVE_NPC = ImmutableSet.of(
		"tz-kih", "tz-kek", "tok-xil", "yt-mejkot", "ket-zek", "yt-hurkot", "tztok-jad"
	);

	@Getter
	private static final Set<String> INFERNO_NPC = ImmutableSet.of(
		"jal-nib", "jal-mejrah", "jal-ak", "jal-akrek-xil", "jal-akrek-mej", "jal-akrek-ket", "jal-imkot", "jal-xil", "jal-zek",
		"jaltok-jad", "yt-hurkot", "tzkal-zuk", "jal-mejjak", "<col=00ffff>rocky support</col>"
	);

	@Getter
	private static final Set<String> EXCLUDED_NPC = ImmutableSet.of(
		"yt-hurkot", "tztok-jad", "jaltok-jad", "jal-mejjak", "tzkal-zuk"
	);

	@Getter
	private static final Set<String> REVIVABLE_NPC = ImmutableSet.of(
		"jal-mejrah", "jal-ak", "jal-imkot", "jal-xil", "jal-zek"
	);

	private static final int FIGHT_CAVES_REGION = 9551;
	private static final int INFERNO_REGION = 9043;
	private static final int JAD_CHALLENGE_VAR = 11878; // 0 = out, 1 = in

	@Getter
	private String spellbookType = "";

	private boolean waveStarted = false;
	private int waveStartTick = -1;
	@Getter
	private final Map<String, Integer> currentWave = new HashMap<>();

	private static final Pattern WAVE_START_PATTERN = Pattern.compile(".*Wave: (\\d+).*");
	private static final String TZHAAR_WAVE_COMPLETE = "Wave completed!";
	private static final String ZUK_KC_MESSAGE = "Your TzKal-Zuk kill count is:";
	private static final String JAD_KC_MESSAGE = "Your TzTok-Jad kill count is:";
	private static final String DEATH_MESSAGE = "You have been defeated!";

	@Getter
	Font font;

	private long lastTickNS = 0;
	@Getter
	private int lastTickDurMS = 0;

	private final Hooks.RenderableDrawListener drawListener = this::shouldDraw;

	@Provides
	TzhaarHPTrackerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TzhaarHPTrackerConfig.class);
	}

	protected void startUp() throws Exception
	{
		npcs.clear();
		hiddenNPCs.clear();
		currentWave.clear();
		loadFont();
		overlayManager.add(overlay);
		overlayManager.add(reminderOverlay);
		hooks.registerRenderableDrawListener(drawListener);

		if (infoHandlers == null)
		{
			infoHandlers = new InfoHandler[]{handleDamage};

			for (InfoHandler info : infoHandlers)
			{
				info.init();
			}
		}

		for (InfoHandler info : infoHandlers)
		{
			info.load();
			eventBus.register(info);
		}

		if (client.getGameState() == GameState.LOGGED_IN && isInAllowedCaves() && npcs.isEmpty())
		{
			for (NPC npc : client.getNpcs())
			{
				if (npc.getName() != null && (INFERNO_NPC.contains(npc.getName()) || FIGHT_CAVE_NPC.contains(npc.getName())))
				{
					try
					{
						if (TzhaarHP.getNPC(npc.getId()) != null)
						{
							int hp = TzhaarHP.getMaxHP(npc.getId()) != 0 ? TzhaarHP.getMaxHP(npc.getId()) : npcManager.getHealth(npc.getId());
							if (hp != 0)
							{
								TzhaarNPC newNPC = new TzhaarNPC(npc, hp, hp, client.getTickCount());
								//Set healed to true -> use ratio + scale to estimate NPCs HP who spawned before plugin startup
								newNPC.setHealed(true);
								npcs.add(newNPC);
							}
						}
					}
					catch (NullPointerException ignored)
					{
					}
				}
			}
		}
	}

	protected void shutDown() throws Exception
	{
		npcs.clear();
		hiddenNPCs.clear();
		currentWave.clear();
		overlayManager.remove(overlay);
		overlayManager.remove(reminderOverlay);
		hooks.unregisterRenderableDrawListener(drawListener);

		for (InfoHandler info : infoHandlers)
		{
			eventBus.unregister(info);
			info.unload();
		}
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned e)
	{
		if (isInAllowedCaves())
		{
			NPC npc = e.getNpc();
			int tick = client.getTickCount();

			if (npc.getName() != null && (INFERNO_NPC.contains(npc.getName().toLowerCase()) || FIGHT_CAVE_NPC.contains(npc.getName().toLowerCase())))
			{
				try
				{
					if (TzhaarHP.getNPC(npc.getId()) != null)
					{
						int hp = TzhaarHP.getMaxHP(npc.getId()) != 0 ? TzhaarHP.getMaxHP(npc.getId()) : npcManager.getHealth(npc.getId());
						if (hp != 0)
						{
							int currentHp = hp;
							//Do not get half HP for Zuk sets
							if (isInInferno() && waveStarted && waveStartTick != -1 && tick > waveStartTick && REVIVABLE_NPC.contains(npc.getName().toLowerCase())
								&& currentWave.containsKey("inferno") && currentWave.get("inferno") != 69)
							{
								currentHp = TzhaarHP.getRespawnedHP(npc.getId()) != 0 ? TzhaarHP.getRespawnedHP(npc.getId()) : (int) Math.ceil((double) hp / 2);
							}
							npcs.add(new TzhaarNPC(npc, currentHp, hp, tick));
						}
					}
				}
				catch (NullPointerException ignored)
				{
				}
			}
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned e)
	{
		npcs.removeIf(n -> n.getNpc().getIndex() == e.getNpc().getIndex());
		hiddenNPCs.removeIf(h -> h.getNpc().getIndex() == e.getNpc().getIndex());
	}

	@Subscribe
	public void onChatMessage(ChatMessage e)
	{
		if (e.getType() == ChatMessageType.GAMEMESSAGE && isInAllowedCaves())
		{
			final String message = Text.removeTags(e.getMessage());
			if (WAVE_START_PATTERN.matcher(message).matches())
			{
				String cave = isInInferno() ? "inferno" : "fc";
				String wave = message.split(": ")[1];
				currentWave.put(cave, Integer.parseInt(wave));

				waveStarted = true;
				waveStartTick = client.getTickCount();
			}
			else if (TZHAAR_WAVE_COMPLETE.equals(message))
			{
				waveStarted = false;
				waveStartTick = -1;
			}
			else if (message.startsWith(JAD_KC_MESSAGE) || message.startsWith(ZUK_KC_MESSAGE) || message.equals(DEATH_MESSAGE))
			{
				waveStarted = false;
				waveStartTick = -1;
				currentWave.clear();
			}
		}
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded e)
	{
		final MenuEntry menuEntry = e.getMenuEntry();
		String option = Text.removeTags(e.getOption()).toLowerCase();
		String target = Text.removeTags(e.getTarget()).toLowerCase();

		if (config.recolorMenu() && isInAllowedCaves())
		{
			NPC npc = client.getCachedNPCs()[e.getIdentifier()];
			for (TzhaarNPC n : npcs)
			{
				if (npc != null && npc.getName() != null && n.getNpc() == npc && !EXCLUDED_NPC.contains(npc.getName().toLowerCase()))
				{
					Color color = null;
					if (npcUtil.isDying(n.getNpc()) || n.isDead())
					{
						color = config.highlightDeadColor();
					}

					if (color == null && (!npcUtil.isDying(npc) || !n.isDead()))
					{
						color = config.highlightAliveColor();
					}

					if (config.dynamicColor() != TzhaarHPTrackerConfig.DynamicColor.OFF)
					{
						color = getDynamicColor(n, true);
					}

					if (color != null)
					{
						final String tzhaar = ColorUtil.prependColorTag(Text.removeTags(e.getTarget()), color);
						menuEntry.setTarget(tzhaar);
					}
				}
			}
		}

		if (isInAllowedBanks() && (config.spellbookWarning() == TzhaarHPTrackerConfig.spellbookWarningMode.REMOVE
			|| config.spellbookWarning() == TzhaarHPTrackerConfig.spellbookWarningMode.BOTH))
		{
			if ((option.contains("jump-in") && target.contains("the inferno")) || (option.contains("enter") && target.contains("cave entrance")))
			{
				if (!config.spellbookCheck().contains(TzhaarHPTrackerConfig.spellbook.NORMAL) && spellbookType.equals("NORMAL"))
				{
					client.setMenuEntries(Arrays.copyOf(client.getMenuEntries(), client.getMenuEntries().length - 1));
				}
				else if (!config.spellbookCheck().contains(TzhaarHPTrackerConfig.spellbook.ANCIENT) && spellbookType.equals("ANCIENT"))
				{
					client.setMenuEntries(Arrays.copyOf(client.getMenuEntries(), client.getMenuEntries().length - 1));
				}
				else if (!config.spellbookCheck().contains(TzhaarHPTrackerConfig.spellbook.LUNAR) && spellbookType.equals("LUNAR"))
				{
					client.setMenuEntries(Arrays.copyOf(client.getMenuEntries(), client.getMenuEntries().length - 1));
				}
				else if (!config.spellbookCheck().contains(TzhaarHPTrackerConfig.spellbook.ARCEUUS) && spellbookType.equals("ARCEUUS"))
				{
					client.setMenuEntries(Arrays.copyOf(client.getMenuEntries(), client.getMenuEntries().length - 1));
				}
			}
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged e)
	{
		if (e.getGameState() != GameState.LOGGED_IN && e.getGameState() != GameState.LOADING)
		{
			npcs.clear();
			hiddenNPCs.clear();
		}
		else
		{
			if (!isInAllowedCaves())
			{
				if (!currentWave.isEmpty())
				{
					currentWave.clear();
				}

				if (!npcs.isEmpty())
				{
					npcs.clear();
				}

				if (!hiddenNPCs.isEmpty())
				{
					hiddenNPCs.clear();
				}
			}
		}
	}

	@Subscribe
	private void onConfigChanged(ConfigChanged e)
	{
		if (e.getGroup().equals(config.GROUP))
		{
			switch (e.getKey())
			{
				case "overlayFontType":
				case "overlayFontName":
				case "overlayFontSize":
				case "overlayFontWeight":
					loadFont();
					break;
			}
		}
	}

	public Color getDynamicColor(TzhaarNPC n, boolean line)
	{
		if (n.getHp() <= 0)
		{
			return line ? config.highlightDeadColor() : config.fillDeadColor();
		}

		double healthRatio = Math.min(1.0, (double) n.getHp() / n.getMaxHp());
		return ColorUtil.colorLerp(line ? config.highlightDeadColor() : config.fillDeadColor(), line ? config.highlightAliveColor() : config.fillAliveColor(), healthRatio);
	}

	public void loadFont()
	{
		switch (config.overlayFontType())
		{
			case SMALL:
				font = FontManager.getRunescapeSmallFont();
				break;
			case REGULAR:
				font = FontManager.getRunescapeFont();
				break;
			case BOLD:
				font = FontManager.getRunescapeBoldFont();
				break;
			case CUSTOM:
				if (!config.overlayFontName().equals(""))
				{
					font = new Font(config.overlayFontName(), config.overlayFontWeight().getWeight(), config.overlayFontSize());
				}
				break;
		}
	}

	public Collection<GameObject> getCaveEntrances()
	{
		Collection<GameObject> objects = new ArrayList<>();
		Tile[][] tiles = client.getScene().getTiles()[client.getPlane()];
		for (Tile[] tile : tiles)
		{
			for (Tile t : tile)
			{
				if (t != null)
				{
					GameObject[] gameObjects = t.getGameObjects();
					if (gameObjects != null)
					{
						objects.addAll(Arrays.stream(gameObjects).filter(o -> o != null && (o.getId() == 11833 || o.getId() == 30352)
							&& o.getWorldLocation().distanceTo(client.getLocalPlayer().getWorldLocation()) <= 30 && !objects.contains(o)).collect(Collectors.toList()));
					}
				}
			}
		}
		return objects;
	}

	@Subscribe
	public void onGameTick(GameTick e)
	{
		if (isInAllowedCaves())
		{
			long time = System.nanoTime();
			lastTickDurMS = (int) ((time - lastTickNS) / 1000000L);
			lastTickNS = time;

			//Clear Hidden NPCs if lag spike over the set amount
			if (lastTickDurMS >= config.lagProtection())
			{
				hiddenNPCs.clear();
			}
		}

		if (isInAllowedBanks())
		{
			int spellbook = client.getVarbitValue(4070);
			if (spellbook == 0)
			{
				spellbookType = "NORMAL";
			}
			else if (spellbook == 1 && !spellbookType.equals("ANCIENT"))
			{
				spellbookType = "ANCIENT";
			}
			else if (spellbook == 2 && !spellbookType.equals("LUNAR"))
			{
				spellbookType = "LUNAR";
			}
			else if (spellbook == 3 && !spellbookType.equals("ARCEUUS"))
			{
				spellbookType = "ARCEUUS";
			}
		}
	}

	@VisibleForTesting
	boolean shouldDraw(Renderable renderable, boolean drawingUI)
	{
		if (config.hideDead())
		{
			if (renderable instanceof NPC)
			{
				//Excluded NPCs should not be hidden -> too much HP + healing
				return hiddenNPCs.stream().noneMatch(n -> n.getNpc().getIndex() == ((NPC) renderable).getIndex() && n.isDead()
					&& !EXCLUDED_NPC.contains(Objects.requireNonNull(n.getNpc().getName()).toLowerCase()));
			}
		}
		return true;
	}

	public boolean isInAllowedCaves()
	{
		return isInFightCaves() || isInInferno();
	}

	public boolean isInFightCaves()
	{
		return ArrayUtils.contains(client.getMapRegions(), FIGHT_CAVES_REGION);
	}

	public boolean isInInferno()
	{
		return ArrayUtils.contains(client.getMapRegions(), INFERNO_REGION);
	}

	//10063-10065 is inferno bank region
	//9808 is fight caves bank area, 9552 is fight pits area
	public boolean isInAllowedBanks()
	{
		return client.getMapRegions() != null && client.getMapRegions().length > 0 && Arrays.stream(client.getMapRegions()).anyMatch(new ArrayList<>
			(Arrays.asList(9808, 9552, 10063, 10064, 10065))::contains);
	}

	public boolean isInJadChallenge()
	{
		return ArrayUtils.contains(client.getMapRegions(), INFERNO_REGION) && client.getVarbitValue(JAD_CHALLENGE_VAR) == 1;
	}
}
