package com.example.sreer.geekspad.ui.fragment;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.example.sreer.geekspad.model.Meetup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.text.SimpleDateFormat;
import java.util.*;

/** Shared, persistent meetup creation, editing, cancellation, and RSVP. */
public class EventsFragment extends Fragment {
  private LinearLayout list;
  private Query query;
  private ValueEventListener listener;
  private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle saved) {
    format.setLenient(false);
    ScrollView scroll = new ScrollView(requireContext());
    list = new LinearLayout(requireContext());
    list.setOrientation(LinearLayout.VERTICAL);
    list.setPadding(24, 16, 24, 32);
    scroll.addView(list);
    return scroll;
  }

  @Override
  public void onStart() {
    super.onStart();
    requireActivity().setTitle("Meetups & events");
    query =
        com.example.sreer.geekspad.db.Backend.database()
            .getReference("events")
            .orderByChild("startsAt")
            .limitToLast(100);
    listener =
        query.addValueEventListener(
            new ValueEventListener() {
              @Override
              public void onDataChange(DataSnapshot snapshot) {
                render(snapshot);
              }

              @Override
              public void onCancelled(DatabaseError error) {
                if (isAdded()) toast("Events unavailable: " + error.getMessage());
              }
            });
  }

  @Override
  public void onStop() {
    if (listener != null) query.removeEventListener(listener);
    super.onStop();
  }

  private void render(DataSnapshot snapshot) {
    if (!isAdded()) return;
    list.removeAllViews();
    Button create = new Button(requireContext());
    create.setText("Organize a meetup");
    create.setOnClickListener(v -> edit(null, null));
    list.addView(create);
    if (!snapshot.exists()) text(list, "No meetups yet. Create the first one.");
    for (DataSnapshot item : snapshot.getChildren()) {
      Meetup event = item.getValue(Meetup.class);
      if (event == null) continue;
      LinearLayout card = new LinearLayout(requireContext());
      card.setOrientation(LinearLayout.VERTICAL);
      card.setPadding(0, 24, 0, 24);
      list.addView(card);
      TextView heading = text(card, event.title + (event.cancelled ? " · CANCELLED" : ""));
      heading.setTextSize(21);
      text(card, format.format(new Date(event.startsAt)) + " · " + event.location);
      text(card, event.description == null ? "" : event.description);
      String uid = FirebaseAuth.getInstance().getUid(), id = item.getKey();
      Button rsvp = new Button(requireContext());
      rsvp.setText("Loading RSVP…");
      rsvp.setEnabled(false);
      card.addView(rsvp);
      DatabaseReference attendance =
          com.example.sreer.geekspad.db.Backend.database().getReference("attendance").child(id);
      attendance
          .get()
          .addOnCompleteListener(
              task -> {
                if (!isAdded()) return;
                if (!task.isSuccessful()) {
                  rsvp.setText("RSVP unavailable");
                  return;
                }
                boolean joined = task.getResult().child(uid).exists();
                text(card, task.getResult().getChildrenCount() + " attending");
                rsvp.setText(joined ? "Cancel my RSVP" : "RSVP to attend");
                rsvp.setEnabled(!event.cancelled && event.startsAt > System.currentTimeMillis());
                rsvp.setOnClickListener(
                    v -> {
                      rsvp.setEnabled(false);
                      attendance
                          .child(uid)
                          .setValue(joined ? null : true)
                          .addOnCompleteListener(
                              result -> {
                                if (!isAdded()) return;
                                if (!result.isSuccessful()) toast("RSVP failed. Please retry.");
                                else toast(joined ? "RSVP cancelled" : "RSVP saved");
                                query.get().addOnSuccessListener(this::render);
                              });
                    });
              });
      if (uid.equals(event.ownerId) && !event.cancelled) {
        Button edit = new Button(requireContext());
        edit.setText("Edit event");
        edit.setOnClickListener(v -> edit(id, event));
        card.addView(edit);
        Button cancel = new Button(requireContext());
        cancel.setText("Cancel event");
        card.addView(cancel);
        cancel.setOnClickListener(
            v ->
                new AlertDialog.Builder(requireContext())
                    .setMessage("Cancel this meetup for everyone?")
                    .setNegativeButton("Keep event", null)
                    .setPositiveButton(
                        "Cancel event",
                        (d, w) ->
                            com.example.sreer.geekspad.db.Backend.database()
                                .getReference("events")
                                .child(id)
                                .child("cancelled")
                                .setValue(true)
                                .addOnFailureListener(e -> toast("Cancellation failed")))
                    .show());
      }
    }
  }

  private void edit(String id, Meetup current) {
    LinearLayout form = new LinearLayout(requireContext());
    form.setOrientation(LinearLayout.VERTICAL);
    form.setPadding(24, 8, 24, 8);
    EditText title = field(form, "Title", current == null ? "" : current.title);
    EditText location =
        field(form, "Location or meeting link", current == null ? "" : current.location);
    EditText date =
        field(
            form,
            "Local time: yyyy-MM-dd HH:mm",
            format.format(
                new Date(
                    current == null ? System.currentTimeMillis() + 86400000 : current.startsAt)));
    EditText description =
        field(form, "Details (optional)", current == null ? "" : current.description);
    AlertDialog dialog =
        new AlertDialog.Builder(requireContext())
            .setTitle(id == null ? "New meetup" : "Edit meetup")
            .setView(form)
            .setNegativeButton("Close", null)
            .setPositiveButton("Save", null)
            .create();
    dialog.setOnShowListener(
        d ->
            dialog
                .getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(
                    v -> {
                      try {
                        String when = date.getText().toString().trim();
                        java.text.ParsePosition position = new java.text.ParsePosition(0);
                        Date parsed = format.parse(when, position);
                        if (parsed == null || position.getIndex() != when.length())
                          throw new IllegalArgumentException("Use yyyy-MM-dd HH:mm");
                        Meetup event = new Meetup();
                        event.title = title.getText().toString().trim();
                        event.location = location.getText().toString().trim();
                        event.startsAt = parsed.getTime();
                        event.description = description.getText().toString().trim();
                        event.ownerId = FirebaseAuth.getInstance().getUid();
                        Meetup.validate(
                            event.title,
                            event.location,
                            event.startsAt,
                            System.currentTimeMillis());
                        if (event.description.length() > 2000)
                          throw new IllegalArgumentException(
                              "Details cannot exceed 2000 characters");
                        DatabaseReference events =
                            com.example.sreer.geekspad.db.Backend.database().getReference("events");
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                        (id == null ? events.push() : events.child(id))
                            .setValue(event)
                            .addOnCompleteListener(
                                task -> {
                                  dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                                  if (task.isSuccessful()) dialog.dismiss();
                                  else toast("Save failed. Check connection and permissions.");
                                });
                      } catch (Exception error) {
                        toast(error.getMessage() == null ? "Invalid date" : error.getMessage());
                      }
                    }));
    dialog.show();
  }

  private TextView text(LinearLayout parent, String value) {
    TextView view = new TextView(requireContext());
    view.setText(value);
    view.setPadding(0, 5, 0, 5);
    parent.addView(view);
    return view;
  }

  private EditText field(LinearLayout parent, String hint, String value) {
    EditText view = new EditText(requireContext());
    view.setHint(hint);
    view.setText(value);
    parent.addView(view);
    return view;
  }

  private void toast(String value) {
    if (isAdded()) Toast.makeText(requireContext(), value, Toast.LENGTH_LONG).show();
  }
}
