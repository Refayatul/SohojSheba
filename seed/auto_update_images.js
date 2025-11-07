const fs = require("fs");
const path = require("path");

// GitHub settings
const GITHUB_USERNAME = "Refayatul";
const REPO_NAME = "SohojSheba";
const BRANCH = "firebaseMIgration"; // your branch

const imagesDir = path.join(__dirname, "images");
const catalogFile = path.join(__dirname, "seed_catalog.json");

// Load the existing catalog
let catalog;
try {
  catalog = JSON.parse(fs.readFileSync(catalogFile, "utf8"));
} catch (err) {
  console.error("Failed to parse seed_catalog.json:", err.message);
  process.exit(1);
}

// Read all image filenames
const imageFiles = fs.readdirSync(imagesDir);

// Helper to create GitHub raw URL
function getGitHubRawURL(filename) {
  return `https://raw.githubusercontent.com/${GITHUB_USERNAME}/${REPO_NAME}/${BRANCH}/seed/images/${filename}`;
}

// Helper to extract numbers from filenames for sorting
function extractNumber(filename) {
  const match = filename.match(/\d+/);
  return match ? parseInt(match[0], 10) : 0;
}

// Helper to create a clean image name without extension
function getImageName(filename) {
  return path.basename(filename, path.extname(filename));
}

// Helper to add missing images to a target object
function addMissingImages(targetObj = {}, prefix) {
  const files = imageFiles
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

// Update services
for (const service of catalog.services || []) {
  addMissingImages(service, service.id);
}

// Update service_details
for (const detail of catalog.service_details || []) {
  addMissingImages(detail, detail.serviceId);
}

// Save back to JSON
fs.writeFileSync(catalogFile, JSON.stringify(catalog, null, 2), "utf8");

console.log("✅ Updated seed_catalog.json with GitHub image URLs and imageNames!");
