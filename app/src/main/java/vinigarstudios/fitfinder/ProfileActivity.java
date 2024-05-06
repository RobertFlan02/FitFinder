package vinigarstudios.fitfinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private ImageView profileImage;
    private EditText editTextUsername;
    private TextView textViewFollowerCount;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String currentUserUID;
    private Uri imageUri;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileImage = findViewById(R.id.profile_image);
        editTextUsername = findViewById(R.id.editTextUsername);
        textViewFollowerCount = findViewById(R.id.textViewFollowerCount);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUserUID = mAuth.getCurrentUser().getUid();
        storageRef = FirebaseStorage.getInstance().getReference().child("profileImages");

        // Load the current user data from Firestore and display it in the appropriate views
        loadUserData();

        // Load the profile image from Firebase Storage
        loadProfileImage();

        // Set onClickListener for profileImage to allow selecting a new image
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser();
            }
        });

        // Set onClickListener for the upload photo button
        Button uploadPhotoButton = findViewById(R.id.upload_photo_button);
        uploadPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser();
            }
        });

        // Set onClickListener for the submit button
        Button submitButton = findViewById(R.id.submit_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newUsername = editTextUsername.getText().toString().trim();
                updateUsername(newUsername);
                if (imageUri != null) {
                    uploadProfilePhoto();
                }
            }
        });
    }

    // Method to load the current username and follower count from Firestore and display them in the appropriate views
    private void loadUserData() {
        DocumentReference docRef = db.collection("profiles").document(currentUserUID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String username = document.getString("profileName");
                        long followerCount = document.getLong("followerCount");

                        // Update the username in EditText
                        editTextUsername.setText(username);
                        // Update the follower count in TextView
                        textViewFollowerCount.setText("Followers: " + followerCount);
                    } else {
                        Toast.makeText(ProfileActivity.this, "No such document", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed with: " + task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Method to update the username in Firestore
    private void updateUsername(String newUsername) {
        DocumentReference docRef = db.collection("profiles").document(currentUserUID);
        docRef.update("profileName", newUsername)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ProfileActivity.this, "Username updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ProfileActivity.this, "Failed to update username", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Called when the user selects an image from the gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // Get the selected image URI and set it to the ImageView
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                profileImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Method to open the image chooser (camera roll)
    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    // Method to load the profile image from Firebase Storage and display it in the ImageView
    private void loadProfileImage() {
        StorageReference photoRef = storageRef.child(currentUserUID + ".jpg");
        photoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Load the image into the profileImage ImageView
                Glide.with(ProfileActivity.this)
                        .load(uri)
                        .placeholder(R.drawable.greyicon) // Placeholder image
                        .error(R.drawable.greyicon) // Error image in case of failure
                        .into(profileImage);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle failure to load image by displaying default image
                profileImage.setImageResource(R.drawable.greyicon);
            }
        });
    }

    // Method to upload the selected profile photo to Firebase Storage
    private void uploadProfilePhoto() {
        StorageReference photoRef = storageRef.child(currentUserUID + ".jpg");
        photoRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get the download URL of the uploaded image
                        photoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                // Update profileImageURL field in Firestore with the download URL
                                updateProfileImageURL(uri.toString());
                                Toast.makeText(ProfileActivity.this, "Photo uploaded successfully", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProfileActivity.this, "Failed to upload photo", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Method to update the profileImageURL field in Firestore with the download URL
    private void updateProfileImageURL(String imageURL) {
        db.collection("profiles").document(currentUserUID)
                .update("profileImageURL", imageURL)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ProfileActivity.this, "Profile image URL updated successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProfileActivity.this, "Failed to update profile image URL", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
