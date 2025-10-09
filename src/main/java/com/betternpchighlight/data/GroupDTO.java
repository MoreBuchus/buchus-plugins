package com.betternpchighlight.data;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class GroupDTO {
    public String uuid;
    public String name;
    public int accentColor;
    public boolean collapsed;
    public boolean isDefault;
    public List<CardDTO> cards = new ArrayList<>();
}
