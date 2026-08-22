package com.yourapp;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvMessages;
    private EditText etMessage;
    private FloatingActionButton btnSend;
    private ImageButton btnClearChat;
    private ImageButton btnAttach;
    private TextView tvStatus;
    private Chip chipHello, chipJoke, chipHowAreYou, chipQuote;

    private ChatAdapter adapter;
    private List<ChatMessage> messageList;
    private Handler handler;

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler(Looper.getMainLooper());
        messageList = new ArrayList<>();

        // Initialize UI Elements
        rvMessages = findViewById(R.id.rvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnClearChat = findViewById(R.id.btnClearChat);
        btnAttach = findViewById(R.id.btnAttach);
        tvStatus = findViewById(R.id.tvStatus);

        chipHello = findViewById(R.id.chipHello);
        chipJoke = findViewById(R.id.chipJoke);
        chipHowAreYou = findViewById(R.id.chipHowAreYou);
        chipQuote = findViewById(R.id.chipQuote);

        // Setup RecyclerView
        adapter = new ChatAdapter(messageList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(adapter);

        // Welcome message
        addReceivedMessage("Hello! 👋 I'm your AI Companion. How can I help you today?");

        // Listeners
        btnSend.setOnClickListener(v -> sendMessage());

        btnAttach.setOnClickListener(v -> {
            sendUserMessage("📷 [Sent an Image Attachment]");
            simulateBotReply("That's a fantastic photo! 📸✨");
        });

        btnClearChat.setOnClickListener(v -> {
            messageList.clear();
            adapter.notifyDataSetChanged();
            addReceivedMessage("Chat cleared! How can I help you?");
            Toast.makeText(MainActivity.this, "Chat history cleared", Toast.LENGTH_SHORT).show();
        });

        // Quick Reply Chips
        chipHello.setOnClickListener(v -> handleQuickReply("Hello! 👋"));
        chipJoke.setOnClickListener(v -> handleQuickReply("Tell me a joke 😄"));
        chipHowAreYou.setOnClickListener(v -> handleQuickReply("How are you? 🤔"));
        chipQuote.setOnClickListener(v -> handleQuickReply("Inspire me ✨"));
    }

    private void handleQuickReply(String text) {
        etMessage.setText(text);
        sendMessage();
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (!TextUtils.isEmpty(text)) {
            sendUserMessage(text);
            etMessage.setText("");
            simulateBotReply(text);
        }
    }

    private void sendUserMessage(String text) {
        messageList.add(new ChatMessage(text, getCurrentTime(), true));
        adapter.notifyItemInserted(messageList.size() - 1);
        rvMessages.smoothScrollToPosition(messageList.size() - 1);
    }

    private void addReceivedMessage(String text) {
        messageList.add(new ChatMessage(text, getCurrentTime(), false));
        adapter.notifyItemInserted(messageList.size() - 1);
        rvMessages.smoothScrollToPosition(messageList.size() - 1);
    }

    private void simulateBotReply(String userQuery) {
        tvStatus.setText(R.string.status_typing);

        handler.postDelayed(() -> {
            tvStatus.setText(R.string.status_online);
            String response = generateBotResponse(userQuery.toLowerCase(Locale.ROOT));
            addReceivedMessage(response);
        }, 1200);
    }

    private String generateBotResponse(String input) {
        if (input.contains("hello") || input.contains("hi") || input.contains("hey")) {
            return "Hey there! Nice to chat with you! 😊";
        } else if (input.contains("joke")) {
            String[] jokes = {
                "Why don't programmers like nature? It has too many bugs! 🐛",
                "There are 10 types of people in the world: those who understand binary, and those who don't! 😂",
                "Why did the computer keep sneezing? It had a virus! 🤒"
            };
            return jokes[new Random().nextInt(jokes.length)];
        } else if (input.contains("how are you")) {
            return "I'm operating at 100% efficiency and full of energy! How are you doing today?";
        } else if (input.contains("inspire") || input.contains("quote")) {
            String[] quotes = {
                "\"The best way to predict the future is to create it.\" - Peter Drucker ✨",
                "\"Believe you can and you're halfway there.\" - Theodore Roosevelt 🚀",
                "\"Small steps every day lead to big results!\" 💪"
            };
            return quotes[new Random().nextInt(quotes.length)];
        } else if (input.contains("photo") || input.contains("image")) {
            return "I love looking at pictures! Very cool! 🖼️";
        } else {
            String[] defaults = {
                "That's super interesting! Tell me more! 🤔",
                "I agree with you! What else is on your mind?",
                "Fascinating point! How can I assist you further?",
                "Thanks for sharing! Chatting with you is great! 👍"
            };
            return defaults[new Random().nextInt(defaults.length)];
        }
    }

    private String getCurrentTime() {
        return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
    }

    // Model
    public static class ChatMessage {
        private final String text;
        private final String timestamp;
        private final boolean isSent;

        public ChatMessage(String text, String timestamp, boolean isSent) {
            this.text = text;
            this.timestamp = timestamp;
            this.isSent = isSent;
        }

        public String getText() {
            return text;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public boolean isSent() {
            return isSent;
        }
    }

    // Adapter
    private class ChatAdapter extends RecyclerView.Adapter<ChatViewHolder> {

        private final List<ChatMessage> messages;

        public ChatAdapter(List<ChatMessage> messages) {
            this.messages = messages;
        }

        @Override
        public int getItemViewType(int position) {
            return messages.get(position).isSent() ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
        }

        @NonNull
        @Override
        public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LinearLayout rootLayout = new LinearLayout(context);
            rootLayout.setLayoutParams(new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            rootLayout.setOrientation(LinearLayout.VERTICAL);
            rootLayout.setPadding(12, 6, 12, 6);

            MaterialCardView cardView = new MaterialCardView(context);
            cardView.setRadius(28f);
            cardView.setCardElevation(2f);
            cardView.setUseCompatPadding(true);

            LinearLayout container = new LinearLayout(context);
            container.setOrientation(LinearLayout.VERTICAL);
            container.setPadding(28, 18, 28, 18);

            TextView tvMessage = new TextView(context);
            tvMessage.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);

            TextView tvTime = new TextView(context);
            tvTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
            tvTime.setPadding(0, 6, 0, 0);

            container.addView(tvMessage);
            container.addView(tvTime);
            cardView.addView(container);
            rootLayout.addView(cardView);

            return new ChatViewHolder(rootLayout, cardView, tvMessage, tvTime);
        }

        @Override
        public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
            ChatMessage msg = messages.get(position);
            holder.tvMessage.setText(msg.getText());
            holder.tvTime.setText(msg.getTimestamp());

            if (getItemViewType(position) == VIEW_TYPE_SENT) {
                holder.rootLayout.setGravity(Gravity.END);
                holder.cardView.setCardBackgroundColor(0xFF6200EE);
                holder.tvMessage.setTextColor(0xFFFFFFFF);
                holder.tvTime.setTextColor(0xD0FFFFFF);
                holder.tvTime.setGravity(Gravity.END);
            } else {
                holder.rootLayout.setGravity(Gravity.START);
                holder.cardView.setCardBackgroundColor(0xFFFFFFFF);
                holder.tvMessage.setTextColor(0xFF1F1F1F);
                holder.tvTime.setTextColor(0xFF888888);
                holder.tvTime.setGravity(Gravity.START);
            }
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }
    }

    // ViewHolder
    private static class ChatViewHolder extends RecyclerView.ViewHolder {
        LinearLayout rootLayout;
        MaterialCardView cardView;
        TextView tvMessage;
        TextView tvTime;

        public ChatViewHolder(@NonNull View itemView, MaterialCardView cardView, TextView tvMessage, TextView tvTime) {
            super(itemView);
            this.rootLayout = (LinearLayout) itemView;
            this.cardView = cardView;
            this.tvMessage = tvMessage;
            this.tvTime = tvTime;
        }
    }
}