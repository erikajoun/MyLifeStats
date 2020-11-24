package com.example.mystats.taskform.views;

import android.widget.DatePicker;
import android.widget.RadioGroup;

public class DateView {
    private RadioGroup mDateRadioGroup;
    private DatePicker mDatePicker;

    public DateView(RadioGroup mDateRadioGroup, DatePicker mDatePicker) {
        this.mDateRadioGroup = mDateRadioGroup;
        this.mDatePicker = mDatePicker;
    }

    public RadioGroup getDateRadioGroup() {
        return mDateRadioGroup;
    }

    public DatePicker getDatePicker() {
        return mDatePicker;
    }

    public boolean hasSelection() {
        return getDateRadioGroup().getCheckedRadioButtonId() != -1;
    }
}
