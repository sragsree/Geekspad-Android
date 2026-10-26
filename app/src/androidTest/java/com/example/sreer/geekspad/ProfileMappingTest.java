package com.example.sreer.geekspad;

import static org.junit.Assert.*;

import android.os.Parcel;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.example.sreer.geekspad.db.FireBaseHelper;
import com.example.sreer.geekspad.model.User;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ProfileMappingTest {
  @Test
  public void firebaseSkillsDeserializeAndSurviveActivityParcel() throws Exception {
    assertTrue(BuildConfig.USE_EMULATORS);
    Tasks.await(
        FirebaseAuth.getInstance().signInWithEmailAndPassword("alex@example.test", "reviewer123"),
        10,
        TimeUnit.SECONDS);
    User user =
        FireBaseHelper.getUserFromSnapShot(
            Tasks.await(
                com.example.sreer.geekspad.db.Backend.database().getReference("users/sam").get(),
                10,
                TimeUnit.SECONDS));
    assertEquals("90%", user.getSkill("Java").proficency);
    assertEquals("sam", user.getUid());
    Parcel parcel = Parcel.obtain();
    try {
      user.writeToParcel(parcel, 0);
      parcel.setDataPosition(0);
      User restored = new User(parcel);
      assertEquals("sam", restored.getUid());
      assertEquals("90%", restored.getSkill("Java").proficency);
    } finally {
      parcel.recycle();
    }
  }
}
