const fs = require("fs");
const path = require("path");

const catalogFile = path.join(__dirname, "seed_catalog.json");
const catalog = JSON.parse(fs.readFileSync(catalogFile, "utf8"));

// Print all service images
for (const service of catalog.services) {
  if (service.images && service.images.length > 0) {
    console.log(`Service ID: ${service.id}`);
    service.images.forEach(url => console.log(`  ${url}`));
  }
}

// Print all service_detail images
for (const detail of catalog.service_details || []) {
  if (detail.images && detail.images.length > 0) {
    console.log(`Service Detail ID: ${detail.serviceId}`);
    detail.images.forEach(url => console.log(`  ${url}`));
  }
}
