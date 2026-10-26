package com.example.sreer.geekspad;

import android.app.Application;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

/** Explicit configuration prevents accidental connections to the historical Firebase project. */
public class GeekspadApplication extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    FirebaseOptions options =
        new FirebaseOptions.Builder()
            .setProjectId(BuildConfig.FIREBASE_PROJECT)
            .setApplicationId(BuildConfig.FIREBASE_APP_ID)
            .setApiKey(BuildConfig.FIREBASE_API_KEY)
            .setDatabaseUrl(BuildConfig.FIREBASE_URL)
            .setGcmSenderId(BuildConfig.FIREBASE_SENDER_ID)
            .build();
    FirebaseApp.initializeApp(this, options);
    com.example.sreer.geekspad.db.Backend.initialize();
    if (BuildConfig.USE_EMULATORS) {
      FirebaseAuth.getInstance().useEmulator(BuildConfig.EMULATOR_HOST, 9099);
    }
    FirebaseMessaging.getInstance().setAutoInitEnabled(!BuildConfig.USE_EMULATORS);
  }
}
