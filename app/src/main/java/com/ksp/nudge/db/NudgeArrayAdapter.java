package com.ksp.nudge.db;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ksp.nudge.R;
import com.ksp.nudge.activity.MessageFormActivity;
import com.ksp.nudge.model.Nudge;
import com.ksp.nudge.model.Recipient;

import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.LocalDateTime;

import java.text.DateFormat;
import java.util.List;
import java.util.Locale;

import androidx.cardview.widget.CardView;


public class NudgeArrayAdapter extends ArrayAdapter<Nudge> {

    public NudgeArrayAdapter(Context context, List<Nudge> nudges){
        super(context, 0, nudges);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Nudge nudge = getItem(position);
        View adaptedView = convertView;
        if (adaptedView == null) {
            adaptedView = LayoutInflater.from(getContext()).inflate(R.layout.active_nudge_item,
                    parent, false);
        }
        TextView recipientView = adaptedView.findViewById(R.id.nudgeRecipientText);
        TextView messageView = adaptedView.findViewById(R.id.nudgeMessageText);
        TextView sendDateView = adaptedView.findViewById(R.id.nudgeSendDateText);
        CardView discardButton = adaptedView.findViewById(R.id.deleteNudgeButton);
        CardView editButton = adaptedView.findViewById(R.id.editNudgeButton);

        messageView.setText(nudge.getNudgeConfig().getMessage());
        sendDateView.setText(getSendDateString(nudge.getNudgeConfig().getSendTime()));
        recipientView.setText(buildContactString(nudge.getRecipients()));

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), MessageFormActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("EDIT_NUDGE_ID", nudge.getNudgeConfig().getId());
                getContext().startActivity(intent);
            }
        });
        discardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NudgeDatabaseHelper.deleteNudge(nudge);
                remove(nudge);
                notifyDataSetChanged();
            }
        });
        return adaptedView;
    }

    private String getSendDateString(Instant sendTime) {
        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT,
                Locale.getDefault()).format(new LocalDateTime(sendTime.getMillis(),
                DateTimeZone.getDefault()).toDate());
    }

    private String buildContactString(List<Recipient> recipients) {
        if (recipients.size() > 2) {
            return recipients.get(0).getName() + " and " +
                    Integer.toString(recipients.size() - 1) + " others";
        } else if (recipients.size() == 2) {
            return recipients.get(0).getName() + " and " + 1 + " other";
        } else {
            return recipients.get(0).getName();
        }
    }
}