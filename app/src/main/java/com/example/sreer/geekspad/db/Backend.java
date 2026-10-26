package com.example.sreer.geekspad.db;

import com.example.sreer.geekspad.BuildConfig;
import com.google.firebase.database.FirebaseDatabase;

/** Keeps one configured instance so subsequent screens cannot bypass emulator routing. */
public final class Backend {
    private static FirebaseDatabase database;
    private Backend() {}
    public static synchronized void initialize() {
        if (database != null) return;
        database = FirebaseDatabase.getInstance();
        if (BuildConfig.USE_EMULATORS) database.useEmulator(BuildConfig.EMULATOR_HOST, 9000);
    }
    public static FirebaseDatabase database() {
        if (database == null) throw new IllegalStateException("Backend must be initialized by the application");
        return database;
    }
}
