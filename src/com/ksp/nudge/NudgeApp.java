package com.ksp.nudge;

import android.app.Application;
import android.arch.persistence.room.Room;

import com.ksp.nudge.db.NudgeDatabase;

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
