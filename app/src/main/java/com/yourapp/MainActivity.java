package com.yourapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    private EditText etContent;
    private TextView tvFileName;
    private TextView tvStats;
    private LinearLayout searchContainer;
    private EditText etSearch;
    private EditText etReplace;

    private Uri currentFileUri = null;
    private String currentFileName = "Untitled.txt";
    private boolean isModified = false;
    private float currentFontSize = 16f; // sp
    private boolean isMonospace = false;

    // Undo / Redo stacks
    private final Stack<String> undoStack = new Stack<>();
    private final Stack<String> redoStack = new Stack<>();
    private boolean isUndoRedoOperation = false;
    private String lastPushedText = "";

    // File Pickers using Storage Access Framework
    private final ActivityResultLauncher<String[]> openFileLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    openUri(uri);
                }
            });

    private final ActivityResultLauncher<String> saveFileLauncher =
            registerForActivityResult(new ActivityResultContracts.CreateDocument("text/plain"), uri -> {
                if (uri != null) {
                    currentFileUri = uri;
                    saveToUri(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupListeners();
        updateTitle();
        updateStats();
    }

    private void initViews() {
        etContent = findViewById(R.id.et_content);
        tvFileName = findViewById(R.id.tv_file_name);
        tvStats = findViewById(R.id.tv_stats);
        searchContainer = findViewById(R.id.search_container);
        etSearch = findViewById(R.id.et_search);
        etReplace = findViewById(R.id.et_replace);

        Button btnNew = findViewById(R.id.btn_new);
        Button btnOpen = findViewById(R.id.btn_open);
        Button btnSave = findViewById(R.id.btn_save);
        Button btnSearchToggle = findViewById(R.id.btn_search_toggle);
        Button btnShare = findViewById(R.id.btn_share);
        Button btnUndo = findViewById(R.id.btn_undo);
        Button btnRedo = findViewById(R.id.btn_redo);

        Button btnFindNext = findViewById(R.id.btn_find_next);
        Button btnDoReplace = findViewById(R.id.btn_do_replace);
        Button btnReplaceAll = findViewById(R.id.btn_replace_all);
        Button btnCloseSearch = findViewById(R.id.btn_close_search);

        Button btnZoomIn = findViewById(R.id.btn_zoom_in);
        Button btnZoomOut = findViewById(R.id.btn_zoom_out);
        Button btnToggleFont = findViewById(R.id.btn_toggle_font);

        btnNew.setOnClickListener(v -> actionNewFile());
        btnOpen.setOnClickListener(v -> actionOpenFile());
        btnSave.setOnClickListener(v -> actionSaveFile());
        btnSearchToggle.setOnClickListener(v -> toggleSearchContainer());
        btnShare.setOnClickListener(v -> actionShareText());
        btnUndo.setOnClickListener(v -> actionUndo());
        btnRedo.setOnClickListener(v -> actionRedo());

        btnFindNext.setOnClickListener(v -> findNext());
        btnDoReplace.setOnClickListener(v -> replaceNext());
        btnReplaceAll.setOnClickListener(v -> replaceAll());
        btnCloseSearch.setOnClickListener(v -> searchContainer.setVisibility(View.GONE));

        btnZoomIn.setOnClickListener(v -> changeFontSize(2f));
        btnZoomOut.setOnClickListener(v -> changeFontSize(-2f));
        btnToggleFont.setOnClickListener(v -> toggleFontType());
    }

    private void setupListeners() {
        etContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (!isUndoRedoOperation && lastPushedText == null) {
                    lastPushedText = s.toString();
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateStats();
                if (!isUndoRedoOperation) {
                    String currentText = s.toString();
                    if (!currentText.equals(lastPushedText)) {
                        if (lastPushedText != null) {
                            undoStack.push(lastPushedText);
                            redoStack.clear();
                        }
                        lastPushedText = currentText;
                        if (!isModified) {
                            isModified = true;
                            updateTitle();
                        }
                    }
                }
            }
        });
    }

    private void actionNewFile() {
        if (isModified) {
            new AlertDialog.Builder(this)
                    .setTitle("Unsaved Changes")
                    .setMessage("You have unsaved changes. Create new file anyway?")
                    .setPositiveButton("Yes", (dialog, which) -> createNewFile())
                    .setNegativeButton("No", null)
                    .show();
        } else {
            createNewFile();
        }
    }

    private void createNewFile() {
        isUndoRedoOperation = true;
        etContent.setText("");
        isUndoRedoOperation = false;

        currentFileUri = null;
        currentFileName = "Untitled.txt";
        isModified = false;
        undoStack.clear();
        redoStack.clear();
        lastPushedText = "";
        updateTitle();
        Toast.makeText(this, "New file created", Toast.LENGTH_SHORT).show();
    }

    private void actionOpenFile() {
        if (isModified) {
            new AlertDialog.Builder(this)
                    .setTitle("Unsaved Changes")
                    .setMessage("You have unsaved changes. Open file anyway?")
                    .setPositiveButton("Yes", (dialog, which) -> openFileLauncher.launch(new String[]{"text/plain", "text/*", "*/*"}))
                    .setNegativeButton("No", null)
                    .show();
        } else {
            openFileLauncher.launch(new String[]{"text/plain", "text/*", "*/*"});
        }
    }

    private void actionSaveFile() {
        if (currentFileUri != null) {
            saveToUri(currentFileUri);
        } else {
            saveFileLauncher.launch(currentFileName);
        }
    }

    private void openUri(Uri uri) {
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }

            if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n') {
                sb.setLength(sb.length() - 1);
            }

            currentFileUri = uri;
            currentFileName = getFileNameFromUri(uri);

            isUndoRedoOperation = true;
            etContent.setText(sb.toString());
            isUndoRedoOperation = false;

            undoStack.clear();
            redoStack.clear();
            lastPushedText = sb.toString();
            isModified = false;

            updateTitle();
            Toast.makeText(this, "File opened", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error reading file", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveToUri(Uri uri) {
        try (OutputStream outputStream = getContentResolver().openOutputStream(uri, "wt")) {
            if (outputStream != null) {
                outputStream.write(etContent.getText().toString().getBytes());
                outputStream.flush();
                isModified = false;
                currentFileName = getFileNameFromUri(uri);
                updateTitle();
                Toast.makeText(this, "Saved successfully", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error saving file", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception ignored) {}
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result != null ? result.lastIndexOf('/') : -1;
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result != null ? result : "Untitled.txt";
    }

    private void actionShareText() {
        String text = etContent.getText().toString();
        if (text.isEmpty()) {
            Toast.makeText(this, "Nothing to share", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, currentFileName);
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(shareIntent, "Share text via"));
    }

    private void actionUndo() {
        if (!undoStack.isEmpty()) {
            String currentText = etContent.getText().toString();
            redoStack.push(currentText);

            String prevText = undoStack.pop();
            isUndoRedoOperation = true;
            etContent.setText(prevText);
            etContent.setSelection(prevText.length());
            isUndoRedoOperation = false;
            lastPushedText = prevText;

            updateTitle();
        } else {
            Toast.makeText(this, "Nothing to undo", Toast.LENGTH_SHORT).show();
        }
    }

    private void actionRedo() {
        if (!redoStack.isEmpty()) {
            String currentText = etContent.getText().toString();
            undoStack.push(currentText);

            String nextText = redoStack.pop();
            isUndoRedoOperation = true;
            etContent.setText(nextText);
            etContent.setSelection(nextText.length());
            isUndoRedoOperation = false;
            lastPushedText = nextText;

            updateTitle();
        } else {
            Toast.makeText(this, "Nothing to redo", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleSearchContainer() {
        if (searchContainer.getVisibility() == View.VISIBLE) {
            searchContainer.setVisibility(View.GONE);
        } else {
            searchContainer.setVisibility(View.VISIBLE);
            etSearch.requestFocus();
        }
    }

    private void findNext() {
        String query = etSearch.getText().toString();
        if (query.isEmpty()) return;

        String content = etContent.getText().toString();
        int selectionStart = etContent.getSelectionEnd();
        int index = content.toLowerCase().indexOf(query.toLowerCase(), selectionStart);

        if (index == -1) {
            // Loop back to start
            index = content.toLowerCase().indexOf(query.toLowerCase(), 0);
        }

        if (index != -1) {
            etContent.requestFocus();
            etContent.setSelection(index, index + query.length());
        } else {
            Toast.makeText(this, "Text not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void replaceNext() {
        String query = etSearch.getText().toString();
        String replaceWith = etReplace.getText().toString();
        if (query.isEmpty()) return;

        int start = etContent.getSelectionStart();
        int end = etContent.getSelectionEnd();
        String selectedText = etContent.getText().subSequence(start, end).toString();

        if (selectedText.equalsIgnoreCase(query)) {
            etContent.getText().replace(start, end, replaceWith);
            findNext();
        } else {
            findNext();
        }
    }

    private void replaceAll() {
        String query = etSearch.getText().toString();
        String replaceWith = etReplace.getText().toString();
        if (query.isEmpty()) return;

        String content = etContent.getText().toString();
        String updated = content.replaceAll("(?i)" + java.util.regex.Pattern.quote(query), replaceWith);
        etContent.setText(updated);
        Toast.makeText(this, "Replaced all occurrences", Toast.LENGTH_SHORT).show();
    }

    private void changeFontSize(float delta) {
        float newSize = currentFontSize + delta;
        if (newSize >= 10f && newSize <= 40f) {
            currentFontSize = newSize;
            etContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, currentFontSize);
        }
    }

    private void toggleFontType() {
        isMonospace = !isMonospace;
        if (isMonospace) {
            etContent.setTypeface(Typeface.MONOSPACE);
        } else {
            etContent.setTypeface(Typeface.DEFAULT);
        }
        Toast.makeText(this, isMonospace ? "Monospace Font" : "Default Font", Toast.LENGTH_SHORT).show();
    }

    private void updateTitle() {
        String title = currentFileName + (isModified ? " *" : "");
        tvFileName.setText(title);
    }

    private void updateStats() {
        String text = etContent.getText().toString();
        int charCount = text.length();
        int lineCount = text.isEmpty() ? 1 : etContent.getLineCount();
        if (lineCount == 0) lineCount = text.split("\r\n|\r|\n").length;

        String trimmed = text.trim();
        int wordCount = trimmed.isEmpty() ? 0 : trimmed.split("\\s+").length;

        String statsText = "Words: " + wordCount + "  |  Chars: " + charCount + "  |  Lines: " + lineCount;
        tvStats.setText(statsText);
    }

    @Override
    public void onBackPressed() {
        if (isModified) {
            new AlertDialog.Builder(this)
                    .setTitle("Exit textedit")
                    .setMessage("You have unsaved changes. Exit without saving?")
                    .setPositiveButton("Exit", (dialog, which) -> finish())
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }
}