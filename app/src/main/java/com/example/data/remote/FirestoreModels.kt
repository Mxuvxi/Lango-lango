package com.example.data.remote

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@Keep
@IgnoreExtraProperties
data class UserPassportDoc(
    @DocumentId val id: String = "",
    val uid: String = "",
    val prenom: String = "",
    val telephone: String = "",
    val ville: String = "Bangui",
    val quartier: String = "",
    val languePref: String = "FR",
    val role: String = "JEUNE",
    val metierPrincipal: String = "",
    val niveauInitial: String = "DEBUTANT",
    val objectif: String = "EMPLOI",
    val bioCourte: String = "",
    val disponible: Boolean = true,
    val scoreFiabilite: Int = 25,
    val consentementLoi24001: Boolean = false,
    val badgesCount: Int = 0,
    val verifiedBadgesCount: Int = 0,
    val portfolioCount: Int = 0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CloudCaisseOperation(
    @DocumentId val id: String = "",
    val userId: String = "",
    val type: String = "ENTREE",
    val categorie: String = "",
    val montantFcfa: Int = 0,
    val clientOuNote: String = "",
    val moyenPaiement: String = "ESPECES",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CloudDevis(
    @DocumentId val id: String = "",
    val userId: String = "",
    val referenceCode: String = "",
    val clientNom: String = "",
    val clientTel: String = "",
    val descriptionTravaux: String = "",
    val montantFcfa: Int = 0,
    val acompteFcfa: Int = 0,
    val statut: String = "BROUILLON",
    val moyenPaiement: String = "ORANGE_MONEY",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CloudOpportunite(
    @DocumentId val id: String = "",
    val authorUid: String = "",
    val employeurNom: String = "",
    val titre: String = "",
    val type: String = "MISSION",
    val ville: String = "Bangui",
    val quartier: String = "",
    val competenceId: String = "",
    val remunerationFcfa: Int = 0,
    val descriptionCourte: String = "",
    val contactTel: String = "",
    val active: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
