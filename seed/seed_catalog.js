/**
 * Seeding Firestore with catalog + details
 * 
 * Setup:
 *   npm init -y
 *   npm i firebase-admin
 *   # Create a service account in Firebase Console and download JSON as seed/serviceAccount.json
 * Run:
 *   node seed/seed_catalog.js
 */
const admin = require("firebase-admin");
const fs = require("fs");
const path = require("path");

const serviceAccount = require("./serviceAccount.json");
admin.initializeApp({ credential: admin.credential.cert(serviceAccount) });
const db = admin.firestore();

(async function main() {
  const raw = fs.readFileSync(path.join(__dirname, "seed_catalog.json"), "utf8");
  const data = JSON.parse(raw);

  await db.collection("config").doc("catalog").set(data.config.catalog);

  const b1 = db.batch();
  for (const s of data.services) {
    b1.set(db.collection("services").doc(s.id), s);
  }
  await b1.commit();

  const b2 = db.batch();
  for (const d of data.service_details) {
    b2.set(db.collection("service_details").doc(d.serviceId), d);
  }
  await b2.commit();

  console.log("Seed complete.");
  process.exit(0);
})().catch(e => {
  console.error(e);
  process.exit(1);
});