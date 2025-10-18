package com.betternpchighlight.ui.highlightpreviewpanel;

import com.betternpchighlight.HighlightInfo;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Random;
import java.util.function.Supplier;

/**
 * A button component that provides a color preview and customization interface
 * for highlight styling options.
 */

@Getter
@Setter
public class HighlightPreviewPanel extends JPanel implements RaveTimerManager.RepaintListener {

    // Constants
    private static final int BUTTON_SIZE = 24;
    private static final int BORDER_THICKNESS = 2;
    private static final Color DEFAULT_OUTLINE_COLOR = Color.CYAN;
    private static final Color DEFAULT_FILL_COLOR = new Color(0, 255, 255, 20);

    private final Supplier<Color> raveColorProvider;
    private final Random random = new Random();

    // Color properties
    @Setter(AccessLevel.NONE)
    private Color outlineColor;
    @Setter(AccessLevel.NONE)
    private Color fillColor;

    // Animation properties
    @Setter(AccessLevel.NONE)
    private boolean raveOutline;
    @Setter(AccessLevel.NONE)
    private boolean raveFill;
    private int raveSpeed;

    // Style properties
    @Setter(AccessLevel.NONE)
    private HighlightInfo.LineType lineType;
    private double outlineWidth;
    private boolean antiAliasing;
    private int outlineFeather;
    @Setter(AccessLevel.NONE)
    private String tagStyle;

    public HighlightPreviewPanel(
            String initialTagStyle,
            Color initialOutline,
            Color initialFill,
            boolean raveOutline,
            boolean raveFill,
            int raveSpeed,
            HighlightInfo.LineType lineType,
            double outlineWidth,
            boolean antiAliasing,
            int outlineFeather,
            Supplier<Color> raveColorProvider) {

        this.tagStyle = initialTagStyle;
        this.outlineColor = (initialOutline != null) ? initialOutline : DEFAULT_OUTLINE_COLOR;
        this.fillColor = (initialFill != null) ? initialFill : DEFAULT_FILL_COLOR;
        this.raveOutline = raveOutline;
        this.raveFill = raveFill;
        this.raveSpeed = raveSpeed;
        this.lineType = lineType;
        this.outlineWidth = outlineWidth;
        this.antiAliasing = antiAliasing;
        this.outlineFeather = outlineFeather;
        this.raveColorProvider = raveColorProvider;

        initializePanel();
        updateRaveState();
    }

    private void initializePanel() {
        setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
        setOpaque(false);
        setBorder(BorderFactory.createLineBorder(outlineColor, BORDER_THICKNESS));
    }

    // Painting Methods
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        try {
            setupRenderingHints(g2d);
            paintFill(g2d);
        } finally {
            g2d.dispose();
        }
    }

    private void setupRenderingHints(Graphics2D g2d) {
        if (antiAliasing) {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        }
    }

    private void paintFill(Graphics2D g2d) {
        if ("Outline".equals(tagStyle)) return;
        Color fillToDraw = getCurrentFillColor();
        g2d.setComposite(AlphaComposite.SrcOver);
        g2d.setColor(fillToDraw);
        g2d.fillRect(BORDER_THICKNESS, BORDER_THICKNESS, getWidth() - (BORDER_THICKNESS * 2), getHeight() - (BORDER_THICKNESS * 2));
    }

    @Override
    protected void paintBorder(Graphics g) {
        if ("Area".equals(tagStyle)) return;
        Graphics2D g2d = (Graphics2D) g.create();
        try {
            Color borderColor = getCurrentOutlineColor();
            g2d.setColor(borderColor);

            float stroke = BORDER_THICKNESS;
            float offset = stroke / 2.0f;

            float width = getWidth();
            float height = getHeight();

            switch (getCurrentLineType()) {
                case REGULAR:
                    drawRegularBorder(g2d, width, height, stroke, offset);
                    break;
                case DASHED:
                    drawDashedBorder(g2d, width, height, stroke, offset);
                    break;
                case CORNER:
                    drawCornerBorder(g2d, width, height, stroke, offset);
                    break;
                default:
                    drawRegularBorder(g2d, width, height, stroke, offset);
                    break;
            }
        } finally {
            g2d.dispose();
        }
    }

    private void drawRegularBorder(Graphics2D g2d, float width, float height, float stroke, float offset) {
        g2d.setStroke(new BasicStroke(stroke));
        float rectWidth = width - stroke;
        float rectHeight = height - stroke;
        g2d.draw(new Rectangle2D.Float(offset, offset, rectWidth, rectHeight));
    }

    private void drawDashedBorder(Graphics2D g2d, float width, float height, float stroke, float offset) {
        float[] dashPattern = {6f, 3f}; // Dash, gap
        g2d.setStroke(new BasicStroke(BORDER_THICKNESS,
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER,
                10.0f,
                dashPattern,
                0.0f));

        // Top line
        g2d.draw(new Line2D.Float(0, offset, width, offset));
        // Right line
        g2d.draw(new Line2D.Float(width - offset, 0, width - offset, height));
        // Bottom line
        g2d.draw(new Line2D.Float(width, width - offset, 0, width - offset));
        // Left line
        g2d.draw(new Line2D.Float(offset, height, offset, offset));
    }

    private void drawCornerBorder(Graphics2D g2d, float width, float height, float stroke, float offset) {
        g2d.setStroke(new BasicStroke(stroke));

        float right = width - offset;
        float bottom = height - offset;

        float cornerLength = Math.max(6f, stroke * 2f);

        // Top-left
        g2d.draw(new Line2D.Float(offset, offset, offset + cornerLength, offset));
        g2d.draw(new Line2D.Float(offset, offset, offset, offset + cornerLength));

        // Top-right
        g2d.draw(new Line2D.Float(right - cornerLength, offset, right, offset));
        g2d.draw(new Line2D.Float(right, offset, right, offset + cornerLength));

        // Bottom-left
        g2d.draw(new Line2D.Float(offset, bottom, offset + cornerLength, bottom));
        g2d.draw(new Line2D.Float(offset, bottom - cornerLength, offset, bottom));

        // Bottom-right
        g2d.draw(new Line2D.Float(right - cornerLength, bottom, right, bottom));
        g2d.draw(new Line2D.Float(right, bottom - cornerLength, right, bottom));
    }

    @Override
    public void repaintComponent() {
        repaint();
    }

    private Color applyRaveColor(Color base) {
        Color raveRgb = raveColorProvider.get();
        return new Color(raveRgb.getRed(), raveRgb.getGreen(), raveRgb.getBlue(), base.getAlpha());
    }

    private Color getCurrentFillColor() {
        if ("Turbo".equals(tagStyle)) {
            Color raveRgb = raveColorProvider.get();
            return new Color(raveRgb.getRed(), raveRgb.getGreen(), raveRgb.getBlue(), random.nextInt(150));
        }
        if (raveFill) {
            return applyRaveColor(fillColor);
        }
        return fillColor;
    }

    private Color getCurrentOutlineColor() {
        if ("Turbo".equals(tagStyle)) {
            Color raveRgb = raveColorProvider.get();
            return new Color(raveRgb.getRed(), raveRgb.getGreen(), raveRgb.getBlue(), random.nextInt(256));
        }
        if (raveOutline) {
            return applyRaveColor(outlineColor);
        }
        return outlineColor;
    }

    private HighlightInfo.LineType getCurrentLineType() {
        if ("Turbo".equals(tagStyle)) {
            return HighlightInfo.LineType.values()[random.nextInt(HighlightInfo.LineType.values().length)];
        }
        return lineType;
    }

    public int getCurrentRaveSpeed() {
        if ("Turbo".equals(tagStyle)) {
            return 600;
        }
        return raveSpeed;
    }

    public void setOutlineColor(Color outlineColor) {
        this.outlineColor = outlineColor;
        repaint();
    }

    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
        repaint();
    }

    public void setRaveOutline(boolean raveOutline) {
        this.raveOutline = raveOutline;
        updateRaveState();
    }

    public void setRaveFill(boolean raveFill) {
        this.raveFill = raveFill;
        updateRaveState();
    }

    public void setLineType(HighlightInfo.LineType lineType) {
        this.lineType = lineType;
        repaint();
    }

    public void setTagStyle(String tagStyle) {
        this.tagStyle = tagStyle;
        updateRaveState();
    }

    private void updateRaveState() {
        if (raveOutline || raveFill || "Turbo".equals(tagStyle)) {
            RaveTimerManager.getInstance().registerListener(this);
        } else {
            RaveTimerManager.getInstance().unregisterListener(this);
        }
        repaint();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        updateRaveState();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        RaveTimerManager.getInstance().unregisterListener(this);
    }
}