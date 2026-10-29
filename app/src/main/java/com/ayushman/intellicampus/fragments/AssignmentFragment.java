package com.ayushman.intellicampus.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import com.google.firebase.auth.FirebaseAuth;

import com.ayushman.intellicampus.R;
import com.ayushman.intellicampus.activities.AssignmentReadActivity;
import com.ayushman.intellicampus.adapters.AssignmentAdapter;
import com.ayushman.intellicampus.constants.RoleConstants;
import com.ayushman.intellicampus.constants.UserConstants;
import com.ayushman.intellicampus.databinding.DialogAddAssignmentBinding;
import com.ayushman.intellicampus.databinding.FragmentAssignmentBinding;
import com.ayushman.intellicampus.firebase.FirestoreManager;
import com.ayushman.intellicampus.interfaces.FirestoreCallback;
import com.ayushman.intellicampus.models.AcademicBatch;
import com.ayushman.intellicampus.models.Assignment;
import com.ayushman.intellicampus.repositories.AcademicBatchRepository;
import com.ayushman.intellicampus.repositories.AssignmentStatusRepository;
import com.ayushman.intellicampus.viewmodels.AssignmentViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AssignmentFragment extends Fragment
        implements AssignmentAdapter.OnAssignmentClickListener {

    private FragmentAssignmentBinding binding;
    private AssignmentViewModel viewModel;
    private AssignmentAdapter adapter;

    private final AcademicBatchRepository batchRepository =
            new AcademicBatchRepository();

    private final AssignmentStatusRepository statusRepository =
            new AssignmentStatusRepository();

    private String studentBatch = "";
    private String programme = "";
    private int semester = 0;

    /*
     * Current user's role.
     *
     * STUDENT:
     *      View + personal completion only
     *
     * TEACHER:
     *      Add + edit/delete own assignments
     *
     * ADMIN:
     *      Full assignment management
     */
    private String userRole = RoleConstants.STUDENT;

    /*
     * Assignment currently opened in Read Mode.
     */
    private Assignment pendingEditAssignment;

    /*
     * Refresh visible overdue states periodically.
     */
    private final Handler overdueHandler =
            new Handler(Looper.getMainLooper());

    private static final long OVERDUE_REFRESH_INTERVAL =
            30_000L;

    private final Runnable overdueRefreshRunnable =
            new Runnable() {

                @Override
                public void run() {

                    if (binding != null &&
                            adapter != null) {

                        adapter.notifyDataSetChanged();

                        overdueHandler.postDelayed(
                                this,
                                OVERDUE_REFRESH_INTERVAL
                        );
                    }
                }
            };

    /*
     * Handles result from AssignmentReadActivity.
     */
    private final ActivityResultLauncher<Intent>
            assignmentReadLauncher =
            registerForActivityResult(
                    new ActivityResultContracts
                            .StartActivityForResult(),
                    result -> {

                        if (result.getResultCode()
                                != android.app.Activity.RESULT_OK) {

                            pendingEditAssignment = null;
                            return;
                        }

                        Intent data =
                                result.getData();

                        if (data == null) {

                            pendingEditAssignment = null;
                            return;
                        }

                        boolean editRequested =
                                data.getBooleanExtra(
                                        AssignmentReadActivity.EXTRA_EDIT,
                                        false
                                );

                        /*
                         * Even if Read Mode requests editing,
                         * verify permission again here.
                         */
                        if (editRequested &&
                                pendingEditAssignment != null &&
                                canEditAssignment(
                                        pendingEditAssignment
                                )) {

                            showAssignmentDialog(
                                    pendingEditAssignment
                            );
                        }

                        pendingEditAssignment = null;
                    }
            );

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding =
                FragmentAssignmentBinding.inflate(
                        inflater,
                        container,
                        false
                );

        setupRecyclerView();
        setupViewModel();
        observeAssignments();
        initListeners();

        loadStudentAcademicContext();

        return binding.getRoot();
    }

    /*
     * Load the current user's role and academic context.
     */
    private void loadStudentAcademicContext() {

        if (!isAdded()) {
            return;
        }

        String uid =
                FirebaseAuth
                        .getInstance()
                        .getCurrentUser() == null
                        ? null
                        : FirebaseAuth
                        .getInstance()
                        .getCurrentUser()
                        .getUid();

        if (uid == null) {
            return;
        }

        FirestoreManager
                .getInstance()
                .getCurrentUserDocument()
                .get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()) {
                        return;
                    }

                    /*
                     * ROLE
                     */
                    userRole =
                            document.getString(
                                    UserConstants.ROLE
                            );

                    if (TextUtils.isEmpty(userRole)) {

                        userRole =
                                RoleConstants.STUDENT;
                    }

                    updateAssignmentManagementControls();

                    /*
                     * BATCH
                     */
                    studentBatch =
                            document.getString(
                                    UserConstants.BATCH
                            );

                    /*
                     * PROGRAMME
                     */
                    String course =
                            document.getString(
                                    UserConstants.COURSE
                            );

                    if (!TextUtils.isEmpty(course)) {

                        programme =
                                course.trim();
                    }

                    if (TextUtils.isEmpty(
                            studentBatch
                    )) {

                        /*
                         * Without a target batch we cannot
                         * perform the academic assignment query.
                         */
                        return;
                    }

                    batchRepository.getBatch(
                            studentBatch,
                            new AcademicBatchRepository
                                    .BatchCallback() {

                                @Override
                                public void onSuccess(
                                        AcademicBatch batch
                                ) {

                                    if (batch == null) {
                                        return;
                                    }

                                    semester =
                                            batch.getCurrentSemester();

                                    if (!TextUtils.isEmpty(
                                            batch.getProgramme()
                                    )) {

                                        programme =
                                                batch.getProgramme();
                                    }

                                    if (semester <= 0 ||
                                            TextUtils.isEmpty(
                                                    programme
                                            )) {

                                        return;
                                    }

                                    viewModel.loadAssignments(
                                            programme,
                                            studentBatch,
                                            semester
                                    );
                                }

                                @Override
                                public void onError(
                                        Exception e
                                ) {

                                    e.printStackTrace();
                                }
                            }
                    );
                })
                .addOnFailureListener(
                        Throwable::printStackTrace
                );
    }

    /*
     * Can this user manage assignments at all?
     */
    private boolean canManageAssignments() {

        return RoleConstants.ADMIN.equalsIgnoreCase(
                userRole
        ) ||
                RoleConstants.TEACHER.equalsIgnoreCase(
                        userRole
                );
    }

    /*
     * Can this user edit/delete this particular assignment?
     *
     * ADMIN:
     *      Everything
     *
     * TEACHER:
     *      Only assignments created by this teacher
     */
    private boolean canEditAssignment(
            @NonNull Assignment assignment
    ) {

        if (RoleConstants.ADMIN.equalsIgnoreCase(
                userRole
        )) {

            return true;
        }

        if (!RoleConstants.TEACHER.equalsIgnoreCase(
                userRole
        )) {

            return false;
        }

        String currentUid =
                FirebaseAuth
                        .getInstance()
                        .getCurrentUser() == null
                        ? null
                        : FirebaseAuth
                        .getInstance()
                        .getCurrentUser()
                        .getUid();

        if (currentUid == null) {
            return false;
        }

        String createdBy =
                assignment.getCreatedBy();

        return createdBy != null &&
                createdBy.equals(currentUid);
    }

    /*
     * Update Add FAB visibility according to role.
     */
    private void updateAssignmentManagementControls() {

        if (binding == null) {
            return;
        }

        boolean canManage =
                canManageAssignments();

        binding.fabAddAssignment.setVisibility(
                canManage
                        ? View.VISIBLE
                        : View.GONE
        );

        if (canManage) {

            binding.fabAddAssignment.setOnClickListener(
                    v -> showAssignmentDialog(null)
            );

        } else {

            binding.fabAddAssignment.setOnClickListener(
                    null
            );
        }
    }

    private void setupRecyclerView() {

        adapter =
                new AssignmentAdapter(this);

        binding.rvAssignments.setLayoutManager(
                new LinearLayoutManager(
                        requireContext()
                )
        );

        binding.rvAssignments.setHasFixedSize(
                true
        );

        binding.rvAssignments.setAdapter(
                adapter
        );
    }

    private void setupViewModel() {

        viewModel =
                new ViewModelProvider(this)
                        .get(
                                AssignmentViewModel.class
                        );
    }

    private void observeAssignments() {

        binding.progressBar.setVisibility(
                View.VISIBLE
        );

        viewModel.getAssignments().observe(
                getViewLifecycleOwner(),
                this::updateAssignmentList
        );
    }

    /*
     * Assignment list received from Firestore.
     *
     * First apply the normal due-date status.
     * Then overlay the current user's personal
     * completion status.
     */
    private void updateAssignmentList(
            List<Assignment> assignments
    ) {

        if (binding == null) {
            return;
        }

        if (assignments == null) {

            binding.progressBar.setVisibility(
                    View.GONE
            );

            adapter.setAssignments(
                    new ArrayList<>()
            );

            binding.layoutEmpty.setVisibility(
                    View.VISIBLE
            );

            binding.rvAssignments.setVisibility(
                    View.GONE
            );

            return;
        }

        /*
         * No assignments.
         */
        if (assignments.isEmpty()) {

            binding.progressBar.setVisibility(
                    View.GONE
            );

            adapter.setAssignments(
                    assignments
            );

            binding.layoutEmpty.setVisibility(
                    View.VISIBLE
            );

            binding.rvAssignments.setVisibility(
                    View.GONE
            );

            return;
        }

        /*
         * Apply personal completion states.
         */
        applyPersonalCompletionStatuses(
                assignments
        );
    }

    private void applyPersonalCompletionStatuses(
            @NonNull List<Assignment> assignments
    ) {

        final int total =
                assignments.size();

        AtomicInteger remaining =
                new AtomicInteger(total);

        /*
         * If one status lookup fails, we still display
         * the assignment using its normal due-date state.
         */
        for (Assignment assignment :
                assignments) {

            if (assignment == null ||
                    TextUtils.isEmpty(
                            assignment.getId()
                    )) {

                if (remaining.decrementAndGet()
                        == 0) {

                    finishAssignmentListUpdate(
                            assignments
                    );
                }

                continue;
            }

            applyDueDateStatus(
                    assignment
            );

            statusRepository.getCompletionStatus(
                    assignment.getId(),
                    new FirestoreCallback<Boolean>() {

                        @Override
                        public void onSuccess(
                                Boolean completed
                        ) {

                            if (Boolean.TRUE.equals(
                                    completed
                            )) {

                                assignment.setStatus(
                                        Assignment
                                                .STATUS_COMPLETED
                                );

                            } else {

                                applyDueDateStatus(
                                        assignment
                                );
                            }

                            if (remaining.decrementAndGet()
                                    == 0) {

                                finishAssignmentListUpdate(
                                        assignments
                                );
                            }
                        }

                        @Override
                        public void onFailure(
                                Exception e
                        ) {

                            applyDueDateStatus(
                                    assignment
                            );

                            if (remaining.decrementAndGet()
                                    == 0) {

                                finishAssignmentListUpdate(
                                        assignments
                                );
                            }
                        }
                    }
            );
        }
    }

    /*
     * Shared assignment status is derived from the due date.
     * COMPLETED is strictly personal and is applied separately.
     */
    private void applyDueDateStatus(
            @NonNull Assignment assignment
    ) {

        long now =
                System.currentTimeMillis();

        if (assignment.getDueDateTime()
                <= now) {

            assignment.setStatus(
                    Assignment.STATUS_OVERDUE
            );

        } else {

            assignment.setStatus(
                    Assignment.STATUS_PENDING
            );
        }
    }

    private void finishAssignmentListUpdate(
            @NonNull List<Assignment> assignments
    ) {

        if (binding == null) {
            return;
        }

        binding.progressBar.setVisibility(
                View.GONE
        );

        adapter.setAssignments(
                assignments
        );

        boolean isEmpty =
                assignments.isEmpty();

        binding.layoutEmpty.setVisibility(
                isEmpty
                        ? View.VISIBLE
                        : View.GONE
        );

        binding.rvAssignments.setVisibility(
                isEmpty
                        ? View.GONE
                        : View.VISIBLE
        );
    }

    private void initListeners() {

        updateAssignmentManagementControls();
    }

    @Override
    public void onResume() {

        super.onResume();

        overdueHandler.removeCallbacks(
                overdueRefreshRunnable
        );

        overdueHandler.post(
                overdueRefreshRunnable
        );
    }

    @Override
    public void onPause() {

        overdueHandler.removeCallbacks(
                overdueRefreshRunnable
        );

        super.onPause();
    }

    /*
     * Assignment tap → Read Mode.
     */
    @Override
    public void onAssignmentClick(
            @NonNull Assignment assignment
    ) {

        /*
         * Only assignments the user can actually edit
         * are retained for the return callback.
         */
        pendingEditAssignment =
                canEditAssignment(
                        assignment
                )
                        ? assignment
                        : null;

        Intent intent =
                new Intent(
                        requireContext(),
                        AssignmentReadActivity.class
                );

        intent.putExtra(
                AssignmentReadActivity.EXTRA_TITLE,
                assignment.getTitle()
        );

        intent.putExtra(
                AssignmentReadActivity.EXTRA_SUBJECT,
                assignment.getSubject()
        );

        intent.putExtra(
                AssignmentReadActivity.EXTRA_DESCRIPTION,
                assignment.getDescription()
        );

        intent.putExtra(
                AssignmentReadActivity.EXTRA_DUE_DATE,
                assignment.getDueDateTime()
        );

        intent.putExtra(
                AssignmentReadActivity.EXTRA_PRIORITY,
                assignment.getPriority()
        );

        intent.putExtra(
                AssignmentReadActivity.EXTRA_STATUS,
                assignment.getStatus()
        );

        assignmentReadLauncher.launch(
                intent
        );
    }

    /*
     * Long press → Delete.
     */
    @Override
    public void onAssignmentLongClick(
            @NonNull Assignment assignment
    ) {

        if (!canEditAssignment(
                assignment
        )) {

            showAssignmentMessage(
                    RoleConstants.TEACHER.equalsIgnoreCase(
                            userRole
                    )
                            ? "You can only delete your own assignments"
                            : "You do not have permission to delete assignments",
                    true
            );

            return;
        }

        deleteAssignment(
                assignment
        );
    }

    /*
     * Checkbox → PERSONAL completion.
     *
     * This no longer updates the shared assignment document.
     */
    @Override
    public void onStatusChanged(
            @NonNull Assignment assignment,
            boolean isCompleted
    ) {

        if (TextUtils.isEmpty(
                assignment.getId()
        )) {

            return;
        }

        if (isCompleted) {

            statusRepository.setCompletionStatus(
                    assignment.getId(),
                    true,
                    new FirestoreCallback<Void>() {

                        @Override
                        public void onSuccess(
                                Void result
                        ) {

                            assignment.setStatus(
                                    Assignment.STATUS_COMPLETED
                            );

                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onFailure(
                                Exception e
                        ) {

                            applyDueDateStatus(
                                    assignment
                            );

                            adapter.notifyDataSetChanged();

                            showAssignmentMessage(
                                    "Failed to update completion status",
                                    true
                            );
                        }
                    }
            );

        } else {

            statusRepository.deleteCompletionStatus(
                    assignment.getId(),
                    new FirestoreCallback<Void>() {

                        @Override
                        public void onSuccess(
                                Void result
                        ) {

                            applyDueDateStatus(
                                    assignment
                            );

                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onFailure(
                                Exception e
                        ) {

                            /*
                             * Keep the assignment visually completed
                             * if Firestore failed to remove the status.
                             */
                            assignment.setStatus(
                                    Assignment.STATUS_COMPLETED
                            );

                            adapter.notifyDataSetChanged();

                            showAssignmentMessage(
                                    "Failed to update completion status",
                                    true
                            );
                        }
                    }
            );
        }
    }

    private void showAssignmentDialog(
            @Nullable Assignment assignment
    ) {

        /*
         * Only admins and teachers can open
         * the management dialog.
         */
        if (!canManageAssignments()) {

            showAssignmentMessage(
                    "You do not have permission to manage assignments",
                    true
            );

            return;
        }

        /*
         * Teachers can edit/delete only their own assignments.
         */
        if (assignment != null &&
                !canEditAssignment(
                        assignment
                )) {

            showAssignmentMessage(
                    "You can only edit your own assignments",
                    true
            );

            return;
        }

        DialogAddAssignmentBinding dialogBinding =
                DialogAddAssignmentBinding.inflate(
                        getLayoutInflater()
                );

        AlertDialog dialog =
                new MaterialAlertDialogBuilder(
                        requireContext()
                )
                        .setView(
                                dialogBinding.getRoot()
                        )
                        .setCancelable(false)
                        .create();

        Calendar calendar =
                Calendar.getInstance();

        ArrayAdapter<String> priorityAdapter =
                new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout
                                .simple_dropdown_item_1line,
                        new String[]{
                                "Low",
                                "Medium",
                                "High"
                        }
                );

        dialogBinding.actPriority.setAdapter(
                priorityAdapter
        );

        /*
         * Populate existing assignment.
         */
        if (assignment != null) {

            dialogBinding.etTitle.setText(
                    assignment.getTitle()
            );

            dialogBinding.etSubject.setText(
                    assignment.getSubject()
            );

            dialogBinding.etDescription.setText(
                    assignment.getDescription()
            );

            switch (
                    assignment.getPriority()
            ) {

                case Assignment.PRIORITY_LOW:

                    dialogBinding.actPriority.setText(
                            "Low",
                            false
                    );

                    break;

                case Assignment.PRIORITY_MEDIUM:

                    dialogBinding.actPriority.setText(
                            "Medium",
                            false
                    );

                    break;

                case Assignment.PRIORITY_HIGH:

                    dialogBinding.actPriority.setText(
                            "High",
                            false
                    );

                    break;
            }

            calendar.setTimeInMillis(
                    assignment.getDueDateTime()
            );

            dialogBinding.btnDueDate.setText(
                    String.format(
                            "%1$td/%1$tm/%1$tY",
                            calendar
                    )
            );

            dialogBinding.btnDueTime.setText(
                    String.format(
                            "%1$tH:%1$tM",
                            calendar
                    )
            );
        }

        /*
         * Date picker.
         */
        dialogBinding.btnDueDate.setOnClickListener(
                v -> {

                    new android.app.DatePickerDialog(
                            requireContext(),

                            (view,
                             year,
                             month,
                             dayOfMonth) -> {

                                calendar.set(
                                        Calendar.YEAR,
                                        year
                                );

                                calendar.set(
                                        Calendar.MONTH,
                                        month
                                );

                                calendar.set(
                                        Calendar.DAY_OF_MONTH,
                                        dayOfMonth
                                );

                                dialogBinding.btnDueDate.setText(
                                        dayOfMonth +
                                                "/" +
                                                (month + 1) +
                                                "/" +
                                                year
                                );
                            },

                            calendar.get(
                                    Calendar.YEAR
                            ),

                            calendar.get(
                                    Calendar.MONTH
                            ),

                            calendar.get(
                                    Calendar.DAY_OF_MONTH
                            )

                    ).show();
                }
        );

        /*
         * Time picker.
         */
        dialogBinding.btnDueTime.setOnClickListener(
                v -> {

                    new android.app.TimePickerDialog(
                            requireContext(),

                            (view,
                             hour,
                             minute) -> {

                                calendar.set(
                                        Calendar.HOUR_OF_DAY,
                                        hour
                                );

                                calendar.set(
                                        Calendar.MINUTE,
                                        minute
                                );

                                dialogBinding.btnDueTime.setText(
                                        String.format(
                                                "%02d:%02d",
                                                hour,
                                                minute
                                        )
                                );
                            },

                            calendar.get(
                                    Calendar.HOUR_OF_DAY
                            ),

                            calendar.get(
                                    Calendar.MINUTE
                            ),

                            DateFormat.is24HourFormat(
                                    requireContext()
                            )

                    ).show();
                }
        );

        /*
         * Cancel.
         */
        dialogBinding.btnCancel.setOnClickListener(
                v -> dialog.dismiss()
        );

        /*
         * Save / Update.
         */
        dialogBinding.btnSave.setOnClickListener(
                v -> {

                    String title =
                            dialogBinding.etTitle
                                    .getText()
                                    .toString()
                                    .trim();

                    String subject =
                            dialogBinding.etSubject
                                    .getText()
                                    .toString()
                                    .trim();

                    String description =
                            dialogBinding.etDescription
                                    .getText()
                                    .toString()
                                    .trim();

                    String priorityString =
                            dialogBinding.actPriority
                                    .getText()
                                    .toString()
                                    .trim();

                    if (title.isEmpty()) {

                        dialogBinding.etTitle.setError(
                                "Title is required"
                        );

                        dialogBinding.etTitle.requestFocus();

                        return;
                    }

                    if (subject.isEmpty()) {

                        dialogBinding.etSubject.setError(
                                "Subject is required"
                        );

                        dialogBinding.etSubject.requestFocus();

                        return;
                    }

                    if (priorityString.isEmpty()) {

                        dialogBinding.actPriority.setError(
                                "Select Priority"
                        );

                        dialogBinding.actPriority.requestFocus();

                        return;
                    }

                    /*
                     * Academic targeting must exist for a new
                     * assignment.
                     */
                    if (assignment == null) {

                        if (TextUtils.isEmpty(
                                programme
                        ) ||
                                TextUtils.isEmpty(
                                        studentBatch
                                ) ||
                                semester <= 0) {

                            showAssignmentMessage(
                                    "Academic programme, batch or semester is not available",
                                    true
                            );

                            return;
                        }
                    }

                    int priority =
                            Assignment.PRIORITY_LOW;

                    switch (priorityString) {

                        case "Medium":

                            priority =
                                    Assignment.PRIORITY_MEDIUM;

                            break;

                        case "High":

                            priority =
                                    Assignment.PRIORITY_HIGH;

                            break;

                        default:

                            priority =
                                    Assignment.PRIORITY_LOW;

                            break;
                    }

                    long currentTime =
                            System.currentTimeMillis();

                    /*
                     * ADD
                     */
                    if (assignment == null) {

                        Assignment newAssignment =
                                new Assignment(
                                        title,
                                        description,
                                        subject,
                                        calendar.getTimeInMillis(),
                                        priority,
                                        Assignment.STATUS_PENDING,
                                        currentTime,
                                        currentTime
                                );

                        newAssignment.setProgramme(
                                programme
                        );

                        newAssignment.setBatch(
                                studentBatch
                        );

                        newAssignment.setSemester(
                                semester
                        );

                        try {

                            viewModel.addAssignment(
                                    newAssignment,

                                    new FirestoreCallback<Void>() {

                                        @Override
                                        public void onSuccess(
                                                Void result
                                        ) {

                                            showAssignmentMessage(
                                                    "Assignment Added",
                                                    false
                                            );

                                            if (dialog.isShowing()) {
                                                dialog.dismiss();
                                            }
                                        }

                                        @Override
                                        public void onFailure(
                                                Exception e
                                        ) {

                                            showAssignmentMessage(
                                                    e.getMessage() == null
                                                            ? "Failed to add assignment"
                                                            : e.getMessage(),
                                                    true
                                            );
                                        }
                                    }
                            );

                        } catch (Exception e) {

                            showAssignmentMessage(
                                    e.getMessage() == null
                                            ? "Failed to add assignment"
                                            : e.getMessage(),
                                    true
                            );
                        }

                    } else {

                        /*
                         * UPDATE
                         */
                        assignment.setTitle(
                                title
                        );

                        assignment.setSubject(
                                subject
                        );

                        assignment.setDescription(
                                description
                        );

                        assignment.setPriority(
                                priority
                        );

                        assignment.setDueDateTime(
                                calendar.getTimeInMillis()
                        );

                        assignment.setUpdatedAt(
                                currentTime
                        );

                        /*
                         * Shared document never stores personal
                         * completion anymore.
                         */
                        applyDueDateStatus(
                                assignment
                        );

                        try {

                            viewModel.updateAssignment(
                                    assignment,

                                    new FirestoreCallback<Void>() {

                                        @Override
                                        public void onSuccess(
                                                Void result
                                        ) {

                                            showAssignmentMessage(
                                                    "Assignment Updated",
                                                    false
                                            );

                                            if (dialog.isShowing()) {
                                                dialog.dismiss();
                                            }
                                        }

                                        @Override
                                        public void onFailure(
                                                Exception e
                                        ) {

                                            showAssignmentMessage(
                                                    e.getMessage() == null
                                                            ? "Failed to update assignment"
                                                            : e.getMessage(),
                                                    true
                                            );
                                        }
                                    }
                            );

                        } catch (Exception e) {

                            showAssignmentMessage(
                                    e.getMessage() == null
                                            ? "Failed to update assignment"
                                            : e.getMessage(),
                                    true
                            );
                        }
                    }
                }
        );

        dialog.show();
    }

    /*
     * Delete assignment.
     */
    private void deleteAssignment(
            @NonNull Assignment assignment
    ) {

        if (!canEditAssignment(
                assignment
        )) {

            showAssignmentMessage(
                    "You do not have permission to delete this assignment",
                    true
            );

            return;
        }

        new MaterialAlertDialogBuilder(
                requireContext()
        )
                .setTitle(
                        "Delete Assignment"
                )
                .setMessage(
                        "Are you sure you want to delete this assignment?"
                )
                .setPositiveButton(
                        "Delete",
                        (dialog, which) -> {

                            try {

                                viewModel.deleteAssignment(
                                        assignment.getId(),

                                        new FirestoreCallback<Void>() {

                                            @Override
                                            public void onSuccess(
                                                    Void result
                                            ) {

                                                /*
                                                 * Also remove this user's
                                                 * personal completion record.
                                                 */
                                                statusRepository
                                                        .deleteCompletionStatus(
                                                                assignment.getId(),
                                                                new FirestoreCallback<Void>() {

                                                                    @Override
                                                                    public void onSuccess(
                                                                            Void result
                                                                    ) {

                                                                        showAssignmentMessage(
                                                                                "Assignment deleted",
                                                                                false
                                                                        );
                                                                    }

                                                                    @Override
                                                                    public void onFailure(
                                                                            Exception e
                                                                    ) {

                                                                        /*
                                                                         * Assignment itself is already deleted,
                                                                         * so don't show a failure for cleanup.
                                                                         */
                                                                        showAssignmentMessage(
                                                                                "Assignment deleted",
                                                                                false
                                                                        );
                                                                    }
                                                                }
                                                        );
                                            }

                                            @Override
                                            public void onFailure(
                                                    Exception e
                                            ) {

                                                showAssignmentMessage(
                                                        e.getMessage() == null
                                                                ? "Failed to delete assignment"
                                                                : e.getMessage(),
                                                        true
                                                );
                                            }
                                        }
                                );

                            } catch (Exception e) {

                                showAssignmentMessage(
                                        e.getMessage() == null
                                                ? "Failed to delete assignment"
                                                : e.getMessage(),
                                        true
                                );
                            }
                        }
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .show();
    }

    /*
     * Safely display a Snackbar.
     */
    private void showAssignmentMessage(
            String message,
            boolean longDuration
    ) {

        if (!isAdded() ||
                binding == null) {

            return;
        }

        Snackbar.make(
                binding.getRoot(),
                message,
                longDuration
                        ? Snackbar.LENGTH_LONG
                        : Snackbar.LENGTH_SHORT
        ).show();
    }

    @Override
    public void onDestroyView() {

        overdueHandler.removeCallbacks(
                overdueRefreshRunnable
        );

        super.onDestroyView();

        binding = null;
    }
}