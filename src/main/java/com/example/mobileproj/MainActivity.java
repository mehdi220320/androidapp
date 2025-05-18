package com.example.mobileproj;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        EdgeToEdge.enable(this);
        addAdminIfNotExists(); // Add this call
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void addAdminIfNotExists() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        String adminUsername = "admin";
        String adminPassword = hashPassword("admin123"); // Reuse your hash method

        firestore.collection("users")
                .whereEqualTo("username", adminUsername)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().isEmpty()) {
                        // Admin doesn't exist → create it
                        Map<String, Object> admin = new HashMap<>();
                        admin.put("username", adminUsername);
                        admin.put("password", adminPassword);
                        admin.put("role", "admin");

                        firestore.collection("users")
                                .document(adminUsername) // Use username as document ID
                                .set(admin)
                                .addOnSuccessListener(v -> Log.d("MainActivity", "Admin created"))
                                .addOnFailureListener(e -> Log.e("MainActivity", "Failed to create admin", e));
                    }
                });
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
            Log.e("Hashing", "Error", e);
            return null;
        }
    }
}