package com.yourapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText etName;
    private Button btnWelcome;
    private TextView tvGreeting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etName = findViewById(R.id.et_name);
        btnWelcome = findViewById(R.id.btn_welcome);
        tvGreeting = findViewById(R.id.tv_greeting);

        btnWelcome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etName.getText().toString().trim();
                if (TextUtils.isEmpty(name)) {
                    tvGreeting.setText("Welcome, Friend! 👋");
                } else {
                    tvGreeting.setText("Welcome, " + name + "! 👋");
                }
                tvGreeting.setVisibility(View.VISIBLE);
            }
        });
    }
}