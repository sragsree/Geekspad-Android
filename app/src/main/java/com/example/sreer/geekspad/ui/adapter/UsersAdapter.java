package com.example.sreer.geekspad.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sreer.geekspad.R;
import com.example.sreer.geekspad.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

/**
 * Created by kalirajkalimuthu on 3/16/17.
 */

public class UsersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public List<User> usersList;

    public UsersAdapter(List<User> users){
          this.usersList = users;
    }


    public UsersAdapter(List<User> users, RecyclerView recyclerView) {

        this.usersList = users;
    }

    public List<User> getUsersList(){
        return this.usersList;
    }


    public User getUser(int position) {
        return usersList.get(position);
    }



    @Override
    public  RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
           View  itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.user_detail_row, parent, false);
            viewHolder = new myUsersHolder(itemView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        myUsersHolder userHolder = (myUsersHolder) holder;
        User user = usersList.get(position);
        userHolder.nickname.setText(user.getFirstname() + " " + user.getLastname());
        userHolder.locationdetail.setText(user.getCountry() + " | " + user.getState() + " | " + user.getCity());

    }
    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public void add(User user) {
        usersList.add(user);
        notifyItemInserted(usersList.size() - 1);
    }

    public void clear(){
        usersList.clear();
        notifyDataSetChanged();
    }

    public void remove(){
        usersList.remove(usersList.size() - 1);
        notifyItemRemoved(usersList.size());
    }

    public static class myUsersHolder extends RecyclerView.ViewHolder {
        TextView nickname, locationdetail;

        public myUsersHolder(View view){
            super(view);
            nickname = (TextView) view.findViewById(R.id.fullname);
            locationdetail = (TextView) view.findViewById(R.id.locationdetail);
        }

    }

}
