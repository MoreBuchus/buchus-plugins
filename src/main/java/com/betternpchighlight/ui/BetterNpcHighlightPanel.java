package com.betternpchighlight.ui;

//todo: add sorting mode

import com.betternpchighlight.BetterNpcHighlightPlugin;
import com.betternpchighlight.TagStyle;
import com.betternpchighlight.data.DataManager;
import com.betternpchighlight.data.NpcHighlightEntry;
import com.betternpchighlight.ui.dropdownbutton.DropDownButtonFactory;
import com.betternpchighlight.util.IconSet;

import com.formdev.flatlaf.FlatClientProperties;

import lombok.Getter;
import lombok.Setter;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.IconTextField;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.List;

public class BetterNpcHighlightPanel extends PluginPanel {

    private ScrollableVerticalPanel cardsPanel;
    private IconTextField searchField;
    @Getter
    private final List<NpcCardGroupPanel> groups = new ArrayList<>();
    private JScrollPane cardsScrollPane;
    @Getter
    private final ColorPickerManager colorPickerManager;
    private final ConfigManager configManager;
    private final DataManager dataManager;
    @Getter
    private final BetterNpcHighlightPlugin plugin;
    private boolean isBatchUpdating = false;

    public static final int MAX_STYLE_ROWS = 9;

    private static final int luminanceOnHover = -80;
    private static final int luminanceOffHover = -130;
    private static final int luminanceOff = -150;

    public static final IconSet MAIN_MENU_ICONS = IconSet.loadIconSet("/main_menu.png", luminanceOnHover + 30, luminanceOff, luminanceOffHover);
    public static final IconSet CARD_MENU_VERTICAL_ICONS = IconSet.loadIconSet("/card_menu_vertical.png", luminanceOnHover + 30, luminanceOff, luminanceOffHover);
    public static final IconSet ADD_CARD_ICONS = IconSet.loadIconSet("/add_card.png", luminanceOnHover + 30, luminanceOff, luminanceOffHover);
    public static final IconSet GROUP_MENU_ICONS = IconSet.loadIconSet("/group_menu.png", luminanceOnHover + 30, luminanceOff, luminanceOffHover);

    @Setter
    private DataChangedListener dataChangedListener;

    public interface DataChangedListener {
        void onDataChanged();
    }

    public BetterNpcHighlightPanel(ColorPickerManager colorPickerManager, ConfigManager configManager, BetterNpcHighlightPlugin plugin, DataManager dataManager) {
        super(false);
        this.colorPickerManager = colorPickerManager;
        this.configManager = configManager;
        this.plugin = plugin;
        this.dataManager = dataManager;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(0, 0, 0, 0));
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        // Create main content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        // Create top panel with search and buttons
        JPanel topPanel = createTopPanel();
        contentPanel.add(topPanel, BorderLayout.NORTH);

        // Create cards panel
        createCardsPanel();
        contentPanel.add(cardsScrollPane, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        topPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(23, 23, 23)), new EmptyBorder(5, 5, 5, 5)));

        // Titlebar
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(ColorScheme.DARK_GRAY_COLOR);
        //titleBar.setBorder(new EmptyBorder(0, 4, 0, 0));

        JLabel title = new JLabel("Better NPC Highlight");
        title.setBorder(new EmptyBorder(0, 4, 0, 0));
        title.setForeground(Color.WHITE);
        titleBar.add(title, BorderLayout.WEST);

        // Create popup menu
        JPopupMenu mainMenu = new JPopupMenu();
        JMenuItem addCardItem = new JMenuItem("Add new card");
        addCardItem.addActionListener(e -> addNewCardToDefaultGroup(NpcCard::focusNameField));
        JMenuItem addGroupItem = new JMenuItem("Add new group");
        addGroupItem.addActionListener(e -> addNewGroup());
        mainMenu.add(addCardItem);
        mainMenu.add(addGroupItem);

        mainMenu.addSeparator();

        JMenuItem importItem = new JMenuItem("Import cards");
        importItem.addActionListener(e -> plugin.importGroupsFromFile());
        mainMenu.add(importItem);

        JMenuItem exportItem = new JMenuItem("Export cards");
        exportItem.addActionListener(e -> plugin.exportGroupsToFile());
        mainMenu.add(exportItem);

        mainMenu.addSeparator();

        JCheckBoxMenuItem debugItem = new JCheckBoxMenuItem("NPC Debugging");
        debugItem.setHorizontalTextPosition(SwingConstants.LEFT);
        debugItem.setSelected(plugin.isDebugModeEnabled());
        debugItem.addActionListener(e -> plugin.setDebugModeEnabled(debugItem.isSelected()));
        mainMenu.add(debugItem);

        mainMenu.addSeparator();

        JMenuItem clearAllItem = new JMenuItem("Clear all...");
        clearAllItem.addActionListener(e -> clearAllExceptDefaultGroup());
        mainMenu.add(clearAllItem);

        // Create main menu button
        JButton mainMenuButton = DropDownButtonFactory.createDropDownButton(MAIN_MENU_ICONS.getOn(), mainMenu);
        mainMenuButton.setIcon(MAIN_MENU_ICONS.getOn());
        mainMenuButton.setRolloverIcon(MAIN_MENU_ICONS.getOnHover());
        mainMenuButton.setBackground(topPanel.getBackground());
        mainMenuButton.setPreferredSize(new Dimension(30, 30));
        mainMenuButton.setToolTipText("Menu");
        styleButton(mainMenuButton);

        mainMenuButton.addActionListener(e -> {
            if (mainMenu.isVisible()) {
                mainMenu.setVisible(false);
            } else {
                int x = mainMenuButton.getWidth() - mainMenu.getPreferredSize().width;
                int y = mainMenuButton.getHeight() + 1;
                mainMenu.show(mainMenuButton, x, y);
            }
        });

        titleBar.add(mainMenuButton, BorderLayout.EAST);
        topPanel.add(titleBar, BorderLayout.NORTH);

        // Search field
        searchField = new IconTextField();
        searchField.setPreferredSize(new Dimension(300, 30));
        searchField.setIcon(IconTextField.Icon.SEARCH);
        searchField.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        searchField.setForeground(ColorScheme.LIGHT_GRAY_COLOR);

        searchField.getDocument().addDocumentListener((SimpleDocumentListener) e -> updateFilter());

        for (TagStyle style : TagStyle.values()) {
            searchField.getSuggestionListModel().addElement(style.toString());
        }
        topPanel.add(searchField, BorderLayout.SOUTH);

        return topPanel;
    }

    public static void styleButton(AbstractButton button) {
        Color color = button.getBackground(); // example
        String hex = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());

        button.putClientProperty(FlatClientProperties.STYLE, "disabledBackground: null; hoverBackground: null; pressedBackground: lighten(" + hex + ",5%); selectedBackground: null; arc: 8");
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(true);
    }

    private void updateFilter() {
        String text = searchField.getText().toLowerCase();

        for (NpcCardGroupPanel group : groups) {
            boolean groupMatches = group.getGroupName().toLowerCase().contains(text);

            boolean anyCardMatches = false;
            for (NpcCard card : group.getCards()) {
                boolean cardMatches = text.isEmpty()
                        || card.getNameText().toLowerCase().contains(text)
                        || card.getAllTagStyles().stream().anyMatch(s -> s.toString().toLowerCase().contains(text));

                card.setVisible(cardMatches);
                if (cardMatches) {
                    anyCardMatches = true;
                }
            }

            // Show group if either group matches, or at least one card matches
            group.setVisible(text.isEmpty() || groupMatches || anyCardMatches);
        }

        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    private void createCardsPanel() {
        cardsPanel = new ScrollableVerticalPanel();
        cardsPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        cardsPanel.setBorder(new EmptyBorder(0, 5, 0, 5));

        cardsScrollPane = new JScrollPane(cardsPanel);
        cardsScrollPane.setBackground(ColorScheme.DARK_GRAY_COLOR);
        cardsScrollPane.getViewport().setBackground(ColorScheme.DARK_GRAY_COLOR);
        cardsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        cardsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }

    private void addNewGroup() {
        String baseName = "New Group";
        NpcCardGroupPanel newGroup = new NpcCardGroupPanel(baseName, colorPickerManager, this);

        // Insert at the start of the list (index 0).
        groups.add(0, newGroup);

        // Remove default group, re-append it at the end (to guarantee ordering).
        NpcCardGroupPanel defaultGroup = groups.stream()
                .filter(NpcCardGroupPanel::isDefault)
                .findFirst()
                .orElse(null);

        if (defaultGroup != null) {
            groups.remove(defaultGroup);
            groups.add(defaultGroup);
        }

        // Insert at the correct UI index (top).
        cardsPanel.add(newGroup, 0);

        // Scroll to it and start editing its name.
        SwingUtilities.invokeLater(() -> {
            newGroup.scrollRectToVisible(newGroup.getBounds());
            newGroup.startNameEdit();
        });
    }


    public void addCardToGroup(NpcCardGroupPanel group) {
        NpcCard card = new NpcCard(this, colorPickerManager);
        group.addCard(card, true);

        SwingUtilities.invokeLater(() -> {
            card.focusNameField();
            if (group.isCollapsed()) {
                group.toggleCollapse();
            }
        });

        triggerDataChanged();
    }

    public void addNewCardToDefaultGroup(Consumer<NpcCard> cardInitializer) {
        addNewCardToDefaultGroup(cardInitializer, true);
    }

    public void addNewCardToDefaultGroup(Consumer<NpcCard> cardInitializer, boolean focus) {
        NpcCard card = new NpcCard(this, colorPickerManager);

        NpcCardGroupPanel defaultGroup = groups.stream()
                .filter(NpcCardGroupPanel::isDefault)
                .findFirst()
                .orElse(null);

        if (defaultGroup == null) {
            // Fallback: if the default group is somehow missing, create it.
            defaultGroup = dataManager.convertGroupFromDto(DataManager.DEFAULT_GROUP, this);
            groups.add(0, defaultGroup);
            sortAndRebuildGroups();
        }

        final NpcCardGroupPanel targetGroup = defaultGroup;
        targetGroup.addCard(card, true);

        cardInitializer.accept(card);

        SwingUtilities.invokeLater(() -> {
            if (focus) card.focusNameField();
            if (targetGroup.isCollapsed()) {
                targetGroup.toggleCollapse();
            }
        });

        triggerDataChanged();
    }

    public void addCardFromCommand(Consumer<NpcCard> cardInitializer) {
        NpcCard card = new NpcCard(this, colorPickerManager);

        NpcCardGroupPanel defaultGroup = groups.stream()
                .filter(NpcCardGroupPanel::isDefault)
                .findFirst()
                .orElse(null);

        if (defaultGroup == null) {
            // Fallback: if the default group is somehow missing, create it.
            defaultGroup = dataManager.convertGroupFromDto(DataManager.DEFAULT_GROUP, this);
            groups.add(0, defaultGroup);
            sortAndRebuildGroups();
        }

        // Add the card, but mark it as not "new" to prevent default styles from being added.
        defaultGroup.addCard(card, false, 0); // Add to top

        cardInitializer.accept(card);

        triggerDataChanged();
    }

    public void triggerDataChanged() {
        if (isBatchUpdating) {
            return;
        }
        if (dataChangedListener != null) {
            dataChangedListener.onDataChanged();
        }
    }

    // Data management methods
    public List<NpcHighlightEntry> getNpcHighlightEntries() {
        List<NpcHighlightEntry> entries = new ArrayList<>();

        for (NpcCardGroupPanel group : groups) {
            for (NpcCard card : group.getCards()) {
                String nameOrId = card.getNameText();
                if (nameOrId == null || nameOrId.trim().isEmpty()) {
                    continue;
                }
                entries.addAll(card.getAllEntries());
            }
        }
        return entries;
    }

    private void clearAllGroups() {
        for (NpcCardGroupPanel group : groups) {
            group.clearCards();
        }
        groups.clear(); // Clear all groups
        cardsPanel.removeAll(); // Clear UI
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    private void clearAllExceptDefaultGroup() {
        int result = JOptionPane.showConfirmDialog(this,
                "This will remove all groups and cards, leaving only an empty default group.\nAre you sure?",
                "Warning",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            // Find the default group and clear its cards
            groups.stream()
                    .filter(NpcCardGroupPanel::isDefault)
                    .findFirst()
                    .ifPresent(NpcCardGroupPanel::clearCards);

            // Remove all non-default groups from the list
            groups.removeIf(group -> !group.isDefault());

            // Rebuild the UI and trigger a save
            sortAndRebuildGroups();
            triggerDataChanged();
        }
    }

    public void requestSave(String configGroup) {
        if (isBatchUpdating) {
            return;
        }
        SwingUtilities.invokeLater(() -> saveAllGroups(configGroup));
    }

    // Save all groups
    public void saveAllGroups(String configGroup) {
        dataManager.saveGroups(configGroup, groups);
    }

    // Load groups from config
    public void loadAllGroups(String configGroup) {
        isBatchUpdating = true;
        try {
            groups.clear(); // Clear existing groups
            List<NpcCardGroupPanel> loadedGroups = dataManager.loadGroups(configGroup, this);
            groups.addAll(loadedGroups);
            sortAndRebuildGroups();
        } finally {
            isBatchUpdating = false;
        }
    }

    public void sortAndRebuildGroups() {
        // Sort groups so that the default group is always last
        groups.sort(Comparator.comparing(NpcCardGroupPanel::isDefault));

        // Re-add groups to the panel in the correct order
        cardsPanel.removeAll();
        for (NpcCardGroupPanel group : groups) {
            cardsPanel.add(group);
        }
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    private interface SimpleDocumentListener extends javax.swing.event.DocumentListener {
        void update(DocumentEvent e);

        @Override
        default void insertUpdate(DocumentEvent e) {
            update(e);
        }

        @Override
        default void removeUpdate(DocumentEvent e) {
            update(e);
        }

        @Override
        default void changedUpdate(DocumentEvent e) {
            update(e);
        }
    }
}
