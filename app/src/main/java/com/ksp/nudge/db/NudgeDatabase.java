package com.ksp.nudge.db;

import com.ksp.nudge.db.dao.NudgeDao;
import com.ksp.nudge.db.dao.RecipientDao;
import com.ksp.nudge.model.NudgeConfig;
import com.ksp.nudge.model.Recipient;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

/**
 * Created by kevin on 9/24/2018.
 */

@Database(entities = {NudgeConfig.class, Recipient.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class NudgeDatabase extends RoomDatabase {
    public abstract NudgeDao nudgeDao();
    public abstract RecipientDao recipientDao();
}
