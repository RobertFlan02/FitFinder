package vinigarstudios.fitfinder.activites;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import vinigarstudios.fitfinder.R;
import vinigarstudios.fitfinder.adapter.FriendReqRecyclerAdapter;
import vinigarstudios.fitfinder.adapter.PostAdapter;
import vinigarstudios.fitfinder.adapter.SearchUserRecyclerAdapter;
import vinigarstudios.fitfinder.enums.ListOrder;
import vinigarstudios.fitfinder.models.PostsModel;
import vinigarstudios.fitfinder.models.UserModel;
import vinigarstudios.utility.AndroidHelper;
import vinigarstudios.utility.FirebaseHelper;
import vinigarstudios.utility.FriendRequestHandler;
import vinigarstudios.utility.VinigarCompatActivity;

public class FriendsActivity extends VinigarCompatActivity
{
    private ListOrder listOrder;
    private RecyclerView searchUserRecyclerView;
    private SearchUserRecyclerAdapter searchUserRecyclerAdapter;
    private RecyclerView friendRequestRecyclerView;
    private FriendReqRecyclerAdapter friendRequestRecyclerAdapter;
    private SwitchCompat friendsFilterSwitch;
    private LinearLayout topBar;
    private BottomNavigationView bottomNavigationView;
    private String getLastText;
    private boolean isFriendsFilterOn;
    private PostAdapter postAdapter;
    private List<PostsModel> postsList;
    private RecyclerView friendsPostRecyclerView;
    private UserModel currentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_friends);
        this.searchUserRecyclerView = findViewById(R.id.searchUserRecyclerList);
        this.friendRequestRecyclerView = findViewById(R.id.friendRequestRecyclerList);
        this.friendsFilterSwitch = findViewById(R.id.friendsSearchFilterButton);
        this.bottomNavigationView = findViewById(R.id.bottomNavigation);
        this.topBar = findViewById(R.id.profile_title_layout);
        this.listOrder = ListOrder.time;

        this.postAdapter = new PostAdapter(this, postsList, database, true);
        this.friendsPostRecyclerView = findViewById(R.id.friendsPostRecyclerList);
        this.friendsPostRecyclerView.setAdapter(postAdapter);
        this.friendsPostRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        FirebaseHelper.GetCurrentUserDetails().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                currentUser = task.getResult().toObject(UserModel.class);
                SetupFriendReqRecyclerView();
                database.collection("posts").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.getResult().size() > 0) //if posts collection has documents (FetchPosts crashes if on 0 posts).
                        {
                            FetchPostsFromFirestore();
                        }
                    }
                });
            }
        });


        this.friendsFilterSwitch.setVisibility(View.INVISIBLE);

        bottomNavigationView.setSelectedItemId(R.id.bottom_friends);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.bottom_home) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (item.getItemId() == R.id.bottom_friends) {
                return true;
            } else if (item.getItemId() == R.id.bottom_upload) {
                startActivity(new Intent(getApplicationContext(), UploadActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (item.getItemId() == R.id.bottom_profile) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            }
            return false;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        this.getMenuInflater().inflate(R.menu.search_view_menu, menu);

        MenuItem searchButton = menu.findItem(R.id.actionSearch);
        MenuItem likesFilter = menu.findItem(R.id.action_order_by_likes);
        MenuItem timeFilter = menu.findItem(R.id.action_order_by_time);
        SearchView searchView = (SearchView) searchButton.getActionView();
        searchView.setQueryHint("Type here to search");

        searchView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(@NonNull View v) {
                //Search was opened.
                searchUserRecyclerView.setVisibility(View.VISIBLE);
                friendsFilterSwitch.setVisibility(View.VISIBLE);
                SetFriendPageVisibility(View.INVISIBLE);
                likesFilter.setVisible(false);
                timeFilter.setVisible(false);
            }

            @Override
            public void onViewDetachedFromWindow(@NonNull View v) {
                //Search was closed.
                searchUserRecyclerView.setVisibility(View.INVISIBLE);
                friendsFilterSwitch.setVisibility(View.INVISIBLE);
                SetFriendPageVisibility(View.VISIBLE);
                likesFilter.setEnabled(true);
                timeFilter.setEnabled(true);
                SetupFriendReqRecyclerView();
            }
        });

        friendsFilterSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                FirebaseHelper.GetCurrentUserDetails().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        currentUser = task.getResult().toObject(UserModel.class);
                        if (isChecked)
                        {
                            isFriendsFilterOn = true;
                            SetupFriendSearchRecyclerView(getLastText);
                        }
                        else
                        {
                            isFriendsFilterOn = false;
                            SetupSearchRecyclerView(getLastText);
                        }
                    }
                });
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query)
            {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText)
            {
                getLastText = newText;
                if (!isFriendsFilterOn)
                {
                    SetupSearchRecyclerView(newText);
                }
                else
                {
                    FirebaseHelper.GetCurrentUserDetails().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            currentUser = task.getResult().toObject(UserModel.class);
                            SetupFriendSearchRecyclerView(newText);
                        }
                    });
                }
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FirebaseHelper.GetCurrentUserDetails().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {


            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                int itemId = item.getItemId();
                if (itemId == R.id.action_order_by_time) {
                    listOrder = ListOrder.time;
                    FetchPostsFromFirestore();
                } else if (itemId == R.id.action_order_by_likes) {
                    listOrder = ListOrder.likes;
                    FetchPostsFromFirestore();
                }
            }
        });
        return true;
    }
    @Override
    protected void onStop() {
        super.onStop();

        if (searchUserRecyclerAdapter != null)
        {
            searchUserRecyclerAdapter.stopListening();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (searchUserRecyclerAdapter != null)
        {
            searchUserRecyclerAdapter.startListening();
        }
    }

    //endregion

    /**
     * Sets up the RecyclerView for Friend Requests.
     */
    private void SetupFriendReqRecyclerView(){

        Query query;

        this.friendRequestRecyclerView = findViewById(R.id.friendRequestRecyclerList);
        if (currentUser.getFriendRequestsFromUserIdList().isEmpty())
        {
            //This query will return empty. There is an error when getFriendsId is empty so we have to handle it in this if statement.
            query = FirebaseHelper.GetAllProfilesCollectionReference()
                    .whereEqualTo("userId", "");
        }
        else
        {
            query = FirebaseHelper.GetAllProfilesCollectionReference()
                    .whereIn("userId", currentUser.getFriendRequestsFromUserIdList());
        }

        FirestoreRecyclerOptions<UserModel> options = new FirestoreRecyclerOptions.Builder<UserModel>()
                .setQuery(query, UserModel.class).build();

        friendRequestRecyclerAdapter = new FriendReqRecyclerAdapter(options, getApplicationContext());
        friendRequestRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        friendRequestRecyclerView.setAdapter(friendRequestRecyclerAdapter);
        friendRequestRecyclerAdapter.startListening();
    }

    private void SetupSearchRecyclerView(String searchTerm){

        this.searchUserRecyclerView = findViewById(R.id.searchUserRecyclerList);
        Query query = FirebaseHelper.GetAllProfilesCollectionReference()
                .whereGreaterThanOrEqualTo("username",searchTerm)
                .whereLessThanOrEqualTo("username",searchTerm+'\uf8ff')
                .whereNotEqualTo("userId", FirebaseHelper.GetCurrentUserId());

        FirestoreRecyclerOptions<UserModel> options = new FirestoreRecyclerOptions.Builder<UserModel>()
                .setQuery(query, UserModel.class).build();

        searchUserRecyclerAdapter = new SearchUserRecyclerAdapter(options, getApplicationContext());
        searchUserRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchUserRecyclerView.setAdapter(searchUserRecyclerAdapter);
        searchUserRecyclerAdapter.startListening();
    }

    /**
     * Sets up the RecyclerView for FriendSearch.
     * @param searchTerm The search filter.
     */
    private void SetupFriendSearchRecyclerView(String searchTerm)
    {
        Query query;

        this.searchUserRecyclerView = findViewById(R.id.searchUserRecyclerList);
        if (currentUser.getFriendsId().isEmpty())
        {
            //This query will return empty. There is an error when getFriendsId is empty so we have to handle it in this if statement.
            query = FirebaseHelper.GetAllProfilesCollectionReference()
                    .whereEqualTo("userId", "");
        }
        else
        {
            query = FirebaseHelper.GetAllProfilesCollectionReference()
                    .whereGreaterThanOrEqualTo("username",searchTerm)
                    .whereLessThanOrEqualTo("username",searchTerm+'\uf8ff')
                    .whereNotEqualTo("userId", currentUser.getUserId())
                    .whereIn("userId", currentUser.getFriendsId());
        }

        FirestoreRecyclerOptions<UserModel> options = new FirestoreRecyclerOptions.Builder<UserModel>()
                .setQuery(query, UserModel.class).build();

        searchUserRecyclerAdapter = new SearchUserRecyclerAdapter(options, getApplicationContext());
        searchUserRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchUserRecyclerView.setAdapter(searchUserRecyclerAdapter);
        searchUserRecyclerAdapter.startListening();
    }

    /**
     * Sets the visibility of friends page
     * @param visibility e.g. View.INVISIBLE
     */
    private void SetFriendPageVisibility(Integer visibility)
    {
        bottomNavigationView.setVisibility(visibility);
        topBar.setVisibility(visibility);
        friendsPostRecyclerView.setVisibility(visibility);
        friendRequestRecyclerView.setVisibility(visibility);
    }

    /**
     * Fetches posts from Firestore.
     */
    private void FetchPostsFromFirestore()
    {
        Query query;
        if (!currentUser.getFriendsId().isEmpty())
        {
            query = database.collection("posts")
                    .whereIn("profileUID", currentUser.getFriendsId()) // Filter by the UIDs of the currentUsers friendsId.
                    .orderBy(listOrder.value, Query.Direction.DESCENDING);
        }
        else
        {
            query = database.collection("posts")
                    .whereEqualTo("profileUID", ""); // Filter by the UIDs of the currentUsers friendsId.
        }
                query
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    postsList = new ArrayList<>();
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        PostsModel post = documentSnapshot.toObject(PostsModel.class);
                        postsList.add(post);
                    }
                    postAdapter.setPostsList(postsList);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch posts", Toast.LENGTH_SHORT).show();
                    Log.e("FETCH POST FRIENDS PAGE ERROR", e.getMessage());
                });
    }

    //Idk how I added a second one of these(github bug?). Check the copy of it in MainActivity for Documentation.
    public static class OtherProfileActivity extends VinigarCompatActivity
    {
        private UserModel currentUser;
        private UserModel otherUser;
        private TextView username;
        private ImageView profileImage;
        private TextView followerCount;
        private Button addFriendButton;

        private Button removeFriendButton;
        private Button acceptFriendReqButton;
        private Button declineFriendReqButton;
        private BottomNavigationView bottomNavigationView;
        @SuppressLint("SetTextI18n")
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            this.setContentView(R.layout.otherprofile);




            FirebaseHelper.GetCurrentUserDetails().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    currentUser = task.getResult().toObject(UserModel.class);
                    HandleButtonVisibility();
                }
            });
            otherUser = AndroidHelper.GetUserModelFromIntent(getIntent());
            username = findViewById(R.id.otherProfileUsernameText);
            profileImage = findViewById(R.id.otherProfileImage);
            followerCount = findViewById(R.id.otherProfileFollowerCount);
            addFriendButton = findViewById(R.id.otherProfileAddFriendButton);
            removeFriendButton = findViewById(R.id.otherProfileRemoveFriendButton);
            acceptFriendReqButton = findViewById(R.id.otherProfileAcceptFriendButton);
            declineFriendReqButton = findViewById(R.id.otherProfileDeclineFriendButton);

            username.setText(otherUser.getUsername());
            followerCount.setText("Friends: " + otherUser.getFollowerCount());

            this.ButtonsFunctionality();

            this.bottomNavigationView = findViewById(R.id.bottomNavigation);

            Glide.with(this)
                    .load(otherUser.getProfileImageURL())
                    .placeholder(R.drawable.greyicon) // Placeholder image while loading
                    .error(R.drawable.greyicon) // Error image if loading fails
                    .into(profileImage);

            //Copy paste but under time pressure (its 5:30 am).
            bottomNavigationView.setSelectedItemId(R.id.bottom_friends);
            bottomNavigationView.setOnItemSelectedListener(item -> {
                if (item.getItemId() == R.id.bottom_home) {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                    return true;
                } else if (item.getItemId() == R.id.bottom_friends) {
                    return true;
                } else if (item.getItemId() == R.id.bottom_upload) {
                    startActivity(new Intent(getApplicationContext(), UploadActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                    return true;
                } else if (item.getItemId() == R.id.bottom_profile) {
                    startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                    return true;
                }
                return false;
            });
        }



        private void HandleButtonVisibility() {
            if (currentUser.getFriendsId().stream().anyMatch(f -> f.startsWith(otherUser.getUserId()))) //if user is friends with this person
            {
                addFriendButton.setVisibility(View.INVISIBLE);
                declineFriendReqButton.setVisibility(View.INVISIBLE);
                acceptFriendReqButton.setVisibility(View.INVISIBLE);
                removeFriendButton.setVisibility(View.VISIBLE);
            }
            else if (currentUser.getFriendRequestsFromUserIdList().stream().anyMatch(f -> f.startsWith(otherUser.getUserId()))) //if user has friendreq from this person
            {
                addFriendButton.setVisibility(View.INVISIBLE);
                declineFriendReqButton.setVisibility(View.VISIBLE);
                acceptFriendReqButton.setVisibility(View.VISIBLE);
                removeFriendButton.setVisibility(View.INVISIBLE);
            }
            else
            {
                addFriendButton.setVisibility(View.VISIBLE);
                declineFriendReqButton.setVisibility(View.INVISIBLE);
                acceptFriendReqButton.setVisibility(View.INVISIBLE);
                removeFriendButton.setVisibility(View.INVISIBLE);
            }
        }

        private void ButtonsFunctionality()
        {
            this.addFriendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (FriendRequestHandler.SendFriendRequest(FirebaseHelper.GetCurrentUserId(), otherUser))
                    {
                        Toast.makeText(getApplicationContext(), "Friend Request Sent to " + otherUser.getUsername() + "!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), FriendsActivity.class));
                        finish();
                    }
                    else
                    {
                        //Friend request already sent
                        Toast.makeText(getApplicationContext(), "Friend Request Already Sent", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            this.removeFriendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try
                    {
                        currentUser.RemoveUser(otherUser);
                        Toast.makeText(getApplicationContext(), "User '" + otherUser.getUsername() + "' is no longer your friend!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), FriendsActivity.class));
                        finish();
                    }
                    catch(Exception e)
                    {
                        Toast.makeText(getApplicationContext(), "Error deleting friend", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            this.acceptFriendReqButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try
                    {
                        currentUser.AcceptUser(otherUser);
                        Toast.makeText(getApplicationContext(), "User '" + otherUser.getUsername() + "' is now your friend!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), FriendsActivity.class));
                        finish();
                    }
                    catch(Exception e)
                    {
                        //Friend request already sent
                        Toast.makeText(getApplicationContext(), "Error accepting friend Request", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            this.declineFriendReqButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try
                    {
                        currentUser.DeclineUser(otherUser);
                        Toast.makeText(getApplicationContext(), "User '" + otherUser.getUsername() + "''s friend request is declined!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), FriendsActivity.class));
                        finish();
                    }
                    catch(Exception e)
                    {
                        //Friend request already sent
                        Toast.makeText(getApplicationContext(), "Error declining friend Request", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }
}