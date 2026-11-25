package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

object SyncStatus {
    const val PENDING_UPLOAD = "PENDING_UPLOAD"
    const val SYNCED = "SYNCED"
    const val LOCAL_ONLY = "LOCAL_ONLY"
}

object LangueCode {
    const val FR = "FR"
    const val SG = "SG"
}

object RoleCode {
    const val JEUNE = "JEUNE"
    const val ARTISAN = "ARTISAN"
    const val EMPLOYEUR = "EMPLOYEUR"
}

@Entity(tableName = "utilisateurs")
data class UtilisateurEntity(
    @PrimaryKey val id: String,
    val prenom: String,
    val telephone: String,
    val ville: String,
    val quartier: String,
    val languePref: String = LangueCode.FR,
    val role: String = RoleCode.JEUNE,
    val consentementLoi24001: Boolean = false,
    val consentDate: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val syncStatus: String = SyncStatus.PENDING_UPLOAD
)

@Entity(
    tableName = "profils",
    foreignKeys = [
        ForeignKey(
            entity = UtilisateurEntity::class,
            parentColumns = ["id"],
            childColumns = ["utilisateurId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ProfilEntity(
    @PrimaryKey val utilisateurId: String,
    val bioCourte: String = "",
    val metierPrincipal: String = "",
    val niveauInitial: String = "DEBUTANT",
    val objectif: String = "EMPLOI",
    val scoreFiabilite: Int = 20,
    val disponible: Boolean = true,
    val photoProfilUri: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "competences")
data class CompetenceEntity(
    @PrimaryKey val id: String,
    val nomFr: String,
    val nomSg: String,
    val categorie: String,
    val iconeKey: String
)

@Entity(
    tableName = "contenus_pedagogiques",
    foreignKeys = [
        ForeignKey(
            entity = CompetenceEntity::class,
            parentColumns = ["id"],
            childColumns = ["competenceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("competenceId")]
)
data class ContenuPedagogiqueEntity(
    @PrimaryKey val id: String,
    val competenceId: String,
    val titreFr: String,
    val titreSg: String,
    val resumeFr: String,
    val resumeSg: String,
    val dureeMin: Int,
    val tailleKo: Int,
    val version: Int = 1,
    val estTelecharge: Boolean = false,
    val audioDureeSec: Int = 180,
    val leconsJson: String = "[]"
)

@Entity(
    tableName = "progressions",
    foreignKeys = [
        ForeignKey(
            entity = UtilisateurEntity::class,
            parentColumns = ["id"],
            childColumns = ["utilisateurId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ContenuPedagogiqueEntity::class,
            parentColumns = ["id"],
            childColumns = ["contenuId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("utilisateurId"), Index("contenuId")]
)
data class ProgressionEntity(
    @PrimaryKey val id: String,
    val utilisateurId: String,
    val contenuId: String,
    val etapeActuelle: Int = 0,
    val totalEtapes: Int = 3,
    val scoreQuiz: Int = 0,
    val estTermine: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: String = SyncStatus.PENDING_UPLOAD
)

@Entity(
    tableName = "badges",
    foreignKeys = [
        ForeignKey(
            entity = UtilisateurEntity::class,
            parentColumns = ["id"],
            childColumns = ["utilisateurId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CompetenceEntity::class,
            parentColumns = ["id"],
            childColumns = ["competenceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("utilisateurId"), Index("competenceId")]
)
data class BadgeEntity(
    @PrimaryKey val id: String,
    val utilisateurId: String,
    val competenceId: String,
    val titreFr: String,
    val titreSg: String,
    val niveau: String = "PRATIQUE", // DEBUTANT, PRATIQUE, VERIFIE
    val obtenuLe: Long = System.currentTimeMillis(),
    val syncStatus: String = SyncStatus.PENDING_UPLOAD
)

@Entity(
    tableName = "verifications",
    foreignKeys = [
        ForeignKey(
            entity = UtilisateurEntity::class,
            parentColumns = ["id"],
            childColumns = ["utilisateurId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = BadgeEntity::class,
            parentColumns = ["id"],
            childColumns = ["badgeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("utilisateurId"), Index("badgeId")]
)
data class VerificationEntity(
    @PrimaryKey val id: String,
    val utilisateurId: String,
    val badgeId: String,
    val verificateurNom: String,
    val verificateurTel: String,
    val relation: String = "CLIENT", // PAIR, CLIENT, MAITRE
    val canal: String = "SMS", // APP, SMS
    val statut: String = "EN_ATTENTE", // EN_ATTENTE, VALIDE, REFUSE
    val note: Int = 5,
    val commentaire: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val syncStatus: String = SyncStatus.PENDING_UPLOAD
)

@Entity(
    tableName = "portfolios",
    foreignKeys = [
        ForeignKey(
            entity = UtilisateurEntity::class,
            parentColumns = ["id"],
            childColumns = ["utilisateurId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CompetenceEntity::class,
            parentColumns = ["id"],
            childColumns = ["competenceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("utilisateurId"), Index("competenceId")]
)
data class PortfolioEntity(
    @PrimaryKey val id: String,
    val utilisateurId: String,
    val competenceId: String,
    val titre: String,
    val descriptionCourte: String,
    val photoLocaleUri: String = "",
    val photoDistanteUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val syncStatus: String = SyncStatus.PENDING_UPLOAD
)

@Entity(
    tableName = "opportunites",
    foreignKeys = [
        ForeignKey(
            entity = CompetenceEntity::class,
            parentColumns = ["id"],
            childColumns = ["competenceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("competenceId"), Index("ville")]
)
data class OpportuniteEntity(
    @PrimaryKey val id: String,
    val employeurNom: String,
    val titre: String,
    val type: String, // EMPLOI, STAGE, MISSION, SERVICE
    val ville: String,
    val quartier: String,
    val competenceId: String,
    val remunerationFcfa: Int,
    val descriptionCourte: String,
    val contactTel: String,
    val expireLe: Long,
    val estActive: Boolean = true
)

@Entity(
    tableName = "candidatures",
    foreignKeys = [
        ForeignKey(
            entity = OpportuniteEntity::class,
            parentColumns = ["id"],
            childColumns = ["opportuniteId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UtilisateurEntity::class,
            parentColumns = ["id"],
            childColumns = ["utilisateurId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("opportuniteId"), Index("utilisateurId")]
)
data class CandidatureEntity(
    @PrimaryKey val id: String,
    val opportuniteId: String,
    val utilisateurId: String,
    val messageCourt: String = "",
    val canalEnvoi: String = "APP", // APP, SMS
    val statut: String = "BROUILLON_OFFLINE", // BROUILLON_OFFLINE, ENVOYEE, VUE, RETENUE, REFUSEE
    val noteEmployeur: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val syncStatus: String = SyncStatus.PENDING_UPLOAD
)

@Entity(
    tableName = "candidatures_recues",
    foreignKeys = [
        ForeignKey(
            entity = OpportuniteEntity::class,
            parentColumns = ["id"],
            childColumns = ["opportuniteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("opportuniteId")]
)
data class CandidatureRecueEntity(
    @PrimaryKey val id: String,
    val opportuniteId: String,
    val candidatNom: String,
    val candidatTel: String,
    val candidatVille: String,
    val metierLabel: String,
    val scoreFiabilite: Int,
    val badgesValides: Int,
    val messageCourt: String,
    val canalEnvoi: String = "APP", // APP, SMS
    val statut: String = "ENVOYEE", // ENVOYEE, VUE, RETENUE, REFUSEE
    val noteEmployeur: String = "",
    val passeportCode: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "caisse_operations",
    foreignKeys = [
        ForeignKey(
            entity = UtilisateurEntity::class,
            parentColumns = ["id"],
            childColumns = ["utilisateurId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("utilisateurId")]
)
data class CaisseOperationEntity(
    @PrimaryKey val id: String,
    val utilisateurId: String,
    val type: String, // ENTREE, SORTIE
    val categorie: String,
    val montantFcfa: Int,
    val clientOuNote: String = "",
    val moyenPaiement: String = "ESPECES", // ESPECES, ORANGE_MONEY, TELECEL_MONEY
    val createdAt: Long = System.currentTimeMillis(),
    val syncStatus: String = SyncStatus.LOCAL_ONLY
)

@Entity(
    tableName = "devis_artisans",
    foreignKeys = [
        ForeignKey(
            entity = UtilisateurEntity::class,
            parentColumns = ["id"],
            childColumns = ["utilisateurId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("utilisateurId")]
)
data class DevisEntity(
    @PrimaryKey val id: String,
    val utilisateurId: String,
    val referenceCode: String,
    val clientNom: String,
    val clientTel: String,
    val descriptionTravaux: String,
    val montantFcfa: Int,
    val acompteFcfa: Int = 0,
    val statut: String = "BROUILLON", // BROUILLON, ENVOYE, PAYE
    val moyenPaiement: String = "ORANGE_MONEY", // ESPECES, ORANGE_MONEY, TELECEL_MONEY
    val createdAt: Long = System.currentTimeMillis(),
    val syncStatus: String = SyncStatus.PENDING_UPLOAD
)

@Entity(
    tableName = "coach_messages",
    foreignKeys = [
        ForeignKey(
            entity = UtilisateurEntity::class,
            parentColumns = ["id"],
            childColumns = ["utilisateurId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("utilisateurId")]
)
data class CoachMessageEntity(
    @PrimaryKey val id: String,
    val utilisateurId: String,
    val role: String, // USER, MODEL
    val texte: String,
    val langue: String = LangueCode.FR,
    val metierContext: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "notifications",
    foreignKeys = [
        ForeignKey(
            entity = UtilisateurEntity::class,
            parentColumns = ["id"],
            childColumns = ["utilisateurId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("utilisateurId")]
)
data class NotificationEntity(
    @PrimaryKey val id: String,
    val utilisateurId: String,
    val titre: String,
    val corps: String,
    val type: String, // OFFRE, VERIFICATION, BADGE
    val canal: String = "IN_APP", // IN_APP, SMS, USSD
    val lue: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "sync_outbox")
data class SyncOutboxEntity(
    @PrimaryKey val id: String,
    val entityType: String,
    val entityId: String,
    val payloadJson: String,
    val createdAt: Long = System.currentTimeMillis(),
    val retryCount: Int = 0,
    val status: String = SyncStatus.PENDING_UPLOAD
)

@Entity(
    tableName = "cours_generes_ia",
    foreignKeys = [
        ForeignKey(
            entity = UtilisateurEntity::class,
            parentColumns = ["id"],
            childColumns = ["utilisateurId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("utilisateurId")]
)
data class CoursGenereIaEntity(
    @PrimaryKey val id: String,
    val utilisateurId: String,
    val titre: String,
    val resume: String = "",
    val notesEtudiant: String = "",
    val coursOriginalId: String? = null,
    val coursOriginalTitre: String = "",
    val photoLocaleUri: String = "",
    val contenuCours: String = "",
    val quizJson: String = "",
    val scoreDernierQuiz: Int = 0,
    val totalQuestionsQuiz: Int = 0,
    val quizPasse: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val syncStatus: String = SyncStatus.PENDING_UPLOAD
)
