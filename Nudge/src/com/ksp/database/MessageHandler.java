package com.ksp.database;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.telephony.SmsManager;

import com.ksp.database.MessageReaderContract.MessageEntry;

public class MessageHandler {

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

    public static void sendMessage(String phoneNumber,String message){
        SmsManager textManager = SmsManager.getDefault();

        textManager.sendTextMessage(phoneNumber, null, message.concat(" - sent by Nudge"), null, null);
    }

    public static String formatMessage(String phoneNumber,String message, String sendTime){
        String recipient = "Recipient: " + phoneNumber + "\n";
        String msg = "Message: " + message + "\n";
        String nextSend = "Next Send: " + sendTime + "\n";

        return recipient + msg + nextSend;
    }

    public static void deleteMessage(String id, MessageReaderDbHelper databaseHelper){
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        String selection = MessageEntry._ID + " LIKE ?";
        String[] selectionArgs = { id };

        database.delete(MessageEntry.TABLE_NAME, selection, selectionArgs);
    }

    public static void updateSendTime(String id, String sendDate, String frequency, MessageReaderDbHelper dbHelper){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String selection = MessageEntry._ID + " LIKE ?";
        String[] selectionArgs = { id };
        ContentValues sendTime = new ContentValues();

        sendTime.put(MessageEntry.COLUMN_NAME_SEND_TIME, MessageHandler.getNextSend(sendDate, frequency));
        db.update(MessageEntry.TABLE_NAME, sendTime, selection, selectionArgs);
    }

    public static boolean isOutstandingMessage(String sendDate) throws ParseException {
        return DateFormat.getInstance().parse(sendDate).compareTo(new Date()) <= 0;
    }
}

