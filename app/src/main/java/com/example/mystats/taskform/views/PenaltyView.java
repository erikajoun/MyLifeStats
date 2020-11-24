package com.example.mystats.taskform.views;

import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

public class PenaltyView {
    private TextView mPenaltyTextView;
    private RadioGroup mPenaltyRadioGroup;

    public PenaltyView(TextView mPenaltyTextView, RadioGroup mPenaltyRadioGroup) {
        this.mPenaltyTextView = mPenaltyTextView;
        this.mPenaltyRadioGroup = mPenaltyRadioGroup;
    }

    public RadioGroup getPenaltyRadioGroup() {
        return mPenaltyRadioGroup;
    }

    public void setVisibility(int visibility) {
        mPenaltyTextView.setVisibility(visibility);
        mPenaltyRadioGroup.setVisibility(visibility);
    }

    public boolean hasSelection() {
        return getPenaltyRadioGroup().getCheckedRadioButtonId() != -1 || mPenaltyRadioGroup.getVisibility() == View.GONE;
    }
}
