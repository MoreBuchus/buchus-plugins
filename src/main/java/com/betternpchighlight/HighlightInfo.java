package com.betternpchighlight;

import lombok.Getter;
import lombok.Setter;

import java.awt.*;

@Getter
@Setter
public class HighlightInfo {
    public enum LineType {
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
    private LineType lineType;
    double outlineWidth;
    boolean antiAliasing;
    int outlineFeather;

    public HighlightInfo(boolean isHighlight, Color color, Color fill) {
        this(isHighlight, color, fill, false, false, 6000, LineType.REGULAR, 2.0, true, 2); // Default values
    }

    public HighlightInfo(boolean isHighlight, Color color, Color fill, boolean raveOutline, boolean raveFill, int raveSpeed, LineType lineType, double outlineWidth, boolean antiAliasing, int outlineFeather) {
        this.isHighlight = isHighlight;
        this.color = color;
        this.fill = fill;
        this.raveOutline = raveOutline;
        this.raveFill = raveFill;
        this.raveSpeed = raveSpeed;
        this.lineType = lineType;
        this.outlineWidth = outlineWidth;
        this.antiAliasing = antiAliasing;
        this.outlineFeather = outlineFeather;
    }
}