package roberta.heartbeepapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

import roberta.heartbeepapp.adapters.AddWatchListAdapter;
import roberta.heartbeepapp.R;
import roberta.heartbeepapp.models.WatchEntity;

public class AddWatchActivity extends AppCompatActivity {

    AddWatchListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_watch);

        getSupportActionBar().setTitle("Add watch");

        RecyclerView recycler = findViewById(R.id.recycler);
        adapter = new AddWatchListAdapter();
        recycler.setAdapter(adapter);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        FirebaseDatabase.getInstance().getReference("users")
                .child("phone")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("wears")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Map<String, Boolean> td = (Map<String,Boolean>) dataSnapshot.getValue();
                        ArrayList<String> currentWatchList = new ArrayList<>();
                        if(td != null){
                            currentWatchList.addAll(td.keySet());
                        }

                        requestAvailableWatches(currentWatchList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void requestAvailableWatches(final ArrayList<String> currentWatchList) {
        FirebaseDatabase.getInstance().getReference("users")
                .child("wear")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Map<String, Object> td = (Map<String,Object>) dataSnapshot.getValue();

                        showEmptyPage(td == null || td.isEmpty());
                        if(td == null || td.isEmpty()){
                            return;
                        }

                        ArrayList<WatchEntity> watchList = new ArrayList<>();

                        for(Map.Entry<String, Object>entry: td.entrySet()){
                            if(!currentWatchList.contains(entry.getKey())) {
                                Map user = (Map) entry.getValue();
                                watchList.add(new WatchEntity(entry.getKey(), (String) user.get("name")));
                            }
                        }


                        adapter.setData(watchList);
                        showEmptyPage(watchList.size() == 0);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void showEmptyPage(Boolean show){
        if(show){
            findViewById(R.id.recycler).setVisibility(View.GONE);
        }else{
            findViewById(R.id.recycler).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
