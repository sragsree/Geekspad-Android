package com.example.sreer.geekspad.ui.fragment;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sreer.geekspad.R;
import com.example.sreer.geekspad.chat.ChatContract;
import com.example.sreer.geekspad.chat.ChatPresenter;
import com.example.sreer.geekspad.db.FireBaseHelper;
import com.example.sreer.geekspad.model.Chat;
import com.example.sreer.geekspad.model.User;
import com.example.sreer.geekspad.ui.adapter.ChatRecyclerAdapter;
import com.example.sreer.geekspad.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;


/**
 * Created by kalirajkalimuthu on 4/8/17.
 */

public class ChatFragment extends Fragment implements ChatContract.View, TextView.OnEditorActionListener, FireBaseHelper.GetUserByMailInterface {
    private RecyclerView mRecyclerViewChat;
    private EditText mETxtMessage;

    private ProgressDialog mProgressDialog;

    private ChatRecyclerAdapter mChatRecyclerAdapter;

    private ChatPresenter mChatPresenter;

    private Button mSendButton;

    private String currentUser;
    private String currentUserMail;
    private FireBaseHelper fireBaseHelper;


    public static ChatFragment newInstance(String receiver,
                                           String receiverMail) {
        Bundle args = new Bundle();
        args.putString(Constants.ARG_RECEIVER, receiver);
        args.putString(Constants.ARG_RECEIVER_MAIL, receiverMail);
        ChatFragment fragment = new ChatFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_message, container, false);
        bindViews(fragmentView);
        return fragmentView;
    }

    private void bindViews(View view) {
        mRecyclerViewChat = (RecyclerView) view.findViewById(R.id.recycler_view_chat);
        mETxtMessage = (EditText) view.findViewById(R.id.messageBodyField);
        mSendButton = (Button) view.findViewById(R.id.sendButton);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle("Chat with "+getArguments().getString(Constants.ARG_RECEIVER));
        fireBaseHelper = new FireBaseHelper(ChatFragment.this);
        init();
    }

    private void init() {
        initCurrentUser();
        mETxtMessage.setOnEditorActionListener(this);

        mChatPresenter = new ChatPresenter(this);
        mChatPresenter.getMessage(FirebaseAuth.getInstance().getCurrentUser().getEmail(),
                getArguments().getString(Constants.ARG_RECEIVER_MAIL));
        mSendButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        mRecyclerViewChat.setLayoutManager(mLayoutManager);
        mRecyclerViewChat.setItemAnimator(new DefaultItemAnimator());

    }

    public void initCurrentUser(){
        fireBaseHelper.getUserByMail(FirebaseAuth.getInstance().getCurrentUser().getEmail());
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEND) {
            sendMessage();
            return true;
        }
        return false;
    }

    private void sendMessage() {
        String message = mETxtMessage.getText().toString().trim();
        if(message.length() <=0 )
            return;
        String receiver = getArguments().getString(Constants.ARG_RECEIVER);
        String receiverMail = getArguments().getString(Constants.ARG_RECEIVER_MAIL);
        Chat chat = new Chat(currentUser,
                receiver,
                currentUserMail,
                receiverMail,
                message,
                System.currentTimeMillis());
        mChatPresenter.sendMessage(getActivity().getApplicationContext(), chat);
    }

    @Override
    public void onSendMessageSuccess() {
        mETxtMessage.setText("");
        Toast.makeText(getActivity(), "Message sent", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSendMessageFailure(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onGetMessagesSuccess(final Chat chat) {
        if (mChatRecyclerAdapter == null) {
            mChatRecyclerAdapter = new ChatRecyclerAdapter(new ArrayList<Chat>());
            mRecyclerViewChat.setAdapter(mChatRecyclerAdapter);
        }
        mChatRecyclerAdapter.add(chat);
        mRecyclerViewChat.smoothScrollToPosition(mChatRecyclerAdapter.getItemCount() - 1);
    }

    @Override
    public void onGetMessagesFailure(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSuccessGetUserByMail(User user) {
        currentUser = user.getFirstname();
        currentUserMail = user.getEmail();
    }

    @Override
    public void onFailureGetUserByMail() {
        Toast.makeText(getActivity(), "Failed to get current user details", Toast.LENGTH_SHORT).show();
    }
}