package me.arblitroshani.thesisproject;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import me.arblitroshani.thesisproject.fragment.QueryFragment;
import me.arblitroshani.thesisproject.fragment.SyncFragment;
import me.arblitroshani.thesisproject.fragment.UploadFragment;

public class MeasureActivity extends AppCompatActivity {

    Fragment currentFragment = null;
    FragmentTransaction ft;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_upload:
                    replaceFragment(new UploadFragment());
                    return true;
                case R.id.navigation_query:
                    replaceFragment(new QueryFragment());
                    return true;
                case R.id.navigation_sync:
                    replaceFragment(new SyncFragment());
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measure);

        replaceFragment(new UploadFragment());

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private void replaceFragment(Fragment newFragment) {
        ft = getSupportFragmentManager().beginTransaction();
        currentFragment = newFragment;
        ft.replace(R.id.content, currentFragment);
        ft.commit();
    }

}
