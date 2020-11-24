package com.example.mystats.database;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.time.LocalDateTime;
import java.util.List;

public class AppRepository {

    private TaskDao mTaskDao;
    private StatDao mStatDao;
    private LiveData<List<Stat>> mAllStats;

    public AppRepository(Application application) {
        AppRoomDatabase db = com.example.mystats.database.AppRoomDatabase.getDatabase(application);
        mTaskDao = db.taskDao();
        mStatDao = db.statDao();
        mAllStats = mStatDao.getAllStats();
    }

    public LiveData<List<Stat>> getAllStats() {
        return mAllStats;
    }

    public LiveData<List<Task>> getToDoTasks() {
        return mTaskDao.getToDoTasks();
    }

    public LiveData<List<Task>> getTaskHistory() {
        return mTaskDao.getTaskHistory();
    }

    public void insert(Task task) {
        new insertAsyncTask(mTaskDao).execute(task);
    }

    public void insert(Stat stat) {
        new insertAsyncStat(mStatDao).execute(stat);
    }

    public void update(Stat stat) {
        new updateAsyncStat(mStatDao).execute(stat);
    }

    public void delete(Stat stat) {
        new deleteAsyncStat(mStatDao).execute(stat);
    }

    public void deleteTask(String taskId) {
        new deleteAsyncTask(mTaskDao, taskId).execute();
    }

    public void markTaskResult(String id, String result) {
        new markTaskResultAsync(mTaskDao, mStatDao, id, result).execute();
    }

    public void renameStatInTasks(String old_stat, String new_stat) {
        new renameAsyncStatInTasks(mTaskDao, old_stat, new_stat).execute();
    }

    public void deleteAsyncTasksWithStat(String stat_name) {
        new deleteAsyncTasksWithStat(mTaskDao).execute(stat_name);
    }

    public void deleteOldTasks() {
        new deleteOldTasksAsync(mTaskDao).execute();
    }

    public void setExplanation(String taskId, String explanation) {
        new setExplanationAsync(mTaskDao, taskId, explanation).execute();
    }

    public void setPhotoRotation(String taskId, int photoRotation) {
        new setPhotoRotationAsync(mTaskDao, taskId, photoRotation).execute();
    }

    public void clearTaskHistory() {
        new clearTaskHistoryAsync(mTaskDao).execute();
    }
    private static class insertAsyncTask extends AsyncTask<Task, Void, Void> {
        private TaskDao mAsyncTaskDao;

        insertAsyncTask(TaskDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Task... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private static class insertAsyncStat extends AsyncTask<Stat, Void, Void> {
        private StatDao mAsyncStatDao;

        insertAsyncStat(StatDao dao) {
            mAsyncStatDao = dao;
        }

        @Override
        protected Void doInBackground(final Stat... params) {
            mAsyncStatDao.insert(params[0]);
            return null;
        }
    }

    private static class updateAsyncStat extends AsyncTask<Stat, Void, Void> {
        private StatDao mAsyncStatDao;

        updateAsyncStat(StatDao dao) {
            mAsyncStatDao = dao;
        }

        @Override
        protected Void doInBackground(final Stat... params) {
            mAsyncStatDao.update(params[0]);
            return null;
        }
    }

    private static class deleteAsyncStat extends AsyncTask<Stat, Void, Void> {
        private StatDao mAsyncStatDao;

        deleteAsyncStat(StatDao dao) {
            mAsyncStatDao = dao;
        }

        @Override
        protected Void doInBackground(final Stat... params) {
            mAsyncStatDao.delete(params[0].getId());
            return null;
        }
    }

    private static class markTaskResultAsync extends AsyncTask<String, Void, Void> {
        private TaskDao mAsyncTaskDao;
        private StatDao mAsyncStatDao;
        private String id;
        private String result;

        markTaskResultAsync(TaskDao taskDao, StatDao statDao, String id, String result) {
            this.mAsyncTaskDao = taskDao;
            this.mAsyncStatDao = statDao;
            this.id = id;
            this.result = result;
        }

        @Override
        protected Void doInBackground(final String... params) {
            switch (result) {
                case "Complete":
                    mAsyncTaskDao.set_status(id, "Complete");
                    mAsyncStatDao.addToStat(mAsyncTaskDao.getStatOfTask(id), mAsyncTaskDao.getValueOfTask(id));
                    mAsyncTaskDao.setDatetimeRemoved(id, LocalDateTime.now().toString());
                    break;
                case "To Do":
                    switch (mAsyncTaskDao.getStatusOfTask(id)) {
                        case "Complete":
                            mAsyncStatDao.addToStat(mAsyncTaskDao.getStatOfTask(id), -mAsyncTaskDao.getValueOfTask(id));
                            break;
                        case "Missed":
                            mAsyncStatDao.addToStat(mAsyncTaskDao.getStatOfTask(id), mAsyncTaskDao.getPenaltyOfTask(id));
                            break;
                    }
                    mAsyncTaskDao.set_status(id, "To Do");
                    mAsyncTaskDao.setDatetimeRemoved(id, "");
                    break;
                case "Missed":
                    mAsyncTaskDao.set_status(id, "Missed");
                    mAsyncStatDao.addToStat(mAsyncTaskDao.getStatOfTask(id), -mAsyncTaskDao.getPenaltyOfTask(id));
                    mAsyncTaskDao.setDatetimeRemoved(id, LocalDateTime.now().toString());
                    break;
                case "Cancelled":
                    mAsyncTaskDao.set_status(id, "Cancelled");
                    mAsyncTaskDao.setDatetimeRemoved(id, LocalDateTime.now().toString());
                    break;
                default:
                    break;
            }
            return null;
        }
    }

    private static class deleteAsyncTasksWithStat extends AsyncTask<String, Void, Void> {
        private TaskDao mAsyncTaskDao;

        deleteAsyncTasksWithStat(TaskDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final String... params) {
            mAsyncTaskDao.deleteByStat(params[0]);
            return null;
        }
    }

    private static class renameAsyncStatInTasks extends AsyncTask<String, Void, Void> {
        private TaskDao mAsyncTaskDao;
        private String old_stat;
        private String new_stat;

        renameAsyncStatInTasks(TaskDao dao, String old_stat, String new_stat) {
            mAsyncTaskDao = dao;
            this.old_stat = old_stat;
            this.new_stat = new_stat;
        }

        @Override
        protected Void doInBackground(final String... params) {
            mAsyncTaskDao.rename_stat(old_stat, new_stat);
            return null;
        }
    }

    private static class deleteOldTasksAsync extends AsyncTask<String, Void, Void> {
        private TaskDao mAsyncTaskDao;

        deleteOldTasksAsync(TaskDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final String... params) {
            mAsyncTaskDao.delete_old_tasks();
            return null;
        }
    }

    private static class setExplanationAsync extends AsyncTask<String, Void, Void> {
        private TaskDao mAsyncTaskDao;
        private String taskId;
        private String explanation;

        setExplanationAsync(TaskDao dao, String taskId, String explanation) {
            mAsyncTaskDao = dao;
            this.taskId = taskId;
            this.explanation = explanation;
        }

        @Override
        protected Void doInBackground(final String... params) {
            mAsyncTaskDao.set_explanation(taskId, explanation);
            return null;
        }
    }

    private static class setPhotoRotationAsync extends AsyncTask<Integer, Void, Void> {
        private TaskDao mAsyncTaskDao;
        private String taskId;
        private int photoRotation;

        setPhotoRotationAsync(TaskDao dao, String taskId, int photoRotation) {
            mAsyncTaskDao = dao;
            this.taskId = taskId;
            this.photoRotation = photoRotation;
        }

        @Override
        protected Void doInBackground(final Integer... params) {
            mAsyncTaskDao.set_photo_rotation(taskId, photoRotation);
            return null;
        }
    }

    private static class deleteAsyncTask extends AsyncTask<String, Void, Void> {
        private TaskDao mAsyncTaskDao;
        private String taskId;

        deleteAsyncTask(TaskDao dao, String taskId) {
            mAsyncTaskDao = dao;
            this.taskId = taskId;
        }

        @Override
        protected Void doInBackground(final String... params) {
            mAsyncTaskDao.delete(taskId);
            return null;
        }
    }

    private static class clearTaskHistoryAsync extends AsyncTask<String, Void, Void> {
        private TaskDao mAsyncTaskDao;

        clearTaskHistoryAsync(TaskDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final String... params) {
            mAsyncTaskDao.deleteHistory();
            return null;
        }
    }
}
