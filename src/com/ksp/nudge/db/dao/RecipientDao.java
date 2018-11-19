package com.ksp.nudge.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.RawQuery;
import android.arch.persistence.room.Update;

import com.ksp.nudge.model.Recipient;

import java.util.List;

/**
 * Created by kevin on 9/24/2018.
 */

@Dao
public interface RecipientDao {
    @Query("SELECT * FROM recipients WHERE nudgeId = :nudgeId")
    List<Recipient> getRecipientsByNudgeId(int nudgeId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRecipients(Recipient... recipients);

    @Delete
    void deleteRecipients(Recipient... recipients);

}
