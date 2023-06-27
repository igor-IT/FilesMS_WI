package com.microservice.upload.service;

import com.microservice.upload.model.FileInfo;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public interface  FilesStorageService {

    public void init();

    public FileInfo save(MultipartFile file, String sessionId) throws IOException;

    public Resource load(String filename);

    public void delete(String filename);


}
