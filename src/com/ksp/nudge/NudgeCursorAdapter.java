package com.ksp.nudge;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.ksp.database.NudgeMessagesContract;
import com.ksp.database.NudgeMessagesDbHelper;


public class NudgeCursorAdapter extends SimpleCursorAdapter{
    private LayoutInflater cursorInflater;

    public NudgeCursorAdapter(Context context, int layout, Cursor c, String[] from,
                              int[] to, int flags){
        super(context,layout,c,from,to,flags);
        cursorInflater = LayoutInflater.from(context);
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return cursorInflater.inflate(R.layout.active_nudge_item,parent,false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView recipientView = (TextView) view.findViewById(R.id.nudgeRecipientText);
        TextView messageView = (TextView) view.findViewById(R.id.nudgeMessageText);
        TextView sendDateView = (TextView) view.findViewById(R.id.nudgeSendDateText);
        ImageButton discardButton = (ImageButton) view.findViewById(R.id.nudgeDiscardButton);
        final NudgeMessagesDbHelper databaseHelper = new NudgeMessagesDbHelper(context);
        final String nudgeId = cursor.getString(cursor.getColumnIndex(NudgeMessagesContract.NudgeMessageEntry._ID));

        recipientView.setText("Recipient: ".concat(cursor.getString(cursor.getColumnIndex(NudgeMessagesContract.NudgeMessageEntry.COLUMN_NAME_RECIPIENT_NAME))));
        messageView.setText("Message: ".concat(cursor.getString(cursor.getColumnIndex(NudgeMessagesContract.NudgeMessageEntry.COLUMN_NAME_MESSAGE))));
        sendDateView.setText("Next Send Time: ".concat(cursor.getString(cursor.getColumnIndex(NudgeMessagesContract.NudgeMessageEntry.COLUMN_NAME_SEND_TIME))));
        discardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseHelper.deleteMessage(nudgeId);
                Cursor newCursor = databaseHelper.readMessagesFromDatabase();
                changeCursor(newCursor);
                notifyDataSetChanged();
            }
        });
    }
}