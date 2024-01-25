package com.microservice.upload.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.microservice.upload.model.FileInfo;
import com.microservice.upload.util.ImageUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class FilesStorageServiceImpl implements FilesStorageService{

    @Value("${site.url}") String siteUrl;
    private final String uploadDir;
    private final Path root ;
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp");
    private static final long MAX_IMAGE_SIZE = 2 * 1024 * 1024; // 2 MB
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);


    @Autowired
    public FilesStorageServiceImpl(@Value("${upload.dir}") String uploadDir ) {
        this.uploadDir = uploadDir;
        this.root = Paths.get(uploadDir);
    }

    //
    @Override
    public void init() {

        try {
            Files.createDirectories(root);
            log.info("upload dir created!");
        } catch (IOException e) {
            log.error("Could not initialize folder for upload!");
            throw new RuntimeException("Could not initialize folder for upload!");
        }
    }
    @Override
    public FileInfo save(MultipartFile file, String sessionId) throws IOException {
        String newDir = root.toString().concat("/").concat(sessionId);
        Path dir = Files.createDirectories(Path.of(newDir));

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String extension = FilenameUtils.getExtension(originalFileName);
        String fileName = uuid + "." + extension.toLowerCase();
        String fileUrl = siteUrl.concat("/").concat(sessionId).concat("/").concat(fileName);
        FileInfo fileInfo = new FileInfo(originalFileName, fileUrl);

        if (isImageFile(extension) && file.getSize() > MAX_IMAGE_SIZE) {
            executorService.submit(new ImageUtil(file, extension, dir.resolve(fileName)));
            executorService.shutdown();
            System.out.println(String.format("File %s saved as %s, url: %s", originalFileName, dir.resolve(fileName).toString(), fileUrl));

            return fileInfo;
        } else {
            try {
                Files.copy(file.getInputStream(), dir.resolve(fileName));
                System.out.println(String.format("Non-image file %s saved as %s, url: %s", originalFileName, dir.resolve(fileName).toString(), fileUrl));

                return fileInfo;
            } catch (Exception e) {
                if (e instanceof FileAlreadyExistsException) {
                    log.error(String.format("A file of %s already exists",fileName));
                    throw new RuntimeException("A file of that name already exists.");
                }
                log.error(String.format("save %s error %s",originalFileName,e.getMessage()));
                throw new RuntimeException(e.getMessage());
            }
        }
    }

    private boolean isImageFile(String extension) {
        return ALLOWED_IMAGE_TYPES.contains(extension.toLowerCase());
    }

    @Override
    public Resource load(String filename) {

        try {
            Path file = this.root.resolve(filename);
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



    @Override
    public void delete(String filename) {
        try {
            Path file = this.root.resolve(filename);
            Files.deleteIfExists(file);

        }  catch (IOException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }


}
