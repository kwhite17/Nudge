package com.ksp.nudge.db;

import com.ksp.nudge.NudgeApp;
import com.ksp.nudge.model.Nudge;
import com.ksp.nudge.model.NudgeConfig;
import com.ksp.nudge.model.Recipient;

import org.joda.time.Instant;

import java.util.ArrayList;
import java.util.List;

import static com.ksp.nudge.NudgeUtils.getNextSend;

public class NudgeDatabaseHelper {

    public static boolean isFilled(Nudge nudge) {
        return !nudge.getRecipients().isEmpty();
    }

    public static Nudge buildNudgeFromId(NudgeConfig config) {
        NudgeDatabase database = NudgeApp.get().getDatabase();
        List<Recipient> recipients = database.recipientDao().getRecipientsByNudgeId(config.getId());
        Nudge nudge = new Nudge();
        nudge.setNudgeConfig(config);
        nudge.setRecipients(recipients);
        return nudge;
    }

    public static List<Nudge> getOutstandingNudges() {
        NudgeDatabase database = NudgeApp.get().getDatabase();
        List<NudgeConfig> configs = database.nudgeDao().getOutstandingNudges(Instant.now());
        List<Nudge> nudges = new ArrayList<>();
        for (NudgeConfig config : configs) {
            nudges.add(buildNudgeFromId(config));
        }
        return nudges;
    }

    public static List<Nudge> getPendingNudges() {
        NudgeDatabase database = NudgeApp.get().getDatabase();
        List<NudgeConfig> configs = database.nudgeDao().getPendingNudges(Instant.now());
        List<Nudge> nudges = new ArrayList<>();
        for (NudgeConfig config : configs) {
            nudges.add(buildNudgeFromId(config));
        }
        return nudges;
    }

    /**
     * Updates the send time of a recurring message in the database
     * @param nudge, the message containing the info needed to update the send time
     */
    private static void updateNudge(Nudge nudge) {
        NudgeDatabase database = NudgeApp.get().getDatabase();
        Instant nextSend = getNextSend(nudge.getNudgeConfig().getSendTime(), nudge.getNudgeConfig().getFrequency());
        nudge.getNudgeConfig().setSendTime(nextSend);
        database.nudgeDao().updateNudge(nudge.getNudgeConfig());
    }

    /**
     * Deletes a message from the database based on its id
     * @param nudge, the message to delete
     */
    static void deleteNudge(Nudge nudge){
        NudgeApp.get().getDatabase().nudgeDao().deleteNudge(nudge.getNudgeConfig());
    }

    public static void insertNudge(Nudge nudge) {
        NudgeDatabase database = NudgeApp.get().getDatabase();
        long nudgeId = database.nudgeDao().insertNudge(nudge.getNudgeConfig());
        for (Recipient recipient : nudge.getRecipients()) {
            recipient.setNudgeId(nudgeId);
        }
        database.recipientDao().insertRecipients(nudge.getRecipients()
                .toArray(new Recipient[0]));
    }

    public static boolean maybeUpdateNudge(Nudge currentNudge) {
        if (currentNudge.getNudgeConfig().getFrequency().equals("Once")) {
            deleteNudge(currentNudge);
            return false;
        }
        updateNudge(currentNudge);
        return true;
    }

    public static void deleteEditableRecipients(List<Recipient> recipients) {
        NudgeDatabase database = NudgeApp.get().getDatabase();
        database.recipientDao().deleteRecipients(recipients.toArray(new Recipient[0]));
    }
}
