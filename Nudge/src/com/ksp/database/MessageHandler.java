package com.ksp.database;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import android.telephony.SmsManager;

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

        if (time.before(Calendar.getInstance())){
            switch(freq){
            case "Once":
                time.add(Calendar.DATE, 1);
                break;
            case "Daily":
                time.add(Calendar.DATE, 1);
                break;
            case "Weekly":
                time.add(Calendar.DATE, 7);
                break;
            case "Monthly":
                time.add(Calendar.MONTH, 1);
                break;
            }
        }
        return DateFormat.getInstance().format(time.getTime());
    }
    
    /**
     * 
     * @param hour, the hour at which the message is to be sent
     * @param min, the minute at which the message is to be sent
     * @param freq, how often the message is to be sent
     * @return the String representing the next the message is to be sent
     */
    public static String getNextSend(int hour, int min, String freq){
        Calendar time = Calendar.getInstance();

        time.set(Calendar.HOUR_OF_DAY, hour);
        time.set(Calendar.MINUTE,min);

        if (time.before(Calendar.getInstance())){
            switch(freq){
            case "Once":
                time.add(Calendar.DATE, 1);
                break;
            case "Daily":
                time.add(Calendar.DATE, 1);
                break;
            case "Weekly":
                time.add(Calendar.DATE, 7);
                break;
            case "Monthly":
                time.add(Calendar.MONTH, 1);
                break;
            }
        }
        return DateFormat.getInstance().format(time.getTime());
    }
    
    /**
     * Sends an SMS message to the recipient
     * @param phoneNumber, the repicient's phone number
     * @param message, the message to send to the recipient
     */
    public static void sendMessage(String phoneNumber,String message){
        SmsManager textManager = SmsManager.getDefault();

        textManager.sendTextMessage(phoneNumber, null, message.concat(" - sent by Nudge"), null, null);
    }
    
    /**
     * 
     * @param phoneNumber, the message recipient's phone number
     * @param message, the message to be sent to the recipient
     * @param sendTime, the time at which the message is to be sent
     * @return a String representing the essential information about the message
     */
    public static String formatMessage(String phoneNumber,String message, String sendTime){
        String recipient = "Recipient: " + phoneNumber + "\n";
        String msg = "Message: " + message + "\n";
        String nextSend = "Next Send: " + sendTime + "\n";

        return recipient + msg + nextSend;
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

