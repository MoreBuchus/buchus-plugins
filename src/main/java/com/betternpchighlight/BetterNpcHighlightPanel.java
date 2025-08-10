package com.betternpchighlight;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Collections;
import java.util.UUID;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.common.collect.ImmutableList;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;
import net.runelite.client.ui.components.colorpicker.RuneliteColorPicker;
import net.runelite.client.ui.components.IconTextField;
import net.runelite.client.util.ImageUtil;

public class BetterNpcHighlightPanel extends PluginPanel {

    private ScrollablePanel cardsPanel;
    private JScrollPane cardsScrollPane;
    private Runnable onDataChanged;
    private final ColorPickerManager colorPickerManager;
    private final ConfigManager configManager;
    private final List<NpcCard> npcCards = new ArrayList<>();
    private boolean loading = false;
    private final Gson gson = new Gson();
    private final String configGroup = "betterNpcHighlight";
    private static final String CARDS_CONFIG_KEY = "cards";

    private static final ImmutableList<String> TAG_STYLES = ImmutableList.of(
            "Tile",
            "True Tile",
            "SW Tile",
            "SW True Tile",
            "Hull",
            "Area",
            "Outline",
            "Clickbox",
            "Turbo"
    );

    private static final int MAX_STYLE_ROWS = 9;

    private static final int luminanceOnHover = -80;
    private static final int luminanceOffHover = -130;
    private static final int luminanceOff = -150;

    private static final IconSet ENTITY_HIDER_ICONS = loadIconSet("/entity_hider_on.png", luminanceOnHover, luminanceOff, luminanceOffHover);
    private static final IconSet DRAW_BENEATH_ICONS = loadIconSet("/draw_beneath.png", luminanceOnHover, luminanceOff, luminanceOffHover);
    private static final IconSet DISPLAY_NAME_ICONS = loadIconSet("/display_name.png", luminanceOnHover, luminanceOff, luminanceOffHover);
    private static final IconSet HIGHLIGHT_DEAD_ICONS = loadIconSet("/highlight_dead.png", luminanceOnHover, luminanceOff, luminanceOffHover);
    private static final IconSet ADD_ICONS = loadIconSet("/add_icon.png", luminanceOnHover + 30, luminanceOff, luminanceOffHover);
    private static final IconSet REMOVE_ICONS = loadIconSet("/remove_icon.png", luminanceOnHover + 30, luminanceOff, luminanceOffHover);
    private static final IconSet DELETE_ICONS = loadIconSet("/delete_icon.png", luminanceOnHover + 30, luminanceOff, luminanceOffHover);

    public BetterNpcHighlightPanel(ColorPickerManager colorPickerManager, ConfigManager configManager) {
        super(false);
        this.colorPickerManager = colorPickerManager;
        this.configManager = configManager;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(0, 4, 0, 4));
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

        // Create bottom panel with action buttons
        JPanel bottomPanel = createBottomPanel();
        contentPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(contentPanel, BorderLayout.CENTER);

        // Add initial empty card
        addNewCard();
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        topPanel.setBorder(new EmptyBorder(5, 5, 10, 5));

        // Search field
        IconTextField searchField = new IconTextField();
        searchField.setPreferredSize(new Dimension(300, 30));
        searchField.setIcon(IconTextField.Icon.SEARCH);
        searchField.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        searchField.setForeground(ColorScheme.LIGHT_GRAY_COLOR);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            private void updateFilter() {
                String text = searchField.getText().toLowerCase();
                for (NpcCard card : npcCards) {
                    boolean visible = text.isEmpty() ||
                            card.getNameText().toLowerCase().contains(text) ||
                            card.getAllTagStyles().toLowerCase().contains(text);
                    card.setVisible(visible);
                }
                cardsPanel.revalidate();
                cardsPanel.repaint();
            }
            @Override
            public void insertUpdate(DocumentEvent e) { updateFilter(); }
            @Override
            public void removeUpdate(DocumentEvent e) { updateFilter(); }
            @Override
            public void changedUpdate(DocumentEvent e) { updateFilter(); }
        });

        TAG_STYLES.forEach(searchField.getSuggestionListModel()::addElement);

        topPanel.add(searchField, BorderLayout.CENTER);
        return topPanel;
    }

    private void createCardsPanel() {
        cardsPanel = new ScrollablePanel();
        cardsPanel.setLayout(new BoxLayout(cardsPanel, BoxLayout.Y_AXIS));
        cardsPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        cardsPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

        cardsScrollPane = new JScrollPane(cardsPanel);
        cardsScrollPane.setBackground(ColorScheme.DARK_GRAY_COLOR);
        cardsScrollPane.getViewport().setBackground(ColorScheme.DARK_GRAY_COLOR);
        cardsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        cardsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        bottomPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        buttonPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

        JButton addBtn = createStyledButton("Add NPC");
        addBtn.addActionListener(e -> addNewCard());

        JButton importBtn = createStyledButton("Import");
        importBtn.addActionListener(e -> importEntries());

        JButton exportBtn = createStyledButton("Export");
        exportBtn.addActionListener(e -> exportEntries());

        JButton clearBtn = createStyledButton("Clear All");
        clearBtn.addActionListener(e -> clearAllEntries());

        buttonPanel.add(addBtn);
        buttonPanel.add(importBtn);
        buttonPanel.add(exportBtn);
        buttonPanel.add(clearBtn);

        bottomPanel.add(buttonPanel, BorderLayout.CENTER);
        return bottomPanel;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        button.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        button.setFont(FontManager.getRunescapeSmallFont());
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColorScheme.MEDIUM_GRAY_COLOR, 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(ColorScheme.DARK_GRAY_COLOR);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(ColorScheme.DARKER_GRAY_COLOR);
            }
        });

        return button;
    }

    private void addNewCard() {
        NpcCard card = new NpcCard();
        npcCards.add(card);
        cardsPanel.add(card);
        cardsPanel.add(Box.createVerticalStrut(5)); // Spacing between cards
        cardsPanel.revalidate();
        cardsPanel.repaint();

        // Auto-focus on the name field for new cards
        SwingUtilities.invokeLater(() -> card.focusNameField());
    }

    private void removeCard(NpcCard card) {
        int index = npcCards.indexOf(card);
        if (index >= 0) {
            npcCards.remove(card);
            cardsPanel.remove(card);
            // Remove the spacing component if it exists
            if (index * 2 < cardsPanel.getComponentCount()) {
                cardsPanel.remove(index * 2);
            }
        }

        // Ensure we always have at least one card
        if (npcCards.isEmpty()) {
            addNewCard();
        }

        cardsPanel.revalidate();
        cardsPanel.repaint();
        saveAllCards(configManager, configGroup);
    }

    private void triggerDataChanged() {
        System.out.println("triggerDataChanged called");
        if (onDataChanged != null) {
            onDataChanged.run();
        }
    }


    // Data management methods
    public List<NpcHighlightEntry> getNpcHighlightEntries() {
        List<NpcHighlightEntry> entries = new ArrayList<>();

        for (NpcCard card : npcCards) {
            String nameOrId = card.getNameText();
            if (nameOrId == null || nameOrId.trim().isEmpty()) {
                continue;
            }

            // Use the card's getAllEntries() method instead of accessing styleButtons directly
            entries.addAll(card.getAllEntries());
        }

        return entries;
    }

    public void setOnTableChanged(Runnable r) {
        this.onDataChanged = r;
    }

    private void addCardFromEntry(NpcHighlightEntry entry) {
        NpcCard card = new NpcCard();
        card.setData(Collections.singletonList(entry));
        npcCards.add(card);
        cardsPanel.add(card);
        cardsPanel.add(Box.createVerticalStrut(5));
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    private void addCardFromEntries(UUID cardId, String npcNameOrId, List<NpcHighlightEntry> entries) {
        NpcCard card = cardId == null ? new NpcCard() : new NpcCard(cardId);
        card.setNameText(npcNameOrId);
        card.clearStyleRows();

        for (NpcHighlightEntry entry : entries) {
            card.addStyleRow(entry);
        }

        npcCards.add(card);
        cardsPanel.add(card);
        cardsPanel.add(Box.createVerticalStrut(5));
        cardsPanel.revalidate();
        cardsPanel.repaint();
        System.out.println("Adding card with UUID: " + cardId + " and name: " + npcNameOrId);
    }

    private String entryToString(NpcHighlightEntry entry) {
        return entry.nameOrId + "|" +
                entry.tagStyle + "|" +
                entry.outlineColor.getRGB() + "|" +
                entry.fillColor.getRGB() + "|" +
                entry.hideNpc + "|" +
                entry.drawUnder + "|" +
                entry.displayName + "|" +
                entry.displayNameColor.getRGB() + "|" +
                entry.highlightDead;
    }

    private NpcHighlightEntry entryFromString(String str) {
        String[] parts = str.split("\\|");
        if (parts.length >= 9) {
            try {
                return new NpcHighlightEntry(
                        parts[0],
                        parts[1],
                        new Color(Integer.parseInt(parts[2]), true),
                        new Color(Integer.parseInt(parts[3]), true),
                        Boolean.parseBoolean(parts[4]),
                        Boolean.parseBoolean(parts[5]),
                        Boolean.parseBoolean(parts[6]),
                        new Color(Integer.parseInt(parts[7]), true),
                        Boolean.parseBoolean(parts[8])
                );
            } catch (Exception e) {
                // Handle parsing errors
            }
        }
        // Fallback for older formats
        if (parts.length >= 8) { // Update to expect 8 parts
            try {
                return new NpcHighlightEntry(
                        parts[0],
                        parts[1],
                        new Color(Integer.parseInt(parts[2]), true),
                        new Color(Integer.parseInt(parts[3]), true),
                        Boolean.parseBoolean(parts[4]),
                        Boolean.parseBoolean(parts[5]),
                        Boolean.parseBoolean(parts[6]),
                        new Color(Integer.parseInt(parts[7]), true),// Parse display name color
                        false
                );
            } catch (Exception e) {
                // Handle parsing errors
            }
        }

        // Fallback for older format or parsing errors
        if (parts.length >= 7) {
            try {
                return new NpcHighlightEntry(
                        parts[0],
                        parts[1],
                        new Color(Integer.parseInt(parts[2]), true),
                        new Color(Integer.parseInt(parts[3]), true),
                        Boolean.parseBoolean(parts[4]),
                        Boolean.parseBoolean(parts[5]),
                        Boolean.parseBoolean(parts[6]),
                        Color.WHITE, // Default display name color
                        false
                );
            } catch (Exception e) {
                // Handle parsing errors
            }
        }

        return null;
    }

    // Import/Export functionality
    private void importEntries() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV Files", "csv"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                importFromFile(fileChooser.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Import successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Import failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportEntries() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV Files", "csv"));
        fileChooser.setSelectedFile(new File("npc_highlights.csv"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                exportToFile(fileChooser.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Export successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Export failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearAllEntries() {
        if (JOptionPane.showConfirmDialog(this, "Clear all entries?", "Confirm",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            clearAllCards();
            addNewCard();
        }
    }

    private void clearAllCards() {
        npcCards.clear();
        cardsPanel.removeAll();
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    public void exportToFile(File file) throws IOException {
        List<NpcHighlightEntry> entries = getNpcHighlightEntries();
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("# Better NPC Highlight Export");
            writer.println("# Format: Name/ID,TagStyle,OutlineColor(RGB),FillColor(RGB),Hide,DrawUnder,DisplayName,HighlightDead");

            for (NpcHighlightEntry entry : entries) {
                writer.printf("%s,%s,%d,%d,%b,%b,%b,%b%n",
                        entry.nameOrId,
                        entry.tagStyle,
                        entry.outlineColor.getRGB(),
                        entry.fillColor.getRGB(),
                        entry.hideNpc,
                        entry.drawUnder,
                        entry.displayName,
                        entry.highlightDead);
            }
        }
    }

    public void importFromFile(File file) throws IOException {
        List<NpcHighlightEntry> importedEntries = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#") || line.trim().isEmpty()) continue;

                NpcHighlightEntry entry = parseImportLine(line);
                if (entry != null) {
                    importedEntries.add(entry);
                }
            }
        }

        if (!importedEntries.isEmpty()) {
            clearAllCards();
            for (NpcHighlightEntry entry : importedEntries) {
                addCardFromEntry(entry);
            }
            if (npcCards.isEmpty()) {
                addNewCard();
            }
        }
    }

    private NpcHighlightEntry parseImportLine(String line) {
        try {
            String[] parts = line.split(",");
            if (parts.length >= 4) {
                // Provide default values for missing parameters
                boolean hideNpc = false;
                boolean drawUnder = false;
                boolean displayName = false;
                Color displayNameColor = Color.CYAN; // or Color.WHITE
                boolean highlightDead = false;

                if (parts.length >= 8) {
                    hideNpc = Boolean.parseBoolean(parts[4].trim());
                    drawUnder = Boolean.parseBoolean(parts[5].trim());
                    displayName = Boolean.parseBoolean(parts[6].trim());
                    highlightDead = Boolean.parseBoolean(parts[7].trim());
                }

                return new NpcHighlightEntry(
                        parts[0].trim(),
                        parts[1].trim(),
                        new Color(Integer.parseInt(parts[2].trim()), true),
                        new Color(Integer.parseInt(parts[3].trim()), true),
                        hideNpc,
                        drawUnder,
                        displayName,
                        displayNameColor,
                        highlightDead
                );
            }
        } catch (Exception e) {
            // Skip invalid lines
        }
        return null;
    }

    public void clearAndLoadEntries(List<NpcHighlightEntry> entries) {
        clearAllCards();
        for (NpcHighlightEntry entry : entries) {
            addCardFromEntry(entry);
        }
        if (npcCards.isEmpty()) {
            addNewCard();
        }
    }

    // NPC Card Component
    public class NpcCard extends JPanel {
        private JPanel bottomRowsPanel;
        private final List<StyleRow> styleRows = new ArrayList<>();
        private JTextField nameField;
        private JComboBox<String> tagStyleCombo;
        private ColorPreviewButton colorPreviewButton;
        private JToggleButton hideNpcButton;
        private JToggleButton drawUnderButton;
        private JToggleButton displayNameButton;
        private Color displayNameColor = Color.CYAN;
        private JToggleButton highlightDeadButton;
        private final UUID cardId;

        public NpcCard() {
            this.cardId = UUID.randomUUID();
            initCard();
        }

        // Add a constructor to create card with existing UUID (for loading)
        public NpcCard(UUID id) {
            this.cardId = id;
            initCard();
        }

        public UUID getCardId() {
            return cardId;
        }

        private class StyleRow {
            JPanel panel;
            JComboBox<String> tagStyleCombo;
            ColorPreviewButton colorPreviewButton; // Combined button
            JButton addButton;
            JButton removeButton;

            StyleRow(NpcHighlightEntry entry) {
                panel = new JPanel(new BorderLayout(0, 0));
                panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
                panel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

                // Left side: tag style combo and combined color button
                JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                leftPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

                tagStyleCombo = new JComboBox<>();
                styleComboBox(tagStyleCombo);
                tagStyleCombo.addActionListener(e -> saveAllCards(configManager, configGroup));
                leftPanel.add(tagStyleCombo);
                leftPanel.add(Box.createHorizontalStrut(4));

                // Initialize combined color button with outline and fill colors
                Color initialOutline = entry != null ? entry.outlineColor : Color.CYAN;
                Color initialFill = entry != null ? entry.fillColor : new Color(0, 255, 255, 20);

                CheckerboardPanel checkerPanel = new CheckerboardPanel();
                colorPreviewButton = new ColorPreviewButton(initialOutline, initialFill, colorPickerManager);
                colorPreviewButton.setOpaque(false);
                checkerPanel.add(colorPreviewButton, BorderLayout.CENTER);
                leftPanel.add(checkerPanel);

                panel.add(leftPanel, BorderLayout.WEST);

                // Right side: ADD and REMOVE buttons
                JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
                rightPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

                addButton = new JButton();
                addButton.setPreferredSize(new Dimension(18, 24));
                addButton.setContentAreaFilled(false);
                addButton.setIcon(ADD_ICONS.on);
                addButton.setRolloverIcon(ADD_ICONS.onHover);
                addButton.setToolTipText("Add a new highlight style below");
                addButton.addActionListener(e -> {
                    if (styleRows.size() < MAX_STYLE_ROWS) {
                        int index = styleRows.indexOf(this);
                        if (index != -1) {
                            addStyleRowAt(index + 1, null);
                        }
                    }
                });


                removeButton = new JButton();
                removeButton.setPreferredSize(new Dimension(18, 24));
                removeButton.setContentAreaFilled(false);
                removeButton.setIcon(REMOVE_ICONS.on);
                removeButton.setRolloverIcon(REMOVE_ICONS.onHover);
                removeButton.setToolTipText("Remove this highlight style");
                removeButton.addActionListener(e -> {
                    if (styleRows.size() > 1) {
                        int index = styleRows.indexOf(this);
                        if (index != -1) {
                            removeStyleRowAt(index);
                        }
                    }
                });

                rightPanel.add(removeButton);
                rightPanel.add(addButton);
                panel.add(rightPanel, BorderLayout.EAST);

                // Initialize combo box options
                updateTagStyleComboBoxOptions(tagStyleCombo);
                tagStyleCombo.addActionListener(e -> refreshAllTagStyleComboBoxes());
                if (entry != null && entry.tagStyle != null) {
                    tagStyleCombo.setSelectedItem(entry.tagStyle);
                }
                // Add listener to save changes when selection changes
                tagStyleCombo.addActionListener(e -> {
                    refreshAllTagStyleComboBoxes();
                    saveAllCards(configManager, configGroup);  // Save on change
                });

                updateStyleButtons();
            }
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
                    saveAllCards(configManager, configGroup);
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    saveAllCards(configManager, configGroup);
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    saveAllCards(configManager, configGroup);
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
                    ENTITY_HIDER_ICONS.on,
                    ENTITY_HIDER_ICONS.off,
                    ENTITY_HIDER_ICONS.onHover,
                    ENTITY_HIDER_ICONS.offHover,
                    "Do not hide NPC", "Hide NPC"
            );
            drawUnderButton = createToggleButton(
                    DRAW_BENEATH_ICONS.on,
                    DRAW_BENEATH_ICONS.off,
                    DRAW_BENEATH_ICONS.onHover,
                    DRAW_BENEATH_ICONS.offHover,
                    "Do not draw overlay beneath NPC", "Draw overlay beneath NPC"
            );

            displayNameButton = createToggleButton(
                    DISPLAY_NAME_ICONS.on,
                    DISPLAY_NAME_ICONS.off,
                    DISPLAY_NAME_ICONS.onHover,
                    DISPLAY_NAME_ICONS.offHover,
                    "Do not display name above NPC", "Display name above NPC"
            );
            highlightDeadButton = createToggleButton(
                    HIGHLIGHT_DEAD_ICONS.on,
                    HIGHLIGHT_DEAD_ICONS.off,
                    HIGHLIGHT_DEAD_ICONS.onHover,
                    HIGHLIGHT_DEAD_ICONS.offHover,
                    "Do not highlight dead NPC", "Highlight dead NPC"
            );

            hideNpcButton.addItemListener(e -> {
                if (!loading) {
                    saveAllCards(configManager, configGroup);
                }
            });

            drawUnderButton.addItemListener(e -> {
                if (!loading) {
                    saveAllCards(configManager, configGroup);
                }
            });

            displayNameButton.addItemListener(e -> {
                if (!loading) {
                    saveAllCards(configManager, configGroup);
                }
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
            removeCardButton.setIcon(DELETE_ICONS.on);
            removeCardButton.setRolloverIcon(DELETE_ICONS.onHover);
            removeCardButton.setPreferredSize(new Dimension(18, 18));
            removeCardButton.setContentAreaFilled(false);
            removeCardButton.setToolTipText("Delete this NPC entry");
            removeCardButton.addActionListener(e -> removeCard(this));
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
            System.out.println("NpcCard created with UUID: " + cardId + " and name: " + getNameText());
        }

        public void addStyleRow(NpcHighlightEntry entry) {
            StyleRow row = new StyleRow(entry);
            styleRows.add(row);
            bottomRowsPanel.add(row.panel);
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
            StyleRow row = new StyleRow(entry);
            styleRows.add(index, row);
            bottomRowsPanel.add(row.panel, index);
            updateStyleButtons();
            bottomRowsPanel.revalidate();
            bottomRowsPanel.repaint();
            saveAllCards(configManager, configGroup);
        }

        public void removeStyleRowAt(int index) {
            StyleRow row = styleRows.remove(index);
            bottomRowsPanel.remove(row.panel);
            updateStyleButtons();
            bottomRowsPanel.revalidate();
            bottomRowsPanel.repaint();
            saveAllCards(configManager, configGroup);
        }

        private void showDisplayNameColorMenu(MouseEvent e) {
            JPopupMenu menu = new JPopupMenu();
            JMenuItem changeColor = new JMenuItem("Change Name/Minimap Color");
            changeColor.addActionListener(ev -> openDisplayNameColorPicker());
            menu.add(changeColor);
            menu.show(displayNameButton, e.getX(), e.getY());
        }

        private void openDisplayNameColorPicker() {
            RuneliteColorPicker picker = colorPickerManager.create(
                    SwingUtilities.getWindowAncestor(this),
                    displayNameColor,
                    "Name/Minimap Color",
                    true
            );
            Point loc = getLocationOnScreen();
            picker.setLocation(loc.x + -400, loc.y);
            picker.setOnColorChange(newColor -> {
                displayNameColor = newColor;
                if (onDataChanged != null) {
                    onDataChanged.run();
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
                        this.getDisplayNameColor(), // Use 'this' instead of 'card'
                        highlightDead
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
                saveAllCards(configManager, configGroup);
            });


            // Trigger state change listener
            button.addActionListener(e -> saveAllCards(configManager, configGroup));

            return button;
        }

        private void updateStyleButtons() {
            boolean canRemove = styleRows.size() > 1;
            boolean canAdd = styleRows.size() < MAX_STYLE_ROWS;

            for (StyleRow row : styleRows) {
                row.removeButton.setEnabled(canRemove);
                row.removeButton.setVisible(canRemove);

                row.addButton.setEnabled(canAdd);
                row.addButton.setVisible(true);

                if (!canAdd) {
                    row.addButton.setToolTipText("Maximum of " + MAX_STYLE_ROWS + " highlight styles allowed");
                } else {
                    row.addButton.setToolTipText("Add a new highlight style below");
                }
            }
        }


        private void updateTagStyleComboBoxOptions(JComboBox<String> comboBoxToUpdate) {
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

            for (String style : TAG_STYLES) {
                if (!selectedStyles.contains(style) || style.equals(currentSelection)) {
                    comboBoxToUpdate.addItem(style);
                }
            }

            if (currentSelection != null) {
                comboBoxToUpdate.setSelectedItem(currentSelection);
            }
        }


        private void refreshAllTagStyleComboBoxes() {
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
            if (!loading) {
                saveAllCards(configManager, configGroup);
            }
        }

        public boolean isDrawUnder() {
            return drawUnderButton.isSelected();
        }

        public void setDrawUnder(boolean selected) {
            drawUnderButton.setSelected(selected);
            if (!loading) {
                saveAllCards(configManager, configGroup);
            }
        }

        public boolean isDisplayName() {
            return displayNameButton.isSelected();
        }

        public void setDisplayName(boolean selected) {
            displayNameButton.setSelected(selected);
            if (!loading) {
                saveAllCards(configManager, configGroup);
            }
        }

        public boolean isHighlightDead() {
            return highlightDeadButton.isSelected();
        }

        public void setHighlightDead(boolean selected) {
            highlightDeadButton.setSelected(selected);
            if (!loading) {
                saveAllCards(configManager, configGroup);
            }
        }

        public Color getDisplayNameColor() {
            return displayNameColor;
        }

        public void setDisplayNameColor(Color color) {
            this.displayNameColor = color;
            if (!loading) {
                saveAllCards(configManager, configGroup);
            }
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

    public List<NpcCard> getNpcCards() {
        return Collections.unmodifiableList(npcCards);
    }

    public class CheckerboardPanel extends JPanel {
        private static final int CHECKER_SIZE = 8;
        private TexturePaint checkerPaint;

        public CheckerboardPanel() {
            setPreferredSize(new Dimension(24, 24));
            createCheckerPaint();
            setLayout(new BorderLayout());
        }

        private void createCheckerPaint() {
            int tileSize = CHECKER_SIZE * 2;
            BufferedImage img = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();

            Color light = new Color(192, 192, 192);
            Color dark = new Color(128, 128, 128);

            g.setColor(light);
            g.fillRect(0, 0, tileSize, tileSize);

            g.setColor(dark);
            g.fillRect(0, 0, CHECKER_SIZE, CHECKER_SIZE);
            g.fillRect(CHECKER_SIZE, CHECKER_SIZE, CHECKER_SIZE, CHECKER_SIZE);

            g.dispose();

            checkerPaint = new TexturePaint(img, new Rectangle(0, 0, tileSize, tileSize));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (checkerPaint != null) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(checkerPaint);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }

    public class ColorPreviewButton extends JButton {
        private Color outlineColor;
        private Color fillColor;

        private final ColorPickerManager colorPickerManager;

        public ColorPreviewButton(Color initialOutline, Color initialFill, ColorPickerManager colorPickerManager) {
            this.outlineColor = initialOutline;
            this.fillColor = initialFill;
            this.colorPickerManager = colorPickerManager;

            setPreferredSize(new Dimension(24, 24));
            setFocusPainted(false);
            setOpaque(true);
            setBackground(fillColor);
            setBorder(BorderFactory.createLineBorder(outlineColor, 2));
            setToolTipText("Right-click to change outline or fill color");

            // Right-click menu to choose which color to edit
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        showColorMenu(e);
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        showColorMenu(e);
                    }
                }
            });
        }

        private void showColorMenu(MouseEvent e) {
            JPopupMenu menu = new JPopupMenu();

            JMenuItem setOutline = new JMenuItem("Change outline color");
            setOutline.addActionListener(ev -> openColorPicker(true));
            menu.add(setOutline);

            JMenuItem setFill = new JMenuItem("Change fill Color");
            setFill.addActionListener(ev -> openColorPicker(false));
            menu.add(setFill);

            menu.show(this, e.getX(), e.getY());
        }

        private void openColorPicker(boolean isOutline) {
            Color initial = isOutline ? outlineColor : fillColor;
            RuneliteColorPicker picker = colorPickerManager.create(
                    SwingUtilities.getWindowAncestor(this),
                    initial,
                    isOutline ? "Outline Color" : "Fill Color",
                    false
            );
            Point loc = getLocationOnScreen();
            picker.setLocation(loc.x + -500, loc.y);
            picker.setOnColorChange(color -> {
                if (isOutline) {
                    setOutlineColor(color);
                } else {
                    setFillColor(color);
                }
                saveAllCards(configManager, configGroup);
            });

            picker.setOnClose(finalColor -> {
                if (isOutline) {
                    setOutlineColor(finalColor);
                } else {
                    setFillColor(finalColor);
                }
            });

            picker.setVisible(true);
        }

        public Color getOutlineColor() {
            return outlineColor;
        }

        public void setOutlineColor(Color outlineColor) {
            this.outlineColor = outlineColor;
            setBorder(BorderFactory.createLineBorder(outlineColor, 2));
            repaint();
        }

        public Color getFillColor() {
            return fillColor;
        }

        public void setFillColor(Color fillColor) {
            this.fillColor = fillColor;
            setBackground(fillColor);
            repaint();
        }
    }

    // A panel that wraps its contents to the width of the scroll pane
    private static class ScrollablePanel extends JPanel implements Scrollable {
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16; // For smooth scrolling
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return visibleRect.height; // For page-up/page-down
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true; // This is the key: force width to match viewport
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false; // Allow vertical scrolling
        }
    }

    private static class IconSet {
        public final ImageIcon on;
        public final ImageIcon off;
        public final ImageIcon onHover;
        public final ImageIcon offHover;

        public IconSet(ImageIcon on, ImageIcon off, ImageIcon onHover, ImageIcon offHover) {
            this.on = on;
            this.off = off;
            this.onHover = onHover;
            this.offHover = offHover;
        }
    }

    private static IconSet loadIconSet(String resourcePath, int hoverLuminance, int offLuminance, int offHoverLuminance) {
        BufferedImage base = ImageUtil.loadImageResource(BetterNpcHighlightPanel.class, resourcePath);
        return new IconSet(
                new ImageIcon(base),
                new ImageIcon(ImageUtil.luminanceOffset(base, offLuminance)),
                new ImageIcon(ImageUtil.luminanceOffset(base, hoverLuminance)),
                new ImageIcon(ImageUtil.luminanceOffset(base, offHoverLuminance))
        );
    }

    public interface ColorButton {
        Color getColor();
        void setColor(Color color);
    }

    private static class CardDTO
    {
        public String uuid;
        public String name;
        public int displayNameColor; // ARGB int
        public boolean hideNpc;
        public boolean drawUnder;
        public boolean displayName;
        public boolean highlightDead;
        public List<StyleDTO> styles = new ArrayList<>();
    }

    private static class StyleDTO
    {
        public String tagStyle;
        public int outlineColor; // ARGB int
        public int fillColor;    // ARGB int
    }

    // Save all cards as a single JSON array under configGroup -> "cards"
    public void saveAllCards(ConfigManager configManager, String configGroup)
    {
        List<CardDTO> cards = new ArrayList<>();

        for (NpcCard card : npcCards)
        {
            CardDTO dto = new CardDTO();
            dto.uuid = card.getCardId().toString();
            dto.name = card.getNameText();
            dto.displayNameColor = card.getDisplayNameColor().getRGB();
            dto.hideNpc = card.isHideNpc();
            dto.drawUnder = card.isDrawUnder();
            dto.displayName = card.isDisplayName();
            dto.highlightDead = card.isHighlightDead();

            for (NpcHighlightEntry e : card.getAllEntries())
            {
                StyleDTO s = new StyleDTO();
                s.tagStyle = e.tagStyle;
                s.outlineColor = e.outlineColor.getRGB();
                s.fillColor = e.fillColor.getRGB();
                dto.styles.add(s);
            }

            cards.add(dto);
        }

        configManager.setConfiguration(configGroup, CARDS_CONFIG_KEY, gson.toJson(cards));
        triggerDataChanged();
    }


    // Load cards from config
    public void loadAllCards(ConfigManager configManager, String configGroup)
    {
        String json = configManager.getConfiguration(configGroup, CARDS_CONFIG_KEY);
        clearAllCards();

        if (json != null && !json.isEmpty())
        {
            Type listType = new TypeToken<List<CardDTO>>(){}.getType();
            List<CardDTO> cards = gson.fromJson(json, listType);

            for (CardDTO dto : cards)
            {
                NpcCard card = new NpcCard(UUID.fromString(dto.uuid));
                card.setNameText(dto.name);
                card.clearStyleRows();

                List<NpcHighlightEntry> entries = new ArrayList<>();
                for (StyleDTO s : dto.styles)
                {
                    entries.add(new NpcHighlightEntry(
                            dto.name,
                            s.tagStyle,
                            new Color(s.outlineColor, true),
                            new Color(s.fillColor, true),
                            dto.hideNpc,
                            dto.drawUnder,
                            dto.displayName,
                            new Color(dto.displayNameColor, true),
                            dto.highlightDead
                    ));
                }

                card.setData(entries);
                npcCards.add(card);
                cardsPanel.add(card);
                cardsPanel.add(Box.createVerticalStrut(5));
            }
        }

        if (npcCards.isEmpty())
        {
            addNewCard();
        }
        triggerDataChanged();

        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    // Entry POJO (unchanged)
    public static class NpcHighlightEntry {
        public String nameOrId;
        public String tagStyle;
        public Color outlineColor;
        public Color fillColor;
        public boolean hideNpc;
        public boolean drawUnder;
        public boolean displayName;
        public Color displayNameColor;
        public boolean highlightDead;

        public NpcHighlightEntry(String nameOrId, String tagStyle, Color outlineColor, Color fillColor,
                                 boolean hideNpc, boolean drawUnder, boolean displayName, Color displayNameColor, boolean highlightDead) {
            this.nameOrId = nameOrId;
            this.tagStyle = tagStyle;
            this.outlineColor = outlineColor;
            this.fillColor = fillColor;
            this.hideNpc = hideNpc;
            this.drawUnder = drawUnder;
            this.displayName = displayName;
            this.displayNameColor = displayNameColor;
            this.highlightDead = highlightDead;
        }
    }

}
