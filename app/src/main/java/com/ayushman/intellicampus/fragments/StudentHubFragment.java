package com.ayushman.intellicampus.fragments;

import androidx.appcompat.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class StudentHubFragment extends Fragment {

    private FirebaseFirestore db;
    private String uid;
    private String userName = "Student";
    private String batch = "";
    private String course = "";
    private int semester = 0;

    private LinearLayout content;
    private TextView tvSubtitle;
    private MaterialButton btnForum;
    private MaterialButton btnDirectory;
    private MaterialButton btnNewPost;
    private boolean showingForum = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_student_hub, container, false);
        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getUid();

        content = view.findViewById(R.id.hubContent);
        tvSubtitle = view.findViewById(R.id.tvHubSubtitle);
        btnForum = view.findViewById(R.id.btnHubForum);
        btnDirectory = view.findViewById(R.id.btnHubDirectory);
        btnNewPost = view.findViewById(R.id.btnHubNewPost);

        btnForum.setOnClickListener(v -> showForum());
        btnDirectory.setOnClickListener(v -> showDirectory());
        btnNewPost.setOnClickListener(v -> showCreatePostDialog());

        if (uid == null) {
            showMessage("Please sign in to use Student Community.");
        } else {
            loadCurrentStudent();
        }
        return view;
    }

    private void loadCurrentStudent() {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (!isAdded() || content == null) return;
                    if (!doc.exists()) {
                        showMessage("Your student profile could not be found.");
                        return;
                    }
                    userName = safe(doc.getString("name"), "Student");
                    batch = safe(doc.getString("batch"), "");
                    course = safe(doc.getString("course"), "");
                    Long sem = doc.getLong("semester");
                    semester = sem == null ? 0 : sem.intValue();

                    // Only a deliberately limited public profile is mirrored.
                    // Private user fields such as email are never copied.
                    if ("student".equalsIgnoreCase(safe(doc.getString("role"), "student"))
                            && !batch.isEmpty()) {
                        Map<String, Object> publicProfile = new HashMap<>();
                        publicProfile.put("uid", uid);
                        publicProfile.put("name", userName);
                        publicProfile.put("batch", batch);
                        publicProfile.put("course", course);
                        publicProfile.put("semester", semester);
                        publicProfile.put("updatedAt", FieldValue.serverTimestamp());
                        db.collection("student_directory").document(uid)
                                .set(publicProfile, com.google.firebase.firestore.SetOptions.merge());
                    }
                    showForum();
                })
                .addOnFailureListener(e -> showMessage("Couldn't load your profile: " + e.getMessage()));
    }

    private void showForum() {
        showingForum = true;
        if (content == null) return;
        btnForum.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.primary)));
        btnForum.setTextColor(getResources().getColor(R.color.white));
        btnDirectory.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.surface)));
        btnDirectory.setTextColor(getResources().getColor(R.color.text_primary));
        btnNewPost.setVisibility(View.VISIBLE);
        tvSubtitle.setText("Ask questions and share ideas with students across batches.");
        content.removeAllViews();
        addLoading("Loading discussions…");

        db.collection("student_forum_posts")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!isAdded() || content == null || !showingForum) return;
                    content.removeAllViews();
                    if (snapshot.isEmpty()) {
                        addMessageCard("No discussions yet", "Start the first discussion and help build the student community.");
                        return;
                    }
                    for (DocumentSnapshot doc : snapshot.getDocuments()) addPostCard(doc);
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || content == null) return;
                    content.removeAllViews();
                    addMessageCard("Unable to load discussions", e.getMessage() + "\nCheck the Firestore rules included with this update.");
                });
    }

    private void showDirectory() {
        showingForum = false;
        if (content == null) return;
        btnForum.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.surface)));
        btnForum.setTextColor(getResources().getColor(R.color.text_primary));
        btnDirectory.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.primary)));
        btnDirectory.setTextColor(getResources().getColor(R.color.white));
        btnNewPost.setVisibility(View.GONE);
        tvSubtitle.setText("Discover students from your own academic batch.");
        content.removeAllViews();

        if (batch.isEmpty()) {
            addMessageCard("Batch not available", "Your account doesn't have a batch assigned. Please contact your administrator.");
            return;
        }

        addLoading("Loading your batch directory…");
        db.collection("student_directory")
                .whereEqualTo("batch", batch)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!isAdded() || content == null || showingForum) return;
                    content.removeAllViews();
                    int count = 0;
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        if (uid.equals(doc.getId())) continue;
                        String name = safe(doc.getString("name"), "Student");
                        String programme = safe(doc.getString("course"), course);
                        Long sem = doc.getLong("semester");
                        String details = programme;
                        if (sem != null && sem > 0) details += " • Semester " + sem;
                        addDirectoryCard(name, details, doc.getId());
                        count++;
                    }
                    if (count == 0) {
                        addMessageCard("You're among the first here", "Other students will appear after they open Student Community for the first time.");
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || content == null) return;
                    content.removeAllViews();
                    addMessageCard("Directory unavailable", e.getMessage() + "\nCheck the student_directory rules in the supplied Firestore rules snippet.");
                });
    }

    private void addPostCard(DocumentSnapshot doc) {
        String author = safe(doc.getString("authorName"), "Student");
        String title = safe(doc.getString("title"), "Discussion");
        String body = safe(doc.getString("body"), "");
        String postId = doc.getId();
        Timestamp created = doc.getTimestamp("createdAt");
        String time = created == null ? "" : new SimpleDateFormat("dd MMM, h:mm a", Locale.getDefault()).format(created.toDate());

        MaterialCardView card = card();
        LinearLayout box = verticalBox();
        box.setPadding(dp(16), dp(14), dp(16), dp(14));
        TextView meta = text(author + (time.isEmpty() ? "" : "  •  " + time), 12, R.color.text_secondary, false);
        TextView heading = text(title, 17, R.color.text_primary, true);
        heading.setPadding(0, dp(8), 0, dp(5));
        TextView message = text(body, 14, R.color.text_secondary, false);
        message.setLineSpacing(dp(2), 1f);
        box.addView(meta);
        box.addView(heading);
        box.addView(message);
        MaterialButton comments = new MaterialButton(requireContext());
        comments.setText("View / add comments");
        comments.setTextSize(12);
        comments.setOnClickListener(v -> showComments(postId, title));
        box.addView(comments);
        card.addView(box);
        content.addView(card);
    }

    private void addDirectoryCard(String name, String details, String studentId) {
        MaterialCardView card = card();
        LinearLayout row = new LinearLayout(requireContext());
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(15), dp(13), dp(15), dp(13));
        TextView initials = text(name.trim().isEmpty() ? "S" : name.substring(0, 1).toUpperCase(Locale.getDefault()),
                18, R.color.primary, true);
        initials.setGravity(Gravity.CENTER);
        initials.setBackgroundResource(R.drawable.bg_category_chip);
        row.addView(initials, new LinearLayout.LayoutParams(dp(44), dp(44)));
        LinearLayout detailsBox = verticalBox();
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -2, 1);
        lp.leftMargin = dp(13);
        detailsBox.setLayoutParams(lp);
        detailsBox.addView(text(name, 15, R.color.text_primary, true));
        detailsBox.addView(text(details, 12, R.color.text_secondary, false));
        row.addView(detailsBox);
        card.addView(row);
        content.addView(card);
    }

    private void showCreatePostDialog() {
        LinearLayout form = verticalBox();
        form.setPadding(dp(20), dp(8), dp(20), 0);
        EditText title = edit("Discussion title");
        EditText body = edit("Write your question or discussion…");
        body.setMinLines(4);
        body.setGravity(Gravity.TOP | Gravity.START);
        form.addView(title);
        form.addView(body);
        new AlertDialog.Builder(requireContext())
                .setTitle("Start a discussion")
                .setView(form)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Publish", (dialog, which) -> {
                    String heading = title.getText().toString().trim();
                    String message = body.getText().toString().trim();
                    if (heading.isEmpty() || message.isEmpty()) {
                        Toast.makeText(requireContext(), "Enter both a title and message.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Map<String, Object> post = new HashMap<>();
                    post.put("authorId", uid);
                    post.put("authorName", userName);
                    post.put("batch", batch);
                    post.put("course", course);
                    post.put("title", heading);
                    post.put("body", message);
                    post.put("createdAt", FieldValue.serverTimestamp());
                    post.put("updatedAt", FieldValue.serverTimestamp());
                    db.collection("student_forum_posts").add(post)
                            .addOnSuccessListener(ref -> {
                                if (!isAdded()) return;
                                Toast.makeText(requireContext(), "Discussion published.", Toast.LENGTH_SHORT).show();
                                showForum();
                            })
                            .addOnFailureListener(e -> Toast.makeText(requireContext(), "Couldn't publish: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }).show();
    }

    private void showComments(String postId, String postTitle) {
        LinearLayout root = verticalBox();
        root.setPadding(dp(18), dp(8), dp(18), dp(8));
        ScrollView scroll = new ScrollView(requireContext());
        LinearLayout commentList = verticalBox();
        scroll.addView(commentList);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, dp(250)));
        EditText input = edit("Write a comment…");
        root.addView(input);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(postTitle)
                .setView(root)
                .setNegativeButton("Close", null)
                .setPositiveButton("Comment", null)
                .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String value = input.getText().toString().trim();
            if (value.isEmpty()) return;
            Map<String, Object> comment = new HashMap<>();
            comment.put("authorId", uid);
            comment.put("authorName", userName);
            comment.put("text", value);
            comment.put("createdAt", FieldValue.serverTimestamp());
            db.collection("student_forum_posts").document(postId)
                    .collection("comments").add(comment)
                    .addOnSuccessListener(ref -> {
                        input.setText("");
                        loadComments(postId, commentList);
                    })
                    .addOnFailureListener(e -> Toast.makeText(requireContext(), "Couldn't add comment: " + e.getMessage(), Toast.LENGTH_LONG).show());
        }));
        loadComments(postId, commentList);
        dialog.show();
    }

    private void loadComments(String postId, LinearLayout target) {
        db.collection("student_forum_posts").document(postId)
                .collection("comments").orderBy("createdAt", Query.Direction.ASCENDING)
                .limit(100).get()
                .addOnSuccessListener(snapshot -> {
                    if (!isAdded()) return;
                    target.removeAllViews();
                    if (snapshot.isEmpty()) {
                        target.addView(text("No comments yet.", 13, R.color.text_secondary, false));
                    }
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String author = safe(doc.getString("authorName"), "Student");
                        String message = safe(doc.getString("text"), "");
                        TextView item = text(author + "\n" + message, 13, R.color.text_primary, false);
                        item.setPadding(0, dp(8), 0, dp(8));
                        target.addView(item);
                    }
                });
    }

    private MaterialCardView card() {
        MaterialCardView card = new MaterialCardView(requireContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.bottomMargin = dp(12);
        card.setLayoutParams(lp);
        card.setRadius(dp(18));
        card.setCardElevation(0);
        card.setStrokeWidth(dp(1));
        card.setStrokeColor(getResources().getColor(R.color.outline));
        card.setCardBackgroundColor(getResources().getColor(R.color.surface));
        return card;
    }

    private LinearLayout verticalBox() {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView view = new TextView(requireContext());
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(getResources().getColor(color));
        if (bold) view.setTypeface(null, android.graphics.Typeface.BOLD);
        return view;
    }

    private EditText edit(String hint) {
        EditText edit = new EditText(requireContext());
        edit.setHint(hint);
        edit.setTextSize(14);
        edit.setPadding(dp(12), dp(10), dp(12), dp(10));
        return edit;
    }

    private void addMessageCard(String title, String message) {
        MaterialCardView card = card();
        LinearLayout box = verticalBox();
        box.setPadding(dp(18), dp(18), dp(18), dp(18));
        box.addView(text(title, 16, R.color.text_primary, true));
        TextView body = text(message, 13, R.color.text_secondary, false);
        body.setPadding(0, dp(6), 0, 0);
        box.addView(body);
        card.addView(box);
        content.addView(card);
    }

    private void addLoading(String value) {
        content.addView(text(value, 14, R.color.text_secondary, false));
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private String safe(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private void showMessage(String message) {
        if (content == null) return;
        content.removeAllViews();
        addMessageCard("Student Community", message);
    }
}
