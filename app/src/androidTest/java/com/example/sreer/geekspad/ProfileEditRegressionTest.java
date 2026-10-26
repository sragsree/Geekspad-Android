package com.example.sreer.geekspad;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.junit.Assert.*;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.example.sreer.geekspad.db.Backend;
import com.example.sreer.geekspad.ui.activity.ProfileEditActivity;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ProfileEditRegressionTest {
  private void awaitEnabled(int id) throws Exception {
    long end = System.currentTimeMillis() + 15000;
    while (System.currentTimeMillis() < end) {
      try {
        onView(withId(id)).check(matches(isEnabled()));
        return;
      } catch (AssertionError | androidx.test.espresso.NoMatchingViewException pending) {
        Thread.sleep(100);
      }
    }
    fail("Timed out waiting for the loaded profile/skills form");
  }

  @Test
  public void invalidNamesStayOnFormAndBirthdayDoesNotCloseBeforeSavingSkills() throws Exception {
    assertTrue(BuildConfig.USE_EMULATORS);
    Tasks.await(
        FirebaseAuth.getInstance().signInWithEmailAndPassword("alex@example.test", "reviewer123"),
        10,
        TimeUnit.SECONDS);
    DataSnapshot before =
        Tasks.await(Backend.database().getReference("users/alex").get(), 10, TimeUnit.SECONDS);
    try (ActivityScenario<ProfileEditActivity> scenario =
        ActivityScenario.launch(ProfileEditActivity.class)) {
      awaitEnabled(R.id.btn_add_skills);
      onView(withId(R.id.fname)).perform(scrollTo(), replaceText(""), closeSoftKeyboard());
      onView(withId(R.id.btn_add_skills)).perform(scrollTo(), click());
      onView(withId(R.id.fname)).check(matches(hasErrorText("FirstName is required!")));
      onView(withId(R.id.fname)).perform(scrollTo(), replaceText("Alex"), closeSoftKeyboard());
      onView(withId(R.id.lname)).perform(scrollTo(), replaceText(""), closeSoftKeyboard());
      onView(withId(R.id.btn_add_skills)).perform(scrollTo(), click());
      onView(withId(R.id.lname)).check(matches(hasErrorText("LastName is required!")));
      onView(withId(R.id.lname)).perform(scrollTo(), replaceText("Rivera"), closeSoftKeyboard());
      onView(withId(R.id.link_calendar)).perform(scrollTo(), click());
      onView(withId(R.id.date_set_button)).perform(click());
      // Returning from the date picker must retain this unsaved profile editor.
      onView(withId(R.id.fname)).check(matches(withText("Alex")));
      scenario.onActivity(activity -> assertFalse(activity.isFinishing()));
      onView(withId(R.id.btn_add_skills)).perform(scrollTo(), click());
      awaitEnabled(R.id.btn_register);
      onView(withText("Java")).check(matches(isDisplayed()));
      onView(withId(R.id.btn_register)).perform(click());
      long end = System.currentTimeMillis() + 15000;
      DataSnapshot after = null;
      while (System.currentTimeMillis() < end) {
        after =
            Tasks.await(Backend.database().getReference("users/alex").get(), 10, TimeUnit.SECONDS);
        if (!java.util.Objects.equals(
            before.child("birthDate").getValue(), after.child("birthDate").getValue())) break;
        Thread.sleep(100);
      }
      assertNotEquals(before.child("birthDate").getValue(), after.child("birthDate").getValue());
      assertEquals(before.child("skills").getValue(), after.child("skills").getValue());
      assertEquals(before.child("latitude").getValue(), after.child("latitude").getValue());
      assertEquals(before.child("longitude").getValue(), after.child("longitude").getValue());
    }
  }

  @Test
  public void saveStaysDisabledWhenTheAuthenticatedProfileIsMissing() throws Exception {
    assertTrue(BuildConfig.USE_EMULATORS);
    Tasks.await(
        FirebaseAuth.getInstance()
            .createUserWithEmailAndPassword(
                "missing-profile-" + System.currentTimeMillis() + "@example.test", "reviewer123"),
        10,
        TimeUnit.SECONDS);
    android.content.Intent intent =
        new android.content.Intent(
            androidx.test.core.app.ApplicationProvider.getApplicationContext(),
            com.example.sreer.geekspad.ui.activity.SkillsActivity.class);
    intent.putExtra("ForEdit", true);
    intent.putExtra(
        "user", new com.example.sreer.geekspad.model.User("Missing", "missing@example.test"));
    try (ActivityScenario<com.example.sreer.geekspad.ui.activity.SkillsActivity> scenario =
        ActivityScenario.launch(intent)) {
      onView(withId(R.id.btn_register)).check(matches(org.hamcrest.Matchers.not(isEnabled())));
      scenario.onActivity(activity -> activity.registerUser());
      onView(withId(R.id.btn_register)).check(matches(org.hamcrest.Matchers.not(isEnabled())));
    }
  }

  @Test
  public void updatingAnExistingSkillNotifiesAChangeAndKeepsTheRow() {
    java.util.List<com.example.sreer.geekspad.model.Skill> skills = new java.util.ArrayList<>();
    skills.add(new com.example.sreer.geekspad.model.Skill("Java", "50%"));
    com.example.sreer.geekspad.ui.adapter.SkillSetRecyclerAdapter adapter =
        new com.example.sreer.geekspad.ui.adapter.SkillSetRecyclerAdapter(skills);
    int[] changed = {0};
    int[] removed = {0};
    adapter.registerAdapterDataObserver(
        new androidx.recyclerview.widget.RecyclerView.AdapterDataObserver() {
          @Override
          public void onItemRangeChanged(int start, int count) {
            changed[0] += count;
          }

          @Override
          public void onItemRangeRemoved(int start, int count) {
            removed[0] += count;
          }
        });
    adapter.updateSkill(new com.example.sreer.geekspad.model.Skill("Java", "85%"), 0);
    assertEquals(1, adapter.getItemCount());
    assertEquals("85%", adapter.getSkill(0).proficency);
    assertEquals(1, changed[0]);
    assertEquals(0, removed[0]);
  }
}
