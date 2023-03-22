package dev.draft.demo;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName="Payments")
public class Payment {
    private String paymentId;
    private String payerId;
    private String orderId;
    private float paymentAmount;
    private String paymentStatus;
    private String paymentDateTimeISO;


    @DynamoDBHashKey(attributeName="paymentId")
    public String getPaymentId() {
        return paymentId;
    }
    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    @DynamoDBAttribute(attributeName="payerId")
    public String getPayerId() {
        return payerId;
    }
    public void setPayerId(String payerId) {
        this.payerId = payerId;
    }

    @DynamoDBAttribute(attributeName="orderId")
    public String getOrderId() {
        return orderId;
    }
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @DynamoDBAttribute(attributeName="paymentAmount")
    public float getPaymentAmount() {
        return paymentAmount;
    }
    public void setPaymentAmount(float paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    @DynamoDBAttribute(attributeName="paymentStatus")
    public String getPaymentStatus() {
        return paymentStatus;
    }
    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    @DynamoDBAttribute(attributeName="paymentDateTimeISO")
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
