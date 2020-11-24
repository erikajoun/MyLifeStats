package com.example.mystats.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "stat_table", indices = {@Index(value = {"stat"}, unique = true)})
public class Stat {
    @NonNull
    @PrimaryKey
    private final String id;

    @NonNull
    @ColumnInfo(name = "sort_order")
    private long order;

    @NonNull
    @ColumnInfo(name = "stat")
    private String stat;

    @NonNull
    @ColumnInfo(name = "value")
    private int value;

    @NonNull
    @ColumnInfo(name = "scaling")
    private String scaling;

    public Stat(@NonNull String id, @NonNull String stat, @NonNull int value) {
        this.id = id;
        this.stat = stat;
        this.value = value;
        this.scaling = "1";
        this.order = System.nanoTime();
    }

    public String getId() { return this.id; }

    public void setOrder(long order) { this.order = order; }

    public long getOrder() { return this.order; }

    public String getStat() { return this.stat; }

    public int getValue() { return this.value; }

    public String getScaling() { return this.scaling; }

    public void setScaling(String scaling) { this.scaling = scaling; }

    public String getValueAsString() { return Integer.toString(this.value); }
}

