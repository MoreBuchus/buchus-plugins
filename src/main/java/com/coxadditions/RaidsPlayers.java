package com.coxadditions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@AllArgsConstructor
@Getter
@Setter
public class RaidsPlayers
{
	private String player;
	private long id;
	private boolean ovlActive = false;
	private boolean enhActive = false;
	private int ovlTicks;
	private int enhTicks;

	public RaidsPlayers(String player, long id)
	{
		this.player = player;
		this.id = id;
	}

	public void updatePotStatus(String pot, int ticks)
	{
		if (pot.equals("OVL"))
		{
			ovlActive = true;
			ovlTicks = ticks;
		}
		else if (pot.equals("ENH"))
		{
			enhActive = true;
			enhTicks = ticks;
		}
	}
}
