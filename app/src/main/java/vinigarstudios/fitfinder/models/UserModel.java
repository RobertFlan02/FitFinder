package vinigarstudios.fitfinder.models;

import com.google.firebase.Timestamp;

public class UserModel
{

    private String userId;
    private String phone;
    private String email;
    private String username;
    private Timestamp createdAt;
    public UserModel()
    {

    }

    public UserModel(String userId, String phone, String email, String username, Timestamp createdAt) {
        this.userId = userId;
        this.phone = phone;
        this.email = email;
        this.username = username;
        this.createdAt = createdAt;
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
}
