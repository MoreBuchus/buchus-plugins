package com.betternpchighlight.data;

import com.betternpchighlight.ui.NpcCard;
import com.betternpchighlight.TagStyle;
import com.betternpchighlight.ui.NpcCardGroupPanel;
import com.betternpchighlight.ui.BetterNpcHighlightPanel;
import com.betternpchighlight.HighlightInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.ColorScheme;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public final class DataManager {
    private static final String CONFIG_KEY = "groups";
    private final ConfigManager configManager;
    private final Gson gson = new Gson();

    public static final GroupDTO DEFAULT_GROUP;
    private static final UUID DEFAULT_GROUP_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private static final int DEFAULT_RAVE_SPEED = 6000;
    private static final Color DEFAULT_DISPLAY_NAME_COLOR = Color.CYAN;

    static {
        DEFAULT_GROUP = new GroupDTO();
        DEFAULT_GROUP.uuid = DEFAULT_GROUP_UUID.toString();
        DEFAULT_GROUP.name = "Default";
        DEFAULT_GROUP.accentColor = ColorScheme.LIGHT_GRAY_COLOR.getRGB();
        DEFAULT_GROUP.collapsed = false;
        DEFAULT_GROUP.isDefault = true;
        DEFAULT_GROUP.cards = new ArrayList<>();
    }

    public DataManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void saveGroups(String configGroup, List<NpcCardGroupPanel> groups) {
        List<GroupDTO> dtoList = groups.stream()
                .map(this::convertGroupToDto)
                .collect(Collectors.toList());
        configManager.setConfiguration(configGroup, CONFIG_KEY, gson.toJson(dtoList));
    }

    public List<NpcCardGroupPanel> loadGroups(String configGroup, BetterNpcHighlightPanel panel) {
        final String json = configManager.getConfiguration(configGroup, CONFIG_KEY);
        log.debug("Raw JSON loaded for groups: {}", json);
        List<NpcCardGroupPanel> groups = new ArrayList<>();

        if (json != null && !json.isEmpty()) {
            final Type listType = new TypeToken<List<GroupDTO>>() {
            }.getType();
            final List<GroupDTO> dtoList = gson.fromJson(json, listType);
            dtoList.stream()
                    .map(groupDto -> convertGroupFromDto(groupDto, panel))
                    .forEach(groups::add);
        }

        // Ensure the default group always exists
        if (groups.stream().noneMatch(g -> g.getGroupId().equals(DEFAULT_GROUP_UUID))) {
            NpcCardGroupPanel defaultGroup = new NpcCardGroupPanel(
                    DEFAULT_GROUP_UUID,
                    DEFAULT_GROUP.name,
                    panel.getColorPickerManager(),
                    panel,
                    true
            );
            defaultGroup.setAccentColor(new Color(DEFAULT_GROUP.accentColor, true));
            groups.add(defaultGroup);
        }

        return groups;
    }


    private CardDTO convertCardToDto(NpcCard card) {
        CardDTO dto = new CardDTO();
        dto.uuid = card.getCardId().toString();
        dto.name = card.getNameText();

        if (card.getDisplayNameColor() != null) {
            dto.displayNameColor = card.getDisplayNameColor().getRGB();
        } else {
            dto.displayNameColor = DEFAULT_DISPLAY_NAME_COLOR.getRGB();
        }

        dto.hideNpc = card.isHideNpc();
        dto.drawUnder = card.isDrawUnder();
        dto.displayName = card.isDisplayName();
        dto.overrideDisplayNameColor = card.isOverrideDisplayNameColor();
        dto.highlightDead = card.isHighlightDead();

        for (final NpcHighlightEntry e : card.getAllEntries()) {
            // Only create a style DTO if there is a tag style.
            if (e.tagStyle != null) {
                StyleDTO s = new StyleDTO();
                s.tagStyle = e.tagStyle.toString();
                s.outlineColor = e.outlineColor.getRGB();
                s.fillColor = e.fillColor.getRGB();
                s.raveOutline = e.raveOutline;
                s.raveFill = e.raveFill;
                s.raveSpeed = e.raveSpeed;
                s.lineType = e.lineType.name();
                s.outlineWidth = e.outlineWidth;
                s.antiAliasing = e.antiAliasing;
                s.outlineFeather = e.outlineFeather;
                dto.styles.add(s);
            }
        }
        return dto;
    }

    private NpcCard convertCardFromDto(CardDTO cardDto, BetterNpcHighlightPanel panel) {
        final NpcCard card = new NpcCard(panel, panel.getColorPickerManager(), UUID.fromString(cardDto.uuid));
        card.setNameText(cardDto.name);
        card.setHideNpc(cardDto.hideNpc);
        card.setDrawUnder(cardDto.drawUnder);
        card.setDisplayName(cardDto.displayName);
        card.setOverrideDisplayNameColor(cardDto.overrideDisplayNameColor);
        card.setHighlightDead(cardDto.highlightDead);
        card.setDisplayNameColor(new Color(cardDto.displayNameColor, true));

        // Create entries only for styles, and apply general settings from the card DTO.
        if (cardDto.styles != null && !cardDto.styles.isEmpty()) {
            final List<NpcHighlightEntry> entries = cardDto.styles.stream()
                    .map(s -> {
                        final int raveSpeed = s.raveSpeed == 0 ? DEFAULT_RAVE_SPEED : s.raveSpeed;
                        final HighlightInfo.LineType lineType = s.lineType == null
                                ? HighlightInfo.LineType.REGULAR
                                : HighlightInfo.LineType.valueOf(s.lineType);
                        return new NpcHighlightEntry(
                                cardDto.name,
                                TagStyle.fromString(s.tagStyle),
                                new Color(s.outlineColor, true),
                                new Color(s.fillColor, true),
                                cardDto.hideNpc,
                                cardDto.drawUnder,
                                cardDto.displayName,
                                card.getDisplayNameColor(),
                                cardDto.overrideDisplayNameColor,
                                cardDto.highlightDead,
                                s.raveOutline,
                                s.raveFill,
                                raveSpeed,
                                lineType,
                                s.outlineWidth,
                                s.antiAliasing,
                                s.outlineFeather
                        );
                    }).collect(Collectors.toList());

            // Filter out duplicate styles before adding them to the card, keeping the first occurrence.
            final Set<TagStyle> seenStyles = new HashSet<>();
            entries.stream()
                    .filter(e -> e.tagStyle != null && seenStyles.add(e.tagStyle))
                    .forEach(card::addStyleRow);
        }
        return card;
    }

    public GroupDTO convertGroupToDto(NpcCardGroupPanel group) {
        GroupDTO groupDto = new GroupDTO();
        groupDto.uuid = group.getGroupId().toString();
        groupDto.name = group.isDefault() ? DataManager.DEFAULT_GROUP.name : group.getGroupName();
        groupDto.accentColor = group.getAccentColor().getRGB();
        groupDto.collapsed = group.isCollapsed();
        groupDto.isDefault = group.isDefault();

        groupDto.cards = group.getCards().stream()
                .filter(card -> !card.getNameText().isEmpty())
                .map(this::convertCardToDto)
                .collect(Collectors.toList());

        return groupDto;
    }

    public NpcCardGroupPanel convertGroupFromDto(GroupDTO groupDto, BetterNpcHighlightPanel panel) {
        NpcCardGroupPanel group = new NpcCardGroupPanel(
                UUID.fromString(groupDto.uuid),
                groupDto.name,
                panel.getColorPickerManager(),
                panel,
                groupDto.isDefault
        );
        group.setAccentColor(new Color(groupDto.accentColor, true));
        if (groupDto.collapsed) {
            group.toggleCollapse();
        }

        groupDto.cards.stream()
                .map(cardDto -> convertCardFromDto(cardDto, panel))
                .forEach(group::addCard);

        return group;
    }
}
