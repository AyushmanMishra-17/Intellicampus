package com.ayushman.intellicampus.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import com.ayushman.intellicampus.R;
import com.ayushman.intellicampus.activities.AssignmentReadActivity;
import com.ayushman.intellicampus.adapters.AssignmentAdapter;
import com.ayushman.intellicampus.databinding.DialogAddAssignmentBinding;
import com.ayushman.intellicampus.databinding.FragmentAssignmentBinding;
import com.ayushman.intellicampus.interfaces.FirestoreCallback;
import com.ayushman.intellicampus.models.Assignment;
import com.ayushman.intellicampus.viewmodels.AssignmentViewModel;

import java.util.Calendar;
import java.util.List;

public class AssignmentFragment extends Fragment
        implements AssignmentAdapter.OnAssignmentClickListener {

    private FragmentAssignmentBinding binding;
    private AssignmentViewModel viewModel;
    private AssignmentAdapter adapter;

    /*
     * Keeps track of the assignment opened in Read Mode.
     * This allows the Edit button to open the existing
     * edit dialog when we return from AssignmentReadActivity.
     */
    private Assignment pendingEditAssignment;

    /*
     * Handler used to refresh the visible assignment cards
     * while this Fragment is open.
     */
    private final Handler overdueHandler =
            new Handler(Looper.getMainLooper());

    /*
     * Check every 30 seconds.
     *
     * We don't need to check every second because an assignment
     * only needs to change its displayed status around its due time.
     */
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
     * Handles the result returned by AssignmentReadActivity.
     */
    private final ActivityResultLauncher<Intent> assignmentReadLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {

                        if (result.getResultCode()
                                != android.app.Activity.RESULT_OK) {

                            pendingEditAssignment = null;
                            return;
                        }

                        Intent data = result.getData();

                        if (data == null) {
                            pendingEditAssignment = null;
                            return;
                        }

                        boolean editRequested =
                                data.getBooleanExtra(
                                        AssignmentReadActivity.EXTRA_EDIT,
                                        false
                                );

                        if (editRequested &&
                                pendingEditAssignment != null) {

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
            Bundle savedInstanceState) {

        binding = FragmentAssignmentBinding.inflate(
                inflater,
                container,
                false
        );

        setupRecyclerView();
        setupViewModel();
        observeAssignments();
        initListeners();

        viewModel.loadAssignments();

        return binding.getRoot();
    }

    private void setupRecyclerView() {

        adapter = new AssignmentAdapter(this);

        binding.rvAssignments.setLayoutManager(
                new LinearLayoutManager(
                        requireContext()
                )
        );

        binding.rvAssignments.setHasFixedSize(true);

        binding.rvAssignments.setAdapter(adapter);
    }

    private void setupViewModel() {

        viewModel =
                new ViewModelProvider(this)
                        .get(AssignmentViewModel.class);
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

    private void updateAssignmentList(
            List<Assignment> assignments) {

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
                assignments == null ||
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

        binding.fabAddAssignment.setOnClickListener(v ->
                showAssignmentDialog(null)
        );
    }

    /*
     * Start the overdue refresh timer when the Fragment
     * becomes visible.
     */
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

    /*
     * Stop the timer when the Fragment is no longer visible.
     */
    @Override
    public void onPause() {
        overdueHandler.removeCallbacks(
                overdueRefreshRunnable
        );

        super.onPause();
    }

    /*
     * Tap assignment → Read Mode.
     */
    @Override
    public void onAssignmentClick(
            @NonNull Assignment assignment) {

        pendingEditAssignment = assignment;

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

        assignmentReadLauncher.launch(intent);
    }

    /*
     * Long press → Delete.
     */
    @Override
    public void onAssignmentLongClick(
            @NonNull Assignment assignment) {

        deleteAssignment(assignment);
    }

    /*
     * Checkbox → Completed / Pending.
     */
    @Override
    public void onStatusChanged(
            @NonNull Assignment assignment,
            boolean isCompleted) {

        assignment.setStatus(
                isCompleted
                        ? Assignment.STATUS_COMPLETED
                        : Assignment.STATUS_PENDING
        );

        assignment.setUpdatedAt(
                System.currentTimeMillis()
        );

        viewModel.updateAssignment(
                assignment
        );
    }

    private void showAssignmentDialog(
            @Nullable Assignment assignment) {

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
         * Populate existing assignment when editing.
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

            switch (assignment.getPriority()) {

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
        dialogBinding.btnDueDate.setOnClickListener(v -> {

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
                                dayOfMonth + "/" +
                                        (month + 1) + "/" +
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
        });

        /*
         * Time picker.
         */
        dialogBinding.btnDueTime.setOnClickListener(v -> {

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
        });

        /*
         * Cancel.
         */
        dialogBinding.btnCancel.setOnClickListener(v ->
                dialog.dismiss()
        );

        /*
         * Save / Update.
         */
        dialogBinding.btnSave.setOnClickListener(v -> {

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
             * ADD.
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

                try {

                    viewModel.addAssignment(
                            newAssignment,

                            new FirestoreCallback<Void>() {

                                @Override
                                public void onSuccess(
                                        Void result) {

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
                                        Exception e) {

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
                 * UPDATE.
                 */
                assignment.setTitle(title);

                assignment.setSubject(subject);

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
                 * Recalculate the status after editing
                 * the due date.
                 */
                if (assignment.getStatus()
                        != Assignment.STATUS_COMPLETED) {

                    if (assignment.getDueDateTime()
                            <= currentTime) {

                        assignment.setStatus(
                                Assignment.STATUS_OVERDUE
                        );

                    } else {

                        assignment.setStatus(
                                Assignment.STATUS_PENDING
                        );
                    }
                }

                try {

                    viewModel.updateAssignment(
                            assignment,

                            new FirestoreCallback<Void>() {

                                @Override
                                public void onSuccess(
                                        Void result) {

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
                                        Exception e) {

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
        });

        dialog.show();
    }

    /*
     * Delete assignment.
     */
    private void deleteAssignment(
            @NonNull Assignment assignment) {

        new MaterialAlertDialogBuilder(
                requireContext()
        )
                .setTitle("Delete Assignment")
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
                                                    Void result) {

                                                showAssignmentMessage(
                                                        "Assignment deleted",
                                                        false
                                                );
                                            }

                                            @Override
                                            public void onFailure(
                                                    Exception e) {

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

    /**
     * Safely display a Snackbar.
     */
    private void showAssignmentMessage(
            String message,
            boolean longDuration) {

        if (!isAdded() || binding == null) {
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

        /*
         * Make absolutely sure the overdue timer stops
         * when the Fragment's view is destroyed.
         */
        overdueHandler.removeCallbacks(
                overdueRefreshRunnable
        );

        super.onDestroyView();

        binding = null;
    }
}