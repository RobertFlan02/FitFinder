package vinigarstudios.utility;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseHelper
{
    public static String CurrentUserId()
    {
        return FirebaseAuth.getInstance().getUid();
    }

    public static DocumentReference CurrentUserDetails()
    {
        return FirebaseFirestore.getInstance().collection("profiles").document(FirebaseHelper.CurrentUserId());
    }
}
