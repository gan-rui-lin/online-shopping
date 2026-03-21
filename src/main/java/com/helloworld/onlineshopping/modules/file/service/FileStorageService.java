package com.helloworld.onlineshopping.modules.file.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.PutObjectRequest;
import com.helloworld.onlineshopping.config.OSSConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

@Slf4j
@Service
public class FileStorageService {

    private final OSSConfig ossConfig;
    private final OSS ossClient;

    @Autowired
    public FileStorageService(OSSConfig ossConfig) {
        this.ossConfig = ossConfig;
        if (ossConfig.getAccessKey() != null && !ossConfig.getAccessKey().isEmpty()) {
            this.ossClient = new OSSClientBuilder().build(ossConfig.getEndpoint(), ossConfig.getAccessKey(), ossConfig.getSecretKey());
        } else {
            this.ossClient = null;
        }
    }

    public String uploadFile(byte[] fileData, String fileName) {
        if (ossClient == null) {
            throw new RuntimeException("OSS is not configured. Please set environment variables: OSS_ENDPOINT, OSS_BUCKET, OSS_ACCESS_KEY, OSS_SECRET_KEY");
        }
        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(ossConfig.getBucket(), fileName, new ByteArrayInputStream(fileData));
            ossClient.putObject(putObjectRequest);
            return "https://" + ossConfig.getBucket() + "." + ossConfig.getEndpoint() + "/" + fileName;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to OSS", e);
        }
    }

    public String uploadFileWithProcessing(byte[] fileData, String fileName, boolean generateThumbnail, boolean addWatermark) throws IOException {
        byte[] processedData = fileData;

        if (generateThumbnail || addWatermark) {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(fileData));
            if (image != null) {
                // 生成缩略图
                if (generateThumbnail) {
                    image = createThumbnail(image, 200, 200);
                }

                // 添加水印
                if (addWatermark) {
                    image = addWatermark(image, "Online Shopping");
                }

                // 转换为字节数组
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                String format = getImageFormat(fileName);
                ImageIO.write(image, format, baos);
                processedData = baos.toByteArray();
            }
        }

        return uploadFile(processedData, fileName);
    }

    public String generateSignedUrl(String fileName) {
        if (ossClient == null) {
            throw new RuntimeException("OSS is not configured. Please set environment variables: OSS_ENDPOINT, OSS_BUCKET, OSS_ACCESS_KEY, OSS_SECRET_KEY");
        }
        try {
            Date expiration = new Date(System.currentTimeMillis() + 3600 * 1000); // 1 hour
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(ossConfig.getBucket(), fileName);
            request.setExpiration(expiration);
            URL signedUrl = ossClient.generatePresignedUrl(request);
            return signedUrl.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate signed URL", e);
        }
    }

    private BufferedImage createThumbnail(BufferedImage originalImage, int width, int height) {
        BufferedImage thumbnail = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = thumbnail.createGraphics();
        g2d.drawImage(originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);
        g2d.dispose();
        return thumbnail;
    }

    private BufferedImage addWatermark(BufferedImage image, String watermarkText) {
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(new Color(255, 255, 255, 128)); // 半透明白色
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        FontMetrics fm = g2d.getFontMetrics();
        int x = image.getWidth() - fm.stringWidth(watermarkText) - 10;
        int y = image.getHeight() - 10;
        g2d.drawString(watermarkText, x, y);
        g2d.dispose();
        return image;
    }

    private String getImageFormat(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "JPEG";
            case "png":
                return "PNG";
            case "gif":
                return "GIF";
            default:
                return "JPEG";
        }
    }

    // Close OSS client when service is destroyed
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }
}
