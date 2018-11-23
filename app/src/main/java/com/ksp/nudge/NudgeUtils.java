package com.ksp.nudge;

import android.content.Context;

import com.klinker.android.send_message.Message;
import com.klinker.android.send_message.Settings;
import com.klinker.android.send_message.Transaction;
import com.ksp.nudge.model.NudgeFrequency;
import com.ksp.nudge.model.Recipient;

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.Days;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.joda.time.LocalDateTime;
import org.joda.time.MonthDay;
import org.joda.time.Months;
import org.joda.time.Period;
import org.joda.time.Weeks;

import java.util.List;

public class NudgeUtils {

    private static final int NEXT_MONTH_OFFSET = 5;

    /**
     * @param dateTime, the current time at which the message is to be sent
     * @param freq,     the frequency with which the message is to be sent
     * @return the String representing the next time to send a message
     */
    public static Instant getNextSend(String dateTime, NudgeFrequency freq) {
        return getNextSend(Instant.ofEpochMilli(Long.parseLong(dateTime)), freq);
    }

    /**
     * @param time, the Calendar representation of the current time
     * @return the String representing the next the message is to be sent
     */
    public static Instant getNextSend(Instant time, NudgeFrequency frequency) {
        if (time.isBeforeNow()) {
            switch (frequency) {
                case ONCE:
                case DAILY:
                    return time.plus(Days.ONE.toStandardDuration());
                case WEEKLY:
                    return time.plus(Weeks.ONE.toStandardDuration());
                case MONTHLY:
                    return time.toDateTime()
                            .plusMonths(1)
                            .toInstant();
                case END_OF_MONTH:
                    return time.toDateTime()
                            .plusDays(NEXT_MONTH_OFFSET)
                            .dayOfMonth()
                            .withMaximumValue()
                            .toInstant();
            }
        } else if (frequency == NudgeFrequency.END_OF_MONTH) {
            return time.toDateTime()
                    .dayOfMonth()
                    .withMaximumValue()
                    .toInstant();
        }
        return time;
    }

    /**
     * Sends an SMS/MMS message to the recipient(s)
     *
     * @param recipients, the list of recipients to message
     * @param body,       the message to send to the recipient(s)
     * @param context
     */
    public static void sendMessage(Context context, List<Recipient> recipients, String body) {
        Settings settings = new Settings();
        settings.setUseSystemSending(true);
        if (recipients.size() > 1) {
            settings.setGroup(true);
        }
        Transaction transaction = new Transaction(context, settings);
        for (Recipient recipient : recipients) {
            Message message = new Message(body, recipient.getPhoneNumber());
            transaction.sendNewMessage(message, message.hashCode());
        }
    }

}

