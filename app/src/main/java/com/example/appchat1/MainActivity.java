package com.example.appchat1;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    public static class Message {
        public String text;
        public boolean isSentByMe;
        public String timestamp;
        public String senderName;

        public Message(String text, boolean isSentByMe, String senderName) {
            this.text = text;
            this.isSentByMe = isSentByMe;
            this.senderName = senderName;
            this.timestamp = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
        }
    }

    private RecyclerView rvMessages;
    private EditText etMessage;
    private FloatingActionButton btnSend;
    private ImageButton btnClearChat;
    private TextView tvActiveContact;
    private TextView tvStatus;
    private ChipGroup chipGroupContacts;

    private MessageAdapter messageAdapter;
    private final Map<String, List<Message>> conversationMap = new HashMap<>();
    private String currentContact = "AI Bot 🤖";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();

    private final String[] contacts = {"AI Bot 🤖", "Sarah 👩", "Tech Support 🛠️", "Alex 👨"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupContactChips();
        initDummyData();
        loadConversation(currentContact);
    }

    private void initViews() {
        rvMessages = findViewById(R.id.rvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnClearChat = findViewById(R.id.btnClearChat);
        tvActiveContact = findViewById(R.id.tvActiveContact);
        tvStatus = findViewById(R.id.tvStatus);
        chipGroupContacts = findViewById(R.id.chipGroupContacts);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);

        btnSend.setOnClickListener(v -> sendMessage());

        btnClearChat.setOnClickListener(v -> {
            List<Message> list = conversationMap.get(currentContact);
            if (list != null) {
                list.clear();
                messageAdapter.notifyDataSetChanged();
                Toast.makeText(MainActivity.this, "Chat cleared", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupContactChips() {
        chipGroupContacts.removeAllViews();
        for (String contact : contacts) {
            Chip chip = new Chip(this);
            chip.setText(contact);
            chip.setCheckable(true);
            chip.setClickable(true);

            if (contact.equals(currentContact)) {
                chip.setChecked(true);
            }

            chip.setOnClickListener(v -> {
                currentContact = contact;
                tvActiveContact.setText(currentContact);
                tvStatus.setText("Online • Tap contact to switch");
                loadConversation(currentContact);
            });

            chipGroupContacts.addView(chip);
        }
    }

    private void initDummyData() {
        List<Message> aiMessages = new ArrayList<>();
        aiMessages.add(new Message("Hello! I am your AI assistant in App_chat_1. How can I help you today?", false, "AI Bot 🤖"));
        conversationMap.put("AI Bot 🤖", aiMessages);

        List<Message> sarahMessages = new ArrayList<>();
        sarahMessages.add(new Message("Hey! Are we still meeting for lunch today?", false, "Sarah 👩"));
        sarahMessages.add(new Message("Yes! Let's meet at 1 PM.", true, "Me"));
        sarahMessages.add(new Message("Awesome, see you then!", false, "Sarah 👩"));
        conversationMap.put("Sarah 👩", sarahMessages);

        List<Message> techMessages = new ArrayList<>();
        techMessages.add(new Message("Welcome to Support. Let us know if you experience any issue.", false, "Tech Support 🛠️"));
        conversationMap.put("Tech Support 🛠️", techMessages);

        List<Message> alexMessages = new ArrayList<>();
        alexMessages.add(new Message("Did you check out the new Android release?", false, "Alex 👨"));
        conversationMap.put("Alex 👨", alexMessages);
    }

    private void loadConversation(String contact) {
        List<Message> list = conversationMap.get(contact);
        if (list == null) {
            list = new ArrayList<>();
            conversationMap.put(contact, list);
        }
        messageAdapter = new MessageAdapter(list);
        rvMessages.setAdapter(messageAdapter);
        rvMessages.scrollToPosition(Math.max(0, list.size() - 1));
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        List<Message> currentList = conversationMap.get(currentContact);
        if (currentList == null) {
            currentList = new ArrayList<>();
            conversationMap.put(currentContact, currentList);
        }

        Message userMsg = new Message(text, true, "Me");
        currentList.add(userMsg);
        messageAdapter.notifyItemInserted(currentList.size() - 1);
        rvMessages.smoothScrollToPosition(currentList.size() - 1);

        etMessage.setText("");

        tvStatus.setText("typing...");
        final String activeContactAtSend = currentContact;
        handler.postDelayed(() -> generateBotResponse(activeContactAtSend, text), 1200);
    }

    private void generateBotResponse(String contact, String userText) {
        tvStatus.setText("Online • Tap contact to switch");
        List<Message> list = conversationMap.get(contact);
        if (list == null) return;

        String replyText;
        if (contact.contains("AI Bot")) {
            replyText = getAIReply(userText);
        } else if (contact.contains("Sarah")) {
            replyText = "Sounds good! Talk to you soon 😊";
        } else if (contact.contains("Tech Support")) {
            replyText = "Thank you for reaching out! Your ticket #" + (1000 + random.nextInt(9000)) + " is currently active.";
        } else {
            replyText = "Got it! Thanks for letting me know.";
        }

        Message replyMsg = new Message(replyText, false, contact);
        list.add(replyMsg);

        if (contact.equals(currentContact)) {
            messageAdapter.notifyItemInserted(list.size() - 1);
            rvMessages.smoothScrollToPosition(list.size() - 1);
        }
    }

    private String getAIReply(String userMsg) {
        String lower = userMsg.toLowerCase(Locale.ROOT);
        if (lower.contains("hello") || lower.contains("hi") || lower.contains("hey")) {
            return "Hello there! Hope you're having a great day!";
        } else if (lower.contains("how are you")) {
            return "I'm running smoothly! Ready to assist you.";
        } else if (lower.contains("name")) {
            return "I am App_chat_1 Bot, built with Android Java!";
        } else if (lower.contains("time")) {
            return "Current time is " + new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
        } else {
            String[] generic = {
                "That's interesting! Tell me more.",
                "I understand! Let's keep chatting.",
                "Fascinating point. What else is on your mind?",
                "App_chat_1 is working seamlessly!"
            };
            return generic[random.nextInt(generic.length)];
        }
    }

    private class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

        private final List<Message> messages;

        public MessageAdapter(List<Message> messages) {
            this.messages = messages;
        }

        @Override
        public int getItemViewType(int position) {
            return messages.get(position).isSentByMe ? 1 : 0;
        }

        @NonNull
        @Override
        public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LinearLayout container = new LinearLayout(parent.getContext());
            container.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            container.setOrientation(LinearLayout.VERTICAL);
            container.setPadding(8, 8, 8, 8);

            LinearLayout bubbleLayout = new LinearLayout(parent.getContext());
            bubbleLayout.setOrientation(LinearLayout.VERTICAL);
            bubbleLayout.setPadding(28, 16, 28, 16);

            GradientDrawable shape = new GradientDrawable();
            shape.setCornerRadius(32f);

            TextView tvSender = new TextView(parent.getContext());
            tvSender.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            tvSender.setPadding(0, 0, 0, 4);

            TextView tvText = new TextView(parent.getContext());
            tvText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);

            TextView tvTime = new TextView(parent.getContext());
            tvTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
            tvTime.setPadding(0, 4, 0, 0);

            bubbleLayout.addView(tvSender);
            bubbleLayout.addView(tvText);
            bubbleLayout.addView(tvTime);

            if (viewType == 1) { // Sent by me
                container.setGravity(Gravity.END);
                shape.setColor(Color.parseColor("#0084FF"));
                tvText.setTextColor(Color.WHITE);
                tvTime.setTextColor(Color.parseColor("#D0E4FF"));
                tvSender.setVisibility(View.GONE);
            } else { // Received
                container.setGravity(Gravity.START);
                shape.setColor(Color.WHITE);
                tvText.setTextColor(Color.parseColor("#1C1C1E"));
                tvTime.setTextColor(Color.parseColor("#8E8E93"));
                tvSender.setTextColor(Color.parseColor("#0084FF"));
                tvSender.setVisibility(View.VISIBLE);
            }

            bubbleLayout.setBackground(shape);
            container.addView(bubbleLayout);

            return new MessageViewHolder(container, tvSender, tvText, tvTime);
        }

        @Override
        public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
            Message msg = messages.get(position);
            holder.tvText.setText(msg.text);
            holder.tvTime.setText(msg.timestamp);

            if (!msg.isSentByMe) {
                holder.tvSender.setText(msg.senderName);
            }
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        class MessageViewHolder extends RecyclerView.ViewHolder {
            TextView tvSender, tvText, tvTime;

            public MessageViewHolder(@NonNull View itemView, TextView tvSender, TextView tvText, TextView tvTime) {
                super(itemView);
                this.tvSender = tvSender;
                this.tvText = tvText;
                this.tvTime = tvTime;
            }
        }
    }
}