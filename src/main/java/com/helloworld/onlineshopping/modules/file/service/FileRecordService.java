package com.helloworld.onlineshopping.modules.file.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.helloworld.onlineshopping.modules.file.entity.FileRecord;
import com.helloworld.onlineshopping.modules.file.mapper.FileRecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
public class FileRecordService {

    @Autowired
    private FileRecordMapper fileRecordMapper;

    @Value("${file.upload.temp-dir:/tmp/uploads}")
    private String tempDir;

    @Value("${file.upload.cleanup-interval:3600000}")
    private long cleanupInterval;

    public FileRecord saveFileRecord(String fileName, String originalName, String fileType, Long fileSize,
                                   String url, String thumbnailUrl, String category, String uploaderId) {
        FileRecord record = new FileRecord();
        record.setFileName(fileName);
        record.setOriginalName(originalName);
        record.setFileType(fileType);
        record.setFileSize(fileSize);
        record.setUrl(url);
        record.setThumbnailUrl(thumbnailUrl);
        record.setCategory(category);
        record.setUploaderId(uploaderId);
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());

        fileRecordMapper.insert(record);
        return record;
    }

    public List<FileRecord> getFilesByCategory(String category) {
        LambdaQueryWrapper<FileRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileRecord::getCategory, category)
               .orderByDesc(FileRecord::getCreateTime);
        return fileRecordMapper.selectList(wrapper);
    }

    public List<FileRecord> getFilesByUploader(String uploaderId) {
        LambdaQueryWrapper<FileRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileRecord::getUploaderId, uploaderId)
               .orderByDesc(FileRecord::getCreateTime);
        return fileRecordMapper.selectList(wrapper);
    }

    public boolean deleteFileRecord(Long id) {
        return fileRecordMapper.deleteById(id) > 0;
    }

    /**
     * Scheduled task to clean up temporary files older than cleanup interval
     * Runs every hour
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    public void cleanupTempFiles() {
        try {
            Path tempPath = Paths.get(tempDir);
            if (!Files.exists(tempPath)) {
                return;
            }

            long cutoffTime = System.currentTimeMillis() - cleanupInterval;

            try (Stream<Path> paths = Files.walk(tempPath)) {
                paths.filter(Files::isRegularFile)
                        .filter(path -> {
                            try {
                                BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
                                return attrs.lastModifiedTime().toMillis() < cutoffTime;
                            } catch (IOException e) {
                                return false;
                            }
                        })
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                                log.info("Cleaned up temp file: {}", path);
                            } catch (IOException e) {
                                log.error("Failed to delete temp file: {}", path, e);
                            }
                        });
            }
        } catch (Exception e) {
            log.error("Error during temp file cleanup", e);
        }
    }

    /**
     * Create temp directory if not exists
     */
    public void ensureTempDirExists() {
        try {
            Path tempPath = Paths.get(tempDir);
            if (!Files.exists(tempPath)) {
                Files.createDirectories(tempPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temp directory", e);
        }
    }
}
