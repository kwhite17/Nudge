package com.ksp.nudge.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import com.ksp.nudge.db.dao.NudgeDao;
import com.ksp.nudge.db.dao.RecipientDao;
import com.ksp.nudge.model.Nudge;
import com.ksp.nudge.model.NudgeConfig;
import com.ksp.nudge.model.Recipient;

/**
 * Created by kevin on 9/24/2018.
 */

@Database(entities = {NudgeConfig.class, Recipient.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class NudgeDatabase extends RoomDatabase {
    public abstract NudgeDao nudgeDao();
    public abstract RecipientDao recipientDao();
}
