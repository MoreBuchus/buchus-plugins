package com.betternpchighlight.ui;

import com.betternpchighlight.BetterNpcHighlightConfig;
import com.betternpchighlight.TagStyle;
import com.betternpchighlight.ui.dropdownbutton.DropDownButtonFactory;
import com.betternpchighlight.util.IconSet;
import lombok.Getter;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;
import net.runelite.client.ui.components.colorpicker.RuneliteColorPicker;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class NpcCardGroupPanel extends JPanel {
    private final ColorPickerManager colorPickerManager;
    private final BetterNpcHighlightPanel panel;

    @Getter
    private final UUID groupId;
    @Getter
    private String groupName;
    @Getter
    private Color accentColor = ColorScheme.LIGHT_GRAY_COLOR;
    private static final int COMPONENT_SIZE = 24;
    @Getter
    private boolean collapsed = false;
    @Getter
    private final boolean isDefault;
    @Getter
    private final List<NpcCard> cards = new ArrayList<>();

    // UI Components
    private JTextField groupNameField;
    private JButton collapseButton;
    private JPanel saveButtonPanel;
    private JButton menuButton;
    private JButton addCardButton;
    private String originalGroupName;
    private final ScrollableVerticalPanel cardsContainer;
    private final Color backgroundColor = new Color(25, 25, 25);

    // Icon Sets
    public static final IconSet ADD_CARD_ICONS = IconSet.loadIconSet("/add_card.png");
    public static final IconSet GROUP_MENU_ICONS = IconSet.loadIconSet("/group_menu.png");
    public static final IconSet EDIT_GROUPNAME_ICONS = IconSet.loadIconSet("/edit_groupname.png");
    public static final IconSet CANCEL_EDIT_GROUPNAME_ICONS = IconSet.loadIconSet("/cancel_edit_groupname.png");
    public static final IconSet SAVE_EDIT_GROUPNAME_ICONS = IconSet.loadIconSet("/save_edit_groupname.png");
    public static final IconSet EXPAND_GROUP_ICONS = IconSet.loadIconSet("/chevron_right.png");
    public static final IconSet COLLAPSE_GROUP_ICONS = IconSet.loadIconSet("/chevron_down.png");

    public NpcCardGroupPanel(String groupName, ColorPickerManager colorPickerManager, BetterNpcHighlightPanel panel) {
        this(UUID.randomUUID(), groupName, colorPickerManager, panel, false);
    }

    public NpcCardGroupPanel(UUID groupId, String groupName, ColorPickerManager colorPickerManager, BetterNpcHighlightPanel panel, boolean isDefault) {
        super();
        this.groupId = groupId;
        this.groupName = groupName;
        this.colorPickerManager = colorPickerManager;
        this.panel = panel;
        this.isDefault = isDefault;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0), BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor)));

        JPanel header = createHeaderPanel();
        cardsContainer = new ScrollableVerticalPanel();

        add(header);
        add(cardsContainer);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && e.getX() < getInsets().left) {
                    toggleCollapse();
                }
            }
        });
    }

    // UI Setup
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(backgroundColor);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

        collapseButton = createCollapseButton();
        updateCollapseButton();
        groupNameField = createGroupNameField();
        JPanel buttonPanel = createHeaderButtonPanel();

        headerPanel.add(collapseButton, BorderLayout.WEST);
        headerPanel.add(groupNameField, BorderLayout.CENTER);
        headerPanel.add(buttonPanel, BorderLayout.EAST);

        MouseAdapter headerMouseListener = createHeaderMouseListener();
        collapseButton.addMouseListener(headerMouseListener);
        headerPanel.addMouseListener(headerMouseListener);
        groupNameField.addMouseListener(headerMouseListener);

        return headerPanel;
    }

    private JButton createCollapseButton() {
        JButton button = new JButton(isCollapsed() ? COLLAPSE_GROUP_ICONS.getOn() : EXPAND_GROUP_ICONS.getOn());
        button.setPreferredSize(new Dimension(COMPONENT_SIZE, COMPONENT_SIZE));
        button.setContentAreaFilled(false);
        button.setBorder(new EmptyBorder(0, 4, 0, 4));
        return button;
    }

    private JTextField createGroupNameField() {
        JTextField field = new JTextField();
        field.setDocument(new LengthRestrictedDocument(20));
        field.setText(groupName);
        field.setOpaque(false);
        field.setEditable(false);
        field.setFocusable(false);
        field.setPreferredSize(new Dimension(Integer.MAX_VALUE, COMPONENT_SIZE));
        field.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        field.setForeground(Color.WHITE);
        field.setSelectionColor(ColorScheme.BRAND_ORANGE_TRANSPARENT);
        field.setSelectedTextColor(Color.WHITE);

        field.addActionListener(e -> finishNameEdit());
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (field.isEditable() && !e.isTemporary()) {
                    cancelNameEdit();
                }
            }
        });

        // ESC should cancel
        field.getInputMap(JComponent.WHEN_FOCUSED)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel-edit");
        field.getActionMap().put("cancel-edit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelNameEdit();
            }
        });
        return field;
    }

    private JPanel createHeaderButtonPanel() {
        JPanel headerButtonPanel = new JPanel();
        headerButtonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 1, 0));
        headerButtonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));
        headerButtonPanel.setOpaque(false);

        addCardButton = createIconButton(ADD_CARD_ICONS, "Add card to group", e -> panel.addCardToGroup(this));
        JButton saveButton = createIconButton(SAVE_EDIT_GROUPNAME_ICONS, "Save", e -> finishNameEdit());
        JButton cancelButton = createIconButton(CANCEL_EDIT_GROUPNAME_ICONS, "Cancel", e -> cancelNameEdit());

        saveButtonPanel = new JPanel();
        saveButtonPanel.setLayout(new BoxLayout(saveButtonPanel, BoxLayout.X_AXIS));
        saveButtonPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        saveButtonPanel.setVisible(false);
        saveButtonPanel.add(saveButton);
        saveButtonPanel.add(cancelButton);

        menuButton = createMenuButton();

        headerButtonPanel.add(saveButtonPanel);
        headerButtonPanel.add(addCardButton);
        headerButtonPanel.add(menuButton);

        return headerButtonPanel;
    }

    private JButton createMenuButton() {
        JPopupMenu menu = createHeaderMenu();

        menuButton = DropDownButtonFactory.createDropDownButton(GROUP_MENU_ICONS.getOn(), menu);
        menuButton.setRolloverIcon(GROUP_MENU_ICONS.getOnHover());
        menuButton.setBackground(backgroundColor);
        menuButton.setPreferredSize(new Dimension(COMPONENT_SIZE, COMPONENT_SIZE));
        menuButton.setMaximumSize(new Dimension(COMPONENT_SIZE, COMPONENT_SIZE));
        menuButton.setAlignmentY(0.5f);
        BetterNpcHighlightPanel.styleButton(menuButton);
        menuButton.setToolTipText("More options");

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

    private JButton createIconButton(IconSet icons, String tooltip, java.awt.event.ActionListener action) {
        JButton button = new JButton(icons.getOn());
        button.setRolloverIcon(icons.getOnHover());
        button.setBackground(backgroundColor);
        button.setPreferredSize(new Dimension(COMPONENT_SIZE, COMPONENT_SIZE));
        button.setMaximumSize(new Dimension(COMPONENT_SIZE, COMPONENT_SIZE));
        button.setAlignmentY(0.5f);
        BetterNpcHighlightPanel.styleButton(button);
        button.setToolTipText(tooltip);
        if (action != null) {
            button.addActionListener(action);
        }
        return button;
    }

    private MouseAdapter createHeaderMouseListener() {
        return new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && !groupNameField.isEditable()) {
                    toggleCollapse();
                }
            }
        };
    }

    private JPopupMenu createHeaderMenu() {
        JPopupMenu menu = new JPopupMenu();

        menu.removeAll();

        JMenuItem renameItem = new JMenuItem("Rename group");
        renameItem.addActionListener(e -> startNameEdit());
        renameItem.setEnabled(!isDefault);
        menu.add(renameItem);

        JMenu moveMenu = new JMenu("Move group");
        moveMenu.setEnabled(!isDefault);

        JMenuItem moveUpItem = new JMenuItem("Move up");
        moveUpItem.addActionListener(e -> panel.moveGroup(this, -1));
        moveMenu.add(moveUpItem);

        JMenuItem moveDownItem = new JMenuItem("Move down");
        moveDownItem.addActionListener(e -> panel.moveGroup(this, 1));
        moveMenu.add(moveDownItem);

        moveMenu.addSeparator();

        JMenuItem moveToTopItem = new JMenuItem("Move to top");
        moveToTopItem.addActionListener(e -> panel.moveGroupToTop(this));
        moveMenu.add(moveToTopItem);

        JMenuItem moveToBottomItem = new JMenuItem("Move to bottom");
        moveToBottomItem.addActionListener(e -> panel.moveGroupToBottom(this));
        moveMenu.add(moveToBottomItem);

        JMenuItem moveToPositionItem = new JMenuItem("Move to position...");
        moveToPositionItem.addActionListener(e -> panel.moveGroupToPositionDialog(this));
        moveMenu.add(moveToPositionItem);

        menu.add(moveMenu);

        moveMenu.getPopupMenu().addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                panel.updateMoveActionStates(NpcCardGroupPanel.this, moveUpItem, moveDownItem, moveToTopItem, moveToBottomItem);
            }

            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }

            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        });

        JMenuItem changeColorItem = new JMenuItem("Change accent color");
        changeColorItem.addActionListener(ev -> showColorPicker());
        menu.add(changeColorItem);

        JMenuItem deleteItem = new JMenuItem("Delete group");
        deleteItem.addActionListener(e -> deleteGroup());
        deleteItem.setEnabled(!isDefault);

        menu.add(deleteItem);
        return menu;
    }

    private void showColorPicker() {
        RuneliteColorPicker picker = colorPickerManager.create(this, accentColor, "Group accent color", false);
        Point panelLocation = panel.getLocationOnScreen();
        picker.setLocation(panelLocation.x - picker.getWidth(), panelLocation.y);
        picker.setOnColorChange(selectedColor -> {
            accentColor = selectedColor;
            setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0), BorderFactory.createMatteBorder(0, 4, 0, 0, selectedColor)));
            revalidate();
            repaint();
            panel.triggerDataChanged();
        });
        picker.setVisible(true);
    }

    private void deleteGroup() {
        if (isDefault) return;
        if (!cards.isEmpty()) {
            int result = JOptionPane.showConfirmDialog(
                    panel,
                    "This group is NOT empty. Are you sure you want to delete it?",
                    "Warning",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (result != JOptionPane.YES_OPTION) {
                return;
            }
        }
        Container parent = getParent();
        if (parent != null) {
            parent.remove(this);
            panel.getGroups().remove(this);
            panel.triggerDataChanged();
            parent.revalidate();
            parent.repaint();
        }
    }

    // Name Editing Logic
    public void startNameEdit() {
        if (isDefault) return;
        if (groupNameField.isEditable()) return;
        originalGroupName = groupName;
        groupNameField.setFocusable(true);
        groupNameField.setEditable(true);
        menuButton.setVisible(false);
        addCardButton.setVisible(false);
        saveButtonPanel.setVisible(true);
        SwingUtilities.invokeLater(() -> {
            groupNameField.requestFocusInWindow();
            groupNameField.selectAll();
        });
    }

    public void finishNameEdit() {
        String newName = groupNameField.getText().trim();
        if (newName.isEmpty()) newName = "New Group";
        groupName = newName;
        groupNameField.setText(groupName);
        groupNameField.setEditable(false);
        groupNameField.setFocusable(false);
        saveButtonPanel.setVisible(false);
        menuButton.setVisible(true);
        addCardButton.setVisible(true);
        this.requestFocusInWindow();
        revalidate();
        repaint();
        panel.triggerDataChanged();
    }

    public void cancelNameEdit() {
        if (!groupNameField.isEditable()) return;
        groupName = originalGroupName;
        groupNameField.setText(originalGroupName);
        groupNameField.setEditable(false);
        groupNameField.setFocusable(false);
        saveButtonPanel.setVisible(false);
        menuButton.setVisible(true);
        addCardButton.setVisible(true);
        this.requestFocusInWindow();
        revalidate();
        repaint();
    }

    // Collapse/Expand
    public void toggleCollapse() {
        collapsed = !collapsed;
        cardsContainer.setVisible(!collapsed);
        updateCollapseButton();
        revalidate();
        repaint();
    }

    private void updateCollapseButton() {
        if (isCollapsed()) {
            collapseButton.setIcon(EXPAND_GROUP_ICONS.getOn());
            collapseButton.setRolloverIcon(EXPAND_GROUP_ICONS.getOn());
        } else {
            collapseButton.setIcon(COLLAPSE_GROUP_ICONS.getOn());
            collapseButton.setRolloverIcon(COLLAPSE_GROUP_ICONS.getOn());
        }
    }

    // Card Management
    public void addCard(NpcCard card, boolean isNew, int position) {
        if (position == -1) {
            // Add to the end
            cards.add(card);
            cardsContainer.add(card);
        } else {
            // Add at a specific position
            cards.add(position, card);
            cardsContainer.add(card, position);
        }
        card.setParentGroup(this);

        // If this is a new card being added by the user (not from loading), apply the default style from the config.
        if (isNew) {
            BetterNpcHighlightConfig.DefaultHighlightStyle defaultStyle = panel.getPlugin().getConfig().tagStyleMode();
            if (defaultStyle != BetterNpcHighlightConfig.DefaultHighlightStyle.NONE) {
                try {
                    TagStyle tagStyle = TagStyle.valueOf(defaultStyle.name());
                    card.addTagStyle(tagStyle);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        revalidate();
        repaint();
    }

    public void addCard(NpcCard card) {
        addCard(card, false, -1); // Overload for loading from config (add to end)
    }

    public void addCard(NpcCard card, boolean isNew) {
        addCard(card, isNew, 0); // Overload for adding to the top
    }

    public void removeCard(NpcCard card) {
        cards.remove(card);
        cardsContainer.remove(card);
        panel.triggerDataChanged();
        revalidate();
        repaint();
    }

    public void clearCards() {
        cards.clear();
        cardsContainer.removeAll();
        panel.triggerDataChanged();
        revalidate();
        repaint();
    }

    public void setAccentColor(Color accentColor) {
        this.accentColor = accentColor;
        setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0), BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor)));
    }

    public void reorderCards(boolean sortByName) {
        List<NpcCard> cardsToDisplay;
        if (sortByName) {
            // Create a sorted copy for display
            cardsToDisplay = new ArrayList<>(this.cards);
            cardsToDisplay.sort(Comparator.comparing(NpcCard::getNameText, String.CASE_INSENSITIVE_ORDER));
        } else {
            // Use the original list to restore the default order
            cardsToDisplay = this.cards;
        }

        // Rebuild the UI with the new sort order
        cardsContainer.removeAll();
        for (NpcCard card : cardsToDisplay) {
            cardsContainer.add(card);
        }
        cardsContainer.revalidate();
        cardsContainer.repaint();
    }
}
