package com.helloworld.onlineshopping.modules.file.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileStorageService {

    @Value("${file.upload.base-dir:./uploads}")
    private String baseDir;

    @Value("${file.upload.public-url:/cdn/images/}")
    private String publicUrl;

    public String uploadFile(byte[] fileData, String fileName) {
        Path targetPath = resolveTargetPath(fileName);
        try {
            Files.createDirectories(targetPath.getParent());
            Files.write(targetPath, fileData);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to store file locally", e);
        }
        return buildPublicUrl(fileName);
    }

    public String generateSignedUrl(String fileName) {
        return buildPublicUrl(fileName);
    }

    private Path resolveTargetPath(String fileName) {
        Path rootPath = Paths.get(baseDir).toAbsolutePath().normalize();
        return rootPath.resolve(fileName);
    }

    private String buildPublicUrl(String fileName) {
        String prefix = publicUrl == null ? "/cdn/images/" : publicUrl.trim();
        if (prefix.isEmpty()) {
            prefix = "/cdn/images/";
        }
        if (!prefix.endsWith("/")) {
            prefix = prefix + "/";
        }
        return prefix + fileName;
    }
}
