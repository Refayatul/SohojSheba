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

/**
 * Helper to sort image names numerically, e.g., passport_step_1.webp -> passport_step_2.webp
 */
function sortImagesNumerically(imageNames) {
  return imageNames
    .slice() // make a copy
    .sort((a, b) => {
      const getNum = str => {
        const match = str.match(/(\d+)/);
        return match ? parseInt(match[1], 10) : 0;
      };
      return getNum(a) - getNum(b);
    });
}

(async function main() {
  const raw = fs.readFileSync(path.join(__dirname, "seed_catalog.json"), "utf8");
  const data = JSON.parse(raw);

  // Set config
  await db.collection("config").doc("catalog").set(data.config.catalog);

  // Batch for services
  const b1 = db.batch();
  for (const s of data.services) {
    if (s.imageNames) {
      const sortedNames = sortImagesNumerically(s.imageNames);
      s.images = sortedNames.map(name =>
        `https://raw.githubusercontent.com/Refayatul/SohojSheba/firebaseMIgration/seed/images/${name}.webp`
      );
    }
    b1.set(db.collection("services").doc(s.id), s);
  }
  await b1.commit();

  // Batch for service_details
  const b2 = db.batch();
  for (const d of data.service_details) {
    if (d.imageNames) {
      const sortedNames = sortImagesNumerically(d.imageNames);
      d.images = sortedNames.map(name =>
        `https://raw.githubusercontent.com/Refayatul/SohojSheba/firebaseMIgration/seed/images/${name}.webp`
      );
    }
    b2.set(db.collection("service_details").doc(d.serviceId), d);
  }
  await b2.commit();

  console.log("Seed complete with sorted image URLs!");
  process.exit(0);
})().catch(e => {
  console.error(e);
  process.exit(1);
});
