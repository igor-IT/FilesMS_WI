package com.microservice.upload.controller;

import com.microservice.upload.model.UserResponse;
import com.microservice.upload.service.FilesStorageService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
public class FilesController {

    @Autowired
    FilesStorageService storageService;

    @GetMapping("/helloWorld")
    public String helloWorld() {
        return "Hello from Upload Microservice!";
    }

    @PostMapping("/upload")
    public ResponseEntity<UserResponse> uploadFile(@RequestParam MultipartFile[] files, HttpSession session) {
        String sessionId = session.getId();

        try {
            UserResponse response = storageService.uploadFiles(files, sessionId);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            UserResponse response = new UserResponse();
            response.setStatus(e.getMessage());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
        }
    }

    @GetMapping(value = "/file/{dir}/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> getFile(@PathVariable String dir, @PathVariable String filename) {
        return storageService.getFile(dir, filename);
    }

    @DeleteMapping("/file/{dir}/{filename:.+}")
    @ResponseBody
    public ResponseEntity<?> deleteFile(@PathVariable String dir, @PathVariable String filename) {
        final String fullName = dir.concat("/").concat(filename);
        return storageService.delete(fullName);
    }
}