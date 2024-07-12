package com.example.springbootfileupload.entity;

import javax.persistence.*;

@Entity
public class UploadedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String originalFileName;
    private String timestampFileName;
    private String status;

    // Constructors, getters, and setters

    public UploadedFile() {
        // Default constructor
    }

    public UploadedFile(String originalFileName, String timestampFileName, String status) {
        this.originalFileName = originalFileName;
        this.timestampFileName = timestampFileName;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getTimestampFileName() {
        return timestampFileName;
    }

    public void setTimestampFileName(String timestampFileName) {
        this.timestampFileName = timestampFileName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
