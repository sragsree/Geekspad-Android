package com.example.sreer.geekspad.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.example.sreer.geekspad.R;
import com.example.sreer.geekspad.model.User;

import java.util.List;

/**
 * Created by kalirajkalimuthu on 4/10/17.
 */

public class ChatUsersRecyclerAdapter extends RecyclerView.Adapter<ChatUsersRecyclerAdapter.myChatUsersHolder> {

    public List<User> usersList;

    public static class myChatUsersHolder extends RecyclerView.ViewHolder {
        TextView fullname, email;

        public myChatUsersHolder(View view){
            super(view);
            fullname = (TextView) view.findViewById(R.id.fullname);
            email = (TextView) view.findViewById(R.id.locationdetail);

        }
    }

    public User getChatUser(int position) {
        return usersList.get(position);
    }



    public ChatUsersRecyclerAdapter(List<User> users){
        this.usersList = users;
    }

    @Override
    public ChatUsersRecyclerAdapter.myChatUsersHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_detail_row, parent, false);

        return new ChatUsersRecyclerAdapter.myChatUsersHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ChatUsersRecyclerAdapter.myChatUsersHolder holder, int position) {
        User user = usersList.get(position);
        holder.fullname.setText(user.getFirstname()+" " +user.getLastname());
        holder.email.setText(user.getEmail());
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

}
