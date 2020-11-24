package com.example.mystats.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mystats.R;
import com.example.mystats.utility.StringUtility;

public class NewStatActivity extends AppCompatActivity {
    public static final String STATE_REPLY = "com.example.mystats.STATE_REPLY";
    public static final String STAT_ID_REPLY = "com.example.mystats.STAT_ID_REPLY";
    public static final String STAT_NAME_REPLY = "com.example.mystats.STAT_NAME_REPLY";
    public static final String STAT_VALUE_REPLY = "com.example.mystats.STAT_VALUE_REPLY";
    public static final String STAT_SCALING_REPLY = "com.example.mystats.STAT_SCALING_REPLY";

    private String mState;
    private String mId;
    private String mName;
    private String mValue;
    private String mScaling;
    private int mMaxValue;
    private int mButtonResource;
    private int mButtonEnabledTextColor;
    private int mEditTextColor;
    private EditText mEditStatNameView;
    private EditText mEditScalingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        switch (getSharedPreferences("Settings", MODE_PRIVATE).getString("theme", "morning")) {
            case "morning":
                setTheme(R.style.MorningTheme);
                mButtonResource = R.drawable.positive_button_morning;
                mButtonEnabledTextColor = Color.BLACK;
                mEditTextColor = Color.BLACK;
                break;
            case "evening":
                setTheme(R.style.EveningTheme);
                mButtonResource = R.drawable.positive_button_evening;
                mButtonEnabledTextColor = Color.BLACK;
                mEditTextColor = Color.BLACK;
                break;
            case "night":
                setTheme(R.style.NightTheme);
                mButtonResource = R.drawable.positive_button_night;
                mButtonEnabledTextColor = Color.WHITE;
                mEditTextColor = Color.WHITE;
                break;
            default:
                break;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_stat);

        final Intent intent = getIntent();
        mState = intent.getStringExtra(StatsSettingsActivity.NEW_STAT_STATE_MESSAGE);
        mName = intent.getStringExtra(StatsSettingsActivity.NEW_STAT_NAME_MESSAGE);
        mValue = intent.getStringExtra(StatsSettingsActivity.NEW_STAT_VALUE_MESSAGE);
        if (mValue == null) {
            mValue = "1";
        }
        mMaxValue = intent.getIntExtra(StatsSettingsActivity.MAX_STAT_VALUE_MESSAGE, 30);
        mScaling = intent.getStringExtra(StatsSettingsActivity.STAT_SCALING_MESSAGE);
        if (mScaling == null) {
            mScaling = "1";
        }

        mEditStatNameView = findViewById(R.id.edit_stat_name);
        mEditStatNameView.setTextColor(mEditTextColor);

        mEditScalingView = findViewById(R.id.edit_scaling);
        mEditScalingView.setTextColor(mEditTextColor);
        mEditScalingView.append(mScaling);

        TextView valueView = findViewById(R.id.value_view);
        String value_text = "Current Value: " + mValue + "/" + mMaxValue;
        valueView.setText(value_text);

        if (mState.contentEquals("Edit")) {
            mId = intent.getStringExtra(StatsSettingsActivity.NEW_STAT_ID_MESSAGE);
            mEditStatNameView.append(mName);
            int scaled_value = (int) Math.round(mMaxValue * Double.parseDouble(mScaling));
            value_text = "Current Value: " + mValue + "/" + scaled_value;
            valueView.setText(value_text);
        }

        final Button button = findViewById(R.id.button_save);
        button.setBackgroundResource(mButtonResource);
        button.setEnabled(false);
        button.setTextColor(Color.GRAY);

        mEditStatNameView.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!mEditStatNameView.getText().toString().trim().isEmpty() &&
                        isValidScaling(mEditScalingView.getText().toString().trim())) {
                    button.setEnabled(true);
                    button.setTextColor(mButtonEnabledTextColor);
                } else {
                    button.setEnabled(false);
                    button.setTextColor(Color.GRAY);
                }
            }
        });

        mEditScalingView.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = mEditScalingView.getText().toString().trim();
                if (isValidScaling(text)) {
                    if (text.startsWith(".")) {
                        text = "0" + text;
                    }
                    int scaled_value = (int) Math.round(mMaxValue * Double.parseDouble(text));
                    String value_text = "Current Value: " + mValue + "/" + scaled_value;
                    valueView.setText(value_text);

                    if (!mEditStatNameView.getText().toString().trim().isEmpty()) {
                        button.setEnabled(true);
                        button.setTextColor(mButtonEnabledTextColor);
                    }
                } else {
                    button.setEnabled(false);
                    button.setTextColor(Color.GRAY);
                }
            }
        });

        button.setOnClickListener(view -> {
            Intent replyIntent = new Intent();
            if (TextUtils.isEmpty(mEditStatNameView.getText())) {
                setResult(RESULT_CANCELED, replyIntent);
            } else {
                replyIntent.putExtra(STATE_REPLY, mState);
                replyIntent.putExtra(STAT_ID_REPLY, mId);
                replyIntent.putExtra(STAT_NAME_REPLY,
                        StringUtility.cleanString(mEditStatNameView.getText().toString().toLowerCase()));
                replyIntent.putExtra(STAT_SCALING_REPLY,
                        StringUtility.cleanDoubleString(mEditScalingView.getText().toString()));
                replyIntent.putExtra(STAT_VALUE_REPLY, mValue);

                setResult(RESULT_OK, replyIntent);
            }
            finish();
        });

        final Button clearButton = findViewById(R.id.button_clear);
        clearButton.setBackgroundResource(mButtonResource);
        if (!mState.contentEquals("Edit")) {
            clearButton.setVisibility(View.GONE);
        } else {
            clearButton.setOnClickListener(view -> {
                mValue = "1";
                String value_text1 = "Current Value: " + mValue + "/" + mMaxValue;
                valueView.setText(value_text1);
                button.setEnabled(true);
                button.setTextColor(mButtonEnabledTextColor);
            });
        }
    }

    private boolean isValidScaling(String text) {
        if (text.isEmpty() || text.endsWith(".")) {
            return false;
        }
        if (Double.parseDouble(text) == 0) {
            return false;
        }
        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }
}