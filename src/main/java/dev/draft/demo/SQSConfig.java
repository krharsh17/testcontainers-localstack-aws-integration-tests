package dev.draft.demo;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import java.net.URI;

@Configuration
public class SQSConfig {

    @Value("${cloud.aws.region.static}")        private String region;
    @Value("${cloud.aws.dynamodb.url}")         private String sqsEndpointUrl;
    @Value("${cloud.aws.dynamodb.access-key}")  private String accessKey;
    @Value("${cloud.aws.dynamodb.secret-key}")  private String secretKey;

    @Bean
    public SqsAsyncClient sqsAsyncClient() {
        return SqsAsyncClient.builder()
                .region(Region.of(region))
                .endpointOverride(URI.create(sqsEndpointUrl))
                .credentialsProvider(this::getAwsBasicCredentials)
                .build();
    }

    @Bean
    public SqsTemplate queueMessagingTemplate(SqsAsyncClient amazonSQS) {
        return SqsTemplate.newTemplate(amazonSQS);
    }

    @Bean
    public AwsBasicCredentials getAwsBasicCredentials() {
        return AwsBasicCredentials.create(accessKey, secretKey);
    }


}
