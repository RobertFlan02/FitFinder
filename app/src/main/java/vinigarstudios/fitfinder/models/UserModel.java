package vinigarstudios.fitfinder.models;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import vinigarstudios.utility.FirebaseHelper;

public class UserModel implements IModel
{
    private String userId;
    private String phone;
    private String email;
    private String username;
    private Timestamp createdAt;
    private String profileImageURL;
    private ArrayList<String> postsListIds;
    private int followerCount;
    private ArrayList<String> friendsId;
    private ArrayList<String> friendRequestsDocIdList;
    private ArrayList<String> friendRequestsFromUserIdList;
    private String token;

    public UserModel()
    {
        this.userId = "DEFAULT PLACEHOLDER";
        this.phone = "DEFAULT PLACEHOLDER";
        this.email = "DEFAULT PLACEHOLDER";
        this.username = "DEFAULT PLACEHOLDER";
        this.createdAt = Timestamp.now();
        this.profileImageURL = "DEFAULT PLACEHOLDER";
        this.followerCount = 0;
        this.token = "DEFUALT PLACEHOLDER";
        this.friendsId = new ArrayList<>();
        this.friendRequestsDocIdList = new ArrayList<>();
        this.postsListIds = new ArrayList<>();
        this.friendRequestsFromUserIdList = new ArrayList<>();
    }

    public UserModel(UserModel copy) {
        this.userId = copy.userId;
        this.phone = copy.phone;
        this.email = copy.email;
        this.username = copy.username;
        this.createdAt = copy.createdAt;
        this.profileImageURL = copy.profileImageURL;
        this.followerCount = copy.followerCount;
        this.token = copy.token;
        this.friendsId = copy.friendsId;
        this.friendRequestsDocIdList = copy.friendRequestsDocIdList;
        this.postsListIds = copy.postsListIds;
        this.friendRequestsFromUserIdList = copy.friendRequestsFromUserIdList;
    }

    public UserModel(String userId, String phone, String email, String username, Timestamp createdAt, String profileImageURL, int followerCount, String token) {
        this.userId = userId;
        this.phone = phone;
        this.email = email;
        this.username = username;
        this.createdAt = createdAt;
        this.profileImageURL = profileImageURL;
        this.followerCount = followerCount;
        this.friendsId = new ArrayList<>();
        this.friendRequestsDocIdList = new ArrayList<>();
        this.postsListIds = new ArrayList<>();
        this.friendRequestsFromUserIdList = new ArrayList<>();
        this.token = token;
    }

    public String GetUserId() {
        return userId;
    }

    public String GetPhone() {
        return phone;
    }

    public String GetEmail() {
        return email;
    }

    public String GetUsername() {
        return username;
    }

    public Timestamp GetCreatedAt() {
        return createdAt;
    }

    public void SetUserId(String userId) {
        this.userId = userId;
    }

    public void SetPhone(String phone) {
        this.phone = phone;
    }

    public void SetEmail(String email) {
        this.email = email;
    }

    public void SetUsername(String username) {
        this.username = username;
    }

    public void SetCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    //Need setters and getters without caps first letter (seriously tho these are methods they should be PascalCase) because Firebase needs
    //setters and getters in camelCase. The top ones Set/Get are already used so for extensibility and backwards compatibility they are untouched :)
    //I refuse to make methods camelCase they are harder to find then.

    public String getUserId() {
        return userId;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public String getProfileImageURL() { return profileImageURL; }

    public int getFollowerCount() { return followerCount; }

    public String getToken() { return token; }


    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setPostsListIds(ArrayList<String> postsListIds) {
        this.postsListIds = postsListIds;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public void setProfileImageURL(String profileImageURL) { this.profileImageURL = profileImageURL; }

    public void setFollowerCount(int followerCount) { this.followerCount = followerCount; }

    public ArrayList<String> getFriendRequestsFromUserIdList() {
        return friendRequestsFromUserIdList;
    }

    public void setFriendRequestsFromUserIdList(ArrayList<String> friendRequestsFromUserIdList) {
        this.friendRequestsFromUserIdList = friendRequestsFromUserIdList;
    }

    public void setToken(String token) { this.token = token; }

    public ArrayList<String> getFriendsId() {
        return friendsId;
    }

    public void setFriendsId(ArrayList<String> friendsId) {
        this.friendsId = friendsId;
    }

    public ArrayList<String> getFriendRequestsDocIdList() {
        return friendRequestsDocIdList;
    }

    public void setFriendRequestsDocIdList(ArrayList<String> friendRequestsDocIdList) {
        this.friendRequestsDocIdList = friendRequestsDocIdList;

    }

    public ArrayList<String> getPostsListIds()
    {
        return postsListIds;
    }


    public void AddToPostsList(String postId)
    {
        if (!postsListIds.contains(postId))
        {
            postsListIds.add(postId);;
            FirebaseHelper.UpdateModelInDatabase("profiles", this, this);
        }
    }

    public void AddToPostsList(PostsModel postsModel)
    {
        if (!postsListIds.contains(postsModel.getPostId()))
        {
            postsListIds.add(postsModel.getPostId());;
            FirebaseHelper.UpdateModelInDatabase("profiles", this, this);
        }
    }

    public void RemoveFromPostsList(PostsModel postsModel)
    {
        if (postsListIds.contains(postsModel.getPostId())) {
            postsListIds.remove(postsModel.getPostId());
            ;
            FirebaseHelper.UpdateModelInDatabase("profiles", this, this);
        }
    }

    @Override
    public String getDocumentId() {
        return userId;
    }

    public void DeclineUser(UserModel otherUser)
    {
        for(String string : getFriendRequestsFromUserIdList())
        {
            if (string.startsWith(otherUser.getUserId()))
            {
                this.RemoveUserAndDeleteFriendRequest(this.getUserId() + "_" + otherUser.getUserId());
                this.getFriendRequestsFromUserIdList().remove(otherUser.getUserId());
            }
        }
        FirebaseHelper.UpdateModelInDatabase("profiles", this, this);
    }

    @Deprecated
    public void DeclineUser(String otherUserId)
    {
        for(String string : getFriendRequestsFromUserIdList())
        {
            if (string.startsWith(otherUserId))
            {
                this.RemoveUserAndDeleteFriendRequest(this.getUserId() + "_" + otherUserId);
                this.getFriendRequestsFromUserIdList().remove(otherUserId);

            }
        }
        FirebaseHelper.UpdateModelInDatabase("profiles", this, this);
    }

    @Deprecated
    public void DeclineFriendReq(String friendRequestId)
    {
        this.RemoveUserAndDeleteFriendRequest("afafa");
        FirebaseHelper.UpdateModelInDatabase("profiles", this, this);
    }

    public void AcceptUser(UserModel otherUser)
    {
        for(String string : getFriendRequestsFromUserIdList())
        {
            if (string.startsWith(otherUser.getUserId()))
            {
                this.RemoveUserAndDeleteFriendRequest(this.getUserId() + "_" + otherUser.getUserId());
                this.getFriendRequestsFromUserIdList().remove(otherUser.getUserId());


                this.getFriendsId().add(otherUser.getUserId());
                this.setFollowerCount(this.getFriendsId().size());
                otherUser.getFriendsId().add(this.getUserId());
                otherUser.setFollowerCount(otherUser.getFollowerCount() + 1);
                FirebaseHelper.UpdateModelInDatabase("profiles", this, this);
                FirebaseHelper.UpdateModelInDatabase("profiles", otherUser, otherUser);
                return;
            }
        }
    }

    @Deprecated
    public void AcceptUser(String otherUserId)
    {
        for(String string : getFriendRequestsDocIdList())
        {
            if (string.startsWith(otherUserId))
            {
                this.RemoveUserAndDeleteFriendRequest(string);
                //this.AddFriendToListAndUpdateFollowerCount(otherUserId);
            }
        }
        FirebaseHelper.UpdateModelInDatabase("profiles", this, this);
    }

    public void RemoveUser(UserModel otherUser)
    {
        for(String string : getFriendsId())
        {
            if (string.equals(otherUser.getUserId()))
            {
                this.getFriendsId().remove(string);
                this.setFollowerCount(this.getFriendsId().size());
                otherUser.getFriendsId().remove(this.getUserId());
                otherUser.setFollowerCount(otherUser.getFriendsId().size());
                FirebaseHelper.UpdateModelInDatabase("profiles", this, this);
                FirebaseHelper.UpdateModelInDatabase("profiles", otherUser, otherUser);
            }
        }
    }

    private void RemoveUserAndDeleteFriendRequest(String string)
    {
        getFriendRequestsDocIdList().remove(string);
        FirebaseHelper.RemoveModelInDatabase("friendRequests", string);
    }
}
