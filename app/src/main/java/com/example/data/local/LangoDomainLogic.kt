package com.example.data.local

import com.example.data.local.entity.CaisseOperationEntity
import com.example.data.local.entity.DevisEntity
import com.example.data.local.entity.OpportuniteEntity

data class CaisseSummary(
    val totalEntrees: Int,
    val totalSorties: Int,
    val soldeNet: Int
)

data class OfflinePassportVerification(
    val isValid: Boolean,
    val passportCode: String,
    val ownerName: String,
    val city: String,
    val phone: String,
    val score: Int,
    val badgesSummary: String,
    val tradeLabel: String
)

object LangoDomainLogic {

    fun computeReliabilityScore(
        badgeCount: Int,
        verifiedBadgeCount: Int,
        validVerificationsCount: Int,
        portfolioCount: Int,
        hasBio: Boolean = false,
        isAvailable: Boolean = true,
        hasProfilePhoto: Boolean = false
    ): Int {
        val raw = 25 +
            (badgeCount * 12) +
            (validVerificationsCount * 8) +
            (verifiedBadgeCount * 10) +
            (portfolioCount * 5) +
            (if (hasBio) 4 else 0) +
            (if (isAvailable) 2 else 0) +
            (if (hasProfilePhoto) 3 else 0)
        return raw.coerceIn(0, 100)
    }

    fun isRecommendedOpportunity(
        opp: OpportuniteEntity,
        userCity: String,
        primaryTradeId: String,
        earnedCompetenceIds: Set<String>
    ): Boolean {
        val tradeMatch = primaryTradeId.isNotBlank() && opp.competenceId == primaryTradeId
        val badgeMatch = opp.competenceId in earnedCompetenceIds
        val cityMatch = userCity.isNotBlank() && opp.ville.equals(userCity.trim(), ignoreCase = true)
        return tradeMatch || badgeMatch || cityMatch
    }

    fun buildUssdSmsCommand(ville: String, metier: String): String {
        val cleanCity = ville.trim().ifBlank { "BANGUI" }.uppercase()
        val cleanTrade = metier.trim().ifBlank { "TOUT" }.uppercase()
        return "LANGO ALERTE $cleanCity $cleanTrade"
    }

    fun computeCaisseSummary(operations: List<CaisseOperationEntity>): CaisseSummary {
        var entrees = 0
        var sorties = 0
        for (op in operations) {
            if (op.type == "ENTREE") {
                entrees += op.montantFcfa
            } else if (op.type == "SORTIE") {
                sorties += op.montantFcfa
            }
        }
        return CaisseSummary(
            totalEntrees = entrees,
            totalSorties = sorties,
            soldeNet = entrees - sorties
        )
    }

    fun buildDevisSmsText(devis: DevisEntity, artisanName: String): String {
        val reste = (devis.montantFcfa - devis.acompteFcfa).coerceAtLeast(0)
        val kind = if (devis.statut == "PAYE") "REÇU PAYÉ" else "DEVIS LANGO"
        return buildString {
            append("$kind ${devis.referenceCode}\n")
            if (artisanName.isNotBlank()) append("Artisan : $artisanName\n")
            append("Client : ${devis.clientNom}\n")
            append("Travaux : ${devis.descriptionTravaux}\n")
            append("Total : ${devis.montantFcfa} FCFA\n")
            if (devis.acompteFcfa > 0) append("Acompte versé : ${devis.acompteFcfa} FCFA\n")
            append("Reste dû : $reste FCFA (${devis.moyenPaiement})")
        }
    }

    fun encodePassportQrPayload(
        passportCode: String,
        ownerName: String,
        city: String,
        phone: String,
        score: Int,
        badgesVerifiedRatio: String,
        tradeLabel: String
    ): String {
        fun clean(value: String, maxLen: Int): String =
            value.replace("|", "/").replace("\n", " ").trim().take(maxLen)

        return listOf(
            "LANGO1",
            clean(passportCode, 16),
            clean(ownerName, 24),
            clean(city, 24),
            clean(phone, 18),
            score.coerceIn(0, 100).toString(),
            clean(badgesVerifiedRatio, 10),
            clean(tradeLabel.ifBlank { "Artisan" }, 24)
        ).joinToString("|")
    }

    fun parsePassportQrPayload(raw: String): OfflinePassportVerification? {
        val trimmed = raw.trim()
        if (trimmed.isBlank()) return null

        if (trimmed.startsWith("LANGO1|")) {
            val parts = trimmed.split("|")
            if (parts.size < 8) return null
            val code = parts[1].trim()
            val name = parts[2].trim()
            val score = parts[5].trim().toIntOrNull()?.coerceIn(0, 100) ?: return null
            if (!code.startsWith("LG-") || name.isBlank()) return null
            return OfflinePassportVerification(
                isValid = true,
                passportCode = code,
                ownerName = name,
                city = parts[3].trim(),
                phone = parts[4].trim(),
                score = score,
                badgesSummary = parts[6].trim(),
                tradeLabel = parts[7].trim()
            )
        }

        // Also allow manual short-code lookup like "LG-PY8401"
        val upper = trimmed.uppercase()
        if (upper.startsWith("LG-") && upper.length in 5..16) {
            return OfflinePassportVerification(
                isValid = true,
                passportCode = upper,
                ownerName = "",
                city = "",
                phone = "",
                score = 0,
                badgesSummary = "",
                tradeLabel = ""
            )
        }
        return null
    }
}
