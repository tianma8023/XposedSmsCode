package com.tianma.xsmscode.common.utils;

import androidx.annotation.ColorInt;

public class ColorUtils {

    private ColorUtils() {
    }

    /**
     * Darken or lighten a specific color.<p/>
     * Calculate an new color according to originColor and factor. Dark color returned if factor < 1.0f, light color returned if factor > 1.0f
     *
     * @param originColor The specific color value.
     * @param factor      The factor that can influence the color returned.
     * @return Dark color returned if factor < 1.0f, light color returned if factor > 1.0f
     */
    public static int gradientColor(@ColorInt int originColor, float factor) {
        if (factor == 1.0f) {
            return originColor;
        }
        int a = originColor >> 24;
        int r = (int) (((originColor >> 16) & 0xff) * factor);
        r = Math.min(r, 0xff);
        int g = (int) (((originColor >> 8) & 0xff) * factor);
        g = Math.min(g, 0xff);
        int b = (int) ((originColor & 0xff) * factor);
        b = Math.min(b, 0xff);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

}