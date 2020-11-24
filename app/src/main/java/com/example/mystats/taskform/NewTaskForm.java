package com.example.mystats.taskform;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.core.content.ContextCompat;
import androidx.core.widget.CompoundButtonCompat;

import com.example.mystats.R;
import com.example.mystats.taskform.views.ButtonView;
import com.example.mystats.taskform.views.DateView;
import com.example.mystats.taskform.views.PenaltyView;
import com.example.mystats.taskform.views.RepeatView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

public class NewTaskForm {
    private EditText mTitleEditText;
    private EditText mDescEditText;
    private Spinner mStatSpinner;
    private Spinner mRepeatSpinner;
    private RadioGroup mValueRadioGroup;
    private RadioGroup mPenaltyRadioGroup;
    private RadioGroup mDateRadioGroup;
    private DatePicker mDatePicker;
    private EditText mCustomIntervalEditText;
    private CheckBox mKeepPrivateCheckBox;
    private Button button;

    private int mButtonResource;
    private int mButtonEnabledTextColor;
    private int mEditTextColor;
    private int mCalendarStyle;
    private int mSpinnerPopupColor;
    private int mRadioButtonTintColor;

    private Activity mActivity;
    private Mediator mMediator;

    private LinkedHashMap<String, Integer> mValueMapping;
    private LinkedHashMap<String, Integer> mPenaltyMapping;

    public void createForm(Activity activity, String theme, ArrayList<String> statNames) {
        this.mActivity = activity;

        switch (theme) {
            case "morning":
                mButtonResource = R.drawable.positive_button_morning;
                mButtonEnabledTextColor = Color.BLACK;
                mEditTextColor = Color.BLACK;
                mRadioButtonTintColor = R.color.colorBlack;
                mCalendarStyle = R.style.MorningCalendar;
                mSpinnerPopupColor = activity.getResources().getColor(R.color.colorSpinnerPopupMorning);
                break;
            case "evening":
                mButtonResource = R.drawable.positive_button_evening;
                mButtonEnabledTextColor = Color.BLACK;
                mEditTextColor = Color.BLACK;
                mRadioButtonTintColor = R.color.colorBlack;
                mCalendarStyle = R.style.EveningCalendar;
                mSpinnerPopupColor = activity.getResources().getColor(R.color.colorSpinnerPopupEvening);
                break;
            case "night":
                mButtonResource = R.drawable.positive_button_night;
                mButtonEnabledTextColor = Color.WHITE;
                mEditTextColor = Color.WHITE;
                mRadioButtonTintColor = R.color.colorWhite;
                mCalendarStyle = R.style.NightCalendar;
                mSpinnerPopupColor = activity.getResources().getColor(R.color.colorSpinnerPopupNight);
                break;
            default:
                break;
        }

        TextView penaltyTextView = activity.findViewById(R.id.penalty_textview);
        TextView repeatTextView = activity.findViewById(R.id.repeat_textview);
        TextView customIntervalTextView = activity.findViewById(R.id.custom_interval_textview);

        mTitleEditText = activity.findViewById(R.id.edit_title);
        mDescEditText = activity.findViewById(R.id.edit_desc);
        mCustomIntervalEditText = activity.findViewById(R.id.custom_interval_edittext);
        mCustomIntervalEditText.setTransformationMethod(null);

        mTitleEditText.setTextColor(mEditTextColor);
        mDescEditText.setTextColor(mEditTextColor);
        mCustomIntervalEditText.setTextColor(mEditTextColor);

        mDatePicker = new DatePicker(new ContextThemeWrapper(activity, mCalendarStyle));
        mDatePicker.setBackgroundColor(Color.parseColor("#FFFFFF"));
        mDatePicker.setVisibility(View.GONE);
        mDatePicker.setMinDate(System.currentTimeMillis() - 1000);

        LinearLayout linearLayoutInner = activity.findViewById(R.id.linear_layout_inner);
        linearLayoutInner.addView(mDatePicker, 6,
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        mStatSpinner = activity.findViewById(R.id.spinner);
        ArrayAdapter<String> stat_adapter = new MySpinnerAdapter(activity.getApplicationContext(),
                android.R.layout.simple_spinner_item, statNames, mEditTextColor, 24);
        stat_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mStatSpinner.setAdapter(stat_adapter);
        mStatSpinner.invalidate();
        mStatSpinner.setBackgroundColor(activity.getResources().getColor(R.color.colorField));
        mStatSpinner.getPopupBackground().setColorFilter(mSpinnerPopupColor, PorterDuff.Mode.SRC_ATOP);

        mRepeatSpinner = activity.findViewById(R.id.repeat_spinner);
        String[] repeatable_options = {"No Repeat", "Daily", "Weekly", "Other Interval"};
        ArrayAdapter<String> repeat_adapter = new MySpinnerAdapter(activity.getApplicationContext(),
                android.R.layout.simple_spinner_item, Arrays.asList(repeatable_options), mEditTextColor, 18);
        mRepeatSpinner.setAdapter(repeat_adapter);
        mRepeatSpinner.invalidate();
        mRepeatSpinner.setBackgroundColor(activity.getResources().getColor(R.color.colorField));
        mRepeatSpinner.getPopupBackground().setColorFilter(mSpinnerPopupColor, PorterDuff.Mode.SRC_ATOP);

        mValueRadioGroup = activity.findViewById(R.id.value_radiogroup);
        mPenaltyRadioGroup = activity.findViewById(R.id.penalty_radiogroup);
        mDateRadioGroup = activity.findViewById(R.id.date_radiogroup);

        mValueMapping = new LinkedHashMap();
        mValueMapping.put("Easy (+1)", 1);
        mValueMapping.put("Medium (+3)", 3);
        mValueMapping.put("Hard (+5)", 5);

        mPenaltyMapping = new LinkedHashMap();
        mPenaltyMapping.put("Optional (-0)", 0);
        mPenaltyMapping.put("Low (-1)", 1);
        mPenaltyMapping.put("Medium (-3)", 3);
        mPenaltyMapping.put("High (-5)", 5);

        for (int i = 0; i < mValueMapping.size(); i++) {
            String choiceText = (String) mValueMapping.keySet().toArray()[i];
            AppCompatRadioButton radioButton = new AppCompatRadioButton(activity);
            CompoundButtonCompat.setButtonTintList(radioButton, ContextCompat.getColorStateList(activity, mRadioButtonTintColor));
            radioButton.setText(choiceText);
            radioButton.setTextSize(20);
            radioButton.setTextColor(mEditTextColor);
            radioButton.setId(i + 1);
            mValueRadioGroup.addView(radioButton);
        }

        for (int i = 0; i < mPenaltyMapping.size(); i++) {
            String choiceText = (String) mPenaltyMapping.keySet().toArray()[i];
            AppCompatRadioButton radioButton = new AppCompatRadioButton(activity);
            CompoundButtonCompat.setButtonTintList(radioButton, ContextCompat.getColorStateList(activity, mRadioButtonTintColor));
            radioButton.setText(choiceText);
            radioButton.setTextSize(20);
            radioButton.setTextColor(mEditTextColor);
            radioButton.setId(i + mValueMapping.size() + 1);
            mPenaltyRadioGroup.addView(radioButton);
        }

        String[] date_choices = {"Today", "Tomorrow", "No Deadline (Optional Task)", "A Specific Date"};
        for (int i = 0; i < date_choices.length; i++) {
            String choiceText = date_choices[i];
            AppCompatRadioButton radioButton = new AppCompatRadioButton(activity);
            CompoundButtonCompat.setButtonTintList(radioButton, ContextCompat.getColorStateList(activity, mRadioButtonTintColor));
            radioButton.setText(choiceText);
            radioButton.setTextSize(20);
            radioButton.setTextColor(mEditTextColor);
            radioButton.setId(i + mPenaltyMapping.size() + mValueMapping.size() + 1);
            mDateRadioGroup.addView(radioButton);
        }

        mKeepPrivateCheckBox = activity.findViewById(R.id.keepprivate_checkbox);
        CompoundButtonCompat.setButtonTintList(mKeepPrivateCheckBox, ContextCompat.getColorStateList(activity, mRadioButtonTintColor));

        button = activity.findViewById(R.id.button_save);
        button.setBackgroundResource(mButtonResource);
        button.setEnabled(false);
        button.setTextColor(Color.GRAY);

        this.mMediator = new NewTaskMediator.Builder()
                .activity(activity)
                .titleEditText(mTitleEditText)
                .valueRadioGroup(mValueRadioGroup)
                .dateView(new DateView(mDateRadioGroup, mDatePicker))
                .penaltyView(new PenaltyView(penaltyTextView, mPenaltyRadioGroup))
                .repeatView(new RepeatView(repeatTextView, mRepeatSpinner, customIntervalTextView, mCustomIntervalEditText))
                .buttonView(new ButtonView(button, mButtonEnabledTextColor))
                .build();

        mRepeatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                mMediator.notifyMediator(mRepeatSpinner);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
        mDateRadioGroup.setOnCheckedChangeListener((group, checkedId) -> mMediator.notifyMediator(mDateRadioGroup));
        mValueRadioGroup.setOnCheckedChangeListener((group, checkedId) -> mMediator.notifyMediator(mValueRadioGroup));
        mPenaltyRadioGroup.setOnCheckedChangeListener((group, checkedId) -> mMediator.notifyMediator(mPenaltyRadioGroup));
        mTitleEditText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mMediator.notifyMediator(mTitleEditText);
            }
        });
    }

    public String getTitle() {
        return mTitleEditText.getText().toString();
    }

    public String getDescription() {
        return mDescEditText.getText().toString();
    }

    public String getStat() {
        return mStatSpinner.getSelectedItem().toString();
    }

    public String getValueChoice() {
        return ((RadioButton) mActivity.findViewById(mValueRadioGroup.getCheckedRadioButtonId())).getText().toString();
    }

    public String getPenaltyChoice() {
        return ((RadioButton) mActivity.findViewById(mPenaltyRadioGroup.getCheckedRadioButtonId())).getText().toString();
    }

    public String getDueDateChoice() {
        return ((RadioButton) mActivity.findViewById(mDateRadioGroup.getCheckedRadioButtonId())).getText().toString();
    }

    public String getRepeatIntervalChoice() {
        return mRepeatSpinner.getSelectedItem().toString();
    }

    public int getRepeatInterval() {
        return Integer.parseInt(mCustomIntervalEditText.getText().toString());
    }

    public boolean getKeepPrivate() {
        return mKeepPrivateCheckBox.isChecked();
    }

    public int getValue() {
        return mValueMapping.get(getValueChoice());
    }

    public int getPenalty() {
        return mPenaltyMapping.get(getPenaltyChoice());
    }

    public Button getButton() {
        return button;
    }

    public DatePicker getDatePicker() {
        return mDatePicker;
    }

    public void checkDateRadioGroup(int index) {
        mDateRadioGroup.check(mValueMapping.size() + mPenaltyMapping.size() + index + 1);
        mMediator.notifyMediator(mDateRadioGroup);
    }

    private static class MySpinnerAdapter extends ArrayAdapter<String> {
        private int textColor;
        private int fontSize;

        private MySpinnerAdapter(Context context, int resource, List<String> items, int textColor, int fontSize) {
            super(context, resource, items);
            this.textColor = textColor;
            this.fontSize = fontSize;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView view = (TextView) super.getView(position, convertView, parent);
            view.setTextColor(textColor);
            view.setTextSize(fontSize);
            return view;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            TextView view = (TextView) super.getDropDownView(position, convertView, parent);
            view.setTextColor(textColor);
            return view;
        }
    }
}
