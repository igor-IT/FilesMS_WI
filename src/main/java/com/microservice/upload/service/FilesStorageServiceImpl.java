package com.microservice.upload.service;

import com.microservice.upload.model.FileInfo;
import com.microservice.upload.model.UserResponse;
import com.microservice.upload.util.FilesStorageUtil;
import com.microservice.upload.util.ImageUtil;
import com.microservice.upload.util.MetricsUtility;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilesStorageServiceImpl implements FilesStorageService {

    private static final long MAX_IMAGE_SIZE = 2 * 1024 * 1024; // 2 MB
    @Value("${site.url}")
    private String siteUrl;
    private final Path root;
    private final AtomicLong fileSize = new AtomicLong(0);
    private final MeterRegistry meterRegistry;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @Autowired
    public FilesStorageServiceImpl(@Value("${upload.dir}") String uploadDir, MeterRegistry meterRegistry) {
        this.root = Paths.get(uploadDir);
        this.meterRegistry = meterRegistry;
    }

    @Override
    @SneakyThrows
    public void init() {
        Files.createDirectories(root);
    }

    @Override
    public UserResponse uploadFiles(MultipartFile[] files, String sessionId) {
        UserResponse response = new UserResponse();
        LocalDateTime localDate = LocalDateTime.now();
        DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        response.setTime(dtFormatter.format(localDate));

        List<FileInfo> filesInfo = Arrays.stream(files)
                .map(file -> save(file, sessionId, meterRegistry))
                .collect(Collectors.toList());

        response.setStatus("Ok");
        response.setFolderId(sessionId);
        response.setBody(filesInfo);
        return response;
    }

    @SneakyThrows
    public FileInfo save(MultipartFile file, String sessionId, MeterRegistry meterRegistry) {
        String newDir = root.resolve(sessionId).toString();
        Path dir = Files.createDirectories(Path.of(newDir));

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileName = FilesStorageUtil.generateFileName(originalFileName);
        String fileUrl = FilesStorageUtil.generateFileUrl(siteUrl, String.valueOf(file), sessionId);
        FileInfo fileInfo = new FileInfo(originalFileName, fileUrl);

        fileSize.set(file.getSize());
        MetricsUtility.registerFileSizeMetric(fileSize, meterRegistry);

        if (FilesStorageUtil.isImageFile(file) && file.getSize() > MAX_IMAGE_SIZE) {
            executorService.submit(new ImageUtil(meterRegistry, file, FilesStorageUtil.getFileExtension(file), dir.resolve(fileName)));
        } else {
            FilesStorageUtil.saveFile(meterRegistry, file, dir, fileName);
        }

        return fileInfo;
    }

    @Override
    public ResponseEntity<Resource> getFile(String dir, String filename) {
        final String fullName = dir.concat("/").concat(filename);

        Resource file = FilesStorageUtil.load(this.root, fullName);
        log.info(String.format("get file %s", fullName));
        MediaType type = null;
        String extension = FilenameUtils.getExtension(filename.toLowerCase());

        switch (extension) {
            case "jpg" -> type = MediaType.IMAGE_JPEG;
            case "png" -> type = MediaType.IMAGE_PNG;
            case "pdf" -> type = MediaType.APPLICATION_PDF;
        }

        if (type == null) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;  filename=\"" + file.getFilename() + "\"").body(file);
        } else {
            return ResponseEntity.ok().contentType(type)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline;  filename=\"" + file.getFilename() + "\"").body(file);
        }
    }

    @Override
    @SneakyThrows
    public ResponseEntity<?> delete(String filename) {
        Path file = this.root.resolve(filename);
        Files.deleteIfExists(file);
        return ResponseEntity.ok().build();
    }
}