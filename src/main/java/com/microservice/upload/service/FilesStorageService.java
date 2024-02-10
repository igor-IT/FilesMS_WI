package com.microservice.upload.service;

import com.microservice.upload.model.UserResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface FilesStorageService {

    public void init();

    public UserResponse uploadFiles(MultipartFile[] files, String sessionId);

    public ResponseEntity<Resource> getFile(String dir, String filename);

    public ResponseEntity<?> delete(String filename);
}