package com.ksp.nudge;

import android.app.Application;

import com.ksp.nudge.db.NudgeDatabase;

import androidx.room.Room;

/**
 * Created by kevin on 9/24/2018.
 */

public class NudgeApp extends Application {

    private static NudgeApp INSTANCE;

    private NudgeDatabase database;

    public static NudgeApp get() {
        return INSTANCE;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        database = Room.databaseBuilder(getApplicationContext(), NudgeDatabase.class,
                "NudgeDb")
                .allowMainThreadQueries()
                .build();
        INSTANCE = this;
    }

    public NudgeDatabase getDatabase() {
        return database;
    }
}
