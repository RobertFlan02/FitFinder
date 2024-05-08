package vinigarstudios.utility;

import vinigarstudios.fitfinder.models.UserModel;

public class FriendRequest
{
    private UserModel fromUser;
    private UserModel toUser;
    public FriendRequest(UserModel fromUser, UserModel toUser)
    {
        this.fromUser = fromUser;
        this.toUser = toUser;
    }

    public UserModel getFromUser() {
        return fromUser;
    }

    public UserModel getToUser() {
        return toUser;
    }
}
