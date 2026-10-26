package com.example.sreer.geekspad;

import static org.junit.Assert.*;

import com.example.sreer.geekspad.chat.ChatInteractor;
import com.example.sreer.geekspad.model.*;
import org.junit.Test;

public class DiscoveryTest {
  private User user() {
    User user = new User("Sam", "sam@example.test", "USA", "California");
    user.addSkill(new Skill("Java", "90%"));
    return user;
  }

  @Test
  public void discoveryUsesMinimumNotExactSkillScore() {
    assertTrue(Discovery.matches(user(), "USA", null, "Java", "80%"));
    assertFalse(Discovery.matches(user(), null, null, "Java", "95%"));
  }

  @Test
  public void clearingFiltersReturnsEveryProfile() {
    assertTrue(Discovery.matches(user(), null, null, null, null));
  }

  @Test
  public void profilesWithoutSkillsDoNotCrash() {
    assertFalse(Discovery.matches(new User(), null, null, "Java", null));
  }

  @Test
  public void locationFiltersCompose() {
    assertFalse(Discovery.matches(user(), "USA", "Texas", "Java", "80%"));
    assertTrue(Discovery.matches(user(), null, "California", null, null));
  }

  @Test
  public void invalidScoresAreSafe() {
    assertEquals(0, Discovery.score(null));
    assertEquals(0, Discovery.score("unknown"));
  }

  @Test
  public void chatRoomSameInBothDirections() {
    assertEquals(ChatInteractor.roomId("alex", "sam"), ChatInteractor.roomId("sam", "alex"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void rejectPastMeetup() {
    Meetup.validate("Title", "Room", 100, 101);
  }

  @Test(expected = IllegalArgumentException.class)
  public void rejectBlankMeetup() {
    Meetup.validate(" ", "Room", 102, 101);
  }

  @Test
  public void acceptFutureMeetup() {
    Meetup.validate("Java meetup", "Room A", 102, 101);
  }
}
