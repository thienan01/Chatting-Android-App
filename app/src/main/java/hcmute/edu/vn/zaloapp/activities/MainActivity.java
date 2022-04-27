package hcmute.edu.vn.zaloapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import hcmute.edu.vn.zaloapp.R;

public class MainActivity extends AppCompatActivity  {
    BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigationView = findViewById(R.id.bottomNAVView);
        configureNAVBar();
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentLayout, new MessageFragment()).commit();
    }

    private void configureNAVBar(){
        bottomNavigationView.setBackground(null);
    }
    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new
            BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;
                    switch (item.getItemId()){
                        case  R.id.miMessage:
                            selectedFragment = new MessageFragment();
                            break;
                        case R.id.miContact:
                            selectedFragment = new ContactFragment();
                            break;
                        case R.id.miDiary:
                            selectedFragment = new NewFeedFragment();
                            break;
                        case R.id.miProfile:
                            selectedFragment = new ProfileFragment();
                            break;
                    }
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragmentLayout
                                    ,selectedFragment).commit();
                    return true;
                }
            };
}