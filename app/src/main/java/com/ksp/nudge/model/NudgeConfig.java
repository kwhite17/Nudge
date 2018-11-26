package com.ksp.nudge.model;

import com.google.auto.value.AutoValue;

import org.joda.time.Instant;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Created by kevin on 9/24/2018.
 */

@AutoValue
@Entity(tableName = "nudges")
public abstract class NudgeConfig {
    @AutoValue.CopyAnnotations
    @PrimaryKey(autoGenerate = true)
    @Nullable
    public abstract Long getId();

    public abstract String getMessage();

    public abstract NudgeFrequency getFrequency();

    public abstract Instant getSendTime();


    public static NudgeConfig create(Long id, String message, NudgeFrequency frequency, Instant
        sendTime) {
        return new AutoValue_NudgeConfig.Builder()
                .setId(id)
                .setMessage(message)
                .setFrequency(frequency)
                .setSendTime(sendTime)
                .build();
    }

    public abstract Builder toBuilder();

    public static Builder builder() {
        return new AutoValue_NudgeConfig.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setId(Long id);

        public abstract Builder setMessage(String message);

        public abstract Builder setFrequency(NudgeFrequency frequency);

        public abstract Builder setSendTime(Instant sendTime);

        public abstract NudgeConfig build();
    }

    public NudgeConfig withMessage(String message) {
        return toBuilder().setMessage(message).build();
    }

    public NudgeConfig withFrequency(NudgeFrequency frequency) {
        return toBuilder().setFrequency(frequency).build();
    }

    public NudgeConfig withSendTime(Instant sendTime) {
        return toBuilder().setSendTime(sendTime).build();
    }
    public NudgeConfig withId(Long id) {
        return toBuilder().setId(id).build();
    }
}
