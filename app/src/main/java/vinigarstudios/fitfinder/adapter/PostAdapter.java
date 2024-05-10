package vinigarstudios.fitfinder.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import vinigarstudios.fitfinder.MainActivity;
import vinigarstudios.fitfinder.ProfileActivity;
import vinigarstudios.fitfinder.R;
import vinigarstudios.fitfinder.models.PostsModel;
import vinigarstudios.fitfinder.models.UserModel;
import vinigarstudios.utility.AndroidHelper;
import vinigarstudios.utility.FirebaseHelper;
import vinigarstudios.fitfinder.notifications.FCMNotificationSender;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<PostsModel> postsList;
    private static FirebaseFirestore db;
    private Context context;
    private boolean refresh;

    public PostAdapter(Context context, List<PostsModel> postsList, FirebaseFirestore db) {
        this.postsList = postsList;
        this.db = db;
        this.context = context;
    }

    public PostAdapter(List<PostsModel> postsList, Context context) {
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
        onRemoveButtonClick(holder, post);

        onProfileImageClick(holder, post);

        holder.bind(post);
    }

    private void onRemoveButtonClick(@NonNull PostViewHolder holder, PostsModel post) {
        holder.getRemovePostButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseHelper.GetCurrentUserDetails().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        UserModel currentUser = task.getResult().toObject(UserModel.class);
                        currentUser.RemoveFromPostsList(post);
                        FirebaseHelper.RemoveModelInDatabase("posts", post);
                        postsList.remove(post);
                        notifyItemRemoved(postsList.indexOf(post));
                    }
                });
            }
        });
    }

    private void onProfileImageClick(@NonNull PostViewHolder holder, PostsModel post) {
        holder.getProfileImageView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseFirestore.getInstance().collection("profiles").document(post.getProfileUID()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        UserModel user = task.getResult().toObject(UserModel.class);
                        if (user.GetUserId().equals(FirebaseHelper.GetCurrentUserId()))
                        {
                            Intent intent = new Intent(context, ProfileActivity.class);
                            context.startActivity(intent);
                            return;
                        }
                        Intent intent = new Intent(context, MainActivity.OtherProfileActivity.class);
                        AndroidHelper.PassUserModelAsIntent(intent, user);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                });
            }
        });
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

        private Button removePostButton;
        private String posterToken;
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
            removePostButton = itemView.findViewById(R.id.removePostButton);
        }

        public void bind(PostsModel post) {
            titleTextView.setText(post.getTitle());
            captionTextView.setText(post.getCaption());
            likesTextView.setText(String.valueOf(post.getLikes()));
            loadImage(post.getPhotoURL()); // Load image from URL
            displayTimestamp(post.getTimestamp()); // Display formatted timestamp

            if (!post.getProfileUID().equals(FirebaseHelper.GetCurrentUserId()))
            {
                removePostButton.setVisibility(View.INVISIBLE);
            }

            // Set initial text based on whether the post is liked or not
            if (post.getUserIDsWhoLiked().contains(FirebaseHelper.GetCurrentUserId())) {
                likeButton.setText("❤️"); // Liked state
            } else {
                likeButton.setText("🤍"); // Unliked state
            }

            // Set OnClickListener to toggle like/unlike and update like display
            likeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (post.getUserIDsWhoLiked().contains(FirebaseHelper.GetCurrentUserId())) {
                        // If already liked, set unliked state and update text
                        likeButton.setText("🤍");
                        post.setLikes(post.getLikes() - 1);
                        likesTextView.setText(String.valueOf(post.getLikes()));
                        post.getUserIDsWhoLiked().remove(FirebaseHelper.GetCurrentUserId());
                    } else {
                        // If not liked, set liked state and update text
                        likeButton.setText("❤️");
                        post.setLikes(post.getLikes() + 1);
                        likesTextView.setText(String.valueOf(post.getLikes()));
                        post.getUserIDsWhoLiked().add(FirebaseHelper.GetCurrentUserId());
                        // Send like notification
                        FCMNotificationSender.sendFCMLikeReceivedNotification(posterToken);
                    }
                    // Update like status in Firestore
                    FirebaseHelper.UpdateModelInDatabase("posts", post, post);
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
                            .error(R.drawable.greyicon) // Error image (same as placeholder)
                            .into(profileImageView);
                    usernameTextView.setText(userModel.getUsername()); // Get username from UserModel

                    posterToken = userModel.getToken();
                    Log.d("FCM Token", "Poster FCM Token: " + posterToken); // Log the FCM token

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
            // load image from URL into ImageView
            Glide.with(itemView.getContext())
                    .load(photoURL)
                    .into(postImageView);
        }

        private void displayTimestamp(Timestamp timestamp) {
            if (timestamp != null) {
                // Format timestamp
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String formattedTimestamp = sdf.format(timestamp.toDate());
                timestampTextView.setText(formattedTimestamp);
            } else {
                timestampTextView.setText("No timestamp available");
            }
        }

        public
        Button getRemovePostButton()
        {
            return removePostButton;
        }

        public ImageView getProfileImageView() {
            return profileImageView;
        }
    }
}
