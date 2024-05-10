package vinigarstudios.fitfinder.models;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import vinigarstudios.utility.FirebaseHelper;

public class FriendRequestModel implements IModel
{
    private String fromUserId;
    private UserModel toUser;
    private String friendReqId;

    public FriendRequestModel()
    {

    }
    public FriendRequestModel(String fromUserId, UserModel toUser)
    {
        this.fromUserId = fromUserId;
        this.toUser = toUser;
        this.friendReqId = toUser.getUserId() + "_" + toUser.getFriendRequestsDocIdList().size();
        toUser.getFriendRequestsDocIdList().add(friendReqId);
        toUser.getFriendRequestsFromUserIdList().add(fromUserId);
        FirebaseHelper.UpdateModelInDatabase("profiles", toUser, toUser);
    }

    public String getFromUserId() {
        return fromUserId;
    }
    public UserModel getToUser() {
        return toUser;
    }
    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public void setToUser(UserModel toUser) {
        this.toUser = toUser;
    }

    public String getFriendReqId() {
        return friendReqId;
    }

    @Override
    public String getDocumentId() {
        return friendReqId;
    }
}
