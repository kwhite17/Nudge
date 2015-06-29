package com.ksp.database;

import android.provider.BaseColumns;

public final class NudgeMessagesContract {

    public NudgeMessagesContract(){}

    public static abstract class NudgeMessageEntry implements BaseColumns{
        public static final String TABLE_NAME = "messages";
        public static final String COLUMN_NAME_RECIPIENT_NAME = "recipientName";
        public static final String COLUMN_NAME_RECIPIENT_NUMBER = "recipientNumber";
        public static final String COLUMN_NAME_SEND_TIME = "sendTime";
        public static final String COLUMN_NAME_MESSAGE = "message";
        public static final String COLUMN_NAME_FREQUENCY = "frequency";
    }
}
