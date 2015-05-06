package com.ksp.message;

import android.telephony.SmsManager;

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
     * Sends an SMS message to the recipient
     * @param phoneNumber, the repicient's phone number
     * @param message, the message to send to the recipient
     */
    public static void sendMessage(String phoneNumber,String message){
        SmsManager textManager = SmsManager.getDefault();
        textManager.sendTextMessage(phoneNumber, null, message, null, null);
    }
    
    /**
     * 
     * @param sendDate, the time the Message is to be sent
     * @return a boolean indicating if it is time for the message to be sent
     * @throws ParseException
     */
    public static boolean isOutstandingMessage(String sendDate) throws ParseException {
        return DateFormat.getInstance().parse(sendDate).compareTo(new Date()) <= 0;
    }
}

