package com.yourapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    public static class Note {
        String id;
        String title;
        String content;
        String date;

        public Note(String id, String title, String content, String date) {
            this.id = id;
            this.title = title;
            this.content = content;
            this.date = date;
        }
    }

    private List<Note> allNotes = new ArrayList<>();
    private List<Note> filteredNotes = new ArrayList<>();
    private NoteAdapter adapter;

    private ListView listViewNotes;
    private TextView tvEmpty;
    private TextView tvNoteCount;
    private EditText etSearch;
    private FloatingActionButton fabAdd;

    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "appnotemb_prefs";
    private static final String KEY_NOTES = "key_notes";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        listViewNotes = findViewById(R.id.listViewNotes);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvNoteCount = findViewById(R.id.tvNoteCount);
        etSearch = findViewById(R.id.etSearch);
        fabAdd = findViewById(R.id.fabAdd);

        adapter = new NoteAdapter(this, filteredNotes);
        listViewNotes.setAdapter(adapter);

        loadNotes();

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNoteDialog(null);
            }
        });

        listViewNotes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Note selectedNote = filteredNotes.get(position);
                showNoteDialog(selectedNote);
            }
        });

        listViewNotes.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Note selectedNote = filteredNotes.get(position);
                showDeleteDialog(selectedNote);
                return true;
            }
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterNotes(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterNotes(String query) {
        filteredNotes.clear();
        if (query.trim().isEmpty()) {
            filteredNotes.addAll(allNotes);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (Note note : allNotes) {
                if (note.title.toLowerCase().contains(lowerQuery) || note.content.toLowerCase().contains(lowerQuery)) {
                    filteredNotes.add(note);
                }
            }
        }
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (filteredNotes.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            listViewNotes.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            listViewNotes.setVisibility(View.VISIBLE);
        }
        tvNoteCount.setText(allNotes.size() + " Note" + (allNotes.size() == 1 ? "" : "s") + " saved");
    }

    private void loadNotes() {
        allNotes.clear();
        String jsonString = sharedPreferences.getString(KEY_NOTES, "[]");
        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String id = obj.getString("id");
                String title = obj.getString("title");
                String content = obj.getString("content");
                String date = obj.optString("date", "");
                allNotes.add(new Note(id, title, content, date));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        filterNotes(etSearch.getText().toString());
    }

    private void saveNotes() {
        JSONArray jsonArray = new JSONArray();
        for (Note note : allNotes) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("id", note.id);
                obj.put("title", note.title);
                obj.put("content", note.content);
                obj.put("date", note.date);
                jsonArray.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        sharedPreferences.edit().putString(KEY_NOTES, jsonArray.toString()).apply();
    }

    private void showNoteDialog(@Nullable final Note existingNote) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(existingNote == null ? "Add Note" : "Edit Note");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final EditText etTitle = new EditText(this);
        etTitle.setHint("Title");
        etTitle.setText(existingNote != null ? existingNote.title : "");
        etTitle.setTextSize(16);
        layout.addView(etTitle);

        final EditText etContent = new EditText(this);
        etContent.setHint("Type note here...");
        etContent.setText(existingNote != null ? existingNote.content : "");
        etContent.setMinLines(5);
        etContent.setGravity(android.view.Gravity.TOP);
        etContent.setTextSize(14);
        layout.addView(etContent);

        builder.setView(layout);

        builder.setPositiveButton(existingNote == null ? "Save" : "Update", (dialog, which) -> {
            String title = etTitle.getText().toString().trim();
            String content = etContent.getText().toString().trim();

            if (title.isEmpty() && content.isEmpty()) {
                Toast.makeText(MainActivity.this, "Cannot save empty note", Toast.LENGTH_SHORT).show();
                return;
            }

            if (title.isEmpty()) {
                title = "Untitled Note";
            }

            String currentDate = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(new Date());

            if (existingNote == null) {
                String id = String.valueOf(System.currentTimeMillis());
                Note newNote = new Note(id, title, content, currentDate);
                allNotes.add(0, newNote);
            } else {
                existingNote.title = title;
                existingNote.content = content;
                existingNote.date = currentDate;
            }

            saveNotes();
            filterNotes(etSearch.getText().toString());
            Toast.makeText(MainActivity.this, "Note saved", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    private void showDeleteDialog(final Note note) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Note")
                .setMessage("Are you sure you want to delete this note?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    allNotes.remove(note);
                    saveNotes();
                    filterNotes(etSearch.getText().toString());
                    Toast.makeText(MainActivity.this, "Note deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private class NoteAdapter extends ArrayAdapter<Note> {

        public NoteAdapter(Context context, List<Note> notes) {
            super(context, 0, notes);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
                convertView.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);
                convertView.setPadding(24, 20, 24, 20);
            }

            Note note = getItem(position);

            TextView text1 = convertView.findViewById(android.R.id.text1);
            TextView text2 = convertView.findViewById(android.R.id.text2);

            if (note != null) {
                text1.setText(note.title);
                text1.setTextSize(18);
                text1.setTextColor(0xFF222222);

                String preview = note.content;
                if (preview.length() > 60) {
                    preview = preview.substring(0, 60) + "...";
                }
                if (!note.date.isEmpty()) {
                    preview = note.date + "\n" + preview;
                }
                text2.setText(preview);
                text2.setTextColor(0xFF666666);
                text2.setTextSize(13);
            }

            return convertView;
        }
    }
}