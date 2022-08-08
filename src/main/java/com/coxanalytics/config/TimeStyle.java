package com.coxanalytics.config;

public enum TimeStyle
{
	SECONDS("Seconds"),
	TICKS("Precise"),
	VARBIT("In Game Setting");

	private final String name;

	public String toString()
	{
		return name;
	}

	TimeStyle(String name)
	{
		this.name = name;
	}
}
