package com.yourapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
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
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotesAdapter adapter;
    private List<Note> noteList;
    private List<Note> filteredList;
    private DatabaseHelper dbHelper;
    private LinearLayout layoutEmpty;
    private EditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        noteList = new ArrayList<>();
        filteredList = new ArrayList<>();

        recyclerView = findViewById(R.id.recyclerViewNotes);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        etSearch = findViewById(R.id.etSearch);
        FloatingActionButton fabAddNote = findViewById(R.id.fabAddNote);

        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        adapter = new NotesAdapter(filteredList);
        recyclerView.setAdapter(adapter);

        fabAddNote.setOnClickListener(v -> showNoteDialog(null));

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
        filterNotes(etSearch.getText().toString());
    }

    private void filterNotes(String query) {
        filteredList.clear();
        if (query.trim().isEmpty()) {
            filteredList.addAll(noteList);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (Note note : noteList) {
                if (note.getTitle().toLowerCase().contains(lowerQuery) ||
                        note.getContent().toLowerCase().contains(lowerQuery)) {
                    filteredList.add(note);
                }
            }
        }
        adapter.notifyDataSetChanged();

        if (filteredList.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showNoteDialog(Note noteToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_note, null);
        builder.setView(dialogView);

        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        EditText etNoteTitle = dialogView.findViewById(R.id.etNoteTitle);
        EditText etNoteContent = dialogView.findViewById(R.id.etNoteContent);

        if (noteToEdit != null) {
            tvDialogTitle.setText("Edit Note");
            etNoteTitle.setText(noteToEdit.getTitle());
            etNoteContent.setText(noteToEdit.getContent());
        } else {
            tvDialogTitle.setText("New Note");
        }

        builder.setPositiveButton("Save", (dialog, which) -> {
            String title = etNoteTitle.getText().toString().trim();
            String content = etNoteContent.getText().toString().trim();

            if (title.isEmpty() && content.isEmpty()) {
                Toast.makeText(MainActivity.this, "Cannot save empty note", Toast.LENGTH_SHORT).show();
                return;
            }

            if (title.isEmpty()) {
                title = "Untitled Note";
            }

            String date = new SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()).format(new Date());

            if (noteToEdit == null) {
                Note newNote = new Note(-1, title, content, date);
                dbHelper.addNote(newNote);
            } else {
                noteToEdit.setTitle(title);
                noteToEdit.setContent(content);
                noteToEdit.setDate(date);
                dbHelper.updateNote(noteToEdit);
            }

            loadNotes();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void confirmDelete(Note note) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Note")
                .setMessage("Are you sure you want to delete this note?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deleteNote(note.getId());
                    loadNotes();
                    Toast.makeText(MainActivity.this, "Note deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // --- Model Class ---
    public static class Note {
        private long id;
        private String title;
        private String content;
        private String date;

        public Note(long id, String title, String content, String date) {
            this.id = id;
            this.title = title;
            this.content = content;
            this.date = date;
        }

        public long getId() { return id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
    }

    // --- Adapter Class ---
    private class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

        private final List<Note> notes;

        public NotesAdapter(List<Note> notes) {
            this.notes = notes;
        }

        @NonNull
        @Override
        public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
            return new NoteViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
            Note note = notes.get(position);
            holder.tvTitle.setText(note.getTitle());
            holder.tvContent.setText(note.getContent());
            holder.tvDate.setText(note.getDate());

            holder.itemView.setOnClickListener(v -> showNoteDialog(note));
            holder.btnDelete.setOnClickListener(v -> confirmDelete(note));
        }

        @Override
        public int getItemCount() {
            return notes.size();
        }

        class NoteViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvContent, tvDate;
            ImageButton btnDelete;

            public NoteViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvNoteTitle);
                tvContent = itemView.findViewById(R.id.tvNoteContent);
                tvDate = itemView.findViewById(R.id.tvNoteDate);
                btnDelete = itemView.findViewById(R.id.btnDelete);
            }
        }
    }

    // --- Database Helper ---
    private static class DatabaseHelper extends SQLiteOpenHelper {

        private static final String DATABASE_NAME = "noty.db";
        private static final int DATABASE_VERSION = 1;

        private static final String TABLE_NOTES = "notes";
        private static final String COLUMN_ID = "id";
        private static final String COLUMN_TITLE = "title";
        private static final String COLUMN_CONTENT = "content";
        private static final String COLUMN_DATE = "date";

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String CREATE_NOTES_TABLE = "CREATE TABLE " + TABLE_NOTES + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_TITLE + " TEXT,"
                    + COLUMN_CONTENT + " TEXT,"
                    + COLUMN_DATE + " TEXT" + ")";
            db.execSQL(CREATE_NOTES_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
            onCreate(db);
        }

        public void addNote(Note note) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_TITLE, note.getTitle());
            values.put(COLUMN_CONTENT, note.getContent());
            values.put(COLUMN_DATE, note.getDate());

            db.insert(TABLE_NOTES, null, values);
            db.close();
        }

        public List<Note> getAllNotes() {
            List<Note> noteList = new ArrayList<>();
            String selectQuery = "SELECT * FROM " + TABLE_NOTES + " ORDER BY " + COLUMN_ID + " DESC";

            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
                    String content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT));
                    String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));

                    Note note = new Note(id, title, content, date);
                    noteList.add(note);
                } while (cursor.moveToNext());
            }

            cursor.close();
            db.close();
            return noteList;
        }

        public void updateNote(Note note) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_TITLE, note.getTitle());
            values.put(COLUMN_CONTENT, note.getContent());
            values.put(COLUMN_DATE, note.getDate());

            db.update(TABLE_NOTES, values, COLUMN_ID + " = ?", new String[]{String.valueOf(note.getId())});
            db.close();
        }

        public void deleteNote(long id) {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(TABLE_NOTES, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
            db.close();
        }
    }
}