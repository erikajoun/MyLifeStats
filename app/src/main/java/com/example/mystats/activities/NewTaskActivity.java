package com.example.mystats.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.example.mystats.R;
import com.example.mystats.database.Stat;
import com.example.mystats.database.AppViewModel;
import com.example.mystats.taskform.NewTaskForm;
import com.example.mystats.utility.StringUtility;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

public class NewTaskActivity extends AppCompatActivity {
    public static final String TITLE_REPLY = "com.example.mystats.REPLY";
    public static final String STATS_REPLY = "com.example.mystats.STATS_REPLY";
    public static final String VALUE_REPLY = "com.example.mystats.VALUE_REPLY";
    public static final String DUEDATE_REPLY = "com.example.mystats.DUEDATE_REPLY";
    public static final String DESCRIPTION_REPLY = "com.example.mystats.DESCRIPTION_REPLY";
    public static final String PENALTY_REPLY = "com.example.mystats.PENALTY_REPLY";
    public static final String REPEAT_REPLY = "com.example.mystats.REPEAT_REPLY";
    public static final String KEEPPRIVATE_REPLY = "com.example.mystats.KEEPPRIVATE_REPLY";
    public static final String SELECTEDTAB_REPLY = "com.example.mystats.SELECTEDTAB_REPLY";

    private String mDueDateString;
    private int mRepeatInterval;

    private String mSelectedTab;

    private NewTaskForm mNewTaskForm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        String theme = getSharedPreferences("Settings", MODE_PRIVATE).getString("theme", "morning");
        switch (theme) {
            case "morning":
                setTheme(R.style.MorningTheme);
                break;
            case "evening":
                setTheme(R.style.EveningTheme);
                break;
            case "night":
                setTheme(R.style.NightTheme);
                break;
            default:
                break;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);

        AppViewModel mAppViewModel = ViewModelProviders.of(this).get(AppViewModel.class);
        mAppViewModel.getAllStats().observe(this, stats -> {
            ArrayList<String> statNames = new ArrayList<>();
            for (Stat stat : stats) {
                statNames.add(stat.getStat());
            }
            mNewTaskForm = new NewTaskForm();
            mNewTaskForm.createForm(NewTaskActivity.this, theme, statNames);

            HashMap<String, Integer> deadlineMapping = new HashMap();
            deadlineMapping.put("Today", 0);
            deadlineMapping.put("Tomorrow", 1);
            deadlineMapping.put("No Deadline", 2);
            deadlineMapping.put("Later", 3);

            final Intent intent = getIntent();
            mSelectedTab = intent.getStringExtra(ToDoActivity.TASK_DEADLINE_REPLY);
            mNewTaskForm.checkDateRadioGroup(deadlineMapping.get(mSelectedTab));

            mNewTaskForm.getButton().setOnClickListener(view -> {
                Intent replyIntent = new Intent();
                String date_choice = mNewTaskForm.getDueDateChoice();
                if (date_choice.contentEquals("Today")) {
                    mSelectedTab = "Today";
                    mDueDateString = LocalDate.now().toString();
                } else if (date_choice.contentEquals("Tomorrow")) {
                    mSelectedTab = "Tomorrow";
                    mDueDateString = LocalDate.now().plusDays(1).toString();
                } else if (date_choice.contentEquals("A Specific Date")) {
                    mSelectedTab = "Later";
                    DatePicker datePicker = mNewTaskForm.getDatePicker();
                    mDueDateString = LocalDate.of(datePicker.getYear(), datePicker.getMonth() + 1,
                            datePicker.getDayOfMonth()).toString();
                } else if (date_choice.contentEquals("No Deadline (Optional Task)")) {
                    mSelectedTab = "No Deadline";
                    mDueDateString = "";
                }

                switch (mNewTaskForm.getRepeatIntervalChoice()) {
                    case "Daily":
                        mRepeatInterval = 1;
                        break;
                    case "Weekly":
                        mRepeatInterval = 7;
                        break;
                    case "Other Interval":
                        mRepeatInterval = mNewTaskForm.getRepeatInterval();
                        break;
                    default:
                        mRepeatInterval = 0;
                        break;
                }

                replyIntent.putExtra(TITLE_REPLY, StringUtility.cleanString(mNewTaskForm.getTitle()));
                replyIntent.putExtra(DESCRIPTION_REPLY, StringUtility.cleanString(mNewTaskForm.getDescription()));
                replyIntent.putExtra(STATS_REPLY, mNewTaskForm.getStat());
                replyIntent.putExtra(VALUE_REPLY, mNewTaskForm.getValue());
                replyIntent.putExtra(DUEDATE_REPLY, mDueDateString);
                replyIntent.putExtra(PENALTY_REPLY, mNewTaskForm.getPenalty());
                replyIntent.putExtra(REPEAT_REPLY, mRepeatInterval);
                replyIntent.putExtra(KEEPPRIVATE_REPLY, mNewTaskForm.getKeepPrivate());
                replyIntent.putExtra(SELECTEDTAB_REPLY, mSelectedTab);

                setResult(RESULT_OK, replyIntent);

                finish();
            });
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}