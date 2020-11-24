package com.example.mystats.service;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.example.mystats.database.AppRepository;
import com.example.mystats.activities.ToDoActivity;

public class DatabaseService extends JobIntentService {
    private static final int JOB_ID = 1000;
    private AppRepository mRepository;

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        mRepository = new AppRepository(this.getApplication());
        mRepository.markTaskResult(intent.getStringExtra(ToDoActivity.TASK_ID_REPLY), intent.getStringExtra(ToDoActivity.TASK_STATUS_REPLY));
    }

    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, DatabaseService.class, JOB_ID, intent);
    }
}
