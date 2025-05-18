package com.example.mobileproj;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdminActivity extends AppCompatActivity {
    EditText usernameInput, passwordInput;
    Spinner roleSpinner;
    Button addUserButton;
    ListView usersListView;
    dbconnect db;
    ArrayList<String> usersList;
    ArrayAdapter<String> listAdapter; // Renamed to avoid confusion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        Log.d("AdminActivity", "Activity started");

        try {
            // Initialize database first
            db = new dbconnect(this);
            db.getWritableDatabase(); // Force connection test

            // Bind UI elements
            usernameInput = findViewById(R.id.username_input);
            passwordInput = findViewById(R.id.password_input);
            roleSpinner = findViewById(R.id.role_spinner);
            addUserButton = findViewById(R.id.add_user_button);
            usersListView = findViewById(R.id.users_list);

            // Initialize user list
            usersList = new ArrayList<>();
            listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, usersList);
            usersListView.setAdapter(listAdapter);

            // Set up spinner
            ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                    R.array.roles, android.R.layout.simple_spinner_item);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            roleSpinner.setAdapter(spinnerAdapter);

            // Load initial data
            loadUsers();

        } catch (Exception e) {
            Log.e("AdminActivity", "Initialization error", e);
            Toast.makeText(this, "Initialization failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    public void addUser(View view) {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String role = roleSpinner.getSelectedItem().toString();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("password", password);
        user.put("role", role);

        firestore.collection("users").document(username)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "User added successfully", Toast.LENGTH_SHORT).show();
                    usernameInput.setText("");
                    passwordInput.setText("");
                    loadUsers(); // Refresh list
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminActivity", "Add user error", e);
                    Toast.makeText(this, "Failed to add user", Toast.LENGTH_SHORT).show();
                });
    }
    public void addUser2(View view) {
        try {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String role = roleSpinner.getSelectedItem().toString();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            ContentValues values = new ContentValues();
            values.put("username", username);
            values.put("password", hashPassword(password));
            values.put("role", role);

            long result = db.getWritableDatabase().insert("users", null, values);

            if (result == -1) {
                Toast.makeText(this, "Failed to add user", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "User added successfully", Toast.LENGTH_SHORT).show();
                loadUsers();
                usernameInput.setText("");
                passwordInput.setText("");
            }
        } catch (Exception e) {
            Log.e("AdminActivity", "Add user error", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    private void loadUsers() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    usersList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String username = doc.getString("username");
                        String role = doc.getString("role");
                        if (username != null && role != null) {
                            usersList.add(username + " (" + role + ")");
                        }
                    }
                    listAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminActivity", "Load users error", e);
                    Toast.makeText(this, "Failed to load users", Toast.LENGTH_SHORT).show();
                });
    }
    private void loadUsers2() {
        try (Cursor cursor = db.getReadableDatabase().rawQuery("SELECT * FROM users", null)) {
            usersList.clear();

            if (cursor.moveToFirst()) {
                int usernameIndex = cursor.getColumnIndex("username");
                int roleIndex = cursor.getColumnIndex("role");

                do {
                    if (usernameIndex != -1 && roleIndex != -1) {
                        usersList.add(cursor.getString(usernameIndex) + " (" +
                                cursor.getString(roleIndex) + ")");
                    }
                } while (cursor.moveToNext());
            }
            listAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            Log.e("AdminActivity", "Load users error", e);
            Toast.makeText(this, "Failed to load users", Toast.LENGTH_SHORT).show();
        }
    }
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

}