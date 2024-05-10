package vinigarstudios.fitfinder;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Firebase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import vinigarstudios.fitfinder.adapter.PostAdapter;
import vinigarstudios.fitfinder.adapter.SearchUserRecyclerAdapter;
import vinigarstudios.fitfinder.enums.ListOrder;
import vinigarstudios.fitfinder.models.PostsModel;
import vinigarstudios.fitfinder.models.UserModel;
import vinigarstudios.utility.FirebaseHelper;
import vinigarstudios.utility.VinigarCompatActivity;

public class FriendsActivity extends VinigarCompatActivity
{
    private ListOrder listOrder;
    private RecyclerView recyclerView;
    private SearchUserRecyclerAdapter recyclerAdapter;
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
        this.recyclerView = findViewById(R.id.searchUserRecyclerList);
        this.friendsFilterSwitch = findViewById(R.id.friendsSearchFilterButton);
        this.bottomNavigationView = findViewById(R.id.bottomNavigation);
        this.topBar = findViewById(R.id.profile_title_layout);
        this.listOrder = ListOrder.time;

        this.postAdapter = new PostAdapter(postsList, database);
        this.friendsPostRecyclerView = findViewById(R.id.friendsPostRecyclerList);
        this.friendsPostRecyclerView.setAdapter(postAdapter);
        this.friendsPostRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        FirebaseHelper.GetCurrentUserDetails().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                currentUser = task.getResult().toObject(UserModel.class);
                FetchPostsFromFirestore();
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
                recyclerView.setVisibility(View.VISIBLE);
                friendsFilterSwitch.setVisibility(View.VISIBLE);
                SetFriendPageVisibility(View.INVISIBLE);
                likesFilter.setVisible(false);
                timeFilter.setVisible(false);
            }

            @Override
            public void onViewDetachedFromWindow(@NonNull View v) {
                //Search was closed.
                recyclerView.setVisibility(View.INVISIBLE);
                friendsFilterSwitch.setVisibility(View.INVISIBLE);
                SetFriendPageVisibility(View.VISIBLE);
                likesFilter.setEnabled(true);
                timeFilter.setEnabled(true);
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

        if (recyclerAdapter != null)
        {
            recyclerAdapter.stopListening();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (recyclerAdapter != null)
        {
            recyclerAdapter.startListening();
        }
    }

    //endregion

    private void SetupSearchRecyclerView(String searchTerm){

        this.recyclerView = findViewById(R.id.searchUserRecyclerList);
        Query query = FirebaseHelper.GetAllProfilesCollectionReference()
                .whereGreaterThanOrEqualTo("username",searchTerm)
                .whereLessThanOrEqualTo("username",searchTerm+'\uf8ff');

        FirestoreRecyclerOptions<UserModel> options = new FirestoreRecyclerOptions.Builder<UserModel>()
                .setQuery(query, UserModel.class).build();

        recyclerAdapter = new SearchUserRecyclerAdapter(options, getApplicationContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(recyclerAdapter);
        recyclerAdapter.startListening();
    }

    private void SetupFriendSearchRecyclerView(String searchTerm)
    {
        Query query;

        this.recyclerView = findViewById(R.id.searchUserRecyclerList);
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
                    .whereIn("userId", currentUser.getFriendsId());
        }

        FirestoreRecyclerOptions<UserModel> options = new FirestoreRecyclerOptions.Builder<UserModel>()
                .setQuery(query, UserModel.class).build();

        recyclerAdapter = new SearchUserRecyclerAdapter(options, getApplicationContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(recyclerAdapter);
        recyclerAdapter.startListening();
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
    }

    private void FetchPostsFromFirestore()
    {
        database.collection("posts")
                .whereIn("profileUID", currentUser.getFriendsId()) // Filter by the UIDs of the currentUsers friendsId.
                .orderBy(listOrder.value, Query.Direction.DESCENDING)
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
}