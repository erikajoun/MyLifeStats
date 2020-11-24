package com.example.mystats.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Task task);

    @Update
    void update(Task... task);

    @Query("DELETE FROM task_table WHERE id = :id")
    void delete(String id);

    @Query("DELETE FROM task_table WHERE stat = :value")
    void deleteByStat(String value);

    @Query("DELETE FROM task_table WHERE status IN ('Complete', 'Missed', 'Cancelled')")
    void deleteHistory();

    @Query("SELECT * from task_table")
    LiveData<List<Task>> getAllTasks();

    @Query("SELECT * from task_table LIMIT 1")
    Task[] getAnyTask();

    @Query("SELECT * from task_table WHERE status IN ('To Do', 'Waiting') ORDER BY datetime_created DESC")
    LiveData<List<Task>> getToDoTasks();

    @Query("SELECT * from task_table WHERE status IN ('Complete', 'Missed', 'Cancelled') ORDER BY datetime_removed DESC")
    LiveData<List<Task>> getTaskHistory();

    @Query("SELECT stat from task_table WHERE id =:id")
    String getStatOfTask(String id);

    @Query("SELECT value from task_table WHERE id =:id")
    int getValueOfTask(String id);

    @Query("SELECT penalty from task_table WHERE id =:id")
    int getPenaltyOfTask(String id);

    @Query("SELECT status from task_table WHERE id =:id")
    String getStatusOfTask(String id);

    @Query("UPDATE task_table SET datetime_removed = :dateRemoved WHERE id = :id")
    void setDatetimeRemoved(String id, String dateRemoved);

    @Query("UPDATE task_table SET status = :status WHERE id = :id")
    void set_status(String id, String status);

    @Query("UPDATE task_table SET stat = :new_stat WHERE stat = :old_stat")
    void rename_stat(String old_stat, String new_stat);

    @Query("DELETE FROM task_table WHERE datetime_removed != '' AND datetime_removed <= datetime('now','-30 day')")
    void delete_old_tasks();

    @Query("UPDATE task_table SET explanation = :explanation WHERE id = :id")
    void set_explanation(String id, String explanation);

    @Query("UPDATE task_table SET photo_rotation = :photo_rotation WHERE id = :id")
    void set_photo_rotation(String id, int photo_rotation);
}
