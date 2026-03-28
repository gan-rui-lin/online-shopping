package com.helloworld.onlineshopping.utils;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageProcessor {

    /**
     * Generate thumbnail with cropping to fit size
     */
    public static byte[] generateThumbnail(byte[] imageData, int width, int height) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Thumbnails.of(new ByteArrayInputStream(imageData))
                .size(width, height)
                .crop(Positions.CENTER)
                .outputFormat("png")
                .toOutputStream(outputStream);
        return outputStream.toByteArray();
    }

    /**
     * Normalize main image to fixed size for stable product card rendering.
     */
    public static byte[] normalizeMainImage(byte[] imageData, int width, int height, float quality) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Thumbnails.of(new ByteArrayInputStream(imageData))
                .size(width, height)
                .crop(Positions.CENTER)
                .outputQuality(quality)
                .outputFormat("jpg")
                .toOutputStream(outputStream);
        return outputStream.toByteArray();
    }

    /**
     * Add configurable watermark
     */
    public static byte[] addWatermark(byte[] imageData, String watermarkText, String position, float opacity, int fontSize) throws IOException {
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageData));
        BufferedImage watermarkedImage = Thumbnails.of(originalImage)
                .scale(1.0)
                .watermark(getPosition(position), createWatermark(watermarkText, fontSize), opacity)
                .asBufferedImage();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(watermarkedImage, "png", outputStream);
        return outputStream.toByteArray();
    }

    /**
     * Add watermark with default settings
     */
    public static byte[] addWatermark(byte[] imageData, String watermarkText) throws IOException {
        return addWatermark(imageData, watermarkText, "BOTTOM_RIGHT", 0.5f, 30);
    }

    /**
     * Compress image to reduce file size
     */
    public static byte[] compressImage(byte[] imageData, float quality) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Thumbnails.of(new ByteArrayInputStream(imageData))
                .scale(1.0)
                .outputQuality(quality)
                .outputFormat("jpg")
                .toOutputStream(outputStream);
        return outputStream.toByteArray();
    }

    private static Positions getPosition(String position) {
        switch (position.toUpperCase()) {
            case "TOP_LEFT": return Positions.TOP_LEFT;
            case "TOP_CENTER": return Positions.TOP_CENTER;
            case "TOP_RIGHT": return Positions.TOP_RIGHT;
            case "CENTER_LEFT": return Positions.CENTER_LEFT;
            case "CENTER": return Positions.CENTER;
            case "CENTER_RIGHT": return Positions.CENTER_RIGHT;
            case "BOTTOM_LEFT": return Positions.BOTTOM_LEFT;
            case "BOTTOM_CENTER": return Positions.BOTTOM_CENTER;
            case "BOTTOM_RIGHT": return Positions.BOTTOM_RIGHT;
            default: return Positions.BOTTOM_RIGHT;
        }
    }

    private static BufferedImage createWatermark(String text, int fontSize) {
        BufferedImage watermark = new BufferedImage(200, 50, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = watermark.createGraphics();
        g2d.setFont(new Font("Arial", Font.BOLD, fontSize));
        g2d.setColor(Color.RED);
        g2d.drawString(text, 10, 30);
        g2d.dispose();
        return watermark;
    }
}
