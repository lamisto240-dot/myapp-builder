package com.colorflip.app;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private ConstraintLayout rootLayout;
    private MaterialCardView cardInfo;
    private TextView tvColorLabel;
    private TextView tvColorName;
    private TextView tvColorHex;
    private TextView tvCopyHint;
    private MaterialButton btnFlip;

    private final Random random = new Random();
    private int currentColorInt = Color.parseColor("#1ABC9C");

    private static class ColorItem {
        String name;
        String hex;

        ColorItem(String name, String hex) {
            this.name = name;
            this.hex = hex;
        }
    }

    private List<ColorItem> colorList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rootLayout = findViewById(R.id.root_layout);
        cardInfo = findViewById(R.id.card_info);
        tvColorLabel = findViewById(R.id.tv_color_label);
        tvColorName = findViewById(R.id.tv_color_name);
        tvColorHex = findViewById(R.id.tv_color_hex);
        tvCopyHint = findViewById(R.id.tv_copy_hint);
        btnFlip = findViewById(R.id.btn_flip);

        initColorList();
        flipColor();

        btnFlip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flipColor();
            }
        });

        cardInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyToClipboard(tvColorHex.getText().toString());
            }
        });
    }

    private void initColorList() {
        colorList = new ArrayList<>();
        colorList.add(new ColorItem("Turquoise", "#1ABC9C"));
        colorList.add(new ColorItem("Emerald", "#2ECC71"));
        colorList.add(new ColorItem("Peter River", "#3498DB"));
        colorList.add(new ColorItem("Amethyst", "#9B59B6"));
        colorList.add(new ColorItem("Wet Asphalt", "#34495E"));
        colorList.add(new ColorItem("Sunflower", "#F1C40F"));
        colorList.add(new ColorItem("Carrot", "#E67E22"));
        colorList.add(new ColorItem("Alizarin", "#E74C3C"));
        colorList.add(new ColorItem("Midnight Blue", "#2C3E50"));
        colorList.add(new ColorItem("Crimson", "#DC143C"));
        colorList.add(new ColorItem("Coral", "#FF7F50"));
        colorList.add(new ColorItem("Hot Pink", "#FF69B4"));
        colorList.add(new ColorItem("Royal Blue", "#4169E1"));
        colorList.add(new ColorItem("Medium Sea Green", "#3CB371"));
        colorList.add(new ColorItem("Slate Gray", "#708090"));
        colorList.add(new ColorItem("Dark Violet", "#9400D3"));
        colorList.add(new ColorItem("Gold", "#FFD700"));
        colorList.add(new ColorItem("Deep Pink", "#FF1493"));
        colorList.add(new ColorItem("Tomato", "#FF6347"));
        colorList.add(new ColorItem("Forest Green", "#228B22"));
        colorList.add(new ColorItem("Steel Blue", "#4682B4"));
        colorList.add(new ColorItem("Chocolate", "#D2691E"));
        colorList.add(new ColorItem("Indigo", "#4B0082"));
        colorList.add(new ColorItem("Teal", "#008080"));
        colorList.add(new ColorItem("Lime Green", "#32CD32"));
        colorList.add(new ColorItem("Dark Red", "#8B0000"));
        colorList.add(new ColorItem("Plum", "#DDA0DD"));
        colorList.add(new ColorItem("Orchid", "#DA70D6"));
        colorList.add(new ColorItem("Salmon", "#FA8072"));
        colorList.add(new ColorItem("Slate Blue", "#6A5ACD"));
    }

    private void flipColor() {
        int index = random.nextInt(colorList.size());
        ColorItem item = colorList.get(index);
        int newColorInt = Color.parseColor(item.hex);

        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), currentColorInt, newColorInt);
        colorAnimation.setDuration(400);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                rootLayout.setBackgroundColor((int) animator.getAnimatedValue());
            }
        });
        colorAnimation.start();

        currentColorInt = newColorInt;
        tvColorName.setText(item.name);
        tvColorHex.setText(item.hex);

        boolean isDark = isColorDark(newColorInt);
        if (isDark) {
            cardInfo.setCardBackgroundColor(Color.parseColor("#DD222222"));
            tvColorLabel.setTextColor(Color.parseColor("#AAAAAA"));
            tvColorName.setTextColor(Color.parseColor("#FFFFFF"));
            tvColorHex.setTextColor(Color.parseColor("#00E676"));
            tvCopyHint.setTextColor(Color.parseColor("#888888"));
            btnFlip.setBackgroundColor(Color.parseColor("#FFFFFF"));
            btnFlip.setTextColor(Color.parseColor("#111111"));
        } else {
            cardInfo.setCardBackgroundColor(Color.parseColor("#DDFFFFFF"));
            tvColorLabel.setTextColor(Color.parseColor("#666666"));
            tvColorName.setTextColor(Color.parseColor("#1A1A1A"));
            tvColorHex.setTextColor(Color.parseColor("#2C3E50"));
            tvCopyHint.setTextColor(Color.parseColor("#888888"));
            btnFlip.setBackgroundColor(Color.parseColor("#111111"));
            btnFlip.setTextColor(Color.parseColor("#FFFFFF"));
        }
    }

    private boolean isColorDark(int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return darkness >= 0.45;
    }

    private void copyToClipboard(String hexText) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Hex Color", hexText);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show();
        }
    }
}