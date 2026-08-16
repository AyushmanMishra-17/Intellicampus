package com.ayushman.intellicampus.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ayushman.intellicampus.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private TextView tvForgotPassword, tvRegister;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvRegister = findViewById(R.id.tvRegister);

        btnLogin.setOnClickListener(v -> loginUser());

        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class)));
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null && user.isEmailVerified()) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }

    @SuppressLint("SetTextI18n")
    private void loginUser() {

        String email = Objects.requireNonNull(etEmail.getText()).toString().trim();
        String password = Objects.requireNonNull(etPassword.getText()).toString().trim();

        // Email Validation
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Enter your email");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email address");
            etEmail.requestFocus();
            return;
        }

        // Password Validation
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Enter your password");
            etPassword.requestFocus();
            return;
        }

        // Disable button while login is in progress
        btnLogin.setEnabled(false);
        btnLogin.setText("Signing In...");

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    // Re-enable button
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Sign In");

                    if (task.isSuccessful()) {

                        FirebaseUser user = auth.getCurrentUser();

                        if (user != null && user.isEmailVerified()) {

                            Toast.makeText(
                                    LoginActivity.this,
                                    "Login Successful",
                                    Toast.LENGTH_SHORT
                            ).show();

                            startActivity(new Intent(
                                    LoginActivity.this,
                                    MainActivity.class));

                            finish();

                        } else {

                            Toast.makeText(
                                    LoginActivity.this,
                                    "Please verify your email before logging in.",
                                    Toast.LENGTH_LONG
                            ).show();

                            auth.signOut();
                        }

                    } else {

                        Toast.makeText(
                                LoginActivity.this,
                                "Login failed. Please check your email and password.",
                                Toast.LENGTH_LONG
                        ).show();
                    }

                });
    }
}