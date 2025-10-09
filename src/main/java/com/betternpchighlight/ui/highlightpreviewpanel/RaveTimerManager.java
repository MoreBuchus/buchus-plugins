package com.betternpchighlight.ui.highlightpreviewpanel;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;

public final class RaveTimerManager {

    private static final int ANIMATION_INTERVAL = 60;
    private static final RaveTimerManager INSTANCE = new RaveTimerManager();

    private final Timer raveTimer;
    private final Set<RepaintListener> listeners = new HashSet<>();

    private RaveTimerManager() {
        raveTimer = new Timer(ANIMATION_INTERVAL, e -> new HashSet<>(listeners).forEach(RepaintListener::repaintComponent));
    }

    public static RaveTimerManager getInstance() {
        return INSTANCE;
    }

    public synchronized void registerListener(RepaintListener listener) {
        listeners.add(listener);
        if (!raveTimer.isRunning()) {
            raveTimer.start();
        }
    }

    public synchronized void unregisterListener(RepaintListener listener) {
        listeners.remove(listener);
        if (listeners.isEmpty() && raveTimer.isRunning()) {
            raveTimer.stop();
        }
    }

    public interface RepaintListener {
        void repaintComponent();
    }
}