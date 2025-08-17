package com.betternpchighlight.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

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
