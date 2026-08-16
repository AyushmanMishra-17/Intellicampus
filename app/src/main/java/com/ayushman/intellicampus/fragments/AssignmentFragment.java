package com.ayushman.intellicampus.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.ayushman.intellicampus.databinding.DialogAddAssignmentBinding;
import com.ayushman.intellicampus.databinding.FragmentAssignmentBinding;
import com.ayushman.intellicampus.adapters.AssignmentAdapter;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentAssignmentBinding.inflate(inflater, container, false);

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
                new LinearLayoutManager(requireContext()));

        binding.rvAssignments.setHasFixedSize(true);
        binding.rvAssignments.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this)
                .get(AssignmentViewModel.class);
    }

    private void observeAssignments() {

        binding.progressBar.setVisibility(View.VISIBLE);

        viewModel.getAssignments().observe(
                getViewLifecycleOwner(),
                this::updateAssignmentList
        );
    }

    private void updateAssignmentList(List<Assignment> assignments) {

        binding.progressBar.setVisibility(View.GONE);

        adapter.setAssignments(assignments);

        boolean isEmpty =
                assignments == null || assignments.isEmpty();

        binding.layoutEmpty.setVisibility(
                isEmpty ? View.VISIBLE : View.GONE);

        binding.rvAssignments.setVisibility(
                isEmpty ? View.GONE : View.VISIBLE);
    }

    private void initListeners() {

        binding.fabAddAssignment.setOnClickListener(v ->
                showAssignmentDialog(null));
    }

    @Override
    public void onAssignmentClick(@NonNull Assignment assignment) {
        showAssignmentDialog(assignment);
    }

    @Override
    public void onAssignmentLongClick(@NonNull Assignment assignment) {
        deleteAssignment(assignment);
    }

    @Override
    public void onStatusChanged(@NonNull Assignment assignment,
                                boolean isCompleted) {

        assignment.setStatus(
                isCompleted
                        ? Assignment.STATUS_COMPLETED
                        : Assignment.STATUS_PENDING);

        assignment.setUpdatedAt(System.currentTimeMillis());

        viewModel.updateAssignment(assignment);
    }

    private void showAssignmentDialog(@Nullable Assignment assignment) {

        DialogAddAssignmentBinding dialogBinding =
                DialogAddAssignmentBinding.inflate(getLayoutInflater());

        AlertDialog dialog =
                new MaterialAlertDialogBuilder(requireContext())
                        .setView(dialogBinding.getRoot())
                        .setCancelable(false)
                        .create();

        Calendar calendar = Calendar.getInstance();

        ArrayAdapter<String> priorityAdapter =
                new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        new String[]{
                                "Low",
                                "Medium",
                                "High"
                        });

        dialogBinding.actPriority.setAdapter(priorityAdapter);

        if (assignment != null) {

            dialogBinding.etTitle.setText(
                    assignment.getTitle());

            dialogBinding.etSubject.setText(
                    assignment.getSubject());

            dialogBinding.etDescription.setText(
                    assignment.getDescription());

            switch (assignment.getPriority()) {

                case Assignment.PRIORITY_LOW:
                    dialogBinding.actPriority.setText("Low", false);
                    break;

                case Assignment.PRIORITY_MEDIUM:
                    dialogBinding.actPriority.setText("Medium", false);
                    break;

                case Assignment.PRIORITY_HIGH:
                    dialogBinding.actPriority.setText("High", false);
                    break;
            }

            calendar.setTimeInMillis(
                    assignment.getDueDateTime());

            dialogBinding.btnDueDate.setText(
                    String.format(
                            "%1$td/%1$tm/%1$tY",
                            calendar));

            dialogBinding.btnDueTime.setText(
                    String.format(
                            "%1$tH:%1$tM",
                            calendar));
        }

        dialogBinding.btnDueDate.setOnClickListener(v -> {

            new DatePickerDialog(
                    requireContext(),
                    (view,
                     year,
                     month,
                     dayOfMonth) -> {

                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        dialogBinding.btnDueDate.setText(
                                dayOfMonth + "/"
                                        + (month + 1)
                                        + "/"
                                        + year);

                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            ).show();

        });

        dialogBinding.btnDueTime.setOnClickListener(v -> {

            new TimePickerDialog(
                    requireContext(),
                    (view,
                     hour,
                     minute) -> {

                        calendar.set(
                                Calendar.HOUR_OF_DAY,
                                hour);

                        calendar.set(
                                Calendar.MINUTE,
                                minute);

                        dialogBinding.btnDueTime.setText(
                                String.format(
                                        "%02d:%02d",
                                        hour,
                                        minute));

                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    DateFormat.is24HourFormat(requireContext())
            ).show();

        });

        dialogBinding.btnCancel.setOnClickListener(v ->
                dialog.dismiss());
        dialogBinding.btnSave.setOnClickListener(v -> {

            String title = dialogBinding.etTitle.getText()
                    .toString()
                    .trim();

            String subject = dialogBinding.etSubject.getText()
                    .toString()
                    .trim();

            String description = dialogBinding.etDescription.getText()
                    .toString()
                    .trim();

            String priorityString = dialogBinding.actPriority.getText()
                    .toString()
                    .trim();

            if (title.isEmpty()) {
                dialogBinding.etTitle.setError("Title is required");
                dialogBinding.etTitle.requestFocus();
                return;
            }

            if (subject.isEmpty()) {
                dialogBinding.etSubject.setError("Subject is required");
                dialogBinding.etSubject.requestFocus();
                return;
            }

            if (priorityString.isEmpty()) {
                dialogBinding.actPriority.setError("Select Priority");
                dialogBinding.actPriority.requestFocus();
                return;
            }

            int priority = Assignment.PRIORITY_LOW;

            switch (priorityString) {

                case "Medium":
                    priority = Assignment.PRIORITY_MEDIUM;
                    break;

                case "High":
                    priority = Assignment.PRIORITY_HIGH;
                    break;

                default:
                    priority = Assignment.PRIORITY_LOW;
                    break;
            }

            long currentTime = System.currentTimeMillis();

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

                viewModel.addAssignment(
                        newAssignment,
                        new FirestoreCallback<Void>() {

                            @Override
                            public void onSuccess(Void result) {

                                Snackbar.make(
                                                binding.getRoot(),
                                                "Assignment Added",
                                                Snackbar.LENGTH_SHORT)
                                        .show();

                                dialog.dismiss();
                            }

                            @Override
                            public void onFailure(Exception e) {

                                Snackbar.make(
                                                binding.getRoot(),
                                                e.getMessage(),
                                                Snackbar.LENGTH_LONG)
                                        .show();
                            }
                        });

            } else {

                assignment.setTitle(title);
                assignment.setSubject(subject);
                assignment.setDescription(description);
                assignment.setPriority(priority);
                assignment.setDueDateTime(calendar.getTimeInMillis());
                assignment.setUpdatedAt(currentTime);

                viewModel.updateAssignment(
                        assignment,
                        new FirestoreCallback<Void>() {

                            @Override
                            public void onSuccess(Void result) {

                                Snackbar.make(
                                                binding.getRoot(),
                                                "Assignment Updated",
                                                Snackbar.LENGTH_SHORT)
                                        .show();

                                dialog.dismiss();
                            }

                            @Override
                            public void onFailure(Exception e) {

                                Snackbar.make(
                                                binding.getRoot(),
                                                e.getMessage(),
                                                Snackbar.LENGTH_LONG)
                                        .show();
                            }
                        });
            }

        });

        dialog.show();
    }

    private void deleteAssignment(@NonNull Assignment assignment) {

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Assignment")
                .setMessage("Are you sure you want to delete this assignment?")
                .setPositiveButton("Delete", (dialog, which) ->

                        viewModel.deleteAssignment(
                                assignment.getId(),
                                new FirestoreCallback<Void>() {

                                    @Override
                                    public void onSuccess(Void result) {

                                        Snackbar.make(
                                                        binding.getRoot(),
                                                        "Assignment deleted",
                                                        Snackbar.LENGTH_SHORT)
                                                .show();
                                    }

                                    @Override
                                    public void onFailure(Exception e) {

                                        Snackbar.make(
                                                        binding.getRoot(),
                                                        e.getMessage(),
                                                        Snackbar.LENGTH_LONG)
                                                .show();
                                    }
                                }))

                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}