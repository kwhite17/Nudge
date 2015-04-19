package com.ksp.message;

import java.util.Calendar;

/**
 * Created by kevin on 4/18/15.
 */
public class Message {
    private String recipientInfo;
    private String recipientNumber;
    private String message = "";
    private String frequency = "Weekly";

    public String getRecipientInfo() {
        return recipientInfo;
    }

    public void setRecipientInfo(String recipientInfo) {
        this.recipientInfo = recipientInfo;
    }

    public String getRecipientNumber() { return recipientNumber; }

    public void setRecipientNumber(String recipientNumber) {
        this.recipientNumber = recipientNumber;
    }
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public boolean isFilled() {
        return recipientInfo != null && recipientNumber != null;
    }
}
