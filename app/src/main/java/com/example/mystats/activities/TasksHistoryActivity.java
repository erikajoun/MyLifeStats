package com.example.mystats.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.widget.CompoundButtonCompat;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mystats.BuildConfig;
import com.example.mystats.R;
import com.example.mystats.adapters.CompletedTaskListAdapter;
import com.example.mystats.database.Task;
import com.example.mystats.database.AppViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TasksHistoryActivity extends AppCompatActivity {
    private AppViewModel mAppViewModel;
    private List<Task> mTasksData;
    private int mCheckboxTintColor;
    private int mButtonResource;
    private int mCalendarStyle;
    private CompletedTaskListAdapter mAdapter;
    private long mFilterDateLong;
    private boolean mKeepPrivate;
    private Button mSendButton;
    private Button mFilterButton;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_data:
                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
                builder.setCancelable(true);
                builder.setTitle("Deleting Task History");
                builder.setMessage("Are you sure you would like to delete all tasks in task history?" +
                        " (Tasks are automatically deleted from history after 30 days)");
                builder.setPositiveButton("Delete Everything", (dialog, which) -> {
                    mAppViewModel.clearTaskHistory();
                    mFilterButton.setEnabled(false);
                    mFilterButton.setTextColor(Color.GRAY);
                    mSendButton.setEnabled(false);
                    mSendButton.setTextColor(Color.GRAY);

                    File path = new File(this.getFilesDir(), "MyStats" + File.separator + "Images");
                    if (path.isDirectory()) {
                        String[] children = path.list();
                        for (int i = 0; i < children.length; i++) {
                            if (children[i].substring(children[i].length() - 5).contentEquals(".jpeg")) {
                                new File(path, children[i]).delete();
                            }
                        }
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static Bitmap getBitmapFromView(View view, int totalHeight, int totalWidth) {

        int height = totalHeight;
        float percent = height / (float) totalHeight;

        Bitmap canvasBitmap = Bitmap.createBitmap((int) (totalWidth * percent), (int) (totalHeight * percent), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(canvasBitmap);

        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);

        canvas.save();
        canvas.scale(percent, percent);
        view.draw(canvas);
        canvas.restore();

        return canvasBitmap;
    }

    private List<Task> filterTasks(List<Task> tasksData, long filterDateLong, boolean keepprivate) {
        List<Task> filteredTasksData = tasksData.stream()
                .filter(task -> (keepprivate == true || task.getKeepPrivate() == false)).collect(Collectors.toList());
        if (filterDateLong != -1) {
            LocalDate filterDate = Instant.ofEpochMilli(filterDateLong).atZone(ZoneId.systemDefault()).toLocalDate();
            filteredTasksData = filteredTasksData.stream().filter(task -> task.getDateRemoved()
                    .compareTo(filterDate.toString()) >= 0).collect(Collectors.toList());
        }
        return filteredTasksData;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String theme = getSharedPreferences("Settings", MODE_PRIVATE).getString("theme", "morning");
        switch (theme) {
            case "morning":
                setTheme(R.style.MorningTheme);
                mButtonResource = R.drawable.positive_button_morning;
                mCheckboxTintColor = R.color.colorBlack;
                mCalendarStyle = R.style.MorningCalendar;
                break;
            case "evening":
                mButtonResource = R.drawable.positive_button_evening;
                setTheme(R.style.EveningTheme);
                mCheckboxTintColor = R.color.colorBlack;
                mCalendarStyle = R.style.EveningCalendar;
                break;
            case "night":
                mButtonResource = R.drawable.positive_button_night;
                setTheme(R.style.NightTheme);
                mCheckboxTintColor = R.color.colorWhite;
                mCalendarStyle = R.style.NightCalendar;
                break;
            default:
                break;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks_history);

        mSendButton = findViewById(R.id.save_button);
        mFilterButton = findViewById(R.id.filter_button);

        RecyclerView recyclerView = findViewById(R.id.recyclerview);

        mAdapter = new CompletedTaskListAdapter(this);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAppViewModel = ViewModelProviders.of(this).get(AppViewModel.class);
        final Calendar myCalendar = Calendar.getInstance();

        mKeepPrivate = false;

        DatePicker.OnDateChangedListener datePickerListener = (datePicker, year, monthOfYear, dayOfMonth) -> {
            LocalDateTime filterDateTime = LocalDateTime.of(LocalDate.of(year,
                    monthOfYear + 1, dayOfMonth), LocalTime.of(0, 0));
            SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
            mFilterDateLong = filterDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            editor.putLong("filter_date_long", mFilterDateLong);
            editor.apply();
            myCalendar.set(filterDateTime.getYear(), filterDateTime.getMonthValue(), filterDateTime.getDayOfMonth());
            mAdapter.setTasks(filterTasks(mTasksData, mFilterDateLong, mKeepPrivate));
        };

        mAppViewModel.getTaskHistory().observe(this, tasks -> {
            mTasksData = tasks;
            mFilterDateLong = getSharedPreferences("Settings", MODE_PRIVATE).getLong("filter_date_long", -1);
            if (mFilterDateLong == -1) {
                myCalendar.set(LocalDate.now().getYear(), LocalDate.now().getMonthValue(), LocalDate.now().getDayOfMonth());
            }
            else {
                LocalDate filterDate = Instant.ofEpochMilli(mFilterDateLong).atZone(ZoneId.systemDefault()).toLocalDate();
                myCalendar.set(filterDate.getYear(), filterDate.getMonthValue(), filterDate.getDayOfMonth());
            }
            if (mTasksData.isEmpty()) {
                mFilterButton.setEnabled(false);
                mFilterButton.setTextColor(Color.GRAY);
                mSendButton.setEnabled(false);
                mSendButton.setTextColor(Color.GRAY);
            }
            else {
                mAdapter.setTasks(filterTasks(mTasksData, mFilterDateLong, mKeepPrivate));
            }
        });

        CheckBox showPrivateCheckbox = findViewById(R.id.showprivate_checkbox);
        CompoundButtonCompat.setButtonTintList(showPrivateCheckbox, ContextCompat.getColorStateList(this, mCheckboxTintColor));
        showPrivateCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mKeepPrivate = isChecked;
            mAdapter.setTasks(filterTasks(mTasksData, mFilterDateLong, mKeepPrivate));
        });

        mFilterButton.setBackgroundResource(mButtonResource);
        mFilterButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
            builder.setCancelable(true);
            builder.setTitle("Filter By Date");
            String message = "Currenly showing: ";
            if (mFilterDateLong == -1) {
                message += "All Tasks (No Filter)";
            } else {
                message += "All Tasks From ";
                LocalDate filterDate = Instant.ofEpochMilli(mFilterDateLong).atZone(ZoneId.systemDefault()).toLocalDate();
                message += filterDate.toString();
            }
            builder.setMessage(message);
            builder.setPositiveButton("Change Min Date", (dialog, which) -> {
                DatePickerDialog datePickerDialog = new DatePickerDialog(TasksHistoryActivity.this, mCalendarStyle,
                        null, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH) - 1,
                        myCalendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.getDatePicker().init(myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH) - 1,
                        myCalendar.get(Calendar.DAY_OF_MONTH), datePickerListener);
                datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                datePickerDialog.show();
            });
            builder.setNegativeButton("Remove Filter", (dialog, which) -> {
                SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
                mFilterDateLong = -1;
                editor.putLong("filter_date_long", mFilterDateLong);
                editor.apply();
                myCalendar.set(LocalDate.now().getYear(), LocalDate.now().getMonthValue(), LocalDate.now().getDayOfMonth());
                mAdapter.setTasks(filterTasks(mTasksData, mFilterDateLong, mKeepPrivate));
            });
            builder.setNeutralButton(android.R.string.cancel, (dialog, which) -> {
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        });

        mSendButton.setBackgroundResource(mButtonResource);
        mSendButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
            builder.setCancelable(true);
            builder.setTitle("Send Task History");
            builder.setMessage("You may send as email what is currently shown in the task history page.");
            RecyclerView scrollView = findViewById(R.id.recyclerview);
            builder.setPositiveButton("Send", (dialog, which) -> {
                Bitmap bitmap = getBitmapFromView(scrollView, scrollView.getHeight(), scrollView.getWidth());
                try {
                    File path = new File(this.getFilesDir(), R.string.app_name + File.separator + R.string.images_dir);
                    if (!path.exists()) {
                        path.mkdirs();
                    }
                    File outFile = new File(path, "task_history.jpeg");
                    FileOutputStream outputStream = new FileOutputStream(outFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.close();

                    Intent emailIntent = new Intent(Intent.ACTION_SEND);
                    emailIntent.setType("image/*");
                    emailIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(Objects.requireNonNull(getApplicationContext()),
                            BuildConfig.APPLICATION_ID + ".fileprovider", outFile));
                    startActivity(Intent.createChooser(emailIntent, "Send your email in:"));
                } catch (IOException e) {
                    Log.e("Debug", "Saving received message failed with", e);
                }
            });
            builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }
}