/** FCM fan-out kept independent of the trigger so routing/cleanup can be tested without sending. */
async function deliverMessage(message, room, database, messaging) {
  if (!message.receiverUid || message.receiverUid === message.senderUid) return;
  const tokenRef = database.ref(`deviceTokens/${message.receiverUid}`);
  const tokens = Object.keys((await tokenRef.get()).val() || {});
  for (let offset = 0; offset < tokens.length; offset += 500) {
    const batch = tokens.slice(offset, offset + 500);
    const response = await messaging.sendEachForMulticast({
      tokens: batch,
      data: { title: "New Geekspad message", room },
      android: { priority: "high" },
    });
    await Promise.all(
      response.responses.map((result, index) => {
        if (
          !result.success &&
          [
            "messaging/registration-token-not-registered",
            "messaging/invalid-registration-token",
          ].includes(result.error?.code)
        ) {
          return tokenRef.child(batch[index]).remove();
        }
        return undefined;
      }),
    );
  }
}
module.exports = { deliverMessage };
