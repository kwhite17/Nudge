package com.ksp.nudge.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.content.Intent;

import com.ksp.nudge.model.NudgeConfig;

import org.joda.time.Instant;

import java.util.List;

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
    NudgeConfig getNudgeById(int id);

    @Query("SELECT * FROM nudges WHERE sendTime <= :now")
    List<NudgeConfig> getOutstandingNudges(Instant now);

    @Query("SELECT * FROM nudges WHERE sendTime > :now")
    List<NudgeConfig> getPendingNudges(Instant now);

    @Delete
    void deleteNudge(NudgeConfig config);
}
