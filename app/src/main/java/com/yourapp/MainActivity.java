package com.yourapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    private TextView tvExpression;
    private TextView tvResult;

    private String currentInput = "";
    private String operator = "";
    private double firstOperand = Double.NaN;
    private boolean isCalculated = false;

    private DecimalFormat decimalFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        decimalFormat = new DecimalFormat("#.########");

        tvExpression = findViewById(R.id.tvExpression);
        tvResult = findViewById(R.id.tvResult);

        setupButtonListeners();
    }

    private void setupButtonListeners() {
        int[] numericButtonIds = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9
        };

        View.OnClickListener numberClickListener = v -> {
            Button button = (Button) v;
            if (isCalculated) {
                currentInput = "";
                isCalculated = false;
            }
            currentInput += button.getText().toString();
            tvResult.setText(currentInput);
        };

        for (int id : numericButtonIds) {
            findViewById(id).setOnClickListener(numberClickListener);
        }

        findViewById(R.id.btnDot).setOnClickListener(v -> {
            if (isCalculated) {
                currentInput = "0";
                isCalculated = false;
            }
            if (currentInput.isEmpty()) {
                currentInput = "0.";
            } else if (!currentInput.contains(".")) {
                currentInput += ".";
            }
            tvResult.setText(currentInput);
        });

        findViewById(R.id.btnPlus).setOnClickListener(v -> onOperatorClick("+"));
        findViewById(R.id.btnMinus).setOnClickListener(v -> onOperatorClick("-"));
        findViewById(R.id.btnMultiply).setOnClickListener(v -> onOperatorClick("×"));
        findViewById(R.id.btnDivide).setOnClickListener(v -> onOperatorClick("÷"));

        findViewById(R.id.btnPercent).setOnClickListener(v -> {
            if (!currentInput.isEmpty()) {
                try {
                    double val = Double.parseDouble(currentInput) / 100.0;
                    currentInput = decimalFormat.format(val);
                    tvResult.setText(currentInput);
                } catch (NumberFormatException ignored) {
                }
            }
        });

        findViewById(R.id.btnPlusMinus).setOnClickListener(v -> {
            if (!currentInput.isEmpty()) {
                try {
                    double val = Double.parseDouble(currentInput) * -1.0;
                    currentInput = decimalFormat.format(val);
                    tvResult.setText(currentInput);
                } catch (NumberFormatException ignored) {
                }
            }
        });

        findViewById(R.id.btnClear).setOnClickListener(v -> clearAll());

        findViewById(R.id.btnDelete).setOnClickListener(v -> {
            if (!isCalculated && !currentInput.isEmpty()) {
                currentInput = currentInput.substring(0, currentInput.length() - 1);
                tvResult.setText(currentInput.isEmpty() ? "0" : currentInput);
            }
        });

        findViewById(R.id.btnEquals).setOnClickListener(v -> calculateResult());
    }

    private void onOperatorClick(String selectedOperator) {
        if (!currentInput.isEmpty()) {
            if (!Double.isNaN(firstOperand) && !operator.isEmpty() && !isCalculated) {
                calculateIntermediateResult();
            } else {
                try {
                    firstOperand = Double.parseDouble(currentInput);
                } catch (NumberFormatException e) {
                    return;
                }
            }
            operator = selectedOperator;
            tvExpression.setText(decimalFormat.format(firstOperand) + " " + operator);
            currentInput = "";
            isCalculated = false;
        } else if (!Double.isNaN(firstOperand)) {
            operator = selectedOperator;
            tvExpression.setText(decimalFormat.format(firstOperand) + " " + operator);
        }
    }

    private void calculateIntermediateResult() {
        try {
            double secondOperand = Double.parseDouble(currentInput);
            firstOperand = compute(firstOperand, secondOperand, operator);
        } catch (NumberFormatException | ArithmeticException ignored) {
        }
    }

    private void calculateResult() {
        if (!Double.isNaN(firstOperand) && !operator.isEmpty() && !currentInput.isEmpty()) {
            try {
                double secondOperand = Double.parseDouble(currentInput);
                tvExpression.setText(decimalFormat.format(firstOperand) + " " + operator + " " + decimalFormat.format(secondOperand) + " =");

                double result = compute(firstOperand, secondOperand, operator);
                if (Double.isInfinite(result) || Double.isNaN(result)) {
                    tvResult.setText("Error");
                    clearState();
                } else {
                    currentInput = decimalFormat.format(result);
                    tvResult.setText(currentInput);
                    firstOperand = Double.NaN;
                    operator = "";
                    isCalculated = true;
                }
            } catch (Exception e) {
                tvResult.setText("Error");
                clearState();
            }
        }
    }

    private double compute(double a, double b, String op) {
        switch (op) {
            case "+": return a + b;
            case "-": return a - b;
            case "×": return a * b;
            case "÷":
                if (b == 0) return Double.NaN;
                return a / b;
            default: return b;
        }
    }

    private void clearAll() {
        clearState();
        tvExpression.setText("");
        tvResult.setText("0");
    }

    private void clearState() {
        currentInput = "";
        operator = "";
        firstOperand = Double.NaN;
        isCalculated = false;
    }
}