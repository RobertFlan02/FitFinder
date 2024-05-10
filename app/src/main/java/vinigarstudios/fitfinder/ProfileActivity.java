package vinigarstudios.fitfinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import vinigarstudios.fitfinder.adapter.PostAdapter;
import vinigarstudios.fitfinder.loginregistration.Login;
import vinigarstudios.fitfinder.models.PostsModel;
import vinigarstudios.utility.VinigarCompatActivity;
import com.google.firebase.storage.UploadTask;

public class ProfileActivity extends VinigarCompatActivity {

    private static final int PICK_IMAGE = 1;
    private ImageView profileImage;
    private EditText editTextUsername;
    private TextView textViewFollowerCount;
    private String currentUserUID;
    private Uri imageUri;
    private StorageReference storageRef;
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<PostsModel> postsList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Pass FirebaseFirestore instance to PostAdapter constructor
        postAdapter = new PostAdapter(postsList, db);
        recyclerView.setAdapter(postAdapter);
        fetchPostsFromFirestore();


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
        db = FirebaseFirestore.getInstance();

        loadUserData();
        loadProfileImage();

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser();
            }
        });

        // Add onClick listener to the submit button
        Button submitButton = findViewById(R.id.submit_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newUsername = editTextUsername.getText().toString();
                updateUsername(newUsername);
            }
        });

        // Add onClick listener to the logout button
        Button logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });
    }

    // This method loads the current user's name and follower count from Firestore and displays them in the appropriate fields
    private void loadUserData() {
        db.collection("profiles").document(currentUserUID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        long followerCount = documentSnapshot.getLong("followerCount");

                        // Updates the username
                        editTextUsername.setText(username);
                        // Updates the follower count
                        textViewFollowerCount.setText("Friends: " + followerCount);
                    } else {
                        Toast.makeText(ProfileActivity.this, "No such document", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProfileActivity.this, "Failed with: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Used when the user uploads a new profile picture
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // Get the selected image URI and display it
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                profileImage.setImageBitmap(bitmap);
                // Call the method to upload the selected image to Firebase Storage
                uploadProfilePhoto();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Used to open the camera roll
    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
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
                // In case of error, display default image
                profileImage.setImageResource(R.drawable.greyicon);
            }
        });
    }

    // Method to update the username in the Firebase database
    private void updateUsername(String newUsername) {
        db.collection("profiles").document(currentUserUID)
                .update("username", newUsername)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Show notification for username update
                        Toast.makeText(ProfileActivity.this, "User details updated successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Show notification for username update failure
                        Toast.makeText(ProfileActivity.this, "Failed to update username", Toast.LENGTH_SHORT).show();
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
        db.collection("profiles").document(currentUserUID)
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

    private void showNotification(String message) {
        Toast.makeText(ProfileActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void fetchPostsFromFirestore() {
        db.collection("posts")
                .whereEqualTo("profileUID", mAuth.getCurrentUser().getUid()) // Filter by the UID of the current user
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    postsList = new ArrayList<>();
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        PostsModel post = documentSnapshot.toObject(PostsModel.class);
                        postsList.add(post);
                    }
                    postAdapter.setPostsList(postsList);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProfileActivity.this, "Failed to fetch posts", Toast.LENGTH_SHORT).show();
                });
    }
}