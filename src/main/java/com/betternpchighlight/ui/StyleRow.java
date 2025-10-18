package com.betternpchighlight.ui;

import com.betternpchighlight.HighlightInfo;
import com.betternpchighlight.TagStyle;
import com.betternpchighlight.data.NpcHighlightEntry;
import com.betternpchighlight.ui.dropdownbutton.DropDownButtonFactory;
import com.betternpchighlight.ui.highlightpreviewpanel.HighlightPreviewPanel;
import com.betternpchighlight.util.IconSet;
import lombok.Getter;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;
import net.runelite.client.ui.components.colorpicker.RuneliteColorPicker;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.Objects;
import java.util.function.Consumer;

public class StyleRow extends JPanel {
    // Constants
    private static final int COMPONENT_SIZE = 24;
    private static final int TOP_PADDING = 4;

    // Default values
    private static final Color DEFAULT_OUTLINE_COLOR = Color.CYAN;
    private static final Color DEFAULT_FILL_COLOR = new Color(0, 255, 255, 20);
    private static final boolean DEFAULT_RAVE_OUTLINE = false;
    private static final boolean DEFAULT_RAVE_FILL = false;
    private static final int DEFAULT_RAVE_SPEED = 6000;
    private static final double DEFAULT_OUTLINE_WIDTH = 2.0;
    private static final boolean DEFAULT_ANTI_ALIASING = true;
    private static final int DEFAULT_OUTLINE_FEATHER = 2;
    private static final HighlightInfo.LineType DEFAULT_TILE_STYLE = HighlightInfo.LineType.REGULAR;

    // Icon sets
    public static final IconSet REMOVE_STYLE_ICONS = IconSet.loadIconSet("/remove_style.png");
    public static final IconSet EDIT_STYLE_ICONS = IconSet.loadIconSet("/edit_style.png");

    // UI Components
    private JLabel tagStyleLabel;
    @Getter
    private TagStyle tagStyle;
    @Getter
    private HighlightPreviewPanel highlightPreviewPanel;
    private JPanel highlightPreviewPanelWrapper;
    @Getter
    private JButton removeButton;
    private JButton editButton;
    private JPopupMenu editButtonMenu;

    // Dependencies
    private final NpcCard parentCard;
    private final ColorPickerManager colorPickerManager;

    // Constructor
    public StyleRow(NpcCard npcCard, ColorPickerManager colorPickerManager, NpcHighlightEntry entry) {
        super();
        this.parentCard = npcCard;
        this.colorPickerManager = colorPickerManager;

        if (entry == null || entry.tagStyle == null) {
            throw new IllegalArgumentException("StyleRow must be initialized with a valid TagStyle.");
        }

        initializeRowLayout();
        createComponents(entry);
        layoutComponents();
        applyEntryData(entry);
        styleActionButton(editButton, EDIT_STYLE_ICONS, "Edit highlight style");
        updateComponentStates();
    }

    // Initialization Methods
    private void initializeRowLayout() {
        setBackground(ColorScheme.DARKER_GRAY_COLOR);
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, ColorScheme.DARK_GRAY_COLOR), BorderFactory.createEmptyBorder(TOP_PADDING, 0, TOP_PADDING, 0)));
        setLayout(new BorderLayout());
    }

    private void createComponents(NpcHighlightEntry entry) {
        createTagStyleLabel();
        createHighlightPreviewPanel(entry);
        editButtonMenu = createEditButtonMenu(); // Menu is now built dynamically
        createActionButtons();
    }

    private void createTagStyleLabel() {
        tagStyleLabel = new JLabel();
        styleTagStyleLabel(tagStyleLabel);
    }

    private JPopupMenu createEditButtonMenu() {
        JPopupMenu menu = new JPopupMenu();
        menu.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                menu.removeAll();
                rebuildEditButtonMenu(menu, tagStyle);
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        });
        return menu;
    }

    private void rebuildEditButtonMenu(JPopupMenu menu, TagStyle tagStyle) {
        addMenuItems_Color(menu, tagStyle);
        addMenuItems_Dimensions(menu, tagStyle);
        menu.addSeparator();
        addMenuItems_Rave(menu, tagStyle);
        addMenuItems_Style(menu, tagStyle);
    }

    private void addMenuItems_Color(JPopupMenu menu, TagStyle tagStyle) {
        if (tagStyle != TagStyle.AREA) {
            JMenuItem setOutline = new JMenuItem("Outline color");
            setOutline.addActionListener(e -> openColorPicker(true));
            menu.add(setOutline);
        }

        if (tagStyle != TagStyle.OUTLINE) {
            JMenuItem setFill = new JMenuItem("Fill color");
            setFill.addActionListener(e -> openColorPicker(false));
            menu.add(setFill);
        }
    }

    private void addMenuItems_Dimensions(JPopupMenu menu, TagStyle tagStyle) {
        if (tagStyle == null) return;

        if (tagStyle.toString().contains("Tile") || tagStyle == TagStyle.HULL || tagStyle == TagStyle.CLICKBOX) {
            DecimalFormat df = new DecimalFormat("#.#");
            JMenuItem setWidth = new JMenuItem("Outline width: " + df.format(highlightPreviewPanel.getOutlineWidth()));
            setWidth.addActionListener(e -> showOutlineWidthDialog(0.0, 0.1));
            menu.add(setWidth);
        } else if (tagStyle == TagStyle.OUTLINE) {
            JMenuItem setWidth = new JMenuItem("Outline width: " + (int) highlightPreviewPanel.getOutlineWidth());
            setWidth.addActionListener(e -> showOutlineWidthDialog(1.0, 1.0));
            menu.add(setWidth);

            JMenuItem setFeather = new JMenuItem("Outline feather: " + highlightPreviewPanel.getOutlineFeather());
            setFeather.addActionListener(e -> {
                int originalFeather = highlightPreviewPanel.getOutlineFeather();
                SpinnerNumberModel model = new SpinnerNumberModel(originalFeather, 0, 5, 1);
                JSpinner spinner = new JSpinner(model);

                spinner.addChangeListener(ev -> {
                    highlightPreviewPanel.setOutlineFeather((Integer) spinner.getValue());
                    parentCard.triggerDataChanged();
                });

                if (!showSpinnerDialog(spinner, "Outline feather:", "Outline Feather")) {
                    highlightPreviewPanel.setOutlineFeather(originalFeather);
                    parentCard.triggerDataChanged();
                }
            });
            menu.add(setFeather);
        }
    }

    private void addMenuItems_Rave(JPopupMenu menu, TagStyle tagStyle) {
        if (tagStyle != TagStyle.AREA) {
            JCheckBoxMenuItem raveOutlineItem = createStyledCheckBox("Rave outline", highlightPreviewPanel.isRaveOutline(), (selected) -> highlightPreviewPanel.setRaveOutline(selected));
            menu.add(raveOutlineItem);
        }

        if (tagStyle != TagStyle.OUTLINE) {
            JCheckBoxMenuItem raveFillItem = createStyledCheckBox("Rave fill", highlightPreviewPanel.isRaveFill(), (selected) -> highlightPreviewPanel.setRaveFill(selected));
            menu.add(raveFillItem);
        }

        JMenuItem setRaveSpeed = new JMenuItem("Rave speed: " + highlightPreviewPanel.getRaveSpeed() + "ms");
        setRaveSpeed.addActionListener(e -> {
            int originalSpeed = highlightPreviewPanel.getRaveSpeed();
            SpinnerNumberModel model = new SpinnerNumberModel(originalSpeed, 100, 100000, 10);
            JSpinner spinner = new JSpinner(model);

            spinner.addChangeListener(ev -> {
                highlightPreviewPanel.setRaveSpeed((Integer) spinner.getValue());
                parentCard.triggerDataChanged();
            });

            if (!showSpinnerDialog(spinner, "Rave speed (milliseconds):", "Rave Speed")) {
                highlightPreviewPanel.setRaveSpeed(originalSpeed);
                parentCard.triggerDataChanged();
            }
        });
        menu.add(setRaveSpeed);
    }

    private void addMenuItems_Style(JPopupMenu menu, TagStyle tagStyle) {
        if (tagStyle == null) return;

        boolean separatorAdded = false;
        if (tagStyle.toString().contains("Tile") || tagStyle == TagStyle.HULL || tagStyle == TagStyle.CLICKBOX) {
            menu.addSeparator();
            separatorAdded = true;

            JCheckBoxMenuItem enableAA = createStyledCheckBox("Anti-aliasing", highlightPreviewPanel.isAntiAliasing(), (selected) -> highlightPreviewPanel.setAntiAliasing(selected));
            menu.add(enableAA);
        }

        if (tagStyle.toString().contains("Tile")) {
            if (!separatorAdded) menu.addSeparator();

            String currentStyleName = highlightPreviewPanel.getLineType().name().charAt(0) + highlightPreviewPanel.getLineType().name().substring(1).toLowerCase();
            JMenu lineTypeMenu = new JMenu("Tile style: " + currentStyleName);
            ButtonGroup group = new ButtonGroup();
            for (HighlightInfo.LineType type : HighlightInfo.LineType.values()) {
                String typeName = type.name().charAt(0) + type.name().substring(1).toLowerCase();
                JRadioButtonMenuItem item = new JRadioButtonMenuItem(typeName);
                item.setSelected(highlightPreviewPanel.getLineType() == type);
                item.addActionListener(e -> {
                    highlightPreviewPanel.setLineType(type);
                    lineTypeMenu.setText("Line type: " + typeName);
                    parentCard.triggerDataChanged();
                });
                group.add(item);
                lineTypeMenu.add(item);
            }
            menu.add(lineTypeMenu);
        } else {
            // Ensure non-tile styles don't retain a non-regular tile style
            if (highlightPreviewPanel.getLineType() != HighlightInfo.LineType.REGULAR) {
                highlightPreviewPanel.setLineType(HighlightInfo.LineType.REGULAR);
                parentCard.triggerDataChanged();
            }
        }
    }

    private JCheckBoxMenuItem createStyledCheckBox(String text, boolean isSelected, Consumer<Boolean> onToggle) {
        JCheckBoxMenuItem item = new JCheckBoxMenuItem(text, isSelected);
        item.setHorizontalTextPosition(SwingConstants.LEFT);
        if (isSelected) {
            item.setFont(item.getFont().deriveFont(Font.BOLD));
        }
        item.addActionListener(e -> {
            onToggle.accept(item.isSelected());
            parentCard.triggerDataChanged();
        });
        return item;
    }

    private void showOutlineWidthDialog(double min, double step) {
        double originalWidth = highlightPreviewPanel.getOutlineWidth();
        SpinnerNumberModel model = new SpinnerNumberModel(originalWidth, min, 50.0, step);
        JSpinner spinner = new JSpinner(model);

        spinner.addChangeListener(e -> {
            highlightPreviewPanel.setOutlineWidth(((Number) spinner.getValue()).doubleValue());
            parentCard.triggerDataChanged();
        });

        boolean confirmed = showSpinnerDialog(spinner, "Outline width:", "Outline Width");

        if (!confirmed) {
            highlightPreviewPanel.setOutlineWidth(originalWidth);
            parentCard.triggerDataChanged();
        }
    }

    private boolean showSpinnerDialog(JSpinner spinner, String labelText, String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(labelText), BorderLayout.NORTH);
        panel.add(spinner, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                title,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        return result == JOptionPane.OK_OPTION;
    }

    private void openColorPicker(boolean isOutline) {
        Color initial = isOutline ? highlightPreviewPanel.getOutlineColor() : highlightPreviewPanel.getFillColor();
        String title = isOutline ? "Outline Color" : "Fill Color";

        RuneliteColorPicker picker = colorPickerManager.create(
                SwingUtilities.getWindowAncestor(this),
                initial,
                title,
                false
        );

        picker.setOnColorChange(color -> {
            if (isOutline) {
                highlightPreviewPanel.setOutlineColor(color);
            } else {
                highlightPreviewPanel.setFillColor(color);
            }
        });

        picker.setOnClose(color -> parentCard.triggerDataChanged());

        Point panelLocation = parentCard.getPanel().getLocationOnScreen();
        picker.setLocation(panelLocation.x - picker.getWidth(), panelLocation.y);

        picker.setVisible(true);
    }

    private void createHighlightPreviewPanel(NpcHighlightEntry entry) {
        HighlightPreviewPanelConfig config = createHighlightPreviewPanelConfig(entry);
        TagStyle initialTagStyle = entry != null ? entry.tagStyle : null;

        highlightPreviewPanel = new HighlightPreviewPanel(
                initialTagStyle.toString(),
                config.outlineColor,
                config.fillColor,
                config.raveOutline,
                config.raveFill,
                config.raveSpeed,
                config.lineType,
                config.outlineWidth,
                config.antiAliasing,
                config.outlineFeather,
                () -> parentCard.getPlugin().getRaveColor(highlightPreviewPanel.getCurrentRaveSpeed())
        );

        styleHighlightPreviewPanel();
    }

    private void updateComponentStates() {
        highlightPreviewPanel.setTagStyle(tagStyle != null ? tagStyle.toString() : "None");
        if (tagStyle == null) {
            editButton.setEnabled(false);
            highlightPreviewPanelWrapper.setVisible(false);
            editButton.setToolTipText("This style has no configurable options");
        } else if (tagStyle == TagStyle.TURBO) {
            editButton.setEnabled(false);
            highlightPreviewPanelWrapper.setVisible(true);
            editButton.setToolTipText("This style has no configurable options");
        } else {
            editButton.setEnabled(true);
            highlightPreviewPanelWrapper.setVisible(true);
            editButton.setToolTipText("Edit highlight style");
        }
        rebuildEditButtonMenu(editButtonMenu, tagStyle);
    }

    private HighlightPreviewPanelConfig createHighlightPreviewPanelConfig(NpcHighlightEntry entry) {
        if (entry == null) {
            return createDefaultConfig();
        }

        return new HighlightPreviewPanelConfig(
                entry.outlineColor,
                entry.fillColor,
                entry.raveOutline,
                entry.raveFill,
                entry.raveSpeed,
                entry.lineType,
                entry.outlineWidth,
                entry.antiAliasing,
                entry.outlineFeather
        );
    }

    private HighlightPreviewPanelConfig createDefaultConfig() {
        return new HighlightPreviewPanelConfig(
                DEFAULT_OUTLINE_COLOR,
                DEFAULT_FILL_COLOR,
                DEFAULT_RAVE_OUTLINE,
                DEFAULT_RAVE_FILL,
                DEFAULT_RAVE_SPEED,
                DEFAULT_TILE_STYLE,
                DEFAULT_OUTLINE_WIDTH,
                DEFAULT_ANTI_ALIASING,
                DEFAULT_OUTLINE_FEATHER
        );
    }

    private void styleHighlightPreviewPanel() {
        highlightPreviewPanel.setOpaque(false);
        highlightPreviewPanel.setPreferredSize(new Dimension(COMPONENT_SIZE, COMPONENT_SIZE));
        highlightPreviewPanel.setMaximumSize(new Dimension(COMPONENT_SIZE, COMPONENT_SIZE));
    }

    private void createActionButtons() {
        editButton = DropDownButtonFactory.createDropDownButton(EDIT_STYLE_ICONS.getOn(), editButtonMenu);
        removeButton = createRemoveButton();
    }

    private JButton createRemoveButton() {
        JButton button = new JButton();
        styleActionButton(button, REMOVE_STYLE_ICONS, "Remove this highlight style");
        button.addActionListener(e -> handleRemoveButtonClick());
        return button;
    }

    private void styleActionButton(JButton button, IconSet icons, String tooltip) {
        button.setPreferredSize(new Dimension(COMPONENT_SIZE, COMPONENT_SIZE));
        button.setMinimumSize(new Dimension(COMPONENT_SIZE, COMPONENT_SIZE));
        button.setMaximumSize(new Dimension(COMPONENT_SIZE, COMPONENT_SIZE));
        button.setIcon(icons.getOn());
        button.setRolloverIcon(icons.getOnHover());
        button.setToolTipText(tooltip);
        BetterNpcHighlightPanel.styleButton(button);
    }

    private void layoutComponents() {
        // Style label
        add(tagStyleLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 1, 0));
        buttonPanel.setBackground(null);

        // Highlight preview panel
        highlightPreviewPanelWrapper = new JPanel(new BorderLayout());
        highlightPreviewPanelWrapper.setOpaque(false);
        highlightPreviewPanelWrapper.setPreferredSize(new Dimension(COMPONENT_SIZE, COMPONENT_SIZE));
        highlightPreviewPanelWrapper.setMaximumSize(new Dimension(COMPONENT_SIZE, COMPONENT_SIZE));
        highlightPreviewPanelWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        highlightPreviewPanelWrapper.add(highlightPreviewPanel);

        buttonPanel.add(highlightPreviewPanelWrapper, BorderLayout.CENTER);

        // Button panel
        buttonPanel.add(editButton);
        buttonPanel.add(removeButton);
        add(buttonPanel, BorderLayout.EAST);
    }

    private void applyEntryData(NpcHighlightEntry entry) {
        if (entry != null && entry.tagStyle != null) {
            setTagStyle(Objects.requireNonNull(TagStyle.fromString(entry.tagStyle.toString())));
        }
        parentCard.updateStyleButtons();
    }

    private void handleRemoveButtonClick() {
        int index = getRowIndex();
        if (index != -1) {
            parentCard.removeStyleRowAt(index);
        }
    }

    private int getRowIndex() {
        return parentCard.getStyleRows().indexOf(this);
    }

    // Styling Methods
    private void styleTagStyleLabel(JLabel label) {
        label.setBackground(ColorScheme.DARK_GRAY_COLOR);
        label.setForeground(ColorScheme.TEXT_COLOR);
        label.setFont(FontManager.getRunescapeSmallFont());
        label.setPreferredSize(new Dimension(115, COMPONENT_SIZE));
        label.setMaximumSize(label.getPreferredSize());
        label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
    }

    public void setTagStyle(TagStyle style) {
        this.tagStyle = style;
        this.tagStyleLabel.setText(style.getName());
        updateComponentStates();
    }

    // Configuration Helper Class
    private static class HighlightPreviewPanelConfig {
        final Color outlineColor;
        final Color fillColor;
        final boolean raveOutline;
        final boolean raveFill;
        final int raveSpeed;
        final HighlightInfo.LineType lineType;
        final double outlineWidth;
        final boolean antiAliasing;
        final int outlineFeather;

        HighlightPreviewPanelConfig(
                Color outlineColor,
                Color fillColor,
                boolean raveOutline,
                boolean raveFill,
                int raveSpeed,
                HighlightInfo.LineType lineType,
                double outlineWidth,
                boolean antiAliasing,
                int outlineFeather
        ) {
            this.outlineColor = outlineColor;
            this.fillColor = fillColor;
            this.raveOutline = raveOutline;
            this.raveFill = raveFill;
            this.raveSpeed = raveSpeed;
            this.lineType = lineType;
            this.outlineWidth = outlineWidth;
            this.antiAliasing = antiAliasing;
            this.outlineFeather = outlineFeather;
        }
    }
}