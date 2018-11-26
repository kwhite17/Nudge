package com.ksp.nudge.model;

import com.google.auto.value.AutoValue;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Created by kevin on 9/24/2018.
 */

@AutoValue
@Entity(tableName = "recipients",
    indices = @Index("nudgeId"),
    foreignKeys = @ForeignKey(entity = NudgeConfig.class,
        parentColumns = "id",
        childColumns = "nudgeId",
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
))
public abstract class Recipient {
    @AutoValue.CopyAnnotations
    @PrimaryKey(autoGenerate = true)
    @Nullable
    public abstract Long getId();
    public abstract String getPhoneNumber();
    public abstract String getName();
    public abstract long getNudgeId();

    public static Recipient create(long id, String phoneNumber, String name, long nudgeId) {
        return new AutoValue_Recipient.Builder()
                .setId(id)
                .setPhoneNumber(phoneNumber)
                .setName(name)
                .setNudgeId(nudgeId)
                .build();
    }

    abstract Builder toBuilder();

    public static Builder builder() {
        return new AutoValue_Recipient.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setId(Long id);

        public abstract Builder setPhoneNumber(String phoneNumber);

        public abstract Builder setName(String name);

        public abstract Builder setNudgeId(long nudgeId);

        public abstract Recipient build();
    }

    public Recipient withId(Long id) {
        return toBuilder().setId(id).build();
    }

}
