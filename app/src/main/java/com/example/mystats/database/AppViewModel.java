package com.example.mystats.database;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class AppViewModel extends AndroidViewModel {

    private AppRepository mRepository;
    private LiveData<List<Stat>> mAllStats;

    public AppViewModel(Application application) {
        super(application);
        mRepository = new AppRepository(application);
        mAllStats = mRepository.getAllStats();
    }

    public LiveData<List<Task>> getToDoTasks() {
        return mRepository.getToDoTasks();
    }

    public LiveData<List<Task>> getTaskHistory() {
        return mRepository.getTaskHistory();
    }

    public LiveData<List<Stat>> getAllStats() {
        return mAllStats;
    }

    public void insert(Task task) {
        mRepository.insert(task);
    }

    public void insert(Stat stat) {
        mRepository.insert(stat);
    }

    public void update(Stat stat) {
        mRepository.update(stat);
    }

    public void markTaskResult(String id, String result) {
        mRepository.markTaskResult(id, result);
    }

    public void delete(Stat stat) {
        mRepository.delete(stat);
    }

    public void deleteAsyncTasksWithStat(String stat_name) {
        mRepository.deleteAsyncTasksWithStat(stat_name);
    }

    public void deleteTask(String taskId) {
        mRepository.deleteTask(taskId);
    }

    public void clearTaskHistory() {
        mRepository.clearTaskHistory();
    }

    public void renameStatInTasks(String old_stat, String new_stat) {
        mRepository.renameStatInTasks(old_stat, new_stat);
    }

    public void swapStats(Stat firstStat, Stat secondStat) {
        long temp = firstStat.getOrder();
        firstStat.setOrder(secondStat.getOrder());
        secondStat.setOrder(temp);
        mRepository.update(secondStat);
        mRepository.update(firstStat);
    }

    public void deleteOldTasks() {
        mRepository.deleteOldTasks();
    }

    public void setExplanation(String taskId, String explanation) {
        mRepository.setExplanation(taskId, explanation);
    }

    public void setPhotoRotation(String taskId, int photoRotation) {
        mRepository.setPhotoRotation(taskId, photoRotation);
    }
}