package vinigarstudios.fitfinder.deprecated;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import vinigarstudios.fitfinder.R;
import vinigarstudios.utility.FirebaseHelper;
import vinigarstudios.utility.VinigarCompatActivity;

@Deprecated
//Use SearchUserAcitivity instead. RecyclerView > ListView.
public class Search extends VinigarCompatActivity
{

    //region Private Fields

    private ListView listView;
    private List<String> itemsList;
    private ArrayAdapter<String> arrayAdapter;

    //endregion


    //region Override Methods
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_search);

        this.listView = findViewById(R.id.searchUserListView);
        this.itemsList = FirebaseHelper.GetListFromDatabase("profiles", "profileName");


        this.arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, itemsList);
        this.listView.setAdapter(arrayAdapter);
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

                arrayAdapter.getFilter().filter(newText);

                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }
    //endregion

}
