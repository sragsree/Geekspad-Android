import { before, after, test } from "node:test";
import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import {
  initializeTestEnvironment,
  assertSucceeds,
  assertFails,
} from "@firebase/rules-unit-testing";
import {
  ref,
  set,
  get,
  update,
  remove,
  serverTimestamp,
} from "firebase/database";
let env;
const profile = (uid, skill = "Java") => ({
  uid,
  firstname: uid,
  lastname: "Reviewer",
  email: `${uid}@example.test`,
  country: "USA",
  state: "California",
  city: "San Diego",
  skills: { [skill]: { skillname: skill, proficency: "80%" } },
});
before(async () => {
  env = await initializeTestEnvironment({
    projectId: "demo-geekspad",
    database: {
      host: "127.0.0.1",
      port: 9000,
      rules: await readFile("database.rules.json", "utf8"),
    },
  });
  await env.clearDatabase();
  await env.withSecurityRulesDisabled(async (context) => {
    const seedDb = context.database();
    for (const uid of ["alex", "sam", "taylor"])
      await set(ref(seedDb, `users/${uid}`), profile(uid));
  });
});
after(async () => env?.cleanup());
const connections = new Map();
const db = (uid) => {
  if (!connections.has(uid))
    connections.set(
      uid,
      env
        .authenticatedContext(uid, { email: `${uid}@example.test` })
        .database(),
    );
  return connections.get(uid);
};
test("profiles are readable only after sign-in and editable only by owner", async () => {
  await assertFails(get(ref(env.unauthenticatedContext().database(), "users")));
  await assertSucceeds(get(ref(db("sam"), "users")));
  await assertSucceeds(set(ref(db("alex"), "users/alex"), profile("alex")));
  await assertFails(set(ref(db("sam"), "users/alex"), profile("alex")));
  await assertFails(
    set(ref(db("alex"), "users/alex"), {
      ...profile("alex"),
      email: "forged@example.test",
    }),
  );
});
test("meetup lifecycle: create, owner edit, RSVP, cancellation and forbidden peer edits", async () => {
  const event = {
    title: "Java meetup",
    location: "Room A",
    description: "A review event",
    startsAt: Date.now() + 86400000,
    ownerId: "alex",
    cancelled: false,
  };
  await assertSucceeds(set(ref(db("alex"), "events/test"), event));
  await assertFails(
    update(ref(db("sam"), "events/test"), { title: "Hijacked" }),
  );
  await assertSucceeds(
    update(ref(db("alex"), "events/test"), { location: "Room B" }),
  );
  await assertSucceeds(set(ref(db("sam"), "attendance/test/sam"), true));
  await assertFails(set(ref(db("sam"), "attendance/test/taylor"), true));
  await assertSucceeds(remove(ref(db("sam"), "attendance/test/sam")));
  await assertSucceeds(
    update(ref(db("alex"), "events/test"), { cancelled: true }),
  );
  await assertFails(set(ref(db("sam"), "attendance/test/sam"), true));
  await assertFails(
    set(ref(db("alex"), "events/past"), {
      ...event,
      startsAt: Date.now() - 10000,
    }),
  );
});
test("peer ratings require context, a listed skill, valid score, and non-self authorship", async () => {
  const rating = {
    skill: "Java",
    score: 4,
    context: "Worked together at a Java meetup.",
    updatedAt: serverTimestamp(),
  };
  await assertSucceeds(set(ref(db("sam"), "ratings/alex/sam/Java"), rating));
  await assertSucceeds(
    set(ref(db("sam"), "ratings/alex/sam/Java"), { ...rating, score: 5 }),
  );
  assert.equal(
    (await get(ref(db("alex"), "ratings/alex"))).child("sam/Java/score").val(),
    5,
  );
  await assertFails(set(ref(db("alex"), "ratings/alex/alex/Java"), rating));
  await assertFails(set(ref(db("taylor"), "ratings/alex/sam/Java"), rating));
  await assertFails(
    set(ref(db("sam"), "ratings/alex/sam/Java"), { ...rating, score: 6 }),
  );
  await assertFails(
    set(ref(db("sam"), "ratings/alex/sam/Python"), {
      ...rating,
      skill: "Python",
    }),
  );
  await assertFails(
    set(ref(db("sam"), "ratings/alex/sam/Java"), { ...rating, context: "ok" }),
  );
});
test("two-way chat acknowledges persistence, isolates rooms and prevents forged/edited/deleted messages", async () => {
  const room = "alex_sam";
  const message = {
    senderUid: "alex",
    receiverUid: "sam",
    sender: "alex",
    receiver: "sam",
    senderMail: "alex@example.test",
    receiverMail: "sam@example.test",
    message: "Hello from Alex",
    timestamp: Date.now(),
  };
  await assertSucceeds(
    update(ref(db("alex"), `rooms/${room}`), {
      first: "alex",
      second: "sam",
      "members/alex": true,
      "members/sam": true,
      "messages/one": message,
    }),
  );
  assert.equal(
    (await get(ref(db("sam"), `rooms/${room}/messages/one`))).val().message,
    "Hello from Alex",
  );
  await assertFails(get(ref(db("taylor"), `rooms/${room}`)));
  await assertSucceeds(
    update(ref(db("alex"), `rooms/${room}`), {
      first: "alex",
      second: "sam",
      "members/alex": true,
      "members/sam": true,
      "messages/repeat": { ...message, message: "Second send" },
    }),
  );
  await assertFails(
    set(ref(db("sam"), `rooms/${room}/messages/forged`), message),
  );
  await assertSucceeds(
    set(ref(db("sam"), `rooms/${room}/messages/two`), {
      ...message,
      sender: "sam",
      senderUid: "sam",
      receiverUid: "alex",
      senderMail: "sam@example.test",
      receiverMail: "alex@example.test",
      message: "Hi Alex",
    }),
  );
  await assertFails(
    update(ref(db("alex"), `rooms/${room}/messages/one`), {
      message: "edited",
    }),
  );
  await assertFails(remove(ref(db("alex"), `rooms/${room}/messages/one`)));
  await assertFails(
    update(ref(db("alex"), `rooms/${room}/members`), {
      sam: null,
      taylor: true,
    }),
  );
  await assertFails(
    set(ref(db("taylor"), `rooms/${room}/messages/intruder`), {
      ...message,
      senderUid: "taylor",
    }),
  );
});
test("FCM device tokens are private and owner-writable only", async () => {
  await assertSucceeds(
    set(ref(db("alex"), "deviceTokens/alex/test-token"), true),
  );
  await assertFails(get(ref(db("sam"), "deviceTokens/alex")));
  await assertFails(set(ref(db("sam"), "deviceTokens/alex/attacker"), true));
});
test("real local Firebase Auth creates and signs in an account", async () => {
  const signup = await fetch(
    "http://127.0.0.1:9099/identitytoolkit.googleapis.com/v1/accounts:signUp?key=demo-key",
    {
      method: "POST",
      headers: { "content-type": "application/json" },
      body: JSON.stringify({
        email: "auth-test@example.test",
        password: "reviewer123",
        returnSecureToken: true,
      }),
    },
  );
  assert.equal(signup.status, 200);
  const signin = await fetch(
    "http://127.0.0.1:9099/identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=demo-key",
    {
      method: "POST",
      headers: { "content-type": "application/json" },
      body: JSON.stringify({
        email: "auth-test@example.test",
        password: "reviewer123",
        returnSecureToken: true,
      }),
    },
  );
  assert.equal(signin.status, 200);
  assert.ok((await signin.json()).idToken);
  const wrong = await fetch(
    "http://127.0.0.1:9099/identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=demo-key",
    {
      method: "POST",
      headers: { "content-type": "application/json" },
      body: JSON.stringify({
        email: "auth-test@example.test",
        password: "wrong-password",
        returnSecureToken: true,
      }),
    },
  );
  assert.equal(wrong.status, 400);
});
