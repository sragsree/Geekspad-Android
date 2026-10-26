import { initializeApp } from "firebase-admin/app";
import { getAuth } from "firebase-admin/auth";
import { getDatabase } from "firebase-admin/database";
// This script refuses cloud targets. All data is fictional and local.
process.env.FIREBASE_AUTH_EMULATOR_HOST = "127.0.0.1:9099";
process.env.FIREBASE_DATABASE_EMULATOR_HOST = "127.0.0.1:9000";
initializeApp({
  projectId: "demo-geekspad",
  databaseURL: "https://demo-geekspad-default-rtdb.firebaseio.com",
});
const people = [
  [
    "alex",
    "Alex",
    "Rivera",
    "alex@example.test",
    "Java",
    "80%",
    "32.7757",
    "-117.0719",
  ],
  [
    "sam",
    "Sam",
    "Chen",
    "sam@example.test",
    "Java",
    "90%",
    "32.7157",
    "-117.1611",
  ],
  [
    "taylor",
    "Taylor",
    "Morgan",
    "taylor@example.test",
    "Python",
    "75%",
    "34.0522",
    "-118.2437",
  ],
];
for (const [
  uid,
  firstname,
  lastname,
  email,
  skill,
  proficency,
  latitude,
  longitude,
] of people) {
  try {
    await getAuth().createUser({ uid, email, password: "reviewer123" });
  } catch (e) {
    if (
      e.code !== "auth/uid-already-exists" &&
      e.code !== "auth/email-already-exists"
    )
      throw e;
  }
  await getDatabase()
    .ref(`users/${uid}`)
    .set({
      uid,
      firstname,
      lastname,
      email,
      country: "USA",
      state: "California",
      city: uid === "taylor" ? "Los Angeles" : "San Diego",
      latitude,
      longitude,
      skills: { [skill]: { skillname: skill, proficency } },
    });
}
await getDatabase()
  .ref("events/welcome")
  .set({
    title: "Local developer meetup",
    location: "Demo community room, San Diego",
    description: "Fictional event for reviewing create/edit/RSVP flows.",
    ownerId: "alex",
    startsAt: Date.now() + 86400000,
    cancelled: false,
  });
console.log(
  "Seeded local accounts: alex@example.test, sam@example.test, taylor@example.test. Password: reviewer123.",
);
process.exit(0);
