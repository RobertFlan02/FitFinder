// This class represents the Upload Activity where users can upload images with titles and captions.
package vinigarstudios.fitfinder.activites;

// Importing necessary libraries and components.
import android.content.ContentValues;
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

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import vinigarstudios.fitfinder.R;
import vinigarstudios.fitfinder.models.PostsModel;
import vinigarstudios.fitfinder.notifications.FCMNotificationSender;
import vinigarstudios.utility.FirebaseHelper;
import vinigarstudios.utility.VinigarCompatActivity;

// The main class for the Upload Activity.
public class UploadActivity extends VinigarCompatActivity {

    // Constants for request codes.
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;

    // Image and UI related variables.
    private ImageView imageView;
    private Uri selectedImageUri;
    private EditText editTextTitle;
    private EditText editTextCaption;
    private StorageReference storageRef;
    private Bitmap imageBitmap;
    private boolean isUploadInProgress = false;

    // onCreate method to initialize the activity.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        // Initializing views and Firebase storage reference.
        imageView = findViewById(R.id.imageView_placeholder);
        storageRef = FirebaseStorage.getInstance().getReference();

        // Setting up onClickListeners for camera and upload buttons.
        Button cameraButton = findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
            }
        });

        Button uploadButton = findViewById(R.id.uploadButton);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser();
            }
        });

        // Initializing other UI elements.
        editTextTitle = findViewById(R.id.editText_title);
        editTextCaption = findViewById(R.id.editText_caption);

        Button buttonCreatePost = findViewById(R.id.button_create_post);
        buttonCreatePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isUploadInProgress) {
                    isUploadInProgress = true;
                    uploadImageAndPostData();
                } else {
                    Toast.makeText(UploadActivity.this, "Upload process is already in progress", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Setting up bottom navigation.
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
    }

    // Method to open the camera for capturing images.
    public void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Image");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Captured by User");
        // Returns URI that points to new image and specifies the image to be stored in external storage
        selectedImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        // Intent to use camera hardware for capture
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Adds our image data to the intent
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, selectedImageUri);
        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
    }

    // Method to open the image chooser for selecting images from gallery.
    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_IMAGE_PICK);
    }

    // Handling the result of camera capture or image selection.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // If user is taking a photo
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                if (selectedImageUri != null) {
                    imageView.setImageURI(selectedImageUri);
                } else {
                    Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show();
                }
                // If user is selecting a photo from gallery
            } else if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                selectedImageUri = data.getData();
                imageView.setImageURI(selectedImageUri);
            }
        } else {
            Toast.makeText(this, "Action cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to save the captured or selected image to the gallery.
    private Uri saveImageToGallery() {
        if (imageBitmap == null) {
            Toast.makeText(this, "Image bitmap is null", Toast.LENGTH_SHORT).show();
            return null;
        }

        // Defining values for the new image file.
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_" + System.currentTimeMillis() + ".jpg");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

        // Inserting the new image file into the MediaStore.
        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri == null) {
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
            return null;
        }

        // Writing the image bitmap to the OutputStream.
        try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
            if (outputStream == null) {
                throw new IOException("OutputStream is null");
            }
            boolean success = imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            if (!success) {
                throw new IOException("Failed to compress bitmap");
            }
            outputStream.flush();
            Toast.makeText(this, "Image saved to gallery", Toast.LENGTH_SHORT).show();
            return uri;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    // Method to upload the image and post data to Firebase.
    private void uploadImageAndPostData() {
        // Getting title and caption from EditText fields.
        String title = editTextTitle.getText().toString();
        String caption = editTextCaption.getText().toString();

        // Validating input fields.
        if (title.isEmpty() || caption.isEmpty()) {
            Toast.makeText(this, "Title and caption cannot be empty", Toast.LENGTH_SHORT).show();
            isUploadInProgress = false;
            return;
        }
        if (title.length() > 60) {
            Toast.makeText(this, "Title cannot exceed 60 characters", Toast.LENGTH_SHORT).show();
            isUploadInProgress = false;
            return;
        }
        if (caption.length() > 150) {
            Toast.makeText(this, "Caption cannot exceed 150 characters", Toast.LENGTH_SHORT).show();
            isUploadInProgress = false;
            return;
        }
        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            isUploadInProgress = false;
            return;
        }

        // Uploading image to Firebase Storage.
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            String uid = mAuth.getCurrentUser().getUid();
            StorageReference imagesRef = storageRef.child("uploadedImages");
            StorageReference photoRef = imagesRef.child(uid + "/" + selectedImageUri.getLastPathSegment());

            UploadTask uploadTask = photoRef.putBytes(data);

            uploadTask.addOnSuccessListener(taskSnapshot -> {
                photoRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    storeImageDataInFirestore(downloadUri.toString(), title, caption);
                    // Sending notification after storing image data in Firestore.
                    sendNotification();
                }).addOnFailureListener(e -> {
                    Toast.makeText(UploadActivity.this, "Failed to retrieve download URL", Toast.LENGTH_SHORT).show();
                    isUploadInProgress = false;
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(UploadActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                isUploadInProgress = false;
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show();
            isUploadInProgress = false;
        }
    }

    // Method to send a notification.
    private void sendNotification() {
        FCMNotificationSender.sendFCMPostCreatedNotification();
    }

    // Method to store image data in Firestore.
    private void storeImageDataInFirestore(String imageUrl, String title, String caption) {
        String uid = mAuth.getCurrentUser().getUid();
        PostsModel post = new PostsModel(imageUrl, uid, title, caption, 0);
        FirebaseHelper.UploadModelToDatabase("posts", post, post.getDocumentId());
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }
}