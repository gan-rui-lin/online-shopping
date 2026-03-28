package com.helloworld.onlineshopping.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.net.URI;

@Configuration
public class LocalCdnConfig implements WebMvcConfigurer {
    @Value("${file.upload.base-dir:./uploads/images}")
    private String baseDir;

    @Value("${file.upload.public-url:/cdn/images/}")
    private String publicUrl;
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String publicPath = resolvePublicPath(publicUrl);
        String baseDirLocation = toFileLocation(baseDir);
        registry.addResourceHandler(publicPath + "**")
                .addResourceLocations(baseDirLocation);
    }

    private String resolvePublicPath(String url) {
        String path = url == null ? "/cdn/images/" : url.trim();
        if (path.isEmpty()) {
            path = "/cdn/images/";
        }
        try {
            URI uri = URI.create(path);
            if (uri.getPath() != null && !uri.getPath().isEmpty()) {
                path = uri.getPath();
            }
        } catch (IllegalArgumentException ignored) {
            // Keep provided path as-is if not a valid URI.
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (!path.endsWith("/")) {
            path = path + "/";
        }
        return path;
    }

    private String normalizeBaseDir(String dir) {
        String normalized = new File(dir).getAbsolutePath();
        if (!normalized.endsWith(File.separator)) {
            normalized = normalized + File.separator;
        }
        return normalized;
    }

    private String toFileLocation(String dir) {
        String normalized = normalizeBaseDir(dir);
        String uri = new File(normalized).toURI().toString();
        if (!uri.endsWith("/")) {
            uri = uri + "/";
        }
        return uri;
    }
}
