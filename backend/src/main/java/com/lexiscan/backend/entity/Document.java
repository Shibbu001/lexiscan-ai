
package com.lexiscan.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    private String storedFileName;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private Long fileSize;

    @Column(columnDefinition = "TEXT")
    private String extractedText;

    @Column(columnDefinition = "TEXT")
    private String analysisJson;

    @Column(nullable = false)
    private String status = "UPLOADED";

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    public Long getId() {
        return id;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String v) {
        originalFileName = v;
    }

    public String getStoredFileName() {
        return storedFileName;
    }

    public void setStoredFileName(String v) {
        storedFileName = v;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String v) {
        contentType = v;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long v) {
        fileSize = v;
    }

    public String getExtractedText() {
        return extractedText;
    }

    public void setExtractedText(String v) {
        extractedText = v;
    }

    public String getAnalysisJson() {
        return analysisJson;
    }

    public void setAnalysisJson(String v) {
        analysisJson = v;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String v) {
        status = v;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User v) {
        user = v;
    }
}
