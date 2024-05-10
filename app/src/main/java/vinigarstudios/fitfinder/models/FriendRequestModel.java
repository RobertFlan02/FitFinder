package vinigarstudios.fitfinder.models;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

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
        this.friendReqId = getFromUserId() + "_" + Integer.toString(toUser.getFriendRequestsDocIdList().size());
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

    public int getFriendReqIncrement() {
        return friendReqIncrement;
    }

    @Override
    public String getDocumentId() {
        return friendReqId;
    }
}
