package com.example.sreer.geekspad.db;

import com.example.sreer.geekspad.model.Discovery;
import com.example.sreer.geekspad.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;

/** All discovery uses UID-owned profiles; email is a lookup value, never a database key. */
public class FireBaseHelper {
  private GetAllUsersInterface all;
  private GetUserByMailInterface one;

  public FireBaseHelper() {}

  public FireBaseHelper(GetAllUsersInterface callback) {
    all = callback;
  }

  public FireBaseHelper(GetUserByMailInterface callback) {
    one = callback;
  }

  public static User getUserFromSnapShot(DataSnapshot snapshot) {
    User user = snapshot.getValue(User.class);
    if (user != null) user.setUid(snapshot.getKey());
    return user;
  }

  public void getAllUsers() {
    load(null, null, null, null, false);
  }

  public void getAllUsers(String skill, String proficiency) {
    load(null, null, skill, proficiency, false);
  }

  public void getUsersByCountry(String country, String state, String skill, String proficiency) {
    load(country, state, skill, proficiency, true);
  }

  public void getFilteredUsers(String country, String state, String skill, String proficiency) {
    load(country, state, skill, proficiency, true);
  }

  private void load(
      String country, String state, String skill, String proficiency, boolean filtered) {
    com.example.sreer.geekspad.db.Backend.database()
        .getReference("users")
        .addListenerForSingleValueEvent(
            new ValueEventListener() {
              @Override
              public void onDataChange(DataSnapshot snapshot) {
                List<User> users = new ArrayList<>();
                String current = FirebaseAuth.getInstance().getUid();
                for (DataSnapshot child : snapshot.getChildren()) {
                  User user = getUserFromSnapShot(child);
                  if (user != null
                      && !child.getKey().equals(current)
                      && Discovery.matches(user, country, state, skill, proficiency))
                    users.add(user);
                }
                if (filtered) all.onSuccessGetFilteredUsers(users);
                else all.onSuccessGetAllUsers(users);
              }

              @Override
              public void onCancelled(DatabaseError error) {
                if (filtered) all.onFailureGetFilteredUsers();
                else all.onFailureGetAllUsers();
              }
            });
  }

  public void getUserByMail(String email) {
    com.example.sreer.geekspad.db.Backend.database()
        .getReference("users")
        .orderByChild("email")
        .equalTo(email)
        .limitToFirst(1)
        .addListenerForSingleValueEvent(
            new ValueEventListener() {
              @Override
              public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                  one.onSuccessGetUserByMail(getUserFromSnapShot(child));
                  return;
                }
                one.onFailureGetUserByMail();
              }

              @Override
              public void onCancelled(DatabaseError error) {
                one.onFailureGetUserByMail();
              }
            });
  }

  public interface GetAllUsersInterface {
    void onSuccessGetAllUsers(List<User> users);

    void onFailureGetAllUsers();

    void onSuccessGetFilteredUsers(List<User> users);

    void onFailureGetFilteredUsers();
  }

  public interface GetUserByMailInterface {
    void onSuccessGetUserByMail(User user);

    void onFailureGetUserByMail();
  }
}
