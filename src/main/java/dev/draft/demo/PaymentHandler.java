package dev.draft.demo;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.messaging.listener.annotation.SqsListener;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Payload;

public class PaymentHandler {
    private static final Logger LOG = LoggerFactory.getLogger(PaymentHandler.class);

    private final AmazonDynamoDB amazonDynamoDB;
    private final ObjectMapper objectMapper;
    private final String paymentTable;

    public PaymentHandler(
            @Value("${payment-handling.payment-table}") String paymentTable,
            AmazonDynamoDB amazonDynamoDB,
            ObjectMapper objectMapper
    ) {
        this.amazonDynamoDB = amazonDynamoDB;
        this.objectMapper = objectMapper;
        this.paymentTable = paymentTable;
    }

    @SqsListener(value = "${payment-handling.payment-queue}")
    public void handlePayment(@Payload Payment payment) throws JsonProcessingException {
        LOG.info("Payment details received: " + payment.toString());

        DynamoDBMapper mapper = new DynamoDBMapper(amazonDynamoDB);

        mapper.save(payment);

        LOG.info("Payment details saved in table");
    }
}
