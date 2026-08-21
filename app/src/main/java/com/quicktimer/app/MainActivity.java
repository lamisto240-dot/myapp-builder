package com.quicktimer.app;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView tvTimerDisplay;
    private TextView tvMillisDisplay;
    private MaterialButton btnStart;
    private MaterialButton btnStop;
    private MaterialButton btnReset;

    private Handler handler;
    private long startTime = 0L;
    private long timeInMilliseconds = 0L;
    private long timeSwapBuff = 0L;
    private long updatedTime = 0L;
    private boolean isRunning = false;

    private final Runnable updateTimerThread = new Runnable() {
        @Override
        public void run() {
            timeInMilliseconds = SystemClock.elapsedRealtime() - startTime;
            updatedTime = timeSwapBuff + timeInMilliseconds;

            updateDisplay(updatedTime);

            if (isRunning) {
                handler.postDelayed(this, 10);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvTimerDisplay = findViewById(R.id.tvTimerDisplay);
        tvMillisDisplay = findViewById(R.id.tvMillisDisplay);
        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        btnReset = findViewById(R.id.btnReset);

        handler = new Handler(Looper.getMainLooper());

        btnStart.setOnClickListener(v -> startTimer());
        btnStop.setOnClickListener(v -> stopTimer());
        btnReset.setOnClickListener(v -> resetTimer());

        if (savedInstanceState != null) {
            timeSwapBuff = savedInstanceState.getLong("timeSwapBuff", 0L);
            isRunning = savedInstanceState.getBoolean("isRunning", false);
            startTime = savedInstanceState.getLong("startTime", 0L);

            if (isRunning) {
                handler.post(updateTimerThread);
                updateButtonStates(true);
            } else {
                updatedTime = timeSwapBuff;
                updateDisplay(updatedTime);
                updateButtonStates(false);
            }
        }
    }

    private void startTimer() {
        if (!isRunning) {
            startTime = SystemClock.elapsedRealtime();
            handler.post(updateTimerThread);
            isRunning = true;
            updateButtonStates(true);
        }
    }

    private void stopTimer() {
        if (isRunning) {
            timeSwapBuff += SystemClock.elapsedRealtime() - startTime;
            handler.removeCallbacks(updateTimerThread);
            isRunning = false;
            updateButtonStates(false);
        }
    }

    private void resetTimer() {
        if (!isRunning) {
            startTime = 0L;
            timeInMilliseconds = 0L;
            timeSwapBuff = 0L;
            updatedTime = 0L;
            handler.removeCallbacks(updateTimerThread);
            updateDisplay(0);
            btnStart.setEnabled(true);
            btnStop.setEnabled(false);
            btnReset.setEnabled(false);
        }
    }

    private void updateDisplay(long totalTimeMillis) {
        int secs = (int) (totalTimeMillis / 1000);
        int mins = secs / 60;
        secs = secs % 60;
        int milliseconds = (int) (totalTimeMillis % 1000) / 10;

        tvTimerDisplay.setText(String.format(Locale.getDefault(), "%02d:%02d", mins, secs));
        tvMillisDisplay.setText(String.format(Locale.getDefault(), ".%02d", milliseconds));
    }

    private void updateButtonStates(boolean running) {
        btnStart.setEnabled(!running);
        btnStop.setEnabled(running);
        btnReset.setEnabled(!running && updatedTime > 0);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        long currentAccumulated = timeSwapBuff;
        if (isRunning) {
            currentAccumulated += (SystemClock.elapsedRealtime() - startTime);
        }
        outState.putLong("timeSwapBuff", currentAccumulated);
        outState.putBoolean("isRunning", isRunning);
        outState.putLong("startTime", SystemClock.elapsedRealtime());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateTimerThread);
    }
}