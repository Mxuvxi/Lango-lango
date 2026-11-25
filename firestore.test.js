const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_lango_123";
const BOB_UID = "bob_lango_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validUserPayload(uid, overrides = {}) {
  const past = new Date(Date.now() - 5000);
  return {
    uid,
    prenom: "Benoit",
    telephone: "+23675001122",
    ville: "Bangui",
    quartier: "PK5",
    languePref: "FR",
    role: "ARTISAN",
    metierPrincipal: "comp_solaire",
    niveauInitial: "INTERMEDIAIRE",
    objectif: "CLIENTS",
    bioCourte: "Installateur panneaux solaires et dépannage.",
    disponible: true,
    scoreFiabilite: 75,
    consentementLoi24001: true,
    badgesCount: 2,
    verifiedBadgesCount: 1,
    portfolioCount: 3,
    createdAt: past,
    updatedAt: past,
    ...overrides,
  };
}

function validCaissePayload(userId, overrides = {}) {
  const past = new Date(Date.now() - 5000);
  return {
    userId,
    type: "ENTREE",
    categorie: "Dépannage solaire",
    montantFcfa: 25000,
    clientOuNote: "Client M. Kossi",
    moyenPaiement: "ORANGE_MONEY",
    createdAt: past,
    updatedAt: past,
    ...overrides,
  };
}

function validDevisPayload(userId, overrides = {}) {
  const past = new Date(Date.now() - 5000);
  return {
    userId,
    referenceCode: "DEV-8A12BC",
    clientNom: "ONG Espoir RCA",
    clientTel: "+23672009988",
    descriptionTravaux: "Installation 2 panneaux 150W + régulateur",
    montantFcfa: 120000,
    acompteFcfa: 40000,
    statut: "ENVOYE",
    moyenPaiement: "ORANGE_MONEY",
    createdAt: past,
    updatedAt: past,
    ...overrides,
  };
}

function validOpportunitePayload(authorUid, overrides = {}) {
  const past = new Date(Date.now() - 5000);
  return {
    authorUid,
    employeurNom: "Atelier Bangui Energie",
    titre: "Technicien installation solaire autonome",
    type: "MISSION",
    ville: "Bangui",
    quartier: "Bimbo",
    competenceId: "comp_solaire",
    remunerationFcfa: 45000,
    descriptionCourte: "Installation kit solaire 300W sur site associatif.",
    contactTel: "+23675112233",
    active: true,
    createdAt: past,
    updatedAt: past,
    ...overrides,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user is denied across all collections", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).set(validUserPayload(ALICE_UID)));
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("caisseOperations").doc("op_1").get()
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("devis").doc("dev_1").get()
  );
  await assertFails(unauthDb.collection("opportunites").get());
  await assertFails(unauthDb.collection("unknown").doc("doc1").get());
});

test("User passport CRUD succeeds for owner and blocks cross-user, extra id, and invalid fields", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  const aliceRef = aliceDb.collection("users").doc(ALICE_UID);
  await assertSucceeds(aliceRef.set(validUserPayload(ALICE_UID)));
  await assertSucceeds(aliceRef.get());

  // Owner partial update preserves createdAt and uid
  await assertSucceeds(
    aliceRef.update({
      ville: "Bouar",
      scoreFiabilite: 85,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Cross-user access denied
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({
      ville: "Berberati",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertFails(bobDb.collection("users").doc(ALICE_UID).delete());

  // Immutable uid or createdAt change denied
  await assertFails(
    aliceRef.update({
      uid: BOB_UID,
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertFails(
    aliceRef.update({
      createdAt: new Date(Date.now() - 100000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Extra @DocumentId `id` field or invalid values denied
  await assertFails(
    bobDb.collection("users").doc(BOB_UID).set({
      ...validUserPayload(BOB_UID),
      id: BOB_UID,
    })
  );
  await assertFails(
    bobDb.collection("users").doc(BOB_UID).set(validUserPayload(BOB_UID, { languePref: "EN" }))
  );
  await assertFails(
    bobDb.collection("users").doc(BOB_UID).set(validUserPayload(BOB_UID, { scoreFiabilite: 150 }))
  );
  await assertFails(
    bobDb.collection("users").doc(BOB_UID).set(
      validUserPayload(BOB_UID, {
        createdAt: new Date(Date.now() + 60000),
      })
    )
  );

  await assertSucceeds(aliceRef.delete());
});

test("Caisse operations subcollection enforces ownership and domain constraints", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  const opRef = aliceDb.collection("users").doc(ALICE_UID).collection("caisseOperations").doc("op_01");
  await assertSucceeds(opRef.set(validCaissePayload(ALICE_UID)));
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("caisseOperations").get());
  await assertSucceeds(
    opRef.update({
      montantFcfa: 30000,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Cross-user rejected
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("caisseOperations").doc("op_01").get()
  );
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("caisseOperations")
      .doc("op_02")
      .set(validCaissePayload(BOB_UID))
  );

  // Negative amount or invalid enum rejected
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("caisseOperations")
      .doc("op_bad1")
      .set(validCaissePayload(ALICE_UID, { montantFcfa: -100 }))
  );
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("caisseOperations")
      .doc("op_bad2")
      .set(validCaissePayload(ALICE_UID, { moyenPaiement: "BITCOIN" }))
  );

  await assertSucceeds(opRef.delete());
});

test("Devis subcollection enforces ownership, acompte bounds, and immutability", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  const devisRef = aliceDb.collection("users").doc(ALICE_UID).collection("devis").doc("dev_01");
  await assertSucceeds(devisRef.set(validDevisPayload(ALICE_UID)));
  await assertSucceeds(
    devisRef.update({
      statut: "PAYE",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Cross-user rejected
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("devis").doc("dev_01").get());

  // Acompte > montantFcfa rejected
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("devis")
      .doc("dev_bad")
      .set(validDevisPayload(ALICE_UID, { montantFcfa: 10000, acompteFcfa: 20000 }))
  );

  await assertSucceeds(devisRef.delete());
});

test("Opportunites allow signed-in reads/queries and enforce authorUid writes", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  const oppRef = aliceDb.collection("opportunites").doc("opp_01");
  await assertSucceeds(oppRef.set(validOpportunitePayload(ALICE_UID)));

  // Signed-in peer can read and run exact Kotlin query filter
  await assertSucceeds(bobDb.collection("opportunites").doc("opp_01").get());
  await assertSucceeds(bobDb.collection("opportunites").where("active", "==", true).get());

  // Spoofing authorUid rejected
  await assertFails(
    bobDb.collection("opportunites").doc("opp_spoof").set(validOpportunitePayload(ALICE_UID))
  );

  // Non-author update/delete rejected
  await assertFails(
    bobDb.collection("opportunites").doc("opp_01").update({
      titre: "Titre piraté",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertFails(bobDb.collection("opportunites").doc("opp_01").delete());

  // Author cannot change authorUid, can update remuneration and delete
  await assertFails(
    oppRef.update({
      authorUid: BOB_UID,
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertSucceeds(
    oppRef.update({
      remunerationFcfa: 55000,
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertSucceeds(oppRef.delete());
});
