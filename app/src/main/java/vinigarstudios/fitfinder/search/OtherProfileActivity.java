package vinigarstudios.fitfinder.search;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

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
    private UserModel otherUser;
    private TextView username;
    private ImageView profileImage;
    private TextView followerCount;
    private Button addFriendButton;
    private BottomNavigationView bottomNavigationView;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.otherprofile);

        otherUser = AndroidHelper.GetUserModelFromIntent(getIntent());
        username = findViewById(R.id.otherProfileUsernameText);
        profileImage = findViewById(R.id.otherProfileImage);
        followerCount = findViewById(R.id.otherProfileFollowerCount);
        addFriendButton = findViewById(R.id.otherProfileAddFriendButton);

        username.setText(otherUser.getUsername());
        followerCount.setText("Followers: " + otherUser.getFollowerCount());

        this.addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FriendRequestHandler.SendFriendRequest(FirebaseHelper.GetCurrentUserId(), otherUser))
                {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                }
                else
                {
                    //Friend request already sent
                    Toast.makeText(getApplicationContext(), "Friend Request Already Sent", Toast.LENGTH_SHORT).show();
                }
            }
        });

        this.bottomNavigationView = findViewById(R.id.bottomNavigation);

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
}
