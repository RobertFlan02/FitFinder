package vinigarstudios.fitfinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.opengl.Visibility;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.Query;

import vinigarstudios.fitfinder.adapter.SearchUserRecyclerAdapter;
import vinigarstudios.fitfinder.models.UserModel;
import vinigarstudios.fitfinder.search.SearchUserActivity;
import vinigarstudios.utility.FirebaseHelper;
import vinigarstudios.utility.VinigarCompatActivity;

public class FriendsActivity extends VinigarCompatActivity
{
    private RecyclerView recyclerView;
    private SearchUserRecyclerAdapter recyclerAdapter;

    private TextView friendsText;

    private BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_friends);
        this.recyclerView = findViewById(R.id.searchUserRecyclerList);
        this.friendsText = findViewById(R.id.friendsActivityText);
        this.bottomNavigationView = findViewById(R.id.bottomNavigation);

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

        MenuItem menuItem = menu.findItem(R.id.actionSearch);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Type here to search");

        searchView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(@NonNull View v) {
                //Search was opened.
                recyclerView.setVisibility(View.VISIBLE);
                SetFriendPageVisibility(View.INVISIBLE);
            }

            @Override
            public void onViewDetachedFromWindow(@NonNull View v) {
                //Search was closed.
                recyclerView.setVisibility(View.INVISIBLE);
                SetFriendPageVisibility(View.VISIBLE);
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
                SetupSearchRecyclerView(newText);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
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

    /**
     * Sets the visibility of friends page
     * @param visibility e.g. View.INVISIBLE
     */
    private void SetFriendPageVisibility(Integer visibility)
    {
        friendsText.setVisibility(visibility);
        bottomNavigationView.setVisibility(visibility);
    }
}