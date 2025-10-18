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

    private static final int offLuminance = -150;
    private static final int offHoverLuminance = -130;
    private static final int hoverLuminance = -50;

    private IconSet(ImageIcon on, ImageIcon off, ImageIcon onHover, ImageIcon offHover) {
        this.on = on;
        this.off = off;
        this.onHover = onHover;
        this.offHover = offHover;
    }

    public static IconSet loadIconSet(String resourcePath) {
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
