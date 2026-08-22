package com.yourapp;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    // UI Elements
    private TextView tvActiveAvatar;
    private TextView tvActiveName;
    private TextView tvActiveStatus;
    private ImageButton btnCall;
    private ImageButton btnClearChat;
    private ImageButton btnAttach;
    private EditText etMessageInput;
    private FloatingActionButton btnSend;
    private RecyclerView rvContacts;
    private RecyclerView rvMessages;

    // Quick Reply Buttons
    private Button btnQuick1, btnQuick2, btnQuick3, btnQuick4;

    // Data Models and Adapters
    private List<Contact> contactsList = new ArrayList<>();
    private Map<String, List<ChatMessage>> chatHistory = new HashMap<>();
    private Contact activeContact;
    
    private ContactAdapter contactAdapter;
    private MessageAdapter messageAdapter;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initData();
        setupContactRecyclerView();
        setupMessageRecyclerView();
        setupListeners();
        setupQuickReplies();

        // Select default contact
        if (!contactsList.isEmpty()) {
            selectContact(contactsList.get(0));
        }
    }

    private void initViews() {
        tvActiveAvatar = findViewById(R.id.tv_active_avatar);
        tvActiveName = findViewById(R.id.tv_active_name);
        tvActiveStatus = findViewById(R.id.tv_active_status);
        btnCall = findViewById(R.id.btn_call);
        btnClearChat = findViewById(R.id.btn_clear_chat);
        btnAttach = findViewById(R.id.btn_attach);
        etMessageInput = findViewById(R.id.et_message_input);
        btnSend = findViewById(R.id.btn_send);
        rvContacts = findViewById(R.id.rv_contacts);
        rvMessages = findViewById(R.id.rv_messages);

        btnQuick1 = findViewById(R.id.btn_quick_1);
        btnQuick2 = findViewById(R.id.btn_quick_2);
        btnQuick3 = findViewById(R.id.btn_quick_3);
        btnQuick4 = findViewById(R.id.btn_quick_4);
    }

    private void initData() {
        contactsList.add(new Contact("1", "AI Assistant", "Online", "#0088CC", true));
        contactsList.add(new Contact("2", "Alice Smith", "Online", "#E91E63", true));
        contactsList.add(new Contact("3", "Bob Miller", "Last seen 5m ago", "#FF9800", false));
        contactsList.add(new Contact("4", "Tech Support", "Online", "#4CAF50", true));
        contactsList.add(new Contact("5", "Sarah Connor", "Offline", "#9C27B0", false));

        // Seed initial chat history
        for (Contact contact : contactsList) {
            List<ChatMessage> messages = new ArrayList<>();
            String time = getCurrentFormattedTime();
            
            if (contact.getId().equals("1")) {
                messages.add(new ChatMessage("1", contact.getName(), "Hello! I am your AI assistant. How can I help you today?", time, false));
            } else if (contact.getId().equals("2")) {
                messages.add(new ChatMessage("1", contact.getName(), "Hey there! Are we still meeting for lunch today?", time, false));
            } else if (contact.getId().equals("4")) {
                messages.add(new ChatMessage("1", contact.getName(), "Welcome to Support! Type your issue below.", time, false));
            } else {
                messages.add(new ChatMessage("1", contact.getName(), "Hey! What's up?", time, false));
            }
            chatHistory.put(contact.getId(), messages);
        }
    }

    private void setupContactRecyclerView() {
        contactAdapter = new ContactAdapter(contactsList, contact -> selectContact(contact));
        rvContacts.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvContacts.setAdapter(contactAdapter);
    }

    private void setupMessageRecyclerView() {
        messageAdapter = new MessageAdapter(new ArrayList<>());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(messageAdapter);
    }

    private void selectContact(Contact contact) {
        this.activeContact = contact;
        tvActiveName.setText(contact.getName());
        tvActiveStatus.setText(contact.getStatus());
        tvActiveAvatar.setText(contact.getInitials());

        // Update header background color for avatar
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(Color.parseColor(contact.getAvatarColor()));
        tvActiveAvatar.setBackground(bg);

        // Load messages
        List<ChatMessage> messages = chatHistory.get(contact.getId());
        if (messages == null) {
            messages = new ArrayList<>();
            chatHistory.put(contact.getId(), messages);
        }
        messageAdapter.setMessages(messages);
        scrollToBottom();

        contactAdapter.setSelectedContactId(contact.getId());
    }

    private void setupListeners() {
        btnSend.setOnClickListener(v -> sendMessage());

        btnCall.setOnClickListener(v -> {
            if (activeContact != null) {
                Toast.makeText(MainActivity.this, "Calling " + activeContact.getName() + "...", Toast.LENGTH_SHORT).show();
            }
        });

        btnClearChat.setOnClickListener(v -> {
            if (activeContact != null) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Clear Chat")
                        .setMessage("Are you sure you want to clear all messages with " + activeContact.getName() + "?")
                        .setPositiveButton("Clear", (dialog, which) -> {
                            List<ChatMessage> list = chatHistory.get(activeContact.getId());
                            if (list != null) {
                                list.clear();
                                messageAdapter.setMessages(list);
                            }
                            Toast.makeText(MainActivity.this, "Chat cleared", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        btnAttach.setOnClickListener(v -> showAttachmentDialog());

        etMessageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) {
                    btnSend.setAlpha(1.0f);
                } else {
                    btnSend.setAlpha(0.7f);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupQuickReplies() {
        View.OnClickListener quickClickListener = v -> {
            Button btn = (Button) v;
            etMessageInput.setText(btn.getText().toString());
            etMessageInput.setSelection(etMessageInput.getText().length());
        };

        btnQuick1.setOnClickListener(quickClickListener);
        btnQuick2.setOnClickListener(quickClickListener);
        btnQuick3.setOnClickListener(quickClickListener);
        btnQuick4.setOnClickListener(quickClickListener);
    }

    private void sendMessage() {
        String text = etMessageInput.getText().toString().trim();
        if (text.isEmpty() || activeContact == null) return;

        String time = getCurrentFormattedTime();
        ChatMessage outgoingMessage = new ChatMessage(
                String.valueOf(System.currentTimeMillis()),
                "Me",
                text,
                time,
                true
        );

        List<ChatMessage> messages = chatHistory.get(activeContact.getId());
        if (messages != null) {
            messages.add(outgoingMessage);
            messageAdapter.setMessages(messages);
            scrollToBottom();
        }

        etMessageInput.setText("");

        // Simulate incoming auto-response after 1.5 seconds
        simulateResponse(text);
    }

    private void simulateResponse(String userMessage) {
        final String contactId = activeContact.getId();
        final String contactName = activeContact.getName();

        tvActiveStatus.setText("typing...");

        handler.postDelayed(() -> {
            if (activeContact != null && activeContact.getId().equals(contactId)) {
                tvActiveStatus.setText(activeContact.getStatus());
            }

            String replyText = generateReplyText(contactId, userMessage);
            ChatMessage response = new ChatMessage(
                    String.valueOf(System.currentTimeMillis()),
                    contactName,
                    replyText,
                    getCurrentFormattedTime(),
                    false
            );

            List<ChatMessage> messages = chatHistory.get(contactId);
            if (messages != null) {
                messages.add(response);
                if (activeContact != null && activeContact.getId().equals(contactId)) {
                    messageAdapter.setMessages(messages);
                    scrollToBottom();
                }
            }
        }, 1500);
    }

    private String generateReplyText(String contactId, String userMessage) {
        String lower = userMessage.toLowerCase();
        if (contactId.equals("1")) { // AI Assistant
            if (lower.contains("hello") || lower.contains("hi")) {
                return "Hello! How can I assist you with App_chat_1 today?";
            } else if (lower.contains("how are you")) {
                return "I'm a virtual chatbot, working perfectly! How are you?";
            } else if (lower.contains("time")) {
                return "Current time is " + getCurrentFormattedTime();
            } else {
                String[] aiResponses = {
                        "That's very interesting! Tell me more.",
                        "I understand. Let me check that for you.",
                        "App_chat_1 is running smoothly. Anything else?",
                        "Thanks for sharing that with me!"
                };
                return aiResponses[random.nextInt(aiResponses.length)];
            }
        } else if (contactId.equals("4")) { // Tech Support
            return "Thanks for reaching out to support. An agent will review your message shortly!";
        } else {
            String[] replies = {
                    "Got it, sounds great!",
                    "Awesome! I'll catch up with you later.",
                    "Sure thing!",
                    "Haha, nice! 😂",
                    "Let me check and get back to you."
            };
            return replies[random.nextInt(replies.length)];
        }
    }

    private void showAttachmentDialog() {
        String[] options = {"📷 Send Photo", "📄 Send Document", "📍 Share Location", "🎵 Audio File"};
        new AlertDialog.Builder(this)
                .setTitle("Attach Item")
                .setItems(options, (dialog, which) -> {
                    String item = options[which];
                    Toast.makeText(MainActivity.this, "Attached: " + item, Toast.LENGTH_SHORT).show();
                    etMessageInput.setText("[Attachment: " + item.substring(2) + "]");
                })
                .show();
    }

    private void scrollToBottom() {
        if (messageAdapter.getItemCount() > 0) {
            rvMessages.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
        }
    }

    private String getCurrentFormattedTime() {
        return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
    }

    // --- DATA MODELS ---

    public static class Contact {
        private String id;
        private String name;
        private String status;
        private String avatarColor;
        private boolean isOnline;

        public Contact(String id, String name, String status, String avatarColor, boolean isOnline) {
            this.id = id;
            this.name = name;
            this.status = status;
            this.avatarColor = avatarColor;
            this.isOnline = isOnline;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getStatus() { return status; }
        public String getAvatarColor() { return avatarColor; }
        public boolean isOnline() { return isOnline; }

        public String getInitials() {
            if (name == null || name.isEmpty()) return "?";
            String[] parts = name.split(" ");
            if (parts.length >= 2) {
                return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
            }
            return ("" + name.charAt(0)).toUpperCase();
        }
    }

    public static class ChatMessage {
        private String id;
        private String senderName;
        private String text;
        private String timestamp;
        private boolean isOutgoing;

        public ChatMessage(String id, String senderName, String text, String timestamp, boolean isOutgoing) {
            this.id = id;
            this.senderName = senderName;
            this.text = text;
            this.timestamp = timestamp;
            this.isOutgoing = isOutgoing;
        }

        public String getId() { return id; }
        public String getSenderName() { return senderName; }
        public String getText() { return text; }
        public String getTimestamp() { return timestamp; }
        public boolean isOutgoing() { return isOutgoing; }
    }

    // --- CONTACT ADAPTER ---

    public interface OnContactClickListener {
        void onContactClick(Contact contact);
    }

    public static class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {
        private List<Contact> contacts;
        private OnContactClickListener listener;
        private String selectedContactId = "";

        public ContactAdapter(List<Contact> contacts, OnContactClickListener listener) {
            this.contacts = contacts;
            this.listener = listener;
        }

        public void setSelectedContactId(String id) {
            this.selectedContactId = id;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
            return new ContactViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
            Contact contact = contacts.get(position);
            holder.tvName.setText(contact.getName());
            holder.tvAvatar.setText(contact.getInitials());

            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.OVAL);
            bg.setColor(Color.parseColor(contact.getAvatarColor()));
            holder.tvAvatar.setBackground(bg);

            holder.vOnlineIndicator.setVisibility(contact.isOnline() ? View.VISIBLE : View.GONE);

            if (contact.getId().equals(selectedContactId)) {
                holder.container.setAlpha(1.0f);
                holder.tvName.setTextColor(Color.YELLOW);
            } else {
                holder.container.setAlpha(0.7f);
                holder.tvName.setTextColor(Color.WHITE);
            }

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onContactClick(contact);
            });
        }

        @Override
        public int getItemCount() {
            return contacts.size();
        }

        static class ContactViewHolder extends RecyclerView.ViewHolder {
            View container;
            TextView tvAvatar, tvName;
            View vOnlineIndicator;

            public ContactViewHolder(@NonNull View itemView) {
                super(itemView);
                container = itemView.findViewById(R.id.contact_container);
                tvAvatar = itemView.findViewById(R.id.tv_contact_avatar);
                tvName = itemView.findViewById(R.id.tv_contact_name);
                vOnlineIndicator = itemView.findViewById(R.id.view_online_indicator);
            }
        }
    }

    // --- MESSAGE ADAPTER ---

    public static class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_OUTGOING = 1;
        private static final int TYPE_INCOMING = 2;

        private List<ChatMessage> messages;

        public MessageAdapter(List<ChatMessage> messages) {
            this.messages = messages;
        }

        public void setMessages(List<ChatMessage> messages) {
            this.messages = messages;
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            return messages.get(position).isOutgoing() ? TYPE_OUTGOING : TYPE_INCOMING;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == TYPE_OUTGOING) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_outgoing, parent, false);
                return new OutgoingViewHolder(v);
            } else {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_incoming, parent, false);
                return new IncomingViewHolder(v);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ChatMessage msg = messages.get(position);
            if (holder instanceof OutgoingViewHolder) {
                OutgoingViewHolder vh = (OutgoingViewHolder) holder;
                vh.tvText.setText(msg.getText());
                vh.tvTime.setText(msg.getTimestamp());
            } else if (holder instanceof IncomingViewHolder) {
                IncomingViewHolder vh = (IncomingViewHolder) holder;
                vh.tvSender.setText(msg.getSenderName());
                vh.tvText.setText(msg.getText());
                vh.tvTime.setText(msg.getTimestamp());
            }
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        static class OutgoingViewHolder extends RecyclerView.ViewHolder {
            TextView tvText, tvTime, tvStatus;

            public OutgoingViewHolder(@NonNull View itemView) {
                super(itemView);
                tvText = itemView.findViewById(R.id.tv_message_text);
                tvTime = itemView.findViewById(R.id.tv_message_time);
                tvStatus = itemView.findViewById(R.id.tv_message_status);
            }
        }

        static class IncomingViewHolder extends RecyclerView.ViewHolder {
            TextView tvSender, tvText, tvTime;

            public IncomingViewHolder(@NonNull View itemView) {
                super(itemView);
                tvSender = itemView.findViewById(R.id.tv_sender_name);
                tvText = itemView.findViewById(R.id.tv_message_text);
                tvTime = itemView.findViewById(R.id.tv_message_time);
            }
        }
    }
}