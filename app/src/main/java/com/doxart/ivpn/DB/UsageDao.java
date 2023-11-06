package com.doxart.ivpn.DB;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UsageDao {
    @Query("SELECT * FROM usage WHERE date = :date")
    Usage getUsage(String date);

    @Query("SELECT * FROM usage")
    LiveData<List<Usage>> getAllUsage();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUsage(Usage budget);
}
