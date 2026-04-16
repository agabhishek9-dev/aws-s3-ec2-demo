package com.letslearn.aswtutorial.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

/**
 * Service class responsible for interacting with Amazon S3.
 *
 * Revision note:
 * - @Service tells Spring to manage this class as a service-layer bean.
 * - This class contains business logic related to file upload and download.
 * - It uses AWS SDK v2 S3Client to communicate with an S3 bucket.
 */
@Service
public class S3Service {

    /**
     * S3Client is the main AWS SDK object used to perform operations on S3.
     *
     * Revision note:
     * - Spring injects this dependency automatically.
     * - S3Client must be configured elsewhere as a Spring bean.
     */
    @Autowired
    private S3Client s3Client;

    /**
     * Reads the bucket name from application.properties or application.yml.
     *
     * Example:
     * aws.bucket.name=my-demo-bucket
     *
     * Revision note:
     * - @Value is used to inject a configuration value.
     * - This avoids hardcoding the bucket name in Java code.
     */
    @Value("${aws.bucket.name}")
    private String bucketName;

    /**
     * Uploads a file received from the client into the configured S3 bucket.
     *
     * @param file MultipartFile received from the request
     * @return the file name used as the S3 object key
     * @throws IOException if reading file bytes fails
     *
     * Revision flow:
     * 1. Read the original file name from MultipartFile.
     * 2. Build a PutObjectRequest with bucket name, object key, and content type.
     * 3. Convert the uploaded file into bytes.
     * 4. Send the file to S3 using s3Client.putObject().
     */
    public String uploadFile(MultipartFile file) throws IOException {

        // Original file name from the uploaded file.
        // In this code, the same file name is used as the S3 object key.
        String originalFileName = file.getOriginalFilename();

        // PutObjectRequest contains metadata/configuration for uploading an object to S3.
        // bucket -> target S3 bucket
        // key -> unique name of the file inside the bucket
        // contentType -> MIME type such as image/png, application/pdf, etc.
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(originalFileName)
                .contentType(file.getContentType())
                .build();

        // Actual upload operation:
        // - First argument: request metadata
        // - Second argument: file content as bytes
        //
        // RequestBody.fromBytes(...) converts the uploaded file into a format
        // that AWS SDK can send to S3.
        s3Client.putObject(putObjectRequest,
                RequestBody.fromBytes(file.getBytes()));

        // Returning the key helps the caller know under what name
        // the file was stored in S3.
        return originalFileName;
    }

    /**
     * Downloads a file from S3 using its object key.
     *
     * @param key the S3 object key (file name/path in bucket)
     * @return file content as byte array
     *
     * Revision flow:
     * 1. Build a GetObjectRequest using bucket name and key.
     * 2. Fetch object content from S3 as bytes.
     * 3. Convert response into byte[] and return it.
     */
    public byte[] downloadFile(String key){

        // getObjectAsBytes() fetches the file directly as binary data.
        // This is useful when you want to return file content in a controller response.
        ResponseBytes<GetObjectResponse> objectAsBytes = s3Client.getObjectAsBytes(
                GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build()
        );

        // Converts AWS response object into raw byte array.
        // This byte[] can later be sent to browser/client for download.
        return objectAsBytes.asByteArray();
    }

}
