package com.example.sreer.geekspad.model;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by kalirajkalimuthu on 4/8/17.
 */

@IgnoreExtraProperties
public class Chat {
    public String sender;
    public String senderMail;
    public String receiver;
    public String receiverMail;
    public String message;
    public long timestamp;

    public Chat() {
    }

    public Chat(String sender,String receiver, String senderMail, String receiverMail, String message, long timestamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.senderMail = senderMail;
        this.receiverMail = receiverMail;
        this.message = message;
        this.timestamp = timestamp;
    }


}