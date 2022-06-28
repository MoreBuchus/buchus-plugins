package com.togglechat;

import lombok.Getter;

public enum TabMode
{
	ALL("All", 0),
	GAME("Game", 1),
	PUBLIC("Public", 2),
	PRIVATE("Private", 3),
	FC("Friends Chat", 4),
	CLAN("Clan Chat", 5),
	TRADE("Trade", 6);

	@Getter
	private final String name;

	@Getter
	private final int tab;

	TabMode(String name, int tab)
	{
		this.name = name;
		this.tab = tab;
	}

	@Override
	public String toString()
	{
		return name;
	}
}
