package com.ksp.nudge.db.dao;

import com.ksp.nudge.model.Recipient;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

/**
 * Created by kevin on 9/24/2018.
 */

@Dao
public interface RecipientDao {
    @Query("SELECT * FROM recipients WHERE nudgeId = :nudgeId")
    List<Recipient> getRecipientsByNudgeId(long nudgeId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRecipients(Recipient... recipients);

    @Delete
    void deleteRecipients(Recipient... recipients);

}
