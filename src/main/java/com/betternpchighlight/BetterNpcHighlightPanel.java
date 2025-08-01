package com.betternpchighlight;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import javax.swing.DefaultCellEditor;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventObject;
import java.util.List;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;
import net.runelite.client.ui.components.colorpicker.RuneliteColorPicker;
import net.runelite.client.ui.components.IconTextField;

public class BetterNpcHighlightPanel extends PluginPanel {

    private JTable npcTable;
    private NpcTableModel tableModel;
    private Runnable onTableChanged;
    private final ColorPickerManager colorPickerManager;
    private final Color hoveredColor = ColorScheme.DARK_GRAY_COLOR;
    private final Color selectedColor = new Color(60, 60, 60, 255);
    private TableRowSorter<NpcTableModel> rowSorter;
    private static final ImmutableList<String> STYLE_TAGS = ImmutableList.of(
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

    private static final String HIDE_NPC_TEXT = "Hide NPC (Entity Hider)";
    private static final String DRAW_UNDER_TEXT = "Draw overlay beneath NPC";
    private static final String DISPLAY_NAME_TEXT = "Display name above NPC";

    public BetterNpcHighlightPanel(ColorPickerManager colorPickerManager) {
        super(false);
        this.colorPickerManager = colorPickerManager;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(8, 6, 0, 6));
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        // Create wrapper panel that will contain everything
        JPanel contentWrapperPane = new JPanel();
        contentWrapperPane.setLayout(new BorderLayout());
        contentWrapperPane.setBackground(ColorScheme.DARK_GRAY_COLOR);

        // South anchored panel for buttons (fixed at top)
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        southPanel.setBorder(new EmptyBorder(8, 8, 8, 8));
        // Make sure it doesn't expand vertically
        southPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, southPanel.getPreferredSize().height));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        buttonPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

        JButton importBtn = createStyledButton("Import");
        importBtn.addActionListener(e -> importEntries());

        JButton exportBtn = createStyledButton("Export");
        exportBtn.addActionListener(e -> exportEntries());

        JButton clearBtn = createStyledButton("Clear All");
        clearBtn.addActionListener(e -> clearAllEntries());

        buttonPanel.add(importBtn);
        buttonPanel.add(exportBtn);
        buttonPanel.add(clearBtn);
        southPanel.add(buttonPanel, BorderLayout.CENTER);

        // Add the fixed button panel to wrapper
        contentWrapperPane.add(southPanel, BorderLayout.SOUTH);

        // Create the table
        setupTable();

        IconTextField searchField = new IconTextField();
        searchField.setPreferredSize(new Dimension(300, 30));
        searchField.setIcon(IconTextField.Icon.SEARCH);
        searchField.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        searchField.setForeground(ColorScheme.LIGHT_GRAY_COLOR);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            private void updateFilter() {
                String text = searchField.getText();
                if (text.isEmpty()) {
                    rowSorter.setRowFilter(null);
                } else {
                    // Filters columns 0 and 1 (nameOrId and tagStyle)
                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text), 0, 1));
                }
            }
            @Override
            public void insertUpdate(DocumentEvent e) { updateFilter(); }
            @Override
            public void removeUpdate(DocumentEvent e) { updateFilter(); }
            @Override
            public void changedUpdate(DocumentEvent e) { updateFilter(); }
        });

        STYLE_TAGS.forEach(searchField.getSuggestionListModel()::addElement);

        JPanel northPanel = new JPanel(new BorderLayout(5, 5));
        northPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        northPanel.add(searchField, BorderLayout.CENTER);
        contentWrapperPane.add(northPanel, BorderLayout.NORTH);

        // Create scroll pane for table
        JScrollPane tableScrollPane = new JScrollPane(npcTable);
        tableScrollPane.setBackground(ColorScheme.DARK_GRAY_COLOR);
        tableScrollPane.getViewport().setBackground(ColorScheme.DARKER_GRAY_COLOR);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(new Color(57, 57, 57, 255), 1, true));
        tableScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        tableScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel corner = new JPanel();
        corner.setBackground(ColorScheme.BRAND_ORANGE);
        corner.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(57, 57, 57, 255)));
        tableScrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, corner);

        // Add table container to wrapper - this will expand to fill remaining space
        contentWrapperPane.add(tableScrollPane, BorderLayout.CENTER);

        // Add the content wrapper to main panel
        add(contentWrapperPane, BorderLayout.CENTER);

        // Setup listeners
        setupTableChangeListener();
        setupContextMenu();
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

    private void setupTable() {
        tableModel = new NpcTableModel();
        tableModel.setParentTable(npcTable);
        npcTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);

                Object hoveredRowObj = npcTable.getClientProperty("hoveredRow");
                int hoveredRow = (hoveredRowObj instanceof Integer) ? (Integer) hoveredRowObj : -1;
                boolean isHovered = row == hoveredRow;
                boolean isSelected = npcTable.isRowSelected(row); // Check if row is selected

                Color baseColor;
                if (isSelected) {
                    // Selected row gets selection color
                    baseColor = selectedColor;
                    c.setForeground(Color.WHITE);
                } else if (isHovered) {
                    baseColor = hoveredColor;
                    c.setForeground(Color.WHITE);
                } else {
                    baseColor = (row % 2 == 0)
                            ? ColorScheme.DARKER_GRAY_COLOR
                            : new Color(27, 27, 27);
                    c.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
                }

                c.setBackground(baseColor);
                c.setFont(FontManager.getRunescapeSmallFont());

                if (c instanceof JComponent) {
                    ((JComponent) c).setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
                }

                return c;
            }

            @Override
            public boolean editCellAt(int row, int column, EventObject e) {
                // For color picker columns, always start editing immediately
                if (column == 2 || column == 3) {
                    if (super.editCellAt(row, column, e)) {
                        // Immediately trigger the color picker
                        TableCellEditor editor = getCellEditor(row, column);
                        if (editor instanceof ColorPickerCellEditor) {
                            SwingUtilities.invokeLater(() -> {
                                ((ColorPickerCellEditor) editor).openColorPickerDirectly();
                            });
                        }
                        return true;
                    }
                    return false;
                }
                return super.editCellAt(row, column, e);
            }
        };

        tableModel.setParentTable(npcTable);

        NpcTableModel model = (NpcTableModel) npcTable.getModel();
        rowSorter = new TableRowSorter<>(model);
        npcTable.setRowSorter(rowSorter);
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            rowSorter.setSortable(i, false);
        }

        // Table appearance
        npcTable.setRowHeight(25);
        npcTable.setShowGrid(false);
        npcTable.setIntercellSpacing(new Dimension(0, 1));
        npcTable.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        npcTable.setSelectionBackground(ColorScheme.DARK_GRAY_COLOR);
        npcTable.setSelectionForeground(Color.WHITE);
        npcTable.setRowSelectionAllowed(true); // Enable row selection
        npcTable.setColumnSelectionAllowed(false);
        npcTable.setCellSelectionEnabled(false);
        npcTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        npcTable.setFillsViewportHeight(true);
        npcTable.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(57, 57, 57, 255)));
        npcTable.putClientProperty("JTable.rowHover", -1);

        TableColumn nameColumn = npcTable.getColumnModel().getColumn(0);
        nameColumn.setCellEditor(new DefaultCellEditor(new JTextField() {{
            setBackground(ColorScheme.DARKER_GRAY_COLOR);
            setForeground(Color.WHITE);
            setFont(FontManager.getRunescapeSmallFont());

            Border innerBorder = BorderFactory.createEmptyBorder(0, 5, 0, 0);
            Border outerBorder = BorderFactory.createLineBorder(ColorScheme.MEDIUM_GRAY_COLOR, 1);
            setBorder(BorderFactory.createCompoundBorder(outerBorder, innerBorder));
            setCaretColor(Color.WHITE); // optional, improves contrast
        }}));


        npcTable.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = npcTable.rowAtPoint(e.getPoint());
                npcTable.putClientProperty("hoveredRow", row);
                npcTable.repaint();
            }
        });

        npcTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                npcTable.putClientProperty("hoveredRow", -1);
                npcTable.repaint();
            }
        });

        npcTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int row = npcTable.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    if (!npcTable.isRowSelected(row)) {
                        if (e.isControlDown() || e.isMetaDown()) {
                            npcTable.addRowSelectionInterval(row, row);
                        } else if (e.isShiftDown()) {
                            int anchor = npcTable.getSelectionModel().getAnchorSelectionIndex();
                            npcTable.setRowSelectionInterval(Math.min(anchor, row), Math.max(anchor, row));
                        } else {
                            npcTable.setRowSelectionInterval(row, row);
                        }
                    }
                } else {
                    npcTable.clearSelection();
                }
            }
        });

        // Delete key removes selected rows
        npcTable.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteRows");
        npcTable.getActionMap().put("deleteRows", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] selectedRows = npcTable.getSelectedRows();
                if (selectedRows.length == 0)
                {
                    return;
                }

                final int[] modelRows = Arrays.stream(selectedRows)
                        .map(npcTable::convertRowIndexToModel)
                        .sorted()
                        .toArray();

                for (int i = modelRows.length - 1; i >= 0; i--)
                {
                    tableModel.removeRow(modelRows[i]);
                }
            }
        });

        // Header styling
        JTableHeader header = npcTable.getTableHeader();
        header.setFont(FontManager.getRunescapeSmallFont());
        header.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        header.setForeground(Color.WHITE);
        header.setReorderingAllowed(false);
        header.setResizingAllowed(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(57, 57, 57)));

        setupColumns();
    }

    private void setupColumns() {
        // Tag Style dropdown
        JComboBox<String> tagStyleCombo = new JComboBox<>(NpcTableModel.TAG_STYLES);
        tagStyleCombo.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        tagStyleCombo.setForeground(Color.WHITE);
        tagStyleCombo.setFont(FontManager.getRunescapeSmallFont());

        npcTable.getColumnModel().getColumn(1).setCellEditor(new ImmediateComboBoxEditor(NpcTableModel.TAG_STYLES));

        // Color picker columns
        npcTable.getColumnModel().getColumn(2).setCellRenderer(new ColorCellRenderer());
        npcTable.getColumnModel().getColumn(2).setCellEditor(new ColorPickerCellEditor(colorPickerManager, this));

        npcTable.getColumnModel().getColumn(3).setCellRenderer(new ColorCellRenderer());
        npcTable.getColumnModel().getColumn(3).setCellEditor(new ColorPickerCellEditor(colorPickerManager, this));

        // Column widths
        TableColumnModel columnModel = npcTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(120); // Name/ID
        columnModel.getColumn(1).setPreferredWidth(80);  // Style

        // Fixed width for outline color
        TableColumn outlineCol = columnModel.getColumn(2);
        outlineCol.setPreferredWidth(28);
        outlineCol.setMinWidth(28);
        outlineCol.setMaxWidth(28);

        // Fixed width for fill color
        TableColumn fillCol = columnModel.getColumn(3);
        fillCol.setPreferredWidth(28);
        fillCol.setMinWidth(28);
        fillCol.setMaxWidth(28);

        npcTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
    }

    private void setupContextMenu() {
        JPopupMenu contextMenu = new JPopupMenu();
        contextMenu.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        contextMenu.setBorder(BorderFactory.createLineBorder(ColorScheme.MEDIUM_GRAY_COLOR));

        JMenuItem addRowItem = createMenuItem("Add Row");
        addRowItem.addActionListener(e -> {
            int selectedRow = npcTable.getSelectedRow();
            if (selectedRow != -1) {
                int modelRow = npcTable.convertRowIndexToModel(selectedRow);
                tableModel.insertEmptyRow(modelRow + 1);
            } else {
                tableModel.addEmptyRow();
            }
        });

        JMenuItem duplicateItem = createMenuItem("Duplicate Selected Row(s)");
        duplicateItem.addActionListener(e -> {
            int[] selectedRows = npcTable.getSelectedRows();
            if (selectedRows.length == 0) {
                return;
            }

            final int[] modelRows = Arrays.stream(selectedRows)
                    .map(npcTable::convertRowIndexToModel)
                    .sorted()
                    .toArray();

            for (int i = modelRows.length - 1; i >= 0; i--) {
                tableModel.duplicateRow(modelRows[i]);
            }
        });

        JMenuItem deleteItem = createMenuItem("Delete Selected Row(s)");
        deleteItem.addActionListener(e -> {
            int[] selectedRows = npcTable.getSelectedRows();

            if (selectedRows.length == 0) {
                return;
            }

            final int[] modelRows = Arrays.stream(selectedRows)
                    .map(npcTable::convertRowIndexToModel)
                    .sorted()
                    .toArray();

            for (int i = modelRows.length - 1; i >= 0; i--) {
                tableModel.removeRow(modelRows[i]);
            }
        });

        contextMenu.add(addRowItem);
        contextMenu.add(duplicateItem);
        contextMenu.add(deleteItem);
        contextMenu.addSeparator();

        JMenuItem hideNpcItem = createMenuItem(HIDE_NPC_TEXT);
        hideNpcItem.addActionListener(e -> toggleBooleanProperty("hideNpc"));

        JMenuItem drawUnderItem = createMenuItem(DRAW_UNDER_TEXT);
        drawUnderItem.addActionListener(e -> toggleBooleanProperty("drawUnder"));

        JMenuItem displayNameItem = createMenuItem(DISPLAY_NAME_TEXT);
        displayNameItem.addActionListener(e -> toggleBooleanProperty("displayName"));

        contextMenu.add(hideNpcItem);
        contextMenu.add(drawUnderItem);
        contextMenu.add(displayNameItem);

        contextMenu.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
                int[] selectedRows = npcTable.getSelectedRows();
                boolean hasSelection = selectedRows.length > 0;

                hideNpcItem.setEnabled(hasSelection);
                drawUnderItem.setEnabled(hasSelection);
                displayNameItem.setEnabled(hasSelection);

                if (hasSelection) {
                    updateMenuCheckmark(hideNpcItem, "hideNpc", selectedRows);
                    updateMenuCheckmark(drawUnderItem, "drawUnder", selectedRows);
                    updateMenuCheckmark(displayNameItem, "displayName", selectedRows);
                } else {
                    hideNpcItem.setText(HIDE_NPC_TEXT);
                    drawUnderItem.setText(DRAW_UNDER_TEXT);
                    displayNameItem.setText(DISPLAY_NAME_TEXT);
                }
            }

            @Override
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {}

            @Override
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {}
        });

        npcTable.setComponentPopupMenu(contextMenu);
    }

    private void toggleBooleanProperty(String propertyName) {
        int[] selectedRows = npcTable.getSelectedRows();
        if (selectedRows.length == 0) return;

        final int[] modelRows = Arrays.stream(selectedRows)
                .map(npcTable::convertRowIndexToModel)
                .toArray();

        boolean allEnabled = Arrays.stream(modelRows).allMatch(modelRow -> {
            NpcTableModel.NpcRow row = tableModel.getRow(modelRow);
            if (row == null) return false;
            switch (propertyName) {
                case "hideNpc": return row.hideNpc;
                case "drawUnder": return row.drawUnder;
                case "displayName": return row.displayName;
                default: return false;
            }
        });

        boolean newValue = !allEnabled;

        for (int modelRow : modelRows) {
            NpcTableModel.NpcRow row = tableModel.getRow(modelRow);
            if (row != null) {
                switch (propertyName) {
                    case "hideNpc": row.hideNpc = newValue; break;
                    case "drawUnder": row.drawUnder = newValue; break;
                    case "displayName": row.displayName = newValue; break;
                }
            }
        }
        tableModel.fireTableDataChanged();
        if (onTableChanged != null) {
            onTableChanged.run();
        }
    }

    private void updateMenuCheckmark(JMenuItem item, String propertyName, int[] selectedRows) {
        final int[] modelRows = Arrays.stream(selectedRows)
                .map(npcTable::convertRowIndexToModel)
                .toArray();

        boolean allEnabled = Arrays.stream(modelRows).allMatch(modelRow -> {
            NpcTableModel.NpcRow row = tableModel.getRow(modelRow);
            if (row == null) return false;
            switch (propertyName) {
                case "hideNpc": return row.hideNpc;
                case "drawUnder": return row.drawUnder;
                case "displayName": return row.displayName;
                default: return false;
            }
        });

        String baseText;
        switch (propertyName) {
            case "hideNpc": baseText = HIDE_NPC_TEXT; break;
            case "drawUnder": baseText = DRAW_UNDER_TEXT; break;
            case "displayName": baseText = DISPLAY_NAME_TEXT; break;
            default: baseText = item.getText().replace(" ✔️", "");
        }

        if (allEnabled) {
            item.setText(baseText + " ✔️");
        } else {
            item.setText(baseText);
        }
    }

    private JMenuItem createMenuItem(String text) {
        JMenuItem item = new JMenuItem(text);
        item.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        item.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        item.setFont(FontManager.getRunescapeSmallFont());
        return item;
    }

    // Data management methods
    public List<NpcHighlightEntry> getNpcHighlightEntries() {
        List<NpcHighlightEntry> entries = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String nameOrId = (String) tableModel.getValueAt(i, 0);
            if (nameOrId != null && !nameOrId.trim().isEmpty()) {
                NpcHighlightEntry entry = new NpcHighlightEntry(
                        nameOrId.trim(),
                        (String) tableModel.getValueAt(i, 1),
                        (Color) tableModel.getValueAt(i, 2),
                        (Color) tableModel.getValueAt(i, 3)
                );
                NpcTableModel.NpcRow row = tableModel.getRow(i);
                if (row != null) {
                    entry.hideNpc = row.hideNpc;
                    entry.drawUnder = row.drawUnder;
                    entry.displayName = row.displayName;
                }
                entries.add(entry);
            }
        }
        return entries;
    }

    public void setOnTableChanged(Runnable r) {
        this.onTableChanged = r;
    }

    private void setupTableChangeListener() {
        tableModel.addTableModelListener(e -> {
            if (onTableChanged != null) {
                onTableChanged.run();
            }
        });
    }

    // Persistence methods
    public void saveToConfig(ConfigManager configManager, String configGroup) {
        List<NpcHighlightEntry> entries = getNpcHighlightEntries();
        StringBuilder sb = new StringBuilder();

        for (NpcHighlightEntry entry : entries) {
            sb.append(entryToString(entry)).append(";");
        }

        configManager.setConfiguration(configGroup, "panelEntries", sb.toString());
    }

    public void loadFromConfig(ConfigManager configManager, String configGroup) {
        String entriesStr = configManager.getConfiguration(configGroup, "panelEntries");
        if (entriesStr != null && !entriesStr.isEmpty()) {
            tableModel.clearData();

            String[] entries = entriesStr.split(";");
            for (String entryStr : entries) {
                if (!entryStr.trim().isEmpty()) {
                    NpcHighlightEntry entry = stringToEntry(entryStr);
                    if (entry != null) {
                        tableModel.addEntry(entry);
                    }
                }
            }
        }

        if (tableModel.getRowCount() == 0) {
            tableModel.addEmptyRow();
        }
    }

    private String entryToString(NpcHighlightEntry entry) {
        return String.format("%s|%s|%d|%d|%b|%b|%b",
                entry.nameOrId,
                entry.tagStyle,
                entry.outlineColor.getRGB(),
                entry.fillColor.getRGB(),
                entry.hideNpc,
                entry.drawUnder,
                entry.displayName);
    }

    private NpcHighlightEntry stringToEntry(String str) {
        try {
            String[] parts = str.split("\\|");
            if (parts.length >= 4) {
                NpcHighlightEntry entry = new NpcHighlightEntry(
                        parts[0],
                        parts[1],
                        new Color(Integer.parseInt(parts[2]), true),
                        new Color(Integer.parseInt(parts[3]), true)
                );
                if (parts.length >= 7) {
                    entry.hideNpc = Boolean.parseBoolean(parts[4]);
                    entry.drawUnder = Boolean.parseBoolean(parts[5]);
                    entry.displayName = Boolean.parseBoolean(parts[6]);
                }
                return entry;
            }
        } catch (Exception e) {
            // Invalid entry format, skip
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
            tableModel.clearData();
            tableModel.addEmptyRow();
        }
    }

    public void exportToFile(File file) throws IOException {
        List<NpcHighlightEntry> entries = getNpcHighlightEntries();
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("# Better NPC Highlight Export");
            writer.println("# Format: Name/ID,TagStyle,OutlineColor(RGB),FillColor(RGB),Hide,DrawUnder,DisplayName");

            for (NpcHighlightEntry entry : entries) {
                writer.printf("%s,%s,%d,%d,%b,%b,%b%n",
                        entry.nameOrId,
                        entry.tagStyle,
                        entry.outlineColor.getRGB(),
                        entry.fillColor.getRGB(),
                        entry.hideNpc,
                        entry.drawUnder,
                        entry.displayName);
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
            tableModel.clearData();
            for (NpcHighlightEntry entry : importedEntries) {
                tableModel.addEntry(entry);
            }
            tableModel.addEmptyRow();
        }
    }

    private NpcHighlightEntry parseImportLine(String line) {
        try {
            String[] parts = line.split(",");
            if (parts.length >= 4) {
                NpcHighlightEntry entry = new NpcHighlightEntry(
                        parts[0].trim(),
                        parts[1].trim(),
                        new Color(Integer.parseInt(parts[2].trim()), true),
                        new Color(Integer.parseInt(parts[3].trim()), true)
                );
                if (parts.length >= 7) {
                    entry.hideNpc = Boolean.parseBoolean(parts[4].trim());
                    entry.drawUnder = Boolean.parseBoolean(parts[5].trim());
                    entry.displayName = Boolean.parseBoolean(parts[6].trim());
                }
                return entry;
            }
        } catch (Exception e) {
            // Skip invalid lines
        }
        return null;
    }

    public void clearAndLoadEntries(List<NpcHighlightEntry> entries) {
        tableModel.clearData();
        for (NpcHighlightEntry entry : entries) {
            tableModel.addEntry(entry);
        }
        if (tableModel.getRowCount() == 0) {
            tableModel.addEmptyRow();
        }
    }

    // Table Model
    static class NpcTableModel extends AbstractTableModel {
        static final String[] COLUMN_NAMES = {"Name/ID", "Style", "□", "■"};
        static final String[] TAG_STYLES = {"Tile", "True Tile", "SW Tile", "SW True Tile", "Hull", "Area", "Outline", "Clickbox", "Turbo"};
        private final List<NpcRow> data = new ArrayList<>();
        private JTable parentTable; // Add this field

        public NpcTableModel() {
            addEmptyRow();
        }

        public NpcRow getRow(int row) {
            if (row >= 0 && row < data.size()) {
                return data.get(row);
            }
            return null;
        }

        // Add this method to set the table reference
        public void setParentTable(JTable table) {
            this.parentTable = table;
        }

        public void addEmptyRow() {
            data.add(new NpcRow());
            fireTableRowsInserted(data.size() - 1, data.size() - 1);
        }

        public void insertEmptyRow(int row) {
            if (row >= 0 && row <= data.size()) {
                data.add(row, new NpcRow());
                fireTableRowsInserted(row, row);
            }
        }

        public void addEntry(NpcHighlightEntry entry) {
            NpcRow row = new NpcRow();
            row.nameOrId = entry.nameOrId;
            row.tagStyle = entry.tagStyle;
            row.outlineColor = entry.outlineColor;
            row.fillColor = entry.fillColor;
            row.hideNpc = entry.hideNpc;
            row.drawUnder = entry.drawUnder;
            row.displayName = entry.displayName;
            data.add(row);
            fireTableRowsInserted(data.size() - 1, data.size() - 1);
        }

        public void clearData() {
            int size = data.size();
            data.clear();
            if (size > 0) {
                fireTableRowsDeleted(0, size - 1);
            }
        }

        public void removeRow(int row) {
            if (row >= 0 && row < data.size()) {
                data.remove(row);
                fireTableRowsDeleted(row, row);

                if (data.isEmpty()) {
                    addEmptyRow();
                }
            }
        }

        public void duplicateRow(int row) {
            if (row >= 0 && row < data.size()) {
                NpcRow original = data.get(row);
                NpcRow duplicate = new NpcRow();
                duplicate.nameOrId = original.nameOrId;
                duplicate.tagStyle = original.tagStyle;
                duplicate.outlineColor = original.outlineColor;
                duplicate.fillColor = original.fillColor;
                duplicate.hideNpc = original.hideNpc;
                duplicate.drawUnder = original.drawUnder;
                duplicate.displayName = original.displayName;

                data.add(row + 1, duplicate);
                fireTableRowsInserted(row + 1, row + 1);
            }
        }

        @Override
        public int getRowCount() { return data.size(); }

        @Override
        public int getColumnCount() { return COLUMN_NAMES.length; }

        @Override
        public String getColumnName(int col) { return COLUMN_NAMES[col]; }

        @Override
        public boolean isCellEditable(int row, int col) {
            return true;
        }

        @Override
        public Object getValueAt(int row, int col) {
            if (row >= data.size()) return null;

            NpcRow npc = data.get(row);
            switch (col) {
                case 0: return npc.nameOrId;
                case 1: return npc.tagStyle;
                case 2: return npc.outlineColor;
                case 3: return npc.fillColor;
            }
            return null;
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            if (row >= data.size()) return;

            NpcRow npc = data.get(row);
            switch (col) {
                case 0:
                    npc.nameOrId = (String)value;
                    // Check if this is the last row and we're adding content
                    if (row == data.size() - 1 && value != null && !((String)value).trim().isEmpty()) {
                        SwingUtilities.invokeLater(() -> {
                            addEmptyRow();
                            // Immediately start editing the new row
                            if (parentTable != null) {
                                SwingUtilities.invokeLater(() -> {
                                    int newRow = data.size() - 1;
                                    parentTable.setRowSelectionInterval(newRow, newRow);
                                    parentTable.editCellAt(newRow, 0);

                                    // Scroll to make the new row visible
                                    Rectangle cellRect = parentTable.getCellRect(newRow, 0, true);
                                    parentTable.scrollRectToVisible(cellRect);

                                    if (parentTable.getEditorComponent() instanceof JTextField) {
                                        parentTable.getEditorComponent().requestFocus();
                                    }
                                });
                            }
                        });
                    }
                    break;
                case 1: npc.tagStyle = (String)value; break;
                case 2: npc.outlineColor = (Color)value; break;
                case 3: npc.fillColor = (Color)value; break;
            }
            fireTableCellUpdated(row, col);
        }
        static class NpcRow {
            String nameOrId = "";
            String tagStyle = TAG_STYLES[0];
            Color outlineColor = Color.CYAN;
            Color fillColor = new Color(0, 255, 255, 20);
            boolean hideNpc = false;
            boolean drawUnder = false;
            boolean displayName = false;
        }
    }

    public class ColorPreviewPanel extends JPanel {
        private Color color;
        private boolean isSelected;

        public ColorPreviewPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(20, 20));
        }

        public void setColor(Color color) {
            this.color = color;
            repaint();
        }

        protected Color getColor() {
            return color;
        }

        protected Paint getCheckerPaint() {
            return createCheckerPaint();
        }


        public void setHighlightState(boolean selectedOrHovered) {
            this.isSelected = selectedOrHovered;
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            g2.setColor(getBackground());
            g2.fillRect(0, 0, getWidth(), getHeight());

            if (color != null) {
                int margin = isSelected ? 1 : 2;
                int x = margin;
                int y = margin;
                int width = getWidth() - margin * 2;
                int height = getHeight() - margin * 2;
                int arc = 8;

                Shape roundRect = new RoundRectangle2D.Float(x, y, width, height, arc, arc);

                // Checkerboard
                g2.setPaint(createCheckerPaint());
                g2.fill(roundRect);

                // Fill color
                g2.setColor(color);
                g2.fill(roundRect);

                // Border
                g2.setColor(getBackground());
                g2.setStroke(new BasicStroke(3f));
                g2.draw(new RoundRectangle2D.Float(
                        x + 0.5f, y + 0.5f,
                        width - 1f, height - 1f,
                        arc, arc
                ));
            }

            g2.dispose();
        }

        private TexturePaint createCheckerPaint() {
            int size = 8;
            int tileSize = size * 2;
            BufferedImage tile = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB_PRE);
            Graphics2D g = tile.createGraphics();

            g.setColor(new Color(220, 220, 220));
            g.fillRect(0, 0, tileSize, tileSize);

            g.setColor(new Color(180, 180, 180));
            g.fillRect(0, 0, size, size);
            g.fillRect(size, size, size, size);
            g.dispose();

            return new TexturePaint(tile, new Rectangle(0, 0, tileSize, tileSize));
        }
    }


    // Color cell editor
    // Color cell editor
    public class ColorPickerCellEditor extends AbstractCellEditor implements TableCellEditor {
        private final ColorPreviewPanel previewPanel = new ColorPreviewPanel();
        private final ColorPickerManager colorPickerManager;
        private final Component parent;
        private Color currentColor;
        private RuneliteColorPicker activeColorPicker;
        private int currentEditingRow = -1; // Track which row we're editing
        private int currentEditingColumn = -1; // Track which column we're editing

        public ColorPickerCellEditor(ColorPickerManager colorPickerManager, Component parent) {
            this.colorPickerManager = colorPickerManager;
            this.parent = parent;

            previewPanel.setToolTipText("Click to open color picker");
            previewPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    openColorPicker();
                }
            });
        }

        public void openColorPickerDirectly() {
            openColorPicker();
        }

        private void openColorPicker() {
            if (activeColorPicker != null) {
                activeColorPicker.dispose();
                activeColorPicker = null;
            }

            // Store the initial color to detect if it actually changed
            final Color initialColor = currentColor;
            final int editingRow = currentEditingRow;
            final int editingColumn = currentEditingColumn;

            activeColorPicker = colorPickerManager.create(
                    SwingUtilities.getWindowAncestor(parent),
                    currentColor,
                    "Choose Color",
                    false
            );

            activeColorPicker.setOnColorChange(newColor -> {
                // Only update if we're still editing the same cell
                if (editingRow == currentEditingRow && editingColumn == currentEditingColumn) {
                    currentColor = newColor;
                    previewPanel.setColor(currentColor);
                }
            });

            activeColorPicker.setOnClose(finalColor -> {
                // Only update if we're still editing the same cell and the color actually changed
                if (editingRow == currentEditingRow && editingColumn == currentEditingColumn) {
                    currentColor = finalColor;
                    previewPanel.setColor(currentColor);
                    activeColorPicker = null;
                    fireEditingStopped();
                } else {
                    // If we're not editing the same cell anymore, just dispose
                    activeColorPicker = null;
                }
            });

            activeColorPicker.setVisible(true);
        }

        @Override
        public Object getCellEditorValue() {
            return currentColor;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            currentColor = (value instanceof Color) ? (Color) value : Color.WHITE;
            previewPanel.setColor(currentColor);

            // Store which cell we're editing
            currentEditingRow = row;
            currentEditingColumn = column;

            // Ensure the row is selected when we start editing a color cell
            if (!table.isRowSelected(row)) {
                table.setRowSelectionInterval(row, row);
            }

            // Set the background to match the current state
            Color background;
            Object hoveredObj = table.getClientProperty("hoveredRow");
            int hoveredRow = (hoveredObj instanceof Integer) ? (Integer) hoveredObj : -1;
            boolean isHovered = (hoveredRow == row);
            boolean isRowSelected = table.isRowSelected(row);

            // Check both row selection AND cell selection (editing state)
            if (isRowSelected || isSelected) {
                // Selected row or cell gets selection color - use the same color as renderer
                background = BetterNpcHighlightPanel.this.selectedColor;
            } else if (isHovered) {
                // Use the same hover color as renderer
                background = BetterNpcHighlightPanel.this.hoveredColor;
            } else {
                // Apply same zebra pattern as the renderer
                background = (row % 2 == 0)
                        ? ColorScheme.DARKER_GRAY_COLOR
                        : new Color(27, 27, 27);
            }

            previewPanel.setBackground(background);
            previewPanel.setHighlightState(isRowSelected || isSelected || isHovered);
            return previewPanel;
        }

        @Override
        public boolean stopCellEditing() {
            if (activeColorPicker != null) {
                activeColorPicker.dispose();
                activeColorPicker = null;
            }
            currentEditingRow = -1;
            currentEditingColumn = -1;
            return super.stopCellEditing();
        }

        @Override
        public void cancelCellEditing() {
            if (activeColorPicker != null) {
                activeColorPicker.dispose();
                activeColorPicker = null;
            }
            currentEditingRow = -1;
            currentEditingColumn = -1;
            super.cancelCellEditing();
        }
    }


    public class ColorCellRenderer extends ColorPreviewPanel implements TableCellRenderer {
        private boolean isSelected;
        private boolean isHovered;
        private int currentRow;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean selected, boolean hasFocus,
                                                       int row, int column) {
            Color color = (value instanceof Color) ? (Color) value : Color.WHITE;
            setColor(color);

            // Check both row selection AND cell selection (for editing state)
            this.isSelected = table.isRowSelected(row) || selected;
            this.currentRow = row;

            // Detect hovered row
            int hoveredRow = -1;
            Object hoveredObj = table.getClientProperty("hoveredRow");
            if (hoveredObj instanceof Integer) {
                hoveredRow = (Integer) hoveredObj;
            }
            this.isHovered = (hoveredRow == row);

            // Set tooltip for RGBA
            setToolTipText(String.format("RGBA(%d, %d, %d, %d)",
                    color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()));

            repaint();
            return this;
        }

        @Override
        public void paintComponent(Graphics g) {
            Color background;
            if (isSelected) {
                // Selected row gets selection color
                background = selectedColor;
            } else if (isHovered) {
                background = hoveredColor;
            } else {
                // Apply zebra pattern based on row number
                background = (currentRow % 2 == 0)
                        ? ColorScheme.DARKER_GRAY_COLOR
                        : new Color(27, 27, 27);
            }

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(background);
            g2.fillRect(0, 0, getWidth(), getHeight());

            Color color = getColor();
            if (color != null) {
                int margin = (isSelected || isHovered) ? 1 : 2;
                int x = margin;
                int y = margin;
                int width = getWidth() - margin * 2;
                int height = getHeight() - margin * 2;
                int arc = 8;

                Shape roundRect = new RoundRectangle2D.Float(x, y, width, height, arc, arc);

                // Checkerboard background for transparency effect
                g2.setPaint(getCheckerPaint());
                g2.fill(roundRect);

                // Fill with actual color
                g2.setColor(color);
                g2.fill(roundRect);

                // Border
                g2.setColor(background);
                g2.setStroke(new BasicStroke(3f));
                g2.draw(new RoundRectangle2D.Float(
                        x + 0.5f, y + 0.5f,
                        width - 1f, height - 1f,
                        arc, arc
                ));
            }

            g2.dispose();
        }
    }

    public class ImmediateComboBoxEditor extends DefaultCellEditor {
        private final JComboBox<String> comboBox;

        public ImmediateComboBoxEditor(String[] items) {
            super(new JComboBox<>(items));
            comboBox = (JComboBox<String>) getComponent();
            comboBox.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            Component comp = super.getTableCellEditorComponent(table, value, isSelected, row, column);

            comboBox.setFont(FontManager.getRunescapeSmallFont());

            // Set selected value
            comboBox.setSelectedItem(value);

            // Delay dropdown opening to allow editor to initialize
            SwingUtilities.invokeLater(() -> comboBox.showPopup());

            return comp;
        }
    }



    // Entry POJO
    public static class NpcHighlightEntry {
        public String nameOrId;
        public String tagStyle;
        public Color outlineColor;
        public Color fillColor;

        public boolean hideNpc;
        public boolean drawUnder;
        public boolean displayName;

        public NpcHighlightEntry(String nameOrId, String tagStyle, Color outlineColor, Color fillColor) {
            this.nameOrId = nameOrId;
            this.tagStyle = tagStyle;
            this.outlineColor = outlineColor;
            this.fillColor = fillColor;
            this.hideNpc = false;
            this.drawUnder = false;
            this.displayName = false;
        }
    }

}
