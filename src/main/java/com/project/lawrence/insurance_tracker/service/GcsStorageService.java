package com.project.lawrence.insurance_tracker.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class GcsStorageService {

    @Value("${gcp.storage.bucket:insurance-tracker-claims-bucket}")
    private String bucketName;

    private final Storage storage;

    public GcsStorageService() {
        Storage storageInstance;
        try {
            // Automatically picks up credentials from environment (GCP default)
            storageInstance = StorageOptions.getDefaultInstance().getService();
        } catch (Exception e) {
            storageInstance = null;
        }
        this.storage = storageInstance;
    }

    public String uploadFile(MultipartFile file, String folder) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = folder + "/" + UUID.randomUUID().toString() + extension;

        if (storage == null) {
            // Local fallback for local development without active GCS credentials
            Path uploadDir = Paths.get("uploads", folder);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            Path destFile = uploadDir.resolve(UUID.randomUUID().toString() + extension);
            Files.write(destFile, file.getBytes());
            return destFile.toUri().toString();
        }

        BlobId blobId = BlobId.of(bucketName, filename);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();
        storage.create(blobInfo, file.getBytes());
        return String.format("https://storage.googleapis.com/%s/%s", bucketName, filename);
    }

    public byte[] downloadFile(String fileUrl) throws IOException {
        if (storage == null || fileUrl.startsWith("file:/")) {
            // Local fallback for reading local URI files
            if (fileUrl.startsWith("file:/")) {
                java.net.URI uri = java.net.URI.create(fileUrl);
                return Files.readAllBytes(Paths.get(uri));
            }
            return new byte[0];
        }

        // Parse GCS URL: https://storage.googleapis.com/bucket-name/folder/filename
        String prefix = "https://storage.googleapis.com/" + bucketName + "/";
        if (fileUrl.startsWith(prefix)) {
            String blobName = fileUrl.substring(prefix.length());
            BlobId blobId = BlobId.of(bucketName, blobName);
            return storage.readAllBytes(blobId);
        }
        throw new IllegalArgumentException("Invalid GCS URL for this bucket: " + fileUrl);
    }

    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) return;
        try {
            if (storage == null || fileUrl.startsWith("file:/")) {
                if (fileUrl.startsWith("file:/")) {
                    java.net.URI uri = java.net.URI.create(fileUrl);
                    Files.deleteIfExists(Paths.get(uri));
                }
                return;
            }

            String prefix = "https://storage.googleapis.com/" + bucketName + "/";
            if (fileUrl.startsWith(prefix)) {
                String blobName = fileUrl.substring(prefix.length());
                BlobId blobId = BlobId.of(bucketName, blobName);
                storage.delete(blobId);
            }
        } catch (Exception e) {
            System.err.println("Error deleting file: " + fileUrl + " -> " + e.getMessage());
        }
    }
}
