package com.coxanalytics.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BackgroundMode
{
	STANDARD("Default"),
	CUSTOM("Custom"),
	HIDE("None");

	private final String name;

	@Override
	public String toString()
	{
		return name;
	}
}
