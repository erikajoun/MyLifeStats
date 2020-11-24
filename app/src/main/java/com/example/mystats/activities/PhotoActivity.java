package com.example.mystats.activities;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mystats.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PhotoActivity extends AppCompatActivity {
    public static final String EXPLANATION_REPLY = "com.example.mystats.EXPLANATION_REPLY";
    public static final String PHOTO_ROTATION_REPLY = "com.example.mystats.PHOTO_ROTATION_REPLY";

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private String mTaskId;
    private ImageView mPhotoView;
    private Button mRotateButton;
    private Bitmap mImageBitmap;
    private String mTaskStatus;
    private Button mSaveButton;

    private int mPhotoRotation;
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
        setContentView(R.layout.activity_photo);

        EditText mEditExplanationView = findViewById(R.id.edit_explanation);
        mEditExplanationView.setTextColor(mEditTextColor);
        final Intent intent = getIntent();
        mTaskId = intent.getStringExtra(ToDoActivity.TASK_ID_REPLY);
        mTaskStatus = intent.getStringExtra(ToDoActivity.TASK_STATUS_REPLY);

        mSaveButton = findViewById(R.id.explanation_save);
        mSaveButton.setBackgroundResource(mButtonResource);
        mSaveButton.setEnabled(false);
        mSaveButton.setTextColor(Color.GRAY);

        mSaveButton.setOnClickListener(view -> {
            if (mImageBitmap != null) {
                saveImage(mImageBitmap, mTaskId);
            }
            Intent replyIntent = new Intent();
            replyIntent.putExtra(EXPLANATION_REPLY, mEditExplanationView.getText().toString());
            replyIntent.putExtra(PHOTO_ROTATION_REPLY, mPhotoRotation);
            setResult(RESULT_OK, replyIntent);
            finish();
        });

        TextView explanationTextview = findViewById(R.id.explanation_textview);
        mEditExplanationView.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mEditExplanationView.getText().toString().trim().isEmpty()) {
                    mSaveButton.setEnabled(false);
                    mSaveButton.setTextColor(Color.GRAY);
                } else {
                    mSaveButton.setEnabled(true);
                    mSaveButton.setTextColor(mButtonEnabledTextColor);
                }
            }
        });

        if (mTaskStatus.contentEquals("Complete")) {
            explanationTextview.setText("Explanation (Optional with photo):");
        } else if (mTaskStatus.contentEquals("Cancelled")) {
            explanationTextview.setText("Why you couldn't complete task:");
        }

        Button takePhotoButton = findViewById(R.id.take_photo_button);
        takePhotoButton.setBackgroundResource(mButtonResource);
        takePhotoButton.setOnClickListener(v -> {
            launchTakePictureIntent();
        });

        mPhotoView = findViewById(R.id.photo);
        mPhotoView.setVisibility(View.GONE);

        mRotateButton = findViewById(R.id.rotate_button);
        mRotateButton.setVisibility(View.GONE);
        mRotateButton.setBackgroundResource(mButtonResource);
        mRotateButton.setOnClickListener(v -> {
            mPhotoRotation += 90;
            if (mPhotoRotation > 270) mPhotoRotation = 0;
            mPhotoView.setRotation(mPhotoRotation);
        });
    }

    private void launchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void saveImage(Bitmap bitmap, String imageName) {
        try {
            File path = new File(this.getFilesDir(), R.string.app_name + File.separator + R.string.images_dir);
            if (!path.exists()) {
                path.mkdirs();
            }
            File outFile = new File(path, imageName + R.string.image_file_extension);
            FileOutputStream outputStream = new FileOutputStream(outFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.close();
        } catch (IOException e) {
            Log.e("Debug", "Saving received message failed with", e);
        }
    }

    private void setReceivedImage(ImageView imageView, Bitmap bitmap) {
        imageView.setVisibility(View.VISIBLE);
        imageView.setImageBitmap(bitmap);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            mImageBitmap = (Bitmap) extras.get("data");
            this.setReceivedImage(mPhotoView, mImageBitmap);
            mRotateButton.setVisibility(View.VISIBLE);
            if (mTaskStatus.contentEquals("Complete")) {
                mSaveButton.setEnabled(true);
                mSaveButton.setTextColor(mButtonEnabledTextColor);
            }
        }
    }
}