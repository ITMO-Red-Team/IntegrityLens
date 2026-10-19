package org.integritylens.api;

import java.net.URI;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Component
public class ObjectStorage implements AutoCloseable {
    private final S3Client client;
    private final String bucket;

    public ObjectStorage(@Value("${integritylens.s3.endpoint}") String endpoint,
            @Value("${integritylens.s3.access-key}") String accessKey,
            @Value("${integritylens.s3.secret-key}") String secretKey,
            @Value("${integritylens.s3.bucket}") String bucket) {
        this.bucket = bucket;
        this.client = S3Client.builder().endpointOverride(URI.create(endpoint)).region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .forcePathStyle(true).httpClientBuilder(UrlConnectionHttpClient.builder()
                        .connectionTimeout(Duration.ofSeconds(5)).socketTimeout(Duration.ofSeconds(30)))
                .overrideConfiguration(ClientOverrideConfiguration.builder()
                        .apiCallTimeout(Duration.ofSeconds(60)).build()).build();
    }

    void put(String key, byte[] bytes, String contentType) {
        client.putObject(r -> r.bucket(bucket).key(key).contentType(contentType), RequestBody.fromBytes(bytes));
    }

    byte[] get(String key) {
        return client.getObjectAsBytes(r -> r.bucket(bucket).key(key)).asByteArray();
    }

    void delete(String key) {
        client.deleteObject(r -> r.bucket(bucket).key(key));
    }

    void check() {
        client.headBucket(r -> r.bucket(bucket));
    }

    @Override
    public void close() {
        client.close();
    }
}
