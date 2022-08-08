package com.coxanalytics.config;

import lombok.Getter;

public enum FontWeight
{
	PLAIN(0),
	BOLD(1),
	ITALIC(2);

	@Getter
	private final int weight;

	FontWeight(int i)
	{
		weight = i;
	}
}
