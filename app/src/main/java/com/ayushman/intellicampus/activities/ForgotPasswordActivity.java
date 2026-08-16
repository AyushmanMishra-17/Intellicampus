package com.ayushman.intellicampus.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import com.ayushman.intellicampus.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {
    private TextInputEditText etEmail;
    private MaterialButton btnResetPassword;
    private TextView tvBackToLogin;
    private FirebaseAuth auth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);
        auth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        btnResetPassword.setOnClickListener(v -> resetPassword());

        tvBackToLogin.setOnClickListener(v -> finish());
    }
    private void resetPassword() {

        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Enter your email");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            etEmail.requestFocus();
            return;
        }

        btnResetPassword.setEnabled(false);
        btnResetPassword.setText("Sending...");

        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {

                    btnResetPassword.setEnabled(true);
                    btnResetPassword.setText("Send Reset Link");

                    if (task.isSuccessful()) {

                        new AlertDialog.Builder(this)
                                .setTitle("Password Reset Link Sent")
                                .setCancelable(false)
                                .setMessage("A password reset link has been sent to your registered email address.")
                                .setPositiveButton("OK", (dialog, which) -> {
                                    etEmail.setText("");
                                    finish();
                                })
                                .show();

                    } else {

                        String error = task.getException() != null
                                ? task.getException().getMessage()
                                : "Unable to send reset email.";

                        new AlertDialog.Builder(this)
                                .setTitle("Reset Failed")
                                .setMessage(error)
                                .setPositiveButton("OK", null)
                                .show();
                    }

                });
    }
}
