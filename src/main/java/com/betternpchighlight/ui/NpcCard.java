package com.betternpchighlight.ui;

import com.betternpchighlight.BetterNpcHighlightPanel;
import com.betternpchighlight.data.NpcHighlightEntry;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;
import net.runelite.client.ui.components.colorpicker.RuneliteColorPicker;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class NpcCard extends JPanel {
    private JPanel bottomRowsPanel;
    private final List<StyleRow> styleRows = new ArrayList<>();
    private JTextField nameField;
    private JComboBox<String> tagStyleCombo;
    private ColorPreviewButton colorPreviewButton;
    private JToggleButton hideNpcButton;
    private JToggleButton drawUnderButton;
    private JToggleButton displayNameButton;
    private Color displayNameColor = null;
    private JToggleButton highlightDeadButton;
    private final UUID cardId;
    public final BetterNpcHighlightPanel panel;
    private final ConfigManager configManager;
    private final ColorPickerManager colorPickerManager;

    public NpcCard(BetterNpcHighlightPanel panel, ConfigManager configManager, ColorPickerManager colorPickerManager) {
        this.cardId = UUID.randomUUID();
        this.panel = panel;
        this.configManager = configManager;
        this.colorPickerManager = colorPickerManager;
        initCard();
    }

    public NpcCard(BetterNpcHighlightPanel panel, ConfigManager configManager, ColorPickerManager colorPickerManager, UUID id) {
        this.cardId = id;
        this.panel = panel;
        this.configManager = configManager;
        this.colorPickerManager = colorPickerManager;
        initCard();
    }

    public UUID getCardId() {
        return cardId;
    }

    private void initCard() {
        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARKER_GRAY_COLOR);
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        // Main content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        // Top row: Name field and tag style dropdown
        JPanel topRow = new JPanel(new BorderLayout(4, 0));
        topRow.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        // Name field (takes most space)
        nameField = createStyledTextField("Name/ID..");
        nameField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                panel.saveAllCards(configManager, "betterNpcHighlight");
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                panel.saveAllCards(configManager, "betterNpcHighlight");
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                panel.saveAllCards(configManager, "betterNpcHighlight");
            }
        });


        topRow.add(nameField, BorderLayout.CENTER);

        // Panel for toggle buttons
        JPanel togglePanel = new JPanel();
        togglePanel.setLayout(new BoxLayout(togglePanel, BoxLayout.X_AXIS));
        togglePanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        topRow.add(togglePanel, BorderLayout.EAST);

        // Create toggle buttons with icons
        hideNpcButton = createToggleButton(
                BetterNpcHighlightPanel.ENTITY_HIDER_ICONS.on,
                BetterNpcHighlightPanel.ENTITY_HIDER_ICONS.off,
                BetterNpcHighlightPanel.ENTITY_HIDER_ICONS.onHover,
                BetterNpcHighlightPanel.ENTITY_HIDER_ICONS.offHover,
                "Do not hide NPC", "Hide NPC"
        );
        drawUnderButton = createToggleButton(
                BetterNpcHighlightPanel.DRAW_BENEATH_ICONS.on,
                BetterNpcHighlightPanel.DRAW_BENEATH_ICONS.off,
                BetterNpcHighlightPanel.DRAW_BENEATH_ICONS.onHover,
                BetterNpcHighlightPanel.DRAW_BENEATH_ICONS.offHover,
                "Do not draw overlay beneath NPC", "Draw overlay beneath NPC"
        );

        displayNameButton = createToggleButton(
                BetterNpcHighlightPanel.DISPLAY_NAME_ICONS.on,
                BetterNpcHighlightPanel.DISPLAY_NAME_ICONS.off,
                BetterNpcHighlightPanel.DISPLAY_NAME_ICONS.onHover,
                BetterNpcHighlightPanel.DISPLAY_NAME_ICONS.offHover,
                "Do not display name above NPC", "Display name above NPC"
        );
        highlightDeadButton = createToggleButton(
                BetterNpcHighlightPanel.HIGHLIGHT_DEAD_ICONS.on,
                BetterNpcHighlightPanel.HIGHLIGHT_DEAD_ICONS.off,
                BetterNpcHighlightPanel.HIGHLIGHT_DEAD_ICONS.onHover,
                BetterNpcHighlightPanel.HIGHLIGHT_DEAD_ICONS.offHover,
                "Do not highlight dead NPC", "Highlight dead NPC"
        );

        hideNpcButton.addItemListener(e -> {
            panel.saveAllCards(configManager, "betterNpcHighlight");
        });

        drawUnderButton.addItemListener(e -> {
            panel.saveAllCards(configManager, "betterNpcHighlight");
        });

        displayNameButton.addItemListener(e -> {
            panel.saveAllCards(configManager, "betterNpcHighlight");
        });


        displayNameButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showDisplayNameColorMenu(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showDisplayNameColorMenu(e);
                }
            }
        });

        togglePanel.add(hideNpcButton);
        togglePanel.add(Box.createHorizontalStrut(4));

        togglePanel.add(drawUnderButton);
        togglePanel.add(Box.createHorizontalStrut(4));

        togglePanel.add(displayNameButton);
        togglePanel.add(Box.createHorizontalStrut(4));

        togglePanel.add(highlightDeadButton);
        togglePanel.add(Box.createHorizontalStrut(4));

        // Remove NPC card button
        JButton removeCardButton = new JButton();
        removeCardButton.setIcon(BetterNpcHighlightPanel.DELETE_ICONS.on);
        removeCardButton.setRolloverIcon(BetterNpcHighlightPanel.DELETE_ICONS.onHover);
        removeCardButton.setPreferredSize(new Dimension(18, 18));
        removeCardButton.setContentAreaFilled(false);
        removeCardButton.setToolTipText("Delete this NPC entry");
        removeCardButton.addActionListener(e -> panel.removeCard(this));
        togglePanel.add(removeCardButton);

        // Bottom rows panel to hold all StyleRows
        bottomRowsPanel = new JPanel();
        bottomRowsPanel.setLayout(new BoxLayout(bottomRowsPanel, BoxLayout.Y_AXIS));
        bottomRowsPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        bottomRowsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        addStyleRow(null);
        updateStyleButtons();

        // Add spacing and components
        contentPanel.add(topRow);
        contentPanel.add(bottomRowsPanel);

        add(contentPanel, BorderLayout.CENTER);

    }

    public void addStyleRow(NpcHighlightEntry entry) {
        StyleRow row = new StyleRow(this, configManager, colorPickerManager, entry);
        styleRows.add(row);
        bottomRowsPanel.add(row);
        updateStyleButtons();
        bottomRowsPanel.revalidate();
        bottomRowsPanel.repaint();
    }

    public void clearStyleRows() {
        styleRows.clear();
        bottomRowsPanel.removeAll();
        bottomRowsPanel.revalidate();
        bottomRowsPanel.repaint();
    }

    public void addStyleRowAt(int index, NpcHighlightEntry entry) {
        StyleRow row = new StyleRow(this, configManager, colorPickerManager, entry);
        styleRows.add(index, row);
        bottomRowsPanel.add(row, index);
        updateStyleButtons();
        bottomRowsPanel.revalidate();
        bottomRowsPanel.repaint();
        panel.saveAllCards(configManager, "betterNpcHighlight");
    }

    public void removeStyleRowAt(int index) {
        StyleRow row = styleRows.remove(index);
        bottomRowsPanel.remove(row);
        updateStyleButtons();
        bottomRowsPanel.revalidate();
        bottomRowsPanel.repaint();
        panel.saveAllCards(configManager, "betterNpcHighlight");
    }

    private void showDisplayNameColorMenu(MouseEvent e) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem changeColor = new JMenuItem("Change name/minimap color");
        changeColor.addActionListener(ev -> openDisplayNameColorPicker());
        menu.add(changeColor);

        JMenuItem resetColor = new JMenuItem("Reset name/minimap color");
        resetColor.addActionListener(ev -> {
            displayNameColor = null; // Reset to default behavior
            panel.saveAllCards(configManager, "betterNpcHighlight");
            if (panel.getOnDataChanged() != null) {
                panel.getOnDataChanged().run();
            }
        });
        menu.add(resetColor);

        menu.show(displayNameButton, e.getX(), e.getY());
    }

    private void openDisplayNameColorPicker() {
        RuneliteColorPicker picker = colorPickerManager.create(
                SwingUtilities.getWindowAncestor(this),
                displayNameColor != null ? displayNameColor : Color.CYAN, // Use CYAN as default in picker if null
                "Name/Minimap color",
                true
        );
        Point loc = getLocationOnScreen();
        picker.setLocation(loc.x + -400, loc.y);
        picker.setOnColorChange(newColor -> {
            displayNameColor = newColor; // This explicitly sets the color
            if (panel.getOnDataChanged() != null) {
                panel.getOnDataChanged().run();
            }
        });
        picker.setVisible(true);
    }


    public List<NpcHighlightEntry> getAllEntries() {
        List<NpcHighlightEntry> entries = new ArrayList<>();
        String npcNameOrId = getNameText().trim();
        if (npcNameOrId.isEmpty()) {
            return entries;
        }

        boolean hide = hideNpcButton.isSelected();
        boolean drawUnder = drawUnderButton.isSelected();
        boolean displayName = displayNameButton.isSelected();
        boolean highlightDead = highlightDeadButton.isSelected();

        for (StyleRow row : styleRows) {
            String tagStyle = (String) row.tagStyleCombo.getSelectedItem();
            if (tagStyle == null || tagStyle.isEmpty()) {
                continue;
            }
            NpcHighlightEntry entry = new NpcHighlightEntry(
                    npcNameOrId,
                    tagStyle,
                    row.colorPreviewButton.getOutlineColor(),
                    row.colorPreviewButton.getFillColor(),
                    hide,
                    drawUnder,
                    displayName,
                    this.displayNameColor, // This can now be null
                    highlightDead,
                    row.colorPreviewButton.isRaveOutline(),
                    row.colorPreviewButton.isRaveFill(),
                    row.colorPreviewButton.getRaveSpeed(),
                    row.colorPreviewButton.getTileStyle(),
                    row.colorPreviewButton.getOutlineWidth(),
                    row.colorPreviewButton.isAntiAliasing(),
                    row.colorPreviewButton.getOutlineFeather()
            );
            entries.add(entry);
        }
        return entries;
    }


    @Override
    public Dimension getMaximumSize()
    {
        Dimension prefSize = super.getPreferredSize();
        return new Dimension(Integer.MAX_VALUE, prefSize.height);
    }

    private JTextField createStyledTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setBackground(ColorScheme.DARK_GRAY_COLOR);
        field.setForeground(Color.WHITE);
        field.setFont(FontManager.getRunescapeFont());
        field.setCaretColor(Color.WHITE);
        field.setPreferredSize(new Dimension(Integer.MAX_VALUE, 24));
        field.setMargin(new Insets(5,5,5,5));

        // Placeholder text functionality
        field.setText(placeholder);
        field.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.WHITE);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
                }
            }
        });

        return field;
    }

    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setBackground(ColorScheme.DARK_GRAY_COLOR);
        comboBox.setForeground(Color.WHITE);
        comboBox.setFont(FontManager.getRunescapeSmallFont());
        comboBox.setPreferredSize(new Dimension(88, 24));
        comboBox.setMaximumSize(new Dimension(88, 24));
    }

    private JToggleButton createToggleButton(ImageIcon iconOn, ImageIcon iconOff, ImageIcon iconOnHover, ImageIcon iconOffHover, String tooltipOn, String tooltipOff) {
        JToggleButton button = new JToggleButton();
        button.setPreferredSize(new Dimension(18, 18));
        button.setSelectedIcon(iconOn);
        button.setRolloverSelectedIcon(iconOnHover);
        button.setIcon(iconOff);
        button.setRolloverIcon(iconOffHover);
        button.setBorderPainted(true);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);

        // Set tooltip based on current state
        button.setToolTipText(button.isSelected() ? tooltipOn : tooltipOff);

        // Update background and tooltip on toggle
        button.addItemListener(e -> {
            if (button.isSelected()) {
                button.setToolTipText(tooltipOn);
                button.setPressedIcon(iconOnHover);
            } else {
                button.setBackground(ColorScheme.DARKER_GRAY_COLOR);
                button.setPressedIcon(iconOffHover);
                button.setToolTipText(tooltipOff);
            }
        });


        return button;
    }

    public List<StyleRow> getStyleRows() {
        return styleRows;
    }

    public void updateStyleButtons() {
        boolean canRemove = styleRows.size() > 1;
        boolean canAdd = styleRows.size() < BetterNpcHighlightPanel.MAX_STYLE_ROWS;

        for (StyleRow row : styleRows) {
            row.removeButton.setEnabled(canRemove);
            row.removeButton.setVisible(canRemove);

            row.addButton.setEnabled(canAdd);
            row.addButton.setVisible(true);

            if (!canAdd) {
                row.addButton.setToolTipText("Maximum of " + BetterNpcHighlightPanel.MAX_STYLE_ROWS + " highlight styles allowed");
            } else {
                row.addButton.setToolTipText("Add a new highlight style below");
            }
        }
    }


    public void updateTagStyleComboBoxOptions(JComboBox<String> comboBoxToUpdate) {
        Set<String> selectedStyles = new HashSet<>();

        // Gather selected styles from all other combo boxes
        for (Component c : bottomRowsPanel.getComponents()) {
            if (c instanceof JPanel) {
                JPanel row = (JPanel) c;
                for (Component comp : row.getComponents()) {
                    if (comp instanceof JComboBox && comp != comboBoxToUpdate) {
                        JComboBox<?> cb = (JComboBox<?>) comp;
                        Object selected = cb.getSelectedItem();
                        if (selected != null) {
                            selectedStyles.add(selected.toString());
                        }
                    }
                }
            }
        }

        Object currentSelection = comboBoxToUpdate.getSelectedItem();
        comboBoxToUpdate.removeAllItems();

        for (String style : BetterNpcHighlightPanel.TAG_STYLES) {
            if (!selectedStyles.contains(style) || style.equals(currentSelection)) {
                comboBoxToUpdate.addItem(style);
            }
        }

        if (currentSelection != null) {
            comboBoxToUpdate.setSelectedItem(currentSelection);
        }
    }


    public void refreshAllTagStyleComboBoxes() {
        for (Component c : bottomRowsPanel.getComponents()) {
            if (c instanceof JPanel) {
                for (Component comp : ((JPanel) c).getComponents()) {
                    if (comp instanceof JComboBox) {
                        updateTagStyleComboBoxOptions((JComboBox<String>) comp);
                    }
                }
            }
        }
    }

    // Getters and setters
    public void setNameText(String text) {
        if (text == null || text.trim().isEmpty()) {
            nameField.setText("Name/ID..");
            nameField.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
        } else {
            nameField.setText(text);
            nameField.setForeground(Color.WHITE);
        }
    }

    public String getNameText() {
        String text = nameField.getText();
        return text.equals("Name/ID..") ? "" : text;
    }

    public String getTagStyle() {
        return (String) tagStyleCombo.getSelectedItem();
    }

    public String getAllTagStyles() {
        StringBuilder sb = new StringBuilder();
        for (StyleRow row : styleRows) {
            Object sel = row.tagStyleCombo.getSelectedItem();
            if (sel != null) {
                sb.append(sel.toString()).append(" ");
            }
        }
        return sb.toString().trim();
    }

    public Color getOutlineColor() {
        return colorPreviewButton.getOutlineColor();
    }

    public Color getFillColor() {
        return colorPreviewButton.getFillColor();
    }

    public boolean isHideNpc() {
        return hideNpcButton.isSelected();
    }

    public void setHideNpc(boolean selected) {
        hideNpcButton.setSelected(selected);
        panel.saveAllCards(configManager, "betterNpcHighlight");
    }

    public boolean isDrawUnder() {
        return drawUnderButton.isSelected();
    }

    public void setDrawUnder(boolean selected) {
        drawUnderButton.setSelected(selected);
        panel.saveAllCards(configManager, "betterNpcHighlight");
    }

    public boolean isDisplayName() {
        return displayNameButton.isSelected();
    }

    public void setDisplayName(boolean selected) {
        displayNameButton.setSelected(selected);
        panel.saveAllCards(configManager, "betterNpcHighlight");
    }

    public boolean isHighlightDead() {
        return highlightDeadButton.isSelected();
    }

    public void setHighlightDead(boolean selected) {
        highlightDeadButton.setSelected(selected);
        panel.saveAllCards(configManager, "betterNpcHighlight");
    }

    public Color getDisplayNameColor() {
        return displayNameColor;
    }

    public void setDisplayNameColor(Color color) {
        this.displayNameColor = color;
        panel.saveAllCards(configManager, "betterNpcHighlight");
    }

    public void setData(List<NpcHighlightEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            setNameText("");
            clearStyleRows();
            addStyleRow(null);
            hideNpcButton.setSelected(false);
            drawUnderButton.setSelected(false);
            displayNameButton.setSelected(false);
            highlightDeadButton.setSelected(false);

            return;
        }

        setNameText(entries.get(0).nameOrId);
        clearStyleRows();

        for (NpcHighlightEntry entry : entries) {
            addStyleRow(entry);
        }

        // Set toggles from first entry (assuming consistent)
        hideNpcButton.setSelected(entries.get(0).hideNpc);
        drawUnderButton.setSelected(entries.get(0).drawUnder);
        displayNameButton.setSelected(entries.get(0).displayName);
        highlightDeadButton.setSelected(entries.get(0).highlightDead);
        setDisplayNameColor(entries.get(0).displayNameColor);
    }

    public void focusNameField() {
        nameField.requestFocus();
    }
}
