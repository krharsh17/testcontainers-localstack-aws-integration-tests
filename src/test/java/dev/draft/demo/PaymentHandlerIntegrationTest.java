package dev.draft.demo;

import io.awspring.cloud.dynamodb.DynamoDbTemplate;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import junit.framework.TestListener;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.given;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.DYNAMODB;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS;

@Testcontainers
@SpringBootTest
public class PaymentHandlerIntegrationTest {
    private static final String QUEUE_NAME = "payment-queue";

    @Container
    static LocalStackContainer localStack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:2.1.0"));

    @BeforeAll
    static void beforeAll() throws IOException, InterruptedException {
        localStack.execInContainer("awslocal", "dynamodb", "create-table",
                "--table-name", "Payments",
                "--attribute-definitions", "AttributeName=paymentId,AttributeType=S",
                "--key-schema", "AttributeName=paymentId,KeyType=HASH",
                "--provisioned-throughput", "ReadCapacityUnits=5,WriteCapacityUnits=5"
        );
    }

    @DynamicPropertySource
    static void overrideConfiguration(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.aws.sqs.endpoint", () -> localStack.getEndpointOverride(SQS));
        registry.add("spring.cloud.aws.dynamodb.endpoint", () -> localStack.getEndpointOverride(DYNAMODB));
        registry.add("spring.cloud.aws.credentials.access-key", () -> localStack.getAccessKey());
        registry.add("spring.cloud.aws.credentials.secret-key", () -> localStack.getSecretKey());
        registry.add("spring.cloud.aws.region.static", () -> localStack.getRegion());
    }

    @Autowired
    private DynamoDbClient dynamoDbClient;

    @Autowired
    private SqsTemplate sqsTemplate;

    @Test
    public void paymentShouldBeSavedToDBOnceConsumedFromQueue() {
        sqsTemplate.send(QUEUE_NAME, new GenericMessage<>("""
                  {
                     "paymentId": "payment_3566",
                     "payerId": "cust_234",
                     "orderId": "order_809",
                     "paymentAmount": 4.99,
                     "paymentStatus": "successful",
                     "paymentDateTimeISO": "2023-03-22T00:18:26+0000"
                  }
                """, Map.of("contentType", "application/json")));

        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder().dynamoDbClient(dynamoDbClient).build();

        PageIterable<Payment> paymentList = enhancedClient.table("Payments", TableSchema.fromBean(Payment.class)).scan();;

        given()
                .await()
                .atMost(10, SECONDS)
                .ignoreExceptions()
                .untilAsserted(() -> assertThat(paymentList.items().stream()).hasSize(1));

        given()
                .await()
                .atMost(10, SECONDS)
                .ignoreExceptions()
                .untilAsserted(() -> assertThat(paymentList.items().iterator().next().getPaymentId()).isEqualTo("payment_3566"));

    }
}
