package com.example.sreer.geekspad;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.junit.Assert.*;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.example.sreer.geekspad.ui.activity.LoginActivity;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Run after npm run seed with Firebase emulators reachable at 10.0.2.2. */
@RunWith(AndroidJUnit4.class)
public class ReviewerFlowTest {
  private interface Condition {
    boolean ready() throws Exception;
  }

  private void await(Condition condition) throws Exception {
    long end = System.currentTimeMillis() + 15000;
    while (System.currentTimeMillis() < end) {
      if (condition.ready()) {
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        return;
      }
      Thread.sleep(100);
    }
    fail("Timed out waiting for emulator-backed UI operation");
  }

  private DataSnapshot read(String path) throws Exception {
    return Tasks.await(
        com.example.sreer.geekspad.db.Backend.database().getReference(path).get(),
        10,
        TimeUnit.SECONDS);
  }

  private void visible(String text) throws Exception {
    await(
        () -> {
          try {
            onView(withText(text)).check(matches(isDisplayed()));
            return true;
          } catch (androidx.test.espresso.NoMatchingViewException | AssertionError pending) {
            return false;
          }
        });
  }

  @Test
  public void loginCreateMeetupRatePeerAndSendMessage() throws Exception {
    assertTrue("This smoke test must never target a hosted project", BuildConfig.USE_EMULATORS);
    FirebaseAuth.getInstance().signOut();
    try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class)) {
      onView(withId(R.id.login)).perform(click());
      await(() -> FirebaseAuth.getInstance().getCurrentUser() != null);
      visible("Sam Chen");
      onView(withId(R.id.menu_events)).perform(click()).check(matches(isSelected()));
      visible("Organize a meetup");
      onView(withText("Organize a meetup")).perform(click());
      String title = "UI meetup " + System.currentTimeMillis();
      onView(withHint("Title")).perform(replaceText(title));
      onView(withHint("Location or meeting link"))
          .perform(replaceText("Review room"), closeSoftKeyboard());
      onView(withText("Save")).perform(click());
      await(
          () -> {
            for (DataSnapshot event : read("events").getChildren())
              if (title.equals(event.child("title").getValue())) return true;
            return false;
          });
      onView(withText(title)).perform(scrollTo()).check(matches(isDisplayed()));
      onView(withId(R.id.menu_list)).perform(click());
      visible("Sam Chen");
      onView(withText("Sam Chen")).perform(click());
      visible("Peer skill ratings");
      onView(withText("Peer skill ratings")).perform(scrollTo(), click());
      visible("Rate a skill");
      onView(withText("Rate a skill")).perform(click());
      visible("Java");
      onView(withText("Java")).perform(click());
      onView(withHint("Where did you work together? Give concrete feedback."))
          .perform(
              replaceText("Worked together at the emulator review meetup."), closeSoftKeyboard());
      onView(withText("Save rating")).perform(click());
      await(() -> read("ratings/sam/alex/Java").exists());
      pressBack();
      pressBack();
      onView(withId(R.id.menu_chat)).perform(click());
      visible("Sam Chen");
      onView(withText("Sam Chen")).perform(click());
      String message = "Emulator review message " + System.currentTimeMillis();
      // Both profile and recipient resolve asynchronously before send is available.
      await(
          () -> {
            try {
              onView(withId(R.id.sendButton)).check(matches(isEnabled()));
              return true;
            } catch (AssertionError pending) {
              return false;
            }
          });
      onView(withId(R.id.messageBodyField)).perform(replaceText(message), closeSoftKeyboard());
      onView(withId(R.id.sendButton)).perform(click());
      await(
          () -> {
            for (DataSnapshot entry : read("rooms/alex_sam/messages").getChildren())
              if (message.equals(entry.child("message").getValue())) return true;
            return false;
          });
      visible(message);
    }
  }
}
