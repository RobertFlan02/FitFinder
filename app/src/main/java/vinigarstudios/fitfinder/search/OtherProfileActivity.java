package vinigarstudios.fitfinder.search;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;

import vinigarstudios.fitfinder.FriendsActivity;
import vinigarstudios.fitfinder.MainActivity;
import vinigarstudios.fitfinder.ProfileActivity;
import vinigarstudios.fitfinder.R;
import vinigarstudios.fitfinder.UploadActivity;
import vinigarstudios.fitfinder.models.UserModel;
import vinigarstudios.utility.AndroidHelper;
import vinigarstudios.utility.FirebaseHelper;
import vinigarstudios.utility.FriendRequestHandler;
import vinigarstudios.utility.VinigarCompatActivity;

public class OtherProfileActivity extends VinigarCompatActivity
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
