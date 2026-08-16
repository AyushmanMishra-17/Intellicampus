package com.ayushman.intellicampus.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.ayushman.intellicampus.R;
import com.ayushman.intellicampus.activities.LoginActivity;
import com.ayushman.intellicampus.constants.RoleConstants;
import com.ayushman.intellicampus.constants.UserConstants;
import com.ayushman.intellicampus.databinding.FragmentProfileBinding;
import com.ayushman.intellicampus.firebase.FirestoreManager;
import com.ayushman.intellicampus.models.AcademicBatch;
import com.ayushman.intellicampus.repositories.AcademicBatchRepository;
import com.ayushman.intellicampus.fragments.NoticeFragment;
import com.ayushman.intellicampus.fragments.ScheduleFragment;
import com.ayushman.intellicampus.fragments.AssignmentFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private FirebaseAuth auth;

    private final AcademicBatchRepository batchRepository =
            new AcademicBatchRepository();

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentProfileBinding.inflate(
                inflater,
                container,
                false
        );

        auth = FirebaseAuth.getInstance();

        loadUserProfile();
        initListeners();

        return binding.getRoot();
    }

    // ========================================================
    // LOAD USER PROFILE
    // ========================================================

    @SuppressLint("SetTextI18n")
    private void loadUserProfile() {

        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
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

                    String name =
                            document.getString(UserConstants.NAME);

                    String email =
                            document.getString(UserConstants.EMAIL);

                    String role =
                            document.getString(UserConstants.ROLE);

                    String studentBatch =
                            document.getString(UserConstants.BATCH);

                    // ------------------------------------------------
                    // NAME
                    // ------------------------------------------------

                    if (name != null &&
                            !name.trim().isEmpty()) {

                        binding.tvProfileName.setText(name);

                        binding.tvAvatarLetter.setText(
                                String.valueOf(
                                        Character.toUpperCase(
                                                name.charAt(0)
                                        )
                                )
                        );

                    } else {

                        binding.tvProfileName.setText("Student");
                        binding.tvAvatarLetter.setText("S");
                    }

                    // ------------------------------------------------
                    // EMAIL
                    // ------------------------------------------------

                    if (email != null &&
                            !email.trim().isEmpty()) {

                        binding.tvProfileEmail.setText(email);
                    }

                    // ------------------------------------------------
                    // BATCH
                    // ------------------------------------------------

                    if (studentBatch != null &&
                            !studentBatch.trim().isEmpty()) {

                        loadBatchForProfile(
                                studentBatch.trim()
                        );

                    } else {

                        binding.tvAcademicBatch.setText(
                                "Batch: Not set"
                        );
                    }

                    // ------------------------------------------------
                    // ROLE
                    // ------------------------------------------------

                    if (RoleConstants.ADMIN.equalsIgnoreCase(role)) {

                        binding.chipRole.setText(
                                "Administrator"
                        );

                        binding.cardAdminPanel.setVisibility(
                                View.VISIBLE
                        );

                    } else {

                        binding.chipRole.setText(
                                "Student"
                        );

                        binding.cardAdminPanel.setVisibility(
                                View.GONE
                        );
                    }
                });
    }

    private void loadBatchForProfile(String batchName) {

        batchRepository.getBatch(
                batchName,
                new AcademicBatchRepository.BatchCallback() {

                    @Override
                    public void onSuccess(
                            AcademicBatch batch
                    ) {

                        binding.tvAcademicBatch.setText(
                                "Batch: " +
                                        batch.getBatch() +
                                        " • Semester " +
                                        batch.getCurrentSemester() +
                                        " • " +
                                        batch.getCurrentAcademicYear()
                        );
                    }

                    @Override
                    public void onError(Exception e) {

                        binding.tvAcademicBatch.setText(
                                "Batch: " + batchName
                        );
                    }
                }
        );
    }

    // ========================================================
    // LISTENERS
    // ========================================================

    private void initListeners() {

        binding.btnLogout.setOnClickListener(v -> {

            auth.signOut();

            Intent intent =
                    new Intent(
                            requireActivity(),
                            LoginActivity.class
                    );

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);
        });

        binding.layoutEditProfile.setOnClickListener(v -> showEditProfileDialog());

        binding.layoutChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        binding.layoutManageBatches.setOnClickListener(v ->
                showBatchManagementDialog());

        binding.layoutPromoteSemester.setOnClickListener(v ->
                showBatchPromotionDialog());

        binding.layoutManageNotice.setOnClickListener(v -> openAdminSection(new NoticeFragment()));

        binding.layoutManageTimetable.setOnClickListener(v -> openAdminSection(new ScheduleFragment()));

        binding.layoutManageAssignments.setOnClickListener(v -> openAdminSection(new AssignmentFragment()));

        binding.btnChangePhoto.setOnClickListener(v -> {
            // Existing placeholder retained.
        });
    }

    private void openAdminSection(@NonNull Fragment fragment) {
        if (!isAdded()) return;

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack("admin_section")
                .commit();
    }

    // ========================================================
    // PROFILE EDITING
    // ========================================================

    private void showEditProfileDialog() {
        if (!isAdded() || auth.getCurrentUser() == null) return;

        EditText input = new EditText(requireContext());
        input.setSingleLine(true);
        input.setHint("Full name");

        FirestoreManager.getInstance().getCurrentUserDocument().get()
                .addOnSuccessListener(document -> {
                    String current = document.getString(UserConstants.NAME);
                    if (current != null) input.setText(current);
                    input.setSelection(input.length());

                    AlertDialog dialog = new AlertDialog.Builder(requireContext())
                            .setTitle("Edit Profile")
                            .setView(input)
                            .setNegativeButton("Cancel", null)
                            .setPositiveButton("Save", null)
                            .create();
                    dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                        String name = input.getText().toString().trim();
                        if (name.length() < 3) {
                            input.setError("Enter at least 3 characters");
                            return;
                        }
                        FirestoreManager.getInstance().getCurrentUserDocument()
                                .update(UserConstants.NAME, name)
                                .addOnSuccessListener(unused -> {
                                    dialog.dismiss();
                                    loadUserProfile();
                                    Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Could not update profile: " + e.getMessage(), Toast.LENGTH_LONG).show());
                    }));
                    dialog.show();
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Could not load profile: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void showChangePasswordDialog() {
        if (!isAdded() || auth.getCurrentUser() == null) return;

        LinearLayout box = new LinearLayout(requireContext());
        box.setOrientation(LinearLayout.VERTICAL);
        int padding = dp(8);
        box.setPadding(padding, 0, padding, 0);

        EditText current = passwordField("Current password");
        EditText next = passwordField("New password");
        EditText confirm = passwordField("Confirm new password");
        box.addView(current);
        box.addView(next);
        box.addView(confirm);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Change Password")
                .setMessage("For security, Firebase requires your current password before changing it.")
                .setView(box)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Change", null)
                .create();

        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String oldPassword = current.getText().toString();
            String newPassword = next.getText().toString();
            String confirmPassword = confirm.getText().toString();
            if (oldPassword.isEmpty()) { current.setError("Enter your current password"); return; }
            if (newPassword.length() < 6) { next.setError("Password must be at least 6 characters"); return; }
            if (!newPassword.equals(confirmPassword)) { confirm.setError("Passwords do not match"); return; }

            FirebaseUser user = auth.getCurrentUser();
            com.google.firebase.auth.AuthCredential credential =
                    com.google.firebase.auth.EmailAuthProvider.getCredential(user.getEmail(), oldPassword);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            user.reauthenticate(credential)
                    .addOnSuccessListener(unused -> user.updatePassword(newPassword)
                            .addOnSuccessListener(done -> {
                                dialog.dismiss();
                                Toast.makeText(requireContext(), "Password changed successfully", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                                Toast.makeText(requireContext(), "Could not change password: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }))
                    .addOnFailureListener(e -> {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        current.setError("Current password is incorrect");
                    });
        }));
        dialog.show();
    }

    private EditText passwordField(String hint) {
        EditText field = new EditText(requireContext());
        field.setHint(hint);
        field.setSingleLine(true);
        field.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        return field;
    }

    // ========================================================
    // BATCH MANAGEMENT
    // ========================================================

    private void showBatchManagementDialog() {

        if (!isAdded()) {
            return;
        }

        batchRepository.getAllBatches(
                new AcademicBatchRepository.BatchListCallback() {

                    @Override
                    public void onSuccess(
                            List<AcademicBatch> batches
                    ) {

                        showBatchListDialog(batches);
                    }

                    @Override
                    public void onError(Exception e) {

                        showResultDialog(
                                "Unable to Load Batches",
                                e.getMessage()
                        );
                    }
                }
        );
    }

    private void showBatchListDialog(
            List<AcademicBatch> batches
    ) {

        if (!isAdded()) {
            return;
        }

        List<String> labels =
                new ArrayList<>();

        for (AcademicBatch batch : batches) {

            labels.add(
                    batch.getBatch() +
                            " • Sem " +
                            batch.getCurrentSemester() +
                            " • " +
                            batch.getCurrentAcademicYear() +
                            (batch.isActive()
                                    ? ""
                                    : " • INACTIVE")
            );
        }

        if (labels.isEmpty()) {

            new AlertDialog.Builder(requireContext())
                    .setTitle("Academic Batches")
                    .setMessage(
                            "No batches exist yet. Create your first batch."
                    )
                    .setPositiveButton(
                            "Add Batch",
                            (dialog, which) ->
                                    showCreateBatchDialog()
                    )
                    .setNegativeButton(
                            "Close",
                            null
                    )
                    .show();

            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Academic Batches")
                .setItems(
                        labels.toArray(
                                new String[0]
                        ),
                        (dialog, which) -> {

                            if (which >= 0 &&
                                    which < batches.size()) {

                                showBatchActionsDialog(
                                        batches.get(which)
                                );
                            }
                        }
                )
                .setPositiveButton(
                        "Add Batch",
                        (dialog, which) ->
                                showCreateBatchDialog()
                )
                .setNegativeButton(
                        "Close",
                        null
                )
                .show();
    }

    // ========================================================
    // CREATE BATCH
    // ========================================================

    private void showCreateBatchDialog() {

        if (!isAdded()) {
            return;
        }

        LinearLayout container =
                new LinearLayout(requireContext());

        container.setOrientation(
                LinearLayout.VERTICAL
        );

        int padding = dp(8);

        container.setPadding(
                padding,
                0,
                padding,
                0
        );

        EditText etBatch =
                new EditText(requireContext());

        etBatch.setHint(
                "Admission batch (e.g. 2024-25)"
        );

        etBatch.setSingleLine(true);

        EditText etProgramme =
                new EditText(requireContext());

        etProgramme.setHint(
                "Programme (e.g. BCA)"
        );

        etProgramme.setSingleLine(true);
        etProgramme.setText("BCA");

        container.addView(etBatch);
        container.addView(etProgramme);

        AlertDialog dialog =
                new AlertDialog.Builder(
                        requireContext()
                )
                        .setTitle("Add Academic Batch")
                        .setMessage(
                                "New students will start in Semester 1."
                        )
                        .setView(container)
                        .setNegativeButton(
                                "Cancel",
                                null
                        )
                        .setPositiveButton(
                                "Create",
                                null
                        )
                        .create();

        dialog.setOnShowListener(
                ignored -> dialog.getButton(
                        AlertDialog.BUTTON_POSITIVE
                ).setOnClickListener(v -> {

                    String batch =
                            etBatch.getText()
                                    .toString()
                                    .trim();

                    String programme =
                            etProgramme.getText()
                                    .toString()
                                    .trim();

                    if (!batch.matches(
                            "\\d{4}-\\d{2}"
                    )) {

                        etBatch.setError(
                                "Use format YYYY-YY, e.g. 2024-25"
                        );

                        return;
                    }

                    if (programme.isEmpty()) {

                        etProgramme.setError(
                                "Enter the programme"
                        );

                        return;
                    }

                    batchRepository.createBatch(
                            batch,
                            programme,
                            new AcademicBatchRepository.BatchCallback() {

                                @Override
                                public void onSuccess(
                                        AcademicBatch created
                                ) {

                                    dialog.dismiss();

                                    showResultDialog(
                                            "Batch Created",
                                            created.getBatch() +
                                                    " has been created. " +
                                                    "New students will start in Semester 1."
                                    );
                                }

                                @Override
                                public void onError(Exception e) {

                                    showResultDialog(
                                            "Could Not Create Batch",
                                            e.getMessage()
                                    );
                                }
                            }
                    );
                })
        );

        dialog.show();
    }

    // ========================================================
    // BATCH ACTIONS
    // ========================================================

    private void showBatchActionsDialog(
            AcademicBatch batch
    ) {

        if (!isAdded()) {
            return;
        }

        String message =
                "Programme: " +
                        batch.getProgramme() +
                        "\nCurrent semester: " +
                        batch.getCurrentSemester() +
                        "\nAcademic year: " +
                        batch.getCurrentAcademicYear() +
                        "\nStatus: " +
                        (batch.isActive()
                                ? "Active"
                                : "Inactive");

        List<String> actions =
                new ArrayList<>();

        if (batch.getCurrentSemester() < 8) {
            actions.add("Promote to Semester " +
                    (batch.getCurrentSemester() + 1));
        }

        actions.add(
                batch.isActive()
                        ? "Deactivate batch"
                        : "Activate batch"
        );

        new AlertDialog.Builder(requireContext())
                .setTitle(batch.getBatch())
                .setMessage(message)
                .setItems(
                        actions.toArray(
                                new String[0]
                        ),
                        (dialog, which) -> {

                            if (batch.getCurrentSemester() < 8 &&
                                    which == 0) {

                                confirmBatchPromotion(batch);

                            } else {

                                toggleBatchStatus(batch);
                            }
                        }
                )
                .setNegativeButton(
                        "Close",
                        null
                )
                .show();
    }

    // ========================================================
    // PROMOTE ONE BATCH
    // ========================================================

    private void showBatchPromotionDialog() {

        batchRepository.getAllBatches(
                new AcademicBatchRepository.BatchListCallback() {

                    @Override
                    public void onSuccess(
                            List<AcademicBatch> batches
                    ) {

                        showBatchListDialogForPromotion(
                                batches
                        );
                    }

                    @Override
                    public void onError(Exception e) {

                        showResultDialog(
                                "Unable to Load Batches",
                                e.getMessage()
                        );
                    }
                }
        );
    }

    private void showBatchListDialogForPromotion(
            List<AcademicBatch> batches
    ) {

        if (!isAdded()) {
            return;
        }

        List<AcademicBatch> promotable =
                new ArrayList<>();

        List<String> labels =
                new ArrayList<>();

        for (AcademicBatch batch : batches) {

            if (!batch.isActive() ||
                    batch.getCurrentSemester() >= 8) {
                continue;
            }

            promotable.add(batch);

            labels.add(
                    batch.getBatch() +
                            " • Sem " +
                            batch.getCurrentSemester() +
                            " → " +
                            (batch.getCurrentSemester() + 1)
            );
        }

        if (labels.isEmpty()) {

            showResultDialog(
                    "Nothing to Promote",
                    "There are no active batches that can be promoted."
            );

            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Promote a Batch")
                .setItems(
                        labels.toArray(
                                new String[0]
                        ),
                        (dialog, which) -> {

                            if (which >= 0 &&
                                    which < promotable.size()) {

                                confirmBatchPromotion(
                                        promotable.get(which)
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

    private void confirmBatchPromotion(
            AcademicBatch batch
    ) {

        if (!isAdded()) {
            return;
        }

        int nextSemester =
                batch.getCurrentSemester() + 1;

        FirebaseFirestore firestore =
                FirestoreManager
                        .getInstance()
                        .getFirestore();

        firestore
                .collection("users")
                .whereEqualTo(
                        UserConstants.BATCH,
                        batch.getBatch()
                )
                .get()
                .addOnSuccessListener(
                        snapshot -> {

                            int count =
                                    snapshot.size();

                            String message =
                                    "Batch " +
                                            batch.getBatch() +
                                            " will move from Semester " +
                                            batch.getCurrentSemester() +
                                            " to Semester " +
                                            nextSemester +
                                            ".\n\n" +
                                            count +
                                            " registered student" +
                                            (count == 1 ? "" : "s") +
                                            " belong to this batch.\n\n" +
                                            "Only the batch's academic state will be updated; " +
                                            "student documents will not be rewritten.";

                            new AlertDialog.Builder(
                                    requireContext()
                            )
                                    .setTitle(
                                            "Confirm Promotion"
                                    )
                                    .setMessage(message)
                                    .setNegativeButton(
                                            "Cancel",
                                            null
                                    )
                                    .setPositiveButton(
                                            "Promote",
                                            (dialog, which) ->
                                                    executeBatchPromotion(
                                                            batch
                                                    )
                                    )
                                    .show();
                        }
                )
                .addOnFailureListener(
                        e ->
                                showResultDialog(
                                        "Promotion Failed",
                                        "Unable to count students.\n\n" +
                                                e.getMessage()
                                )
                );
    }

    private void executeBatchPromotion(
            AcademicBatch batch
    ) {

        batchRepository.promoteBatch(
                batch,
                new AcademicBatchRepository.BatchCallback() {

                    @Override
                    public void onSuccess(
                            AcademicBatch updated
                    ) {

                        showResultDialog(
                                "Promotion Successful",
                                updated.getBatch() +
                                        " is now in Semester " +
                                        updated.getCurrentSemester() +
                                        " • " +
                                        updated.getCurrentAcademicYear() +
                                        "."
                        );
                    }

                    @Override
                    public void onError(Exception e) {

                        showResultDialog(
                                "Promotion Failed",
                                e.getMessage()
                        );
                    }
                }
        );
    }

    // ========================================================
    // ACTIVATE / DEACTIVATE
    // ========================================================

    private void toggleBatchStatus(
            AcademicBatch batch
    ) {

        boolean newStatus =
                !batch.isActive();

        FirestoreManager
                .getInstance()
                .getFirestore()
                .collection(
                        "academic_batches"
                )
                .document(
                        batch.getBatch()
                )
                .update(
                        "active",
                        newStatus
                )
                .addOnSuccessListener(
                        unused ->
                                showResultDialog(
                                        "Batch Updated",
                                        batch.getBatch() +
                                                " is now " +
                                                (newStatus
                                                        ? "active."
                                                        : "inactive.")
                                )
                )
                .addOnFailureListener(
                        e ->
                                showResultDialog(
                                        "Update Failed",
                                        e.getMessage()
                                )
                );
    }

    // ========================================================
    // RESULT
    // ========================================================

    private void showResultDialog(
            String title,
            String message
    ) {

        if (!isAdded()) {
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(message == null
                        ? "Unknown error."
                        : message)
                .setPositiveButton(
                        "OK",
                        null
                )
                .show();
    }

    private int dp(int value) {
        return (int) (
                value *
                        getResources()
                                .getDisplayMetrics()
                                .density
        );
    }

    @Override
    public void onDestroyView() {

        super.onDestroyView();

        binding = null;
    }
}
