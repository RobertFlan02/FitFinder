package vinigarstudios.fitfinder;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import vinigarstudios.fitfinder.models.PostsModel;
import vinigarstudios.utility.FirebaseHelper;
import vinigarstudios.utility.VinigarCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


public class UploadActivity extends VinigarCompatActivity {

    private Button uploadButton;
    private ImageView imageView;
    private StorageReference storageRef;
    private Uri selectedImageUri;
    private EditText editTextTitle;
    private EditText editTextCaption;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 2;
    private Button cameraButton;

    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        cameraButton = findViewById(R.id.cameraButton);

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        imageView = findViewById(R.id.imageView_placeholder);

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

        // Find the EditText fields by their IDs
        editTextTitle = findViewById(R.id.editText_title);
        editTextCaption = findViewById(R.id.editText_caption);

        // Find the create post button by its ID
        Button buttonCreatePost = findViewById(R.id.button_create_post);

        // Set an OnClickListener to the create post button
        buttonCreatePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // When the button is clicked, upload image and post data to Firestore
                uploadImageAndPostData();
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

    private void dispatchTakePictureIntent() {
        // Check for camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            // Permission has already been granted
            Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start the camera intent
                dispatchTakePictureIntent();
            } else {
                // Permission denied
                // Handle the denial gracefully
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
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
                // Selected image from gallery
                selectedImageUri = data.getData();
                // Set the selected image to the ImageView
                imageView.setImageURI(selectedImageUri);
            } else if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
                // Captured image from camera
                // Here you can handle the captured image
                // For example, you can display it in an ImageView
                // imageView.setImageBitmap((Bitmap) data.getExtras().get("data"));
            }
        } else {
            Toast.makeText(this, "Action cancelled", Toast.LENGTH_SHORT).show();
        }
    }


    private void uploadImageAndPostData() {
        // Retrieve the text from title and caption fields
        String title = editTextTitle.getText().toString();
        String caption = editTextCaption.getText().toString();

        // Check if title and caption are not empty
        if (title.isEmpty() || caption.isEmpty()) {
            Toast.makeText(this, "Title and caption cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (title.length() > 60) {
            Toast.makeText(this, "Title cannot exceed 60 characters", Toast.LENGTH_SHORT).show();
            return;
        }
        if (caption.length() > 150) {
            Toast.makeText(this, "Caption cannot exceed 150 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if an image is selected
        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            // Storage reference
            String uid = mAuth.getCurrentUser().getUid(); // Get current user's UID
            StorageReference imagesRef = storageRef.child("uploadedImages"); // Reference to "uploadedImages" folder
            StorageReference photoRef = imagesRef.child(uid + "/" + selectedImageUri.getLastPathSegment());

            UploadTask uploadTask = photoRef.putBytes(data);

            uploadTask.addOnSuccessListener(taskSnapshot -> {
                // Image uploaded successfully
                photoRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    // Image download URL retrieved successfully
                    storeImageDataInFirestore(downloadUri.toString(), title, caption);
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

    private void storeImageDataInFirestore(String imageUrl, String title, String caption) {
        String uid = mAuth.getCurrentUser().getUid(); // Get current user's UID

        // Create a Posts object with image URL, title, caption, user UID, and timestamp
        PostsModel post = new PostsModel(imageUrl, uid, title, caption, 0);

        // Add post to Firestore collection
        FirebaseHelper.UploadModelToDatabase("posts", post, post.getDocumentId());
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }
}