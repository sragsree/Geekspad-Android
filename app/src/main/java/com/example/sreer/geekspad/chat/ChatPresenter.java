package com.example.sreer.geekspad.chat;

import android.content.Context;

import com.example.sreer.geekspad.model.Chat;


/**
 * Created by kalirajkalimuthu on 4/8/17.
 */

public class ChatPresenter implements ChatContract.Presenter, ChatContract.OnSendMessageListener,
        ChatContract.OnGetMessagesListener {
    private ChatContract.View mView;
    private ChatInteractor mChatInteractor;

    public ChatPresenter(ChatContract.View view) {
        this.mView = view;
        mChatInteractor = new ChatInteractor(this, this);
    }

    @Override
    public void sendMessage(Context context, Chat chat) {
        mChatInteractor.sendMessageToFirebaseUser(context, chat);
    }

    @Override
    public void getMessage(String senderEmail, String receiverEmail) {
        mChatInteractor.getMessageFromFirebaseUser(senderEmail, receiverEmail);
    }

    @Override
    public void onSendMessageSuccess() {
        mView.onSendMessageSuccess();
    }

    @Override
    public void onSendMessageFailure(String message) {
        mView.onSendMessageFailure(message);
    }

    @Override
    public void onGetMessagesSuccess(Chat chat) {
        mView.onGetMessagesSuccess(chat);
    }

    @Override
    public void onGetMessagesFailure(String message) {
        mView.onGetMessagesFailure(message);
    }
}
