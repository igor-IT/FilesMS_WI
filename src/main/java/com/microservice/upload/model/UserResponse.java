package com.microservice.upload.model;

import lombok.Data;

import java.util.List;
@Data
public class UserResponse {
    private String status;
    private String time;

    private String folderId;
    private List<FileInfo> body;
}
