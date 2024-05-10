package vinigarstudios.fitfinder.models;

public class FriendRequestModel implements IModel
{
    private String fromUserId;
    private UserModel toUser;
    private String friendReqId;

    private int friendReqIncrement;

    public FriendRequestModel()
    {

    }
    public FriendRequestModel(String fromUserId, UserModel toUser)
    {
        this.fromUserId = fromUserId;
        this.toUser = toUser;
        this.friendReqId = fromUserId + "_" + friendReqIncrement;
        this.friendReqIncrement += 1;
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
