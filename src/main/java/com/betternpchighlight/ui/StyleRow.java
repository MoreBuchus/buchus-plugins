package com.betternpchighlight.ui;

import com.betternpchighlight.BetterNpcHighlightPanel;
import com.betternpchighlight.HighlightColor;
import com.betternpchighlight.data.NpcHighlightEntry;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;

import javax.swing.*;
import java.awt.*;

public class StyleRow extends JPanel {
    JComboBox<String> tagStyleCombo;
    ColorPreviewButton colorPreviewButton; // Combined button
    JButton addButton;
    JButton removeButton;
    private final NpcCard npcCard;
    private final ConfigManager configManager;
    private final ColorPickerManager colorPickerManager;

    public StyleRow(NpcCard npcCard, ConfigManager configManager, ColorPickerManager colorPickerManager, NpcHighlightEntry entry) {
        super(new BorderLayout(0, 0));
        this.npcCard = npcCard;
        this.configManager = configManager;
        this.colorPickerManager = colorPickerManager;
        setBackground(ColorScheme.DARKER_GRAY_COLOR);
        setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        // Left side: tag style combo and combined color button
        JPanel leftPanel = new JPanel(new BorderLayout(4, 0));
        leftPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        tagStyleCombo = new JComboBox<>();
        styleComboBox(tagStyleCombo);
        tagStyleCombo.addActionListener(e -> npcCard.panel.saveAllCards(configManager, "betterNpcHighlight"));
        leftPanel.add(tagStyleCombo, BorderLayout.CENTER);

        // Initialize combined color button with outline and fill colors
        Color initialOutline = entry != null ? entry.outlineColor : Color.CYAN;
        Color initialFill = entry != null ? entry.fillColor : new Color(0, 255, 255, 20);

        // Get rave settings from entry
        boolean initialRaveOutline = entry != null && entry.raveOutline;
        boolean initialRaveFill = entry != null && entry.raveFill;
        int initialRaveSpeed = entry != null ? entry.raveSpeed : 6000;
        HighlightColor.TileStyle initialTileStyle = entry != null ? entry.tileStyle : HighlightColor.TileStyle.REGULAR;
        double initialOutlineWidth = entry != null ? entry.outlineWidth : 2.0;
        boolean initialAntiAliasing = entry != null ? entry.antiAliasing : true;
        int initialOutlineFeather = entry != null ? entry.outlineFeather : 2;

        CheckerboardPanel checkerPanel = new CheckerboardPanel();
        colorPreviewButton = new ColorPreviewButton(initialOutline, initialFill, initialRaveOutline, initialRaveFill, initialRaveSpeed, initialTileStyle,
                initialOutlineWidth, initialAntiAliasing, initialOutlineFeather, colorPickerManager, npcCard.panel.getPlugin(), configManager);
        colorPreviewButton.setOpaque(false);
        checkerPanel.add(colorPreviewButton, BorderLayout.CENTER);
        leftPanel.add(checkerPanel, BorderLayout.EAST);

        add(leftPanel, BorderLayout.CENTER);

        // Right side: ADD and REMOVE buttons
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        addButton = new JButton();
        addButton.setPreferredSize(new Dimension(18, 24));
        addButton.setContentAreaFilled(false);
        addButton.setIcon(BetterNpcHighlightPanel.ADD_STYLE_ICONS.on);
        addButton.setRolloverIcon(BetterNpcHighlightPanel.ADD_STYLE_ICONS.onHover);
        addButton.setToolTipText("Add a new highlight style below");
        addButton.addActionListener(e -> {
            if (npcCard.getStyleRows().size() < BetterNpcHighlightPanel.MAX_STYLE_ROWS) {
                int index = npcCard.getStyleRows().indexOf(this);
                if (index != -1) {
                    npcCard.addStyleRowAt(index + 1, null);
                }
            }
        });

        removeButton = new JButton();
        removeButton.setPreferredSize(new Dimension(18, 24));
        removeButton.setContentAreaFilled(false);
        removeButton.setIcon(BetterNpcHighlightPanel.REMOVE_STYLE_ICONS.on);
        removeButton.setRolloverIcon(BetterNpcHighlightPanel.REMOVE_STYLE_ICONS.onHover);
        removeButton.setToolTipText("Remove this highlight style");
        removeButton.addActionListener(e -> {
            if (npcCard.getStyleRows().size() > 1) {
                int index = npcCard.getStyleRows().indexOf(this);
                if (index != -1) {
                    npcCard.removeStyleRowAt(index);
                }
            }
        });
        rightPanel.add(Box.createHorizontalStrut(46));
        rightPanel.add(removeButton);
        rightPanel.add(addButton);

        add(rightPanel, BorderLayout.EAST);

        // Initialize combo box options
        npcCard.updateTagStyleComboBoxOptions(tagStyleCombo);
        tagStyleCombo.addActionListener(e -> npcCard.refreshAllTagStyleComboBoxes());
        if (entry != null && entry.tagStyle != null) {
            tagStyleCombo.setSelectedItem(entry.tagStyle);
        }
        // Add listener to save changes when selection changes
        tagStyleCombo.addActionListener(e -> {
            npcCard.refreshAllTagStyleComboBoxes();
            npcCard.panel.saveAllCards(configManager, "betterNpcHighlight");  // Save on change
        });

        npcCard.updateStyleButtons();
    }

    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setBackground(ColorScheme.DARK_GRAY_COLOR);
        comboBox.setForeground(ColorScheme.TEXT_COLOR);
        comboBox.setFont(net.runelite.client.ui.FontManager.getRunescapeSmallFont());
        comboBox.setPreferredSize(new Dimension(114, 24));
        comboBox.setMaximumSize(new Dimension(114, 24));
    }
}
