package com.nexsplits.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KillTimerMode
{
	OFF("Off"),
	INFOBOX("Infobox"),
	OVERLAY("Overlay Panel"),
	BOTH("Both");

	private final String name;

	@Override
	public String toString()
	{
		return name;
	}
}
