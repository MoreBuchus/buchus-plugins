package com.nexsplits.config;

import java.awt.Font;
import lombok.Getter;

@Getter
public enum FontWeight
{
	PLAIN(Font.PLAIN),
	BOLD(Font.BOLD),
	ITALIC(Font.ITALIC),
	BOLD_ITALIC(Font.BOLD | Font.ITALIC);

	private final int weight;

	FontWeight(int i)
	{
		weight = i;
	}
}
