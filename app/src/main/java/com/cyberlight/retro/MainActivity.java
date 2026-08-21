package com.cyberlight.retro;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_CAMERA_REQUEST = 101;

    // Modes
    private static final int MODE_OFF = 0;
    private static final int MODE_ON = 1;
    private static final int MODE_STROBE = 2;
    private static final int MODE_SOS = 3;
    private static final int MODE_SCREEN = 4;

    private int currentMode = MODE_OFF;

    // Camera Hardware
    private CameraManager cameraManager;
    private String cameraId = null;

    // UI elements
    private TextView tvStatus;
    private TextView tvHzValue;
    private TextView tvPowerState;
    private ImageView powerIcon;
    private FrameLayout btnPower;
    private MaterialCardView btnPowerContainer;
    private View powerGlowView;
    private View screenFlashOverlay;

    private MaterialButton btnModeStrobe;
    private MaterialButton btnModeSos;
    private MaterialButton btnModeScreen;
    private SeekBar seekBarFrequency;

    // Strobe & SOS logic
    private Handler handler = new Handler(Looper.getMainLooper());
    private int strobeFrequency = 5; // Default 5 Hz
    private boolean flashState = false;

    // SOS pattern in milliseconds
    private final long[] sosPattern = new long[]{
            150, 150, 150, 150, 150, 400,  // S: dot, dot, dot
            450, 150, 450, 150, 450, 400,  // O: dash, dash, dash
            150, 150, 150, 150, 150, 1200  // S: dot, dot, dot + pause before repeat
    };
    private int sosStepIndex = 0;

    private final Runnable strobeRunnable = new Runnable() {
        @Override
        public void run() {
            if (currentMode != MODE_STROBE) return;

            flashState = !flashState;
            setHardwareTorch(flashState);

            long interval = 1000L / (strobeFrequency * 2L);
            if (interval < 20) interval = 20;

            handler.postDelayed(this, interval);
        }
    };

    private final Runnable sosRunnable = new Runnable() {
        @Override
        public void run() {
            if (currentMode != MODE_SOS) return;

            boolean state = (sosStepIndex % 2 == 0);
            setHardwareTorch(state);

            long delay = sosPattern[sosStepIndex];
            sosStepIndex = (sosStepIndex + 1) % sosPattern.length;

            handler.postDelayed(this, delay);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initCameraManager();
        setupListeners();
        updateUI();

        checkCameraPermission();
    }

    private void initViews() {
        tvStatus = findViewById(R.id.tvStatus);
        tvHzValue = findViewById(R.id.tvHzValue);
        tvPowerState = findViewById(R.id.tvPowerState);
        powerIcon = findViewById(R.id.powerIcon);
        btnPower = findViewById(R.id.btnPower);
        btnPowerContainer = findViewById(R.id.btnPowerContainer);
        powerGlowView = findViewById(R.id.powerGlowView);
        screenFlashOverlay = findViewById(R.id.screenFlashOverlay);

        btnModeStrobe = findViewById(R.id.btnModeStrobe);
        btnModeSos = findViewById(R.id.btnModeSos);
        btnModeScreen = findViewById(R.id.btnModeScreen);
        seekBarFrequency = findViewById(R.id.seekBarFrequency);

        seekBarFrequency.setProgress(strobeFrequency);
        tvHzValue.setText(getString(R.string.strobe_hz, strobeFrequency));
    }

    private void initCameraManager() {
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        if (cameraManager != null) {
            try {
                String[] ids = cameraManager.getCameraIdList();
                for (String id : ids) {
                    CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                    Boolean hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                    Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);

                    if (hasFlash != null && hasFlash && facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                        cameraId = id;
                        break;
                    }
                }
                if (cameraId == null) {
                    for (String id : ids) {
                        CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                        Boolean hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                        if (hasFlash != null && hasFlash) {
                            cameraId = id;
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CAMERA_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initCameraManager();
            } else {
                Toast.makeText(this, R.string.perm_required, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupListeners() {
        btnPower.setOnClickListener(v -> {
            if (currentMode != MODE_OFF) {
                setMode(MODE_OFF);
            } else {
                setMode(MODE_ON);
            }
        });

        btnModeStrobe.setOnClickListener(v -> {
            if (currentMode == MODE_STROBE) {
                setMode(MODE_OFF);
            } else {
                setMode(MODE_STROBE);
            }
        });

        btnModeSos.setOnClickListener(v -> {
            if (currentMode == MODE_SOS) {
                setMode(MODE_OFF);
            } else {
                setMode(MODE_SOS);
            }
        });

        btnModeScreen.setOnClickListener(v -> {
            if (currentMode == MODE_SCREEN) {
                setMode(MODE_OFF);
            } else {
                setMode(MODE_SCREEN);
            }
        });

        screenFlashOverlay.setOnClickListener(v -> setMode(MODE_OFF));

        seekBarFrequency.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                strobeFrequency = Math.max(1, progress);
                tvHzValue.setText(getString(R.string.strobe_hz, strobeFrequency));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setMode(int mode) {
        stopAllEffects();
        currentMode = mode;

        switch (currentMode) {
            case MODE_OFF:
                setHardwareTorch(false);
                setScreenFlash(false);
                break;

            case MODE_ON:
                setScreenFlash(false);
                setHardwareTorch(true);
                break;

            case MODE_STROBE:
                setScreenFlash(false);
                flashState = false;
                handler.post(strobeRunnable);
                break;

            case MODE_SOS:
                setScreenFlash(false);
                sosStepIndex = 0;
                handler.post(sosRunnable);
                break;

            case MODE_SCREEN:
                setHardwareTorch(false);
                setScreenFlash(true);
                break;
        }

        updateUI();
    }

    private void stopAllEffects() {
        handler.removeCallbacks(strobeRunnable);
        handler.removeCallbacks(sosRunnable);
    }

    private void setHardwareTorch(boolean enable) {
        if (cameraId == null) {
            return;
        }
        try {
            cameraManager.setTorchMode(cameraId, enable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setScreenFlash(boolean enable) {
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        if (enable) {
            layoutParams.screenBrightness = 1.0f;
            screenFlashOverlay.setVisibility(View.VISIBLE);
        } else {
            layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
            screenFlashOverlay.setVisibility(View.GONE);
        }
        getWindow().setAttributes(layoutParams);
    }

    private void updateUI() {
        switch (currentMode) {
            case MODE_OFF:
                tvStatus.setText(R.string.status_off);
                tvStatus.setTextColor(Color.parseColor("#7A7A9E"));
                btnPowerContainer.setStrokeColor(Color.parseColor("#4400F0FF"));
                powerGlowView.setBackgroundColor(Color.parseColor("#0500F0FF"));
                powerIcon.setColorFilter(Color.parseColor("#7A7A9E"));
                tvPowerState.setText("SYSTEM READY");
                tvPowerState.setTextColor(Color.parseColor("#7A7A9E"));

                btnModeStrobe.setStrokeColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#00F0FF")));
                btnModeSos.setStrokeColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#FF0055")));
                btnModeScreen.setStrokeColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#FFE600")));
                break;

            case MODE_ON:
                tvStatus.setText(R.string.status_on);
                tvStatus.setTextColor(Color.parseColor("#00F0FF"));
                btnPowerContainer.setStrokeColor(Color.parseColor("#00F0FF"));
                powerGlowView.setBackgroundColor(Color.parseColor("#3300F0FF"));
                powerIcon.setColorFilter(Color.parseColor("#00F0FF"));
                tvPowerState.setText("TORCH ACTIVE");
                tvPowerState.setTextColor(Color.parseColor("#00F0FF"));
                break;

            case MODE_STROBE:
                tvStatus.setText(R.string.status_strobe);
                tvStatus.setTextColor(Color.parseColor("#FF0055"));
                btnPowerContainer.setStrokeColor(Color.parseColor("#FF0055"));
                powerGlowView.setBackgroundColor(Color.parseColor("#33FF0055"));
                powerIcon.setColorFilter(Color.parseColor("#FF0055"));
                tvPowerState.setText("STROBE ACTIVE");
                tvPowerState.setTextColor(Color.parseColor("#FF0055"));
                break;

            case MODE_SOS:
                tvStatus.setText(R.string.status_sos);
                tvStatus.setTextColor(Color.parseColor("#FFE600"));
                btnPowerContainer.setStrokeColor(Color.parseColor("#FFE600"));
                powerGlowView.setBackgroundColor(Color.parseColor("#33FFE600"));
                powerIcon.setColorFilter(Color.parseColor("#FFE600"));
                tvPowerState.setText("S.O.S ACTIVE");
                tvPowerState.setTextColor(Color.parseColor("#FFE600"));
                break;

            case MODE_SCREEN:
                tvStatus.setText("SYSTEM: SCREEN LIGHT ACTIVE");
                tvStatus.setTextColor(Color.parseColor("#FFFFFF"));
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        setMode(MODE_OFF);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAllEffects();
        setHardwareTorch(false);
    }
}