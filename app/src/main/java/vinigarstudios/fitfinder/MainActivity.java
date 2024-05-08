package vinigarstudios.fitfinder;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import vinigarstudios.fitfinder.adapter.PostAdapter;
import vinigarstudios.fitfinder.models.PostsModel;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<PostsModel> postsList;
    private FirebaseFirestore db;

    private static final int ORDER_BY_TIME = 0;
    private static final int ORDER_BY_LIKES = 1;

    private int currentOrder = ORDER_BY_TIME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize PostAdapter
        postAdapter = new PostAdapter(postsList, db);
        recyclerView.setAdapter(postAdapter);

        // Fetch and display posts from Firestore
        fetchPostsFromFirestore();

        // Set up bottom navigation
        setupBottomNavigation();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_order_by_time) {
            currentOrder = ORDER_BY_TIME;
            fetchPostsFromFirestore();
            return true;
        } else if (itemId == R.id.action_order_by_likes) {
            currentOrder = ORDER_BY_LIKES;
            fetchPostsFromFirestore();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void fetchPostsFromFirestore() {
        Query query;
        if (currentOrder == ORDER_BY_TIME) {
            query = db.collection("posts").orderBy("timestamp", Query.Direction.DESCENDING);
        } else {
            query = db.collection("posts").orderBy("likes", Query.Direction.DESCENDING);
        }

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    postsList = new ArrayList<>();
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        PostsModel post = documentSnapshot.toObject(PostsModel.class);
                        postsList.add(post);
                    }
                    postAdapter.setPostsList(postsList);
                })
                .addOnFailureListener(e -> {
                    // Handle error
                    Toast.makeText(MainActivity.this, "Failed to fetch posts", Toast.LENGTH_SHORT).show();
                });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.bottom_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.bottom_home) {
                return true;
            } else if (item.getItemId() == R.id.bottom_friends) {
                startActivity(new Intent(getApplicationContext(), FriendsActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
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
}

