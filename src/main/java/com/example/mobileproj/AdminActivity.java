package com.example.mobileproj;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import androidx.appcompat.widget.Toolbar;
import android.widget.Button;

public class AdminActivity extends AppCompatActivity {
    private EditText usernameInput, passwordInput;
    private Spinner roleSpinner;
    private RecyclerView usersRecyclerView;
    private SearchView searchView;
    private dbconnect db;
    private List<String[]> usersList; // Stores [username, role] pairs
    private List<String[]> allUsersList; // Stores all users for filtering
    private UserAdapter userAdapter;
    ArrayAdapter<String> listAdapter; // Renamed to avoid confusion



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        Log.d("AdminActivity", "Activity started");
//logout button menu
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Button logoutButton = findViewById(R.id.loginButton);
        logoutButton.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
        });
        try {
            // Initialize views
            usernameInput = findViewById(R.id.username_input);
            passwordInput = findViewById(R.id.password_input);
            roleSpinner = findViewById(R.id.role_spinner);
            usersRecyclerView = findViewById(R.id.users_list);
            searchView = findViewById(R.id.search_view);

            // Initialize database
            db = new dbconnect(this);

            // Setup RecyclerView
            allUsersList = new ArrayList<>();
            usersList = new ArrayList<>();
            userAdapter = new UserAdapter(usersList);
            usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            usersRecyclerView.setAdapter(userAdapter);

            // Setup Spinner
            ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                    R.array.roles, android.R.layout.simple_spinner_item);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            roleSpinner.setAdapter(spinnerAdapter);

            // Setup SearchView
            setupSearchView();

            // Load users
            loadUsers();
        } catch (Exception e) {
            Log.e("AdminActivity", "Initialization error", e);
            Toast.makeText(this, "Initialization failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.loginButton) {
            startActivity(new Intent(this, LoginActivity.class));
            finish(); // Optional: close current activity
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterUsers(newText);
                return true;
            }
        });
    }

    private void filterUsers(String query) {
        usersList.clear();
        if (query.isEmpty()) {
            usersList.addAll(allUsersList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (String[] user : allUsersList) {
                if (user[0].toLowerCase().contains(lowerCaseQuery) ||
                        user[1].toLowerCase().contains(lowerCaseQuery)) {
                    usersList.add(user);
                }
            }
        }
        userAdapter.notifyDataSetChanged();
    }

    public void addUser(View view) {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String role = roleSpinner.getSelectedItem().toString();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add to Firestore
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("password", hashPassword(password));
        user.put("role", role);

        firestore.collection("users").document(username)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "User added successfully", Toast.LENGTH_SHORT).show();
                    clearInputs();
                    loadUsers();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to add user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("AdminActivity", "Add user error", e);
                });
    }

    private void loadUsers() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allUsersList.clear();
                    usersList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String username = doc.getString("username");
                        String role = doc.getString("role");
                        if (username != null && role != null) {
                            String[] userEntry = new String[]{username, role};
                            allUsersList.add(userEntry);
                            usersList.add(userEntry);
                        }
                    }
                    userAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load users", Toast.LENGTH_SHORT).show();
                    Log.e("AdminActivity", "Load users error", e);
                });
    }

    private void clearInputs() {
        usernameInput.setText("");
        passwordInput.setText("");
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

    private class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
        private List<String[]> users;

        UserAdapter(List<String[]> users) {
            this.users = users;
        }

        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_user, parent, false);
            return new UserViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
            String[] user = users.get(position);
            holder.nameView.setText(user[0]); // Username
            holder.roleView.setText(user[1]); // Role

            // Style the role based on its value
            int bgResource = R.drawable.role_bg; // Default background
            if (user[1].equalsIgnoreCase("admin")) {
                bgResource = R.drawable.role_admin_bg;
            } else if (user[1].equalsIgnoreCase("user")) {
                bgResource = R.drawable.role_user_bg;
            }
            holder.roleView.setBackgroundResource(bgResource);
        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        class UserViewHolder extends RecyclerView.ViewHolder {
            TextView nameView;
            TextView roleView;

            UserViewHolder(View itemView) {
                super(itemView);
                nameView = itemView.findViewById(R.id.user_name);
                roleView = itemView.findViewById(R.id.user_role);
            }
        }
    }
}