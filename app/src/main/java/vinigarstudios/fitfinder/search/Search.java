package vinigarstudios.fitfinder.search;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import vinigarstudios.fitfinder.R;
import vinigarstudios.utility.VinigarCompatActivity;

public class Search extends VinigarCompatActivity
{

    //region Private Fields

    private ListView listView;
    private String[] placeHolderItems;
    private ArrayAdapter<String> arrayAdapter;

    //endregion


    //region Override Methods
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_search);

        this.listView = findViewById(R.id.searchUserListView);
        this.placeHolderItems = new String[]{"Test", "Test2"};

        this.arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, placeHolderItems);
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
