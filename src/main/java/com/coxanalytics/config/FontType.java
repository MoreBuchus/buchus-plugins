package com.coxanalytics.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FontType
{
	SMALL("RS Small"),
	REGULAR("RS Regular"),
	BOLD("RS Bold"),
	CUSTOM("Custom");

	private final String name;

	@Override
	public String toString()
	{
		return name;
	}
}
