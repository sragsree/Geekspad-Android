package com.example.sreer.geekspad.ui.activity;

import android.content.Intent;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.example.sreer.geekspad.R;
import com.example.sreer.geekspad.ui.fragment.ProfileEditFragment;
import com.example.sreer.geekspad.ui.fragment.ProfileViewFragment;

public class ProfileActivity extends AppCompatActivity {
    private Bundle data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        data = getIntent().getExtras();
        FragmentManager fragments = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragments.beginTransaction();
        ProfileViewFragment profileView = new ProfileViewFragment();
        if(data!=null)
            profileView.setArguments(data);
        fragmentTransaction.replace(R.id.profileFragment, profileView);
        fragmentTransaction.commit();
    }


}