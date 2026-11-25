package com.example

import com.example.data.local.LangoDomainLogic
import com.example.data.local.LangoSeedData
import com.example.data.local.LessonContentCatalog
import com.example.data.local.entity.CaisseOperationEntity
import com.example.data.local.entity.DevisEntity
import com.example.data.local.entity.LangueCode
import com.example.ui.i18n.LangoI18n
import com.example.ui.util.QrCodeGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LangoDomainUnitTest {

    @Test
    fun `seed catalog contains RCA cities, skills, packs, opportunities and received candidates`() {
        assertTrue(LangoSeedData.rcaVilles.contains("Bangui - PK5"))
        assertTrue(LangoSeedData.rcaVilles.contains("Bimbo"))
        assertEquals(5, LangoSeedData.competences.size)
        assertEquals(5, LangoSeedData.contenus.size)
        val opps = LangoSeedData.buildOpportunites(1_000_000L)
        assertEquals(5, opps.size)
        assertTrue(opps.all { it.remunerationFcfa > 0 && it.contactTel.startsWith("+236") })
        val received = LangoSeedData.buildReceivedCandidatures(1_000_000L)
        assertTrue(received.size >= 4)
        assertTrue(received.all { it.passeportCode.startsWith("LG-") })
    }

    @Test
    fun `every training pack has valid bilingual lesson steps and quiz`() {
        LangoSeedData.contenus.forEach { pack ->
            val content = LessonContentCatalog.getForPack(pack.id)
            assertEquals(3, content.steps.size)
            assertEquals(3, content.quiz.size)
            content.steps.forEach { step ->
                assertFalse(step.titreFr.isBlank())
                assertFalse(step.titreSg.isBlank())
                assertFalse(step.texteFr.isBlank())
                assertFalse(step.texteSg.isBlank())
            }
            content.quiz.forEach { q ->
                assertEquals(3, q.optionsFr.size)
                assertEquals(3, q.optionsSg.size)
                assertTrue(q.correctIndex in 0..2)
            }
        }
    }

    @Test
    fun `bilingual i18n dictionary provides non-blank French and Sango copy`() {
        val fr = LangoI18n.forLang(LangueCode.FR)
        val sg = LangoI18n.forLang(LangueCode.SG)
        assertFalse(fr.slogan.isBlank())
        assertFalse(sg.slogan.isBlank())
        assertFalse(fr.consentTitle.contains("24.001").not())
        assertFalse(sg.consentTitle.contains("24.001").not())
    }

    @Test
    fun `reliability score increases with badges, verifications, portfolio, bio and clamps to 100`() {
        val base = LangoDomainLogic.computeReliabilityScore(
            badgeCount = 0,
            verifiedBadgeCount = 0,
            validVerificationsCount = 0,
            portfolioCount = 0,
            hasBio = false,
            isAvailable = false
        )
        assertEquals(25, base)

        val certified = LangoDomainLogic.computeReliabilityScore(
            badgeCount = 1,
            verifiedBadgeCount = 1,
            validVerificationsCount = 3,
            portfolioCount = 2,
            hasBio = true,
            isAvailable = true,
            hasProfilePhoto = true
        )
        assertEquals(90, certified)

        val capped = LangoDomainLogic.computeReliabilityScore(
            badgeCount = 5,
            verifiedBadgeCount = 5,
            validVerificationsCount = 10,
            portfolioCount = 5,
            hasBio = true,
            isAvailable = true
        )
        assertEquals(100, capped)
    }

    @Test
    fun `opportunity recommendation matches trade, earned badge, or user city`() {
        val opps = LangoSeedData.buildOpportunites(1_000_000L)
        val sewingOpp = opps.first { it.competenceId == "comp_couture" } // Bangui - PK5
        val agroOpp = opps.first { it.competenceId == "comp_agro" } // Bouar

        assertTrue(
            LangoDomainLogic.isRecommendedOpportunity(
                opp = sewingOpp,
                userCity = "Berbérati",
                primaryTradeId = "comp_couture",
                earnedCompetenceIds = emptySet()
            )
        )
        assertTrue(
            LangoDomainLogic.isRecommendedOpportunity(
                opp = sewingOpp,
                userCity = "Bangui - PK5",
                primaryTradeId = "comp_solaire",
                earnedCompetenceIds = emptySet()
            )
        )
        assertFalse(
            LangoDomainLogic.isRecommendedOpportunity(
                opp = agroOpp,
                userCity = "Bangui - PK5",
                primaryTradeId = "comp_couture",
                earnedCompetenceIds = setOf("comp_couture")
            )
        )
    }

    @Test
    fun `ussd sms alert command formats city and trade cleanly`() {
        assertEquals(
            "LANGO ALERTE BIMBO INSTALLATION SOLAIRE",
            LangoDomainLogic.buildUssdSmsCommand("Bimbo", "Installation Solaire")
        )
        assertEquals(
            "LANGO ALERTE BANGUI TOUT",
            LangoDomainLogic.buildUssdSmsCommand("", "")
        )
    }

    @Test
    fun `caisse summary computes entries exits and net balance in FCFA`() {
        val ops = listOf(
            CaisseOperationEntity("1", "u1", "ENTREE", "Couture", 15000),
            CaisseOperationEntity("2", "u1", "ENTREE", "Pose", 25000),
            CaisseOperationEntity("3", "u1", "SORTIE", "Matériel", 12000)
        )
        val summary = LangoDomainLogic.computeCaisseSummary(ops)
        assertEquals(40000, summary.totalEntrees)
        assertEquals(12000, summary.totalSorties)
        assertEquals(28000, summary.soldeNet)
    }

    @Test
    fun `devis sms text shows remaining balance and receipt status`() {
        val devis = DevisEntity(
            id = "d1",
            utilisateurId = "u1",
            referenceCode = "DEV-123456",
            clientNom = "Moussa",
            clientTel = "+23675001122",
            descriptionTravaux = "Installation kit 12V",
            montantFcfa = 50000,
            acompteFcfa = 20000,
            statut = "BROUILLON",
            moyenPaiement = "ORANGE_MONEY"
        )
        val text = LangoDomainLogic.buildDevisSmsText(devis, "Amina")
        assertTrue(text.contains("DEVIS LANGO DEV-123456"))
        assertTrue(text.contains("Reste dû : 30000 FCFA"))

        val paidText = LangoDomainLogic.buildDevisSmsText(devis.copy(statut = "PAYE"), "Amina")
        assertTrue(paidText.contains("REÇU PAYÉ DEV-123456"))
    }

    @Test
    fun `offline passport qr payload encodes and parses cleanly`() {
        val payload = LangoDomainLogic.encodePassportQrPayload(
            passportCode = "LG-A1B2C3",
            ownerName = "Amina",
            city = "Bangui - PK5",
            phone = "+23675001122",
            score = 88,
            badgesVerifiedRatio = "3/3",
            tradeLabel = "Couture & Retouches"
        )
        val parsed = LangoDomainLogic.parsePassportQrPayload(payload)
        assertNotNull(parsed)
        assertEquals("LG-A1B2C3", parsed?.passportCode)
        assertEquals("Amina", parsed?.ownerName)
        assertEquals(88, parsed?.score)
        assertEquals("3/3", parsed?.badgesSummary)

        val shortLookup = LangoDomainLogic.parsePassportQrPayload("lg-py8401")
        assertNotNull(shortLookup)
        assertEquals("LG-PY8401", shortLookup?.passportCode)

        assertNull(LangoDomainLogic.parsePassportQrPayload("INVALID_TEXT"))
    }

    @Test
    fun `pure kotlin qr generator produces valid finder patterns and format info`() {
        assertEquals(0x77C4, QrCodeGenerator.computeFormatBitsLevelL(0))
        val payload = LangoDomainLogic.encodePassportQrPayload(
            passportCode = "LG-A1B2C3",
            ownerName = "Amina",
            city = "Bangui - PK5",
            phone = "+23675001122",
            score = 88,
            badgesVerifiedRatio = "3/3",
            tradeLabel = "Couture"
        )
        val matrix = QrCodeGenerator.encode(payload)
        assertTrue(matrix.size >= 21)
        assertEquals(matrix.size * matrix.size, matrix.modules.size)
        // Top-left finder corners and center must be dark
        assertTrue(matrix.isDark(0, 0))
        assertTrue(matrix.isDark(3, 3))
        assertTrue(matrix.isDark(0, 6))
        assertTrue(matrix.isDark(6, 0))
        // White separator around top-left finder
        assertFalse(matrix.isDark(7, 0))
        assertFalse(matrix.isDark(0, 7))
        // Dark module above bottom-left format strip
        assertTrue(matrix.isDark(matrix.size - 8, 8))
    }

    @Test
    fun `sms permission snapshot computes full grant and fallback modes`() {
        val full = com.example.service.SmsPermissionSnapshot(
            hasTelephonyHardware = true,
            canSendSms = true,
            canReceiveSms = true
        )
        assertTrue(full.isFullyGranted)
        assertFalse(full.isFallbackComposerOnly)

        val deniedSend = com.example.service.SmsPermissionSnapshot(
            hasTelephonyHardware = true,
            canSendSms = false,
            canReceiveSms = true
        )
        assertFalse(deniedSend.isFullyGranted)
        assertTrue(deniedSend.isFallbackComposerOnly)

        val noTelephony = com.example.service.SmsPermissionSnapshot(
            hasTelephonyHardware = false,
            canSendSms = true,
            canReceiveSms = true
        )
        assertFalse(noTelephony.isFullyGranted)
        assertTrue(noTelephony.isFallbackComposerOnly)
    }

    @Test
    fun `sms alert parser detects Lango offer, verification, and Mobile Money messages`() {
        val offer = com.example.service.SmsPermissionService.parseLangoSmsAlert(
            sender = "+23675001122",
            body = "LANGO OFFRE Couture à PK5 45000 FCFA"
        )
        assertNotNull(offer)
        assertEquals("OFFRE", offer?.category)

        val verif = com.example.service.SmsPermissionService.parseLangoSmsAlert(
            sender = "+23672003344",
            body = "CODE LANGO VERIF-8821 valide par Client"
        )
        assertNotNull(verif)
        assertEquals("VERIFICATION", verif?.category)

        val mobileMoney = com.example.service.SmsPermissionService.parseLangoSmsAlert(
            sender = "OrangeMoney",
            body = "Orange Money: Vous avez reçu 25000 FCFA via *144#."
        )
        assertNotNull(mobileMoney)
        assertEquals("MOBILE_MONEY", mobileMoney?.category)

        assertNull(
            com.example.service.SmsPermissionService.parseLangoSmsAlert(
                sender = "+23670000000",
                body = "Bonjour, on se voit ce soir ?"
            )
        )
    }
}

