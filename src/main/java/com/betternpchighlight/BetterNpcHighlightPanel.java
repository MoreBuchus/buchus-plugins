package com.betternpchighlight;

import com.betternpchighlight.ui.ScrollablePanel;
import com.betternpchighlight.ui.NpcCard;
import com.betternpchighlight.data.CardDTO;
import com.betternpchighlight.data.NpcHighlightEntry;
import com.betternpchighlight.data.StyleDTO;
import com.betternpchighlight.util.IconSet;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.IconTextField;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;
import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class BetterNpcHighlightPanel extends PluginPanel {

    private ScrollablePanel cardsPanel;
    private JScrollPane cardsScrollPane;
    private Runnable onDataChanged;
    private final ColorPickerManager colorPickerManager;
    private final ConfigManager configManager;
    private final BetterNpcHighlightPlugin plugin;
    private final List<NpcCard> npcCards = new ArrayList<>();

    private final Gson gson = new Gson();
    private final String configGroup = "betterNpcHighlight";
    private static final String CARDS_CONFIG_KEY = "cards";

    public static final ImmutableList<String> TAG_STYLES = ImmutableList.of(
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

    public static final int MAX_STYLE_ROWS = 9;

    private static final int luminanceOnHover = -80;
    private static final int luminanceOffHover = -130;
    private static final int luminanceOff = -150;

    public static final IconSet ENTITY_HIDER_ICONS = loadIconSet("/entity_hider_on.png", luminanceOnHover, luminanceOff, luminanceOffHover);
    public static final IconSet DRAW_BENEATH_ICONS = loadIconSet("/draw_beneath.png", luminanceOnHover, luminanceOff, luminanceOffHover);
    public static final IconSet DISPLAY_NAME_ICONS = loadIconSet("/display_name.png", luminanceOnHover, luminanceOff, luminanceOffHover);
    public static final IconSet HIGHLIGHT_DEAD_ICONS = loadIconSet("/highlight_dead.png", luminanceOnHover, luminanceOff, luminanceOffHover);
    public static final IconSet ADD_ICONS = loadIconSet("/add_icon.png", luminanceOnHover + 30, luminanceOff, luminanceOffHover);
    public static final IconSet REMOVE_ICONS = loadIconSet("/remove_icon.png", luminanceOnHover + 30, luminanceOff, luminanceOffHover);
    public static final IconSet DELETE_ICONS = loadIconSet("/delete_icon.png", luminanceOnHover + 30, luminanceOff, luminanceOffHover);

    public BetterNpcHighlightPanel(ColorPickerManager colorPickerManager, ConfigManager configManager, BetterNpcHighlightPlugin plugin) {
        super(false);
        this.colorPickerManager = colorPickerManager;
        this.configManager = configManager;
        this.plugin = plugin;
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

        JButton clearBtn = createStyledButton("Clear All");
        clearBtn.addActionListener(e -> clearAllEntries());

        buttonPanel.add(addBtn);
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
        NpcCard card = new NpcCard(this, configManager, colorPickerManager);
        npcCards.add(card);
        cardsPanel.add(card);
        cardsPanel.add(Box.createVerticalStrut(5)); // Spacing between cards
        cardsPanel.revalidate();
        cardsPanel.repaint();

        // Auto-focus on the name field for new cards
        SwingUtilities.invokeLater(() -> card.focusNameField());
    }

    public void removeCard(NpcCard card) {
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
        NpcCard card = new NpcCard(this, configManager, colorPickerManager);
        card.setData(Collections.singletonList(entry));
        npcCards.add(card);
        cardsPanel.add(card);
        cardsPanel.add(Box.createVerticalStrut(5));
        cardsPanel.revalidate();
        cardsPanel.repaint();
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

    private void clearAndLoadEntries(List<NpcHighlightEntry> entries) {
        clearAllCards();
        for (NpcHighlightEntry entry : entries) {
            addCardFromEntry(entry);
        }
        if (npcCards.isEmpty()) {
            addNewCard();
        }
    }

    public List<NpcCard> getNpcCards() {
        return Collections.unmodifiableList(npcCards);
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

    // Save all cards as a single JSON array under configGroup -> "cards"
    public void saveAllCards(ConfigManager configManager, String configGroup)
    {
        List<CardDTO> cards = new ArrayList<>();

        for (NpcCard card : npcCards)
        {
            CardDTO dto = new CardDTO();
            dto.uuid = card.getCardId().toString();
            dto.name = card.getNameText();

            // Handle null displayNameColor
            if (card.getDisplayNameColor() != null) {
                dto.displayNameColor = card.getDisplayNameColor().getRGB();
                dto.hasCustomDisplayNameColor = true;
            } else {
                dto.displayNameColor = Color.CYAN.getRGB(); // Default value for storage only
                dto.hasCustomDisplayNameColor = false;
            }

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
                s.raveOutline = e.raveOutline;
                s.raveFill = e.raveFill;
                s.raveSpeed = e.raveSpeed;
                s.tileStyle = e.tileStyle.name();
                // New properties
                s.outlineWidth = e.outlineWidth;
                s.antiAliasing = e.antiAliasing;
                s.outlineFeather = e.outlineFeather;
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
                NpcCard card = new NpcCard(this, configManager, colorPickerManager, UUID.fromString(dto.uuid));
                card.setNameText(dto.name);
                card.clearStyleRows();

                List<NpcHighlightEntry> entries = new ArrayList<>();
                for (StyleDTO s : dto.styles)
                {
                    Color displayNameColor;
                    if (dto.hasCustomDisplayNameColor) {
                        displayNameColor = new Color(dto.displayNameColor, true);
                    } else {
                        displayNameColor = null; // Use null to fall back to primary highlight color
                    }
                    int raveSpeed = s.raveSpeed == 0 ? 6000 : s.raveSpeed;
                    HighlightColor.TileStyle tileStyle = s.tileStyle == null ? HighlightColor.TileStyle.REGULAR : HighlightColor.TileStyle.valueOf(s.tileStyle);
                    // Use values from s, HighlightColor constructor will apply defaults if s has default values (e.g., 0.0, false, 0)
                    entries.add(new NpcHighlightEntry(
                            dto.name,
                            s.tagStyle,
                            new Color(s.outlineColor, true),
                            new Color(s.fillColor, true),
                            dto.hideNpc,
                            dto.drawUnder,
                            dto.displayName,
                            displayNameColor,
                            dto.highlightDead,
                            s.raveOutline,
                            s.raveFill,
                            raveSpeed,
                            tileStyle,
                            s.outlineWidth, // Will be 0.0 if not in old JSON
                            s.antiAliasing, // Will be false if not in old JSON
                            s.outlineFeather // Will be 0 if not in old JSON
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

    public BetterNpcHighlightPlugin getPlugin() {
        return plugin;
    }

    public Runnable getOnDataChanged() {
        return onDataChanged;
    }
}