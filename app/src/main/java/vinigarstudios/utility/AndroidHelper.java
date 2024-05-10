package vinigarstudios.utility;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import vinigarstudios.fitfinder.models.PostsModel;
import vinigarstudios.fitfinder.models.UserModel;

public class AndroidHelper
{
    public static  void ShowToast(Context context,String message)
    {
        Toast.makeText(context,message,Toast.LENGTH_LONG).show();
    }

    /**
     * Pass the UserModel into an Intent.
     * @param intent The Intent.
     * @param model The UserModel being passed in.
     */
    public static void PassUserModelAsIntent(Intent intent, UserModel model)
    {
        intent.putExtra("username", model.GetUsername());
        intent.putExtra("phone", model.GetPhone());
        intent.putExtra("userId", model.GetUserId());
        intent.putExtra("followerCount", model.getFollowerCount());
        intent.putExtra("profileImageURL", model.getProfileImageURL());
        intent.putExtra("email", model.getEmail());
        intent.putExtra("friendRequestsDocIdList", model.getFriendRequestsDocIdList());
        intent.putExtra("friendsId", model.getFriendsId());
        intent.putExtra("createdAt", model.getCreatedAt());
        intent.putExtra("postListsId", model.getPostsListIds());
        intent.putExtra("friendRequestsFromUserIdList", model.getFriendRequestsFromUserIdList());
        intent.putExtra("token", model.getToken());

    }

    /**
     * Get the UserModel you passed in from the method - PassUserModelAsIntent(intent, model).
     * @param intent The intent
     * @return Returns a UserModel.
     */
    public static UserModel GetUserModelFromIntent(Intent intent)
    {
        UserModel userModel = new UserModel();
        userModel.setUsername(intent.getStringExtra("username"));
        userModel.setPhone(intent.getStringExtra("phone"));
        userModel.setEmail(intent.getStringExtra("email"));
        userModel.setFriendRequestsDocIdList(intent.getStringArrayListExtra("friendRequestsDocIdList"));
        userModel.setFriendsId(intent.getStringArrayListExtra("friendsId"));
        userModel.setPostsListIds(intent.getStringArrayListExtra("postListsId"));
        userModel.setCreatedAt(intent.getParcelableExtra("createdAt"));
        userModel.setUserId(intent.getStringExtra("userId"));
        userModel.setFollowerCount(intent.getIntExtra("followerCount", 0));
        userModel.setProfileImageURL(intent.getStringExtra("profileImageURL"));
        userModel.setFriendRequestsFromUserIdList(intent.getStringArrayListExtra("friendRequestsFromUserIdList"));
        userModel.setToken(intent.getStringExtra("token"));

        return userModel;
    }

    public static void PassPostModelAsIntent(Intent intent, PostsModel model)
    {
        intent.putExtra("caption",model.getCaption());
        intent.putExtra("likes",model.getLikes());
        intent.putExtra("photoURL",model.getPhotoURL());
        intent.putExtra("profileUID",model.getProfileUID());
        intent.putExtra("timestamp",model.getTimestamp());
        intent.putExtra("title",model.getTitle());
    }

    public static PostsModel GetPostsModelFromIntent(Intent intent)
    {
        PostsModel postsModel = new PostsModel();
        postsModel.setCaption(intent.getStringExtra("caption"));
        postsModel.setLikes(intent.getIntExtra("likes", 0));
        postsModel.setPhotoURL(intent.getStringExtra("photoURL"));
        postsModel.setProfileUID(intent.getStringExtra("profileUID"));
        return postsModel;
    }

    public static void SetProfilePic(Context context, Uri imageUri, ImageView imageView)
    {
        Glide.with(context).load(imageUri).apply(RequestOptions.circleCropTransform()).into(imageView);
    }
}