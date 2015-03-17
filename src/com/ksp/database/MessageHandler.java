package com.ksp.database;

import android.content.Context;
import android.telephony.SmsManager;

import com.ksp.nudge.SendMessageService;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class MessageHandler {
    
    /**
     * 
     * @param messageContext the context in which we are calculating the next send time
     * @param dateTime, the current time at which the message is to be sent
     * @param freq, the frequency with which the message is to be sent
     * @return the String representing the next time to send a message
     */
    public static String getNextSend(String dateTime, String freq, Context messageContext) {
        Calendar time = Calendar.getInstance();
        try {
            time.setTime(DateFormat.getInstance().parse(dateTime));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return getNextSend(time,freq,messageContext);
    }

    /**
     * 
     * @param time, the Calendar representation of the current time
     * @param messageContext the context in which we are calculating the next send time
     * @return the String representing the next the message is to be sent
     */
    public static String getNextSend(Calendar time, String freq, Context messageContext){

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
        SendMessageService.setServiceAlarm(messageContext, time);
        return DateFormat.getInstance().format(time.getTime());
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

