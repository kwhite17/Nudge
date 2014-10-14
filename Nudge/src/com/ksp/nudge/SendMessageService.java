package com.ksp.nudge;

import java.text.ParseException;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;

import com.ksp.database.MessageHandler;
import com.ksp.database.NudgeMessagesContract.NudgeMessageEntry;
import com.ksp.database.NudgeMessagesDbHelper;

public class SendMessageService extends Service {
    Thread sendMsgThread;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate(){
        sendMsgThread = new Thread(new Runnable(){

            @Override
            public void run() {
                while (true){
                    try {
                        Thread.sleep(60000);
                        deliverOustandingMessages();
                    } catch (InterruptedException | ParseException e) {
                        e.printStackTrace();
                    }
                }
            }

        });
        sendMsgThread.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        return START_STICKY;
    }

    public void deliverOustandingMessages() throws ParseException{
        NudgeMessagesDbHelper dbHelper = new NudgeMessagesDbHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {
                NudgeMessageEntry._ID,
                NudgeMessageEntry.COLUMN_NAME_RECIPIENT_NUMBER,
                NudgeMessageEntry.COLUMN_NAME_SEND_TIME,
                NudgeMessageEntry.COLUMN_NAME_MESSAGE,
                NudgeMessageEntry.COLUMN_NAME_FREQUENCY,
        };
        String sortOrder = NudgeMessageEntry.COLUMN_NAME_RECIPIENT_NUMBER + " DESC";
        Cursor msgCursor = db.query(NudgeMessageEntry.TABLE_NAME, projection, null, null, null, null, sortOrder);

        msgCursor.moveToFirst();
        while (!msgCursor.isAfterLast()){
            String id = msgCursor.getString(msgCursor.getColumnIndex(NudgeMessageEntry._ID));
            String recipient = msgCursor.getString(msgCursor.getColumnIndex(NudgeMessageEntry.COLUMN_NAME_RECIPIENT_NUMBER));
            String message = msgCursor.getString(msgCursor.getColumnIndex(NudgeMessageEntry.COLUMN_NAME_MESSAGE));
            String sendDate = msgCursor.getString(msgCursor.getColumnIndex(NudgeMessageEntry.COLUMN_NAME_SEND_TIME));
            String frequency = msgCursor.getString(msgCursor.getColumnIndex(NudgeMessageEntry.COLUMN_NAME_FREQUENCY));

            if (MessageHandler.isOutstandingMessage(sendDate)){
                MessageHandler.sendMessage(recipient, message);
                if (frequency.equals("Once")){
                    MessageHandler.deleteMessage(id, dbHelper);
                } else{
                    MessageHandler.updateSendTime(id, sendDate, frequency, dbHelper);
                }
            }

            msgCursor.moveToNext();
        }
        msgCursor.close();

    }

}
