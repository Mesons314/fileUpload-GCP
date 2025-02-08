package com.xeroKarao.demo.util;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import com.xeroKarao.demo.dto.FileDto;
import com.xeroKarao.demo.exception.FileWriteException;
import com.xeroKarao.demo.exception.GCPFileUploadException;
import com.xeroKarao.demo.exception.InvalidFileTypeException;
import lombok.Value;
import org.apache.coyote.BadRequestException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;

import static com.sun.org.apache.xalan.internal.xsltc.compiler.Constants.CHARACTERS;

@Component
public class BucketUtil {

    //Line 24 to 35 are present in the
    //resources -> application.properties
    //there you have to do the gcp configuration
    @Value("${gcp.config.file}")
    private String gcpConfigFile;

    @Value("${gcp.project.id}")//the id of the project in gcp would come
    private String gcpProjectId;

    @Value("${gcp.bucket.id}")
    private String gcpBucketId;

    @Value("${gcp.dir.name}")
    private String gcpDirectoryName;

    private static final String[] ALLOWED_EXTENSIONS = {".png", ".jpeg", ".pdf", ".doc", ".mp3"};


    //Uploading the files to GCP and return its url
    public FileDto uploadFile(MultipartFile multipartFile, String fileName, String contentType){

        try (InputStream credentialStream = new ClassPathResource(gcpConfigFile).getInputStream()){
            Storage storage = StorageOptions.newBuilder()
                    .setCredentials(GoogleCredentials.fromStream(credentialStream))
                    .build()
                    .getService();

            byte[] fileData = convertFile(multipartFile);

            String uniqueFileName = generateUniqueName(fileName);

            BlobId blobId = BlobId.of(gcpBucketId,gcpDirectoryName+" / " + uniqueFileName);

            //Creating the metadata of the file
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(contentType)
                    .build();

            //this part is uploading the file to gcp
            Blob blob = storage.create(blobInfo,fileData);

            //returning the file info
            if(blob != null){
                return new FileDto(blob.getName(),blob.getMediaLink());
            }
        } catch (IOException e) {
            throw  new GCPFileUploadException("An error has occured");
        }
        throw  new GCPFileUploadException("An error has occured");
    }

    //Converting The file to bytes
    private byte[] convertFile(MultipartFile file){
        try{
            if(file.getOriginalFilename() == null){
                throw new BadRequestException("This is not running");
            }
            return file.getBytes();
        }catch (IOException e){
            throw new FileWriteException("An error has occured while converting the file");
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
        if(fileName != null && fileName.contains(".")){
            String fileExtension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();

            for(String allowedExtensions:ALLOWED_EXTENSIONS ){
                if(fileExtension.equals(allowedExtensions)){
                    return allowedExtensions;
                }
            }
        }

        throw new InvalidFileTypeException("something is wrong");
    }


}
