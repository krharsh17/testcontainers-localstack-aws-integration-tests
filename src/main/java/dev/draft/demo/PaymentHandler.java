package dev.draft.demo;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import io.awspring.cloud.messaging.listener.annotation.SqsListener;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class PaymentHandler {
    private static final Logger LOG = LoggerFactory.getLogger(PaymentHandler.class);

    private final AmazonDynamoDB amazonDynamoDB;
    public PaymentHandler(AmazonDynamoDB amazonDynamoDB) {
        this.amazonDynamoDB = amazonDynamoDB;
    }

    @SqsListener(value = "${payment-handling.payment-queue}")
    public void handlePayment(@Payload Payment payment) {
        LOG.info("Payment details received: " + payment.toString());

        DynamoDBMapper mapper = new DynamoDBMapper(amazonDynamoDB);

        mapper.save(payment);

        LOG.info("Payment details saved in table");
    }
}
