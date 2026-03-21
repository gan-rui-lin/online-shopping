package com.helloworld.onlineshopping.utils;

import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import static org.junit.jupiter.api.Assertions.*;

public class ImageProcessorTest {

    private byte[] createTestPng() throws Exception {
        BufferedImage img = new BufferedImage(400, 300, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, 400, 300);
        g.setColor(Color.YELLOW);
        g.fillOval(50, 50, 300, 200);
        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        return baos.toByteArray();
    }

    @Test
    public void testGenerateThumbnailAddWatermarkCompress() throws Exception {
        byte[] original = createTestPng();

        byte[] thumb = ImageProcessor.generateThumbnail(original, 100, 100);
        assertNotNull(thumb);
        assertTrue(thumb.length > 0);

        byte[] watermarked = ImageProcessor.addWatermark(original, "Test Watermark");
        assertNotNull(watermarked);
        assertTrue(watermarked.length > 0);

        byte[] compressed = ImageProcessor.compressImage(original, 0.6f);
        assertNotNull(compressed);
        assertTrue(compressed.length > 0);
    }
}

