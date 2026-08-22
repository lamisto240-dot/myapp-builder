package com.example.calculator;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tvExpression;
    private TextView tvResult;

    private String currentInput = "";
    private String expressionText = "";
    private boolean isEvaluated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvExpression = findViewById(R.id.tvExpression);
        tvResult = findViewById(R.id.tvResult);

        int[] buttonIds = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
                R.id.btnClear, R.id.btnDelete, R.id.btnPercent, R.id.btnDivide,
                R.id.btnMultiply, R.id.btnSubtract, R.id.btnAdd, R.id.btnDecimal,
                R.id.btnPlusMinus, R.id.btnEqual
        };

        for (int id : buttonIds) {
            Button btn = findViewById(id);
            if (btn != null) {
                btn.setOnClickListener(this);
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.btnClear) {
            clearAll();
        } else if (id == R.id.btnDelete) {
            deleteLastCharacter();
        } else if (id == R.id.btnEqual) {
            evaluateExpression();
        } else if (id == R.id.btnPlusMinus) {
            toggleSign();
        } else if (id == R.id.btnPercent) {
            applyPercent();
        } else if (id == R.id.btnAdd || id == R.id.btnSubtract || id == R.id.btnMultiply || id == R.id.btnDivide) {
            appendOperator(((Button) v).getText().toString());
        } else if (id == R.id.btnDecimal) {
            appendDecimal();
        } else {
            // Digit buttons
            appendDigit(((Button) v).getText().toString());
        }
    }

    private void clearAll() {
        currentInput = "";
        expressionText = "";
        isEvaluated = false;
        tvExpression.setText("");
        tvResult.setText("0");
    }

    private void deleteLastCharacter() {
        if (isEvaluated) {
            clearAll();
            return;
        }

        if (!currentInput.isEmpty()) {
            currentInput = currentInput.substring(0, currentInput.length() - 1);
            if (currentInput.endsWith(" ")) {
                currentInput = currentInput.substring(0, currentInput.length() - 1);
            }
            tvResult.setText(currentInput.isEmpty() ? "0" : currentInput);
            updateLiveResult();
        }
    }

    private void appendDigit(String digit) {
        if (isEvaluated) {
            currentInput = "";
            expressionText = "";
            isEvaluated = false;
        }

        currentInput += digit;
        tvResult.setText(currentInput);
        updateLiveResult();
    }

    private void appendDecimal() {
        if (isEvaluated) {
            currentInput = "0";
            expressionText = "";
            isEvaluated = false;
        }

        String[] tokens = currentInput.split(" ");
        String lastToken = tokens.length > 0 ? tokens[tokens.length - 1] : "";

        if (!lastToken.contains(".")) {
            if (lastToken.isEmpty() || isOperator(lastToken)) {
                currentInput += "0.";
            } else {
                currentInput += ".";
            }
            tvResult.setText(currentInput);
        }
    }

    private void appendOperator(String op) {
        if (isEvaluated) {
            isEvaluated = false;
        }

        if (currentInput.isEmpty()) {
            if (op.equals("-")) {
                currentInput = "-";
                tvResult.setText(currentInput);
            }
            return;
        }

        if (currentInput.endsWith(" ")) {
            // Replace previous operator
            currentInput = currentInput.substring(0, currentInput.length() - 3) + " " + op + " ";
        } else {
            currentInput += " " + op + " ";
        }

        tvResult.setText(currentInput);
    }

    private void toggleSign() {
        if (currentInput.isEmpty() || isEvaluated) return;

        String[] tokens = currentInput.split(" ");
        if (tokens.length == 0) return;

        String lastToken = tokens[tokens.length - 1];
        if (isOperator(lastToken)) return;

        try {
            double value = Double.parseDouble(lastToken);
            value = -value;
            String newLastToken = formatResult(value);

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < tokens.length - 1; i++) {
                sb.append(tokens[i]).append(" ");
            }
            sb.append(newLastToken);
            currentInput = sb.toString();
            tvResult.setText(currentInput);
            updateLiveResult();
        } catch (NumberFormatException ignored) {
        }
    }

    private void applyPercent() {
        if (currentInput.isEmpty() || isEvaluated) return;

        String[] tokens = currentInput.split(" ");
        if (tokens.length == 0) return;

        String lastToken = tokens[tokens.length - 1];
        if (isOperator(lastToken)) return;

        try {
            double value = Double.parseDouble(lastToken) / 100.0;
            String newLastToken = formatResult(value);

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < tokens.length - 1; i++) {
                sb.append(tokens[i]).append(" ");
            }
            sb.append(newLastToken);
            currentInput = sb.toString();
            tvResult.setText(currentInput);
            updateLiveResult();
        } catch (NumberFormatException ignored) {
        }
    }

    private void updateLiveResult() {
        if (currentInput.trim().isEmpty()) {
            tvExpression.setText("");
            return;
        }
        try {
            double result = evaluate(currentInput);
            if (!Double.isNaN(result) && !Double.isInfinite(result)) {
                tvExpression.setText("= " + formatResult(result));
            } else {
                tvExpression.setText("");
            }
        } catch (Exception e) {
            tvExpression.setText("");
        }
    }

    private void evaluateExpression() {
        if (currentInput.isEmpty()) return;

        try {
            double result = evaluate(currentInput);
            if (Double.isInfinite(result) || Double.isNaN(result)) {
                tvResult.setText("Error");
            } else {
                tvExpression.setText(currentInput + " =");
                currentInput = formatResult(result);
                tvResult.setText(currentInput);
                isEvaluated = true;
            }
        } catch (Exception e) {
            tvResult.setText("Error");
        }
    }

    private double evaluate(String expression) {
        String[] tokens = expression.trim().split("\\s+");
        if (tokens.length == 0 || tokens[0].isEmpty()) return 0;

        List<String> output = new ArrayList<>();
        Stack<String> operators = new Stack<>();

        for (String token : tokens) {
            if (isOperator(token)) {
                while (!operators.isEmpty() && precedence(operators.peek()) >= precedence(token)) {
                    output.add(operators.pop());
                }
                operators.push(token);
            } else {
                output.add(token);
            }
        }

        while (!operators.isEmpty()) {
            output.add(operators.pop());
        }

        Stack<Double> values = new Stack<>();
        for (String token : output) {
            if (isOperator(token)) {
                if (values.size() < 2) return Double.NaN;
                double b = values.pop();
                double a = values.pop();
                switch (token) {
                    case "+":
                        values.push(a + b);
                        break;
                    case "-":
                        values.push(a - b);
                        break;
                    case "×":
                        values.push(a * b);
                        break;
                    case "÷":
                        if (b == 0) return Double.POSITIVE_INFINITY;
                        values.push(a / b);
                        break;
                }
            } else {
                try {
                    values.push(Double.parseDouble(token));
                } catch (NumberFormatException e) {
                    return Double.NaN;
                }
            }
        }

        return values.isEmpty() ? 0 : values.pop();
    }

    private boolean isOperator(String token) {
        return token.equals("+") || token.equals("-") || token.equals("×") || token.equals("÷");
    }

    private int precedence(String op) {
        if (op.equals("+") || op.equals("-")) return 1;
        if (op.equals("×") || op.equals("÷")) return 2;
        return 0;
    }

    private String formatResult(double value) {
        if (value == (long) value) {
            return String.format("%d", (long) value);
        } else {
            DecimalFormat df = new DecimalFormat("#.########");
            return df.format(value);
        }
    }
}