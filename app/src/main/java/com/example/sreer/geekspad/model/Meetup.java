package com.example.sreer.geekspad.model;

public class Meetup {
  public String title, location, description, ownerId;
  public long startsAt;
  public boolean cancelled;

  public Meetup() {}

  public static void validate(String title, String location, long startsAt, long now) {
    if (title == null || title.trim().isEmpty() || title.length() > 100)
      throw new IllegalArgumentException("Title must contain 1–100 characters");
    if (location == null || location.trim().isEmpty() || location.length() > 200)
      throw new IllegalArgumentException("Location must contain 1–200 characters");
    if (startsAt <= now) throw new IllegalArgumentException("Choose a future date and time");
  }
}
