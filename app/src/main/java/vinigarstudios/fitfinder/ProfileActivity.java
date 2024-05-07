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

import com.google.android.material.bottomnavigation.BottomNavigationView;

import vinigarstudios.utility.FirebaseHelper;
import vinigarstudios.utility.VinigarCompatActivity;

public class ProfileActivity extends VinigarCompatActivity {

    private static final int PICK_IMAGE = 1;
    private ImageView profileImage;
    private EditText editTextUsername;
    private TextView textViewFollowerCount;
    private String currentUserUID;
    private Uri imageUri;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.bottom_profile);
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
            } else if (item.getItemId() == R.id.bottom_upload) {
                startActivity(new Intent(getApplicationContext(), UploadActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else return item.getItemId() == R.id.bottom_profile;
        });

        profileImage = findViewById(R.id.profile_image);
        editTextUsername = findViewById(R.id.editTextUsername);
        textViewFollowerCount = findViewById(R.id.textViewFollowerCount);

        currentUserUID = mAuth.getCurrentUser().getUid();
        storageRef = FirebaseStorage.getInstance().getReference().child("profileImages");

        loadUserData();
        loadProfileImage();

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser();
            }
        });
    }

    // This method loads the current users name and follower count from firestore and displays them in the appropriate fields
    private void loadUserData() {
        DocumentReference docRef = database.collection("profiles").document(currentUserUID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String username = document.getString("profileName");
                        long followerCount = document.getLong("followerCount");

                        // Updates the username
                        editTextUsername.setText(username);
                        // Updates the follower count
                        textViewFollowerCount.setText("Followers: " + followerCount);
                        // error handling
                    } else {
                        Toast.makeText(ProfileActivity.this, "No such document", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed with: " + task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Used when the user uploads a new profile picture
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // Get the selected image URI and displays it
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                profileImage.setImageBitmap(bitmap);
                uploadProfilePhoto();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Used to open the camera roll
    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    // Method to load the profile image from Firebase and display it
    private void loadProfileImage() {
        StorageReference photoRef = storageRef.child(currentUserUID + ".jpg");
        photoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Loads the image
                Glide.with(ProfileActivity.this)
                        .load(uri)
                        .placeholder(R.drawable.greyicon) // Default image
                        .error(R.drawable.greyicon)
                        .into(profileImage);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // In case of error display default image
                profileImage.setImageResource(R.drawable.greyicon);
            }
        });
    }

    // Uploads the selected profile photo to Firebase
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
        database.collection("profiles").document(currentUserUID)
                .update("profileImageURL", imageURL)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Show single notification for user details update
                        showNotification("User details updated successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProfileActivity.this, "Failed to update profile image URL", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Method to show notification
    private void showNotification(String message) {
        Toast.makeText(ProfileActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
