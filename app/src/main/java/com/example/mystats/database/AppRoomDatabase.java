package com.example.mystats.database;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.UUID;

@Database(entities = {Task.class, Stat.class}, version = 7, exportSchema = false)
public abstract class AppRoomDatabase extends RoomDatabase {
    public abstract TaskDao taskDao();
    public abstract StatDao statDao();
    private static AppRoomDatabase INSTANCE;

    static AppRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppRoomDatabase.class) {
                INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                        AppRoomDatabase.class, "app_database")
                        .fallbackToDestructiveMigration()
                        .addCallback(sRoomDatabaseCallback)
                        .build();
            }
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback sRoomDatabaseCallback =
            new RoomDatabase.Callback() {
                @Override
                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                    super.onCreate(db);
                    new PopulateDbAsync(INSTANCE).execute();
                }
            };

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final TaskDao taskDao;
        private final StatDao statDao;

        Task[] tasks = {
                new Task(UUID.randomUUID().toString(), "Message someone you haven't talked to in a while", "Courage", 5, false),
                new Task(UUID.randomUUID().toString(), "Show gratitude to someone", "Expression", 3, false),
                new Task(UUID.randomUUID().toString(), "Get a checkup", "Health", 5, false),
                new Task(UUID.randomUUID().toString(), "Do 20 push ups", "Strength", 1, false),
                new Task(UUID.randomUUID().toString(), "Read 3 chapters of a book", "Knowledge", 3, false),
        };

        Stat[] stats = {new Stat(UUID.randomUUID().toString(), "Knowledge", 1),
                new Stat(UUID.randomUUID().toString(), "Strength", 1),
                new Stat(UUID.randomUUID().toString(), "Health", 1),
                new Stat(UUID.randomUUID().toString(), "Expression", 1),
                new Stat(UUID.randomUUID().toString(), "Courage", 1),
        };

        PopulateDbAsync(AppRoomDatabase db) {
            taskDao = db.taskDao();
            statDao = db.statDao();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            if (statDao.getAnyStat().length < 1) {
                for (int i = 0; i <= stats.length - 1; i++) {
                    statDao.insert(stats[i]);
                }
                for (int i = 0; i <= tasks.length - 1; i++) {
                    taskDao.insert(tasks[i]);
                }
            }
            return null;
        }
    }
}