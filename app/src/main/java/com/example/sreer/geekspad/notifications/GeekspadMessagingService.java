package com.example.sreer.geekspad.notifications;

import android.Manifest;
import android.app.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.example.sreer.geekspad.BuildConfig;
import com.example.sreer.geekspad.R;
import com.example.sreer.geekspad.ui.activity.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.*;

/** Real FCM delivery only for explicitly configured cloud builds; never simulated as push. */
public class GeekspadMessagingService extends FirebaseMessagingService {
  private static final String CHANNEL = "messages";

  public static void registerToken() {
    if (BuildConfig.USE_EMULATORS || FirebaseAuth.getInstance().getCurrentUser() == null) return;
    FirebaseMessaging.getInstance()
        .getToken()
        .addOnSuccessListener(GeekspadMessagingService::saveToken);
  }

  public static void unregisterToken(Runnable done) {
    if (BuildConfig.USE_EMULATORS || FirebaseAuth.getInstance().getUid() == null) {
      done.run();
      return;
    }
    String uid = FirebaseAuth.getInstance().getUid();
    FirebaseMessaging.getInstance()
        .getToken()
        .addOnCompleteListener(
            task -> {
              if (task.isSuccessful())
                com.example.sreer.geekspad.db.Backend.database()
                    .getReference("deviceTokens")
                    .child(uid)
                    .child(task.getResult())
                    .removeValue();
              FirebaseMessaging.getInstance()
                  .deleteToken()
                  .addOnCompleteListener(deleted -> done.run());
            });
  }

  private static void saveToken(String token) {
    String uid = FirebaseAuth.getInstance().getUid();
    if (!BuildConfig.USE_EMULATORS && uid != null)
      com.example.sreer.geekspad.db.Backend.database()
          .getReference("deviceTokens")
          .child(uid)
          .child(token)
          .setValue(true);
  }

  @Override
  public void onNewToken(String token) {
    saveToken(token);
  }

  @Override
  public void onMessageReceived(RemoteMessage message) {
    if (BuildConfig.USE_EMULATORS) return;
    if (Build.VERSION.SDK_INT >= 33
        && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) return;
    if (Build.VERSION.SDK_INT >= 26)
      getSystemService(NotificationManager.class)
          .createNotificationChannel(
              new NotificationChannel(CHANNEL, "Messages", NotificationManager.IMPORTANCE_DEFAULT));
    Intent intent = new Intent(this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    PendingIntent action =
        PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    String title =
        message.getNotification() == null
            ? message.getData().get("title")
            : message.getNotification().getTitle();
    NotificationManagerCompat.from(this)
        .notify(
            (int) System.currentTimeMillis(),
            new NotificationCompat.Builder(this, CHANNEL)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title == null ? "Geekspad message" : title)
                .setContentText("You have a new private message.")
                .setContentIntent(action)
                .setAutoCancel(true)
                .build());
  }
}
