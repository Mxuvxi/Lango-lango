package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.CaisseDevisDao
import com.example.data.local.dao.CoachDao
import com.example.data.local.dao.CoursIaDao
import com.example.data.local.dao.FormationDao
import com.example.data.local.dao.OpportuniteDao
import com.example.data.local.dao.PreuveDao
import com.example.data.local.dao.SyncOutboxDao
import com.example.data.local.dao.UtilisateurDao
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
import com.example.data.local.entity.UtilisateurEntity
import com.example.data.local.entity.VerificationEntity

@Database(
    entities = [
        UtilisateurEntity::class,
        ProfilEntity::class,
        CompetenceEntity::class,
        ContenuPedagogiqueEntity::class,
        ProgressionEntity::class,
        BadgeEntity::class,
        VerificationEntity::class,
        PortfolioEntity::class,
        OpportuniteEntity::class,
        CandidatureEntity::class,
        CandidatureRecueEntity::class,
        CaisseOperationEntity::class,
        DevisEntity::class,
        CoachMessageEntity::class,
        NotificationEntity::class,
        SyncOutboxEntity::class,
        CoursGenereIaEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class LangoDatabase : RoomDatabase() {
    abstract fun utilisateurDao(): UtilisateurDao
    abstract fun formationDao(): FormationDao
    abstract fun preuveDao(): PreuveDao
    abstract fun opportuniteDao(): OpportuniteDao
    abstract fun caisseDevisDao(): CaisseDevisDao
    abstract fun coachDao(): CoachDao
    abstract fun coursIaDao(): CoursIaDao
    abstract fun syncOutboxDao(): SyncOutboxDao

    companion object {
        @Volatile
        private var INSTANCE: LangoDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE profils ADD COLUMN photoProfilUri TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE candidatures ADD COLUMN noteEmployeur TEXT NOT NULL DEFAULT ''")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS candidatures_recues (
                        id TEXT NOT NULL PRIMARY KEY,
                        opportuniteId TEXT NOT NULL,
                        candidatNom TEXT NOT NULL,
                        candidatTel TEXT NOT NULL,
                        candidatVille TEXT NOT NULL,
                        metierLabel TEXT NOT NULL,
                        scoreFiabilite INTEGER NOT NULL,
                        badgesValides INTEGER NOT NULL,
                        messageCourt TEXT NOT NULL,
                        canalEnvoi TEXT NOT NULL,
                        statut TEXT NOT NULL,
                        noteEmployeur TEXT NOT NULL,
                        passeportCode TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        FOREIGN KEY(opportuniteId) REFERENCES opportunites(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_candidatures_recues_opportuniteId ON candidatures_recues(opportuniteId)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS caisse_operations (
                        id TEXT NOT NULL PRIMARY KEY,
                        utilisateurId TEXT NOT NULL,
                        type TEXT NOT NULL,
                        categorie TEXT NOT NULL,
                        montantFcfa INTEGER NOT NULL,
                        clientOuNote TEXT NOT NULL,
                        moyenPaiement TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        syncStatus TEXT NOT NULL,
                        FOREIGN KEY(utilisateurId) REFERENCES utilisateurs(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_caisse_operations_utilisateurId ON caisse_operations(utilisateurId)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS devis_artisans (
                        id TEXT NOT NULL PRIMARY KEY,
                        utilisateurId TEXT NOT NULL,
                        referenceCode TEXT NOT NULL,
                        clientNom TEXT NOT NULL,
                        clientTel TEXT NOT NULL,
                        descriptionTravaux TEXT NOT NULL,
                        montantFcfa INTEGER NOT NULL,
                        acompteFcfa INTEGER NOT NULL,
                        statut TEXT NOT NULL,
                        moyenPaiement TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        syncStatus TEXT NOT NULL,
                        FOREIGN KEY(utilisateurId) REFERENCES utilisateurs(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_devis_artisans_utilisateurId ON devis_artisans(utilisateurId)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS coach_messages (
                        id TEXT NOT NULL PRIMARY KEY,
                        utilisateurId TEXT NOT NULL,
                        role TEXT NOT NULL,
                        texte TEXT NOT NULL,
                        langue TEXT NOT NULL,
                        metierContext TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        FOREIGN KEY(utilisateurId) REFERENCES utilisateurs(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_coach_messages_utilisateurId ON coach_messages(utilisateurId)")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS cours_generes_ia (
                        id TEXT NOT NULL PRIMARY KEY,
                        utilisateurId TEXT NOT NULL,
                        titre TEXT NOT NULL,
                        resume TEXT NOT NULL DEFAULT '',
                        notesEtudiant TEXT NOT NULL DEFAULT '',
                        coursOriginalId TEXT,
                        coursOriginalTitre TEXT NOT NULL DEFAULT '',
                        photoLocaleUri TEXT NOT NULL DEFAULT '',
                        contenuCours TEXT NOT NULL DEFAULT '',
                        quizJson TEXT NOT NULL DEFAULT '',
                        scoreDernierQuiz INTEGER NOT NULL DEFAULT 0,
                        totalQuestionsQuiz INTEGER NOT NULL DEFAULT 0,
                        quizPasse INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL,
                        syncStatus TEXT NOT NULL DEFAULT 'PENDING_UPLOAD',
                        FOREIGN KEY(utilisateurId) REFERENCES utilisateurs(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_cours_generes_ia_utilisateurId ON cours_generes_ia(utilisateurId)")
            }
        }

        fun getInstance(context: Context): LangoDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    LangoDatabase::class.java,
                    "lango_offline.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
