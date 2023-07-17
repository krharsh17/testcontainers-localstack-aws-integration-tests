package dev.draft.demo;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import lombok.Data;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@EnableDynamoDBRepositories(basePackages = "dev.draft.demo")
public class DynamoDBConfig {

    @Value("${cloud.aws.region.static}")        private String region;
    @Value("${cloud.aws.dynamodb.url}")         private String dynamoDbEndpointUrl;
    @Value("${cloud.aws.dynamodb.access-key}")  private String accessKey;
    @Value("${cloud.aws.dynamodb.secret-key}")  private String secretKey;

    @Bean
    public AmazonDynamoDB amazonDynamoDB() {
        System.err.println("Initializing Dynamo DB with URL" + dynamoDbEndpointUrl);
        AmazonDynamoDB amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(getEndpointConfiguration(dynamoDbEndpointUrl))
                .withCredentials(getCredentialsProvider())
                .build();

        System.out.println("Tables found: " + amazonDynamoDB.listTables().getTableNames().size());

        System.err.println("Connected to DynamoDB");
        return amazonDynamoDB;
    }

    private EndpointConfiguration getEndpointConfiguration(String url) {
        return new EndpointConfiguration(url, region);
    }

    private AWSStaticCredentialsProvider getCredentialsProvider() {
        return new AWSStaticCredentialsProvider(getBasicAWSCredentials());
    }

    @Bean
    public BasicAWSCredentials getBasicAWSCredentials() {
        return new BasicAWSCredentials(accessKey, secretKey);
    }
}
