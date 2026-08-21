package com.yourapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.NumberFormat;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText etBillAmount;
    private TextInputEditText etTipPercent;
    private TextInputLayout tilBillAmount;
    private TextInputLayout tilTipPercent;
    private TextView tvTipAmount;
    private TextView tvTotalAmount;
    private ChipGroup chipGroup;
    private MaterialButton btnCalculate;

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etBillAmount = findViewById(R.id.etBillAmount);
        etTipPercent = findViewById(R.id.etTipPercent);
        tilBillAmount = findViewById(R.id.tilBillAmount);
        tilTipPercent = findViewById(R.id.tilTipPercent);
        tvTipAmount = findViewById(R.id.tvTipAmount);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        chipGroup = findViewById(R.id.chipGroup);
        btnCalculate = findViewById(R.id.btnCalculate);
    }

    private void setupListeners() {
        btnCalculate.setOnClickListener(v -> calculateTip());

        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int chipId = checkedIds.get(0);
                if (chipId == R.id.chip15) {
                    etTipPercent.setText("15");
                } else if (chipId == R.id.chip18) {
                    etTipPercent.setText("18");
                } else if (chipId == R.id.chip20) {
                    etTipPercent.setText("20");
                } else if (chipId == R.id.chip25) {
                    etTipPercent.setText("25");
                }
                clearErrors();
            }
        });

        etBillAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilBillAmount.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etTipPercent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilTipPercent.setError(null);
                // Uncheck chips if custom value entered
                if (etTipPercent.hasFocus()) {
                    chipGroup.clearCheck();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void calculateTip() {
        clearErrors();

        String billStr = etBillAmount.getText() != null ? etBillAmount.getText().toString().trim() : "";
        String tipStr = etTipPercent.getText() != null ? etTipPercent.getText().toString().trim() : "";

        boolean hasError = false;

        if (billStr.isEmpty()) {
            tilBillAmount.setError(getString(R.string.error_empty_bill));
            hasError = true;
        }

        if (tipStr.isEmpty()) {
            tilTipPercent.setError(getString(R.string.error_empty_tip));
            hasError = true;
        }

        if (hasError) {
            return;
        }

        try {
            double billAmount = Double.parseDouble(billStr);
            double tipPercent = Double.parseDouble(tipStr);

            if (billAmount < 0) {
                tilBillAmount.setError("Bill amount must be positive");
                return;
            }

            if (tipPercent < 0) {
                tilTipPercent.setError("Tip percentage must be positive");
                return;
            }

            double tipAmount = billAmount * (tipPercent / 100.0);
            double totalAmount = billAmount + tipAmount;

            tvTipAmount.setText(currencyFormat.format(tipAmount));
            tvTotalAmount.setText(currencyFormat.format(totalAmount));

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearErrors() {
        tilBillAmount.setError(null);
        tilTipPercent.setError(null);
    }
}