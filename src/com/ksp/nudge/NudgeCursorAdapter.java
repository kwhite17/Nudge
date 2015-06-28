package com.ksp.nudge;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.ksp.database.NudgeDatabaseHelper;

import static android.provider.BaseColumns._ID;
import static com.ksp.database.NudgeMessagesContract.NudgeMessageEntry.COLUMN_NAME_MESSAGE;
import static com.ksp.database.NudgeMessagesContract.NudgeMessageEntry.COLUMN_NAME_RECIPIENT_NAME;
import static com.ksp.database.NudgeMessagesContract.NudgeMessageEntry.COLUMN_NAME_SEND_TIME;
import static com.ksp.nudge.R.id.deleteNudgeButton;
import static com.ksp.nudge.R.id.editNudgeButton;
import static com.ksp.nudge.R.id.nudgeMessageText;
import static com.ksp.nudge.R.id.nudgeRecipientText;
import static com.ksp.nudge.R.id.nudgeSendDateText;


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
        TextView recipientView = (TextView) view.findViewById(nudgeRecipientText);
        TextView messageView = (TextView) view.findViewById(nudgeMessageText);
        TextView sendDateView = (TextView) view.findViewById(nudgeSendDateText);
        CardView discardButton = (CardView) view.findViewById(deleteNudgeButton);
        CardView editButton = (CardView) view.findViewById(editNudgeButton);
        final NudgeDatabaseHelper databaseHelper = new NudgeDatabaseHelper(context);
        final String nudgeId = cursor.getString(cursor.getColumnIndex(_ID));
        final Context activityContext = context;
        recipientView.setText(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_RECIPIENT_NAME)));
        messageView.setText(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_MESSAGE)));
        sendDateView.setText(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_SEND_TIME)));
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activityContext, MessageFormActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("EDIT_NUDGE_ID",nudgeId);
                activityContext.startActivity(intent);
            }
        });
        discardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseHelper.deleteMessage(nudgeId);
                refreshAdapter(databaseHelper);
            }
        });
    }

    /**
     * Updates the data in the adapter and notifies the necessary objects
     * @param databaseHelper, the helper used to read the new data form the database
     */
    public void refreshAdapter(NudgeDatabaseHelper databaseHelper){
        getCursor().close();
        Cursor newCursor = databaseHelper.readMessagesFromDatabase();
        changeCursor(newCursor);
        notifyDataSetChanged();
    }
}