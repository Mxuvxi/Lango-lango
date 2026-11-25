package com.example.data.local

import com.example.data.local.entity.CompetenceEntity
import com.example.data.local.entity.ContenuPedagogiqueEntity
import com.example.data.local.entity.OpportuniteEntity

object LangoSeedData {

    val rcaVilles = listOf(
        "Bangui - PK5",
        "Bangui - Gobongo",
        "Bangui - Fouh",
        "Bimbo",
        "Bégoua",
        "Berbérati",
        "Bouar",
        "Bambari"
    )

    val competences = listOf(
        CompetenceEntity(
            id = "comp_couture",
            nomFr = "Couture & Retouches",
            nomSg = "Fungo bongo",
            categorie = "Artisanat",
            iconeKey = "sewing"
        ),
        CompetenceEntity(
            id = "comp_solaire",
            nomFr = "Installation Solaire",
            nomSg = "Lengo ti lâ",
            categorie = "Énergie",
            iconeKey = "solar"
        ),
        CompetenceEntity(
            id = "comp_reparation",
            nomFr = "Réparation Téléphone & Moto",
            nomSg = "Lekengo téléfone & moto",
            categorie = "Technique",
            iconeKey = "repair"
        ),
        CompetenceEntity(
            id = "comp_agro",
            nomFr = "Transformation Agroalimentaire",
            nomSg = "Sarango kobe ti gâ",
            categorie = "Agro",
            iconeKey = "agro"
        ),
        CompetenceEntity(
            id = "comp_gestion",
            nomFr = "Caisse & Mobile Money",
            nomSg = "Bata nginza & Mobile Money",
            categorie = "Commerce",
            iconeKey = "commerce"
        )
    )

    val contenus = listOf(
        ContenuPedagogiqueEntity(
            id = "pack_couture_1",
            competenceId = "comp_couture",
            titreFr = "Couper un pagne & finitions propres",
            titreSg = "Fâ bongo na nzoni kua",
            resumeFr = "Mesures de base, coupe droite sans gaspillage et points solides à la main ou machine.",
            resumeSg = "Mû mesure nzoni, fâ bongo si a buba pepe na kua ti maboko.",
            dureeMin = 12,
            tailleKo = 2400,
            estTelecharge = true,
            audioDureeSec = 240
        ),
        ContenuPedagogiqueEntity(
            id = "pack_solaire_1",
            competenceId = "comp_solaire",
            titreFr = "Kit solaire 12V pour boutique",
            titreSg = "Kete panneau ti lâ 12V",
            resumeFr = "Branchement sûr panneau, régulateur et batterie sans court-circuit.",
            resumeSg = "Zia panneau, régulateur na batterie nzoni ti tene a gbi pepe.",
            dureeMin = 15,
            tailleKo = 3100,
            estTelecharge = false,
            audioDureeSec = 310
        ),
        ContenuPedagogiqueEntity(
            id = "pack_reparation_1",
            competenceId = "comp_reparation",
            titreFr = "Diagnostic panne charge téléphone",
            titreSg = "Gi lege ti téléfone so a charger pepe",
            resumeFr = "Tester câble, connecteur USB et batterie avec un multimètre simple.",
            resumeSg = "Bâ câble, connecteur na batterie ti hinga ndo so a buba.",
            dureeMin = 10,
            tailleKo = 1850,
            estTelecharge = true,
            audioDureeSec = 200
        ),
        ContenuPedagogiqueEntity(
            id = "pack_agro_1",
            competenceId = "comp_agro",
            titreFr = "Séchage hygiénique manioc & arachide",
            titreSg = "Hurungo gozo na karako nzoni",
            resumeFr = "Techniques propres de séchage, pesée et conservation en sacs hermétiques.",
            resumeSg = "Huru gozo na karako na ndo so a yeke nzoni ti bata na sac.",
            dureeMin = 14,
            tailleKo = 2750,
            estTelecharge = false,
            audioDureeSec = 270
        ),
        ContenuPedagogiqueEntity(
            id = "pack_gestion_1",
            competenceId = "comp_gestion",
            titreFr = "Tenir son cahier & frais Mobile Money",
            titreSg = "Sû nginza na mbeti & Mobile Money",
            resumeFr = "Séparer capital et bénéfice, calculer les frais Orange/Telecel Money.",
            resumeSg = "Kangbi nginza ti kua na bénéfice, diko frais ti Mobile Money.",
            dureeMin = 9,
            tailleKo = 1400,
            estTelecharge = false,
            audioDureeSec = 180
        )
    )

    fun buildOpportunites(nowMs: Long = System.currentTimeMillis()): List<OpportuniteEntity> {
        val twoWeeks = 14L * 24L * 3600L * 1000L
        return listOf(
            OpportuniteEntity(
                id = "opp_1",
                employeurNom = "Atelier Kéti Couture",
                titre = "Aide-couturier(ère) pour tenues scolaires",
                type = "MISSION",
                ville = "Bangui - PK5",
                quartier = "PK5 Marché",
                competenceId = "comp_couture",
                remunerationFcfa = 45000,
                descriptionCourte = "Mission de 3 semaines pour ourlets et assemblage. Machine fournie sur place.",
                contactTel = "+23675001122",
                expireLe = nowMs + twoWeeks
            ),
            OpportuniteEntity(
                id = "opp_2",
                employeurNom = "ONG Lumière Oubangui",
                titre = "Technicien(ne) pose kits solaires ruraux",
                type = "EMPLOI",
                ville = "Bimbo",
                quartier = "Bimbo Centre",
                competenceId = "comp_solaire",
                remunerationFcfa = 85000,
                descriptionCourte = "Installation et maintenance de kits 12V dans des centres communautaires.",
                contactTel = "+23672003344",
                expireLe = nowMs + twoWeeks
            ),
            OpportuniteEntity(
                id = "opp_3",
                employeurNom = "Kiosque Centrafrique Répare",
                titre = "Apprenti(e) réparation smartphones",
                type = "STAGE",
                ville = "Bangui - Gobongo",
                quartier = "Carrefour Gobongo",
                competenceId = "comp_reparation",
                remunerationFcfa = 30000,
                descriptionCourte = "Stage rémunéré + prime par appareil réparé. Ouvert aux débutants motivés.",
                contactTel = "+23675005566",
                expireLe = nowMs + twoWeeks
            ),
            OpportuniteEntity(
                id = "opp_4",
                employeurNom = "Coopérative Femmes de Bouar",
                titre = "Conditionnement pâte d'arachide",
                type = "SERVICE",
                ville = "Bouar",
                quartier = "Quartier Haoussa",
                competenceId = "comp_agro",
                remunerationFcfa = 55000,
                descriptionCourte = "Tri, torréfaction et mise en pot hygiénique pour livraison hebdomadaire.",
                contactTel = "+23670007788",
                expireLe = nowMs + twoWeeks
            ),
            OpportuniteEntity(
                id = "opp_5",
                employeurNom = "Boutique Sango Services",
                titre = "Gérant(e) point Mobile Money & caisse",
                type = "EMPLOI",
                ville = "Berbérati",
                quartier = "Centre-ville",
                competenceId = "comp_gestion",
                remunerationFcfa = 65000,
                descriptionCourte = "Tenue de caisse journalière Orange Money / Telecel Money et accueil clients.",
                contactTel = "+23672009900",
                expireLe = nowMs + twoWeeks
            )
        )
    }

    fun buildReceivedCandidatures(
        nowMs: Long = System.currentTimeMillis()
    ): List<com.example.data.local.entity.CandidatureRecueEntity> {
        return listOf(
            com.example.data.local.entity.CandidatureRecueEntity(
                id = "cand_rec_1",
                opportuniteId = "opp_1",
                candidatNom = "Prisca Yango",
                candidatTel = "+23675214490",
                candidatVille = "Bangui - PK5",
                metierLabel = "Couture & Retouches",
                scoreFiabilite = 84,
                badgesValides = 3,
                messageCourt = "5 ans d'expérience sur machine industrielle et tenues scolaires à PK5.",
                canalEnvoi = "APP",
                statut = "ENVOYEE",
                passeportCode = "LG-PY8401",
                createdAt = nowMs - 3_600_000L
            ),
            com.example.data.local.entity.CandidatureRecueEntity(
                id = "cand_rec_2",
                opportuniteId = "opp_2",
                candidatNom = "Benoît Kossi",
                candidatTel = "+23672441805",
                candidatVille = "Bimbo",
                metierLabel = "Installation Solaire",
                scoreFiabilite = 91,
                badgesValides = 3,
                messageCourt = "Certifié 3 tiers en pose de kits 12V et maintenance batterie.",
                canalEnvoi = "SMS",
                statut = "VUE",
                noteEmployeur = "Bon profil technique à appeler demain",
                passeportCode = "LG-BK9102",
                createdAt = nowMs - 7_200_000L
            ),
            com.example.data.local.entity.CandidatureRecueEntity(
                id = "cand_rec_3",
                opportuniteId = "opp_3",
                candidatNom = "Serge Ndongo",
                candidatTel = "+23675983011",
                candidatVille = "Bangui - Gobongo",
                metierLabel = "Réparation Téléphone & Moto",
                scoreFiabilite = 68,
                badgesValides = 2,
                messageCourt = "Sait souder connecteurs de charge et diagnostiquer batteries au multimètre.",
                canalEnvoi = "APP",
                statut = "RETENUE",
                noteEmployeur = "Convoqué pour essai atelier lundi 8h",
                passeportCode = "LG-SN6803",
                createdAt = nowMs - 18_000_000L
            ),
            com.example.data.local.entity.CandidatureRecueEntity(
                id = "cand_rec_4",
                opportuniteId = "opp_5",
                candidatNom = "Nadège Mboli",
                candidatTel = "+23672056177",
                candidatVille = "Berbérati",
                metierLabel = "Caisse & Mobile Money",
                scoreFiabilite = 79,
                badgesValides = 3,
                messageCourt = "Gestion de kiosque Orange/Telecel Money depuis 2 ans sans erreur de caisse.",
                canalEnvoi = "APP",
                statut = "ENVOYEE",
                passeportCode = "LG-NM7904",
                createdAt = nowMs - 24_000_000L
            )
        )
    }
}
