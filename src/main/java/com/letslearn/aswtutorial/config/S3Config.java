package com.letslearn.aswtutorial.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/*
 * Revision note:
 * This class is responsible for creating and configuring the AWS S3 client bean.
 *
 * Why this class is needed:
 * - Spring Boot does not automatically know how you want to configure the AWS S3 client.
 * - So we create the S3Client manually as a Spring Bean.
 *
 * Main idea:
 * - In LOCAL profile -> use access key + secret key from application properties
 * - In DEV profile   -> use DefaultCredentialsProvider from AWS SDK
 *
 * Important interview/revision point:
 * @Configuration tells Spring that this class contains bean definitions.
 */
@Configuration
public class S3Config {

    /*
     * Reads the AWS region from application.properties / application.yml
     *
     * Example:
     * cloud.aws.region.status=ap-south-1
     *
     * This region is used while building the S3 client.
     */
    @Value("${cloud.aws.region.status}")
    private String region;

    /*
     * Creates an S3Client bean only when the "local" profile is active.
     *
     * Why local profile uses StaticCredentialsProvider:
     * - In local development, we often keep credentials in properties for testing.
     * - These credentials are explicitly passed to the AWS client.
     *
     * Bean name:
     * - "s3Client"
     * - This name can be useful when multiple beans of the same type exist.
     *
     * Revision point:
     * @Profile("local") means this bean is created only if spring.profiles.active=local
     */
    @Bean("s3Client")
    @Profile("local")
    public S3Client s3Client(
            @Value("${cloud.aws.credential.access-key}") String accessKey,
            @Value("${cloud.aws.credential.secret-key}") String secretKey) {

        /*
         * AwsBasicCredentials holds the raw AWS access key and secret key.
         *
         * Note:
         * - This is fine for local/demo setups.
         * - For production, hardcoding or storing credentials in properties is not recommended.
         */
        AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(accessKey, secretKey);

        /*
         * Build and return the S3 client.
         *
         * Steps:
         * 1. builder() -> start S3 client configuration
         * 2. region(...) -> tell AWS which region to connect to
         * 3. credentialsProvider(...) -> provide credentials explicitly using static provider
         * 4. build() -> create the final S3Client object
         */
        return S3Client
                .builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
                .build();
    }

    /*
     * Creates an S3Client bean only when the "dev" profile is active.
     *
     * Why DefaultCredentialsProvider is used here:
     * - It is the recommended AWS SDK approach for non-local environments.
     * - It automatically checks multiple credential sources in order.
     *
     * Common credential lookup sources include:
     * - Environment variables
     * - AWS credentials file
     * - IAM role attached to EC2 / ECS / Lambda
     *
     * Revision point:
     * This avoids storing secrets directly in the application code or properties.
     */
    @Bean("s3Client")
    @Profile("dev")
    public S3Client s3ClientDev() {

        /*
         * Here AWS SDK automatically resolves credentials using DefaultCredentialsProvider.
         *
         * Good for:
         * - EC2 instances with IAM roles
         * - ECS tasks
         * - AWS Lambda
         * - Developer machines configured with AWS CLI
         */
        return S3Client
                .builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}
