package com.example.sreer.geekspad.db;

import android.widget.Toast;

import com.example.sreer.geekspad.model.Skill;
import com.example.sreer.geekspad.model.User;
import com.example.sreer.geekspad.ui.fragment.UsersListViewFragment;
import com.example.sreer.geekspad.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sreer on 03-05-2017.
 */

public class FireBaseHelper {
    private  GetAllUsersInterface getAllUsersInterface;
    private GetUserByMailInterface getUserByMailInterface;

    public FireBaseHelper(){

    }

    public FireBaseHelper(GetAllUsersInterface getAllUsersInterface){
        this.getAllUsersInterface = getAllUsersInterface;
    }

    public FireBaseHelper(GetUserByMailInterface getUserByMailInterface){
        this.getUserByMailInterface =getUserByMailInterface;
    }


    public  static  User getUserFromSnapShot(DataSnapshot dataSnapshot){

        User user = new User();
        if(dataSnapshot.hasChild("skills")) {
            DataSnapshot skillSnapshot = dataSnapshot.child("skills");
            for (DataSnapshot skillItem : skillSnapshot.getChildren()) {
                Skill skill = skillItem.getValue(Skill.class);
                user.addSkill(skill);
            }
            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                String dataKey = userSnapshot.getKey();
                if (dataKey.equals("firstname"))
                    user.setFirstname(String.valueOf(userSnapshot.getValue()));
                if (dataKey.equals("lastname"))
                    user.setLastname(String.valueOf(userSnapshot.getValue()));
                if (dataKey.equals("email"))
                    user.setEmail(String.valueOf(userSnapshot.getValue()));
                if (dataKey.equals("phone"))
                    user.setPhone(String.valueOf(userSnapshot.getValue()));
                if (dataKey.equals("birthDate"))
                    user.setBirthDate(String.valueOf(userSnapshot.getValue()));
                if (dataKey.equals("country"))
                    user.setCountry(String.valueOf(userSnapshot.getValue()));
                if (dataKey.equals("state"))
                    user.setState(String.valueOf(userSnapshot.getValue()));
                if (dataKey.equals("city"))
                    user.setCity(String.valueOf(userSnapshot.getValue()));
                if (dataKey.equals("latitude"))
                    user.setLatitude(String.valueOf(userSnapshot.getValue()));
                if (dataKey.equals("longitude"))
                    user.setLongitude(String.valueOf(userSnapshot.getValue()));
            }
        }
        else {
            user = dataSnapshot.getValue(User.class);
        }
        return  user;
    }


    public void  getAllUsers(){
        getAllUsers(null,null);
    }

    public  void getAllUsers(final String skill, final String proficiency){

        FirebaseDatabase.getInstance().getReference().child(Constants.ARG_USERS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<User> allUsers = new ArrayList<User>();
                if (dataSnapshot.getValue() != null) {
                   for(DataSnapshot userDataSnapshot: dataSnapshot.getChildren()) {
                       User user = getUserFromSnapShot(userDataSnapshot);
                       if(!FirebaseAuth.getInstance().getCurrentUser().getEmail().equals(user.getEmail())) {
                           if(skill != null){
                               Skill userSkill = user.getSkill(skill);
                               if(proficiency != null) {
                                   if (userSkill != null && userSkill.proficency.equals(proficiency))
                                       allUsers.add(user);
                               }
                               else if(userSkill != null)
                                   allUsers.add(user);
                           }
                           else
                               allUsers.add(user);

                       }
                   }
                    getAllUsersInterface.onSuccessGetAllUsers(allUsers);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
               getAllUsersInterface.onFailureGetAllUsers();
            }
        });

    }


    public void getUsersByCountry(String country, final String state, final String skill, final String proficiency){

        FirebaseDatabase.getInstance().getReference().child(Constants.ARG_USERS).orderByChild("country").equalTo(country).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<User> allUsers = new ArrayList<User>();
                if (dataSnapshot.getValue() != null) {
                    for(DataSnapshot userDataSnapshot: dataSnapshot.getChildren()) {
                        User user = getUserFromSnapShot(userDataSnapshot);
                        if (state != null && user.getState().equals(state)) {
                            if (!FirebaseAuth.getInstance().getCurrentUser().getEmail().equals(user.getEmail())) {
                                if (skill != null) {
                                    Skill userSkill = user.getSkill(skill);
                                    if (proficiency != null) {
                                        if (userSkill != null && userSkill.proficency.equals(proficiency))
                                            allUsers.add(user);
                                    } else if (userSkill != null)
                                        allUsers.add(user);
                                } else
                                    allUsers.add(user);
                            }
                        }
                        else if (state ==  null){
                            if (!FirebaseAuth.getInstance().getCurrentUser().getEmail().equals(user.getEmail())) {
                                if (skill != null) {
                                    Skill userSkill = user.getSkill(skill);
                                    if (proficiency != null) {
                                        if (userSkill != null && userSkill.proficency.equals(proficiency))
                                            allUsers.add(user);
                                    } else if (userSkill != null)
                                        allUsers.add(user);
                                } else
                                    allUsers.add(user);
                            }
                        }
                    }

                }
                getAllUsersInterface.onSuccessGetFilteredUsers(allUsers);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
               getAllUsersInterface.onFailureGetFilteredUsers();
            }
        });

    }



    public void getFilteredUsers(String country, String state, String skill, String proficiency){
            if(country != null)
            getUsersByCountry(country,state,skill,proficiency);
            else if(country == null && state == null && skill != null)
                getAllUsers(skill, proficiency);
    }

    public void getUserByMail(String email) {
        String mail = email.replaceAll("\\.", "-");
        FirebaseDatabase.getInstance().getReference().
                child(Constants.ARG_USERS)
                .child(mail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    User user = getUserFromSnapShot(dataSnapshot);
                    getUserByMailInterface.onSuccessGetUserByMail(user);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                getUserByMailInterface.onFailureGetUserByMail();
            }
        });
    }

    /**
     * Created by kalirajkalimuthu on 5/3/17.
     */

    public static interface GetAllUsersInterface {
        public void onSuccessGetAllUsers(List<User> users);
        public void onFailureGetAllUsers();
        public void onSuccessGetFilteredUsers(List<User> users);
        public void onFailureGetFilteredUsers();
    }

    public static interface GetUserByMailInterface{
        public void onSuccessGetUserByMail(User user);
        public void onFailureGetUserByMail();
    }



/*
    public void initCurrentUser(String country, String state){
        FirebaseDatabase.getInstance().getReference().
                child(Constants.ARG_USERS)
                .orderByChild("country")
                .equalTo(country).orderByChild("state").equalTo(state).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {

                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

  */

}
