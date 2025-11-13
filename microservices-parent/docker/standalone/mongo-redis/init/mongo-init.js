print("Mongo Init Script - START");

function upsertUser(dbName, userDoc) {
    const targetDb = db.getSiblingDB(dbName);
    const existing = targetDb.getUser(userDoc.user);

    if (existing) {
        targetDb.updateUser(userDoc.user, {
            pwd: userDoc.pwd,
            roles: userDoc.roles,
        });
        print(`🟡 Updated user "${userDoc.user}" on "${dbName}"`);
    } else {
        targetDb.createUser(userDoc);
        print(`✅ Created user "${userDoc.user}" on "${dbName}"`);
    }
}

/**
 * Function: ensureCollection
 * ---------------------------
 * Create a collection only if it does not already exist.
 *
 * @param {string} dbName      - The name of the database to target.
 * @param {string} collName    - The name of the collection to create.
 * @param {object} [options]   - (Optional) Extra options to pass to createCollection().
 */
function ensureCollection(dbName, collName, options = {}) {
    const targetDb = db.getSiblingDB(dbName);
    const exists = targetDb.getCollectionInfos({ name: collName }).length > 0;
    if (!exists) {
        targetDb.createCollection(collName, options);
        print(`✅ Created collection "${dbName}.${collName}"`);
    } else {
        print(`🟡 Collection "${dbName}.${collName}" already exists`);
    }
}

upsertUser("admin", {
    user: "admin",
    pwd: "password",
    roles: [{ role: "root", db: "admin" }],
});

upsertUser("product-service", {
    user: "productAdmin",
    pwd: "password",
    roles: [{ role: "readWrite", db: "product-service" }],
});

ensureCollection("product-service", "user");

print('✅ Ensured user "productAdmin" has readWrite on "product-service" and base collection exists.');
print("🔵 Mongo Init Script - END");