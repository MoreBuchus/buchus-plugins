package com.betternpchighlight.ui;

import com.betternpchighlight.BetterNpcHighlightPlugin;

import com.betternpchighlight.TagStyle;
import com.betternpchighlight.data.NpcHighlightEntry;
import com.betternpchighlight.ui.dropdownbutton.DropDownButtonFactory;
import lombok.Getter;
import lombok.Setter;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;
import net.runelite.client.ui.components.colorpicker.RuneliteColorPicker;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class NpcCard extends JPanel {

    // Constants
    private static final String PLACEHOLDER_TEXT = "Name/ID...";
    private static final int COMPONENT_SIZE = 24;
    private static final int BORDER_PADDING = 4;
    private static final int HEADER_TOP_PADDING = 6;

    // Dependencies
    @Getter
    private final UUID cardId;
    @Getter
    private final BetterNpcHighlightPanel panel;
    private final ColorPickerManager colorPickerManager;

    // UI Components - Header
    private final JPanel topRow;
    private JLabel nameLabel;
    private JTextField nameEditor;
    private JButton menuButton;
    private JButton editNameButton;
    private JButton addStyleButton;
    private JButton saveButton;
    private JButton cancelButton;
    private String originalNameText;

    // UI Components - Menu
    private JCheckBoxMenuItem drawUnderItem;
    private JMenu displayNameMenu;
    private JCheckBoxMenuItem displayNameItem;
    private JCheckBoxMenuItem overrideDisplayNameColorItem;
    private JCheckBoxMenuItem highlightDeadItem;
    private JCheckBoxMenuItem entityHiderItem;

    // UI Components - Style Rows
    private JPanel styleRowsPanel;
    private final List<StyleRow> styleRows = new ArrayList<>();

    // State
    @Setter
    @Getter
    private NpcCardGroupPanel parentGroup;
    @Setter
    @Getter
    private Color displayNameColor = null;

    // Constructors
    public NpcCard(BetterNpcHighlightPanel panel, ColorPickerManager colorPickerManager) {
        this(panel, colorPickerManager, UUID.randomUUID());
    }

    public NpcCard(BetterNpcHighlightPanel panel, ColorPickerManager colorPickerManager, UUID id) {
        this.cardId = id;
        this.panel = panel;
        this.colorPickerManager = colorPickerManager;
        this.topRow = new JPanel();

        initializeCard();
    }

    // Initialization Methods
    private void initializeCard() {
        setupCardLayout();

        JPanel contentPanel = createContentPanel();
        createHeaderComponents();
        createStyleRowsPanel();

        contentPanel.add(topRow);
        contentPanel.add(styleRowsPanel);
        add(contentPanel, BorderLayout.CENTER);
    }

    private void setupCardLayout() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(HEADER_TOP_PADDING, 0, 0, 0));
        setFocusable(true);
    }

    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(BORDER_PADDING, BORDER_PADDING, 0, BORDER_PADDING));
        contentPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        return contentPanel;
    }

    private void createHeaderComponents() {
        topRow.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        topRow.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        topRow.setLayout(new BorderLayout());

        createNameLabel();
        topRow.add(nameLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 1, 0));
        buttonPanel.setBackground(null);

        editNameButton = createEditNameButton();
        buttonPanel.add(editNameButton);

        saveButton = createSaveButton();
        buttonPanel.add(saveButton);

        cancelButton = createCancelButton();
        buttonPanel.add(cancelButton);

        addStyleButton = createAddStyleButton();
        buttonPanel.add(addStyleButton);

        JButton menuButton = createMenuButton();
        buttonPanel.add(menuButton);

        topRow.add(buttonPanel, BorderLayout.EAST);
    }

    private void createNameLabel() {
        nameLabel = new JLabel(PLACEHOLDER_TEXT);
        styleNameLabel(nameLabel, true);
        nameLabel.addMouseListener(new NameLabelMouseListener());
    }

    private void styleNameLabel(JLabel label, boolean isPlaceholder) {
        Color foregroundColor = isPlaceholder ? ColorScheme.MEDIUM_GRAY_COLOR : Color.WHITE;
        label.setForeground(foregroundColor);
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 0, ColorScheme.DARK_GRAY_COLOR),
                BorderFactory.createEmptyBorder(2, 5, 0, 5)
        ));
        label.setOpaque(true);
        label.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        label.setPreferredSize(new Dimension(0, COMPONENT_SIZE));
        label.setMaximumSize(new Dimension(Integer.MAX_VALUE, COMPONENT_SIZE));
    }

    private JButton createSaveButton() {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(COMPONENT_SIZE, COMPONENT_SIZE));
        button.setIcon(NpcCardGroupPanel.SAVE_EDIT_GROUPNAME_ICONS.getOn());
        button.setRolloverIcon(NpcCardGroupPanel.SAVE_EDIT_GROUPNAME_ICONS.getOnHover());
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
                    finishEditingName();
                }
            }
        });
        button.setToolTipText("Save");
        button.setVisible(false);
        BetterNpcHighlightPanel.styleButton(button);
        return button;
    }

    private JButton createCancelButton() {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(COMPONENT_SIZE, COMPONENT_SIZE));
        button.setIcon(NpcCardGroupPanel.CANCEL_EDIT_GROUPNAME_ICONS.getOn());
        button.setRolloverIcon(NpcCardGroupPanel.CANCEL_EDIT_GROUPNAME_ICONS.getOnHover());
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
                    cancelEditingName();
                }
            }
        });
        button.setToolTipText("Cancel");
        button.setVisible(false);
        BetterNpcHighlightPanel.styleButton(button);
        return button;
    }

    private JButton createEditNameButton() {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(COMPONENT_SIZE, COMPONENT_SIZE));
        button.setIcon(NpcCardGroupPanel.EDIT_GROUPNAME_ICONS.getOn());
        button.setRolloverIcon(NpcCardGroupPanel.EDIT_GROUPNAME_ICONS.getOnHover());
        button.addActionListener(e -> startEditingName());
        button.setToolTipText("Edit name/id");
        BetterNpcHighlightPanel.styleButton(button);
        return button;
    }

    private JButton createAddStyleButton() {
        JPopupMenu addStyleMenu = new JPopupMenu();
        JButton button = DropDownButtonFactory.createDropDownButton(StyleRow.ADD_STYLE_ICONS.getOn(), addStyleMenu);
        button.setMinimumSize(new Dimension(COMPONENT_SIZE, COMPONENT_SIZE));
        button.setMaximumSize(new Dimension(COMPONENT_SIZE, COMPONENT_SIZE));
        button.setPreferredSize(new Dimension(COMPONENT_SIZE, COMPONENT_SIZE));
        button.setRolloverIcon(StyleRow.ADD_STYLE_ICONS.getOnHover());
        button.setToolTipText("Add a new highlight style");
        BetterNpcHighlightPanel.styleButton(button);

        // Add a placeholder item to make the DropDownButtonFactory happy.
        addStyleMenu.add(new JMenuItem("Loading..."));

        // Rebuild the menu each time it's opened to reflect the current state of the card.
        // This is the correct pattern for DropDownButtonFactory with dynamic content.
        addStyleMenu.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                rebuildAddStyleMenu(addStyleMenu);
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        });

        return button;
    }

    private void rebuildAddStyleMenu(JPopupMenu menu) {
        menu.removeAll();
        List<TagStyle> existingStyles = getAllTagStyles();

        for (TagStyle style : TagStyle.values()) {
            JMenuItem item = new JMenuItem(style.getName());
            item.setFont(FontManager.getRunescapeSmallFont());

            // Disable the menu item if the style already exists on this card
            if (existingStyles.contains(style)) {
                item.setEnabled(false);
            } else {
                item.addActionListener(e -> {
                    if (styleRows.size() < BetterNpcHighlightPanel.MAX_STYLE_ROWS) {
                        addTagStyle(style);
                    }
                });
            }
            menu.add(item);
        }
    }

    private JButton createMenuButton() {
        JPopupMenu menu = createContextMenu();

        menuButton = DropDownButtonFactory.createDropDownButton(BetterNpcHighlightPanel.CARD_MENU_VERTICAL_ICONS.getOn(), menu);
        menuButton.setRolloverIcon(BetterNpcHighlightPanel.CARD_MENU_VERTICAL_ICONS.getOnHover());
        menuButton.setPreferredSize(new Dimension(COMPONENT_SIZE, COMPONENT_SIZE));
        menuButton.setMinimumSize(new Dimension(COMPONENT_SIZE, COMPONENT_SIZE));
        menuButton.setMaximumSize(new Dimension(COMPONENT_SIZE, COMPONENT_SIZE));
        menuButton.setToolTipText("More options");
        BetterNpcHighlightPanel.styleButton(menuButton);

        // Simple action listener using the proven approach
        menuButton.addActionListener(e -> {
            if (menu.isVisible()) {
                menu.setVisible(false);
            } else {
                int x = menuButton.getWidth() - menu.getPreferredSize().width;
                int y = menuButton.getHeight();
                menu.show(menuButton, x, y);
            }
        });

        return menuButton;
    }

    private JPopupMenu createContextMenu() {
        JPopupMenu menu = new JPopupMenu();

        createMenuItems();

        menu.add(displayNameMenu);
        menu.add(entityHiderItem);
        menu.add(drawUnderItem);
        menu.add(highlightDeadItem);
        menu.addSeparator();

        JMenu moveToGroupMenu = new JMenu("Move to group");
        menu.add(moveToGroupMenu);

        //  Repopulate just before menu shows
        menu.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                populateGroupMenu(moveToGroupMenu);
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        });

        JMenuItem deleteItem = createDeleteMenuItem();
        menu.add(deleteItem);

        return menu;
    }

    private void populateGroupMenu(JMenu moveToGroupMenu) {
        moveToGroupMenu.removeAll();
        for (NpcCardGroupPanel group : panel.getGroups()) {
            JMenuItem item = createGroupMenuItem(group);
            moveToGroupMenu.add(item);
        }
    }

    private JMenuItem createGroupMenuItem(NpcCardGroupPanel group) {
        JMenuItem item = new JMenuItem(group.getGroupName());

        if (group == getParentGroup()) {
            item.setFont(item.getFont().deriveFont(Font.BOLD));
        }

        item.addActionListener(e -> moveToGroup(group));
        return item;
    }

    private void moveToGroup(NpcCardGroupPanel targetGroup) {
        if (getParentGroup() != targetGroup) {
            if (getParentGroup() != null) {
                getParentGroup().removeCard(this);
            }
            targetGroup.addCard(this);
            panel.triggerDataChanged();
        }
    }

    private void createMenuItems() {
        entityHiderItem = createCheckBoxMenuItem("Hide NPC");
        drawUnderItem = createCheckBoxMenuItem("Draw overlay beneath NPC");

        displayNameMenu = new JMenu("Display name above NPC");
        displayNameItem = createCheckBoxMenuItem("Enable");
        displayNameItem.addItemListener(e -> panel.triggerDataChanged());

        overrideDisplayNameColorItem = createCheckBoxMenuItem("Enable custom name color");
        overrideDisplayNameColorItem.addItemListener(e -> panel.triggerDataChanged());

        JMenuItem setDisplayNameColorItem = new JMenuItem("Set custom name color...");
        setDisplayNameColorItem.addActionListener(e -> openColorPicker());

        displayNameMenu.add(displayNameItem);
        displayNameMenu.addSeparator();
        displayNameMenu.add(overrideDisplayNameColorItem);
        displayNameMenu.add(setDisplayNameColorItem);

        highlightDeadItem = createCheckBoxMenuItem("Highlight dead NPC");
    }

    private void openColorPicker() {
        Color initial = displayNameColor != null ? displayNameColor : Color.CYAN;
        String title = "Display name color";

        RuneliteColorPicker picker = colorPickerManager.create(
                SwingUtilities.getWindowAncestor(this),
                initial,
                title,
                true
        );

        picker.setOnColorChange(color -> {
            setDisplayNameColor(color);
            panel.triggerDataChanged();
        });

        Point panelLocation = panel.getLocationOnScreen();
        picker.setLocation(panelLocation.x - picker.getWidth(), panelLocation.y);

        picker.setVisible(true);
    }


    private JCheckBoxMenuItem createCheckBoxMenuItem(String text) {
        JCheckBoxMenuItem item = new JCheckBoxMenuItem(text, false);
        item.setHorizontalTextPosition(SwingConstants.LEFT);
        item.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                item.setFont(item.getFont().deriveFont(Font.BOLD));
            } else {
                item.setFont(item.getFont().deriveFont(Font.PLAIN));
            }
            panel.triggerDataChanged();
        });
        return item;
    }

    private JMenuItem createDeleteMenuItem() {
        JMenuItem deleteItem = new JMenuItem("Delete this card");
        deleteItem.addActionListener(e -> {
            if (parentGroup != null) {
                parentGroup.removeCard(this);
            }
        });
        return deleteItem;
    }

    private void createStyleRowsPanel() {
        styleRowsPanel = new JPanel();
        styleRowsPanel.setLayout(new BoxLayout(styleRowsPanel, BoxLayout.Y_AXIS));
        styleRowsPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        styleRowsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
    }

    // Name Editing Methods
    void startEditingName() {
        originalNameText = getNameText();
        if (originalNameText.isEmpty()) {
            nameEditor = createNameEditor(PLACEHOLDER_TEXT);
        } else {
            nameEditor = createNameEditor(originalNameText);
        }
        SwingUtilities.invokeLater(this::replaceNameLabelWithEditor);
        editNameButton.setVisible(false);
        saveButton.setVisible(true);
        cancelButton.setVisible(true);
    }

    private void cancelEditingName() {
        if (nameEditor == null) {
            return;
        }

        // Restore original text
        setNameText(originalNameText);

        // Restore label UI
        replaceEditorWithNameLabel();
        nameEditor = null;
        saveButton.setVisible(false);
        cancelButton.setVisible(false);
        editNameButton.setVisible(true);
        revalidate();
        repaint();
        this.requestFocusInWindow();
    }


    private void replaceNameLabelWithEditor() {
        topRow.remove(nameLabel);
        topRow.add(nameEditor, BorderLayout.CENTER);
        topRow.revalidate();
        topRow.repaint();

        nameEditor.selectAll();
        nameEditor.requestFocusInWindow();
    }

    private void finishEditingName() {
        if (nameEditor == null) {
            return;
        }

        String newName = nameEditor.getText().trim();
        // If the editor is empty, ensure we save an empty string to trigger placeholder logic
        if (newName.equals(PLACEHOLDER_TEXT)) {
            newName = "";
        }

        // Prevent duplicate names
        if (!newName.isEmpty() && isDuplicateName(newName)) {
            JOptionPane.showMessageDialog(this, "A card with the name '" + newName + "' already exists.", "Error", JOptionPane.ERROR_MESSAGE);
            cancelEditingName(); // Revert to the original name
            return;
        }

        setNameText(newName);

        replaceEditorWithNameLabel();
        nameEditor = null;
        saveButton.setVisible(false);
        cancelButton.setVisible(false);
        editNameButton.setVisible(true);

        // Request focus on the card panel itself to prevent the editor from re-engaging
        SwingUtilities.invokeLater(this::requestFocusInWindow);
        panel.resort();
        panel.triggerDataChanged();
        revalidate();
        repaint();
    }

    private void replaceEditorWithNameLabel() {
        topRow.remove(nameEditor);
        topRow.add(nameLabel, BorderLayout.CENTER);
        topRow.revalidate();
        topRow.repaint();
    }

    private JTextField createNameEditor(String text) {
        JTextField field = new JTextField();
        styleNameEditor(field);
        field.setText(text);

        // ENTER should save
        field.addActionListener(e -> finishEditingName());
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                Component oppositeComponent = e.getOppositeComponent();
                if (field.isEditable() && !e.isTemporary() && oppositeComponent != saveButton && oppositeComponent != cancelButton) {
                    cancelEditingName();
                }
            }
        });

        // ESC should cancel
        field.getInputMap(JComponent.WHEN_FOCUSED)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel-edit");
        field.getActionMap().put("cancel-edit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelEditingName();
            }
        });

        return field;
    }


    private void styleNameEditor(JTextField field) {
        field.setOpaque(false);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 0, ColorScheme.DARK_GRAY_COLOR),
                BorderFactory.createEmptyBorder(2, 5, 0, 5)
        ));
        field.setPreferredSize(new Dimension(0, COMPONENT_SIZE));
        field.setSelectedTextColor(Color.WHITE);
        field.setSelectionColor(ColorScheme.BRAND_ORANGE_TRANSPARENT);
        field.setForeground(Color.WHITE);
    }

    // Style Row Management
    public void addStyleRow(NpcHighlightEntry entry) {
        if (entry == null || entry.tagStyle == null) {
            return;
        }

        // Prevent adding duplicate style rows
        if (styleRows.stream().anyMatch(row -> row.getTagStyle() == entry.tagStyle)) {
            return;
        }

        addStyleRowAt(styleRows.size(), entry);
    }

    public void addStyleRowAt(int index, NpcHighlightEntry entry) {
        if (styleRows.size() >= BetterNpcHighlightPanel.MAX_STYLE_ROWS) {
            return;
        }

        StyleRow row = new StyleRow(this, colorPickerManager, entry);
        styleRows.add(index, row);
        styleRowsPanel.add(row, index);
        updateStyleButtons();
        refreshStyleRowsPanel();
        panel.triggerDataChanged();
    }

    public void removeStyleRowAt(int index) {
        if (isValidStyleRowIndex(index)) {
            StyleRow row = styleRows.remove(index);
            styleRowsPanel.remove(row);

            updateStyleButtons();
            refreshStyleRowsPanel();
            panel.triggerDataChanged();
        }
    }

    private boolean isValidStyleRowIndex(int index) {
        return index >= 0 && index < styleRows.size();
    }

    public void updateStyleButtons() {
        String removeTooltip = "Remove this highlight style";

        for (StyleRow row : styleRows) {
            row.getRemoveButton().setEnabled(true);
            row.getRemoveButton().setToolTipText(removeTooltip);
        }

        boolean canAdd = styleRows.size() < BetterNpcHighlightPanel.MAX_STYLE_ROWS;
        addStyleButton.setEnabled(canAdd);

        String addTooltip = canAdd ?
                "Add a new highlight style" :
                "Maximum of " + BetterNpcHighlightPanel.MAX_STYLE_ROWS + " highlight styles allowed";
        addStyleButton.setToolTipText(addTooltip);
    }

    private void refreshStyleRowsPanel() {
        styleRowsPanel.revalidate();
        styleRowsPanel.repaint();
    }

    // Data Management Methods
    public List<NpcHighlightEntry> getAllEntries() {
        String npcNameOrId = getNameText().trim();
        if (npcNameOrId.isEmpty()) {
            return new ArrayList<>();
        }

        // If there are no style rows, create a single "base" entry that only contains the card's toggle settings
        if (styleRows.isEmpty()) {
            return List.of(createBaseEntry(npcNameOrId));
        }

        // Otherwise, create an entry for each style row
        List<NpcHighlightEntry> entries = new ArrayList<>();
        for (StyleRow row : styleRows) {
            entries.add(createEntryFromStyleRow(npcNameOrId, row));
        }
        return entries;
    }

    private NpcHighlightEntry createEntryFromStyleRow(String npcNameOrId, StyleRow row) {
        TagStyle tagStyle = row.getTagStyle();
        Color nameColor = isOverrideDisplayNameColor() ? getDisplayNameColor() : null;

        return new NpcHighlightEntry(
                npcNameOrId,
                tagStyle,
                row.getHighlightPreviewPanel().getOutlineColor(),
                row.getHighlightPreviewPanel().getFillColor(),
                isHideNpc(),
                isDrawUnder(),
                isDisplayName(),
                nameColor,
                isOverrideDisplayNameColor(),
                isHighlightDead(),
                row.getHighlightPreviewPanel().isRaveOutline(),
                row.getHighlightPreviewPanel().isRaveFill(),
                row.getHighlightPreviewPanel().getRaveSpeed(),
                row.getHighlightPreviewPanel().getLineType(),
                row.getHighlightPreviewPanel().getOutlineWidth(),
                row.getHighlightPreviewPanel().isAntiAliasing(),
                row.getHighlightPreviewPanel().getOutlineFeather()
        );
    }

    private NpcHighlightEntry createBaseEntry(String npcNameOrId) {
        return new NpcHighlightEntry(
                npcNameOrId,
                null, // No tag style
                null, null, // No colors
                isHideNpc(),
                isDrawUnder(),
                isDisplayName(),
                isOverrideDisplayNameColor() ? getDisplayNameColor() : null,
                isOverrideDisplayNameColor(),
                isHighlightDead(),
                false, false, 0, null, 0, false, 0 // Default style values
        );
    }

    public void triggerDataChanged() {
        panel.triggerDataChanged();
    }

    // Getters and Setters
    public BetterNpcHighlightPlugin getPlugin() {
        return panel.getPlugin();
    }

    public String getNameText() {
        String text = nameLabel.getText();
        return PLACEHOLDER_TEXT.equals(text) ? "" : text;
    }

    public void setNameText(String text) {
        if (text == null || text.trim().isEmpty()) {
            nameLabel.setText(PLACEHOLDER_TEXT);
            styleNameLabel(nameLabel, true);
        } else {
            nameLabel.setText(text);
            styleNameLabel(nameLabel, false);
        }
    }

    public List<StyleRow> getStyleRows() {
        return new ArrayList<>(styleRows); // Return defensive copy
    }

    public boolean isHideNpc() {
        return entityHiderItem.isSelected();
    }

    public void setHideNpc(boolean selected) {
        entityHiderItem.setSelected(selected);
    }

    public boolean isDrawUnder() {
        return drawUnderItem.isSelected();
    }

    public void setDrawUnder(boolean selected) {
        drawUnderItem.setSelected(selected);
    }

    public boolean isDisplayName() {
        return displayNameItem.isSelected();
    }

    public void setDisplayName(boolean selected) {
        displayNameItem.setSelected(selected);
    }

    public boolean isOverrideDisplayNameColor() {
        return overrideDisplayNameColorItem.isSelected();
    }

    public void setOverrideDisplayNameColor(boolean selected) {
        overrideDisplayNameColorItem.setSelected(selected);
    }

    public boolean isHighlightDead() {
        return highlightDeadItem.isSelected();
    }

    public void setHighlightDead(boolean selected) {
        highlightDeadItem.setSelected(selected);
    }

    public void focusNameField() {
        SwingUtilities.invokeLater(() -> {
            if (isShowing()) {
                startEditingName();
            }
        });
    }

    // TagStyle with default settings
    public void addTagStyle(TagStyle style) {
        addTagStyle(style, null, null);
    }

    // TagStyle with preset colors
    public void addTagStyle(TagStyle style, Color outlineColor, Color fillColor) {
        // If the style is null (e.g. from an old config), do nothing
        if (style == null) {
            return;
        }

        // If the style already exists, update its colors instead of adding a new row
        StyleRow existingRow = styleRows.stream()
                .filter(row -> row.getTagStyle() == style)
                .findFirst()
                .orElse(null);

        if (existingRow != null) {
            Color finalOutline = (outlineColor != null) ? outlineColor : getPlugin().getPresetOutlineColor(0);
            Color finalFill = (fillColor != null) ? fillColor : getPlugin().getPresetFillColor(0);

            existingRow.getHighlightPreviewPanel().setOutlineColor(finalOutline);
            existingRow.getHighlightPreviewPanel().setFillColor(finalFill);
            panel.triggerDataChanged();
        } else {
            // If the style doesn't exist, add a new style row.
            NpcHighlightEntry newEntry = new NpcHighlightEntry(getNameText(), style, getPlugin().getConfig());
            // Use provided colors, or fall back to defaults if null
            newEntry.outlineColor = (outlineColor != null) ? outlineColor : getPlugin().getPresetOutlineColor(0);
            newEntry.fillColor = (fillColor != null) ? fillColor : getPlugin().getPresetFillColor(0);

            // Inherit the card's main toggle settings
            newEntry.hideNpc = isHideNpc();
            newEntry.drawUnder = isDrawUnder();
            newEntry.displayName = isDisplayName();
            newEntry.overrideDisplayNameColor = isOverrideDisplayNameColor();
            newEntry.displayNameColor = getDisplayNameColor();
            newEntry.highlightDead = isHighlightDead();

            addStyleRow(newEntry);
        }
    }

    public void removeTagStyle(TagStyle style) {
        StyleRow rowToRemove = styleRows.stream()
                .filter(row -> row.getTagStyle() == style)
                .findFirst()
                .orElse(null);

        if (rowToRemove != null) {
            int index = styleRows.indexOf(rowToRemove);
            removeStyleRowAt(index);
        }
    }

    public List<TagStyle> getAllTagStyles() {
        return styleRows.stream()
                .map(StyleRow::getTagStyle)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toList());
    }

    public Color getOutlineColorForStyle(TagStyle style) {
        return styleRows.stream()
                .filter(row -> row.getTagStyle() == style)
                .findFirst()
                .map(row -> row.getHighlightPreviewPanel().getOutlineColor())
                .orElse(null);
    }

    public Color getFillColorForStyle(TagStyle style) {
        return styleRows.stream()
                .filter(row -> row.getTagStyle() == style)
                .findFirst()
                .map(row -> row.getHighlightPreviewPanel().getFillColor())
                .orElse(null);
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension prefSize = super.getPreferredSize();
        return new Dimension(Integer.MAX_VALUE, prefSize.height);
    }

    // Inner Classes for Event Handling
    private class NameLabelMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
                startEditingName();
            }
        }
    }

    private boolean isDuplicateName(String name) {
        for (NpcCardGroupPanel group : panel.getGroups()) {
            for (NpcCard card : group.getCards()) {
                if (card.getCardId().equals(this.getCardId())) {
                    continue;
                }
                if (name.equalsIgnoreCase(card.getNameText())) {
                    return true;
                }
            }
        }
        return false;
    }
}