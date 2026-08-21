package com.appchat.one;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;
    private EditText etMessage;
    private ImageButton btnSend;
    private ImageButton btnClear;
    private ImageButton btnSwitchContact;
    private TextView tvContactName;
    private TextView tvStatus;
    private TextView tvAvatar;
    private LinearLayout layoutTyping;

    private String currentContact = "AI Assistant";
    private final Handler autoReplyHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Views using findViewById
        recyclerView = findViewById(R.id.recyclerViewChat);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnClear = findViewById(R.id.btnClear);
        btnSwitchContact = findViewById(R.id.btnSwitchContact);
        tvContactName = findViewById(R.id.tvContactName);
        tvStatus = findViewById(R.id.tvStatus);
        tvAvatar = findViewById(R.id.tvAvatar);
        layoutTyping = findViewById(R.id.layoutTyping);

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(chatAdapter);

        // Setup Initial Welcome Messages
        loadInitialMessages();

        // Send Button Click
        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                sendMessage(text, true);
                etMessage.setText("");
                triggerAutoReply(text);
            }
        });

        // Quick Suggestion Chips
        findViewById(R.id.chipHello).setOnClickListener(v -> sendQuickMessage("Hello 👋"));
        findViewById(R.id.chipJoke).setOnClickListener(v -> sendQuickMessage("Tell me a joke 🎭"));
        findViewById(R.id.chipTime).setOnClickListener(v -> sendQuickMessage("What time is it? ⏰"));
        findViewById(R.id.chipHelp).setOnClickListener(v -> sendQuickMessage("How can you help me? ℹ️"));

        // Clear Chat
        btnClear.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Clear Conversation")
                    .setMessage("Are you sure you want to clear all messages?")
                    .setPositiveButton("Clear", (dialog, which) -> {
                        messageList.clear();
                        chatAdapter.notifyDataSetChanged();
                        Toast.makeText(this, "Chat cleared", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Switch Contact Dialog
        btnSwitchContact.setOnClickListener(v -> showContactSelector());
    }

    private void sendQuickMessage(String text) {
        sendMessage(text, true);
        triggerAutoReply(text);
    }

    private void sendMessage(String text, boolean isSentByMe) {
        String currentTime = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
        ChatMessage message = new ChatMessage(text, currentTime, isSentByMe);
        messageList.add(message);
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.smoothScrollToPosition(messageList.size() - 1);
    }

    private void triggerAutoReply(String userText) {
        layoutTyping.setVisibility(View.VISIBLE);
        tvStatus.setText("typing...");

        autoReplyHandler.postDelayed(() -> {
            layoutTyping.setVisibility(View.GONE);
            tvStatus.setText("Online");

            String reply = generateResponse(userText);
            sendMessage(reply, false);
        }, 1500);
    }

    private String generateResponse(String input) {
        String lower = input.toLowerCase(Locale.ROOT);
        if (lower.contains("hello") || lower.contains("hi") || lower.contains("hey")) {
            return "Hello there! 👋 How can I assist you with App_chat_1 today?";
        } else if (lower.contains("joke")) {
            String[] jokes = {
                    "Why do programmers prefer dark mode? Because light attracts bugs! 🐛",
                    "There are 10 types of people in the world: those who understand binary, and those who don't. 😄",
                    "Why did the developer leave his job? Because he didn't get arrays! 🚀"
            };
            return jokes[new Random().nextInt(jokes.length)];
        } else if (lower.contains("time")) {
            String timeStr = new SimpleDateFormat("EEEE, hh:mm:ss a", Locale.getDefault()).format(new Date());
            return "Current local time is " + timeStr;
        } else if (lower.contains("help")) {
            return "I am your AI assistant in App_chat_1! You can ask me questions, request a joke, check the time, or switch chat partners.";
        } else if (lower.contains("who are you") || lower.contains("name")) {
            return "I am " + currentContact + ", your smart companion in App_chat_1!";
        } else {
            String[] defaults = {
                    "That's interesting! Tell me more.",
                    "Got it! I am processing that information.",
                    "Fascinating! How else can I help you?",
                    "Thanks for sharing. What's on your mind next?",
                    "I am always here to chat with you on App_chat_1!"
            };
            return defaults[new Random().nextInt(defaults.length)];
        }
    }

    private void loadInitialMessages() {
        messageList.clear();
        String time = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
        messageList.add(new ChatMessage("Welcome to App_chat_1! 🎉", time, false));
        messageList.add(new ChatMessage("Type a message or click any quick suggestion chip below to start chatting.", time, false));
        chatAdapter.notifyDataSetChanged();
    }

    private void showContactSelector() {
        String[] contacts = {"AI Assistant 🤖", "Tech Support 💻", "Sarah Connor 👩‍💻", "Design Group 🎨"};
        new AlertDialog.Builder(this)
                .setTitle("Select Chat Partner")
                .setItems(contacts, (dialog, which) -> {
                    currentContact = contacts[which];
                    tvContactName.setText(currentContact);
                    tvAvatar.setText(currentContact.substring(0, 1));
                    loadInitialMessages();
                    Toast.makeText(this, "Switched to " + currentContact, Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    // --- Chat Message Model ---
    public static class ChatMessage {
        private final String text;
        private final String time;
        private final boolean isSentByMe;

        public ChatMessage(String text, String time, boolean isSentByMe) {
            this.text = text;
            this.time = time;
            this.isSentByMe = isSentByMe;
        }

        public String getText() { return text; }
        public String getTime() { return time; }
        public boolean isSentByMe() { return isSentByMe; }
    }

    // --- Adapter ---
    private class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

        private final List<ChatMessage> messages;

        public ChatAdapter(List<ChatMessage> messages) {
            this.messages = messages;
        }

        @NonNull
        @Override
        public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LinearLayout container = new LinearLayout(context);
            container.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            container.setOrientation(LinearLayout.VERTICAL);
            container.setPadding(16, 8, 16, 8);

            LinearLayout bubble = new LinearLayout(context);
            bubble.setOrientation(LinearLayout.VERTICAL);
            bubble.setPadding(28, 20, 28, 20);

            TextView tvMessage = new TextView(context);
            tvMessage.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            tvMessage.setTextColor(Color.parseColor("#1C1B1F"));

            TextView tvTime = new TextView(context);
            tvTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
            tvTime.setTextColor(Color.parseColor("#757575"));
            tvTime.setPadding(0, 6, 0, 0);

            bubble.addView(tvMessage);
            bubble.addView(tvTime);
            container.addView(bubble);

            return new ChatViewHolder(container, bubble, tvMessage, tvTime);
        }

        @Override
        public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
            ChatMessage msg = messages.get(position);
            holder.tvMessage.setText(msg.getText());

            if (msg.isSentByMe()) {
                holder.container.setGravity(Gravity.END);
                holder.tvTime.setText(msg.getTime() + "  ✓✓");
                holder.tvTime.setGravity(Gravity.END);

                GradientDrawable drawable = new GradientDrawable();
                drawable.setColor(Color.parseColor("#DCF8C6"));
                drawable.setCornerRadii(new float[]{30, 30, 8, 8, 30, 30, 30, 30});
                holder.bubble.setBackground(drawable);
            } else {
                holder.container.setGravity(Gravity.START);
                holder.tvTime.setText(msg.getTime());
                holder.tvTime.setGravity(Gravity.START);

                GradientDrawable drawable = new GradientDrawable();
                drawable.setColor(Color.parseColor("#FFFFFF"));
                drawable.setCornerRadii(new float[]{8, 8, 30, 30, 30, 30, 30, 30});
                holder.bubble.setBackground(drawable);
            }

            // Long click to copy text
            holder.container.setOnLongClickListener(v -> {
                ClipboardManager clipboard = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Chat Message", msg.getText());
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(v.getContext(), "Message copied to clipboard", Toast.LENGTH_SHORT).show();
                }
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        class ChatViewHolder extends RecyclerView.ViewHolder {
            LinearLayout container;
            LinearLayout bubble;
            TextView tvMessage;
            TextView tvTime;

            public ChatViewHolder(@NonNull View itemView, LinearLayout bubble, TextView tvMessage, TextView tvTime) {
                super(itemView);
                this.container = (LinearLayout) itemView;
                this.bubble = bubble;
                this.tvMessage = tvMessage;
                this.tvTime = tvTime;
            }
        }
    }
}