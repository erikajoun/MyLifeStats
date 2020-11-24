package com.example.mystats.taskform.views;

import android.graphics.Color;
import android.widget.Button;

public class ButtonView {
    private Button button;
    private int buttonEnabledTextColor;

    public ButtonView(Button button, int buttonEnabledTextColor) {
        this.button = button;
        this.buttonEnabledTextColor = buttonEnabledTextColor;
    }

    public Button getButton() {
        return button;
    }

    public void setEnabled(boolean enabled) {
        button.setEnabled(enabled);
        if (enabled) {
            button.setTextColor(buttonEnabledTextColor);
        } else {
            button.setTextColor(Color.GRAY);
        }
    }
}
