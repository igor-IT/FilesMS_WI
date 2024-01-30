package com.microservice.upload.controller;

import com.microservice.upload.model.FileInfo;
import com.microservice.upload.model.UserResponse;
import com.microservice.upload.service.FilesStorageService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
public class FilesController {

    @Autowired
    FilesStorageService storageService;

    @GetMapping("/helloWorld")
    public String helloWorld(){
        return "Hello from Upload Microservice!";
    }

    @PostMapping("/upload")
    public ResponseEntity<UserResponse> uploadFile(@RequestParam MultipartFile[] files, HttpSession session) {
        String sessionId = session.getId();

        List<FileInfo> filesInfo = new ArrayList<>();

        UserResponse response = new UserResponse();
        LocalDateTime localDate = LocalDateTime.now(); // fixed: LocalDateTime
        DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String date = dtFormatter.format(localDate);
        response.setTime(date);

        try {
            for (MultipartFile file : files) {
                FileInfo info =
                        storageService.save(file, sessionId);
                filesInfo.add(info);

            }
            response.setStatus("Ok");
            response.setFolderId(sessionId);
            response.setBody(filesInfo);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            response.setStatus(e.getMessage());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
        }
    }

    @GetMapping(value ="/file/{dir}/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> getFile(@PathVariable String dir, @PathVariable String filename) {
        final String fullName = dir.concat("/").concat(filename);
        try {
            Resource file = storageService.load(fullName);
            log.info(String.format("get file %s", fullName));
            MediaType type = null;
            String extension = FilenameUtils.getExtension(filename.toLowerCase());

            switch (extension){
                case "jpg": type = MediaType.IMAGE_JPEG;break;
                case "png": type = MediaType.IMAGE_PNG; break;
                case "pdf": type=MediaType.APPLICATION_PDF;break;
            }

            if (type==null) {
                return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;  filename=\"" + file.getFilename() + "\"").body(file);}
             {return ResponseEntity.ok().contentType(type)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline;  filename=\"" + file.getFilename() + "\"").body(file);}
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).build();
        }
    }

    @DeleteMapping("/file/{dir}/{filename:.+}")
    @ResponseBody
    public ResponseEntity deleteFile(@PathVariable String dir, @PathVariable String filename) {
        final String fullName = dir.concat("/").concat(filename);
        try {
            storageService.delete(fullName);
            log.info(String.format("delete %s from admin", fullName));
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.error(String.format("delete wrong %s", fullName));
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
