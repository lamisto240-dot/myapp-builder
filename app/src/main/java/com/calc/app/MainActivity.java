package com.calc.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tvExpression;
    private TextView tvResult;

    private String currentNumber = "";
    private Double firstOperand = null;
    private String pendingOperator = "";
    private boolean isNewInput = true;

    private DecimalFormat decimalFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        decimalFormat = new DecimalFormat("#.########");

        tvExpression = findViewById(R.id.tvExpression);
        tvResult = findViewById(R.id.tvResult);

        int[] buttonIds = new int[]{
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
            R.id.btnDot, R.id.btnAdd, R.id.btnSubtract, R.id.btnMultiply,
            R.id.btnDivide, R.id.btnPercent, R.id.btnEquals, R.id.btnAC, R.id.btnC
        };

        for (int id : buttonIds) {
            View v = findViewById(id);
            if (v != null) {
                v.setOnClickListener(this);
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.btnAC) {
            clearAll();
        } else if (id == R.id.btnC) {
            clearEntry();
        } else if (id == R.id.btnPercent) {
            applyPercent();
        } else if (id == R.id.btnEquals) {
            calculateResult();
        } else if (id == R.id.btnAdd || id == R.id.btnSubtract ||
                   id == R.id.btnMultiply || id == R.id.btnDivide) {
            Button b = (Button) v;
            setOperator(b.getText().toString());
        } else if (id == R.id.btnDot) {
            appendDot();
        } else {
            Button b = (Button) v;
            appendDigit(b.getText().toString());
        }
    }

    private void appendDigit(String digit) {
        if (isNewInput) {
            currentNumber = digit;
            isNewInput = false;
        } else {
            currentNumber += digit;
        }
        tvResult.setText(currentNumber);
    }

    private void appendDot() {
        if (isNewInput) {
            currentNumber = "0.";
            isNewInput = false;
        } else if (!currentNumber.contains(".")) {
            currentNumber += ".";
        }
        tvResult.setText(currentNumber);
    }

    private void setOperator(String op) {
        if (!currentNumber.isEmpty()) {
            double val = Double.parseDouble(currentNumber);
            if (firstOperand == null) {
                firstOperand = val;
            } else if (!pendingOperator.isEmpty() && !isNewInput) {
                firstOperand = performOperation(firstOperand, val, pendingOperator);
                tvResult.setText(decimalFormat.format(firstOperand));
            }
        }
        pendingOperator = op;
        isNewInput = true;
        if (firstOperand != null) {
            tvExpression.setText(decimalFormat.format(firstOperand) + " " + pendingOperator);
        }
    }

    private void calculateResult() {
        if (firstOperand != null && !pendingOperator.isEmpty() && !currentNumber.isEmpty()) {
            double secondOperand = Double.parseDouble(currentNumber);
            tvExpression.setText(decimalFormat.format(firstOperand) + " " + pendingOperator + " " + decimalFormat.format(secondOperand) + " =");
            double result = performOperation(firstOperand, secondOperand, pendingOperator);
            tvResult.setText(decimalFormat.format(result));

            firstOperand = result;
            pendingOperator = "";
            isNewInput = true;
        }
    }

    private double performOperation(double op1, double op2, String operator) {
        switch (operator) {
            case "+":
                return op1 + op2;
            case "-":
                return op1 - op2;
            case "×":
                return op1 * op2;
            case "÷":
                if (op2 == 0) {
                    return 0;
                }
                return op1 / op2;
            default:
                return op2;
        }
    }

    private void applyPercent() {
        if (!currentNumber.isEmpty()) {
            double val = Double.parseDouble(currentNumber) / 100.0;
            currentNumber = String.valueOf(val);
            tvResult.setText(decimalFormat.format(val));
        }
    }

    private void clearAll() {
        currentNumber = "";
        firstOperand = null;
        pendingOperator = "";
        isNewInput = true;
        tvExpression.setText("");
        tvResult.setText("0");
    }

    private void clearEntry() {
        if (!currentNumber.isEmpty() && !isNewInput) {
            if (currentNumber.length() > 1) {
                currentNumber = currentNumber.substring(0, currentNumber.length() - 1);
            } else {
                currentNumber = "";
                isNewInput = true;
            }
            tvResult.setText(currentNumber.isEmpty() ? "0" : currentNumber);
        }
    }
}