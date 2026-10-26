package com.example.sreer.geekspad.ui.activity;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.*;

/** Peer ratings are separate from the profile's self-declared proficiency. */
public class RatingsActivity extends AppCompatActivity {
  private String subject;
  private LinearLayout list;
  private DatabaseReference ratings;
  private ValueEventListener listener;

  @Override
  protected void onCreate(Bundle saved) {
    super.onCreate(saved);
    subject = getIntent().getStringExtra("uid");
    if (subject == null || FirebaseAuth.getInstance().getCurrentUser() == null) {
      finish();
      return;
    }
    setTitle("Peer ratings · " + getIntent().getStringExtra("name"));
    ScrollView scroll = new ScrollView(this);
    list = new LinearLayout(this);
    list.setOrientation(LinearLayout.VERTICAL);
    list.setPadding(24, 16, 24, 24);
    scroll.addView(list);
    setContentView(scroll);
    ratings = com.example.sreer.geekspad.db.Backend.database().getReference("ratings").child(subject);
  }

  @Override
  protected void onStart() {
    super.onStart();
    if (ratings == null) return;
    listener =
        ratings.addValueEventListener(
            new ValueEventListener() {
              @Override
              public void onDataChange(DataSnapshot snapshot) {
                list.removeAllViews();
                text(
                    "Peer feedback (1–5). Separate from self-declared skill percentages. One rating"
                        + " per person and skill; resubmitting updates your rating.");
                if (!subject.equals(FirebaseAuth.getInstance().getUid())) {
                  Button add = new Button(RatingsActivity.this);
                  add.setText("Rate a skill");
                  add.setOnClickListener(v -> chooseSkill());
                  list.addView(add);
                }
                Map<String, double[]> aggregates = new TreeMap<>();
                for (DataSnapshot peer : snapshot.getChildren())
                  for (DataSnapshot rating : peer.getChildren()) {
                    String skill = rating.child("skill").getValue(String.class);
                    Number score = (Number) rating.child("score").getValue();
                    if (skill == null || score == null) continue;
                    double[] sum = aggregates.get(skill);
                    if (sum == null) {
                      sum = new double[2];
                      aggregates.put(skill, sum);
                    }
                    sum[0] += score.doubleValue();
                    sum[1]++;
                  }
                if (aggregates.isEmpty()) text("No peer ratings yet.");
                for (Map.Entry<String, double[]> entry : aggregates.entrySet())
                  text(
                      String.format(
                          Locale.US,
                          "%s · %.1f/5 (%d reviews)",
                          entry.getKey(),
                          entry.getValue()[0] / entry.getValue()[1],
                          (int) entry.getValue()[1]));
                for (DataSnapshot peer : snapshot.getChildren())
                  for (DataSnapshot rating : peer.getChildren())
                    text(
                        rating.child("skill").getValue()
                            + " · "
                            + rating.child("score").getValue()
                            + "/5\n"
                            + rating.child("context").getValue());
              }

              @Override
              public void onCancelled(DatabaseError error) {
                toast("Ratings unavailable");
              }
            });
  }

  @Override
  protected void onStop() {
    if (listener != null) ratings.removeEventListener(listener);
    super.onStop();
  }

  private void chooseSkill() {
    com.example.sreer.geekspad.db.Backend.database()
        .getReference("users")
        .child(subject)
        .child("skills")
        .get()
        .addOnCompleteListener(
            task -> {
              if (!task.isSuccessful()) {
                toast("Skills unavailable");
                return;
              }
              List<String> names = new ArrayList<>();
              for (DataSnapshot s : task.getResult().getChildren()) names.add(s.getKey());
              if (names.isEmpty()) {
                toast("This profile has not listed any skills");
                return;
              }
              new AlertDialog.Builder(this)
                  .setTitle("Choose a listed skill")
                  .setItems(names.toArray(new String[0]), (d, which) -> rate(names.get(which)))
                  .show();
            });
  }

  private void rate(String skill) {
    LinearLayout form = new LinearLayout(this);
    form.setOrientation(LinearLayout.VERTICAL);
    form.setPadding(24, 8, 24, 8);
    RatingBar stars = new RatingBar(this);
    stars.setNumStars(5);
    stars.setStepSize(1);
    stars.setRating(3);
    form.addView(stars);
    EditText context = new EditText(this);
    context.setHint("Where did you work together? Give concrete feedback.");
    form.addView(context);
    AlertDialog dialog =
        new AlertDialog.Builder(this)
            .setTitle(skill)
            .setView(form)
            .setNegativeButton("Close", null)
            .setPositiveButton("Save rating", null)
            .create();
    dialog.setOnShowListener(
        d ->
            dialog
                .getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(
                    v -> {
                      String detail = context.getText().toString().trim();
                      if (detail.length() < 10 || detail.length() > 500 || stars.getRating() < 1) {
                        context.setError("Use 10–500 characters and at least one star");
                        return;
                      }
                      Map<String, Object> value = new HashMap<>();
                      value.put("skill", skill);
                      value.put("score", (int) stars.getRating());
                      value.put("context", detail);
                      value.put("updatedAt", ServerValue.TIMESTAMP);
                      dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                      ratings
                          .child(FirebaseAuth.getInstance().getUid())
                          .child(skill)
                          .setValue(value)
                          .addOnCompleteListener(
                              task -> {
                                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                                if (task.isSuccessful()) dialog.dismiss();
                                else toast("Rating failed. Please retry.");
                              });
                    }));
    dialog.show();
  }

  private void text(String value) {
    TextView view = new TextView(this);
    view.setText(value);
    view.setTextSize(16);
    view.setPadding(0, 12, 0, 12);
    list.addView(view);
  }

  private void toast(String value) {
    Toast.makeText(this, value, Toast.LENGTH_LONG).show();
  }
}
