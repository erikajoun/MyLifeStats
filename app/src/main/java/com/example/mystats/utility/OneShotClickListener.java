package com.example.mystats.utility;

import android.view.View;

public abstract class OneShotClickListener implements View.OnClickListener {
    private boolean mHasClicked;

    @Override public final void onClick(View view) {
        if (!mHasClicked) {
            onClicked(view);
            mHasClicked = true;
        }
    }

    public abstract void onClicked(View v);
}
