package com.betternpchighlight.util;

import lombok.Getter;
import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

@Getter
public final class IconSet {
    private static final Map<String, IconSet> CACHE = new HashMap<>();

    private final ImageIcon on;
    private final ImageIcon off;
    private final ImageIcon onHover;
    private final ImageIcon offHover;

    private IconSet(ImageIcon on, ImageIcon off, ImageIcon onHover, ImageIcon offHover) {
        this.on = on;
        this.off = off;
        this.onHover = onHover;
        this.offHover = offHover;
    }

    /**
     * Loads an IconSet from the specified resource path.
     * Results are cached to prevent redundant image processing.
     *
     * @param resourcePath      Path to the base image resource.
     * @param hoverLuminance    Luminance offset for the 'on hover' state.
     * @param offLuminance      Luminance offset for the 'off' state.
     * @param offHoverLuminance Luminance offset for the 'off hover' state.
     * @return The cached or newly created IconSet.
     */
    public static IconSet loadIconSet(String resourcePath, int hoverLuminance, int offLuminance, int offHoverLuminance) {
        return CACHE.computeIfAbsent(resourcePath, path -> {
            BufferedImage base = ImageUtil.loadImageResource(IconSet.class, path);
            return new IconSet(
                    new ImageIcon(base),
                    new ImageIcon(ImageUtil.luminanceOffset(base, offLuminance)),
                    new ImageIcon(ImageUtil.luminanceOffset(base, hoverLuminance)),
                    new ImageIcon(ImageUtil.luminanceOffset(base, offHoverLuminance))
            );
        });
    }
}
