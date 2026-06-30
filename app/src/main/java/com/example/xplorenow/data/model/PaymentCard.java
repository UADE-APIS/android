package com.example.xplorenow.data.model;

public class PaymentCard {

    private final String holderName;
    private final String cardNumber;
    private final String expiryDate;
    private final String cvv;

    public PaymentCard(String holderName, String cardNumber, String expiryDate, String cvv) {
        this.holderName = holderName;
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
        this.cvv = cvv;
    }

    public String getHolderName() {
        return holderName;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public String getCvv() {
        return cvv;
    }
}
