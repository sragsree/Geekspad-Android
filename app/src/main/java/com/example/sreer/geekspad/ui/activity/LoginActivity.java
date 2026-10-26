package com.example.sreer.geekspad.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sreer.geekspad.BuildConfig;
import com.example.sreer.geekspad.R;
import com.google.firebase.auth.FirebaseAuth;

/** Email/password authentication through Firebase, or the same API on local emulators. */
public class LoginActivity extends AppCompatActivity {
  private EditText email, password;

  @Override
  protected void onCreate(Bundle state) {
    super.onCreate(state);
    setContentView(R.layout.activity_login);
    setTitle(BuildConfig.USE_EMULATORS ? "Geekspad · Local emulator" : "Geekspad");
    email = findViewById(R.id.username);
    password = findViewById(R.id.password);
    if (BuildConfig.USE_EMULATORS) {
      email.setText("alex@example.test");
      password.setText("reviewer123");
      ((TextView) findViewById(R.id.demo_notice))
          .setText(
              "LOCAL EMULATOR · Seeded accounts only. Start backend first (README). No real push"
                  + " notifications or Maps without configuration.");
    }
    findViewById(R.id.login).setOnClickListener(v -> login());
    findViewById(R.id.link_signup)
        .setOnClickListener(v -> startActivity(new Intent(this, SignupActivity.class)));
    password.setOnEditorActionListener(
        (v, action, event) -> {
          login();
          return true;
        });
  }

  private void login() {
    String address = email.getText().toString().trim(), secret = password.getText().toString();
    if (!Patterns.EMAIL_ADDRESS.matcher(address).matches()) {
      email.setError("Enter a valid email");
      return;
    }
    if (secret.isEmpty()) {
      password.setError("Enter your password");
      return;
    }
    findViewById(R.id.login).setEnabled(false);
    findViewById(R.id.login_progress).setVisibility(View.VISIBLE);
    FirebaseAuth.getInstance()
        .signInWithEmailAndPassword(address, secret)
        .addOnCompleteListener(
            this,
            task -> {
              findViewById(R.id.login).setEnabled(true);
              findViewById(R.id.login_progress).setVisibility(View.GONE);
              if (task.isSuccessful()) {
                startActivity(new Intent(this, HomePageActivity.class));
                finish();
              } else
                Toast.makeText(
                        this,
                        "Sign-in failed. Check credentials and backend connectivity.",
                        Toast.LENGTH_LONG)
                    .show();
            });
  }
}
