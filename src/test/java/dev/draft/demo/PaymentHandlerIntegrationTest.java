package dev.draft.demo;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.given;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.DYNAMODB;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS;

@Testcontainers
@SpringBootTest
public class PaymentHandlerIntegrationTest {
    private static final String QUEUE_NAME = "payment-queue";

    @Container
    static LocalStackContainer localStack =
            new LocalStackContainer(DockerImageName.parse("localstack/localstack:0.13.0"))
                    .withServices(DYNAMODB, SQS);

    @BeforeAll
    static void beforeAll() throws IOException, InterruptedException {
        localStack.execInContainer("awslocal", "sqs", "create-queue", "--queue-name", QUEUE_NAME);
        localStack.execInContainer("awslocal", "dynamodb", "create-table",
                "--table-name", "Payments",
                "--attribute-definitions", "AttributeName=paymentId,AttributeType=S",
                "--key-schema", "AttributeName=paymentId,KeyType=HASH",
                "--provisioned-throughput", "ReadCapacityUnits=5,WriteCapacityUnits=5"
        );
        System.setProperty("cloud.aws.dynamodb.url", localStack.getEndpointOverride(DYNAMODB).toString());

    }

    @DynamicPropertySource
    static void overrideConfiguration(DynamicPropertyRegistry registry) {
        registry.add("payment-handling.payment-queue", () -> QUEUE_NAME);
        registry.add("cloud.aws.sqs.endpoint", () -> localStack.getEndpointOverride(SQS));
        registry.add("cloud.aws.dynamodb.endpoint", () -> localStack.getEndpointOverride(DYNAMODB));
        registry.add("cloud.aws.credentials.access-key", localStack::getAccessKey);
        registry.add("cloud.aws.credentials.secret-key", localStack::getSecretKey);
    }

    @Autowired
    private AmazonDynamoDB amazonDynamoDB;

    @Autowired
    private SqsTemplate queueMessagingTemplate;

    @Test
    public void paymentShouldBeSavedToDBOnceConsumedFromQueue() {
        queueMessagingTemplate.send(QUEUE_NAME, new GenericMessage<>("""
        {
           "paymentId": "payment_3566",
           "payerId": "cust_234",
           "orderId": "order_809",
           "paymentAmount": 4.99,
           "paymentStatus": "successful",
           "paymentDateTimeISO": "2023-03-22T00:18:26+0000"
        }
      """, Map.of("contentType", "application/json")));


        Payment payment = new Payment();
        payment.setPaymentId("payment_3566");

        DynamoDBQueryExpression<Payment> queryExpression = new DynamoDBQueryExpression<Payment>().withHashKeyValues(payment);

        given()
                .ignoreException(AmazonDynamoDBException.class)
                .await()
                .atMost(10, SECONDS)
                .untilAsserted(() -> assertNotNull(new DynamoDBMapper(amazonDynamoDB).query(Payment.class, queryExpression).get(0)));
    }
}
