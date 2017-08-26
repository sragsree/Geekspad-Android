package com.example.sreer.geekspad.ui.activity;



import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.sreer.geekspad.R;
import com.example.sreer.geekspad.model.Skill;
import com.example.sreer.geekspad.model.User;
import com.example.sreer.geekspad.ui.fragment.ProfileViewFragment;
import com.example.sreer.geekspad.ui.fragment.SkillsViewFragment;
import com.example.sreer.geekspad.utils.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class SkillsActivity extends AppCompatActivity {

    private SkillsViewFragment skillsViewFragment;
    private FirebaseAuth auth;
    public ProgressDialog progressDailog;
    private boolean isEdit;
    private User editUserInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_skills);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            setTitle("Skills");
            Bundle data = getIntent().getExtras();
            isEdit = data.getBoolean("ForEdit");
            if(isEdit)
                editUserInfo = (User)data.getParcelable("user");
            FragmentManager fragments = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragments.beginTransaction();
            skillsViewFragment = new SkillsViewFragment();
            skillsViewFragment.setArguments(data);
            fragmentTransaction.add(R.id.skills_fragment, skillsViewFragment);
            fragmentTransaction.commit();

        auth = FirebaseAuth.getInstance();

        progressDailog = new ProgressDialog(this);
        progressDailog.setMessage("Loading");
        progressDailog.getWindow().
                setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        progressDailog.setCancelable(false);

        Button btn_register = (Button) findViewById(R.id.btn_register);
        if(isEdit)
            btn_register.setText("Update");

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    public void registerUser(){
        progressDailog.show();
        Bundle data = getIntent().getExtras();
        User user = (User)data.getParcelable("user");
        String email =  data.getString("email");
        String password = data.getString("password");
        addSkillstoUser(user);
        if(isEdit)
            addUserToFirebase(user);
        else
            signupUserwithFireBase(email,password,user);
    }

    public void addSkillstoUser(User user){
        List<Skill> Allskills = skillsViewFragment.getSkillSetRecyclerAdapter().getAllSkills();
        for(Skill skill: Allskills)
            user.addSkill(skill);
    }

    public boolean signupUserwithFireBase(String email, String password, final User user){

        auth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(SkillsActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            addUserToFirebase(user);
                            Log.i("User Details:", "Username and password are added to firbase");
                            //finish();
                        } else {
                            progressDailog.dismiss();
                            System.out.print(task.getException().getMessage());
                            Toast.makeText(SkillsActivity.this, "Creation User Account Failed" + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            Log.w("Authentication Failed", "User Details are not added to firbase:  "+ task.getException().getMessage());
                        }
                    }
                });
        return true;
    }


    public void addUserToFirebase(User user){
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        try {
            database.child(Constants.ARG_USERS)
                    .child(user.cleanEmailAddress())
                    .setValue(user)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                if(isEdit)
                                    goToProfile();
                                else
                                    goToHomePage();
                            } else {
                                progressDailog.dismiss();
                                Toast.makeText(getApplicationContext(), "Adding user details to firebase failed",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });

        }
        catch (Exception e){
            e.printStackTrace();
        }

    }


    public void goToHomePage(){
        Intent userView = new Intent(this, HomePageActivity.class);
        startActivity(userView);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) // Press Back Icon
        {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public void goToProfile(){
        Intent goToProfile = getIntent();
        setResult(RESULT_OK,goToProfile);
        finish();
    }

//    @Override
//    public void onBackPressed(){
//        Intent intent = new Intent(this, ProfileEditActivity.class);
//        intent.putExtra("user", editUserInfo);
//        startActivity(intent);
//        finish();
//    }

}
