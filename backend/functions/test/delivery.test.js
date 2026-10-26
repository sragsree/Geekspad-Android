const { test } = require("node:test");
const assert = require("node:assert/strict");
const { deliverMessage } = require("../delivery");

test("routes only to recipient, batches at 500 and removes permanently invalid tokens", async () => {
  const tokens = Object.fromEntries(
    Array.from({ length: 501 }, (_, i) => [`token${i}`, true]),
  );
  const sent = [],
    removed = [],
    paths = [];
  const database = {
    ref: (path) => {
      paths.push(path);
      return {
        get: async () => ({ val: () => tokens }),
        child: (token) => ({ remove: async () => removed.push(token) }),
      };
    },
  };
  const messaging = {
    sendEachForMulticast: async (payload) => {
      sent.push(payload);
      return {
        responses: payload.tokens.map((token) =>
          token === "token0"
            ? {
                success: false,
                error: { code: "messaging/registration-token-not-registered" },
              }
            : { success: true },
        ),
      };
    },
  };
  await deliverMessage(
    { senderUid: "alex", receiverUid: "sam", message: "private text" },
    "alex_sam",
    database,
    messaging,
  );
  assert.deepEqual(paths, ["deviceTokens/sam"]);
  assert.deepEqual(
    sent.map((batch) => batch.tokens.length),
    [500, 1],
  );
  assert.deepEqual(removed, ["token0"]);
  assert.equal(sent[0].data.room, "alex_sam");
  assert.equal(JSON.stringify(sent).includes("private text"), false);
});

test("does not send for missing recipient or self message", async () => {
  const fail = {
    ref() {
      throw new Error("must not read tokens");
    },
  };
  await deliverMessage({ senderUid: "alex" }, "room", fail, {});
  await deliverMessage(
    { senderUid: "alex", receiverUid: "alex" },
    "room",
    fail,
    {},
  );
});
