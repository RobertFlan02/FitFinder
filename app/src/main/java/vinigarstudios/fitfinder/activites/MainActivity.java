package vinigarstudios.fitfinder;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

import vinigarstudios.fitfinder.adapter.PostAdapter;
import vinigarstudios.fitfinder.models.PostsModel;
import vinigarstudios.fitfinder.models.UserModel;
import vinigarstudios.utility.AndroidHelper;
import vinigarstudios.utility.FirebaseHelper;
import vinigarstudios.utility.FriendRequestHandler;
import vinigarstudios.utility.VinigarCompatActivity;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<PostsModel> postsList;
    private FirebaseFirestore db;

    private static final int ORDER_BY_TIME = 0;
    private static final int ORDER_BY_LIKES = 1;

    private int currentOrder = ORDER_BY_TIME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Request permission to send notifications to user
        askNotificationPermission();

        FirebaseMessaging.getInstance().subscribeToTopic("web_app")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Done";
                        if (!task.isSuccessful()) {
                            msg = "Failed";
                        }

                    }
                });

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize PostAdapter
        postAdapter = new PostAdapter(this, postsList, db);
        recyclerView.setAdapter(postAdapter);

        // Fetch and display posts from Firestore
        fetchPostsFromFirestore();

        // Set up bottom navigation
        setupBottomNavigation();

    }

    // Required to activate requestPermissionLauncher
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Can post notifications.
                } else {
                }
            });

    // Prompt to ask users for notifications permissions
    private void askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU) i.e. Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                // Can post notifications.
            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Notification Permission")
                        .setMessage("Please Allow Notification Permission")
                        .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .setNegativeButton("Don't Allow", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .create()
                        .show();
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_order_by_time) {
            currentOrder = ORDER_BY_TIME;
            fetchPostsFromFirestore();
            return true;
        } else if (itemId == R.id.action_order_by_likes) {
            currentOrder = ORDER_BY_LIKES;
            fetchPostsFromFirestore();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void fetchPostsFromFirestore() {
        Query query;
        if (currentOrder == ORDER_BY_TIME) {
            query = db.collection("posts").orderBy("timestamp", Query.Direction.DESCENDING);
        } else {
            query = db.collection("posts").orderBy("likes", Query.Direction.DESCENDING);
        }

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    postsList = new ArrayList<>();
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        PostsModel post = documentSnapshot.toObject(PostsModel.class);
                        postsList.add(post);
                    }
                    postAdapter.setPostsList(postsList);
                })
                .addOnFailureListener(e -> {
                    // Handle error
                    Toast.makeText(MainActivity.this, "Failed to fetch posts", Toast.LENGTH_SHORT).show();
                });
    }

    // Bottom Navigation Bar
    private void setupBottomNavigation() {
        // Locates the navbar in the activity_main.xml file
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        // Sets the navbar to display home as selected option
        bottomNavigationView.setSelectedItemId(R.id.bottom_home);
        // Check which button is clicked on navbar
        bottomNavigationView.setOnItemSelectedListener(item -> {
            // On home already, do nothing
            if (item.getItemId() == R.id.bottom_home) {
                return true;
                // If user selects friends button, open Friends Activity
            } else if (item.getItemId() == R.id.bottom_friends) {
                startActivity(new Intent(getApplicationContext(), FriendsActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
                // If user selects posts button, open Upload Activity
            } else if (item.getItemId() == R.id.bottom_upload) {
                startActivity(new Intent(getApplicationContext(), UploadActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
                // If user selects profile button, open Profile Activity
            } else if (item.getItemId() == R.id.bottom_profile) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            }
            return false;
        });
    }

    public static class OtherProfileActivity extends VinigarCompatActivity
    {
        private UserModel currentUser;
        private UserModel otherUser;
        private TextView username;
        private ImageView profileImage;
        private TextView followerCount;
        private Button addFriendButton;

        private Button removeFriendButton;
        private Button acceptFriendReqButton;
        private Button declineFriendReqButton;
        private BottomNavigationView bottomNavigationView;
        @SuppressLint("SetTextI18n")
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            this.setContentView(R.layout.otherprofile);




            FirebaseHelper.GetCurrentUserDetails().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    currentUser = task.getResult().toObject(UserModel.class);
                    HandleButtonVisibility();
                }
            });

            //Initialising Variables.
            otherUser = AndroidHelper.GetUserModelFromIntent(getIntent());
            username = findViewById(R.id.otherProfileUsernameText);
            profileImage = findViewById(R.id.otherProfileImage);
            followerCount = findViewById(R.id.otherProfileFollowerCount);
            addFriendButton = findViewById(R.id.otherProfileAddFriendButton);
            removeFriendButton = findViewById(R.id.otherProfileRemoveFriendButton);
            acceptFriendReqButton = findViewById(R.id.otherProfileAcceptFriendButton);
            declineFriendReqButton = findViewById(R.id.otherProfileDeclineFriendButton);

            username.setText(otherUser.getUsername());
            followerCount.setText("Friends: " + otherUser.getFollowerCount());

            this.ButtonsFunctionality();

            this.bottomNavigationView = findViewById(R.id.bottomNavigation);

            Glide.with(this)
                    .load(otherUser.getProfileImageURL())
                    .placeholder(R.drawable.greyicon) // Placeholder image while loading
                    .error(R.drawable.greyicon) // Error image if loading fails
                    .into(profileImage);

            //Copy paste but under time pressure (its 5:30 am).
            bottomNavigationView.setSelectedItemId(R.id.bottom_friends);
            bottomNavigationView.setOnItemSelectedListener(item -> {
                if (item.getItemId() == R.id.bottom_home) {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                    return true;
                } else if (item.getItemId() == R.id.bottom_friends) {
                    return true;
                } else if (item.getItemId() == R.id.bottom_upload) {
                    startActivity(new Intent(getApplicationContext(), UploadActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                    return true;
                } else if (item.getItemId() == R.id.bottom_profile) {
                    startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                    return true;
                }
                return false;
            });
        }


        /**
         * Handles the button visibility in MainActivity.
         */
        private void HandleButtonVisibility() {
            if (currentUser.getFriendsId().stream().anyMatch(f -> f.startsWith(otherUser.getUserId()))) //if user is friends with this person
            {
                addFriendButton.setVisibility(View.INVISIBLE);
                declineFriendReqButton.setVisibility(View.INVISIBLE);
                acceptFriendReqButton.setVisibility(View.INVISIBLE);
                removeFriendButton.setVisibility(View.VISIBLE);
            }
            else if (currentUser.getFriendRequestsFromUserIdList().stream().anyMatch(f -> f.startsWith(otherUser.getUserId()))) //if user has friendreq from this person
            {
                addFriendButton.setVisibility(View.INVISIBLE);
                declineFriendReqButton.setVisibility(View.VISIBLE);
                acceptFriendReqButton.setVisibility(View.VISIBLE);
                removeFriendButton.setVisibility(View.INVISIBLE);
            }
            else
            {
                addFriendButton.setVisibility(View.VISIBLE);
                declineFriendReqButton.setVisibility(View.INVISIBLE);
                acceptFriendReqButton.setVisibility(View.INVISIBLE);
                removeFriendButton.setVisibility(View.INVISIBLE);
            }
        }

        /**
         * How all the different profile buttons work -> Add, Remove, AcceptRequest, DeclineRequest.
         */
        private void ButtonsFunctionality()
        {
            this.addFriendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (FriendRequestHandler.SendFriendRequest(FirebaseHelper.GetCurrentUserId(), otherUser))
                    {
                        Toast.makeText(getApplicationContext(), "Friend Request Sent to " + otherUser.getUsername() + "!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), FriendsActivity.class));
                        finish();
                    }
                    else
                    {
                        //Friend request already sent
                        Toast.makeText(getApplicationContext(), "Friend Request Already Sent", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            //Self Explanatory
            this.removeFriendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try
                    {
                        currentUser.RemoveUser(otherUser);
                        Toast.makeText(getApplicationContext(), "User '" + otherUser.getUsername() + "' is no longer your friend!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), FriendsActivity.class));
                        finish();
                    }
                    catch(Exception e)
                    {
                        Toast.makeText(getApplicationContext(), "Error deleting friend", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            //Self Explanatory
            this.acceptFriendReqButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try
                    {
                        currentUser.AcceptUser(otherUser);
                        Toast.makeText(getApplicationContext(), "User '" + otherUser.getUsername() + "' is now your friend!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), FriendsActivity.class));
                        finish();
                    }
                    catch(Exception e)
                    {
                        //Friend request already sent
                        Toast.makeText(getApplicationContext(), "Error accepting friend Request", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            //Self Explanatory
            this.declineFriendReqButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try
                    {
                        currentUser.DeclineUser(otherUser);
                        Toast.makeText(getApplicationContext(), "User '" + otherUser.getUsername() + "''s friend request is declined!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), FriendsActivity.class));
                        finish();
                    }
                    catch(Exception e)
                    {
                        //Friend request already sent
                        Toast.makeText(getApplicationContext(), "Error declining friend Request", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }
}

