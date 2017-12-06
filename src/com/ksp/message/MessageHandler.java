package com.ksp.message;

import android.content.Context;
import android.util.Log;

import com.klinker.android.send_message.Message;
import com.klinker.android.send_message.Settings;
import com.klinker.android.send_message.Transaction;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import static java.util.Calendar.DATE;
import static java.util.Calendar.MONTH;

public class MessageHandler {
    
    /**
     * 
     * @param dateTime, the current time at which the message is to be sent
     * @param freq, the frequency with which the message is to be sent
     * @return the String representing the next time to send a message
     */
    public static String getNextSend(String dateTime, String freq) {
        Calendar time = Calendar.getInstance();
        try {
            time.setTime(DateFormat.getInstance().parse(dateTime));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return getNextSend(time, freq);
    }

    /**
     * 
     * @param time, the Calendar representation of the current time
     * @return the String representing the next the message is to be sent
     */
    public static String getNextSend(Calendar time, String freq){

        if (time.before(Calendar.getInstance())){
            switch(freq){
            case "Once":
                incrementUntilPresentOrFuture(time, DATE, 1);
                break;
            case "Daily":
                incrementUntilPresentOrFuture(time, DATE, 1);
                break;
            case "Weekly":
                incrementUntilPresentOrFuture(time, DATE, 7);
                break;
            case "Monthly":
                incrementUntilPresentOrFuture(time, MONTH, 1);
                break;
            case "Monthly: Last Day":
                incrementUntilPresentOrFuture(time, MONTH, 1);
                time.set(DATE, time.getActualMaximum(DATE));
                break;
            }
        }
        return DateFormat.getInstance().format(time.getTime());
    }

    private static void incrementUntilPresentOrFuture(Calendar time, int period, int interval) {
        Calendar presentTime = Calendar.getInstance();
        while(time.before(presentTime)){
            time.add(period, interval);
        }
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
    
    /**
     * 
     * @param sendDate, the time the NudgeInfo is to be sent
     * @return a boolean indicating if it is time for the message to be sent
     * @throws ParseException
     */
    public static boolean isOutstandingMessage(String sendDate) throws ParseException {
        return DateFormat.getInstance().parse(sendDate).compareTo(new Date()) <= 0;
    }
}

