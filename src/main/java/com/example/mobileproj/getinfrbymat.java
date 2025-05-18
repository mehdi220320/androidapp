package com.example.mobileproj;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class getinfrbymat extends AppCompatActivity {
    private EditText part11, part22;
    private Button searchButton;
    private ListView infractionsListView;
    private dbconnect db;
    private infadapter adapter;
    private List<Map<String, Object>> dataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        db = new dbconnect(this);
        searchButton = findViewById(R.id.search_button);
        part11 = findViewById(R.id.part11_input);
        part22 = findViewById(R.id.part22_input);
        infractionsListView = findViewById(R.id.infractions_list);

        dataList = new ArrayList<>();
        adapter = new infadapter(this, dataList);
        infractionsListView.setAdapter(adapter);

        searchButton.setOnClickListener(v -> searchInfractions2());
    }
    private void searchInfractions2() {
        String searchTerm = part11.getText().toString().trim() + part22.getText().toString().trim();
        if (searchTerm.isEmpty()) {
            Toast.makeText(this, "Enter serial number", Toast.LENGTH_SHORT).show();
            return;
        }

        dataList.clear();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("infractions")
                .whereEqualTo("numserie", searchTerm)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Map<String, Object> map = new HashMap<>();

                            // Assuming your image is stored as base64 string in Firestore
                            String base64Image = document.getString("photo");
                            byte[] imageBytes = Base64.decode(base64Image, Base64.DEFAULT);

                            map.put("image", imageBytes);
                            map.put("numserie", "Serial: " + document.getString("numserie"));
                            map.put("date", "Date: " + document.getString("date"));
                            map.put("location", "Location: " + document.getString("location"));

                            dataList.add(map);
                        }

                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "No infraction found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching data", Toast.LENGTH_SHORT).show();
                    Log.e("FIREBASE", "Error: ", e);
                });
    }
    private void searchInfractions() {
        String searchTerm = part11.getText().toString().trim() + part22.getText().toString().trim();
        if (searchTerm.isEmpty()) {
            Toast.makeText(this, "Enter serial number", Toast.LENGTH_SHORT).show();
            return;
        }

        dataList.clear();
        SQLiteDatabase database = db.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = database.query("infractions",
                    new String[]{"photo", "numserie", "date", "location"},
                    "numserie=?",
                    new String[]{searchTerm},
                    null, null, "date DESC");

            if (cursor.moveToFirst()) {
                do {
                    byte[] imageBytes = cursor.getBlob(0);

                    Map<String, Object> map = new HashMap<>();
                    map.put("image", imageBytes);
                    map.put("numserie", "Serial: " + cursor.getString(1));
                    map.put("date", "Date: " + cursor.getString(2));
                    map.put("location", "Location: " + cursor.getString(3));

                    dataList.add(map);
                } while (cursor.moveToNext());

                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "No infraction found", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Search error", Toast.LENGTH_SHORT).show();
            Log.e("SEARCH", "Error: ", e);
        } finally {
            if (cursor != null) cursor.close();
            database.close();
        }
    }
}