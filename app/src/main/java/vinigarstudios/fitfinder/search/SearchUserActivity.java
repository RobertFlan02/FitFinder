package vinigarstudios.fitfinder.search;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;

import vinigarstudios.fitfinder.R;
import vinigarstudios.fitfinder.adapter.SearchUserRecyclerAdapter;
import vinigarstudios.utility.FirebaseHelper;
import vinigarstudios.fitfinder.models.UserModel;
import vinigarstudios.utility.VinigarCompatActivity;

public class SearchUserActivity extends VinigarCompatActivity
{

    //region Private Fields

    private RecyclerView recyclerView;
    private List<String> itemsList;
    private SearchUserRecyclerAdapter recyclerAdapter;

    //endregion


    //region Override Methods
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_search_user);

        this.itemsList = FirebaseHelper.GetListFromDatabase("profiles", "profileName");
        this.recyclerView = findViewById(R.id.searchUserRecyclerList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        this.getMenuInflater().inflate(R.menu.search_view_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.actionSearch);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Type here to search");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

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
                .whereGreaterThanOrEqualTo("profileName",searchTerm)
                .whereLessThanOrEqualTo("profileName",searchTerm+'\uf8ff');

        FirestoreRecyclerOptions<UserModel> options = new FirestoreRecyclerOptions.Builder<UserModel>()
                .setQuery(query, UserModel.class).build();

        recyclerAdapter = new SearchUserRecyclerAdapter(options, getApplicationContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(recyclerAdapter);
        recyclerAdapter.startListening();

    }

}
