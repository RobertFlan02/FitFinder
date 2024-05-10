package vinigarstudios.fitfinder.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import vinigarstudios.fitfinder.R;
import vinigarstudios.fitfinder.models.PostsModel;
import vinigarstudios.fitfinder.models.UserModel;
import vinigarstudios.utility.FirebaseHelper;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<PostsModel> postsList;
    private static FirebaseFirestore db;

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

            // Set initial text based on whether the post is liked or not
            if (post.getUserIDsWhoLiked().contains(FirebaseHelper.GetCurrentUserId())) {
                likeButton.setText("❤️"); // Liked state
            } else {
                likeButton.setText("🤍"); // Unliked state
            }

            // Set OnClickListener to toggle like/unlike and update button text
            likeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!post.getUserIDsWhoLiked().contains(FirebaseHelper.GetCurrentUserId())) {
                        // If not liked, set liked state and update text
                        likeButton.setText("❤️");
                        post.setLikes(post.getLikes() + 1);
                        PostsModel newPost = post;
                        likesTextView.setText(String.valueOf(post.getLikes()));
                        post.getUserIDsWhoLiked().add(FirebaseHelper.GetCurrentUserId());
                        FirebaseHelper.UpdateModelInDatabase("posts", post, newPost);
                    } else {
                        // If liked, set unliked state and update text
                        likeButton.setText("🤍");
                        post.setLikes(post.getLikes() - 1);
                        PostsModel newPost = post;
                        likesTextView.setText(String.valueOf(post.getLikes()));
                        post.getUserIDsWhoLiked().remove(FirebaseHelper.GetCurrentUserId());
                        FirebaseHelper.UpdateModelInDatabase("posts", post, newPost);
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
