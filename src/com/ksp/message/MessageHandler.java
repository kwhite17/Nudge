package com.ksp.message;

import android.content.Context;

import com.klinker.android.send_message.Message;
import com.klinker.android.send_message.Settings;
import com.klinker.android.send_message.Transaction;

import org.joda.time.Days;
import org.joda.time.Instant;
import org.joda.time.LocalDateTime;
import org.joda.time.Months;
import org.joda.time.Period;
import org.joda.time.Weeks;

public class MessageHandler {
    
    /**
     * 
     * @param dateTime, the current time at which the message is to be sent
     * @param freq, the frequency with which the message is to be sent
     * @return the String representing the next time to send a message
     */
    public static Instant getNextSend(String dateTime, String freq) {
        return getNextSend(Instant.ofEpochMilli(Long.parseLong(dateTime)), freq);
    }

    /**
     * 
     * @param time, the Calendar representation of the current time
     * @return the String representing the next the message is to be sent
     */
    public static Instant getNextSend(Instant time, String freq){

        if (time.isBeforeNow()){
            switch(freq){
            case "Once":
            case "Daily":
                return incrementUntilPresentOrFuture(time, Days.ONE.toPeriod());
            case "Weekly":
                return incrementUntilPresentOrFuture(time, Weeks.ONE.toPeriod());
            case "Monthly":
                return incrementUntilPresentOrFuture(time, Months.ONE.toPeriod());
            case "Monthly: Last Day":
                Instant finalInstant = incrementUntilPresentOrFuture(time, Months.ONE.toPeriod());
                return new LocalDateTime(finalInstant.getMillis()).
                        dayOfMonth().withMaximumValue()
                        .toDateTime()
                        .toInstant();
            }
        }
        return time;
    }

    private static Instant incrementUntilPresentOrFuture(Instant time, Period period) {
        Instant finalTime = time;
        while(finalTime.isBeforeNow()){
            finalTime = finalTime.plus(period.toStandardDuration());
        }
        return finalTime;
    }
    
    /**
     * Sends an SMS/MMS message to the recipient(s)
     * @param numberString, the CSV of recipient phone numbers
     * @param body, the message to send to the recipient(s)
     * @param context
     */
    public static void sendMessage(Context context, String numberString, String body){
        String[] phoneNumbers = numberString.split(",");
        Settings settings = new Settings();
        settings.setUseSystemSending(true);
        if (phoneNumbers.length > 1) {
            settings.setGroup(true);
        }
        Transaction transaction = new Transaction(context, settings);
        for (String number : phoneNumbers) {
            Message message = new Message(body, number);
            transaction.sendNewMessage(message, message.hashCode());
        }
    }

}

