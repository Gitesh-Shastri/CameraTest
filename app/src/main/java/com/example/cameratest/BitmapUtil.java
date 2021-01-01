package com.example.cameratest;

import android.graphics.Bitmap;
import android.graphics.Color;

class BitmapUtil {

    static Bitmap removeTransparency(Bitmap source, int width, int height) {
        try {
            int firstX = 0, firstY = 0;
            int lastX = width;
            int lastY = height;
            int[] pixels = new int[width * height];
            source.getPixels(pixels, 0, width, 0, 0, width, height);
            loop:
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (pixels[x + (y * width)] != Color.TRANSPARENT) {
                        firstX = x;
                        break loop;
                    }
                }
            }
            loop:
            for (int y = 0; y < height; y++) {
                for (int x = firstX; x < height; x++) {
                    if (pixels[x + (y * width)] != Color.TRANSPARENT) {
                        firstY = y;
                        break loop;
                    }
                }
            }
            loop:
            for (int x = width - 1; x >= firstX; x--) {
                for (int y = height - 1; y >= firstY; y--) {
                    if (pixels[x + (y * width)] != Color.TRANSPARENT) {
                        lastX = x;
                        break loop;
                    }
                }
            }
            loop:
            for (int y = height - 1; y >= firstY; y--) {
                for (int x = width - 1; x >= firstX; x--) {
                    if (pixels[x + (y * width)] != Color.TRANSPARENT) {
                        lastY = y;
                        break loop;
                    }
                }
            }
            return Bitmap.createBitmap(source, 0, 0, width, height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
