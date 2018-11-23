package com.ksp.nudge.db.dao;

import android.arch.persistence.room.Room;
import android.os.Build;

import com.ksp.nudge.BuildConfig;
import com.ksp.nudge.db.NudgeDatabase;
import com.ksp.nudge.model.NudgeConfig;
import com.ksp.nudge.model.NudgeFrequency;

import org.joda.time.Instant;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

@RunWith(RobolectricTestRunner.class)
public class NudgeDaoTest {
    private static final String TEST_MESSAGE = "Test Message";

    private NudgeDao nudgeDao;
    private NudgeConfig initialConfig;
    private NudgeDatabase nudgeDatabase;

    @Before
    public void createDatabase() {
        nudgeDatabase = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.application, NudgeDatabase.class)
                .allowMainThreadQueries()
                .build();
        nudgeDao = nudgeDatabase.nudgeDao();
        initialConfig = new NudgeConfig();
        initialConfig.setSendTime(Instant.ofEpochMilli(0));
        initialConfig.setFrequency(NudgeFrequency.ONCE);
        initialConfig.setMessage(TEST_MESSAGE);
    }

    @After
    public void cleanUp() {
        nudgeDatabase.clearAllTables();
    }

    @Test
    public void itCanReadItsOwnWrite() {
        long id = nudgeDao.insertNudge(initialConfig);
        NudgeConfig config = nudgeDao.getNudgeById(id);
        initialConfig.setId(id);
        Assert.assertEquals(config, initialConfig);
    }
}
