package com.nexsplits.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TimeStyle
{
	SECONDS("Seconds"),
	TICKS("Precise"),
	VARBIT("In Game Setting");

	private final String name;

	@Override
	public String toString()
	{
		return name;
	}
}
