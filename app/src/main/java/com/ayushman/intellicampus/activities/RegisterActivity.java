package com.ayushman.intellicampus.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ayushman.intellicampus.R;
import com.ayushman.intellicampus.constants.RoleConstants;
import com.ayushman.intellicampus.constants.UserConstants;
import com.ayushman.intellicampus.models.AcademicBatch;
import com.ayushman.intellicampus.repositories.AcademicBatchRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword;
    private AutoCompleteTextView actBatch;
    private MaterialButton btnRegister;
    private TextView tvLogin;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    private final AcademicBatchRepository batchRepository =
            new AcademicBatchRepository();

    private final List<AcademicBatch> availableBatches =
            new ArrayList<>();

    private AcademicBatch selectedBatch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        actBatch = findViewById(R.id.actBatch);

        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        loadAvailableBatches();

        btnRegister.setOnClickListener(v -> registerUser());

        tvLogin.setOnClickListener(v -> finish());
    }

    // ========================================================
    // LOAD AVAILABLE BATCHES
    // ========================================================

    private void loadAvailableBatches() {

        actBatch.setEnabled(false);
        actBatch.setText("Loading batches...", false);

        batchRepository.getActiveBatches(
                new AcademicBatchRepository.BatchListCallback() {

                    @Override
                    public void onSuccess(
                            List<AcademicBatch> batches
                    ) {

                        availableBatches.clear();
                        availableBatches.addAll(batches);

                        List<String> labels =
                                new ArrayList<>();

                        for (AcademicBatch batch : batches) {

                            labels.add(
                                    batch.getBatch() +
                                            " • " +
                                            batch.getProgramme()
                            );
                        }

                        ArrayAdapter<String> adapter =
                                new ArrayAdapter<>(
                                        RegisterActivity.this,
                                        android.R.layout.simple_dropdown_item_1line,
                                        labels
                                );

                        actBatch.setAdapter(adapter);
                        actBatch.setEnabled(!labels.isEmpty());
                        actBatch.setText("", false);

                        actBatch.setOnItemClickListener(
                                (parent, view, position, id) -> {

                                    if (position >= 0 &&
                                            position < availableBatches.size()) {

                                        selectedBatch =
                                                availableBatches.get(position);
                                    }
                                }
                        );

                        if (labels.isEmpty()) {

                            actBatch.setHint(
                                    "No active batches available"
                            );

                            new AlertDialog.Builder(
                                    RegisterActivity.this
                            )
                                    .setTitle(
                                            "Registration Unavailable"
                                    )
                                    .setMessage(
                                            "No active academic batches have been created yet. " +
                                                    "Please contact the administrator."
                                    )
                                    .setPositiveButton(
                                            "OK",
                                            null
                                    )
                                    .show();
                        }
                    }

                    @Override
                    public void onError(Exception e) {

                        actBatch.setEnabled(false);
                        actBatch.setText("", false);
                        actBatch.setHint(
                                "Unable to load batches"
                        );

                        new AlertDialog.Builder(
                                RegisterActivity.this
                        )
                                .setTitle("Unable to Load Batches")
                                .setMessage(
                                        "We couldn't load the available admission batches.\n\n" +
                                                e.getMessage()
                                )
                                .setPositiveButton("OK", null)
                                .show();
                    }
                }
        );
    }

    // ========================================================
    // REGISTER
    // ========================================================

    private void registerUser() {

        String name =
                String.valueOf(
                        etName.getText()
                ).trim();

        String email =
                String.valueOf(
                        etEmail.getText()
                ).trim();

        String password =
                String.valueOf(
                        etPassword.getText()
                ).trim();

        String confirmPassword =
                String.valueOf(
                        etConfirmPassword.getText()
                ).trim();

        // ----------------------------------------------------
        // NAME
        // ----------------------------------------------------

        if (TextUtils.isEmpty(name)) {

            etName.setError(
                    "Enter your full name"
            );

            etName.requestFocus();
            return;
        }

        if (name.length() < 3) {

            etName.setError(
                    "Name must be at least 3 characters"
            );

            etName.requestFocus();
            return;
        }

        // ----------------------------------------------------
        // EMAIL
        // ----------------------------------------------------

        if (TextUtils.isEmpty(email)) {

            etEmail.setError(
                    "Enter your email"
            );

            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()) {

            etEmail.setError(
                    "Enter a valid email address"
            );

            etEmail.requestFocus();
            return;
        }

        // ----------------------------------------------------
        // BATCH
        // ----------------------------------------------------

        if (selectedBatch == null) {

            actBatch.setError(
                    "Select your admission batch"
            );

            actBatch.requestFocus();
            return;
        }

        // ----------------------------------------------------
        // PASSWORD
        // ----------------------------------------------------

        if (TextUtils.isEmpty(password)) {

            etPassword.setError(
                    "Enter your password"
            );

            etPassword.requestFocus();
            return;
        }

        if (password.length() < 8) {

            etPassword.setError(
                    "Password must be at least 8 characters"
            );

            etPassword.requestFocus();
            return;
        }

        // ----------------------------------------------------
        // CONFIRM PASSWORD
        // ----------------------------------------------------

        if (!password.equals(confirmPassword)) {

            etConfirmPassword.setError(
                    "Passwords do not match"
            );

            etConfirmPassword.requestFocus();
            return;
        }

        btnRegister.setEnabled(false);
        btnRegister.setText("Creating Account...");

        auth.createUserWithEmailAndPassword(
                        email,
                        password
                )
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {

                        btnRegister.setEnabled(true);
                        btnRegister.setText(
                                "Create Account"
                        );

                        etEmail.setError(
                                "Account could not be created. The email may already be in use."
                        );

                        etEmail.requestFocus();

                        return;
                    }

                    FirebaseUser user =
                            auth.getCurrentUser();

                    if (user == null) {

                        btnRegister.setEnabled(true);
                        btnRegister.setText(
                                "Create Account"
                        );

                        return;
                    }

                    // ------------------------------------------------
                    // SAVE USER
                    // ------------------------------------------------

                    Map<String, Object> userData =
                            new HashMap<>();

                    userData.put(
                            UserConstants.NAME,
                            name
                    );

                    userData.put(
                            UserConstants.EMAIL,
                            email
                    );

                    userData.put(
                            UserConstants.CREATED_AT,
                            System.currentTimeMillis()
                    );

                    userData.put(
                            UserConstants.ROLE,
                            RoleConstants.STUDENT
                    );

                    // The batch is permanent.
                    // Semester is maintained centrally on the
                    // academic_batches document.
                    userData.put(
                            UserConstants.BATCH,
                            selectedBatch.getBatch()
                    );

                    userData.put(
                            UserConstants.COURSE,
                            selectedBatch.getProgramme()
                    );

                    firestore
                            .collection("users")
                            .document(user.getUid())
                            .set(userData)
                            .addOnSuccessListener(
                                    unused -> {

                                        user.sendEmailVerification()
                                                .addOnCompleteListener(
                                                        verifyTask -> {

                                                            btnRegister.setEnabled(
                                                                    true
                                                            );

                                                            btnRegister.setText(
                                                                    "Create Account"
                                                            );

                                                            new AlertDialog.Builder(
                                                                    RegisterActivity.this
                                                            )
                                                                    .setTitle(
                                                                            "Registration Successful"
                                                                    )
                                                                    .setMessage(
                                                                            "Your " +
                                                                                    selectedBatch.getBatch() +
                                                                                    " batch has been selected.\n\n" +
                                                                                    "A verification email has been sent. " +
                                                                                    "Please verify your email before logging in."
                                                                    )
                                                                    .setCancelable(
                                                                            false
                                                                    )
                                                                    .setPositiveButton(
                                                                            "Go to Login",
                                                                            (dialog, which) -> {

                                                                                auth.signOut();
                                                                                finish();
                                                                            }
                                                                    )
                                                                    .show();
                                                        }
                                                );
                                    }
                            )
                            .addOnFailureListener(
                                    e -> {

                                        btnRegister.setEnabled(true);
                                        btnRegister.setText(
                                                "Create Account"
                                        );

                                        new AlertDialog.Builder(
                                                RegisterActivity.this
                                        )
                                                .setTitle(
                                                        "Registration Failed"
                                                )
                                                .setMessage(
                                                        "Unable to save your profile.\n\n" +
                                                                e.getMessage()
                                                )
                                                .setPositiveButton(
                                                        "OK",
                                                        null
                                                )
                                                .show();
                                    }
                            );
                });
    }
}
