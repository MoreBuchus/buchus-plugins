package com.nexsplits.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CustomOverlayInfo
{
	PLAYERS("Players"),
	ELAPSED("Elapsed Time"),
	PHASE("Phase Splits"),
	CURRENT("Current Split");

	private final String info;

	@Override
	public String toString()
	{
		return info;
	}
}
