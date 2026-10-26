const { onValueCreated } = require("firebase-functions/v2/database");
const { initializeApp } = require("firebase-admin/app");
const { getDatabase } = require("firebase-admin/database");
const { getMessaging } = require("firebase-admin/messaging");
const { deliverMessage } = require("./delivery");
initializeApp();
// Run only after database rules accept an authenticated participant's message.
// Set region here to match a non-default regional Realtime Database instance.
exports.notifyMessage = onValueCreated(
  "/rooms/{room}/messages/{message}",
  (event) =>
    deliverMessage(
      event.data.val(),
      event.params.room,
      getDatabase(),
      getMessaging(),
    ),
);
