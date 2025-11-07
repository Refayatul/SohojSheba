const admin = require("firebase-admin");
const fs = require("fs");
const path = require("path");

// ---------------------------
// GitHub settings (must be defined BEFORE usage)
const GITHUB_USERNAME = "Refayatul";
const REPO_NAME = "SohojSheba";
const BRANCH = "firebaseMIgration"; // your branch

// Firebase service account
const serviceAccount = require("./serviceAccount.json");
admin.initializeApp({ credential: admin.credential.cert(serviceAccount) });
const db = admin.firestore();

// Paths
const imagesDir = path.join(__dirname, "images");
const catalogFile = path.join(__dirname, "seed_catalog.json");

// ---------------------------
// Helpers
function sortImagesNumerically(imageNames) {
  return imageNames
    .slice()
    .sort((a, b) => {
      const matchA = a.match(/\d+/);
      const matchB = b.match(/\d+/);
      const numA = matchA ? parseInt(matchA[0], 10) : 0;
      const numB = matchB ? parseInt(matchB[0], 10) : 0;
      return numA - numB;
    });
}

function getGitHubRawURL(filename) {
  return `https://raw.githubusercontent.com/${GITHUB_USERNAME}/${REPO_NAME}/${BRANCH}/seed/images/${filename}`;
}

function getImageName(filename) {
  return path.basename(filename, path.extname(filename));
}

function extractNumber(filename) {
  const match = filename.match(/\d+/);
  return match ? parseInt(match[0], 10) : 0;
}

function addMissingImages(targetObj = {}, prefix) {
  const files = fs.readdirSync(imagesDir)
    .filter(f => f.startsWith(prefix))
    .sort((a, b) => extractNumber(a) - extractNumber(b));

  targetObj.images = targetObj.images || [];
  targetObj.imageNames = targetObj.imageNames || [];

  for (const file of files) {
    const url = getGitHubRawURL(file);
    const name = getImageName(file);

    if (!targetObj.images.includes(url)) targetObj.images.push(url);
    if (!targetObj.imageNames.includes(name)) targetObj.imageNames.push(name);
  }
}

// ---------------------------
// Main function
(async function main() {
  let catalog;
  try {
    const raw = fs.readFileSync(catalogFile, "utf8");
    catalog = JSON.parse(raw);
  } catch (err) {
    console.error("Failed to read or parse seed_catalog.json:", err.message);
    process.exit(1);
  }

  // Update images for services and service_details
  for (const service of catalog.services || []) {
    addMissingImages(service, service.id);
  }
  for (const detail of catalog.service_details || []) {
    addMissingImages(detail, detail.serviceId);
  }

  // Save updated catalog.json
  fs.writeFileSync(catalogFile, JSON.stringify(catalog, null, 2), "utf8");
  console.log("✅ Updated seed_catalog.json with GitHub image URLs and imageNames!");

  // ---------------------------
  // Seed Firebase
  if (catalog.config && catalog.config.catalog) {
    await db.collection("config").doc("catalog").set(catalog.config.catalog);
  }

  // Batch for services
  const b1 = db.batch();
  for (const s of catalog.services || []) {
    if (s.imageNames) {
      const sortedNames = sortImagesNumerically(s.imageNames);
      s.images = sortedNames.map(name =>
        `https://raw.githubusercontent.com/${GITHUB_USERNAME}/${REPO_NAME}/${BRANCH}/seed/images/${name}.webp`
      );
    }
    b1.set(db.collection("services").doc(s.id), s);
  }
  await b1.commit();

  // Batch for service_details
  const b2 = db.batch();
  for (const d of catalog.service_details || []) {
    if (d.imageNames) {
      const sortedNames = sortImagesNumerically(d.imageNames);
      d.images = sortedNames.map(name =>
        `https://raw.githubusercontent.com/${GITHUB_USERNAME}/${REPO_NAME}/${BRANCH}/seed/images/${name}.webp`
      );
    }
    b2.set(db.collection("service_details").doc(d.serviceId), d);
  }
  await b2.commit();

  console.log("✅ Firebase seeding complete!");
  process.exit(0);
})().catch(e => {
  console.error(e);
  process.exit(1);
});
