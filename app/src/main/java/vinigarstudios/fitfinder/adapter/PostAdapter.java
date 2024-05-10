package vinigarstudios.fitfinder.adapter;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.installations.remote.TokenResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import vinigarstudios.fitfinder.models.PostsModel;
import vinigarstudios.fitfinder.models.UserModel;
import vinigarstudios.fitfinder.R;
import vinigarstudios.utility.FirebaseHelper;
import vinigarstudios.fitfinder.notifications.FCMNotificationSender;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<PostsModel> postsList;
    private static FirebaseFirestore db;
    private static final String TAG = "MyFirebaseMsgService";

    public PostAdapter(List<PostsModel> postsList, FirebaseFirestore db) {
        this.postsList = postsList;
        this.db = db;
    }


    public PostAdapter(List<PostsModel> postsList) {
        this.postsList = postsList;
        this.db = FirebaseFirestore.getInstance();
    }

    public void setPostsList(List<PostsModel> postsList) {
        this.postsList = postsList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        PostsModel post = postsList.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return postsList == null ? 0 : postsList.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {

        private TextView titleTextView;
        private TextView captionTextView;
        private TextView likesTextView;
        private ImageView postImageView;
        private TextView timestampTextView;
        private ImageView profileImageView;
        private TextView usernameTextView;
        private Button likeButton;
        private String token;
        private String msg;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            captionTextView = itemView.findViewById(R.id.captionTextView);
            likesTextView = itemView.findViewById(R.id.likesTextView);
            postImageView = itemView.findViewById(R.id.postImageView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
            profileImageView = itemView.findViewById(R.id.profileImageView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            likeButton = itemView.findViewById(R.id.likeButton);
        }

        public void bind(PostsModel post) {
            titleTextView.setText(post.getTitle());
            captionTextView.setText(post.getCaption());
            likesTextView.setText(String.valueOf(post.getLikes()));
            loadImage(post.getPhotoURL()); // Load image from URL
            displayTimestamp(post.getTimestamp()); // Display formatted timestamp

            likeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Token retrieval successful
                            token = task.getResult();
                            msg = "Your FCM token: " + token; // Direct concatenation
                            Log.d(TAG, msg);
                        } else {
                            // Token retrieval failed
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        }
                    });
                    if(!post.getUserIDsWhoLiked().contains(FirebaseHelper.GetCurrentUserId()))
                    {
                        FCMNotificationSender.sendFCMLikeNotification(token);
                        post.setLikes(post.getLikes() + 1);
                        PostsModel newPost = post;
                        likesTextView.setText(String.valueOf(post.getLikes()));
                        post.getUserIDsWhoLiked().add(FirebaseHelper.GetCurrentUserId());
                        FirebaseHelper.UpdateModelInDatabase("posts", post, newPost);
                        likeButton.setBackgroundColor(Color.rgb(1, 0, 1));
                    }
                    else if (post.getUserIDsWhoLiked().contains(FirebaseHelper.GetCurrentUserId()))
                    {
                        FCMNotificationSender.sendFCMDislikeNotification(token);
                        post.setLikes(post.getLikes() - 1);
                        PostsModel newPost = post;
                        likesTextView.setText(String.valueOf(post.getLikes()));
                        post.getUserIDsWhoLiked().remove(FirebaseHelper.GetCurrentUserId());
                        FirebaseHelper.UpdateModelInDatabase("posts", post, newPost);
                        likeButton.setBackgroundColor(Color.rgb(0, 1, 0));
                    }
                }
            });

            // Load profile image and username
            String profileUID = post.getProfileUID();
            db.collection("profiles").document(profileUID).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    UserModel userModel = documentSnapshot.toObject(UserModel.class);
                    Glide.with(itemView.getContext())
                            .load(userModel.getProfileImageURL()) // Get profile image URL from UserModel
                            .placeholder(R.drawable.greyicon) // Placeholder image
                            .error(R.drawable.greyicon) // Error image
                            .into(profileImageView);
                    usernameTextView.setText(userModel.getUsername()); // Get username from UserModel
                } else {
                    profileImageView.setImageResource(R.drawable.greyicon);
                    usernameTextView.setText("Unknown User");
                }
            }).addOnFailureListener(e -> {
                // Handle failure to retrieve user data
                profileImageView.setImageResource(R.drawable.greyicon);
                usernameTextView.setText("Unknown User");
            });
        }

        private void loadImage(String photoURL) {
            // Use Glide or Picasso to load image from URL into ImageView
            Glide.with(itemView.getContext())
                    .load(photoURL)
                    .into(postImageView);
        }

        private void displayTimestamp(Timestamp timestamp) {
            if (timestamp != null) {
                // Format timestamp as desired (e.g., convert to date and time string)
                // Example:
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String formattedTimestamp = sdf.format(timestamp.toDate());
                timestampTextView.setText(formattedTimestamp);
            } else {
                timestampTextView.setText("No timestamp available");
            }
        }
    }
}