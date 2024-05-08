package vinigarstudios.fitfinder.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import vinigarstudios.fitfinder.R;
import vinigarstudios.fitfinder.models.UserModel;
import vinigarstudios.fitfinder.search.OtherProfileActivity;
import vinigarstudios.utility.FirebaseHelper;
import vinigarstudios.utility.AndroidHelper;

public class SearchUserRecyclerAdapter extends FirestoreRecyclerAdapter<UserModel, SearchUserRecyclerAdapter.UserModelViewHolder>
{
    private Context context;
    public SearchUserRecyclerAdapter(@NonNull FirestoreRecyclerOptions<UserModel> options, Context context)
    {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull UserModelViewHolder holder, int position, @NonNull UserModel model) {
        holder.usernameTextView.setText(model.GetUsername());
        if(model.GetUserId().equals(FirebaseHelper.GetCurrentUserId())){
            holder.usernameTextView.setText(model.GetUsername()+ " (Me)");
            return;
        }

        FirebaseHelper.GetOtherProfilePicStorageRef(model.GetUserId()).getDownloadUrl()
                .addOnCompleteListener(t -> {
                    if(t.isSuccessful()){
                        Uri uri  = t.getResult();
                        AndroidHelper.SetProfilePic(context, uri, holder.profilePic);
                    }
                });

        holder.itemView.setOnClickListener(v -> {
            //navigate to other profile page
            Intent intent = new Intent(context, OtherProfileActivity.class);
            AndroidHelper.PassUserModelAsIntent(intent, model);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
    }

    @NonNull
    @Override
    public UserModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.search_user_recycler_row, parent, false);
        return new UserModelViewHolder(view);
    }

    class UserModelViewHolder extends RecyclerView.ViewHolder
    {
        TextView usernameTextView;
        ImageView profilePic;
        public UserModelViewHolder(@NonNull View itemView) {
            super(itemView);
            this.usernameTextView = itemView.findViewById(R.id.searchUsernameText);
            this.profilePic = itemView.findViewById(R.id.searchProfilePicImageView);
        }
    }
}


