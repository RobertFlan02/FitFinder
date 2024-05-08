package vinigarstudios.fitfinder.search;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import vinigarstudios.fitfinder.R;
import vinigarstudios.fitfinder.models.UserModel;
import vinigarstudios.utility.AndroidHelper;
import vinigarstudios.utility.VinigarCompatActivity;

public class OtherProfileActivity extends VinigarCompatActivity
{
    private UserModel otherUser;
    private TextView username;
    private ImageView profileImage;
    private TextView followerCount;

    private Button addFriendButton;
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
    }
}
