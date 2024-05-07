package vinigarstudios.fitfinder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import vinigarstudios.fitfinder.loginregistration.Login;
import vinigarstudios.fitfinder.search.Search;
import vinigarstudios.utility.VinigarCompatActivity;

public class UploadActivity extends VinigarCompatActivity {

    private Button uploadButton;
    private ImageView imageView;
    private StorageReference storageRef;
    private FirebaseAuth mAuth;
    private FirebaseFirestore database;

    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        // Initialize Firebase components
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        // Find the uploadButton by its ID
        uploadButton = findViewById(R.id.uploadButton);

        // Set an OnClickListener to the uploadButton
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // When the button is clicked, open image chooser
                openImageChooser();
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.bottom_upload);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.bottom_home) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (item.getItemId() == R.id.bottom_friends) {
                startActivity(new Intent(getApplicationContext(), FriendsActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (item.getItemId() == R.id.bottom_profile) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else return item.getItemId() == R.id.bottom_upload;
        });

        // Other initialization code...
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                Uri selectedImageUri = data.getData();
                uploadImageToFirebaseStorage(selectedImageUri);
            }
        } else {
            Toast.makeText(this, "Action cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImageToFirebaseStorage(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            // Storage reference
            String uid = mAuth.getCurrentUser().getUid(); // Get current user's UID
            StorageReference photoRef = storageRef.child(uid + "/" + imageUri.getLastPathSegment());

            UploadTask uploadTask = photoRef.putBytes(data);

            uploadTask.addOnSuccessListener(taskSnapshot -> {
                // Image uploaded successfully
                photoRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    // Image download URL retrieved successfully
                    storeImageUrlInFirestore(downloadUri.toString());
                }).addOnFailureListener(e -> {
                    // Failed to retrieve download URL
                    Toast.makeText(UploadActivity.this, "Failed to retrieve download URL", Toast.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(e -> {
                // Upload failed
                Toast.makeText(UploadActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show();
        }
    }

    private void storeImageUrlInFirestore(String imageUrl) {
        String uid = mAuth.getCurrentUser().getUid(); // Get current user's UID

        // Create a Photo object with image URL and user UID
        Photo photo = new Photo(imageUrl, uid);

        // Add photo to Firestore collection
        database.collection("photos")
                .add(photo)
                .addOnSuccessListener(documentReference -> {
                    // Image URL stored in Firestore successfully
                    Toast.makeText(UploadActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Failed to store image URL in Firestore
                    Toast.makeText(UploadActivity.this, "Failed to store image URL in Firestore", Toast.LENGTH_SHORT).show();
                });
    }
}
