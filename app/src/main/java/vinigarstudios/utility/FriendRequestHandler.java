package vinigarstudios.utility;

import com.google.firebase.firestore.DocumentReference;

import vinigarstudios.fitfinder.models.FriendRequestModel;
import vinigarstudios.fitfinder.models.UserModel;

public class FriendRequestHandler
{
    public static boolean SendFriendRequest(String fromUserId, UserModel toUser)
    {
        for (String friendRequest : toUser.getFriendRequestsFromUserIdList())
        {
            if (friendRequest.startsWith(fromUserId))
            {
                return false;
            }
        }
        FriendRequestModel friendRequest = new FriendRequestModel(fromUserId, toUser);
        DocumentReference docRef = FirebaseHelper.UploadModelToDatabase("friendRequests", friendRequest, friendRequest.getFriendReqId());
        return true;
    }
}
