package com.ksp.nudge.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * Created by kevin on 9/24/2018.
 */

@Entity(tableName = "recipients", foreignKeys = @ForeignKey(entity = NudgeConfig.class,
        parentColumns = "id",
        childColumns = "nudgeId",
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
))
public class Recipient {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    private String phoneNumber;

    private String name;

    private long nudgeId;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(@NonNull String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getNudgeId() {
        return nudgeId;
    }

    public void setNudgeId(long nudgeId) {
        this.nudgeId = nudgeId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
