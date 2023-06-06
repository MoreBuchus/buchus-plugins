package com.nexsplits.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PhaseNameTypeMode
{
	NUMBER("Number"),
	NAME("Name");

	private final String name;

	@Override
	public String toString()
	{
		return name;
	}
}
