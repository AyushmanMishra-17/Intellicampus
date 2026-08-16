package com.ayushman.intellicampus.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ayushman.intellicampus.adapters.SubjectAdapter;
import com.ayushman.intellicampus.constants.UserConstants;
import com.ayushman.intellicampus.databinding.ActivitySubjectListBinding;
import com.ayushman.intellicampus.models.AcademicBatch;
import com.ayushman.intellicampus.models.Subject;
import com.ayushman.intellicampus.repositories.AcademicBatchRepository;
import com.ayushman.intellicampus.viewmodels.StudyViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SubjectListActivity extends AppCompatActivity {

    private ActivitySubjectListBinding binding;

    private SubjectAdapter adapter;

    private final List<Subject> subjectList =
            new ArrayList<>();

    private StudyViewModel viewModel;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    private final AcademicBatchRepository batchRepository =
            new AcademicBatchRepository();

    private String course;
    private String batch;
    private String academicYear;
    private int semester;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        binding =
                ActivitySubjectListBinding.inflate(
                        getLayoutInflater()
                );

        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        setupRecyclerView();
        setupViewModel();
        loadStudentProfile();
    }

    // ========================================================
    // RECYCLER VIEW
    // ========================================================

    private void setupRecyclerView() {

        adapter =
                new SubjectAdapter(subjectList);

        binding.rvSubjects.setLayoutManager(
                new LinearLayoutManager(this)
        );

        binding.rvSubjects.setAdapter(adapter);
    }

    // ========================================================
    // VIEW MODEL
    // ========================================================

    private void setupViewModel() {

        viewModel =
                new ViewModelProvider(this)
                        .get(StudyViewModel.class);

        viewModel.getSubjects()
                .observe(
                        this,
                        subjects -> {

                            subjectList.clear();

                            if (subjects != null) {
                                subjectList.addAll(subjects);
                            }

                            adapter.notifyDataSetChanged();
                            updateEmptyState();
                        }
                );

        viewModel.getError()
                .observe(
                        this,
                        error -> {

                            if (error == null ||
                                    error.trim().isEmpty()) {
                                return;
                            }

                            Toast.makeText(
                                    this,
                                    "Unable to load subjects: " +
                                            error,
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    // ========================================================
    // LOAD STUDENT PROFILE
    // ========================================================

    private void loadStudentProfile() {

        FirebaseUser currentUser =
                auth.getCurrentUser();

        if (currentUser == null) {

            Toast.makeText(
                    this,
                    "No user is logged in.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        firestore
                .collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()) {

                        Toast.makeText(
                                this,
                                "Student profile not found.",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    String storedBatch =
                            document.getString(
                                    UserConstants.BATCH
                            );

                    String storedCourse =
                            document.getString(
                                    UserConstants.COURSE
                            );

                    if (storedBatch == null ||
                            storedBatch.trim().isEmpty()) {

                        Toast.makeText(
                                this,
                                "Academic batch is not set for this account.",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    batch = storedBatch.trim();

                    if (storedCourse != null &&
                            !storedCourse.trim().isEmpty()) {

                        course =
                                storedCourse.trim();
                    }

                    loadBatch();
                })
                .addOnFailureListener(
                        error ->
                                Toast.makeText(
                                        this,
                                        "Failed to load profile: " +
                                                error.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show()
                );
    }

    // ========================================================
    // LOAD BATCH
    // ========================================================

    private void loadBatch() {

        batchRepository.getBatch(
                batch,
                new AcademicBatchRepository.BatchCallback() {

                    @Override
                    public void onSuccess(
                            AcademicBatch academicBatch
                    ) {

                        if (academicBatch.getProgramme() != null &&
                                !academicBatch.getProgramme()
                                        .trim().isEmpty()) {

                            course =
                                    academicBatch
                                            .getProgramme()
                                            .trim();
                        }

                        semester =
                                academicBatch
                                        .getCurrentSemester();

                        academicYear =
                                academicBatch
                                        .getCurrentAcademicYear();

                        if (course == null ||
                                course.isEmpty()) {

                            Toast.makeText(
                                    SubjectListActivity.this,
                                    "Programme is not configured for this batch.",
                                    Toast.LENGTH_LONG
                            ).show();

                            return;
                        }

                        if (semester <= 0) {

                            Toast.makeText(
                                    SubjectListActivity.this,
                                    "Invalid semester configured for this batch.",
                                    Toast.LENGTH_LONG
                            ).show();

                            return;
                        }

                        updateHeader();
                        loadSubjects();
                    }

                    @Override
                    public void onError(Exception e) {

                        Toast.makeText(
                                SubjectListActivity.this,
                                "Unable to load academic batch: " +
                                        e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    // ========================================================
    // LOAD SUBJECTS
    // ========================================================

    private void loadSubjects() {

        if (course == null ||
                course.trim().isEmpty() ||
                semester <= 0) {
            return;
        }

        viewModel.loadSubjects(
                course,
                semester,
                batch
        );
    }

    // ========================================================
    // HEADER
    // ========================================================

    private void updateHeader() {

        if (course == null ||
                semester <= 0) {
            return;
        }

        StringBuilder subtitle =
                new StringBuilder();

        subtitle
                .append(course)
                .append(" • Semester ")
                .append(semester);

        if (academicYear != null &&
                !academicYear.trim().isEmpty()) {

            subtitle
                    .append(" • ")
                    .append(academicYear);
        }

        binding.tvSubtitle.setText(
                subtitle.toString()
        );
    }

    // ========================================================
    // EMPTY STATE
    // ========================================================

    private void updateEmptyState() {

        binding.rvSubjects.setVisibility(
                subjectList.isEmpty()
                        ? View.GONE
                        : View.VISIBLE
        );
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        binding = null;
    }
}
