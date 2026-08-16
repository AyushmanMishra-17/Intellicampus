package com.ayushman.intellicampus.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ayushman.intellicampus.adapters.TimetableAdapter;
import com.ayushman.intellicampus.constants.RoleConstants;
import com.ayushman.intellicampus.constants.UserConstants;
import com.ayushman.intellicampus.firebase.FirestoreManager;
import com.ayushman.intellicampus.bottomsheets.AddEditTimetableBottomSheet;
import com.ayushman.intellicampus.databinding.FragmentScheduleBinding;
import com.ayushman.intellicampus.models.Timetable;
import com.ayushman.intellicampus.viewmodels.TimetableViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class ScheduleFragment extends Fragment {

    private FragmentScheduleBinding binding;
    private TimetableAdapter adapter;
    private TimetableViewModel viewModel;

    private final List<Timetable> timetableList = new ArrayList<>();

    public ScheduleFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentScheduleBinding.inflate(inflater, container, false);

        initViews();
        setupRecyclerView();
        setupViewModel();
        observeTimetable();

        binding.fabAddTimetable.setOnClickListener(v ->
                AddEditTimetableBottomSheet
                        .newInstance()
                        .show(getParentFragmentManager(), "ADD_TIMETABLE")
        );

        checkAdminAccess();

        return binding.getRoot();
    }

    private void checkAdminAccess() {
        FirestoreManager.getInstance()
                .getCurrentUserDocument()
                .get()
                .addOnSuccessListener(document -> {
                    String role = document.getString(UserConstants.ROLE);
                    boolean isAdmin = RoleConstants.ADMIN.equalsIgnoreCase(role);

                    binding.fabAddTimetable.setVisibility(
                            isAdmin ? View.VISIBLE : View.GONE);
                    adapter.setAdmin(isAdmin);
                })
                .addOnFailureListener(e -> {
                    binding.fabAddTimetable.setVisibility(View.GONE);
                    adapter.setAdmin(false);
                });
    }

    private void initViews() {
        // Future UI initialization
    }

    private void setupRecyclerView() {

        adapter = new TimetableAdapter(new TimetableAdapter.OnTimeTableClickListener() {

            @Override
            public void onEditClick(@NonNull Timetable timetable) {

                AddEditTimetableBottomSheet
                        .newInstance(timetable)
                        .show(getParentFragmentManager(), "EDIT_TIMETABLE");

            }

            @Override
            public void onDeleteClick(@NonNull Timetable timetable) {

                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Delete Timetable")
                        .setMessage("Are you sure you want to delete this timetable?")
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Delete", (dialog, which) ->

                                viewModel.deleteTimetable(timetable.getId())
                                        .addOnSuccessListener(unused ->

                                                Snackbar.make(
                                                        binding.getRoot(),
                                                        "Timetable deleted successfully",
                                                        Snackbar.LENGTH_SHORT
                                                ).show()

                                        ).addOnFailureListener(e ->

                                                Snackbar.make(
                                                        binding.getRoot(),
                                                        e.getMessage(),
                                                        Snackbar.LENGTH_LONG
                                                ).show()

                                        )

                        )
                        .show();

            }
        });

        binding.rvSchedule.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        binding.rvSchedule.setAdapter(adapter);
    }

    private void setupViewModel() {

        viewModel = new ViewModelProvider(this)
                .get(TimetableViewModel.class);

        viewModel.loadTimetable();

    }

    private void observeTimetable() {

        viewModel.getTimetableList().observe(getViewLifecycleOwner(), timetables -> {

            timetableList.clear();

            if (timetables != null) {
                timetableList.addAll(timetables);
            }

            adapter.setTimetable(timetableList);

            binding.layoutEmpty.setVisibility(
                    timetableList.isEmpty()
                            ? View.VISIBLE
                            : View.GONE
            );

        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}