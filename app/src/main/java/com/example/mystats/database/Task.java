package com.example.mystats.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity(tableName = "task_table")
public class Task {
    @NonNull
    @PrimaryKey
    private final String id;

    @NonNull
    @ColumnInfo(name = "datetime_created")
    private String datetimeCreated;

    @NonNull
    @ColumnInfo(name = "title")
    private final String title;

    @ColumnInfo(name = "Description")
    private String description = "";

    @NonNull
    @ColumnInfo(name = "stat")
    private final String stat;

    @ColumnInfo(name = "value")
    private final int value;

    @ColumnInfo(name = "penalty")
    private int penalty = 0;

    @ColumnInfo(name = "deadline")
    private String deadline = "";

    @ColumnInfo(name = "repeat_interval")
    private int repeatInterval = -1;

    @ColumnInfo(name = "keep_private")
    private final boolean keepPrivate;

    @NonNull
    @ColumnInfo(name = "status")
    private String status = "To Do";

    @ColumnInfo(name = "datetime_removed")
    private String datetimeRemoved = "";

    @ColumnInfo(name = "explanation")
    private String explanation = "";

    @ColumnInfo(name = "photo_rotation")
    private int photoRotation = 0;

    public Task(@NonNull String id, @NonNull String title, @NonNull String stat, int value, boolean keepPrivate) {
        this.id = id;
        this.datetimeCreated = LocalDateTime.now().toString();
        this.title = title;
        this.stat = stat;
        this.value = value;
        this.keepPrivate = keepPrivate;
    }

    public static class Builder {
        Task task;

        public Builder(@NonNull String id, @NonNull String title, @NonNull String stat, int value, boolean keepPrivate) {
            task = new Task(id, title, stat, value, keepPrivate);
        }

        public Builder description(String description) {
            task.description = description;
            return this;
        }

        public Builder deadline(String deadline, int penalty) {
            task.deadline = deadline;
            task.penalty = penalty;
            return this;
        }

        public Builder repeatInterval(int repeatInterval) {
            task.repeatInterval = repeatInterval;
            return this;
        }

        public Builder status(String status) {
            task.status = status;
            return this;
        }

        public Builder datetimeRemoved(String datetimeRemoved) {
            task.datetimeRemoved = datetimeRemoved;
            return this;
        }

        public Builder explanation(String explanation) {
            task.explanation = explanation;
            return this;
        }

        public Builder photoRotation(int photoRotation) {
            task.photoRotation = photoRotation;
            return this;
        }

        public Task build() {
            return task;
        }
    }

    public String getId() {
        return this.id;
    }

    public String getDatetimeCreated() {
        return this.datetimeCreated;
    }

    public void setDatetimeCreated(String datetimeCreated) {
        this.datetimeCreated = datetimeCreated;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStat() {
        return this.stat;
    }

    public int getValue() {
        return this.value;
    }

    public int getPenalty() {
        return this.penalty;
    }

    public void setPenalty(int penalty) {
        this.penalty = penalty;
    }

    public String getDeadline() {
        return this.deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public int getRepeatInterval() {
        return this.repeatInterval;
    }

    public void setRepeatInterval(int repeat_interval) {
        this.repeatInterval = repeat_interval;
    }

    public boolean getKeepPrivate() {
        return this.keepPrivate;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDatetimeRemoved() {
        return this.datetimeRemoved;
    }

    public String getDateRemoved() {
        return this.datetimeRemoved.substring(0, datetimeRemoved.indexOf("T"));
    }

    public void setDatetimeRemoved(String datetimeRemoved) {
        this.datetimeRemoved = datetimeRemoved;
    }

    public String getExplanation() {
        return this.explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public int getPhotoRotation() {
        return this.photoRotation;
    }

    public void setPhotoRotation(int photoRotation) {
        this.photoRotation = photoRotation;
    }

    public boolean getOptional() {
        return this.penalty == 0 || this.deadline.isEmpty();
    }

    public boolean hasDeadline() {
        return !this.deadline.isEmpty();
    }

    public long daysLeft() {
        if (deadline.isEmpty()) {
            throw new java.lang.RuntimeException("daysLeft() is being called on optional task");
        }
        LocalDate currentDate = LocalDate.now();
        long noOfDaysBetween = ChronoUnit.DAYS.between(currentDate, LocalDate.parse(deadline));
        return noOfDaysBetween;
    }

    public boolean isAvailable() {
        if (repeatInterval == -1) {
            return true;
        }
        return this.daysLeft() + 1 <= this.repeatInterval;
    }
}