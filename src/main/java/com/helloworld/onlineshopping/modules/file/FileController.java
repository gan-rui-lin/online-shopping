package com.helloworld.onlineshopping.modules.file;

import com.helloworld.onlineshopping.modules.file.service.FileRecordService;
import com.helloworld.onlineshopping.modules.file.service.FileStorageService;
import com.helloworld.onlineshopping.modules.file.vo.FileUploadResponse;
import com.helloworld.onlineshopping.modules.file.vo.MultipleFileUploadResponse;
import com.helloworld.onlineshopping.modules.file.vo.SignedUrlResponse;
import com.helloworld.onlineshopping.utils.ImageProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileStorageService fileStorageService;
    private final FileRecordService fileRecordService;

    @Value("${file.upload.max-size:10MB}")
    private DataSize maxFileSize;

    @Value("${file.upload.allowed-types:image/jpeg,image/png,image/gif,image/webp}")
    private String allowedTypes;

    @Value("${file.upload.main-image.force-spec:true}")
    private boolean forceMainImageSpec;

    @Value("${file.upload.main-image.width:800}")
    private int mainImageWidth;

    @Value("${file.upload.main-image.height:800}")
    private int mainImageHeight;

    @Value("${file.upload.main-image.quality:0.85}")
    private float mainImageQuality;

    private Set<String> allowedImageTypes;

    @Autowired
    public FileController(FileStorageService fileStorageService, FileRecordService fileRecordService) {
        this.fileStorageService = fileStorageService;
        this.fileRecordService = fileRecordService;
    }

    @PostConstruct
    private void initAllowedTypes() {
        allowedImageTypes = Arrays.stream(allowedTypes.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toSet());
    }

    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "generateThumbnail", defaultValue = "true") boolean generateThumbnail,
            @RequestParam(value = "addWatermark", defaultValue = "false") boolean addWatermark) throws IOException {
        validateFile(file);
        FileUploadResponse response = uploadSingleFile(file, category, generateThumbnail, addWatermark);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload/multiple")
    public ResponseEntity<MultipleFileUploadResponse> uploadMultipleFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "generateThumbnail", defaultValue = "true") boolean generateThumbnail,
            @RequestParam(value = "addWatermark", defaultValue = "false") boolean addWatermark) {
        List<FileUploadResponse> results = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                validateFile(file);
                results.add(uploadSingleFile(file, category, generateThumbnail, addWatermark));
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

        MultipleFileUploadResponse response = new MultipleFileUploadResponse();
        response.setFiles(results);
        response.setTotalCount(results.size());
        response.setUploadTime(java.time.LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload/local-folder")
    public ResponseEntity<MultipleFileUploadResponse> uploadFromLocalFolder(
            @RequestParam("folderPath") String folderPath,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "generateThumbnail", defaultValue = "true") boolean generateThumbnail,
            @RequestParam(value = "addWatermark", defaultValue = "false") boolean addWatermark,
            @RequestParam(value = "recursive", defaultValue = "false") boolean recursive) {
        Path folder = Paths.get(folderPath).toAbsolutePath().normalize();
        if (!Files.exists(folder) || !Files.isDirectory(folder)) {
            throw new IllegalArgumentException("Folder does not exist: " + folder);
        }

        List<FileUploadResponse> results = new ArrayList<>();
        try (Stream<Path> stream = recursive ? Files.walk(folder) : Files.list(folder)) {
            List<Path> files = stream
                    .filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .collect(Collectors.toList());

            for (Path filePath : files) {
                try {
                    String contentType = resolveContentType(filePath);
                    byte[] fileData = Files.readAllBytes(filePath);
                    validateFile(filePath.getFileName().toString(), contentType, fileData.length);
                    results.add(uploadSingleFile(fileData, filePath.getFileName().toString(), contentType, category, generateThumbnail, addWatermark));
                } catch (Exception e) {
                    FileUploadResponse error = new FileUploadResponse();
                    error.setFileName(filePath.getFileName().toString());
                    error.setCategory("Failed: " + e.getMessage());
                    results.add(error);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to scan local folder", e);
        }

        MultipleFileUploadResponse response = new MultipleFileUploadResponse();
        response.setFiles(results);
        response.setTotalCount(results.size());
        response.setUploadTime(java.time.LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    private FileUploadResponse uploadSingleFile(MultipartFile file, String category, boolean generateThumbnail, boolean addWatermark) throws IOException {
        return uploadSingleFile(file.getBytes(), file.getOriginalFilename(), file.getContentType(), category, generateThumbnail, addWatermark);
    }

    private FileUploadResponse uploadSingleFile(byte[] fileData, String originalFilename, String contentType, String category,
                                                boolean generateThumbnail, boolean addWatermark) throws IOException {
        String fileName = generateUniqueFileName(originalFilename);

        byte[] processedData = processMainImage(fileData, category, addWatermark);
        String fileUrl = fileStorageService.uploadFile(processedData, fileName);

        String thumbnailUrl = null;
        if (generateThumbnail) {
            byte[] thumbnailData = ImageProcessor.generateThumbnail(fileData, 200, 200);
            thumbnailUrl = fileStorageService.uploadFile(thumbnailData, "thumb_" + fileName);
        }

        fileRecordService.saveFileRecord(
                fileName,
                originalFilename,
                contentType,
                (long) fileData.length,
                fileUrl,
                thumbnailUrl,
                category,
                null
        );

        FileUploadResponse response = new FileUploadResponse();
        response.setUrl(fileUrl);
        response.setThumbnailUrl(thumbnailUrl);
        response.setFileName(fileName);
        response.setFileType(contentType);
        response.setFileSize((long) fileData.length);
        response.setUploadTime(java.time.LocalDateTime.now());
        response.setCategory(category);

        return response;
    }

    @GetMapping("/signed-url")
    public ResponseEntity<SignedUrlResponse> getSignedUrl(@RequestParam("fileName") String fileName) {
        String signedUrl = fileStorageService.generateSignedUrl(fileName);
        SignedUrlResponse response = new SignedUrlResponse();
        response.setSignedUrl(signedUrl);
        response.setFileName(fileName);
        return ResponseEntity.ok(response);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        long maxBytes = maxFileSize.toBytes();
        if (file.getSize() > maxBytes) {
            throw new IllegalArgumentException("File size exceeds maximum limit of " + (maxBytes / 1024 / 1024) + "MB");
        }

        if (file.getContentType() == null || !allowedImageTypes.contains(file.getContentType())) {
            throw new IllegalArgumentException("File type not allowed. Allowed types: " + allowedImageTypes);
        }
    }

    private void validateFile(String originalFileName, String contentType, long fileSize) {
        if (fileSize <= 0) {
            throw new IllegalArgumentException("File is empty");
        }
        long maxBytes = maxFileSize.toBytes();
        if (fileSize > maxBytes) {
            throw new IllegalArgumentException("File size exceeds maximum limit of " + (maxBytes / 1024 / 1024) + "MB");
        }
        if (contentType == null || !allowedImageTypes.contains(contentType)) {
            throw new IllegalArgumentException("File type not allowed for " + originalFileName + ". Allowed types: " + allowedImageTypes);
        }
    }

    private String resolveContentType(Path path) throws IOException {
        String contentType = Files.probeContentType(path);
        if (contentType != null) {
            return contentType;
        }

        String fileName = path.getFileName().toString().toLowerCase();
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (fileName.endsWith(".png")) {
            return "image/png";
        }
        if (fileName.endsWith(".gif")) {
            return "image/gif";
        }
        if (fileName.endsWith(".webp")) {
            return "image/webp";
        }
        return null;
    }

    private byte[] processMainImage(byte[] fileData, String category, boolean addWatermark) throws IOException {
        byte[] processed = fileData;
        if (addWatermark) {
            processed = ImageProcessor.addWatermark(processed, "Online Shopping", "BOTTOM_RIGHT", 0.3f, 24);
        }
        if (forceMainImageSpec && isMainImageCategory(category)) {
            return ImageProcessor.normalizeMainImage(processed, mainImageWidth, mainImageHeight, mainImageQuality);
        }
        return ImageProcessor.compressImage(processed, 0.85f);
    }

    private boolean isMainImageCategory(String category) {
        if (!StringUtils.hasText(category)) {
            return true;
        }
        String normalized = category.trim().toLowerCase(Locale.ROOT);
        return normalized.equals("main")
                || normalized.equals("spu_main")
                || normalized.startsWith("spu-")
                || normalized.startsWith("spu_main-");
    }

    private String generateUniqueFileName(String originalFilename) {
        String safeName = originalFilename == null ? "file" : originalFilename.trim();
        if (safeName.isEmpty()) {
            safeName = "file";
        }
        int idx = safeName.lastIndexOf('.');
        String extension = idx >= 0 ? safeName.substring(idx) : ".dat";
        return UUID.randomUUID() + extension;
    }
}
