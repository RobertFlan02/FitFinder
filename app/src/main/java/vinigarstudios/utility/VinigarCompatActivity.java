package vinigarstudios.utility;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import vinigarstudios.fitfinder.loginregistration.Login;

/**
 * This class has base implementation that should be in most activity view models.
 * 1. It makes sure user is signed in and puts him to Login page if user is not signed in -> Hence why login doesn't inherit this.
 * 2. Contains a reference to the CurrentUser mAuth and Database.
 */
public class VinigarCompatActivity extends AppCompatActivity {

    protected FirebaseAuth mAuth;
    protected FirebaseUser user;
    protected FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();

        if (user == null){
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }
    }

    /**
     * Gets the Auth.
     * @return mAuth
     */
    public FirebaseAuth GetAuth()
    {
        return mAuth;
    }

    /**
     * Gets the User.
     * @return user
     */
    public FirebaseUser GetUser()
    {
        return user;
    }

    /**
     * Gets the Database.
     * @return database
     */
    public FirebaseFirestore GetDatabase()
    {
        return database;
    }
}
