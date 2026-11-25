package com.example.data.local

import com.example.data.local.entity.CompetenceEntity
import com.example.data.local.entity.ContenuPedagogiqueEntity
import com.example.data.local.entity.OpportuniteEntity
import com.example.data.local.entity.CandidatureRecueEntity

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
        ),
        CompetenceEntity(
            id = "comp_photo",
            nomFr = "Photographie & Reportage Pro",
            nomSg = "Photo na video ti Afrika",
            categorie = "Média",
            iconeKey = "camera"
        ),
        CompetenceEntity(
            id = "comp_cinema",
            nomFr = "Réalisation & Vidéo Mobile",
            nomSg = "Sarango video na smartphone",
            categorie = "Média",
            iconeKey = "movie"
        ),
        CompetenceEntity(
            id = "comp_ia",
            nomFr = "Intelligence Artificielle & Numérique",
            nomSg = "IA na Kete Kua ti Digital",
            categorie = "Numérique",
            iconeKey = "ai"
        ),
        CompetenceEntity(
            id = "comp_langues",
            nomFr = "Langues & Communication Pro",
            nomSg = "Amango na Tënë (Français/Anglais)",
            categorie = "Communication",
            iconeKey = "language"
        ),
        CompetenceEntity(
            id = "comp_batiment",
            nomFr = "Maçonnerie & Éco-Construction",
            nomSg = "Lekengo da na sese",
            categorie = "Bâtiment",
            iconeKey = "building"
        ),
        CompetenceEntity(
            id = "comp_sante",
            nomFr = "Santé Communautaire & Secours",
            nomSg = "Bata ngangu ti tere & Secours",
            categorie = "Santé",
            iconeKey = "health"
        ),
        CompetenceEntity(
            id = "comp_mecanique",
            nomFr = "Mécanique Moto & Entretien",
            nomSg = "Lekengo Moto & Kua",
            categorie = "Technique",
            iconeKey = "moto"
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
        ),
        ContenuPedagogiqueEntity(
            id = "pack_photo_1",
            competenceId = "comp_photo",
            titreFr = "Lumière naturelle & Cadrage Pro",
            titreSg = "Mû ngangu ti wâ ti lâ nzoni",
            resumeFr = "Maîtriser la Golden Hour, le contre-jour et la règle des tiers en reportage photo.",
            resumeSg = "Mû ngangu ti wâ ti lâ na zia foto na ndo so alingbi.",
            dureeMin = 15,
            tailleKo = 3200,
            estTelecharge = true,
            audioDureeSec = 300
        ),
        ContenuPedagogiqueEntity(
            id = "pack_cinema_1",
            competenceId = "comp_cinema",
            titreFr = "Réalisation & Vidéo Mobile",
            titreSg = "Sarango video na smartphone",
            resumeFr = "Scénario, plans de coupe B-Roll et stabilité pour documentaires et reportages.",
            resumeSg = "Sû mbeti ti video na kete plan ti fa kaye ti Afrika.",
            dureeMin = 18,
            tailleKo = 4100,
            estTelecharge = false,
            audioDureeSec = 360
        ),
        ContenuPedagogiqueEntity(
            id = "pack_ia_1",
            competenceId = "comp_ia",
            titreFr = "IA & Assistants Mobiles pour Entrepreneurs",
            titreSg = "IA na Téléfone ti Lengo Kua",
            resumeFr = "Utiliser l'intelligence artificielle pour rédiger des messages pro, devis et idées de business.",
            resumeSg = "Sã IA ti sû mbeti ti kosa na mû wango ti biisi.",
            dureeMin = 12,
            tailleKo = 2100,
            estTelecharge = false,
            audioDureeSec = 250
        ),
        ContenuPedagogiqueEntity(
            id = "pack_langues_1",
            competenceId = "comp_langues",
            titreFr = "Français Pro & Anglais Commercial",
            titreSg = "Français na Anglais ti Kua",
            resumeFr = "Phrases clés pour négocier avec des clients, rédiger des factures et accueillir des touristes.",
            resumeSg = "Tënë ti kapa na client, sû facture na tisa azo ti kodro nde.",
            dureeMin = 15,
            tailleKo = 2900,
            estTelecharge = true,
            audioDureeSec = 320
        ),
        ContenuPedagogiqueEntity(
            id = "pack_batiment_1",
            competenceId = "comp_batiment",
            titreFr = "Briques de Terre Stabilisée & Maçonnerie",
            titreSg = "Lekengo da na sese na wen",
            resumeFr = "Techniques durables de construction économique et résistante à la saison des pluies.",
            resumeSg = "Lekengo da so aninga na ngoi ti ngu ti tï.",
            dureeMin = 16,
            tailleKo = 3500,
            estTelecharge = false,
            audioDureeSec = 340
        ),
        ContenuPedagogiqueEntity(
            id = "pack_sante_1",
            competenceId = "comp_sante",
            titreFr = "Premiers Secours & Eau Potable",
            titreSg = "Batango tere na Ngu Nzoni",
            resumeFr = "Gestes d'urgence de base, désinfection de l'eau et prévention des maladies courantes.",
            resumeSg = "Mû maboko na zo so agomer na sukulungo ngu.",
            dureeMin = 14,
            tailleKo = 2600,
            estTelecharge = false,
            audioDureeSec = 290
        ),
        ContenuPedagogiqueEntity(
            id = "pack_mecanique_1",
            competenceId = "comp_mecanique",
            titreFr = "Entretien & Réparation Moto Express",
            titreSg = "Lekengo Moto na vidange",
            resumeFr = "Vidange, tension de chaîne, changement de bougie et rustines pour motos-taxis (Djakarta).",
            resumeSg = "Gbian mafuta ti moto, kanga chaîne na bougie.",
            dureeMin = 15,
            tailleKo = 3100,
            estTelecharge = true,
            audioDureeSec = 310
        )
    )

    fun buildOpportunites(nowMs: Long = System.currentTimeMillis()): List<OpportuniteEntity> {
        val twoWeeks = 14L * 24L * 3600L * 1000L
        return listOf(
            OpportuniteEntity(
                id = "opp_1",
                employeurNom = "Atelier Solaire Bangui",
                titre = "Technicien installation kits solaires 12V",
                type = "MISSION",
                ville = "Bangui - PK5",
                quartier = "PK5 Marché",
                competenceId = "comp_solaire",
                remunerationFcfa = 45000,
                descriptionCourte = "Installation de 3 kits solaires pour des boutiques au marché central.",
                contactTel = "+23675001122",
                expireLe = nowMs + twoWeeks,
                estActive = true
            ),
            OpportuniteEntity(
                id = "opp_2",
                employeurNom = "ONG Espoir Centrafrique",
                titre = "Formateur transformation agroalimentaire manioc",
                type = "STAGE",
                ville = "Bimbo",
                quartier = "Centre",
                competenceId = "comp_agro",
                remunerationFcfa = 60000,
                descriptionCourte = "Accompagnement de groupements féminins pour le séchage hygiénique.",
                contactTel = "+23672113344",
                expireLe = nowMs + twoWeeks,
                estActive = true
            ),
            OpportuniteEntity(
                id = "opp_3",
                employeurNom = "Studio Oubangui Média",
                titre = "Caméraman & Photographe reportage culturel",
                type = "EMPLOI",
                ville = "Bangui - Gobongo",
                quartier = "Gobongo",
                competenceId = "comp_photo",
                remunerationFcfa = 75000,
                descriptionCourte = "Couverture photo et vidéo mobile de festivals et reportages d'artisanat.",
                contactTel = "+23670558899",
                expireLe = nowMs + twoWeeks,
                estActive = true
            )
        )
    }

    fun buildReceivedCandidatures(): List<CandidatureRecueEntity> {
        val now = System.currentTimeMillis()
        return listOf(
            CandidatureRecueEntity(
                id = "rec_cand_101",
                opportuniteId = "opp_1",
                candidatNom = "Benoit Kossi",
                candidatTel = "+23675123456",
                candidatVille = "Bangui - PK5",
                metierLabel = "Installation Solaire",
                scoreFiabilite = 85,
                badgesValides = 2,
                messageCourt = "Certifié Lango 3/3, disponible immédiatement pour les kits solaires.",
                canalEnvoi = "APP",
                statut = "ENVOYEE",
                noteEmployeur = "",
                passeportCode = "LG-USR101",
                createdAt = now - 3600000L
            ),
            CandidatureRecueEntity(
                id = "rec_cand_102",
                opportuniteId = "opp_3",
                candidatNom = "Sarah Ngoko",
                candidatTel = "+23672987654",
                candidatVille = "Bangui - Gobongo",
                metierLabel = "Photographie & Reportage Pro",
                scoreFiabilite = 92,
                badgesValides = 3,
                messageCourt = "Expérience de 2 ans en reportage mobile et cadrage pro.",
                canalEnvoi = "SMS",
                statut = "RETENUE",
                noteEmployeur = "Excellent portfolio, convoquée pour entretien.",
                passeportCode = "LG-USR102",
                createdAt = now - 7200000L
            )
        )
    }
}
