package com.betternpchighlight;

import java.awt.Color;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HighlightColor
{
	public enum TileStyle
	{
		REGULAR,
		DASHED,
		CORNER
	}

	boolean isHighlight;
	Color color;
	Color fill;
	boolean raveOutline;
	boolean raveFill;
	int raveSpeed;
	private TileStyle tileStyle;
	// New fields
	double outlineWidth;
	boolean antiAliasing;
	int outlineFeather;

	public HighlightColor(boolean isHighlight, Color color, Color fill) {
		this(isHighlight, color, fill, false, false, 6000, TileStyle.REGULAR, 2.0, true, 2); // Default values
	}

	public HighlightColor(boolean isHighlight, Color color, Color fill, boolean raveOutline, boolean raveFill, int raveSpeed) {
		this(isHighlight, color, fill, raveOutline, raveFill, raveSpeed, TileStyle.REGULAR, 2.0, true, 2); // Default values
	}

	public HighlightColor(boolean isHighlight, Color color, Color fill, boolean raveOutline, boolean raveFill, int raveSpeed, TileStyle tileStyle) {
		this(isHighlight, color, fill, raveOutline, raveFill, raveSpeed, tileStyle, 2.0, true, 2); // Default values
	}

	public HighlightColor(boolean isHighlight, Color color, Color fill, boolean raveOutline, boolean raveFill, int raveSpeed, TileStyle tileStyle, double outlineWidth, boolean antiAliasing, int outlineFeather) {
		this.isHighlight = isHighlight;
		this.color = color;
		this.fill = fill;
		this.raveOutline = raveOutline;
		this.raveFill = raveFill;
		this.raveSpeed = raveSpeed;
		this.tileStyle = tileStyle;
		this.outlineWidth = outlineWidth;
		this.antiAliasing = antiAliasing;
		this.outlineFeather = outlineFeather;
	}
}