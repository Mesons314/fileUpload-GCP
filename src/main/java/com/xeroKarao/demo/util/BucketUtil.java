package com.xeroKarao.demo.util;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import com.xeroKarao.demo.dto.FileDto;
import com.xeroKarao.demo.exception.FileWriteException;
import com.xeroKarao.demo.exception.GCPFileUploadException;
import com.xeroKarao.demo.exception.InvalidFileTypeException;
import com.xeroKarao.demo.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;

@Service
public class BucketUtil {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private final String gcpConfigFile;
    private final String gcpProjectId;
    private final String gcpBucketId;
    private final String gcpDirectoryName;

    private static final String[] ALLOWED_EXTENSIONS = {".png", ".jpeg", ".pdf", ".doc"};
    private Storage storage; // Store the GCP Storage object to reuse it

    // Constructor-based dependency injection
    public BucketUtil(
            @Value("${spring.cloud.gcp.credentials.location}") String gcpConfigFile,
            @Value("${spring.cloud.gcp.project-id}") String gcpProjectId,
            @Value("${gcp.bucket.id}") String gcpBucketId,
            @Value("${gcp.dir.name}") String gcpDirectoryName) {
        this.gcpConfigFile = gcpConfigFile.replace("file:", "");
        this.gcpProjectId = gcpProjectId;
        this.gcpBucketId = gcpBucketId;
        this.gcpDirectoryName = gcpDirectoryName;
    }

    // Initialize GCP Storage after Spring sets up the beans
    @PostConstruct
    private void initializeStorage() throws IOException {
        try (InputStream credentialStream = new ClassPathResource(gcpConfigFile).getInputStream()) {
            this.storage = StorageOptions.newBuilder()
                    .setCredentials(GoogleCredentials.fromStream(credentialStream))
                    .setProjectId(gcpProjectId)
                    .build()
                    .getService();
        }
    }

    // Uploading the files to GCP and return its URL
    public FileDto uploadFile(MultipartFile multipartFile, String fileName, String contentType) throws IOException {
        byte[] fileData = convertFile(multipartFile);

        String uniqueFileName = generateUniqueName(fileName);
        BlobId blobId = BlobId.of(gcpBucketId, gcpDirectoryName + "/" + uniqueFileName);

        // Creating the metadata of the file
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(contentType)
                .build();

        // Uploading the file to GCP
        Blob blob = storage.create(blobInfo, fileData);

        // Returning the file info
        if (blob != null) {
            return new FileDto(blob.getName(), blob.getMediaLink());
        }
        throw new GCPFileUploadException("An error has occurred");
    }

    // Converting The file to bytes
    private byte[] convertFile(MultipartFile file) {
        try {
            if (file.getOriginalFilename() == null) {
                throw new BadRequestException("File name is missing");
            }
            return file.getBytes();
        } catch (IOException e) {
            throw new FileWriteException("An error has occurred while converting the file");
        }
    }

    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generateUniqueName(String fileName) {
        String randomString = generateRandomString(6); // Generate a 6-character secure random string
        String extension = checkFileExtension(fileName);
        return fileName + "-" + randomString + extension;
    }

    private static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    private static String checkFileExtension(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            String fileExtension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();

            for (String allowedExtensions : ALLOWED_EXTENSIONS) {
                if (fileExtension.equals(allowedExtensions)) {
                    return allowedExtensions;
                }
            }
        }
        throw new InvalidFileTypeException("Invalid file type");
    }
}
