package com.example.mobileproj;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.security.MessageDigest;

public class LoginActivity extends AppCompatActivity {
    EditText username, password;
    Button login, searchButton;
    FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
        searchButton = findViewById(R.id.btn_search);

        login.setOnClickListener(v -> {
            String user = username.getText().toString().trim();
            String pass = password.getText().toString().trim();

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String hashedPassword = hashPassword(pass);
            Log.d("LOGIN_DEBUG", "Hashed password: " + hashedPassword);

            // Query Firestore for the user
            firestore.collection("users")
                    .whereEqualTo("username", user)
                    .whereEqualTo("password", hashedPassword)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (!task.getResult().isEmpty()) {
                                // User found, check role
                                for (QueryDocumentSnapshot doc : task.getResult()) {
                                    String role = doc.getString("role");
                                    if (role != null) {
                                        Intent intent = role.equals("admin")
                                                ? new Intent(LoginActivity.this, AdminActivity.class)
                                                : new Intent(LoginActivity.this, UserActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(this, "Role missing", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } else {
                                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Login error", Toast.LENGTH_SHORT).show();
                            Log.e("LOGIN_ERROR", "Firestore query failed", task.getException());
                        }
                    });
        });

        searchButton.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, getinfrbymat.class)));
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }

            return hexString.toString();
        } catch (Exception e) {
            Log.e("HASH", "Hashing error", e);
            return null;
        }
    }
}
