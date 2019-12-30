package roberta.heartbeepapp.activities;

import android.content.Intent;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import roberta.heartbeepapp.R;
import roberta.heartbeepapp.utilities.Utils;
import roberta.heartbeepapp.adapters.WearableViewPagerAdapter;

public class MainActivity extends AppCompatActivity {

    private WearableViewPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("UserId", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());

        Utils.getWeekStart(LocalDate.now());

        FirebaseDatabase.getInstance().getReference("users")
                .child("phone")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .child("name")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue() != null)
                            getSupportActionBar().setTitle(dataSnapshot.getValue().toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        FirebaseDatabase.getInstance().getReference("users")
                .child("phone")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .child("wears")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Map<String, Boolean> td = (HashMap<String,Boolean>) dataSnapshot.getValue();

                        showEmptyPage(td == null || td.isEmpty());
                        if(td == null || td.isEmpty()) {
                            return;
                        }

                        ArrayList<String> wearsIdList = new ArrayList<>();
                        final ArrayList<String> wearsNameList = new ArrayList<>();

                        for(Map.Entry<String, Boolean> entry: td.entrySet()){
                            if(entry.getValue()){
                                wearsIdList.add(entry.getKey());
                            }
                        }
                        for(int i = 0; i < wearsIdList.size(); i++){
                            //subscribe for notification on all registered smart watches
                            FirebaseMessaging.getInstance().subscribeToTopic(wearsIdList.get(i));

                            FirebaseDatabase.getInstance().getReference("users")
                                    .child("wear")
                                    .child(wearsIdList.get(i))
                                    .child("name")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            wearsNameList.add(dataSnapshot.getValue().toString());
                                            adapter.notifyDataSetChanged();
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }

                        initViews(wearsIdList, wearsNameList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        FloatingActionButton addWatchBtn = findViewById(R.id.add_watch_btn);
        addWatchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddWatchActivity.class);
                startActivity(intent);
            }
        });
    }

    private void showEmptyPage(Boolean show){
        if(show) {
            findViewById(R.id.viewpager).setVisibility(View.GONE);
            findViewById(R.id.tabs).setVisibility(View.GONE);
        }else{
            findViewById(R.id.viewpager).setVisibility(View.VISIBLE);
            findViewById(R.id.tabs).setVisibility(View.VISIBLE);
        }
    }

    private void initViews(ArrayList<String> dataList, ArrayList<String> nameList) {
        ViewPager vp = findViewById(R.id.viewpager);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(vp);
        vp.setOffscreenPageLimit(0);
        adapter = new WearableViewPagerAdapter(getSupportFragmentManager(), dataList, nameList);
        vp.setAdapter(adapter);

        showEmptyPage(dataList.size() == 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.menu_logout:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            break;
        }

        return super.onOptionsItemSelected(item);
    }
}
