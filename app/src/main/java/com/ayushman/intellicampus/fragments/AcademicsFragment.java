package com.ayushman.intellicampus.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.ayushman.intellicampus.R;
import com.ayushman.intellicampus.databinding.FragmentAcademicsBinding;

public class AcademicsFragment extends Fragment {

    private FragmentAcademicsBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentAcademicsBinding.inflate(inflater, container, false);

        initViews();
        initListeners();
        loadData();

        return binding.getRoot();
    }

    private void initViews() {

    }

    private void initListeners() {

        binding.cardAssignments.setOnClickListener(v ->
                openAssignments());

        binding.cardNotes.setOnClickListener(v ->
                openSubjects());

        binding.cardAttendance.setOnClickListener(v ->
                Toast.makeText(requireContext(),
                        "Attendance module coming soon",
                        Toast.LENGTH_SHORT).show());
    }

    private void loadData() {

        // Will be connected to Firestore later.
        binding.tvAssignmentCount.setText("0 Pending");
    }

    private void openAssignments() {
        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new AssignmentFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    private void openSubjects() {

        Intent intent = new Intent(
                requireContext(),
                com.ayushman.intellicampus.activities.SubjectListActivity.class
        );

        startActivity(intent);
    }
}