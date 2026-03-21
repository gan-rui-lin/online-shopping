package com.helloworld.onlineshopping.modules.file;

import com.helloworld.onlineshopping.modules.file.service.FileStorageService;
import com.helloworld.onlineshopping.modules.file.vo.FileUploadResponse;
import com.helloworld.onlineshopping.modules.file.vo.SignedUrlResponse;
import com.helloworld.onlineshopping.utils.ImageProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileStorageService fileStorageService;

    // File upload limits
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/gif", "image/webp");

    @Autowired
    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        validateFile(file);

        byte[] fileData = file.getBytes();
        String fileName = generateUniqueFileName(file.getOriginalFilename());

        // Process image: generate thumbnail, add watermark, compress
        byte[] processedData = processImage(fileData);

        String fileUrl = fileStorageService.uploadFile(processedData, fileName);
        String thumbnailUrl = fileStorageService.uploadFile(ImageProcessor.generateThumbnail(fileData, 200, 200), "thumb_" + fileName);

        FileUploadResponse response = new FileUploadResponse();
        response.setUrl(fileUrl);
        response.setThumbnailUrl(thumbnailUrl);
        response.setFileName(fileName);
        response.setFileType(file.getContentType());
        response.setFileSize(file.getSize());
        response.setUploadTime(java.time.LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload/multiple")
    public ResponseEntity<List<FileUploadResponse>> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        List<FileUploadResponse> results = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                validateFile(file);
                FileUploadResponse result = uploadSingleFile(file);
                results.add(result);
            } catch (Exception e) {
                FileUploadResponse error = new FileUploadResponse();
                error.setFileName(file != null ? file.getOriginalFilename() : null);
                error.setUrl(null);
                error.setThumbnailUrl(null);
                // store error message in category field to avoid changing VO
                error.setCategory("Failed: " + e.getMessage());
                results.add(error);
            }
        }

        return ResponseEntity.ok(results);
    }

    private FileUploadResponse uploadSingleFile(MultipartFile file) throws IOException {
        byte[] fileData = file.getBytes();
        String fileName = generateUniqueFileName(file.getOriginalFilename());

        byte[] processedData = processImage(fileData);

        String fileUrl = fileStorageService.uploadFile(processedData, fileName);
        String thumbnailUrl = fileStorageService.uploadFile(ImageProcessor.generateThumbnail(fileData, 200, 200), "thumb_" + fileName);

        FileUploadResponse response = new FileUploadResponse();
        response.setUrl(fileUrl);
        response.setThumbnailUrl(thumbnailUrl);
        response.setFileName(fileName);
        response.setFileType(file.getContentType());
        response.setFileSize(file.getSize());
        response.setUploadTime(java.time.LocalDateTime.now());

        return response;
    }

    @GetMapping("/signed-url")
    public ResponseEntity<SignedUrlResponse> getSignedUrl(@RequestParam("fileName") String fileName) {
        String signedUrl = fileStorageService.generateSignedUrl(fileName);
        SignedUrlResponse response = new SignedUrlResponse();
        response.setSignedUrl(signedUrl);
        response.setFileName(fileName);
        // expiration not provided by service, leave null or could be set if available
        return ResponseEntity.ok(response);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum limit of " + (MAX_FILE_SIZE / 1024 / 1024) + "MB");
        }

        if (!ALLOWED_IMAGE_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("File type not allowed. Allowed types: " + ALLOWED_IMAGE_TYPES);
        }
    }

    private byte[] processImage(byte[] fileData) throws IOException {
        // Generate thumbnail
        byte[] thumbnail = ImageProcessor.generateThumbnail(fileData, 800, 800);

        // Add watermark
        byte[] watermarked = ImageProcessor.addWatermark(thumbnail, "Online Shopping", "BOTTOM_RIGHT", 0.3f, 24);

        // Compress
        return ImageProcessor.compressImage(watermarked, 0.8f);
    }

    private String generateUniqueFileName(String originalFilename) {
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            originalFilename = "file";
        }
        int idx = originalFilename.lastIndexOf('.');
        String extension = idx >= 0 ? originalFilename.substring(idx) : ".dat";
        return UUID.randomUUID() + extension;
    }
}
