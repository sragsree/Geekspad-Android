package com.example.sreer.geekspad.chat;


import android.content.Context;

import com.example.sreer.geekspad.model.Chat;


/**
 * Created by kalirajkalimuthu on 4/8/17.
 */

public interface ChatContract {
    interface View {
        void onSendMessageSuccess();

        void onSendMessageFailure(String message);

        void onGetMessagesSuccess(Chat chat);

        void onGetMessagesFailure(String message);
    }

    interface Presenter {
        void sendMessage(Context context, Chat chat);

        void getMessage(String senderMail, String receiverMail);
    }

    interface Interactor {
        void sendMessageToFirebaseUser(Context context, Chat chat);

        void getMessageFromFirebaseUser(String senderEmail, String receiverEmail);
    }

    interface OnSendMessageListener {
        void onSendMessageSuccess();

        void onSendMessageFailure(String message);
    }

    interface OnGetMessagesListener {
        void onGetMessagesSuccess(Chat chat);

        void onGetMessagesFailure(String message);
    }
}