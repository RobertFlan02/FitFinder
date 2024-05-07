package vinigarstudios.utility;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import vinigarstudios.fitfinder.models.UserModel;
import com.google.firebase.firestore.auth.User;

public class AndroidHelper
{
    public static  void ShowToast(Context context,String message)
    {
        Toast.makeText(context,message,Toast.LENGTH_LONG).show();
    }

    public static void PassUserModelAsIntent(Intent intent, UserModel model)
    {
        intent.putExtra("username",model.GetUsername());
        intent.putExtra("phone",model.GetPhone());
        intent.putExtra("userId",model.GetUserId());

    }

    public static UserModel GetUserModelFromIntent(Intent intent)
    {
        UserModel userModel = new UserModel();
        userModel.SetUsername(intent.getStringExtra("username"));
        userModel.SetPhone(intent.getStringExtra("phone"));
        userModel.SetUserId(intent.getStringExtra("userId"));
        return userModel;
    }

    public static void SetProfilePic(Context context, Uri imageUri, ImageView imageView)
    {
        Glide.with(context).load(imageUri).apply(RequestOptions.circleCropTransform()).into(imageView);
    }
}