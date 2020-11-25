package com.example.mystats.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.example.mystats.R;
import com.example.mystats.database.Stat;
import com.example.mystats.database.AppViewModel;
import com.example.mystats.utility.OneShotClickListener;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class MainActivity extends AppCompatActivity {
    private static final int MORNING_START_HOUR = 6;
    private static final int EVENING_START_HOUR = 17;
    private static final int NIGHT_START_HOUR = 20;

    private int mButtonResource;
    private int mChartWebColor;
    private int mChartFillColor;
    private int mChartFillAlpha;
    private CountDownTimer mTimer;
    private int mCountdownTextColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocalTime currentTime = LocalTime.now();
        int hour = currentTime.getHour();
        String themeChoice = "morning";
        if (hour >= NIGHT_START_HOUR || hour <= MORNING_START_HOUR) {
            themeChoice = "night";
        } else if (hour >= EVENING_START_HOUR) {
            themeChoice = "evening";
        }

        SharedPreferences sharedPref = getSharedPreferences("Settings", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("theme", themeChoice);
        editor.apply();

        int chart_size = sharedPref.getInt("chart_size", 100);
        int maxStatValue = sharedPref.getInt("max_stat_value", 30);
        switch (sharedPref.getString("theme", "morning")) {
            case "morning":
                setTheme(R.style.MorningTheme);
                mButtonResource = R.drawable.positive_button_morning;
                mChartWebColor = Color.BLACK;
                mChartFillColor = Color.WHITE;
                mChartFillAlpha = 225;
                mCountdownTextColor = Color.WHITE;
                break;
            case "evening":
                setTheme(R.style.EveningTheme);
                mButtonResource = R.drawable.positive_button_evening;
                mChartWebColor = Color.BLACK;
                mChartFillColor = getResources().getColor(R.color.chartFillColorEvening);
                mChartFillAlpha = 225;
                mCountdownTextColor = getResources().getColor(R.color.chartFillColorEvening);
                break;
            case "night":
                setTheme(R.style.NightTheme);
                mButtonResource = R.drawable.positive_button_night;
                mChartWebColor = getResources().getColor(R.color.webColorNight);
                mChartFillColor = Color.WHITE;
                mChartFillAlpha = 225;
                mCountdownTextColor = Color.WHITE;
                break;
            default:
                break;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView countdownView = findViewById(R.id.countdown_view);
        LocalDateTime midnight = LocalDate.now().plusDays(1).atStartOfDay();
        long secondsLeftTillMidnight = Duration.between(LocalDateTime.now(), midnight).getSeconds();
        mTimer = new CountDownTimer(secondsLeftTillMidnight * 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                String countdownText = "Time Remaining in Day: \n";
                long hours = millisUntilFinished / 1000 / 60 / 60;
                if (hours > 0) {
                    countdownText += hours + " Hour";
                    if (hours > 1) {
                        countdownText += "s";
                    }
                    countdownText += " and ";
                }
                long minutes = (millisUntilFinished / 1000 / 60 % 60 + 1);
                countdownText += minutes + " Minute";
                if (minutes > 1) {
                    countdownText += "s";
                }
                countdownView.setText(countdownText);
            }
            public void onFinish() {
                mTimer.start();
            }
        }.start();

        RadarChart chart = findViewById(R.id.radar_chart);
        chart.setWebLineWidth(1f);
        chart.setWebColor(mChartWebColor);
        chart.setWebLineWidthInner(1f);
        chart.setWebColorInner(mChartWebColor);
        chart.setWebAlpha(200);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.animateXY(700, 700, Easing.EaseInCirc);
        chart.setScaleX((float) chart_size / 100);
        chart.setScaleY((float) chart_size / 100);

        XAxis xAxis = chart.getXAxis();
        xAxis.setTextSize(18);
        xAxis.setTextColor(mChartWebColor);

        YAxis yAxis = chart.getYAxis();
        yAxis.setLabelCount(0, true);
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisMaximum((float) maxStatValue);
        yAxis.setDrawLabels(false);

        AppViewModel mAppViewModel = ViewModelProviders.of(this).get(AppViewModel.class);
        ViewModelProviders.of(this).get(AppViewModel.class).deleteOldTasks();
        mAppViewModel.getAllStats().observe(this, stats -> {
            ArrayList<String> labels = new ArrayList<>();
            List<RadarEntry> entries = new ArrayList<>();
            for (Stat stat : stats) {
                labels.add(stat.getStat());
                int scaled_value = (int) Math.round(stat.getValue() / Double.parseDouble(stat.getScaling()));
                entries.add(new RadarEntry(min(max(1, scaled_value), maxStatValue)));
            }
            chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
            RadarDataSet statsSet = new RadarDataSet(entries, "RadarDataSet");
            statsSet.setDrawFilled(true);
            statsSet.setDrawValues(false);
            statsSet.setColor(mChartFillColor);
            statsSet.setFillColor(mChartFillColor);
            statsSet.setFillAlpha(mChartFillAlpha);
            statsSet.setLineWidth(0f);
            chart.setData(new RadarData(statsSet));
            chart.invalidate();
        });

        Button toDoButton = findViewById(R.id.button_todo);
        Button taskHistoryButton = findViewById(R.id.button_task_history);
        Button statsSettingsButton = findViewById(R.id.stats_settings);

        toDoButton.setBackgroundResource(mButtonResource);
        taskHistoryButton.setBackgroundResource(mButtonResource);
        statsSettingsButton.setBackgroundResource(mButtonResource);
        countdownView.setTextColor(mCountdownTextColor);

        toDoButton.setOnClickListener(new OneShotClickListener() {
            @Override
            public void onClicked(View view) {
                launchToDo(view);
            }
        });
        taskHistoryButton.setOnClickListener(new OneShotClickListener() {
            @Override
            public void onClicked(View view) {
                launchTaskHistory(view);
            }
        });
        statsSettingsButton.setOnClickListener(new OneShotClickListener() {
            @Override
            public void onClicked(View view) {
                launchStatsSettings(view);
            }
        });
    }

    public void launchToDo(View view) {
        startActivity(new Intent(this, ToDoActivity.class));
    }

    public void launchTaskHistory(View view) {
        startActivity(new Intent(this, TasksHistoryActivity.class));
    }

    public void launchStatsSettings(View view) {
        startActivity(new Intent(this, StatsSettingsActivity.class));
    }
}
