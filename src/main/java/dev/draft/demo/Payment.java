package dev.draft.demo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class Payment {
    private String paymentId;
    private String payerId;
    private String orderId;
    private float paymentAmount;
    private String paymentStatus;
    private String paymentDateTimeISO;

    public Payment() {

    }

    @JsonCreator
    public Payment(String json) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Payment payment = objectMapper.readValue(json, Payment.class);
        this.paymentId = payment.paymentId;
        this.payerId = payment.payerId;
        this.orderId = payment.orderId;
        this.paymentAmount = payment.paymentAmount;
        this.paymentStatus = payment.paymentStatus;
        this.paymentDateTimeISO = payment.paymentDateTimeISO;
    }

    @DynamoDbPartitionKey
    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    @DynamoDbAttribute("payerId")
    public String getPayerId() {
        return payerId;
    }

    public void setPayerId(String payerId) {
        this.payerId = payerId;
    }

    @DynamoDbAttribute("orderId")
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @DynamoDbAttribute("paymentAmount")
    public float getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(float paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    @DynamoDbAttribute("paymentStatus")
    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    @DynamoDbAttribute("paymentDateTimeISO")
    public String getPaymentDateTimeISO() {
        return paymentDateTimeISO;
    }

    public void setPaymentDateTimeISO(String paymentDateTimeISO) {
        this.paymentDateTimeISO = paymentDateTimeISO;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "paymentId='" + paymentId + '\'' +
                ", payerId='" + payerId + '\'' +
                ", orderId='" + orderId + '\'' +
                ", paymentAmount=" + paymentAmount +
                ", paymentStatus='" + paymentStatus + '\'' +
                ", paymentDateTimeISO='" + paymentDateTimeISO + '\'' +
                '}';
    }
}
