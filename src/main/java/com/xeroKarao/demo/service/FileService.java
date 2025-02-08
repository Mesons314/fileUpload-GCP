package com.xeroKarao.demo.service;

import com.xeroKarao.demo.model.FileData;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {
    public List<FileData> uploadFile(MultipartFile[] files);
}
