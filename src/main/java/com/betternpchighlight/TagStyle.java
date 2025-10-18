package com.betternpchighlight;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TagStyle {
    TILE("Tile"),
    TRUE_TILE("True Tile"),
    SW_TILE("SW Tile"),
    SW_TRUE_TILE("SW True Tile"),
    HULL("Hull"),
    AREA("Area"),
    OUTLINE("Outline"),
    CLICKBOX("Clickbox"),
    TURBO("Turbo");

    private final String name;

    @Override
    public String toString() {
        return name;
    }

    public static TagStyle fromString(String text) {
        for (TagStyle style : TagStyle.values()) {
            if (style.name.equalsIgnoreCase(text)) {
                return style;
            }
        }
        return null;
    }

    public static TagStyle fromAbbreviation(String abbr) {
        switch (abbr.toLowerCase()) {
            case "t":
            case "tile":
                return TILE;
            case "tt":
            case "truetile":
                return TRUE_TILE;
            case "sw":
            case "swt":
            case "swtile":
            case "southwesttile":
            case "southwest":
            case "southwestt":
                return SW_TILE;
            case "swtt":
            case "swtruetile":
            case "southwesttruetile":
            case "southwesttt":
                return SW_TRUE_TILE;
            case "h":
            case "hull":
                return HULL;
            case "a":
            case "area":
                return AREA;
            case "o":
            case "outline":
                return OUTLINE;
            case "c":
            case "clickbox":
            case "box":
                return CLICKBOX;
            case "tu":
            case "turbo":
                return TURBO;
            default:
                return null;
        }
    }
}