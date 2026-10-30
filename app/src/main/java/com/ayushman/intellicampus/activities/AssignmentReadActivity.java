package com.ayushman.intellicampus.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ayushman.intellicampus.R;
import com.ayushman.intellicampus.constants.RoleConstants;
import com.ayushman.intellicampus.constants.UserConstants;
import com.ayushman.intellicampus.firebase.FirestoreManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AssignmentReadActivity extends AppCompatActivity {

    public static final String EXTRA_TITLE =
            "assignment_title";

    public static final String EXTRA_SUBJECT =
            "assignment_subject";

    public static final String EXTRA_DESCRIPTION =
            "assignment_description";

    public static final String EXTRA_DUE_DATE =
            "assignment_due_date";

    public static final String EXTRA_PRIORITY =
            "assignment_priority";

    public static final String EXTRA_STATUS =
            "assignment_status";

    public static final String EXTRA_CREATED_BY =
            "assignment_created_by";

    public static final String EXTRA_EDIT =
            "assignment_edit";

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat(
                    "dd MMMM yyyy, hh:mm a",
                    Locale.getDefault()
            );

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_assignment_read);

        /*
         * Toolbar.
         */
        MaterialToolbar toolbar =
                findViewById(R.id.toolbar);

        /*
         * Edit button.
         */
        MaterialButton btnEditAssignment =
                findViewById(R.id.btnEditAssignment);

        /*
         * Text views.
         */
        TextView tvTitle =
                findViewById(R.id.tvReadTitle);

        TextView tvSubject =
                findViewById(R.id.tvReadSubject);

        TextView tvDueDate =
                findViewById(R.id.tvReadDueDate);

        TextView tvPriority =
                findViewById(R.id.tvReadPriority);

        TextView tvStatus =
                findViewById(R.id.tvReadStatus);

        TextView tvDescription =
                findViewById(R.id.tvReadDescription);

        /*
         * Back arrow.
         */
        toolbar.setNavigationOnClickListener(v ->
                getOnBackPressedDispatcher().onBackPressed()
        );

        /*
         * Hide Edit by default.
         *
         * The role and ownership check is asynchronous,
         * so the button must remain hidden until permission
         * has been confirmed.
         */
        btnEditAssignment.setVisibility(
                View.GONE
        );

        /*
         * Retrieve the creator UID.
         *
         * Admins can edit any assignment.
         * Teachers can edit only assignments they created.
         * Students cannot edit assignments.
         */
        String createdBy =
                getIntent().getStringExtra(
                        EXTRA_CREATED_BY
                );

        FirestoreManager.getInstance()
                .getCurrentUserDocument()
                .get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()) {
                        return;
                    }

                    String role =
                            document.getString(
                                    UserConstants.ROLE
                            );

                    boolean isAdmin =
                            RoleConstants.ADMIN.equalsIgnoreCase(
                                    role
                            );

                    boolean isTeacher =
                            RoleConstants.TEACHER.equalsIgnoreCase(
                                    role
                            );

                    String currentUid =
                            FirestoreManager.getInstance()
                                    .getCurrentUserId();

                    /*
                     * A teacher may edit only an assignment
                     * created by that same teacher.
                     */
                    boolean teacherOwnsAssignment =
                            isTeacher &&
                                    currentUid != null &&
                                    createdBy != null &&
                                    currentUid.equals(
                                            createdBy
                                    );

                    boolean canEdit =
                            isAdmin ||
                                    teacherOwnsAssignment;

                    if (!canEdit) {
                        return;
                    }

                    /*
                     * Permission confirmed.
                     */
                    btnEditAssignment.setVisibility(
                            View.VISIBLE
                    );

                    btnEditAssignment.setOnClickListener(v -> {

                        Intent resultIntent =
                                new Intent();

                        resultIntent.putExtra(
                                EXTRA_EDIT,
                                true
                        );

                        setResult(
                                RESULT_OK,
                                resultIntent
                        );

                        finish();
                    });
                })
                .addOnFailureListener(
                        e -> btnEditAssignment.setVisibility(
                                View.GONE
                        )
                );

        /*
         * Retrieve assignment data.
         */
        String title =
                getIntent().getStringExtra(
                        EXTRA_TITLE
                );

        String subject =
                getIntent().getStringExtra(
                        EXTRA_SUBJECT
                );

        String description =
                getIntent().getStringExtra(
                        EXTRA_DESCRIPTION
                );

        long dueDate =
                getIntent().getLongExtra(
                        EXTRA_DUE_DATE,
                        0L
                );

        int priority =
                getIntent().getIntExtra(
                        EXTRA_PRIORITY,
                        0
                );

        int status =
                getIntent().getIntExtra(
                        EXTRA_STATUS,
                        0
                );

        /*
         * Title.
         */
        tvTitle.setText(
                title == null ||
                        title.trim().isEmpty()
                        ? "Assignment"
                        : title
        );

        /*
         * Subject.
         */
        tvSubject.setText(
                subject == null ||
                        subject.trim().isEmpty()
                        ? "No subject"
                        : subject
        );

        /*
         * Due date.
         */
        if (dueDate > 0) {

            tvDueDate.setText(
                    "Due • " +
                            DATE_FORMAT.format(
                                    new Date(dueDate)
                            )
            );

        } else {

            tvDueDate.setText(
                    "Due date not set"
            );
        }

        /*
         * Priority.
         */
        tvPriority.setText(
                "Priority • " +
                        getPriorityText(priority)
        );

        /*
         * Status.
         */
        tvStatus.setText(
                "Status • " +
                        getStatusText(status)
        );

        /*
         * Description.
         */
        if (description == null ||
                description.trim().isEmpty()) {

            tvDescription.setText(
                    "No description provided."
            );

        } else {

            tvDescription.setText(
                    description
            );
        }
    }

    private String getPriorityText(int priority) {

        switch (priority) {

            case 0:
                return "Low";

            case 1:
                return "Medium";

            case 2:
                return "High";

            default:
                return "Unknown";
        }
    }

    private String getStatusText(int status) {

        switch (status) {

            case 1:
                return "Completed";

            case 2:
                return "Overdue";

            case 0:
            default:
                return "Pending";
        }
    }
}