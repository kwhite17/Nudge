package com.ksp.database;

import android.provider.BaseColumns;

public final class MessageReaderContract {

    public MessageReaderContract(){}

    public static abstract class MessageEntry implements BaseColumns{
        public static final String TABLE_NAME = "messages";
        public static final String COLUMN_NAME_RECIPIENT = "recipient";
        public static final String COLUMN_NAME_SEND_TIME = "sendTime";
        public static final String COLUMN_NAME_MESSAGE = "message";
        public static final String COLUMN_NAME_FREQUENCY = "frequency";
    }
}
