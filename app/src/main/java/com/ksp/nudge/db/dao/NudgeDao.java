package com.ksp.nudge.db.dao;

import com.ksp.nudge.model.NudgeConfig;

import org.joda.time.Instant;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

/**
 * Created by kevin on 9/24/2018.
 */

@Dao
public interface NudgeDao {

    @Insert
    long insertNudge(NudgeConfig nudge);

    @Update
    void updateNudge(NudgeConfig nudge);

    @Query("SELECT * FROM nudges WHERE id = :id")
    NudgeConfig getNudgeById(long id);

    @Query("SELECT * FROM nudges WHERE sendTime <= :now")
    List<NudgeConfig> getOutstandingNudges(Instant now);

    @Query("SELECT * FROM nudges WHERE sendTime > :now")
    List<NudgeConfig> getPendingNudges(Instant now);

    @Delete
    void deleteNudge(NudgeConfig config);
}
