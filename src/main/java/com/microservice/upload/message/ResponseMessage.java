package com.microservice.upload.message;

import com.microservice.upload.model.FileInfo;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ResponseMessage {
    private String message;
    private List<FileInfo> files;

    public ResponseMessage(List<FileInfo> files) {
        this.files = files;
    }

    public ResponseMessage(String message) {
        this.message = message;
    }
}
