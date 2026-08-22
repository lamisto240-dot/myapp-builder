package com.yourapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NoteAdapter adapter;
    private List<Note> noteList = new ArrayList<>();
    private DatabaseHelper dbHelper;
    private LinearLayout layoutEmpty;
    private TextView tvNoteCount;
    private EditText etSearch;

    private static final String[] FISH_TAGS = {
            "Big Catch 🐋", "Keeper 🐟", "Minnow 🐠", "Trophy 🏆", "Idea 💡", "To-Do 📝"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        tvNoteCount = findViewById(R.id.tvNoteCount);
        etSearch = findViewById(R.id.etSearch);
        recyclerView = findViewById(R.id.recyclerViewNotes);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NoteAdapter(this, noteList, new NoteAdapter.OnNoteClickListener() {
            @Override
            public void onEdit(Note note) {
                showAddEditNoteDialog(note);
            }

            @Override
            public void onDelete(Note note) {
                confirmDeleteNote(note);
            }
        });
        recyclerView.setAdapter(adapter);

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddEditNoteDialog(null);
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

        loadNotes();
    }

    private void loadNotes() {
        noteList.clear();
        noteList.addAll(dbHelper.getAllNotes());
        adapter.notifyDataSetChanged();
        updateUI();
    }

    private void filterNotes(String query) {
        noteList.clear();
        if (query.trim().isEmpty()) {
            noteList.addAll(dbHelper.getAllNotes());
        } else {
            noteList.addAll(dbHelper.searchNotes(query.trim()));
        }
        adapter.notifyDataSetChanged();
        updateUI();
    }

    private void updateUI() {
        int count = noteList.size();
        tvNoteCount.setText(count + (count == 1 ? " note" : " notes"));
        if (count == 0) {
            layoutEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showAddEditNoteDialog(final Note existingNote) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_note, null);
        builder.setView(view);

        TextView tvDialogTitle = view.findViewById(R.id.tvDialogTitle);
        final EditText etTitle = view.findViewById(R.id.etNoteTitle);
        final EditText etContent = view.findViewById(R.id.etNoteContent);
        final Spinner spinnerTag = view.findViewById(R.id.spinnerTag);

        ArrayAdapter<String> tagAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, FISH_TAGS);
        spinnerTag.setAdapter(tagAdapter);

        final boolean isEdit = (existingNote != null);

        if (isEdit) {
            tvDialogTitle.setText("Edit Catch / Note");
            etTitle.setText(existingNote.getTitle());
            etContent.setText(existingNote.getContent());
            for (int i = 0; i < FISH_TAGS.length; i++) {
                if (FISH_TAGS[i].equalsIgnoreCase(existingNote.getTag())) {
                    spinnerTag.setSelection(i);
                    break;
                }
            }
        } else {
            tvDialogTitle.setText("Catch a New Note");
        }

        builder.setPositiveButton(isEdit ? "Update" : "Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String title = etTitle.getText().toString().trim();
                String content = etContent.getText().toString().trim();
                String tag = spinnerTag.getSelectedItem().toString();

                if (title.isEmpty()) {
                    title = "Untitled Note";
                }

                String timestamp = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()).format(new Date());

                if (isEdit) {
                    existingNote.setTitle(title);
                    existingNote.setContent(content);
                    existingNote.setTag(tag);
                    existingNote.setTimestamp(timestamp);
                    dbHelper.updateNote(existingNote);
                    Toast.makeText(MainActivity.this, "Note updated!", Toast.LENGTH_SHORT).show();
                } else {
                    Note newNote = new Note(0, title, content, timestamp, tag);
                    dbHelper.addNote(newNote);
                    Toast.makeText(MainActivity.this, "Note saved!", Toast.LENGTH_SHORT).show();
                }

                loadNotes();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    private void confirmDeleteNote(final Note note) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Note")
                .setMessage("Are you sure you want to release (delete) this note?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dbHelper.deleteNote(note.getId());
                        Toast.makeText(MainActivity.this, "Note deleted", Toast.LENGTH_SHORT).show();
                        loadNotes();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Model Class
    public static class Note {
        private long id;
        private String title;
        private String content;
        private String timestamp;
        private String tag;

        public Note(long id, String title, String content, String timestamp, String tag) {
            this.id = id;
            this.title = title;
            this.content = content;
            this.timestamp = timestamp;
            this.tag = tag;
        }

        public long getId() { return id; }
        public void setId(long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        public String getTag() { return tag; }
        public void setTag(String tag) { this.tag = tag; }
    }

    // Database Helper
    public static class DatabaseHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "biggest_fish_notes.db";
        private static final int DATABASE_VERSION = 1;

        private static final String TABLE_NOTES = "notes";
        private static final String COLUMN_ID = "id";
        private static final String COLUMN_TITLE = "title";
        private static final String COLUMN_CONTENT = "content";
        private static final String COLUMN_TIMESTAMP = "timestamp";
        private static final String COLUMN_TAG = "tag";

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String createTable = "CREATE TABLE " + TABLE_NOTES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TITLE + " TEXT, " +
                    COLUMN_CONTENT + " TEXT, " +
                    COLUMN_TIMESTAMP + " TEXT, " +
                    COLUMN_TAG + " TEXT" + ")";
            db.execSQL(createTable);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
            onCreate(db);
        }

        public long addNote(Note note) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_TITLE, note.getTitle());
            values.put(COLUMN_CONTENT, note.getContent());
            values.put(COLUMN_TIMESTAMP, note.getTimestamp());
            values.put(COLUMN_TAG, note.getTag());
            long id = db.insert(TABLE_NOTES, null, values);
            db.close();
            return id;
        }

        public int updateNote(Note note) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_TITLE, note.getTitle());
            values.put(COLUMN_CONTENT, note.getContent());
            values.put(COLUMN_TIMESTAMP, note.getTimestamp());
            values.put(COLUMN_TAG, note.getTag());
            int rows = db.update(TABLE_NOTES, values, COLUMN_ID + "=?", new String[]{String.valueOf(note.getId())});
            db.close();
            return rows;
        }

        public void deleteNote(long id) {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(TABLE_NOTES, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
            db.close();
        }

        public List<Note> getAllNotes() {
            List<Note> list = new ArrayList<>();
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NOTES + " ORDER BY " + COLUMN_ID + " DESC", null);

            if (cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
                    String content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT));
                    String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP));
                    String tag = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TAG));

                    list.add(new Note(id, title, content, timestamp, tag));
                } while (cursor.moveToNext());
            }
            cursor.close();
            db.close();
            return list;
        }

        public List<Note> searchNotes(String query) {
            List<Note> list = new ArrayList<>();
            SQLiteDatabase db = this.getReadableDatabase();
            String selectQuery = "SELECT * FROM " + TABLE_NOTES + " WHERE " +
                    COLUMN_TITLE + " LIKE ? OR " + COLUMN_CONTENT + " LIKE ? OR " + COLUMN_TAG + " LIKE ?" +
                    " ORDER BY " + COLUMN_ID + " DESC";
            String wildQuery = "%" + query + "%";
            Cursor cursor = db.rawQuery(selectQuery, new String[]{wildQuery, wildQuery, wildQuery});

            if (cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
                    String content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT));
                    String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP));
                    String tag = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TAG));

                    list.add(new Note(id, title, content, timestamp, tag));
                } while (cursor.moveToNext());
            }
            cursor.close();
            db.close();
            return list;
        }
    }

    // RecyclerView Adapter
    public static class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

        public interface OnNoteClickListener {
            void onEdit(Note note);
            void onDelete(Note note);
        }

        private Context context;
        private List<Note> notes;
        private OnNoteClickListener listener;

        public NoteAdapter(Context context, List<Note> notes, OnNoteClickListener listener) {
            this.context = context;
            this.notes = notes;
            this.listener = listener;
        }

        @NonNull
        @Override
        public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false);
            return new NoteViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
            final Note note = notes.get(position);
            holder.tvTitle.setText(note.getTitle());
            holder.tvContent.setText(note.getContent());
            holder.tvTimestamp.setText(note.getTimestamp());
            holder.tvCategoryTag.setText(note.getTag());

            holder.btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) listener.onEdit(note);
                }
            });

            holder.btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) listener.onDelete(note);
                }
            });
        }

        @Override
        public int getItemCount() {
            return notes.size();
        }

        static class NoteViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvContent, tvTimestamp, tvCategoryTag;
            Button btnEdit, btnDelete;

            public NoteViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvContent = itemView.findViewById(R.id.tvContent);
                tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
                tvCategoryTag = itemView.findViewById(R.id.tvCategoryTag);
                btnEdit = itemView.findViewById(R.id.btnEdit);
                btnDelete = itemView.findViewById(R.id.btnDelete);
            }
        }
    }
}