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
        this.userId = "DEFAULT PLACEHOLDER";
        this.phone = "DEFAULT PLACEHOLDER";
        this.email = "DEFAULT PLACEHOLDER";
        this.username = "DEFAULT PLACEHOLDER";
        this.createdAt = Timestamp.now();
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


    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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


}
