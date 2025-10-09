package com.betternpchighlight.data;

import java.util.ArrayList;
import java.util.List;

public class CardDTO {
    public String uuid;
    public String name;
    public int displayNameColor; // ARGB int
    public boolean overrideDisplayNameColor;
    public boolean hideNpc;
    public boolean drawUnder;
    public boolean displayName;
    public boolean highlightDead;
    public List<StyleDTO> styles = new ArrayList<>();
}
