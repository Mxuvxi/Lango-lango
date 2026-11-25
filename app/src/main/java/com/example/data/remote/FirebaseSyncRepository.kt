package com.example.data.remote

import android.content.Context
import com.example.R
import com.example.data.local.entity.CaisseOperationEntity
import com.example.data.local.entity.CoursGenereIaEntity
import com.example.data.local.entity.DevisEntity
import com.example.data.local.entity.OpportuniteEntity
import com.example.data.local.entity.ProfilEntity
import com.example.data.local.entity.UtilisateurEntity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirebaseSyncRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String =
        auth.currentUser?.uid
            ?: throw IllegalStateException("Google Sign-In required before accessing Firestore.")

    fun observeUserPassport(userId: String): Flow<UserPassportDoc?> =
        db.collection("users")
            .document(userId)
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(
                        UserPassportDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                } else {
                    null
                }
            }

    fun observeOpportunites(): Flow<List<CloudOpportunite>> =
        db.collection("opportunites")
            .whereEqualTo("active", true)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    CloudOpportunite::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            }

    suspend fun getUserPassportOnce(userId: String): Result<UserPassportDoc?> =
        try {
            val uid = requireUserId()
            check(uid == userId) { "Signed-in user changed; refusing cross-user read." }
            val snap = db.collection("users").document(userId).get().await()
            Result.success(
                if (snap.exists()) {
                    snap.toObject(
                        UserPassportDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                } else {
                    null
                }
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, "users/$userId")
            Result.failure(e)
        }

    suspend fun upsertUserPassport(
        user: UtilisateurEntity,
        profile: ProfilEntity?,
        badgesCount: Int,
        verifiedBadgesCount: Int,
        portfolioCount: Int
    ): Result<Unit> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val path = "users/$uid"
        val docRef = db.collection("users").document(uid)
        return try {
            val snapshot = docRef.get().await()
            val payload = mutableMapOf<String, Any>(
                "prenom" to user.prenom.trim().ifBlank { "Utilisateur Lango" }.take(80),
                "telephone" to user.telephone.trim().take(30),
                "ville" to user.ville.trim().ifBlank { "Bangui" }.take(60),
                "quartier" to user.quartier.trim().take(80),
                "languePref" to if (user.languePref == "SG") "SG" else "FR",
                "role" to if (user.role in listOf("JEUNE", "ARTISAN", "EMPLOYEUR")) user.role else "JEUNE",
                "metierPrincipal" to (profile?.metierPrincipal ?: "").take(80),
                "niveauInitial" to (profile?.niveauInitial ?: "DEBUTANT").take(40),
                "objectif" to (profile?.objectif ?: "EMPLOI").take(40),
                "bioCourte" to (profile?.bioCourte ?: "").take(400),
                "disponible" to (profile?.disponible ?: true),
                "scoreFiabilite" to (profile?.scoreFiabilite ?: 25).coerceIn(0, 100),
                "consentementLoi24001" to user.consentementLoi24001,
                "badgesCount" to badgesCount.coerceIn(0, 100),
                "verifiedBadgesCount" to verifiedBadgesCount.coerceIn(0, 100),
                "portfolioCount" to portfolioCount.coerceIn(0, 500),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (snapshot.exists()) {
                docRef.update(payload).await()
            } else {
                payload["uid"] = uid
                payload["createdAt"] = FieldValue.serverTimestamp()
                docRef.set(payload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun upsertCaisseOperation(op: CaisseOperationEntity): Result<Unit> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val path = "users/$uid/caisseOperations/${op.id}"
        val docRef = db.collection("users").document(uid).collection("caisseOperations").document(op.id)
        return try {
            val snapshot = docRef.get().await()
            val payload = mutableMapOf<String, Any>(
                "type" to if (op.type == "SORTIE") "SORTIE" else "ENTREE",
                "categorie" to op.categorie.trim().ifBlank { "Opération" }.take(80),
                "montantFcfa" to op.montantFcfa.coerceIn(0, 100_000_000),
                "clientOuNote" to op.clientOuNote.trim().take(200),
                "moyenPaiement" to if (op.moyenPaiement in listOf("ESPECES", "ORANGE_MONEY", "TELECEL_MONEY")) op.moyenPaiement else "ESPECES",
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (snapshot.exists()) {
                docRef.update(payload).await()
            } else {
                payload["userId"] = uid
                payload["createdAt"] = FieldValue.serverTimestamp()
                docRef.set(payload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteCaisseOperation(opId: String): Result<Unit> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val path = "users/$uid/caisseOperations/$opId"
        return try {
            db.collection("users").document(uid).collection("caisseOperations").document(opId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun upsertDevis(devis: DevisEntity): Result<Unit> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val path = "users/$uid/devis/${devis.id}"
        val docRef = db.collection("users").document(uid).collection("devis").document(devis.id)
        return try {
            val snapshot = docRef.get().await()
            val montant = devis.montantFcfa.coerceIn(0, 100_000_000)
            val acompte = devis.acompteFcfa.coerceIn(0, montant)
            val payload = mutableMapOf<String, Any>(
                "referenceCode" to devis.referenceCode.trim().ifBlank { "DEV-LANGO" }.take(30),
                "clientNom" to devis.clientNom.trim().ifBlank { "Client" }.take(80),
                "clientTel" to devis.clientTel.trim().take(30),
                "descriptionTravaux" to devis.descriptionTravaux.trim().ifBlank { "Prestation artisanale" }.take(300),
                "montantFcfa" to montant,
                "acompteFcfa" to acompte,
                "statut" to if (devis.statut in listOf("BROUILLON", "ENVOYE", "PAYE")) devis.statut else "BROUILLON",
                "moyenPaiement" to if (devis.moyenPaiement in listOf("ESPECES", "ORANGE_MONEY", "TELECEL_MONEY")) devis.moyenPaiement else "ORANGE_MONEY",
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (snapshot.exists()) {
                docRef.update(payload).await()
            } else {
                payload["userId"] = uid
                payload["createdAt"] = FieldValue.serverTimestamp()
                docRef.set(payload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteDevis(devisId: String): Result<Unit> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val path = "users/$uid/devis/$devisId"
        return try {
            db.collection("users").document(uid).collection("devis").document(devisId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun publishOpportunite(opp: OpportuniteEntity): Result<Unit> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val path = "opportunites/${opp.id}"
        val docRef = db.collection("opportunites").document(opp.id)
        return try {
            val payload = mapOf(
                "authorUid" to uid,
                "employeurNom" to opp.employeurNom.trim().ifBlank { "Employeur" }.take(100),
                "titre" to opp.titre.trim().ifBlank { "Mission locale" }.take(120),
                "type" to if (opp.type in listOf("EMPLOI", "STAGE", "MISSION", "SERVICE")) opp.type else "MISSION",
                "ville" to opp.ville.trim().ifBlank { "Bangui" }.take(60),
                "quartier" to opp.quartier.trim().take(80),
                "competenceId" to opp.competenceId.trim().ifBlank { "comp_solaire" }.take(60),
                "remunerationFcfa" to opp.remunerationFcfa.coerceIn(0, 100_000_000),
                "descriptionCourte" to opp.descriptionCourte.trim().ifBlank { "Opportunité Lango" }.take(500),
                "contactTel" to opp.contactTel.trim().take(30),
                "active" to opp.estActive,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun upsertCoursGenereIa(cours: CoursGenereIaEntity): Result<Unit> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val path = "users/$uid/coursGeneresIa/${cours.id}"
        val docRef = db.collection("users").document(uid).collection("coursGeneresIa").document(cours.id)
        return try {
            val snapshot = docRef.get().await()
            val payload = mutableMapOf<String, Any>(
                "titre" to cours.titre.trim().ifBlank { "Cours IA" }.take(150),
                "resume" to cours.resume.trim().take(1000),
                "notesEtudiant" to cours.notesEtudiant.trim().take(5000),
                "coursOriginalId" to (cours.coursOriginalId ?: "").take(100),
                "coursOriginalTitre" to cours.coursOriginalTitre.take(150),
                "photoLocaleUri" to cours.photoLocaleUri.take(500),
                "contenuCours" to cours.contenuCours.take(20000),
                "quizJson" to cours.quizJson.take(20000),
                "scoreDernierQuiz" to cours.scoreDernierQuiz.coerceIn(0, 1000),
                "totalQuestionsQuiz" to cours.totalQuestionsQuiz.coerceIn(0, 1000),
                "quizPasse" to cours.quizPasse,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (snapshot.exists()) {
                docRef.update(payload).await()
            } else {
                payload["userId"] = uid
                payload["createdAt"] = FieldValue.serverTimestamp()
                docRef.set(payload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteCoursGenereIa(coursId: String): Result<Unit> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val path = "users/$uid/coursGeneresIa/$coursId"
        return try {
            db.collection("users").document(uid).collection("coursGeneresIa").document(coursId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteAllUserCloudData(): Result<Unit> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return try {
            val userDocRef = db.collection("users").document(uid)
            val caisseSnaps = userDocRef.collection("caisseOperations").get().await()
            caisseSnaps.documents.forEach { it.reference.delete().await() }

            val devisSnaps = userDocRef.collection("devis").get().await()
            devisSnaps.documents.forEach { it.reference.delete().await() }

            val coursSnaps = userDocRef.collection("coursGeneresIa").get().await()
            coursSnaps.documents.forEach { it.reference.delete().await() }

            val oppSnaps = db.collection("opportunites").whereEqualTo("authorUid", uid).get().await()
            oppSnaps.documents.forEach { it.reference.delete().await() }

            userDocRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "users/$uid")
            Result.failure(e)
        }
    }
}
