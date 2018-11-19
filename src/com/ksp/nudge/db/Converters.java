package com.ksp.nudge.db;

import android.arch.persistence.room.TypeConverter;

import org.joda.time.Instant;

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
}
