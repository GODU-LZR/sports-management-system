package com.example.middleware.minio.pojo;

import lombok.Data;

import java.io.Serializable;

@Data
public class UploadResult  implements Serializable {

    private String url;
    private String fileRecordID;
}
