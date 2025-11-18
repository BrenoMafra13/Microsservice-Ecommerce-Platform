print("Mongo Init Script - START");

/**
 * Create or update a MongoDB user given a database and user document
 * @param dbName
 * @param userDoc
 */
function upsertUser(dbName, userDoc) {
    const targetDb = db.getSiblingDB(dbName);
    const existing = targetDb.getUser(userDoc.user);

    if (existing) {
        targetDb.updateUser(userDoc.user, {
            pwd: userDoc.pwd,
            roles: userDoc.roles
        });
        print(`Updated user "${userDoc.user}" on "${dbName}"`);
    } else {
        targetDb.createUser(userDoc);
        print(`Created user "${userDoc.user}" on "${dbName}"`);
    }
}

/**
 * Create collection only if it does not exist already
 * @param dbName
 * @param collName
 * @param options
 */
function ensureCollection(dbName, collName, options = {}) {
    const targetDb = db.getSiblingDB(dbName);
    const exists = targetDb.getCollectionInfos({ name: collName }).length > 0;

    if (!exists) {
        targetDb.createCollection(collName, options);
        print(`Created collection "${dbName}.${collName}"`);
    } else {
        print(`Collection "${dbName}.${collName}" already exists`);
    }
}

/******************************/
/* Admin User (used by mongo-express)
/******************************/
upsertUser("admin", {
    user: "admin",
    pwd: "password",
    roles: [{ role: "root", db: "admin" }]
});

/******************************/
/* Product-service User (used by product-service)
/******************************/
upsertUser("product-service", {
    user: "productAdmin",
    pwd: "password",
    roles: [{ role: "readWrite", db: "product-service" }]
});

ensureCollection("product-service", "user");

print('Ensure user "productAdmin" has readWrite on "product-service" and base collection exists');
print("Mongo Init Script - STOP");
