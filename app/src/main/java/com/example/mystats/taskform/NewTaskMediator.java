package com.example.mystats.taskform;

import android.app.Activity;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.example.mystats.taskform.views.ButtonView;
import com.example.mystats.taskform.views.DateView;
import com.example.mystats.taskform.views.PenaltyView;
import com.example.mystats.taskform.views.RepeatView;


public class NewTaskMediator implements Mediator {
    private Activity _activity;
    private EditText mTitleEditText;
    private RadioGroup mValueRadioGroup;
    private PenaltyView penaltyView;
    private DateView dateView;
    private RepeatView repeatView;
    private ButtonView buttonView;

    public static class Builder {
        NewTaskMediator newTaskMediator = new NewTaskMediator();

        public Builder activity(Activity _activity) {
            newTaskMediator._activity = _activity;
            return this;
        }

        public Builder titleEditText(EditText mTitleEditText) {
            newTaskMediator.mTitleEditText = mTitleEditText;
            return this;
        }

        public Builder valueRadioGroup(RadioGroup mValueRadioGroup) {
            newTaskMediator.mValueRadioGroup = mValueRadioGroup;
            return this;
        }

        public Builder penaltyView(PenaltyView penaltyView) {
            newTaskMediator.penaltyView = penaltyView;
            return this;
        }

        public Builder dateView(DateView dateView) {
            newTaskMediator.dateView = dateView;
            return this;
        }

        public Builder repeatView(RepeatView repeatView) {
            newTaskMediator.repeatView = repeatView;
            return this;
        }

        public Builder buttonView(ButtonView buttonView) {
            newTaskMediator.buttonView = buttonView;
            return this;
        }

        public NewTaskMediator build() {
            return newTaskMediator;
        }
    }

    public void notifyMediator(Object sender) {
        if (sender == repeatView.getRepeatSpinner()) {
            if (repeatView.getRepeatSpinner().getSelectedItem().toString().contentEquals("Other Interval")) {
                repeatView.setCustomIntervalVisibility(View.VISIBLE);
            } else {
                repeatView.setCustomIntervalVisibility(View.GONE);
            }
        }

        if (sender == dateView.getDateRadioGroup()) {
            String selection = ((RadioButton) _activity.findViewById(dateView.getDateRadioGroup().getCheckedRadioButtonId())).getText().toString();
            if (selection.contentEquals("A Specific Date")) {
                dateView.getDatePicker().setVisibility(View.VISIBLE);
            } else {
                dateView.getDatePicker().setVisibility(View.GONE);
            }

            if (selection.contentEquals("No Deadline (Optional Task)")) {
                repeatView.setRepeatVisibility(View.GONE);
                penaltyView.setVisibility(View.GONE);
                ((RadioButton) penaltyView.getPenaltyRadioGroup().getChildAt(0)).setChecked(true);
            } else {
                repeatView.setRepeatVisibility(View.VISIBLE);
                penaltyView.setVisibility(View.VISIBLE);
                penaltyView.getPenaltyRadioGroup().clearCheck();
            }

            if (selection.contentEquals("Later")) {
                dateView.getDatePicker().setVisibility(View.VISIBLE);
            }
        }

        if (dateView.hasSelection() && mValueRadioGroup.getCheckedRadioButtonId() != -1 &&
                penaltyView.hasSelection() && !mTitleEditText.getText().toString().trim().isEmpty()) {
            buttonView.setEnabled(true);
        } else {
            buttonView.setEnabled(false);
        }
    }
}
