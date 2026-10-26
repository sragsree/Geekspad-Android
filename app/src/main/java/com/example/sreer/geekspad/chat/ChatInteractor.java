package com.example.sreer.geekspad.chat;

import android.content.Context;
import com.example.sreer.geekspad.db.FireBaseHelper;
import com.example.sreer.geekspad.model.Chat;
import com.example.sreer.geekspad.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.*;

/** One deterministic UID room, acknowledged writes, private reads, and detachable listeners. */
public class ChatInteractor implements ChatContract.Interactor {
  private final ChatContract.OnSendMessageListener sends;
  private final ChatContract.OnGetMessagesListener reads;
  private DatabaseReference room;
  private Query messages;
  private ChildEventListener listener;
  private String peerUid;
  private boolean closed;

  public ChatInteractor(
      ChatContract.OnSendMessageListener sends, ChatContract.OnGetMessagesListener reads) {
    this.sends = sends;
    this.reads = reads;
  }

  public static String roomId(String a, String b) {
    return a.compareTo(b) < 0 ? a + "_" + b : b + "_" + a;
  }

  @Override
  public void getMessageFromFirebaseUser(String sender, String receiver) {
    closed = false;
    new FireBaseHelper(
            new FireBaseHelper.GetUserByMailInterface() {
              @Override
              public void onSuccessGetUserByMail(User peer) {
                if (closed) return;
                peerUid = peer.getUid();
                String current = FirebaseAuth.getInstance().getUid();
                room =
                    com.example.sreer.geekspad.db.Backend.database()
                        .getReference("rooms")
                        .child(roomId(current, peerUid));
                reads.onConversationReady();
                messages = room.child("messages").orderByChild("timestamp").limitToLast(200);
                listener =
                    messages.addChildEventListener(
                        new ChildEventListener() {
                          @Override
                          public void onChildAdded(DataSnapshot snapshot, String previous) {
                            Chat chat = snapshot.getValue(Chat.class);
                            if (!closed && chat != null) reads.onGetMessagesSuccess(chat);
                          }

                          @Override
                          public void onChildChanged(DataSnapshot s, String p) {}

                          @Override
                          public void onChildRemoved(DataSnapshot s) {}

                          @Override
                          public void onChildMoved(DataSnapshot s, String p) {}

                          @Override
                          public void onCancelled(DatabaseError error) {
                            if (!closed)
                              reads.onGetMessagesFailure(
                                  "Unable to load messages: " + error.getMessage());
                          }
                        });
              }

              @Override
              public void onFailureGetUserByMail() {
                if (!closed) reads.onGetMessagesFailure("Recipient unavailable");
              }
            })
        .getUserByMail(receiver);
  }

  @Override
  public void sendMessageToFirebaseUser(Context context, Chat chat) {
    if (room == null || peerUid == null || closed) {
      sends.onSendMessageFailure("Conversation is still loading");
      return;
    }
    if (chat.message == null || chat.message.trim().isEmpty() || chat.message.length() > 4000) {
      sends.onSendMessageFailure("Use 1–4000 characters");
      return;
    }
    String uid = FirebaseAuth.getInstance().getUid();
    chat.senderUid = uid;
    chat.receiverUid = peerUid;
    Map<String, Object> changes = new HashMap<>();
    changes.put("first", uid.compareTo(peerUid) < 0 ? uid : peerUid);
    changes.put("second", uid.compareTo(peerUid) < 0 ? peerUid : uid);
    changes.put("members/" + uid, true);
    changes.put("members/" + peerUid, true);
    changes.put("messages/" + room.child("messages").push().getKey(), chat);
    room.updateChildren(changes)
        .addOnCompleteListener(
            task -> {
              if (closed) return;
              if (task.isSuccessful()) sends.onSendMessageSuccess();
              else sends.onSendMessageFailure("Message was not saved. Retry when connected.");
            });
  }

  public void close() {
    closed = true;
    if (listener != null && messages != null) messages.removeEventListener(listener);
  }
}
