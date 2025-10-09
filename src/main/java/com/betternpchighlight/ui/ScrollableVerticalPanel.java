package com.betternpchighlight.ui;

import javax.swing.*;
import java.awt.*;

/**
 * A panel that can be scrolled vertically and forces its content to fit the width of the viewport.
 */
public class ScrollableVerticalPanel extends JPanel implements Scrollable {
    public ScrollableVerticalPanel() {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

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
        return true; // Force width to match viewport
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false; // Allow vertical scrolling
    }
}