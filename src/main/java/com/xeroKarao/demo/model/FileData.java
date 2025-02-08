package com.xeroKarao.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Data
public class FileData {

    @Id
    @GeneratedValue
    private int id;

    private String fileName;
    private String fileUrl;

    public FileData(String filename,String fileurl){
        this.fileName = filename;
        this.fileUrl = fileurl;
    }
}
