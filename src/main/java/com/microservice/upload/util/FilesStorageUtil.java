package com.microservice.upload.util;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class FilesStorageUtil {

    public static void saveFile(MeterRegistry meterRegistry, MultipartFile file, Path dir, String fileName) {
        Timer timer = meterRegistry.timer("save-file");
        timer.record(() -> {
            try {
                return Files.copy(file.getInputStream(), dir.resolve(fileName));
            } catch (IOException e) {
                log.error("A file of {} already exists", fileName);
                throw new RuntimeException("A file of that name already exists.", e);
            }
        });
        MetricsUtility.registerTimer(timer, "save-file-total-time", meterRegistry);
    }

    public static String generateFileName(String originalFileName) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String extension = FilenameUtils.getExtension(originalFileName);
        return uuid + "." + extension.toLowerCase();
    }

    public static String generateFileUrl(String siteUrl, String sessionId, String fileName) {
        return siteUrl.concat("/").concat(sessionId).concat("/").concat(fileName);
    }

    public static boolean isImageFile(MultipartFile file) {
        List<String> ALLOWED_IMAGE_TYPES = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp");
        return ALLOWED_IMAGE_TYPES.contains(FilesStorageUtil.getFileExtension(file).toLowerCase());
    }

    public static String getFileExtension(MultipartFile file) {
        return FilenameUtils.getExtension(StringUtils.cleanPath(file.getOriginalFilename()));
    }

    public static Resource load(Path root, String filename) {
        try {
            Path file = root.resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read the file!");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error: " + e.getMessage());

        }
    }
}