package com.ayushman.intellicampus.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.ayushman.intellicampus.R;
import com.ayushman.intellicampus.fragments.StudentHubFragment;
import com.ayushman.intellicampus.fragments.StudyPlannerFragment;
import com.ayushman.intellicampus.databinding.FragmentAcademicsBinding;
import com.ayushman.intellicampus.models.Assignment;
import com.ayushman.intellicampus.viewmodels.AssignmentViewModel;

import java.util.List;

public class AcademicsFragment extends Fragment {

    private FragmentAcademicsBinding binding;
    private AssignmentViewModel assignmentViewModel;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentAcademicsBinding.inflate(
                inflater,
                container,
                false
        );

        initViews();
        initListeners();
        setupAssignmentViewModel();
        loadData();

        return binding.getRoot();
    }

    private void initViews() {
        // Reserved for future view initialization.
    }

    private void initListeners() {

        binding.cardAssignments.setOnClickListener(v ->
                openAssignments());

        binding.cardNotes.setOnClickListener(v ->
                openSubjects());

        binding.cardCommunity.setOnClickListener(v ->
                openFeature(new StudentHubFragment()));

        binding.cardStudyPlanner.setOnClickListener(v ->
                openFeature(new StudyPlannerFragment()));
    }

    private void setupAssignmentViewModel() {

        assignmentViewModel =
                new ViewModelProvider(this)
                        .get(AssignmentViewModel.class);

        assignmentViewModel
                .getAssignments()
                .observe(
                        getViewLifecycleOwner(),
                        this::updateAssignmentCount
                );
    }

    private void loadData() {

        // Load assignments from Firestore.
        // The ViewModel listens for real-time changes,
        // so the dashboard count updates automatically.
        assignmentViewModel.loadAssignments();
    }

    private void updateAssignmentCount(
            List<Assignment> assignments) {

        if (binding == null) {
            return;
        }

        int pendingCount = 0;

        if (assignments != null) {

            for (Assignment assignment : assignments) {

                if (assignment == null) {
                    continue;
                }

                /*
                 * Pending means the assignment is neither
                 * completed nor overdue.
                 */
                if (!assignment.isCompleted()
                        && !assignment.isOverdue()) {

                    pendingCount++;
                }
            }
        }

        binding.tvAssignmentCount.setText(
                pendingCount + " Pending"
        );
    }

    private void openAssignments() {

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.fragment_container,
                        new AssignmentFragment()
                )
                .addToBackStack(null)
                .commit();
    }

    private void openSubjects() {

        Intent intent = new Intent(
                requireContext(),
                com.ayushman.intellicampus.activities.SubjectListActivity.class
        );

        startActivity(intent);
    }

    private void openFeature(Fragment fragment) {
        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}