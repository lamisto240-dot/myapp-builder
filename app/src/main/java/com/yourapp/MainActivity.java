package com.yourapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.yourapp.databinding.ActivityMainBinding;
import java.text.NumberFormat;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupListeners();
    }

    private void setupListeners() {
        // Calculate Button action
        binding.btnCalculate.setOnClickListener(v -> calculateTip());

        // Quick tip percentage chip handlers
        binding.chip10.setOnClickListener(v -> setTipPercentage(10));
        binding.chip15.setOnClickListener(v -> setTipPercentage(15));
        binding.chip18.setOnClickListener(v -> setTipPercentage(18));
        binding.chip20.setOnClickListener(v -> setTipPercentage(20));

        // Auto calculate on text changes if inputs are valid
        TextWatcher autoCalculateWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (hasValidInputs()) {
                    calculateTip();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        binding.etBillAmount.addTextChangedListener(autoCalculateWatcher);
        binding.etTipPercent.addTextChangedListener(autoCalculateWatcher);
        binding.etPeopleCount.addTextChangedListener(autoCalculateWatcher);
    }

    private void setTipPercentage(int percent) {
        binding.etTipPercent.setText(String.valueOf(percent));
        calculateTip();
    }

    private boolean hasValidInputs() {
        String billStr = binding.etBillAmount.getText() != null ? binding.etBillAmount.getText().toString().trim() : "";
        String tipPercentStr = binding.etTipPercent.getText() != null ? binding.etTipPercent.getText().toString().trim() : "";
        return !billStr.isEmpty() && !tipPercentStr.isEmpty();
    }

    private void calculateTip() {
        String billStr = binding.etBillAmount.getText() != null ? binding.etBillAmount.getText().toString().trim() : "";
        String tipPercentStr = binding.etTipPercent.getText() != null ? binding.etTipPercent.getText().toString().trim() : "";
        String peopleStr = binding.etPeopleCount.getText() != null ? binding.etPeopleCount.getText().toString().trim() : "1";

        if (billStr.isEmpty()) {
            binding.tilBillAmount.setError("Please enter bill amount");
            return;
        } else {
            binding.tilBillAmount.setError(null);
        }

        if (tipPercentStr.isEmpty()) {
            binding.tilTipPercent.setError("Please enter tip percentage");
            return;
        } else {
            binding.tilTipPercent.setError(null);
        }

        try {
            double bill = Double.parseDouble(billStr);
            double tipPercent = Double.parseDouble(tipPercentStr);
            int peopleCount = peopleStr.isEmpty() ? 1 : Integer.parseInt(peopleStr);
            if (peopleCount < 1) peopleCount = 1;

            double tipAmount = bill * (tipPercent / 100.0);
            double totalAmount = bill + tipAmount;
            double perPersonAmount = totalAmount / peopleCount;

            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

            binding.tvTipAmount.setText(currencyFormat.format(tipAmount));
            binding.tvTotalAmount.setText(currencyFormat.format(totalAmount));
            binding.tvPerPersonAmount.setText(currencyFormat.format(perPersonAmount));

        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.invalid_input, Toast.LENGTH_SHORT).show();
        }
    }
}