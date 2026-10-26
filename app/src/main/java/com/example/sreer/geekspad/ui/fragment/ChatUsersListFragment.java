package com.example.sreer.geekspad.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.example.sreer.geekspad.R;
import com.example.sreer.geekspad.db.FireBaseHelper;
import com.example.sreer.geekspad.model.User;
import com.example.sreer.geekspad.ui.activity.ChatActivity;
import com.example.sreer.geekspad.ui.adapter.ChatUsersRecyclerAdapter;
import com.example.sreer.geekspad.utils.Constants;
import com.example.sreer.geekspad.utils.ItemClickSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kalirajkalimuthu on 4/10/17.
 */

public class ChatUsersListFragment extends Fragment implements ItemClickSupport.OnItemClickListener, FireBaseHelper.GetAllUsersInterface {

    private RecyclerView chatUsersRecyclerView;
    private ChatUsersRecyclerAdapter mChatUsersRecyclerAdapter;
    private List<User>  chatUsersList = new ArrayList<>();
    private FireBaseHelper fireBaseHelper;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_chat_users_list, container, false);
        chatUsersRecyclerView = (RecyclerView) fragmentView.findViewById(R.id.chat_users_recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        chatUsersRecyclerView.setLayoutManager(mLayoutManager);
        mChatUsersRecyclerAdapter = new ChatUsersRecyclerAdapter(chatUsersList);
        chatUsersRecyclerView.setAdapter(mChatUsersRecyclerAdapter);
        ItemClickSupport.addTo(chatUsersRecyclerView)
                .setOnItemClickListener(this);
        return fragmentView;
    }


    @Override
    public void onViewCreated(View view, Bundle bundle) {
        getActivity().setTitle("Chat Users");
        init();
    }

    public void init() {
       fireBaseHelper = new FireBaseHelper(ChatUsersListFragment.this);
       fireBaseHelper.getAllUsers();
    }



    @Override
    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
        User user = ((ChatUsersRecyclerAdapter) recyclerView.getAdapter()).getChatUser(position);
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra(Constants.ARG_RECEIVER, user.getFirstname()+" "+user.getLastname());
        intent.putExtra(Constants.ARG_RECEIVER_MAIL,user.getEmail());
        startActivity(intent);
    }

    public void applyFilter(String country, String state, String skill, String skill_level){
        fireBaseHelper.getFilteredUsers(country,state,skill,skill_level);
    }

    @Override
    public void onSuccessGetAllUsers(List<User> users) {
        mChatUsersRecyclerAdapter = new ChatUsersRecyclerAdapter(users);
        chatUsersRecyclerView.setAdapter(mChatUsersRecyclerAdapter);
    }

    @Override
    public void onFailureGetAllUsers() {
        if (isAdded()) android.widget.Toast.makeText(requireContext(), "People unavailable. Check backend connection.", android.widget.Toast.LENGTH_LONG).show();

    }

    @Override
    public void onSuccessGetFilteredUsers(List<User> users) {
        mChatUsersRecyclerAdapter = new ChatUsersRecyclerAdapter(users);
        chatUsersRecyclerView.setAdapter(mChatUsersRecyclerAdapter);
    }

    @Override
    public void onFailureGetFilteredUsers() {
        onFailureGetAllUsers();

    }
}
