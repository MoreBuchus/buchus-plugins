package com.betternpchighlight.ui;

import javax.swing.*;
import java.awt.*;

// A panel that wraps its contents to the width of the scroll pane
public class ScrollablePanel extends JPanel implements Scrollable {
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
