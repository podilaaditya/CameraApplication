package com.aptina.miscellaneous;

/**
 * Class for decoding YUV data.
 */
public class YUVDecoder {
    
    /**
     * Decoder initialization flag.
     */
    private static boolean sRgbDecoderInitialized = false;
    
    /**
     * Color mapping arrays.
     */
    private static int[] mLut1, mLut2, mLut3, mLut4, mLut5;
    
    /**
     * Recode raw UYVY byte data to the array of ARGB pixels.
     * 
     * @param source Source data in UYVY format.
     * @param width Width of the source image.
     * @param height Height of the source image.
     * 
     * @return Array of pixels.
     */
    public static int[] UYVY2RGB(byte[] source, int width, int height) {
        int pixelCount = width*height;        
        int uyvyByteCount = pixelCount*2;  // UYVY: 2 bytes per pixel
        int[] destination = new int[pixelCount];

        int Y1, Y2, Y, R, G, B, Cr, Cb;
        
        if (!sRgbDecoderInitialized) {
            mLut1 = new int[256];
            mLut2 = new int[256];
            mLut3 = new int[256];
            mLut4 = new int[256];
            mLut5 = new int[256];
            
            for (int i = 0; i < 256; i++) {
                mLut1[i] = i*1166;
                mLut2[i] = i*1635;
                mLut3[i] = i*401;
                mLut4[i] = i*833;
                mLut5[i] = i*2067;
            }
            sRgbDecoderInitialized = true;
        }
        
        // Decode byte array.
        for (int i = 0, j = 0; i < uyvyByteCount; i = i+4, j = j+2) {
            // Get UYVY pixels.
            Cb = (source[i+0] < 0) ? source[i+0]+256 : source[i+0];
            Y1 = (source[i+1] < 0) ? source[i+1]+256 : source[i+1];
            Cr = (source[i+2] < 0) ? source[i+2]+256 : source[i+2];
            Y2 = (source[i+3] < 0) ? source[i+3]+256 : source[i+3];

            // Transform them into RGB.
            int A = 255;
            // First pixel
            Y = mLut1[Y1];
            B = (int) clamp((Y + mLut5[Cb] - 283210) >> 10, 0, 255);            
            G = (int) clamp((Y - mLut3[Cb] - mLut4[Cr] + 139301) >> 10, 0, 255); 
            R = (int) clamp((Y + mLut2[Cr] - 227920) >> 10, 0, 255);
            destination[j] = (A << 24) | (R << 16) | (G << 8) | B;

            // Second pixel
            Y = mLut1[Y2];
            B = (int) clamp((Y + mLut5[Cb] - 283210) >> 10, 0, 255);            
            G = (int) clamp((Y - mLut3[Cb] - mLut4[Cr] + 139301) >> 10, 0, 255); 
            R = (int) clamp((Y + mLut2[Cr] - 227920) >> 10, 0, 255);
            destination[j+1] = (A << 24) | (R << 16) | (G << 8) | B;
        }

        return destination;
    }

    /**
     * Recode raw UYVY byte data to the 2x down-scaled array of ARGB pixels.
     * 
     * @param source Source data in UYVY format.
     * @param width Width of the source image.
     * @param height Height of the source image.
     * 
     * @return Array of pixels.
     */
    public static int[] UYVY2RGBScaled(byte[] source, int width, int height) {        
        int scaledWidth = width/2;           // Scaled width.
        int scaledHeight = height/2;         // Scaled height.
        int scaledPixelCount = scaledWidth*scaledHeight; // Scaled image pixel count.

        // Scaled image data array.
        int[] destination = new int[scaledPixelCount];

        // Loop variables
        int Y1, Y2, Y3, Y4, U1, U2, V1, V2, Cr, Cb, Yt, Y, R, G, B;
        
        if (!sRgbDecoderInitialized) {
            mLut1 = new int[256];
            mLut2 = new int[256];
            mLut3 = new int[256];
            mLut4 = new int[256];
            mLut5 = new int[256];
            
            for (int i = 0; i < 256; i++) {
                mLut1[i] = i*1166;
                mLut2[i] = i*1635;
                mLut3[i] = i*401;
                mLut4[i] = i*833;
                mLut5[i] = i*2067;
            }
            sRgbDecoderInitialized = true;
        }
        
        for (int pixel = 0; pixel < scaledPixelCount; pixel++) {
            int offset = ((pixel / scaledWidth) * 2 * (width*2)) + ((pixel % scaledWidth) * 4);

            // Get all pixels.
            U1  = (source[offset] < 0)           ? source[offset]+256           : source[offset];
            U2  = (source[offset+width*2] < 0)   ? source[offset+width*2]+256   : source[offset+width*2];
            V1  = (source[offset+2] < 0)         ? source[offset+2]+256         : source[offset+2];
            V2  = (source[offset+2+width*2] < 0) ? source[offset+2+width*2]+256 : source[offset+2+width*2];
            Y1  = (source[offset+1] < 0)         ? source[offset+1]+256         : source[offset+1];            
            Y2  = (source[offset+3] < 0)         ? source[offset+3]+256         : source[offset+3];
            Y3  = (source[offset+1+width*2] < 0) ? source[offset+1+width*2]+256 : source[offset+1+width*2];;
            Y4  = (source[offset+3+width*2] < 0) ? source[offset+3+width*2]+256 : source[offset+3+width*2];

            // Get average values for downscaling.
            Cb = (U1+U2)/2;
            Cr = (V1+V2)/2;
            Yt = (Y1+Y2+Y3+Y4)/4;
            Y  = mLut1[Yt];
            
            // Transform them into RGB.
            int A = 255;
            // First pixel
            B = (int) clamp((Y + mLut5[Cb] - 283210) >> 10, 0, 255);            
            G = (int) clamp((Y - mLut3[Cb] - mLut4[Cr] + 139301) >> 10, 0, 255); 
            R = (int) clamp((Y + mLut2[Cr] - 227920) >> 10, 0, 255);            
            destination[pixel] = (A << 24) | (R << 16) | (G << 8) | B;
        }

        return destination;
    }
    
    /**
     * Clamps given value to the specified boundaries.
     * 
     * @param value Value to limit.
     * @param min Right margin of the clamp interval.
     * @param max Left margin of the clamp interval.
     * 
     * @return Modified value that lies within given limits.
     */
    private static long clamp(long value, long min, long max) {
        return (Math.max(Math.min(value, max), min));
    }
}
