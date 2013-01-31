package com.aptina.miscellaneous;

import android.content.Context;

/**
 * Helper class to convert various dimensions. 
 */
public class DimensionConverter {
    /**
     * Converts density independent pixels to pixels.
     * @param dip value in DIP units.
     * @return value in pixels.
     */
    public static float dipToPixels(Context context, float dip)
    {
        // Get the screen's density scale
        final float scale = context.getResources().getDisplayMetrics().density;
        return (dip * scale);
    }
}
