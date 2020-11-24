package com.example.mystats.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface StatDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Stat stat);

    @Update
    void update(Stat stat);

    @Query("UPDATE stat_table SET value = value + :value WHERE stat = :stat")
    void addToStat(String stat, int value);

    @Query("DELETE FROM stat_table WHERE id = :id")
    void delete(String id);

    @Query("SELECT * from stat_table ORDER BY sort_order ASC")
    LiveData<List<Stat>> getAllStats();

    @Query("SELECT * from stat_table LIMIT 1")
    Stat[] getAnyStat();

    @Query("UPDATE stat_table SET scaling = :scaling WHERE stat= :stat")
    void setScaling(String stat, double scaling);
}
