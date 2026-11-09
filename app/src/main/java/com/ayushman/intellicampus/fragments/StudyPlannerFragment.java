package com.ayushman.intellicampus.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.ayushman.intellicampus.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class StudyPlannerFragment extends Fragment {

    private FirebaseFirestore db;
    private String uid;
    private LinearLayout taskContainer;
    private TextView tvSummary;
    private long selectedDueAt = 0L;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_study_planner, container, false);
        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getUid();
        taskContainer = view.findViewById(R.id.studyTaskContainer);
        tvSummary = view.findViewById(R.id.tvStudyPlannerSummary);
        view.findViewById(R.id.btnAddStudyTask).setOnClickListener(v -> showAddTaskDialog());
        if (uid == null) {
            showEmpty("Please sign in to use your Study Planner.");
        } else {
            loadTasks();
        }
        return view;
    }

    private com.google.firebase.firestore.CollectionReference tasks() {
        return db.collection("users").document(uid).collection("study_tasks");
    }

    private void loadTasks() {
        if (taskContainer == null) return;
        taskContainer.removeAllViews();
        taskContainer.addView(text("Loading your study plan…", 14, R.color.text_secondary, false));
        tasks().orderBy("dueAt", Query.Direction.ASCENDING).limit(100).get()
                .addOnSuccessListener(snapshot -> {
                    if (!isAdded() || taskContainer == null) return;
                    taskContainer.removeAllViews();
                    int total = snapshot.size();
                    int completed = 0;
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        if (Boolean.TRUE.equals(doc.getBoolean("completed"))) completed++;
                    }
                    tvSummary.setText(completed + " of " + total + " tasks completed");
                    if (snapshot.isEmpty()) {
                        showEmpty("Your planner is clear. Add a study task to get started.");
                        return;
                    }
                    for (DocumentSnapshot doc : snapshot.getDocuments()) addTaskCard(doc);
                })
                .addOnFailureListener(e -> showEmpty("Couldn't load your planner: " + e.getMessage()
                        + "\nCheck the study_tasks Firestore rules included with this update."));
    }

    private void showAddTaskDialog() {
        LinearLayout form = new LinearLayout(requireContext());
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(20), dp(8), dp(20), 0);

        EditText title = new EditText(requireContext());
        title.setHint("Task title (e.g. Revise DBMS Unit 2)");
        title.setSingleLine(true);
        title.setTextSize(14);
        title.setPadding(dp(12), dp(10), dp(12), dp(10));

        EditText description = new EditText(requireContext());
        description.setHint("Notes (optional)");
        description.setMinLines(2);
        description.setTextSize(14);
        description.setGravity(Gravity.TOP);
        description.setPadding(dp(12), dp(10), dp(12), dp(10));

        selectedDueAt = startOfToday();
        MaterialButton dateButton = new MaterialButton(requireContext());
        dateButton.setText("Due: " + formatDate(selectedDueAt));
        dateButton.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(selectedDueAt);
            new DatePickerDialog(requireContext(), (picker, year, month, day) -> {
                Calendar selected = Calendar.getInstance();
                selected.set(year, month, day, 23, 59, 0);
                selected.set(Calendar.MILLISECOND, 0);
                selectedDueAt = selected.getTimeInMillis();
                dateButton.setText("Due: " + formatDate(selectedDueAt));
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        form.addView(title);
        form.addView(description);
        form.addView(dateButton);

        new AlertDialog.Builder(requireContext())
                .setTitle("Add study task")
                .setView(form)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save task", (dialog, which) -> {
                    String taskTitle = title.getText().toString().trim();
                    String notes = description.getText().toString().trim();
                    if (taskTitle.isEmpty()) {
                        Toast.makeText(requireContext(), "Enter a task title.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Map<String, Object> data = new HashMap<>();
                    data.put("title", taskTitle);
                    data.put("description", notes);
                    data.put("dueAt", selectedDueAt);
                    data.put("completed", false);
                    data.put("createdAt", FieldValue.serverTimestamp());
                    data.put("updatedAt", FieldValue.serverTimestamp());
                    tasks().add(data)
                            .addOnSuccessListener(ref -> {
                                if (!isAdded()) return;
                                Toast.makeText(requireContext(), "Study task added.", Toast.LENGTH_SHORT).show();
                                loadTasks();
                            })
                            .addOnFailureListener(e -> Toast.makeText(requireContext(), "Couldn't save task: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }).show();
    }

    private void addTaskCard(DocumentSnapshot doc) {
        String title = doc.getString("title");
        String description = doc.getString("description");
        Long due = doc.getLong("dueAt");
        boolean completed = Boolean.TRUE.equals(doc.getBoolean("completed"));

        MaterialCardView card = new MaterialCardView(requireContext());
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(-1, -2);
        cardParams.bottomMargin = dp(12);
        card.setLayoutParams(cardParams);
        card.setRadius(dp(18));
        card.setCardElevation(0);
        card.setStrokeWidth(dp(1));
        card.setStrokeColor(getResources().getColor(R.color.outline));
        card.setCardBackgroundColor(getResources().getColor(R.color.surface));

        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(14), dp(12), dp(14), dp(12));

        LinearLayout details = new LinearLayout(requireContext());
        details.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams detailsParams = new LinearLayout.LayoutParams(0, -2, 1);
        detailsParams.rightMargin = dp(8);
        details.setLayoutParams(detailsParams);

        TextView heading = text(title == null ? "Study task" : title, 15, R.color.text_primary, true);
        if (completed) heading.setPaintFlags(heading.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
        details.addView(heading);
        if (description != null && !description.trim().isEmpty()) {
            TextView note = text(description, 12, R.color.text_secondary, false);
            note.setPadding(0, dp(4), 0, dp(4));
            details.addView(note);
        }
        String dueText = due == null ? "No due date" : "Due " + formatDate(due);
        details.addView(text(dueText, 12, R.color.text_secondary, false));
        row.addView(details);

        MaterialButton done = new MaterialButton(requireContext());
        done.setText(completed ? "Done" : "Complete");
        done.setTextSize(11);
        done.setMinWidth(0);
        done.setOnClickListener(v -> tasks().document(doc.getId())
                .update("completed", !completed, "updatedAt", FieldValue.serverTimestamp())
                .addOnSuccessListener(unused -> loadTasks())
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Couldn't update task: " + e.getMessage(), Toast.LENGTH_LONG).show()));
        row.addView(done);

        MaterialButton delete = new MaterialButton(requireContext());
        delete.setText("×");
        delete.setTextSize(18);
        delete.setMinWidth(dp(36));
        delete.setOnClickListener(v -> new AlertDialog.Builder(requireContext())
                .setMessage("Delete this study task?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (d, w) -> tasks().document(doc.getId()).delete()
                        .addOnSuccessListener(unused -> loadTasks())
                        .addOnFailureListener(e -> Toast.makeText(requireContext(), "Couldn't delete task: " + e.getMessage(), Toast.LENGTH_LONG).show()))
                .show());
        row.addView(delete);

        card.addView(row);
        taskContainer.addView(card);
    }

    private void showEmpty(String message) {
        if (taskContainer == null) return;
        taskContainer.removeAllViews();
        taskContainer.addView(text(message, 14, R.color.text_secondary, false));
        if (tvSummary != null) tvSummary.setText("Your personal study plan");
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView view = new TextView(requireContext());
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(getResources().getColor(color));
        if (bold) view.setTypeface(null, android.graphics.Typeface.BOLD);
        return view;
    }

    private long startOfToday() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    private String formatDate(long millis) {
        return new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(new Date(millis));
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        taskContainer = null;
        tvSummary = null;
    }
}
