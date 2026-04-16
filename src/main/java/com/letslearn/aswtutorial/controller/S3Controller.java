package com.letslearn.aswtutorial.controller;

import com.letslearn.aswtutorial.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * This is a REST controller for handling Amazon S3-related operations.
 *
 * Revision note:
 * - @RestController = marks this class as a Spring REST API controller.
 * - @RequestMapping("/v1") = all endpoints in this class will start with /v1.
 * - This controller delegates actual S3 logic to the S3Service class.
 */
@RestController
@RequestMapping("/v1")
public class S3Controller {

    /**
     * S3Service is injected by Spring IoC container.
     *
     * Revision note:
     * - @Autowired tells Spring to provide the required bean automatically.
     * - Controller should only handle HTTP request/response logic.
     * - Business logic such as upload/download should stay in the service layer.
     */
    @Autowired
    private S3Service s3Service;

    /**
     * Uploads a file to Amazon S3.
     *
     * Why this API exists:
     * - It accepts a file from the client.
     * - Passes that file to the service layer.
     * - Returns a success response after upload completes.
     *
     * Revision note:
     * - MultipartFile is used in Spring Boot to receive uploaded files.
     * - file is taken from request parameter, usually sent as form-data.
     * - IOException may occur while reading file content before sending to S3.
     *
     * Important:
     * - For file upload, POST is more appropriate than GET.
     */
    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) throws IOException {

        // Delegate the actual upload operation to the service layer.
        s3Service.uploadFile(file);

        // Return HTTP 200 OK with a simple success message.
        return ResponseEntity.ok("File uploaded successfully");
    }

    /**
     * Downloads a file from Amazon S3 using its file name.
     *
     * Flow:
     * - fileName is received from the URL path.
     * - Service layer fetches file content from S3 as byte[].
     * - Controller sends those bytes back in the HTTP response.
     *
     * Revision note:
     * - @PathVariable binds the value from URL to method parameter.
     * - ResponseEntity<byte[]> is used when returning binary/file data.
     * - CONTENT_DISPOSITION header tells browser to treat response as attachment.
     */
    @GetMapping("/download/{fileName}")
    public ResponseEntity<byte[]> download(@PathVariable String fileName) {

        // Fetch file data from S3 using the provided file name.
        byte[] data = s3Service.downloadFile(fileName);

        // Build the HTTP response for file download.
        return ResponseEntity.ok()
                // This header tells the browser to download the file instead of displaying it.
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(data);
    }
}
