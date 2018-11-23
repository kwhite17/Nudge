package com.ksp.nudge;

import com.ksp.nudge.model.NudgeFrequency;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class NudgeUtilsTest {

    private Instant initialTime;

    @Before
    public void setUp() {
        initialTime = Instant.now().minus(Duration.standardMinutes(30));
    }

    @Test
    public void itGetsNextSendTimeWhenFrequencyIsOnce() {
        validateNextSendTime(NudgeFrequency.ONCE);
    }

    @Test
    public void itGetsNextSendTimeWhenFrequencyIsDaily() {
        validateNextSendTime(NudgeFrequency.DAILY);
    }

    @Test
    public void itGetsNextSendTimeWhenFrequencyIsWeekly() {
        validateNextSendTime(NudgeFrequency.WEEKLY);
    }

    @Test
    public void itGetsNextSendTimeWhenFrequencyIsMonthly() {
        List<Integer> daysBetween = Arrays.asList(28, 29, 30, 31);
        Instant nextSendTime = NudgeUtils.getNextSend(initialTime, NudgeFrequency.MONTHLY);
        Assert.assertTrue(daysBetween.contains(Days.daysBetween(initialTime, nextSendTime).getDays()));
    }

    @Test
    public void itGetsNextSendTimeWhenFrequencyIsEndOfMonthAndScheduleTimeIsNowOrInPast() {
        List<Integer> daysBetween = Arrays.asList(28, 29, 30, 31);
        Instant endMonthInitial = initialTime.toDateTime()
                .minusMonths(1)
                .dayOfMonth()
                .withMaximumValue()
                .toInstant();
        Instant nextSendTime = NudgeUtils.getNextSend(endMonthInitial, NudgeFrequency.END_OF_MONTH);
        Assert.assertTrue(daysBetween.contains(Days.daysBetween(endMonthInitial, nextSendTime).getDays()));
        DateTime expectedValue = nextSendTime.toDateTime()
                .dayOfMonth()
                .withMaximumValue();
        Assert.assertEquals(expectedValue, nextSendTime.toDateTime());
    }

    @Test
    public void itGetsNextSendTimeWhenFrequencyIsEndOfMonthAndScheduledTimeInFuture() {
        Instant endMonthInitial = initialTime.toDateTime()
                .plusMonths(1)
                .dayOfMonth()
                .withMaximumValue()
                .toInstant();
        Instant nextSendTime = NudgeUtils.getNextSend(endMonthInitial, NudgeFrequency.END_OF_MONTH);
        Assert.assertTrue(Days.daysBetween(endMonthInitial, nextSendTime).isLessThan(Days.days(31)));
        DateTime expectedValue = nextSendTime.toDateTime()
                .dayOfMonth()
                .withMaximumValue();
        Assert.assertEquals(expectedValue, nextSendTime.toDateTime());
    }

    @Test
    public void itReturnsOriginalTimeIfSendTimeInFutureAndNotEndOfMonth() {
        Instant futureInitialTime = initialTime.toDateTime()
                .plusMonths(1)
                .toInstant();
        for (NudgeFrequency frequency : NudgeFrequency.values()) {
            if (frequency == NudgeFrequency.END_OF_MONTH) {
                continue;
            }
            Assert.assertEquals(futureInitialTime, NudgeUtils.getNextSend(futureInitialTime, frequency));
        }
    }

    private void validateNextSendTime(NudgeFrequency frequency) {
        Instant nextSendTime = NudgeUtils.getNextSend(initialTime, frequency);
        Assert.assertEquals(initialTime.plus(getDurationToAdd(frequency)), nextSendTime);
    }

    private Duration getDurationToAdd(NudgeFrequency frequency) {
        if (frequency == NudgeFrequency.WEEKLY) {
            return Duration.standardDays(7);
        } else if (frequency == NudgeFrequency.DAILY || frequency == NudgeFrequency.ONCE) {
            return Duration.standardDays(1);
        }
        throw new IllegalArgumentException();
    }
}
