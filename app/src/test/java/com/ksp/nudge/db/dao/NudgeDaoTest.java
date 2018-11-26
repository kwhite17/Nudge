package com.ksp.nudge.db.dao;

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

import java.util.ArrayList;
import java.util.List;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

@RunWith(RobolectricTestRunner.class)
public class NudgeDaoTest {
  private static final String TEST_MESSAGE = "Test Message";
  private static final NudgeConfig INITIAL_CONFIG = NudgeConfig.builder()
      .setSendTime(Instant.ofEpochMilli(0))
      .setFrequency(NudgeFrequency.ONCE)
      .setMessage(TEST_MESSAGE)
      .build();

  private NudgeDao nudgeDao;
  private NudgeDatabase nudgeDatabase;

  @Before
  public void createDatabase() {
    nudgeDatabase = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), NudgeDatabase.class)
        .allowMainThreadQueries()
        .build();
    nudgeDao = nudgeDatabase.nudgeDao();
  }

  @After
  public void cleanUp() {
    nudgeDatabase.clearAllTables();
  }

  @Test
  public void itCanReadItsOwnWrite() {
    Long id = nudgeDao.insertNudge(INITIAL_CONFIG);

    NudgeConfig actualConfig = nudgeDao.getNudgeById(id);
    Assert.assertEquals(actualConfig, INITIAL_CONFIG.withId(id));
  }

  @Test
  public void itCanReadItsOwnUpdate() {
    Long id = nudgeDao.insertNudge(INITIAL_CONFIG);
    String newMessage = "new message";
    NudgeConfig expectedConfig = INITIAL_CONFIG.withId(id)
        .withMessage(newMessage);

    nudgeDao.updateNudge(expectedConfig);
    Assert.assertEquals(expectedConfig, nudgeDao.getNudgeById(id));
  }

  @Test
  public void itCanDeleteConfig() {
    Long id = nudgeDao.insertNudge(INITIAL_CONFIG);

    nudgeDao.deleteNudge(INITIAL_CONFIG.withId(id));
    Assert.assertTrue(nudgeDao.getPendingNudges(Instant.now()).isEmpty());
  }

  @Test
  public void itCanRetrieveOutstandingNudges() {
    int numRecords = 10;
    int expectedRecords = 6;
    List<NudgeConfig> expectedConfigs = new ArrayList<>();
    for (int i = 0; i < numRecords; i++) {
      NudgeConfig newConfig = INITIAL_CONFIG.withSendTime(Instant.ofEpochMilli(i));
      Long id = nudgeDao.insertNudge(newConfig);
      if (i < expectedRecords) {
        expectedConfigs.add(newConfig.withId(id));
      }
    }
    List<NudgeConfig> actualConfigs = nudgeDao.getOutstandingNudges(Instant.ofEpochMilli
        (expectedRecords - 1));
    System.out.println(actualConfigs);
    Assert.assertEquals(expectedRecords, actualConfigs.size());
    for (NudgeConfig config : expectedConfigs) {
      Assert.assertTrue(expectedConfigs.contains(config));
    }
  }

  @Test
  public void itCanRetrievePendingNudges() {
    int numRecords = 10;
    int expectedRecords = 4;
    List<NudgeConfig> expectedConfigs = new ArrayList<>();
    for (int i = 0; i < numRecords; i++) {
      NudgeConfig newConfig = INITIAL_CONFIG.withSendTime(Instant.ofEpochMilli(i));
      Long id = nudgeDao.insertNudge(newConfig);
      if (i < numRecords - expectedRecords) {
        expectedConfigs.add(newConfig.withId(id));
      }
    }
    List<NudgeConfig> actualConfigs = nudgeDao.getPendingNudges(Instant.ofEpochMilli
        (numRecords - expectedRecords - 1));
    System.out.println(actualConfigs);
    Assert.assertEquals(expectedRecords, actualConfigs.size());
    for (NudgeConfig config : expectedConfigs) {
      Assert.assertTrue(expectedConfigs.contains(config));
    }
  }
}
