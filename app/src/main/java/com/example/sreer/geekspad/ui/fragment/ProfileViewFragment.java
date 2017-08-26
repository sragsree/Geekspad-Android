package com.example.sreer.geekspad.ui.fragment;


import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.sreer.geekspad.R;
import com.example.sreer.geekspad.db.FireBaseHelper;
import com.example.sreer.geekspad.model.Skill;
import com.example.sreer.geekspad.model.User;
import com.example.sreer.geekspad.ui.activity.ChatActivity;
import com.example.sreer.geekspad.ui.activity.ProfileEditActivity;
import com.example.sreer.geekspad.ui.adapter.SkillSetRecyclerAdapter;
import com.example.sreer.geekspad.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;


public class ProfileViewFragment extends Fragment {
    private TextView mProfileLine1, mProfileLine2, mProfileLine3;
    private ImageView mEditIcon, mPhoneIcon , mChatIcon;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserRefDatabase;
    private ProgressDialog mProgress;
    private RecyclerView mSkills;
    private Boolean isView = false;
    private String emailKey,phone;
    private User profileOwner;
    private final static int EDIT_STATUS = 1;


    public ProfileViewFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile_view, container, false);
        mProfileLine1 = (TextView) view.findViewById(R.id.TextView_line1);
        mProfileLine2 = (TextView) view.findViewById(R.id.TextView_line2);
        mProfileLine3 = (TextView) view.findViewById(R.id.TextView_line3);
        mAuth = FirebaseAuth.getInstance();
        mEditIcon = (ImageView) view.findViewById(R.id.imageView_edit_icon);
        mUserRefDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        mProgress = new ProgressDialog(getActivity());
        mSkills = (RecyclerView) view.findViewById(R.id.recycler_profile_skills);
        mPhoneIcon = (ImageView) view.findViewById(R.id.imageView_phone_icon);
        mChatIcon = (ImageView) view.findViewById(R.id.imageView_chat_icon);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle("Profile");
        mProgress.setMessage("Loading...");
        mProgress.show();
        if(getArguments() != null){
            emailKey = getArguments().getString("email");
            isView = true;
            setupPhoneDetails();
        }
        else
            setupEdit();
        getUserInfo();
    }

    public void getUserInfo() {
        User user = new User();
        if(isView){
            user.setEmail(emailKey);
        }
        else if (mAuth.getCurrentUser() != null) {
            user.setEmail(mAuth.getCurrentUser().getEmail());
        }
        FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(user.cleanEmailAddress())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            User user = FireBaseHelper.getUserFromSnapShot(dataSnapshot);
                            mProfileLine1.setText(user.getFirstname()+" "+user.getLastname());
                            mProfileLine2.setText("Lives in "+user.getCity()+", "+user.getState()+", "+user.getCountry());
                            mProfileLine3.setText("Email: "+user.getEmail());
                            phone = user.getPhone();
                            displaySkills(user);
                            if(phone == null)
                                mPhoneIcon.setVisibility(View.INVISIBLE);
                            profileOwner = user;
                            mProgress.dismiss();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(getActivity(),"Loading Failed",Toast.LENGTH_SHORT);
                        mProgress.dismiss();
                    }
                });
    }

    public void editProfile(){
        Intent editProfile = new Intent(getActivity(), ProfileEditActivity.class);
        //startActivity(editProfile);
        startActivityForResult(editProfile,EDIT_STATUS);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDIT_STATUS) {
            switch (resultCode) {
                case RESULT_OK:
                    getUserInfo();
                    break;
                case RESULT_CANCELED:
                    break;
            }
        }
    }

    public void displaySkills(User user){
        List<Skill> skillList = new ArrayList<>();
        skillList.addAll(user.getSkills().values());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mSkills.setLayoutManager(mLayoutManager);
        SkillSetRecyclerAdapter skillSetRecyclerAdapter = new SkillSetRecyclerAdapter(skillList);
        mSkills.setAdapter(skillSetRecyclerAdapter);
    }

    public void setupPhoneDetails(){
        mPhoneIcon.setVisibility(View.VISIBLE);
        mChatIcon.setVisibility(View.VISIBLE);
        mEditIcon.setVisibility(View.INVISIBLE);
        mPhoneIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(phone!=null) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + phone));
                    startActivity(intent);
                }
                else
                    Toast.makeText(getActivity(),"Phone Details not available",Toast.LENGTH_SHORT);
            }
        });
        mChatIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra(Constants.ARG_RECEIVER, profileOwner.getFirstname()+" "+profileOwner.getLastname());
                intent.putExtra(Constants.ARG_RECEIVER_MAIL,profileOwner.getEmail());
                startActivity(intent);
            }
        });
    }

    public void setupEdit(){
        mPhoneIcon.setVisibility(View.INVISIBLE);
        mChatIcon.setVisibility(View.INVISIBLE);
        mEditIcon.setVisibility(View.VISIBLE);
        mEditIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editProfile();
            }
        });
    }

}

