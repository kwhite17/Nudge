package com.ksp.nudge.db.dao;

import com.ksp.nudge.db.NudgeDatabase;
import com.ksp.nudge.model.NudgeConfig;
import com.ksp.nudge.model.NudgeFrequency;
import com.ksp.nudge.model.Recipient;

import org.joda.time.Instant;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.List;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

@RunWith(RobolectricTestRunner.class)
public class RecipientDaoTest {
    private static final String TEST_MESSAGE = "Test Message";
    private static final NudgeConfig INITIAL_CONFIG = NudgeConfig.builder()
        .setSendTime(Instant.ofEpochMilli(0))
        .setFrequency(NudgeFrequency.ONCE)
        .setMessage(TEST_MESSAGE)
        .build();
    private static final String INITIAL_PHONE_NUMBER = "1234567890";
    private static final String INITIAL_NAME = "Kevin";

    private NudgeDao nudgeDao;
    private RecipientDao recipientDao;
    private Long initialNudgeId;

    @Before
    public void createDatabase() {
        NudgeDatabase nudgeDatabase = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(),
            NudgeDatabase.class)
            .allowMainThreadQueries()
            .build();
        nudgeDao = nudgeDatabase.nudgeDao();
        recipientDao = nudgeDatabase.recipientDao();
        initialNudgeId = nudgeDao.insertNudge(INITIAL_CONFIG);
    }

    @Test
    public void itCanReadItsOwnWrite() {
        Recipient testRecipient = Recipient.builder()
            .setNudgeId(initialNudgeId)
            .setName(INITIAL_NAME)
            .setPhoneNumber(INITIAL_PHONE_NUMBER)
            .build();
        List<Long> ids = recipientDao.insertRecipients(testRecipient);
        List<Recipient> recipients = recipientDao.getRecipientsByNudgeId(initialNudgeId);

        Assert.assertEquals(1, recipients.size());
        Assert.assertTrue(recipients.contains(testRecipient.withId(ids.get(0))));
    }

    @Test
    public void itCanReadRecipientAfterConfigUpdate() {
        Recipient testRecipient = Recipient.builder()
            .setNudgeId(initialNudgeId)
            .setName(INITIAL_NAME)
            .setPhoneNumber(INITIAL_PHONE_NUMBER)
            .build();
        List<Long> ids = recipientDao.insertRecipients(testRecipient);
        nudgeDao.updateNudge(INITIAL_CONFIG.withId(initialNudgeId)
            .withMessage("New Message")
        );

        List<Recipient> recipients = recipientDao.getRecipientsByNudgeId(initialNudgeId);
        Assert.assertEquals(1, recipients.size());
        Assert.assertTrue(recipients.contains(testRecipient.withId(ids.get(0))));
    }

    @Test
    public void itCanDeleteRecipients() {
        Recipient testRecipient = Recipient.builder()
            .setNudgeId(initialNudgeId)
            .setName(INITIAL_NAME)
            .setPhoneNumber(INITIAL_PHONE_NUMBER)
            .build();
        List<Long> ids = recipientDao.insertRecipients(testRecipient);

        recipientDao.deleteRecipients(testRecipient.withId(ids.get(0)));
        Assert.assertTrue(recipientDao.getRecipientsByNudgeId(initialNudgeId).isEmpty());
    }

    @Test
    public void itDeletesRecipientOnNudgeDeletion() {
        Recipient testRecipient = Recipient.builder()
            .setNudgeId(initialNudgeId)
            .setName(INITIAL_NAME)
            .setPhoneNumber(INITIAL_PHONE_NUMBER)
            .build();
        recipientDao.insertRecipients(testRecipient);

        nudgeDao.deleteNudge(INITIAL_CONFIG.withId(initialNudgeId));
        Assert.assertTrue(recipientDao.getRecipientsByNudgeId(initialNudgeId).isEmpty());
    }
}
