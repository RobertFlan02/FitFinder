package vinigarstudios.fitfinder.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

import vinigarstudios.fitfinder.activites.FriendsActivity;
import vinigarstudios.fitfinder.activites.MainActivity;
import vinigarstudios.fitfinder.R;
import vinigarstudios.fitfinder.models.UserModel;
import vinigarstudios.utility.AndroidHelper;
import vinigarstudios.utility.FirebaseHelper;

public class FriendReqRecyclerAdapter extends FirestoreRecyclerAdapter<UserModel, FriendReqRecyclerAdapter.UserModelViewHolder>
{
    private Context context;

    public FriendReqRecyclerAdapter(@NonNull FirestoreRecyclerOptions<UserModel> options, Context context)
    {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull UserModelViewHolder holder, int position, @NonNull UserModel model) {
        // Load profile picture from profileImageURL
        holder.itemView.setTag(holder.getAbsoluteAdapterPosition());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            holder.itemView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                @Override
                public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

                    menu.add(model.getUsername() + " has sent you a friend request!").setEnabled(false);
                    FirebaseHelper.GetCurrentUserDetails().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            UserModel currentUser = task.getResult().toObject(UserModel.class);

                            menu.add("Accept " + model.getUsername()).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(@NonNull MenuItem item) {
                                    assert currentUser != null;
                                    currentUser.AcceptUser(model);
                                    Intent intent = new Intent(context, FriendsActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    context.startActivity(intent);
                                    return true;
                                }
                            });
                            menu.add("Decline " + model.getUsername()).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(@NonNull MenuItem item) {
                                    assert currentUser != null;
                                    currentUser.DeclineUser(model);
                                    Intent intent = new Intent(context, FriendsActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    context.startActivity(intent);
                                    return true;
                                }
                            });
                        }
                    });
                    menu.add("Go to profile page").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(@NonNull MenuItem item) {
                            NavigateToOtherProfilePage(model);
                            return false;
                        }
                    });
                }
            });
        }

        String profileImageURL = model.getProfileImageURL();
        if (profileImageURL != null && !profileImageURL.isEmpty()) {
            Uri uri = Uri.parse(profileImageURL);
            Glide.with(context)
                    .load(uri)
                    .placeholder(R.drawable.greyicon)
                    .error(R.drawable.greyicon)
                    .into(holder.profilePic);
        } else {
            // Load default profile picture if no profileImageURL is available
            holder.profilePic.setImageResource(R.drawable.greyicon);
        }

        holder.itemView.setOnClickListener(v -> {
            NavigateToOtherProfilePage(model);
        });
    }

    private void NavigateToOtherProfilePage(@NonNull UserModel model) {
        // Navigate to other profile page
        Intent intent = new Intent(context, MainActivity.OtherProfileActivity.class);
        AndroidHelper.PassUserModelAsIntent(intent, model);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @NonNull
    @Override
    public UserModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_req_recycler_row, parent, false);
        return new UserModelViewHolder(view);
    }

    static class UserModelViewHolder extends RecyclerView.ViewHolder {
        ImageView profilePic;

        public UserModelViewHolder(@NonNull View itemView) {
            super(itemView);
            this.profilePic = itemView.findViewById(R.id.searchProfilePicImageView);
        }



    }
}