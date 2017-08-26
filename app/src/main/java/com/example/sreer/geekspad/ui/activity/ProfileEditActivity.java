package com.example.sreer.geekspad.ui.activity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.sreer.geekspad.R;
import com.example.sreer.geekspad.ui.fragment.ProfileEditFragment;

public class ProfileEditActivity extends AppCompatActivity {

    private Button mEditSkills;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);
        FragmentManager fragments = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragments.beginTransaction();
        ProfileEditFragment profileEdit = new ProfileEditFragment();
        fragmentTransaction.replace(R.id.profileEdit, profileEdit);
        fragmentTransaction.commit();

    }
}
