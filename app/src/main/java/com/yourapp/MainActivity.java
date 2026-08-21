package com.yourapp;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    public static class ChatMessage {
        private final String message;
        private final boolean isUser;
        private final String timestamp;

        public ChatMessage(String message, boolean isUser, String timestamp) {
            this.message = message;
            this.isUser = isUser;
            this.timestamp = timestamp;
        }

        public String getMessage() {
            return message;
        }

        public boolean isUser() {
            return isUser;
        }

        public String getTimestamp() {
            return timestamp;
        }
    }

    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;
    private EditText etMessage;
    private TextView tvTypingIndicator;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();

    private final String[] botJokes = {
        "Why do programmers prefer dark mode? Because light attracts bugs! 🐛",
        "There are 10 types of people in the world: those who understand binary, and those who don't. 💻",
        "Why did the Java developer wear glasses? Because they couldn't C#! 😂",
        "A SQL query walks into a bar, walks up to two tables and asks: 'Can I join you?' 📊"
    };

    private final String[] botGreetings = {
        "Hey there! How can I help you today? 😊",
        "Hello! Great to hear from you. What's on your mind?",
        "Hi! Ready to chat whenever you are! 🚀",
        "Greetings! How is your day going?"
    };

    private final String[] botDefaultReplies = {
        "That sounds very interesting! Tell me more about it.",
        "I hear you! What else is new?",
        "Awesome! I'm enjoying our conversation.",
        "Thanks for sharing! Is there anything else you'd like to discuss?",
        "Got it! That makes total sense."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Alex (Online)");
        toolbar.setSubtitle("App_chat_1");
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recyclerViewChat);
        etMessage = findViewById(R.id.etMessage);
        tvTypingIndicator = findViewById(R.id.tvTypingIndicator);
        FloatingActionButton btnSend = findViewById(R.id.btnSend);

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(chatAdapter);

        // Add welcome message
        addBotMessage("Hello! I'm Alex. Welcome to App_chat_1! Ask me anything or say hi! 👋");

        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (!TextUtils.isEmpty(text)) {
                sendMessage(text);
            }
        });

        setupQuickReplies();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, 1, Menu.NONE, "Clear Chat");
        menu.add(Menu.NONE, 2, Menu.NONE, "About");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == 1) {
            clearChat();
            return true;
        } else if (item.getItemId() == 2) {
            Toast.makeText(this, "App_chat_1 v1.0 - Interactive Chat App", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendMessage(String text) {
        String currentTime = getCurrentTime();
        messageList.add(new ChatMessage(text, true, currentTime));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.smoothScrollToPosition(messageList.size() - 1);
        etMessage.setText("");

        simulateBotResponse(text);
    }

    private void addBotMessage(String text) {
        String currentTime = getCurrentTime();
        messageList.add(new ChatMessage(text, false, currentTime));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.smoothScrollToPosition(messageList.size() - 1);
    }

    private void simulateBotResponse(String userMessage) {
        tvTypingIndicator.setVisibility(View.VISIBLE);

        handler.postDelayed(() -> {
            tvTypingIndicator.setVisibility(View.GONE);
            String response = generateBotReply(userMessage);
            addBotMessage(response);
        }, 1200 + random.nextInt(800));
    }

    private String generateBotReply(String input) {
        String lower = input.toLowerCase(Locale.ROOT);
        if (lower.contains("hello") || lower.contains("hi") || lower.contains("hey")) {
            return botGreetings[random.nextInt(botGreetings.length)];
        } else if (lower.contains("joke")) {
            return botJokes[random.nextInt(botJokes.length)];
        } else if (lower.contains("how are you")) {
            return "I'm doing great, thank you for asking! How are you doing today? 😊";
        } else if (lower.contains("who are you") || lower.contains("what can you do")) {
            return "I am Alex, your virtual chat buddy in App_chat_1! You can message me anytime.";
        } else if (lower.contains("bye")) {
            return "Goodbye! Have a fantastic day ahead! 👋";
        } else {
            return botDefaultReplies[random.nextInt(botDefaultReplies.length)];
        }
    }

    private void clearChat() {
        messageList.clear();
        chatAdapter.notifyDataSetChanged();
        addBotMessage("Chat history cleared. Start a new conversation! 😊");
    }

    private void setupQuickReplies() {
        String[] replies = {"Hello! 👋", "How are you?", "Tell me a joke 😄", "What can you do?", "Clear Chat 🗑️"};
        LinearLayout container = findViewById(R.id.quickRepliesContainer);
        container.removeAllViews();

        for (String reply : replies) {
            TextView chip = new TextView(this);
            chip.setText(reply);
            chip.setTextSize(13);
            chip.setTextColor(Color.parseColor("#1E88E5"));
            chip.setBackground(createChipBackground());
            chip.setPadding(dpToPx(12), dpToPx(6), dpToPx(12), dpToPx(6));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
            chip.setLayoutParams(params);

            chip.setOnClickListener(v -> {
                if (reply.contains("Clear Chat")) {
                    clearChat();
                } else {
                    sendMessage(reply);
                }
            });

            container.addView(chip);
        }
    }

    private GradientDrawable createChipBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(dpToPx(16));
        drawable.setColor(Color.parseColor("#E3F2FD"));
        drawable.setStroke(dpToPx(1), Color.parseColor("#90CAF9"));
        return drawable;
    }

    private String getCurrentTime() {
        return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    // RecyclerView Adapter
    private class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {
        private static final int TYPE_SENT = 1;
        private static final int TYPE_RECEIVED = 2;

        private final List<ChatMessage> messages;

        public ChatAdapter(List<ChatMessage> messages) {
            this.messages = messages;
        }

        @Override
        public int getItemViewType(int position) {
            return messages.get(position).isUser() ? TYPE_SENT : TYPE_RECEIVED;
        }

        @NonNull
        @Override
        public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LinearLayout container = new LinearLayout(parent.getContext());
            container.setLayoutParams(new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            container.setPadding(0, dpToPx(4), 0, dpToPx(4));

            MaterialCardView card = new MaterialCardView(parent.getContext());
            card.setRadius(dpToPx(14));
            card.setStrokeWidth(0);

            LinearLayout innerLayout = new LinearLayout(parent.getContext());
            innerLayout.setOrientation(LinearLayout.VERTICAL);
            innerLayout.setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(8));

            TextView tvMessage = new TextView(parent.getContext());
            tvMessage.setTextSize(15);
            tvMessage.setLineSpacing(0, 1.1f);

            TextView tvTime = new TextView(parent.getContext());
            tvTime.setTextSize(10);

            LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            timeParams.gravity = Gravity.END;
            timeParams.topMargin = dpToPx(4);
            tvTime.setLayoutParams(timeParams);

            innerLayout.addView(tvMessage);
            innerLayout.addView(tvTime);
            card.addView(innerLayout);
            container.addView(card);

            return new MessageViewHolder(container, card, tvMessage, tvTime);
        }

        @Override
        public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
            ChatMessage chatMessage = messages.get(position);
            holder.tvMessage.setText(chatMessage.getMessage());
            holder.tvTime.setText(chatMessage.getTimestamp());

            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            if (chatMessage.isUser()) {
                holder.container.setGravity(Gravity.END);
                holder.cardView.setCardBackgroundColor(Color.parseColor("#1E88E5"));
                holder.tvMessage.setTextColor(Color.WHITE);
                holder.tvTime.setTextColor(Color.parseColor("#E0E0E0"));
                cardParams.setMargins(dpToPx(64), 0, 0, 0);
            } else {
                holder.container.setGravity(Gravity.START);
                holder.cardView.setCardBackgroundColor(Color.WHITE);
                holder.tvMessage.setTextColor(Color.parseColor("#212121"));
                holder.tvTime.setTextColor(Color.parseColor("#9E9E9E"));
                cardParams.setMargins(0, 0, dpToPx(64), 0);
            }

            holder.cardView.setLayoutParams(cardParams);
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        class MessageViewHolder extends RecyclerView.ViewHolder {
            LinearLayout container;
            MaterialCardView cardView;
            TextView tvMessage;
            TextView tvTime;

            public MessageViewHolder(@NonNull View itemView, MaterialCardView cardView, TextView tvMessage, TextView tvTime) {
                super(itemView);
                this.container = (LinearLayout) itemView;
                this.cardView = cardView;
                this.tvMessage = tvMessage;
                this.tvTime = tvTime;
            }
        }
    }
}