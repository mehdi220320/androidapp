package com.example.mobileproj;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;


public class UserActivity  extends AppCompatActivity {
    private static final int CAMERA_REQUEST = 100;
    private ImageView imageView;
    private EditText numserieInput, locationInput, part1, part2;
    private Button captureButton, submitButton;
    private dbconnect db;
    private byte[] photoBytes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        db = new dbconnect(this);
        SQLiteDatabase database = db.getWritableDatabase();

        Cursor cursor = database.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name='infractions'", null);
        if (!cursor.moveToFirst()) {
            database.execSQL("CREATE TABLE IF NOT EXISTS infractions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "photo BLOB," +
                    "numserie TEXT," +
                    "date TEXT," +
                    "location TEXT)");
        }

        imageView = findViewById(R.id.image_view);
         part1 = findViewById(R.id.part1_input);
         part2 = findViewById(R.id.part2_input);
        locationInput = findViewById(R.id.location_input);
        captureButton = findViewById(R.id.capture_button);
        submitButton = findViewById(R.id.submit_button);

        captureButton.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(takePictureIntent, CAMERA_REQUEST);
        });

        submitButton.setOnClickListener(v -> saveInfractionToFirestore());  }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            Bitmap photoBitmap = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(photoBitmap);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            photoBitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
            photoBytes = stream.toByteArray();
        }
    }

    private void saveInfraction() {
        String numserie = part1.getText().toString().trim()+part2.getText().toString().trim();
        String location = locationInput.getText().toString().trim();

        if (photoBytes == null) {
            Toast.makeText(this, "Please take a photo first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (numserie.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase database = null;
        try {
            database = db.getWritableDatabase();
            database.beginTransaction();

            ContentValues values = new ContentValues();
            values.put("photo", photoBytes);
            values.put("numserie", numserie);
            values.put("date", new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date()));
            values.put("location", location);

            long result = database.insert("infractions", null, values);

            if (result != -1) {
                database.setTransactionSuccessful();
                Toast.makeText(this, "Infraction saved! ID: " + result, Toast.LENGTH_SHORT).show();
                resetForm();
            } else {
                Toast.makeText(this, "Failed to save infraction", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("DB_ERROR", "Save failed", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            try {
                if (database != null) {
                    if (database.inTransaction()) {
                        database.endTransaction();
                    }
                    database.close();
                }
            } catch (Exception e) {
                Log.e("DB_ERROR", "Error closing database", e);
            }
        }
    }

    private void saveInfractionToFirestore() {
        String numserie = part1.getText().toString().trim() + part2.getText().toString().trim();
        String location = locationInput.getText().toString().trim();

        if (photoBytes == null) {
            Toast.makeText(this, "Please take a photo first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (numserie.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Encode image to Base64
        String encodedPhoto = Base64.encodeToString(photoBytes, Base64.DEFAULT);
        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());

        // Prepare Firestore document
        Map<String, Object> infraction = new HashMap<>();
        infraction.put("numserie", numserie);
        infraction.put("location", location);
        infraction.put("date", currentDate);
        infraction.put("photo", encodedPhoto);

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        String documentId = UUID.randomUUID().toString(); // Optional: or use firestore.collection("infractions").add()

        firestore.collection("infractions")
                .document(documentId)
                .set(infraction)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Infraction saved to Firestore! ID: " + documentId, Toast.LENGTH_SHORT).show();
                    resetForm(); // Your existing method
                })
                .addOnFailureListener(e -> {
                    Log.e("FIRESTORE_ERROR", "Failed to save infraction", e);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void resetForm() {
        imageView.setImageResource(android.R.color.transparent);
        part1.setText(""); // reset both parts
        part2.setText("");
        locationInput.setText("");
        photoBytes = null;
    }
}