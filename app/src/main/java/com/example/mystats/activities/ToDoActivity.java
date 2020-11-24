package com.example.mystats.activities;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Pair;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mystats.R;
import com.example.mystats.adapters.TaskListAdapter;
import com.example.mystats.database.Task;
import com.example.mystats.database.AppViewModel;
import com.example.mystats.service.AlarmReceiver;
import com.example.mystats.utility.OneShotClickListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Stack;
import java.util.UUID;
import java.util.stream.Collectors;

public class ToDoActivity extends AppCompatActivity implements TaskListAdapter.ListItemClickListener {
    public static final String TASK_ID_REPLY = "com.example.mystats.TASK_ID_REPLY";
    public static final String TASK_STATUS_REPLY = "com.example.mystats.TASK_STATUS_REPLY";
    public static final String TASK_DEADLINE_REPLY = "com.example.mystats.TASK_DEADLINE_REPLY";

    private static final int NEW_TASK_ACTIVITY_REQUEST_CODE = 1;
    private static final int PHOTO_ACTIVITY_REQUEST_CODE = 3;

    private AppViewModel mAppViewModel;
    private List<Task> mTasksData;
    private AlarmManager mAlarmManager;
    private static final int NOTIFICATION_ID = 0;

    private Task mSelectedTask;
    private String mSelectedStatus;
    private String mSelectedTab;
    private FloatingActionButton mAddTaskButton;
    private TabLayout mTabLayout;
    private HashMap<String, Integer> mTabMapping;

    private Stack<Pair<Pair<String, String>, String>> mActionsHistory;
    private MenuItem mMenuUndoButton;
    private AlertDialog.Builder mBuilder;

    private int mAddButtonTint;
    private Drawable mAddButtonDrawable;
    private int mTabTextColor;
    private int mTabIndicatorColor;

    private TaskListAdapter mAdapter;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.todo_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mMenuUndoButton = menu.findItem(R.id.undo);
        mMenuUndoButton.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.undo && !mActionsHistory.empty()) {
            mBuilder.setCancelable(true);
            Pair action = mActionsHistory.peek();
            String taskId = ((Pair) action.first).first.toString();
            String taskTitle = ((Pair) action.first).second.toString();
            String taskAction = action.second.toString();
            String actionDesc = taskAction + " Task: " + taskTitle;
            mBuilder.setMessage("Undo Last Action? (" + actionDesc + ")");
            mBuilder.setPositiveButton("Undo", (dialog, which) -> {
                mActionsHistory.pop();
                switch (taskAction) {
                    case "Create":
                        mAppViewModel.deleteTask(taskId);
                        break;
                    case "Complete":
                        mAppViewModel.markTaskResult(taskId, "To Do");
                    case "Cancel":
                        mAppViewModel.markTaskResult(taskId, "To Do");
                    case "Fail":
                        mAppViewModel.markTaskResult(taskId, "To Do");
                }
                if (mActionsHistory.isEmpty()) {
                    mMenuUndoButton.setVisible(false);
                }
            });
            mBuilder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {});
            AlertDialog dialog = mBuilder.create();
            dialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mAddTaskButton.setOnClickListener(new OneShotClickListener() {
            @Override
            public void onClicked(View view) {
                launchNewTaskActivity(view);
            }
        });
        mTabLayout.getTabAt(mTabMapping.get(mSelectedTab)).select();
    }

    private List<Task> filterTasks(List<Task> tasksData, String deadlineString) {
        List<Task> filteredTasksData = new ArrayList<>();
        switch (deadlineString) {
            case "Today":
                filteredTasksData = tasksData.stream().filter(task -> task.hasDeadline() == true).collect(Collectors.toList());
                filteredTasksData = filteredTasksData.stream().filter(task -> task.daysLeft() == 0).collect(Collectors.toList());
                break;
            case "Tomorrow":
                filteredTasksData = tasksData.stream().filter(task -> task.hasDeadline() == true).collect(Collectors.toList());
                filteredTasksData = filteredTasksData.stream().filter(task -> task.daysLeft() == 1).collect(Collectors.toList());
                break;
            case "Later":
                filteredTasksData = tasksData.stream().filter(task -> task.hasDeadline() == true).collect(Collectors.toList());
                filteredTasksData = filteredTasksData.stream().filter(task -> task.daysLeft() > 1).collect(Collectors.toList());
                break;
            case "No Deadline":
                filteredTasksData = tasksData.stream().filter(task -> task.hasDeadline() == false).collect(Collectors.toList());
                break;
            default:
                break;
        }
        return filteredTasksData;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String theme = getSharedPreferences("Settings", MODE_PRIVATE).getString("theme", "morning");
        switch(theme) {
            case "morning":
                setTheme(R.style.MorningTheme);
                mAddButtonTint = getResources().getColor(R.color.colorAddMorning);
                mAddButtonDrawable = getResources().getDrawable(R.drawable.gray_plus_sign);
                mTabTextColor = Color.BLACK;
                mTabIndicatorColor = Color.WHITE;
                break;
            case "evening":
                setTheme(R.style.EveningTheme);
                mAddButtonTint = getResources().getColor(R.color.colorAddEvening);
                mAddButtonDrawable = getResources().getDrawable(R.drawable.white_plus_sign);
                mTabTextColor = Color.BLACK;
                mTabIndicatorColor = Color.WHITE;
                break;
            case "night":
                setTheme(R.style.NightTheme);
                mAddButtonTint = getResources().getColor(R.color.colorAddNight);
                mAddButtonDrawable = getResources().getDrawable(R.drawable.white_plus_sign);
                mTabTextColor = Color.WHITE;
                mTabIndicatorColor = Color.WHITE;
                break;
            default:
                break;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do);

        mBuilder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.MyAlertDialogStyle));
        mActionsHistory = new Stack<>();
        mSelectedTab = "Today";

        mTabMapping = new HashMap<>();
        mTabMapping.put("Today", 0);
        mTabMapping.put("Tomorrow", 1);
        mTabMapping.put("Later", 2);
        mTabMapping.put("No Deadline", 3);

        mAddTaskButton = findViewById(R.id.add_button);
        mAddTaskButton.setBackgroundTintList(ColorStateList.valueOf(mAddButtonTint));
        mAddTaskButton.setImageDrawable(mAddButtonDrawable);

        mAdapter = new TaskListAdapter(this, this, theme);
        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAppViewModel = ViewModelProviders.of(this).get(AppViewModel.class);
        mAppViewModel.getToDoTasks().observe(this, tasks -> {
            for (Task task : tasks) {
                if (!task.getOptional() && task.daysLeft() < 0) {
                    missTask(task);
                }
                else if (task.getStatus().contentEquals("Waiting") && task.isAvailable()) {
                    task.setStatus("To Do");
                }
            }
            mTasksData = tasks;
            mAdapter.setTasks(filterTasks(mTasksData, mSelectedTab));

        });

        mTabLayout = findViewById(R.id.tabs);
        mTabLayout.setSelectedTabIndicatorColor(mTabIndicatorColor);
        mTabLayout.setBackgroundColor(getResources().getColor(R.color.cardColor));
        mTabLayout.setTabTextColors(mTabTextColor, mTabTextColor);

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mSelectedTab = Objects.requireNonNull(tab.getText()).toString();
                mAdapter.setTasks(filterTasks(mTasksData, mSelectedTab));
                recyclerView.scrollToPosition(0);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PHOTO_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            mAppViewModel.setExplanation(mSelectedTask.getId(), data.getStringExtra(PhotoActivity.EXPLANATION_REPLY));
            mAppViewModel.setPhotoRotation(mSelectedTask.getId(), data.getIntExtra(PhotoActivity.PHOTO_ROTATION_REPLY, 0));
            if (mSelectedStatus.contentEquals("Complete")) {
                completeTask(mSelectedTask);
            } else if (mSelectedStatus.contentEquals("Cancelled")) {
                cancelTask(mSelectedTask);
            }
        } else if (requestCode == NEW_TASK_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            mSelectedTab = data.getStringExtra(NewTaskActivity.SELECTEDTAB_REPLY);
            String deadlineString = data.getStringExtra(NewTaskActivity.DUEDATE_REPLY);
            Task task = new Task.Builder(UUID.randomUUID().toString(),
                    data.getStringExtra(NewTaskActivity.TITLE_REPLY),
                    data.getStringExtra(NewTaskActivity.STATS_REPLY),
                    data.getIntExtra(NewTaskActivity.VALUE_REPLY, 0),
                    data.getBooleanExtra(NewTaskActivity.KEEPPRIVATE_REPLY, false))
                    .description(data.getStringExtra(NewTaskActivity.DESCRIPTION_REPLY))
                    .deadline(deadlineString, data.getIntExtra(NewTaskActivity.PENALTY_REPLY, 0))
                    .repeatInterval(data.getIntExtra(NewTaskActivity.REPEAT_REPLY, 0))
                    .build();
            try {
                mAppViewModel.insert(task);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (task.getDeadline().length() > 0) {
                Intent notifyIntent = new Intent(this, AlarmReceiver.class);
                notifyIntent.putExtra(TASK_ID_REPLY, task.getId());
                notifyIntent.putExtra(TASK_STATUS_REPLY, "Missed");
                PendingIntent notifyPendingIntent = PendingIntent.getBroadcast
                        (this, NOTIFICATION_ID, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

                LocalDateTime deadlineDateTime = LocalDateTime.of(LocalDate.parse(deadlineString).plusDays(1), LocalTime.of(0, 0));
                long deadlineMillis = deadlineDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                mAlarmManager.set(AlarmManager.RTC_WAKEUP, deadlineMillis, notifyPendingIntent);
            }

            mActionsHistory.push(Pair.create(Pair.create(task.getId(), task.getTitle()), "Create"));
            mMenuUndoButton.setVisible(true);

        }
    }

    public void launchPhotoActivity() {
        Intent intent = new Intent(ToDoActivity.this, PhotoActivity.class);
        intent.putExtra(TASK_ID_REPLY, mSelectedTask.getId());
        intent.putExtra(TASK_STATUS_REPLY, mSelectedStatus);
        startActivityForResult(intent, PHOTO_ACTIVITY_REQUEST_CODE);
    }

    public void launchNewTaskActivity(View view) {
        Intent intent = new Intent(ToDoActivity.this, NewTaskActivity.class);
        intent.putExtra(TASK_DEADLINE_REPLY, mSelectedTab);
        startActivityForResult(intent, NEW_TASK_ACTIVITY_REQUEST_CODE);
    }

    @Override
    public void onListItemClick(View view, int index) {
        Task task = filterTasks(mTasksData, mSelectedTab).get(index);

        if (!task.getOptional() && task.daysLeft() < 0) {
            missTask(task);
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
            builder.setCancelable(true);
            builder.setMessage("This task is no longer available.");
            builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {});
        }
        else {
            mSelectedTask = task;

            String button_label = ((Button) view).getText().toString();
            if (button_label.contentEquals("Complete")) {
                mSelectedStatus = "Complete";
                launchPhotoActivity();
            } else if (button_label.contentEquals("Cancel")) {
                mSelectedStatus = "Cancelled";
                mBuilder.setCancelable(true);

                if (task.getStatus().contentEquals("Waiting")) {
                    mBuilder.setMessage("This task is set to automatically schedule itself but is not available yet. Cancel repeating task?");
                    mBuilder.setPositiveButton("Yes",
                            (dialog, which) -> cancelTask(task));
                    mBuilder.setNegativeButton("No", (dialog, which) -> {
                    });
                } else if (!task.getOptional()) {
                    mBuilder.setMessage("To cancel a task without penalty before the deadline, you may provide a reason and a photo.");
                    mBuilder.setPositiveButton("Provide Reason",
                            (dialog, which) -> launchPhotoActivity());
                    mBuilder.setNegativeButton("Fail Task (Lose Points)", (dialog, which) -> failTask(task));
                    mBuilder.setNeutralButton(android.R.string.cancel, (dialog, which) -> {
                    });
                } else {
                    mBuilder.setMessage("Are you sure you would like to cancel this task? (Won't lose points since task is optional)");
                    mBuilder.setPositiveButton("Cancel Task",
                            (dialog, which) -> cancelTask(task));
                    mBuilder.setNegativeButton("Keep Task", (dialog, which) -> {
                    });
                }

                AlertDialog dialog = mBuilder.create();
                dialog.show();
            }
        }
    }

    private void missTask(Task task) {
        mAppViewModel.markTaskResult(task.getId(), "Missed");

        if (task.getRepeatInterval() > 0) {
            String new_deadline = LocalDate.parse(task.getDeadline()).plusDays(task.getRepeatInterval()).toString();
            Task new_task = new Task.Builder(UUID.randomUUID().toString(), task.getTitle(), task.getStat(), task.getValue(), task.getKeepPrivate())
                    .description(task.getDescription())
                    .deadline(new_deadline, task.getPenalty())
                    .status("To Do")
                    .repeatInterval(task.getRepeatInterval())
                    .build();

            try {
                mAppViewModel.insert(new_task);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void failTask(Task task) {
        mAppViewModel.markTaskResult(task.getId(), "Missed");

        mActionsHistory.push(Pair.create(Pair.create(task.getId(), task.getTitle()), "Fail"));
        mMenuUndoButton.setVisible(true);

        if (task.getRepeatInterval() > 0) {
            String new_deadline = LocalDate.parse(task.getDeadline()).plusDays(task.getRepeatInterval()).toString();
            Task new_task = new Task.Builder(UUID.randomUUID().toString(), task.getTitle(), task.getStat(), task.getValue(), task.getKeepPrivate())
                    .description(task.getDescription())
                    .deadline(new_deadline, task.getPenalty())
                    .status("Waiting")
                    .repeatInterval(task.getRepeatInterval())
                    .build();

            try {
                mAppViewModel.insert(new_task);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void cancelTask(Task task) {
        mAppViewModel.markTaskResult(task.getId(), "Cancelled");

        mActionsHistory.push(Pair.create(Pair.create(task.getId(), task.getTitle()), "Cancel"));
        mMenuUndoButton.setVisible(true);

        if (task.getStatus().contentEquals("To Do") && task.getRepeatInterval() > 0) {
            String new_deadline = LocalDate.parse(task.getDeadline()).plusDays(task.getRepeatInterval()).toString();
            Task new_task = new Task.Builder(UUID.randomUUID().toString(), task.getTitle(), task.getStat(), task.getValue(), task.getKeepPrivate())
                    .description(task.getDescription())
                    .deadline(new_deadline, task.getPenalty())
                    .status("Waiting")
                    .repeatInterval(task.getRepeatInterval())
                    .build();

            try {
                mAppViewModel.insert(new_task);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void completeTask(Task task) {
        mAppViewModel.markTaskResult(task.getId(), "Complete");
        String toastMessage = "Your " + task.getStat();
        if (task.getValue() <= 2) {
            toastMessage += " increased a bit!";
        }
        else if (task.getValue() >= 5) {
            toastMessage += " greatly increased!";
        }
        else {
            toastMessage += " increased!";
        }

        Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();

        mActionsHistory.push(Pair.create(Pair.create(task.getId(), task.getTitle()), "Complete"));
        mMenuUndoButton.setVisible(true);

        if (task.getRepeatInterval() > 0) {
            String new_deadline = LocalDate.parse(task.getDeadline()).plusDays(task.getRepeatInterval()).toString();
            Task new_task = new Task.Builder(UUID.randomUUID().toString(), task.getTitle(), task.getStat(), task.getValue(), task.getKeepPrivate())
                    .description(task.getDescription())
                    .deadline(new_deadline, task.getPenalty())
                    .status("Waiting")
                    .repeatInterval(task.getRepeatInterval())
                    .build();

            try {
                mAppViewModel.insert(new_task);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}