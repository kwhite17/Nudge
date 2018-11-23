package com.ksp.nudge.db;

import com.ksp.nudge.model.NudgeFrequency;

import org.joda.time.Instant;

import androidx.room.TypeConverter;

/**
 * Created by kevin on 9/24/2018.
 */

public class Converters {
    @TypeConverter
    public static Instant fromTimestamp(Long timestamp) {
        return timestamp == null ? null : Instant.ofEpochMilli(timestamp);
    }

    @TypeConverter
    public static Long toEpochMillis(Instant instant) {
        return instant == null ? null : instant.getMillis();
    }

    @TypeConverter
    public static String toString(NudgeFrequency frequency) {
        return frequency.getDisplayText();
    }

    @TypeConverter
    public static NudgeFrequency fromDisplayText(String displayText) {
        return NudgeFrequency.fromDisplayText(displayText);
    }
}
