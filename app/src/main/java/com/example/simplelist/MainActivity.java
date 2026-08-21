package com.example.simplelist;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "SimpleListPrefs";
    private static final String KEY_TASKS = "tasks_json";

    private TextInputEditText etTaskInput;
    private MaterialButton btnAddTask;
    private RecyclerView rvTaskList;
    private TextView tvEmptyState;

    private TaskAdapter adapter;
    private List<Task> taskList;

    public static class Task {
        private final String id;
        private final String title;
        private boolean isCompleted;

        public Task(String id, String title, boolean isCompleted) {
            this.id = id;
            this.title = title;
            this.isCompleted = isCompleted;
        }

        public String getId() { return id; }
        public String getTitle() { return title; }
        public boolean isCompleted() { return isCompleted; }
        public void setCompleted(boolean completed) { isCompleted = completed; }

        public JSONObject toJsonObject() throws JSONException {
            JSONObject obj = new JSONObject();
            obj.put("id", id);
            obj.put("title", title);
            obj.put("isCompleted", isCompleted);
            return obj;
        }

        public static Task fromJsonObject(JSONObject obj) throws JSONException {
            return new Task(
                    obj.getString("id"),
                    obj.getString("title"),
                    obj.optBoolean("isCompleted", false)
            );
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etTaskInput = findViewById(R.id.etTaskInput);
        btnAddTask = findViewById(R.id.btnAddTask);
        rvTaskList = findViewById(R.id.rvTaskList);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        taskList = loadTasks();

        adapter = new TaskAdapter(taskList, new TaskAdapter.OnTaskClickListener() {
            @Override
            public void onDeleteClick(int position) {
                taskList.remove(position);
                adapter.notifyItemRemoved(position);
                adapter.notifyItemRangeChanged(position, taskList.size());
                saveTasks();
                updateEmptyState();
            }

            @Override
            public void onStatusChanged(int position, boolean isCompleted) {
                taskList.get(position).setCompleted(isCompleted);
                saveTasks();
            }
        });

        rvTaskList.setLayoutManager(new LinearLayoutManager(this));
        rvTaskList.setAdapter(adapter);

        btnAddTask.setOnClickListener(v -> addTask());

        updateEmptyState();
    }

    private void addTask() {
        String title = etTaskInput.getText() != null ? etTaskInput.getText().toString().trim() : "";
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "Please enter a task", Toast.LENGTH_SHORT).show();
            return;
        }

        String id = String.valueOf(System.currentTimeMillis());
        Task newTask = new Task(id, title, false);
        taskList.add(0, newTask);
        adapter.notifyItemInserted(0);
        rvTaskList.scrollToPosition(0);

        etTaskInput.setText("");
        saveTasks();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (taskList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvTaskList.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvTaskList.setVisibility(View.VISIBLE);
        }
    }

    private void saveTasks() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        JSONArray array = new JSONArray();
        for (Task task : taskList) {
            try {
                array.put(task.toJsonObject());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        prefs.edit().putString(KEY_TASKS, array.toString()).apply();
    }

    private List<Task> loadTasks() {
        List<Task> list = new ArrayList<>();
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String jsonString = prefs.getString(KEY_TASKS, null);

        if (jsonString != null) {
            try {
                JSONArray array = new JSONArray(jsonString);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    list.add(Task.fromJsonObject(obj));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    private static class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

        public interface OnTaskClickListener {
            void onDeleteClick(int position);
            void onStatusChanged(int position, boolean isCompleted);
        }

        private final List<Task> tasks;
        private final OnTaskClickListener listener;

        public TaskAdapter(List<Task> tasks, OnTaskClickListener listener) {
            this.tasks = tasks;
            this.listener = listener;
        }

        @NonNull
        @Override
        public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_task, parent, false);
            return new TaskViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
            Task task = tasks.get(position);
            holder.tvTitle.setText(task.getTitle());

            holder.cbCompleted.setOnCheckedChangeListener(null);
            holder.cbCompleted.setChecked(task.isCompleted());

            applyStrikethrough(holder.tvTitle, task.isCompleted());

            holder.cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int adapterPos = holder.getAdapterPosition();
                if (adapterPos != RecyclerView.NO_POSITION) {
                    applyStrikethrough(holder.tvTitle, isChecked);
                    listener.onStatusChanged(adapterPos, isChecked);
                }
            });

            holder.btnDelete.setOnClickListener(v -> {
                int adapterPos = holder.getAdapterPosition();
                if (adapterPos != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(adapterPos);
                }
            });
        }

        private void applyStrikethrough(TextView textView, boolean isCompleted) {
            if (isCompleted) {
                textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                textView.setAlpha(0.5f);
            } else {
                textView.setPaintFlags(textView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                textView.setAlpha(1.0f);
            }
        }

        @Override
        public int getItemCount() {
            return tasks.size();
        }

        static class TaskViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle;
            CheckBox cbCompleted;
            ImageButton btnDelete;

            public TaskViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvTaskTitle);
                cbCompleted = itemView.findViewById(R.id.cbTaskCompleted);
                btnDelete = itemView.findViewById(R.id.btnDeleteTask);
            }
        }
    }
}