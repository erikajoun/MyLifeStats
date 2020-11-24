package com.example.mystats.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mystats.R;
import com.example.mystats.adapters.StatsListAdapter;
import com.example.mystats.database.Stat;
import com.example.mystats.database.AppViewModel;
import com.example.mystats.utility.OneShotClickListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class StatsSettingsActivity extends AppCompatActivity implements StatsListAdapter.ListItemClickListener {
    public static final String NEW_STAT_STATE_MESSAGE = "com.example.mystats.extra.NEW_STAT_STATE_MESSAGE";
    public static final String NEW_STAT_ID_MESSAGE = "com.example.mystats.extra.NEW_STAT_ID_MESSAGE";
    public static final String NEW_STAT_NAME_MESSAGE = "com.example.mystats.extra.NEW_STAT_NAME_MESSAGE";
    public static final String NEW_STAT_VALUE_MESSAGE = "com.example.mystats.extra.NEW_STAT_VALUE_MESSAGE";
    public static final String MAX_STAT_VALUE_MESSAGE = "com.example.mystats.extra.MAX_STAT_VALUE_MESSAGE";
    public static final String CHART_SIZE_MESSAGE = "com.example.mystats.extra.CHART_SIZE_MESSAGE";
    public static final String STAT_SCALING_MESSAGE = "com.example.mystats.extra.STAT_SCALING_MESSAGE";

    private static final int NEW_STAT_ACTIVITY_REQUEST_CODE = 1;
    private static final int EDIT_MAX_ACTIVITY_REQUEST_CODE = 2;

    private AppViewModel mAppViewModel;
    private List<Stat> mStatsData;
    private Stat mSelectedStat;
    private FloatingActionButton mNewStatButton;
    private Button mEditMaxButton;
    private SharedPreferences mSharedPref;
    private StatsListAdapter mAdapter;

    private int mButtonResource;
    private int mAddButtonTint;
    private Drawable mAddButtonDrawable;

    @Override
    protected void onResume() {
        super.onResume();
        mNewStatButton.setOnClickListener(new OneShotClickListener() {
            @Override
            public void onClicked(View view) {
                launchNewStatActivity(view);
            }
        });
        mEditMaxButton.setOnClickListener(new OneShotClickListener() {
            @Override
            public void onClicked(View view) {
                launchEditMaxActivity(view);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String theme = getSharedPreferences("Settings", MODE_PRIVATE).getString("theme", "morning");
        switch (theme) {
            case "morning":
                setTheme(R.style.MorningTheme);
                mButtonResource = R.drawable.positive_button_morning;
                mAddButtonTint = getResources().getColor(R.color.colorAddMorning);
                mAddButtonDrawable = getResources().getDrawable(R.drawable.gray_plus_sign);
                break;
            case "evening":
                setTheme(R.style.EveningTheme);
                mButtonResource = R.drawable.positive_button_evening;
                mAddButtonTint = getResources().getColor(R.color.colorAddEvening);
                mAddButtonDrawable = getResources().getDrawable(R.drawable.white_plus_sign);
                break;
            case "night":
                setTheme(R.style.NightTheme);
                mButtonResource = R.drawable.positive_button_night;
                mAddButtonTint = getResources().getColor(R.color.colorAddNight);
                mAddButtonDrawable = getResources().getDrawable(R.drawable.white_plus_sign);
                break;
            default:
                break;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats_settings);

        mNewStatButton = findViewById(R.id.add_stat_button);
        mNewStatButton.setBackgroundTintList(ColorStateList.valueOf(mAddButtonTint));
        mNewStatButton.setImageDrawable(mAddButtonDrawable);

        mEditMaxButton = findViewById(R.id.edit_max_button);
        mEditMaxButton.setBackgroundResource(mButtonResource);

        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        mAdapter = new StatsListAdapter(this, this, theme);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAppViewModel = ViewModelProviders.of(this).get(AppViewModel.class);

        mAppViewModel.getAllStats().observe(this, stats -> {
            mStatsData = stats;
            mAdapter.setStats(stats);
        });

        mSharedPref = getSharedPreferences("Settings", MODE_PRIVATE);
        int max = mSharedPref.getInt("max_stat_value", 30);
        mAdapter.setmMaxStatsValue(max);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == NEW_STAT_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            String state = data.getStringExtra(NewStatActivity.STATE_REPLY);
            String name = data.getStringExtra(NewStatActivity.STAT_NAME_REPLY);
            String value = data.getStringExtra(NewStatActivity.STAT_VALUE_REPLY);
            String id = data.getStringExtra(NewStatActivity.STAT_ID_REPLY);
            String scaling = data.getStringExtra(NewStatActivity.STAT_SCALING_REPLY);

            if (state.contentEquals("Add")) {
                Stat newStat = new Stat(UUID.randomUUID().toString(), name, 1);
                newStat.setScaling(scaling);
                if (mStatsData.stream()
                        .filter(stat -> stat.getStat().contentEquals(newStat.getStat())).collect(Collectors.toList()).size() > 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
                    builder.setCancelable(true);
                    builder.setTitle("Cannot Add Stat");
                    builder.setMessage("Another stat with the same name already exists.");
                    builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    mAppViewModel.insert(newStat);
                }

            } else if (state.contentEquals("Edit")) {
                Stat newStat = new Stat(id, name, Integer.parseInt(value));
                newStat.setScaling(scaling);

                if (!mSelectedStat.getStat().equals(name) && mStatsData.stream()
                        .filter(stat -> stat.getStat().contentEquals(newStat.getStat())).collect(Collectors.toList()).size() > 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
                    builder.setCancelable(true);
                    builder.setTitle("Cannot Edit Stat Name");
                    String firstStatName = mSelectedStat.getStat();
                    String secondStatName = name;
                    builder.setMessage("Another stat with the same name already exists. Would you like to swap " +
                            "the position of " + firstStatName + " and " + secondStatName + "?");
                    builder.setPositiveButton("Swap Stats", (dialog, which) -> {
                        Stat secondStat = mStatsData.stream().filter(stat -> stat.getStat().equals(name))
                                .collect(Collectors.toList()).get(0);
                        mAppViewModel.swapStats(mSelectedStat, secondStat);
                    });
                    builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    mAppViewModel.renameStatInTasks(mSelectedStat.getStat(), name);
                    mAppViewModel.update(newStat);
                }
            }
        } else if (requestCode == EDIT_MAX_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            int newMax = data.getIntExtra(ChartSettingsActivity.NEW_MAX_REPLY, 0);
            int newSize = data.getIntExtra(ChartSettingsActivity.NEW_SIZE_REPLY, 0);
            SharedPreferences.Editor editor = mSharedPref.edit();
            editor.putInt("max_stat_value", newMax);
            editor.putInt("chart_size", newSize);
            editor.apply();
            mAdapter.setmMaxStatsValue(newMax);
        }
    }

    public void launchEditMaxActivity(View view) {
        Intent intent = new Intent(StatsSettingsActivity.this, ChartSettingsActivity.class);
        int max = mSharedPref.getInt("max_stat_value", 30);
        int chart_size = mSharedPref.getInt("chart_size", 100);
        intent.putExtra(MAX_STAT_VALUE_MESSAGE, max);
        intent.putExtra(CHART_SIZE_MESSAGE, chart_size);
        startActivityForResult(intent, EDIT_MAX_ACTIVITY_REQUEST_CODE);
    }

    public void launchNewStatActivity(View view) {
        if (mStatsData.size() == 12) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
            builder.setCancelable(true);
            builder.setTitle("Cannot Add New Stat");
            builder.setMessage("The maximum number of stats is 12.");
            builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            Intent intent = new Intent(StatsSettingsActivity.this, NewStatActivity.class);
            intent.putExtra(NEW_STAT_STATE_MESSAGE, "Add");
            int max = mSharedPref.getInt("max_stat_value", 30);
            intent.putExtra(MAX_STAT_VALUE_MESSAGE, max);
            startActivityForResult(intent, NEW_STAT_ACTIVITY_REQUEST_CODE);
        }
    }

    @Override
    public void onListItemClick(View view, final int index) {
        mSelectedStat = mStatsData.get(index);

        String button_label = ((Button) view).getText().toString();
        Intent intent = new Intent(StatsSettingsActivity.this, NewStatActivity.class);

        if (button_label.contentEquals("Delete")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.MyAlertDialogStyle));
            if (mStatsData.size() <= 3) {
                builder.setCancelable(true);
                builder.setTitle("Cannot Delete Stat");
                builder.setMessage("You need at least 3 stats");
                builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                });
            } else {
                builder.setCancelable(true);
                builder.setMessage("Are you sure you want to remove the " + mSelectedStat.getStat() + " stat?" +
                        " (All tasks for this stat will also be removed.)");
                builder.setPositiveButton("Confirm",
                        (dialog, which) -> {
                            Stat stat = mSelectedStat;
                            mAppViewModel.delete(stat);
                            mAppViewModel.deleteAsyncTasksWithStat(stat.getStat());
                        });
                builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                });
            }
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            if (button_label.contentEquals("Edit")) {
                intent.putExtra(NEW_STAT_STATE_MESSAGE, "Edit");
            } else if (button_label.contentEquals("New Stat")) {
                intent.putExtra(NEW_STAT_STATE_MESSAGE, "Add");
            }

            intent.putExtra(NEW_STAT_ID_MESSAGE, mSelectedStat.getId());
            intent.putExtra(NEW_STAT_NAME_MESSAGE, mSelectedStat.getStat());
            intent.putExtra(NEW_STAT_VALUE_MESSAGE, Integer.toString(mSelectedStat.getValue()));
            int max = mSharedPref.getInt("max_stat_value", 30);
            intent.putExtra(MAX_STAT_VALUE_MESSAGE, max);
            intent.putExtra(STAT_SCALING_MESSAGE, mSelectedStat.getScaling());
            startActivityForResult(intent, NEW_STAT_ACTIVITY_REQUEST_CODE);
        }
    }
}
