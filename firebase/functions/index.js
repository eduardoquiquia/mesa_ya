const admin = require("firebase-admin");
const functions = require("firebase-functions");

admin.initializeApp();

exports.notifyReservationStatus = functions.firestore
  .document("reservas_admin/{reservaId}")
  .onUpdate(async (change) => {
    const before = change.before.data();
    const after = change.after.data();

    if (!after || before.estado === after.estado) {
      return null;
    }

    const userId = after.userId;
    if (!userId) {
      return null;
    }

    const userSnapshot = await admin.firestore()
      .collection("users")
      .doc(userId)
      .get();

    const token = userSnapshot.get("fcmToken");
    if (!token) {
      return null;
    }

    const statusLabel = {
      pendiente: "pendiente",
      en_preparacion: "en preparacion",
      completada: "completado"
    }[after.estado] || after.estado;

    return admin.messaging().send({
      token,
      notification: {
        title: "MesaYa",
        body: `Tu reserva esta ${statusLabel}.`
      },
      data: {
        reservaId: String(after.id || change.after.id),
        estado: String(after.estado || "")
      }
    });
  });
