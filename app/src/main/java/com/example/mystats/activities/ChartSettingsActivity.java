package com.example.mystats.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mystats.R;

import java.lang.reflect.Field;
import java.util.Locale;

public class ChartSettingsActivity extends AppCompatActivity {
    public static final String NEW_MAX_REPLY = "com.example.mystats.NEW_MAX_REPLY";
    public static final String NEW_SIZE_REPLY = "com.example.mystats.NEW_SIZE_REPLY";

    private int mButtonResource;
    private int mButtonEnabledTextColor;
    private int mEditTextColor;

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
        setContentView(R.layout.activity_edit_max);

        EditText editMax = findViewById(R.id.edit_max);
        editMax.setTextColor(mEditTextColor);

        final Intent intent = getIntent();
        int prevMax = intent.getIntExtra(StatsSettingsActivity.MAX_STAT_VALUE_MESSAGE, 30);
        int prevSize = intent.getIntExtra(StatsSettingsActivity.CHART_SIZE_MESSAGE, 100);
        editMax.setTransformationMethod(null);
        editMax.append(String.format(Locale.getDefault(), "%d", prevMax));

        NumberPicker sizePicker = findViewById(R.id.size_picker);
        int NUMBER_OF_VALUES = 4;
        int PICKER_RANGE = 10;
        String[] displayedValues = new String[NUMBER_OF_VALUES];
        for (int i = 0; i < NUMBER_OF_VALUES; i++) {
            displayedValues[i] = String.valueOf(100 + PICKER_RANGE - (PICKER_RANGE * (i + 1)));
        }
        sizePicker.setMinValue(0);
        sizePicker.setMaxValue(displayedValues.length - 1);
        sizePicker.setDisplayedValues(displayedValues);

        for (int i = 0; i < displayedValues.length; i++) {
            if (displayedValues[i].equals(Integer.toString(prevSize))) {
                sizePicker.setValue(i);
            }
        }

        setNumberPickerDividerColor(sizePicker, mEditTextColor);
        setNumberPickerTextColor(sizePicker, mEditTextColor);

        final Button button = findViewById(R.id.button_save);
        button.setBackgroundResource(mButtonResource);

        editMax.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (s.toString().startsWith("0") && s.length() > 1) {
                    s.delete(0, s.length() - 1);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String currentText = editMax.getText().toString();
                if (!currentText.trim().isEmpty() && !currentText.trim().contentEquals("0")) {
                    button.setEnabled(true);
                    button.setTextColor(mButtonEnabledTextColor);
                } else {
                    button.setEnabled(false);
                    button.setTextColor(Color.GRAY);
                }
            }
        });

        button.setOnClickListener(view -> {
            int newMax = Integer.parseInt(editMax.getText().toString());
            Intent replyIntent = new Intent();
            replyIntent.putExtra(NEW_MAX_REPLY, newMax);
            replyIntent.putExtra(NEW_SIZE_REPLY, Integer.parseInt(displayedValues[sizePicker.getValue()]));
            setResult(RESULT_OK, replyIntent);
            finish();
        });
    }

    private void setNumberPickerTextColor(NumberPicker numberPicker, int color) {
        try {
            Field selectorWheelPaintField = numberPicker.getClass()
                    .getDeclaredField("mSelectorWheelPaint");
            selectorWheelPaintField.setAccessible(true);
            ((Paint) selectorWheelPaintField.get(numberPicker)).setColor(color);
        } catch (NoSuchFieldException e) {
            Log.w("setPickerTextColor", e);
        } catch (IllegalAccessException e) {
            Log.w("setPickerTextColor", e);
        } catch (IllegalArgumentException e) {
            Log.w("setPickerTextColor", e);
        }

        final int count = numberPicker.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = numberPicker.getChildAt(i);
            if (child instanceof EditText)
                ((EditText) child).setTextColor(color);
        }
        numberPicker.invalidate();
    }

    private void setNumberPickerDividerColor(NumberPicker picker, int color) {

        java.lang.reflect.Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (java.lang.reflect.Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    ColorDrawable colorDrawable = new ColorDrawable(color);
                    pf.set(picker, colorDrawable);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
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