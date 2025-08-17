package com.betternpchighlight.ui;

import com.betternpchighlight.BetterNpcHighlightPlugin;
import com.betternpchighlight.HighlightColor;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;
import net.runelite.client.ui.components.colorpicker.RuneliteColorPicker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;

public class ColorPreviewButton extends JButton {
    private final ColorPickerManager colorPickerManager;
    private final BetterNpcHighlightPlugin plugin;
    private final ConfigManager configManager;

    private Color outlineColor;
    private Color fillColor;

    private boolean raveOutline = false;
    private boolean raveFill = false;
    private int raveSpeed;
    private HighlightColor.TileStyle tileStyle;
    private double outlineWidth = 2.0;
    private boolean antiAliasing = true;
    private int outlineFeather = 2;
    private Timer raveTimer;

    public ColorPreviewButton(Color initialOutline, Color initialFill, boolean raveOutline, boolean raveFill, int raveSpeed, HighlightColor.TileStyle tileStyle,
                              double outlineWidth, boolean antiAliasing, int outlineFeather, ColorPickerManager colorPickerManager, BetterNpcHighlightPlugin plugin, ConfigManager configManager) {
        this.outlineColor = initialOutline;
        this.fillColor = initialFill;
        this.raveOutline = raveOutline;
        this.raveFill = raveFill;
        this.raveSpeed = raveSpeed;
        this.tileStyle = tileStyle;
        this.outlineWidth = outlineWidth;
        this.antiAliasing = antiAliasing;
        this.outlineFeather = outlineFeather;

        this.colorPickerManager = colorPickerManager;
        this.plugin = plugin;
        this.configManager = configManager;

        setPreferredSize(new Dimension(24, 24));
        setFocusPainted(false);
        setOpaque(true);
        setBackground(fillColor);
        setBorder(BorderFactory.createLineBorder(outlineColor, 2));
        setToolTipText("Right-click to customize highlight style");

        // Right-click menu to choose which color to edit or toggle rave
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    Component parentComponent = getParent();
                    StyleRow styleRow = null;
                    while (parentComponent != null) {
                        if (parentComponent instanceof StyleRow) {
                            styleRow = (StyleRow) parentComponent;
                            break;
                        }
                        parentComponent = parentComponent.getParent();
                    }
                    String tagStyle = (String) styleRow.tagStyleCombo.getSelectedItem();
                    showColorMenu(e, tagStyle);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    Component parentComponent = getParent();
                    StyleRow styleRow = null;
                    while (parentComponent != null) {
                        if (parentComponent instanceof StyleRow) {
                            styleRow = (StyleRow) parentComponent;
                            break;
                        }
                        parentComponent = parentComponent.getParent();
                    }
                    String tagStyle = (String) styleRow.tagStyleCombo.getSelectedItem();
                    showColorMenu(e, tagStyle);
                }
            }
        });
        updateRaveTimer();
    }

    private void showColorMenu(MouseEvent e, String tagStyle) {
        JPopupMenu menu = new JPopupMenu();
        DecimalFormat df = new DecimalFormat("#.#");

        JMenuItem setOutline = new JMenuItem("Outline color");
        setOutline.addActionListener(ev -> openColorPicker(true));
        menu.add(setOutline);

        JMenuItem setFill = new JMenuItem("Fill color");
        setFill.addActionListener(ev -> openColorPicker(false));
        menu.add(setFill);

        if (tagStyle != null) {
            String lowerCaseTagStyle = tagStyle.toLowerCase();

            if (lowerCaseTagStyle.contains("tile") || lowerCaseTagStyle.equals("hull") || lowerCaseTagStyle.equals("clickbox")) {
                // Tile, True Tile, SW Tile, SW True Tile, Hull, Clickbox
                JMenuItem setWidth = new JMenuItem("Outline width: " + df.format(outlineWidth));
                setWidth.addActionListener(ev -> {
                    double originalWidth = getOutlineWidth();
                    SpinnerNumberModel model = new SpinnerNumberModel(originalWidth, 0.0, 50.0, 0.1);
                    JSpinner spinner = new JSpinner(model);
                    spinner.addChangeListener(changeEvent -> {
                        setOutlineWidth((Double) spinner.getValue());
                        plugin.getPanel().saveAllCards(configManager, "betterNpcHighlight");
                    });

                    JPanel panel = new JPanel(new BorderLayout());
                    panel.add(new JLabel("Outline width:"), BorderLayout.NORTH);
                    panel.add(spinner, BorderLayout.CENTER);

                    int result = JOptionPane.showConfirmDialog(
                            this,
                            panel,
                            "Outline Width",
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.PLAIN_MESSAGE
                    );

                    if (result == JOptionPane.OK_OPTION) {
                        plugin.getPanel().saveAllCards(configManager, "betterNpcHighlight");
                    } else {
                        setOutlineWidth(originalWidth);
                        plugin.getPanel().saveAllCards(configManager, "betterNpcHighlight");
                    }
                });
                menu.add(setWidth);

                JCheckBoxMenuItem enableAA = new JCheckBoxMenuItem("Anti-aliasing");
                enableAA.setSelected(isAntiAliasing());
                enableAA.setHorizontalTextPosition(SwingConstants.LEFT);
                if (enableAA.isSelected()) {
                    enableAA.setFont(enableAA.getFont().deriveFont(Font.BOLD));
                }
                enableAA.addActionListener(ev -> {
                    setAntiAliasing(enableAA.isSelected());
                    plugin.getPanel().saveAllCards(configManager, "betterNpcHighlight");
                });
                menu.add(enableAA);
            } else if (lowerCaseTagStyle.equals("outline")) {
                // Outline
                JMenuItem setWidth = new JMenuItem("Outline width: " + (int)outlineWidth);
                setWidth.addActionListener(ev -> {
                    int originalWidth = (int) getOutlineWidth();
                    SpinnerNumberModel model = new SpinnerNumberModel(originalWidth, 1, 50, 1);
                    JSpinner spinner = new JSpinner(model);
                    spinner.addChangeListener(changeEvent -> {
                        setOutlineWidth((Integer) spinner.getValue());
                        plugin.getPanel().saveAllCards(configManager, "betterNpcHighlight");
                    });

                    JPanel panel = new JPanel(new BorderLayout());
                    panel.add(new JLabel("Outline width:"), BorderLayout.NORTH);
                    panel.add(spinner, BorderLayout.CENTER);

                    int result = JOptionPane.showConfirmDialog(
                            this,
                            panel,
                            "Outline Width",
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.PLAIN_MESSAGE
                    );

                    if (result == JOptionPane.OK_OPTION) {
                        plugin.getPanel().saveAllCards(configManager, "betterNpcHighlight");
                    } else {
                        setOutlineWidth(originalWidth);
                        plugin.getPanel().saveAllCards(configManager, "betterNpcHighlight");
                    }
                });
                menu.add(setWidth);

                JMenuItem setFeather = new JMenuItem("Outline feather: " + outlineFeather);
                setFeather.addActionListener(ev -> {
                    int originalFeather = getOutlineFeather();
                    SpinnerNumberModel model = new SpinnerNumberModel(originalFeather, 0, 5, 1);
                    JSpinner spinner = new JSpinner(model);
                    spinner.addChangeListener(changeEvent -> {
                        setOutlineFeather((Integer) spinner.getValue());
                        plugin.getPanel().saveAllCards(configManager, "betterNpcHighlight");
                    });

                    JPanel panel = new JPanel(new BorderLayout());
                    panel.add(new JLabel("Outline feather:"), BorderLayout.NORTH);
                    panel.add(spinner, BorderLayout.CENTER);

                    int result = JOptionPane.showConfirmDialog(
                            this,
                            panel,
                            "Outline Feather",
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.PLAIN_MESSAGE
                    );

                    if (result == JOptionPane.OK_OPTION) {
                        plugin.getPanel().saveAllCards(configManager, "betterNpcHighlight");
                    } else {
                        setOutlineFeather(originalFeather);
                        plugin.getPanel().saveAllCards(configManager, "betterNpcHighlight");
                    }
                });
                menu.add(setFeather);
            }
        }

        menu.addSeparator();

        JCheckBoxMenuItem raveOutlineItem = new JCheckBoxMenuItem("Rave outline");
        raveOutlineItem.setSelected(isRaveOutline());
        raveOutlineItem.setHorizontalTextPosition(SwingConstants.LEFT);
        if (raveOutlineItem.isSelected()) {
            raveOutlineItem.setFont(raveOutlineItem.getFont().deriveFont(Font.BOLD));
        }
        raveOutlineItem.addActionListener(ev -> {
            setRaveOutline(raveOutlineItem.isSelected());
            plugin.getPanel().saveAllCards(configManager, "betterNpcHighlight");
        });
        menu.add(raveOutlineItem);

        JCheckBoxMenuItem raveFillItem = new JCheckBoxMenuItem("Rave fill");
        raveFillItem.setSelected(isRaveFill());
        raveFillItem.setHorizontalTextPosition(SwingConstants.LEFT);
        if (raveFillItem.isSelected()) {
            raveFillItem.setFont(raveFillItem.getFont().deriveFont(Font.BOLD));
        }
        raveFillItem.addActionListener(ev -> {
            setRaveFill(raveFillItem.isSelected());
            plugin.getPanel().saveAllCards(configManager, "betterNpcHighlight");
        });
        menu.add(raveFillItem);

        JMenuItem setRaveSpeed = new JMenuItem("Rave speed: " + raveSpeed + "ms");
        setRaveSpeed.addActionListener(ev -> {
            // Store original speed in case we need to revert
            int originalSpeed = this.raveSpeed;

            // Create spinner for milliseconds
            SpinnerNumberModel model = new SpinnerNumberModel(originalSpeed, 100, 100000, 10);
            JSpinner spinner = new JSpinner(model);

            // Live preview while changing value
            spinner.addChangeListener(changeEvent -> {
                int newSpeed = (Integer) spinner.getValue();
                this.setRaveSpeed(newSpeed);
                plugin.getPanel().saveAllCards(configManager, "betterNpcHighlight");
            });

            // Build panel
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(new JLabel("Rave speed (miliseconds):"), BorderLayout.NORTH);
            panel.add(spinner, BorderLayout.CENTER);

            // Show dialog
            int result = JOptionPane.showConfirmDialog(
                    this,
                    panel,
                    "Rave Speed",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (result == JOptionPane.OK_OPTION) {
                // User confirmed — persist
                plugin.getPanel().saveAllCards(configManager, "betterNpcHighlight");
            } else {
                // User cancelled — restore original speed
                this.setRaveSpeed(originalSpeed);
                plugin.getPanel().saveAllCards(configManager, "betterNpcHighlight");
            }
        });
        menu.add(setRaveSpeed);

        if (tagStyle != null && (tagStyle.toLowerCase().contains("tile"))) {
            menu.addSeparator();
            JMenu tileStyleMenu = new JMenu("Tile style: " + tileStyle.name().charAt(0) + tileStyle.name().substring(1).toLowerCase());
            ButtonGroup group = new ButtonGroup();
            for (HighlightColor.TileStyle style : HighlightColor.TileStyle.values()) {
                JRadioButtonMenuItem item = new JRadioButtonMenuItem(style.name().charAt(0) + style.name().substring(1).toLowerCase());
                item.setSelected(this.tileStyle == style);
                item.addActionListener(ev -> {
                    setTileStyle(style);
                    plugin.getPanel().saveAllCards(configManager, "betterNpcHighlight");
                });
                group.add(item);
                tileStyleMenu.add(item);
            }
            menu.add(tileStyleMenu);
        }

        menu.show(this, e.getX(), e.getY());
    }

    private void startRaveTimer() {
        if (raveTimer != null && raveTimer.isRunning()) {
            return;
        }
        // Fire every 50ms for a smooth animation
        raveTimer = new Timer(50, e -> {
            repaint();
        });
        raveTimer.start();
    }

    private void stopRaveTimer() {
        if (raveTimer != null) {
            raveTimer.stop();
            raveTimer = null;
        }
    }

    private void updateRaveTimer() {
        if (raveOutline || raveFill) {
            startRaveTimer();
        } else {
            stopRaveTimer();
        }
    }

    public void setRaveOutline(boolean raveOutline) {
        this.raveOutline = raveOutline;
        updateRaveTimer();
        repaint();
    }

    public void setRaveFill(boolean raveFill) {
        this.raveFill = raveFill;
        updateRaveTimer();
        repaint();
    }

    public boolean isRaveOutline() {
        return raveOutline;
    }

    public boolean isRaveFill() {
        return raveFill;
    }

    public int getRaveSpeed() {
        return raveSpeed;
    }

    public void setRaveSpeed(int raveSpeed) {
        this.raveSpeed = raveSpeed;
    }

    public HighlightColor.TileStyle getTileStyle() {
        return tileStyle;
    }

    public void setTileStyle(HighlightColor.TileStyle tileStyle) {
        this.tileStyle = tileStyle;
    }

    public Color getOutlineColor() {
        return outlineColor;
    }

    public void setOutlineColor(Color outlineColor) {
        this.outlineColor = outlineColor;
        setBorder(BorderFactory.createLineBorder(outlineColor, 2));
    }

    public Color getFillColor() {
        return fillColor;
    }

    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
        setBackground(fillColor);
    }

    public double getOutlineWidth() {
        return outlineWidth;
    }

    public void setOutlineWidth(double outlineWidth) {
        this.outlineWidth = outlineWidth;
    }

    public boolean isAntiAliasing() {
        return antiAliasing;
    }

    public void setAntiAliasing(boolean antiAliasing) {
        this.antiAliasing = antiAliasing;
    }

    public int getOutlineFeather() {
        return outlineFeather;
    }

    public void setOutlineFeather(int outlineFeather) {
        this.outlineFeather = outlineFeather;
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (raveFill) {
            // Calculate the current rave color, which fades through the hue spectrum
            Color raveRgb = plugin.getRaveColor(this.raveSpeed);

            // Combine the rave color's RGB with the original fill color's alpha
            Color finalRaveColor = new Color(raveRgb.getRed(), raveRgb.getGreen(), raveRgb.getBlue(), fillColor.getAlpha());

            // Paint the solid, fading color
            g.setColor(finalRaveColor);
            g.fillRect(0, 0, getWidth(), getHeight());
        } else {
            super.paintComponent(g);
        }
    }

    @Override
    protected void paintBorder(Graphics g) {
        if (raveOutline) {
            Graphics2D g2d = (Graphics2D) g.create();

            // Calculate the current rave color, which fades through the hue spectrum
            Color raveRgb = plugin.getRaveColor(this.raveSpeed);
            // Combine the rave color's RGB with the original outline color's alpha
            Color finalRaveColor = new Color(raveRgb.getRed(), raveRgb.getGreen(), raveRgb.getBlue(), outlineColor.getAlpha());

            g2d.setColor(finalRaveColor);
            g2d.setStroke(new BasicStroke(2)); // Same width as the normal border
            // Draw the rect inside the button bounds
            g2d.drawRect(1, 1, getWidth() - 2, getHeight() - 2);

            g2d.dispose();
        } else {
            super.paintBorder(g);
        }
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
        picker.setLocation(loc.x - 500, loc.y);
        picker.setOnColorChange(color -> {
            if (isOutline) {
                setOutlineColor(color);
            } else {
                setFillColor(color);
            }
        });

        picker.setOnClose(color -> plugin.getPanel().saveAllCards(configManager, "betterNpcHighlight"));
        picker.setVisible(true);
    }
}
