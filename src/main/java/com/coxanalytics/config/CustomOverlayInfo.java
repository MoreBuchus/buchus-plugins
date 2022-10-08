package com.coxanalytics.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CustomOverlayInfo
{
	TOTAL("Total Points"),
	PERSONAL("Personal Points"),
	ELAPSED("Elapsed Time"),
	FLOOR("Floor Splits"),
	CURRENT("Current Split");

	@Getter
	private final String info;

	@Override
	public String toString()
	{
		return info;
	}
}
