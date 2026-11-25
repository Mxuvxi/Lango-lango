package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.local.entity.BadgeEntity
import com.example.data.local.entity.CaisseOperationEntity
import com.example.data.local.entity.CandidatureEntity
import com.example.data.local.entity.CandidatureRecueEntity
import com.example.data.local.entity.CoachMessageEntity
import com.example.data.local.entity.CompetenceEntity
import com.example.data.local.entity.ContenuPedagogiqueEntity
import com.example.data.local.entity.CoursGenereIaEntity
import com.example.data.local.entity.DevisEntity
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.OpportuniteEntity
import com.example.data.local.entity.PortfolioEntity
import com.example.data.local.entity.ProfilEntity
import com.example.data.local.entity.ProgressionEntity
import com.example.data.local.entity.SyncOutboxEntity
import com.example.data.local.entity.SyncStatus
import com.example.data.local.entity.UtilisateurEntity
import com.example.data.local.entity.VerificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UtilisateurDao {
    @Query("SELECT * FROM utilisateurs LIMIT 1")
    fun observeCurrentUser(): Flow<UtilisateurEntity?>

    @Query("SELECT * FROM utilisateurs LIMIT 1")
    suspend fun getCurrentUserOnce(): UtilisateurEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUser(user: UtilisateurEntity)

    @Query("SELECT * FROM profils WHERE utilisateurId = :userId LIMIT 1")
    fun observeProfile(userId: String): Flow<ProfilEntity?>

    @Query("SELECT * FROM profils WHERE utilisateurId = :userId LIMIT 1")
    suspend fun getProfileOnce(userId: String): ProfilEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: ProfilEntity)

    @Query("DELETE FROM utilisateurs")
    suspend fun deleteAllUsers()

    @Query("DELETE FROM sync_outbox")
    suspend fun deleteAllOutbox()

    @Transaction
    suspend fun wipeAllPersonalData() {
        deleteAllUsers()
        deleteAllOutbox()
    }
}

@Dao
interface FormationDao {
    @Query("SELECT * FROM competences ORDER BY nomFr ASC")
    fun observeCompetences(): Flow<List<CompetenceEntity>>

    @Query("SELECT COUNT(*) FROM competences")
    suspend fun countCompetences(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompetences(items: List<CompetenceEntity>)

    @Query("SELECT * FROM contenus_pedagogiques ORDER BY estTelecharge DESC, tailleKo ASC")
    fun observeContenus(): Flow<List<ContenuPedagogiqueEntity>>

    @Query("SELECT * FROM contenus_pedagogiques WHERE id = :contenuId LIMIT 1")
    suspend fun getContenuOnce(contenuId: String): ContenuPedagogiqueEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContenus(items: List<ContenuPedagogiqueEntity>)

    @Query("UPDATE contenus_pedagogiques SET estTelecharge = :downloaded WHERE id = :contenuId")
    suspend fun setPackDownloaded(contenuId: String, downloaded: Boolean)

    @Query("SELECT * FROM progressions WHERE utilisateurId = :userId")
    fun observeProgressions(userId: String): Flow<List<ProgressionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgression(progression: ProgressionEntity)
}

@Dao
interface PreuveDao {
    @Query("SELECT * FROM badges WHERE utilisateurId = :userId ORDER BY obtenuLe DESC")
    fun observeBadges(userId: String): Flow<List<BadgeEntity>>

    @Query("SELECT * FROM badges WHERE utilisateurId = :userId")
    suspend fun getBadgesOnce(userId: String): List<BadgeEntity>

    @Query("SELECT * FROM badges WHERE utilisateurId = :userId AND competenceId = :competenceId LIMIT 1")
    suspend fun getBadgeForCompetence(userId: String, competenceId: String): BadgeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadge(badge: BadgeEntity)

    @Query("SELECT * FROM verifications WHERE utilisateurId = :userId ORDER BY createdAt DESC")
    fun observeVerifications(userId: String): Flow<List<VerificationEntity>>

    @Query("SELECT * FROM verifications WHERE id = :verifId LIMIT 1")
    suspend fun getVerificationOnce(verifId: String): VerificationEntity?

    @Query("SELECT * FROM verifications WHERE utilisateurId = :userId")
    suspend fun getVerificationsOnce(userId: String): List<VerificationEntity>

    @Query("SELECT COUNT(*) FROM verifications WHERE badgeId = :badgeId AND statut = 'VALIDE'")
    suspend fun countValidVerificationsForBadge(badgeId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerification(verification: VerificationEntity)

    @Query("SELECT * FROM portfolios WHERE utilisateurId = :userId ORDER BY createdAt DESC")
    fun observePortfolio(userId: String): Flow<List<PortfolioEntity>>

    @Query("SELECT * FROM portfolios WHERE utilisateurId = :userId")
    suspend fun getPortfolioOnce(userId: String): List<PortfolioEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPortfolioItem(item: PortfolioEntity)

    @Query("DELETE FROM portfolios WHERE id = :itemId")
    suspend fun deletePortfolioItem(itemId: String)
}

@Dao
interface OpportuniteDao {
    @Query("SELECT * FROM opportunites WHERE estActive = 1 ORDER BY expireLe ASC")
    fun observeOpportunites(): Flow<List<OpportuniteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOpportunites(items: List<OpportuniteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOpportunite(item: OpportuniteEntity)

    @Query("SELECT * FROM candidatures WHERE utilisateurId = :userId ORDER BY createdAt DESC")
    fun observeCandidatures(userId: String): Flow<List<CandidatureEntity>>

    @Query("SELECT * FROM candidatures WHERE utilisateurId = :userId AND opportuniteId = :opportuniteId LIMIT 1")
    suspend fun getCandidatureForOffer(userId: String, opportuniteId: String): CandidatureEntity?

    @Query("UPDATE candidatures SET statut = 'ENVOYEE', syncStatus = 'SYNCED' WHERE statut = 'BROUILLON_OFFLINE'")
    suspend fun promoteOfflineCandidaturesToSent()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCandidature(candidature: CandidatureEntity)

    @Query("SELECT * FROM candidatures_recues ORDER BY scoreFiabilite DESC, createdAt DESC")
    fun observeReceivedCandidatures(): Flow<List<CandidatureRecueEntity>>

    @Query("SELECT COUNT(*) FROM candidatures_recues")
    suspend fun countReceivedCandidatures(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceivedCandidatures(items: List<CandidatureRecueEntity>)

    @Query("UPDATE candidatures_recues SET statut = :statut, noteEmployeur = :noteEmployeur WHERE id = :id")
    suspend fun updateReceivedCandidatureStatus(id: String, statut: String, noteEmployeur: String)

    @Query("UPDATE candidatures SET statut = :statut, noteEmployeur = :noteEmployeur WHERE opportuniteId = :opportuniteId")
    suspend fun syncUserCandidatureStatusForOffer(opportuniteId: String, statut: String, noteEmployeur: String)

    @Query("SELECT * FROM notifications WHERE utilisateurId = :userId ORDER BY createdAt DESC")
    fun observeNotifications(userId: String): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET lue = 1 WHERE utilisateurId = :userId")
    suspend fun markAllNotificationsRead(userId: String)
}

@Dao
interface CaisseDevisDao {
    @Query("SELECT * FROM caisse_operations WHERE utilisateurId = :userId ORDER BY createdAt DESC")
    fun observeCaisseOperations(userId: String): Flow<List<CaisseOperationEntity>>

    @Query("SELECT * FROM caisse_operations WHERE utilisateurId = :userId")
    suspend fun getCaisseOperationsOnce(userId: String): List<CaisseOperationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCaisseOperation(op: CaisseOperationEntity)

    @Query("DELETE FROM caisse_operations WHERE id = :opId")
    suspend fun deleteCaisseOperation(opId: String)

    @Query("SELECT * FROM devis_artisans WHERE utilisateurId = :userId ORDER BY createdAt DESC")
    fun observeDevis(userId: String): Flow<List<DevisEntity>>

    @Query("SELECT * FROM devis_artisans WHERE utilisateurId = :userId")
    suspend fun getDevisOnce(userId: String): List<DevisEntity>

    @Query("SELECT * FROM devis_artisans WHERE id = :devisId LIMIT 1")
    suspend fun getDevisById(devisId: String): DevisEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevis(devis: DevisEntity)

    @Query("DELETE FROM devis_artisans WHERE id = :devisId")
    suspend fun deleteDevis(devisId: String)
}

@Dao
interface CoachDao {
    @Query("SELECT * FROM coach_messages WHERE utilisateurId = :userId ORDER BY createdAt ASC")
    fun observeMessages(userId: String): Flow<List<CoachMessageEntity>>

    @Query("SELECT * FROM coach_messages WHERE utilisateurId = :userId ORDER BY createdAt ASC")
    suspend fun getMessagesOnce(userId: String): List<CoachMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: CoachMessageEntity)

    @Query("DELETE FROM coach_messages WHERE utilisateurId = :userId")
    suspend fun clearMessages(userId: String)
}

@Dao
interface SyncOutboxDao {
    @Query("SELECT COUNT(*) FROM sync_outbox WHERE status = :status")
    fun observePendingCount(status: String = SyncStatus.PENDING_UPLOAD): Flow<Int>

    @Query("SELECT * FROM sync_outbox ORDER BY createdAt DESC LIMIT 25")
    fun observeRecentOutbox(): Flow<List<SyncOutboxEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueue(item: SyncOutboxEntity)

    @Query("UPDATE sync_outbox SET status = :newStatus WHERE status = :oldStatus")
    suspend fun markAllAsSynced(
        oldStatus: String = SyncStatus.PENDING_UPLOAD,
        newStatus: String = SyncStatus.SYNCED
    )
}

@Dao
interface CoursIaDao {
    @Query("SELECT * FROM cours_generes_ia WHERE utilisateurId = :userId ORDER BY createdAt DESC")
    fun observeCoursGeneres(userId: String): Flow<List<CoursGenereIaEntity>>

    @Query("SELECT * FROM cours_generes_ia WHERE utilisateurId = :userId ORDER BY createdAt DESC")
    suspend fun getCoursGeneresOnce(userId: String): List<CoursGenereIaEntity>

    @Query("SELECT * FROM cours_generes_ia WHERE id = :id LIMIT 1")
    suspend fun getCoursById(id: String): CoursGenereIaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCours(cours: CoursGenereIaEntity)

    @Query("UPDATE cours_generes_ia SET scoreDernierQuiz = :score, totalQuestionsQuiz = :total, quizPasse = 1, syncStatus = :syncStatus WHERE id = :id")
    suspend fun updateQuizResult(id: String, score: Int, total: Int, syncStatus: String = SyncStatus.PENDING_UPLOAD)

    @Query("DELETE FROM cours_generes_ia WHERE id = :id")
    suspend fun deleteCours(id: String)
}
