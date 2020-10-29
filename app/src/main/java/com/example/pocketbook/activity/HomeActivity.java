package com.example.pocketbook.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.pocketbook.fragment.HomeFragment;
import com.example.pocketbook.fragment.AddFragment;
import com.example.pocketbook.fragment.ProfileFragment;
import com.example.pocketbook.fragment.ScanFragment;
import com.example.pocketbook.R;

import com.example.pocketbook.fragment.SearchFragment;
import com.example.pocketbook.model.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG ="MainActivity";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private StorageReference mStorageRef;
    private Button buttonUploadImg;
    private String name;
    private ImageView image;
    private ArrayList<String> pathArray;
    private int array_position;
    private FirebaseUser currentUser;
    String email;
    private User user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_layout);
        Intent intent = getIntent();

        user = (User) intent.getSerializableExtra("CURRENT_USER");
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setOnNavigationItemSelectedListener(NavListener);
        getSupportFragmentManager().beginTransaction().replace(R.id.container,new HomeFragment()).commit();

    }

    private void toastMessage(String message) {
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener NavListener =
            new BottomNavigationView.OnNavigationItemSelectedListener(){
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;
                    switch (item.getItemId()){
                        case R.id.bottom_nav_home:
                            selectedFragment = new HomeFragment();
//                            selectedFragment.setArguments(bundle);
                            break;
                        case R.id.bottom_nav_search:
                            selectedFragment = new SearchFragment();
//                            selectedFragment.setArguments(bundle);
                            break;
                        case R.id.bottom_nav_add:
                            selectedFragment = new AddFragment();
//                            selectedFragment.setArguments(bundle);
                            break;
                        case R.id.bottom_nav_scan:
                            selectedFragment = new ScanFragment();
//                            selectedFragment.setArguments(bundle);
                            break;
                        case R.id.bottom_nav_profile:
                            selectedFragment = new ProfileFragment(user);
//                            selectedFragment.setArguments(bundle);
                            break;
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.container,selectedFragment).commit();
                    return true;
                }
            };

//    public void updateUI(FirebaseUser currentUser) {
//        Intent profileIntent = new Intent(getApplicationContext(), HomeActivity.class);
//        profileIntent.putExtra("email", currentUser.getEmail());
//        Log.v("DATA", currentUser.getUid());
//        startActivity(profileIntent);
//    }
}


