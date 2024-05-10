package vinigarstudios.fitfinder.loginregistration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.regex.Pattern;

import vinigarstudios.fitfinder.activites.MainActivity;
import vinigarstudios.fitfinder.R;
import vinigarstudios.fitfinder.models.UserModel;
import vinigarstudios.fitfinder.notifications.FCMTokenManager;
import vinigarstudios.utility.FirebaseHelper;

public class Register extends AppCompatActivity {

    private TextInputEditText editTextUsername, editTextEmail, editTextPassword, editTextConfirmPassword;
    private Button buttonReg;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private TextView textView;
    private UserModel userModel;
    private static final String TAG = "Register";
    private String username;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^\\w+([.-]?\\w+)*@\\w+([.-]?\\w+)*(\\.\\w{2,})+$");
    //this code makes sure that the string matches the format of a standard email address
    //it checks for letters numbers or underscores before the @
    //also allows hyphens and dots
    //then checks for more numbers or letters after the @
    //then it requires a dot with atleast 2 character after i.e .com or .ie

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
        //checks if user is logged in and directs them to the mainactivity
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        editTextUsername = findViewById(R.id.username);
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        buttonReg = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.btn_loginNow);
        editTextConfirmPassword = findViewById(R.id.confirm_password);
        ToggleButton showPasswordToggle = findViewById(R.id.show_password_toggle);
        //sets up textfields, buttons or event listeners etc

        showPasswordToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (showPasswordToggle.isChecked()) {
                    // Show the password
                    editTextPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    editTextConfirmPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    // Hide the password
                    editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    editTextConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
            }
            //this code allows the user to show password by pressing a button
            //if toggle button retruns true it means user wants to show password
            //input is then changed to TYPE_TEXT_VARIATION_VISIBLE_PASSWORD to reveal password
            //if toggle returns fakse, user wants to hide password
            //input type is then changed to TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD to hide password
        });

        editTextPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePasswordStrengthFeedback(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            //code gives you feedback on how strong your password is depending on what you type
        });

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        buttonReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String email, password, confirmPassword;
                username = String.valueOf(editTextUsername.getText());
                email = String.valueOf(editTextEmail.getText());
                password = String.valueOf(editTextPassword.getText());
                confirmPassword = String.valueOf(editTextConfirmPassword.getText());

                if (TextUtils.isEmpty(username)) {
                    Toast.makeText(Register.this, "Enter username", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                if (username.length() < 6) {
                    Toast.makeText(Register.this, "Username must be at least 6 characters", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(Register.this, "Enter email", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                if (!isValidEmail(email)) {
                    Toast.makeText(Register.this, "Invalid email address", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                if (!isValidEmailLength(email)) {
                    Toast.makeText(Register.this, "Email must have at least 6 characters before the '@' sign", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(Register.this, "Enter password", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(Register.this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                if (TextUtils.isEmpty(confirmPassword)) {
                    Toast.makeText(Register.this, "Confirm your password", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    Toast.makeText(Register.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                if (!containsCapital(password)) {
                    Toast.makeText(Register.this, "Password must contain at least one capital letter", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                //this code is used to error handle the register

                FirebaseFirestore.getInstance().collection("profiles")
                        .whereEqualTo("email", email)
                        .get()

                        // check if email already exists in the database
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    if (!task.getResult().isEmpty()) {
                                        // Email already exists in the database
                                        Toast.makeText(Register.this, "Email already in use.", Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.GONE);
                                    } else {
                                        // Email is not in use, proceed with account creation
                                        mAuth.createUserWithEmailAndPassword(email, password)
                                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                                        progressBar.setVisibility(View.GONE);
                                                        if (task.isSuccessful()) {
                                                            // User registered successfully, now create user profile
                                                            FirebaseUser user = mAuth.getCurrentUser();
                                                            if (user != null) {
                                                                String userId = user.getUid();
                                                                String userEmail = user.getEmail();
                                                                // Obtain the current users FCM token for notifications use later
                                                                FCMTokenManager.getCurrentUserToken(new FCMTokenManager.TokenRetrievedCallback() {
                                                                    @Override
                                                                    public void onTokenRetrieved(String token) {
                                                                        Log.d(TAG, "FCM token retrieved: " + token);
                                                                        SetData(token);
                                                                    }

                                                                    @Override
                                                                    public void onTokenRetrievalFailed(Exception exception) {
                                                                        Log.e(TAG, "Failed to retrieve FCM token", exception);
                                                                    }
                                                                });
                                                            }

                                                            Toast.makeText(Register.this, "Account Made", Toast.LENGTH_SHORT).show();
                                                            Intent intent = new Intent(getApplicationContext(), Login.class);
                                                            startActivity(intent);
                                                            finish();
                                                        } else {
                                                            // If sign in fails, display a message to the user.
                                                            Toast.makeText(Register.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    }
                                } else {
                                    // Error occurred while checking email existence
                                    Toast.makeText(Register.this, "Error checking email existence.", Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                }
                            }
                        });
            }// more error handling
        });

    }

    // Method to calculate password strength
    private int getPasswordStrength(String password) {
        int strength = 0;
        if (password.length() >= 2 ) {
            strength++;
        }
        if (containsCapital(password)) {
            strength++;
        }
        if (password.matches(".*\\d.*")) {
            strength++;
        }
        if (password.matches(".*[!@#$%^&*()].*")) {
            strength++;
        }
        return strength;
    }

    // password gets stronger by 1 after it has more than 2 characters
    //password gets stringer by 1 after it contains a capital letter
    //password gets stronger by 1 after adding a digit
    //the strongest contains all of the above aswell as a special symbol


    private void updatePasswordStrengthFeedback(String password) {
        int strength = getPasswordStrength(password);

        switch (strength) {
            case 0:

                Toast.makeText(Register.this, "Password too weak. Try more characters", Toast.LENGTH_SHORT).show();
                break;
            case 1:

                Toast.makeText(Register.this, "Weak password. Make 1 character capital", Toast.LENGTH_SHORT).show();
                break;
            case 2:

                Toast.makeText(Register.this, "Medium strength password. Add a number", Toast.LENGTH_SHORT).show();
                break;
            case 3:

                Toast.makeText(Register.this, "Strong password. Add special symbol", Toast.LENGTH_SHORT).show();
                break;
            case 4:

                Toast.makeText(Register.this, "Very strong password", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    //this code calculates the strenght of a password based on certain critria, after 1 criteria is met the
    //password gets stronger,



    private boolean containsCapital(String str) {
        for (char c : str.toCharArray()) {
            if (Character.isUpperCase(c)) {
                return true;
            }
        }
        return false;

        //checks if a string contains at least one uppercase letter.
    }


    private boolean isValidEmailLength(String email) {
        String[] emailSplit = email.split("@");
        return emailSplit.length == 2 && emailSplit[0].length() >= 5;
        // checks if an email address has a valid length before the @ symbol
    }

    // Validate email format
    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    private void SetData(String token) {

        if (userModel != null) {
            userModel.SetUsername(username);
        } else {
            userModel = new UserModel(mAuth.getCurrentUser().getUid(), "PHONE NUMBER", editTextEmail.getText().toString(), username, Timestamp.now(), "PROFILE IMAGE URL", 0, token);
        }

        FirebaseHelper.GetCurrentUserDetails().set(userModel);
        //sets data for a user model  and stores it in Firebase
    }
}
