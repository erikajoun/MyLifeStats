package com.example.mystats.taskform.views;

import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class RepeatView {
    private TextView mRepeatTextView;
    private Spinner mRepeatSpinner;
    private TextView mCustomIntervalTextView;
    private EditText mCustomIntervalEditText;

    public RepeatView(TextView mRepeatTextView, Spinner mRepeatSpinner, TextView mCustomIntervalTextView, EditText mCustomIntervalEditText) {
        this.mRepeatTextView = mRepeatTextView;
        this.mRepeatSpinner = mRepeatSpinner;
        this.mCustomIntervalTextView = mCustomIntervalTextView;
        this.mCustomIntervalEditText = mCustomIntervalEditText;
    }

    public Spinner getRepeatSpinner() {
        return mRepeatSpinner;
    }

    public void setRepeatVisibility(int visibility) {
        mRepeatTextView.setVisibility(visibility);
        mRepeatSpinner.setVisibility(visibility);
    }

    public void setCustomIntervalVisibility(int visibility) {
        mCustomIntervalTextView.setVisibility(visibility);
        mCustomIntervalEditText.setVisibility(visibility);
    }
}
