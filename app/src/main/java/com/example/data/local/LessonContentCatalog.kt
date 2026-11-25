package com.example.data.local

data class LessonStep(
    val numero: Int,
    val titreFr: String,
    val titreSg: String,
    val texteFr: String,
    val texteSg: String,
    val conseilPratiqueFr: String,
    val conseilPratiqueSg: String
)

data class QuizQuestion(
    val id: String,
    val questionFr: String,
    val questionSg: String,
    val optionsFr: List<String>,
    val optionsSg: List<String>,
    val correctIndex: Int
)

data class PackLessonContent(
    val contenuId: String,
    val badgeTitreFr: String,
    val badgeTitreSg: String,
    val steps: List<LessonStep>,
    val quiz: List<QuizQuestion>
)

object LessonContentCatalog {

    private val packs: Map<String, PackLessonContent> = mapOf(
        "pack_couture_1" to PackLessonContent(
            contenuId = "pack_couture_1",
            badgeTitreFr = "Coupe & Finitions Pagne",
            badgeTitreSg = "Fâ bongo nzoni",
            steps = listOf(
                LessonStep(
                    numero = 1,
                    titreFr = "Prendre les 3 mesures clés",
                    titreSg = "Mû mesure ota ti kota",
                    texteFr = "Avant de couper un pagne, mesure toujours la taille, les hanches et la longueur souhaitée avec le mètre ruban bien plat, sans tirer sur le tissu.",
                    texteSg = "Kozo ti fâ bongo, mû mesure ti gbe ti yâ, ti ngbonga na ti yongo ni nzoni, mo gboto bongo ni pepe.",
                    conseilPratiqueFr = "Astuce : note les 3 chiffres au crayon ou sur ton téléphone Lango.",
                    conseilPratiqueSg = "Wango : sû anuméro ota ni na mbeti wala na téléfone."
                ),
                LessonStep(
                    numero = 2,
                    titreFr = "Plier et tracer droit à la craie",
                    titreSg = "Kanga bongo na sû lege na craie",
                    texteFr = "Pose le tissu sur une table propre, aligne les lisières et trace avec une craie de tailleur en laissant 1,5 cm de marge pour l’ourlet.",
                    texteSg = "Zia bongo na ndo ti table so a yeke nzoni, bâ yanga ni alingbi si mo sû na craie, zia kete ndo ti kanga ni.",
                    conseilPratiqueFr = "Astuce : vérifie le sens du motif du pagne avant de couper.",
                    conseilPratiqueSg = "Wango : bâ dessin ti bongo ni kozo si mo fâ."
                ),
                LessonStep(
                    numero = 3,
                    titreFr = "Finitions propres sans fil qui dépasse",
                    titreSg = "Hunzi kua nzoni sân kamba ti yeke",
                    texteFr = "Fais un point d’arrêt au début et à la fin de chaque couture, puis coupe les fils à ras et repasse la couture à plat.",
                    texteSg = "Kanga nda ti kamba na tongo nda ni na hunzingo ni, fâ akamba so ayeke na gigi si mo repasser ni.",
                    conseilPratiqueFr = "Une finition propre fidélise le client dès le premier essayage.",
                    conseilPratiqueSg = "Nzoni kua a sara si client a kiri na mo lâ kue."
                )
            ),
            quiz = listOf(
                QuizQuestion(
                    id = "q_cout_1",
                    questionFr = "Quelle marge faut-il laisser au bord pour l’ourlet ?",
                    questionSg = "Ndo oke a lingbi ti zia ti kanga yanga ti bongo ?",
                    optionsFr = listOf("0 cm (couper à ras)", "1,5 cm de marge", "10 cm"),
                    optionsSg = listOf("0 cm", "1,5 cm ti kanga", "10 cm"),
                    correctIndex = 1
                ),
                QuizQuestion(
                    id = "q_cout_2",
                    questionFr = "Que vérifier avant de couper un pagne à motifs ?",
                    questionSg = "Nye la a lingbi ti bâ kozo ti fâ bongo ?",
                    optionsFr = listOf("Le sens du motif", "La couleur des ciseaux", "L'heure du marché"),
                    optionsSg = listOf("Lege ti dessin ti bongo", "Couleur ti ciseaux", "Ngoi ti marché"),
                    correctIndex = 0
                ),
                QuizQuestion(
                    id = "q_cout_3",
                    questionFr = "Pourquoi faire un point d’arrêt en début et fin de couture ?",
                    questionSg = "Ngbanga ti nye mo kanga nda ti kamba ?",
                    optionsFr = listOf("Pour aller plus vite", "Pour éviter que la couture ne se défasse", "Pour économiser la craie"),
                    optionsSg = listOf("Ti sara hio", "Ti tene bongo a zi pepe", "Ti bata craie"),
                    correctIndex = 1
                )
            )
        ),
        "pack_solaire_1" to PackLessonContent(
            contenuId = "pack_solaire_1",
            badgeTitreFr = "Installateur Kit Solaire 12V",
            badgeTitreSg = "Zo ti zia panneau ti lâ 12V",
            steps = listOf(
                LessonStep(
                    numero = 1,
                    titreFr = "Orientation du panneau au soleil",
                    titreSg = "Zia panneau na lê ti lâ",
                    texteFr = "En RCA, incline le panneau légèrement vers le sud/nord selon la saison sans aucune ombre d’arbre ou de tôle voisine entre 9h et 15h.",
                    texteSg = "Na RCA, zia panneau na nduzu na ndo so gbe ti keke a kanga lâ pepe ti londo na 9h ti si na 15h.",
                    conseilPratiqueFr = "Nettoie la poussière rouge (latérite) chaque semaine avec un chiffon doux.",
                    conseilPratiqueSg = "Sukula fuku ti sese na ndo ti panneau yenga kue."
                ),
                LessonStep(
                    numero = 2,
                    titreFr = "Ordre de branchement sécurisé",
                    titreSg = "Lege ti zia akamba sân kpale",
                    texteFr = "Branche TOUJOURS la batterie sur le régulateur en premier, puis le panneau solaire, et enfin les ampoules 12V.",
                    texteSg = "Kanga kamba ti batterie na régulateur KOZO, na peko ni panneau, na ndani ampoule 12V.",
                    conseilPratiqueFr = "Respecte le rouge (+) et le noir (-) pour ne pas griller le régulateur.",
                    conseilPratiqueSg = "Bâ bengba (+) na vuko (-) nzoni si régulateur a buba pepe."
                ),
                LessonStep(
                    numero = 3,
                    titreFr = "Protéger la batterie",
                    titreSg = "Bata batterie aninga",
                    texteFr = "Ne laisse jamais la batterie descendre sous 11,5V et place-la sur un support sec en bois, pas directement sur le sol humide.",
                    texteSg = "Zia batterie a tï na gbe ti 11,5V pepe, zia lo na ndo ti keke so a huru.",
                    conseilPratiqueFr = "Le régulateur protège contre la surcharge : ne le contourne jamais.",
                    conseilPratiqueSg = "Régulateur a bata batterie, zia ni lakue."
                )
            ),
            quiz = listOf(
                QuizQuestion(
                    id = "q_sol_1",
                    questionFr = "Que faut-il brancher EN PREMIER sur le régulateur ?",
                    questionSg = "Nye la a lingbi ti kanga KOZO na régulateur ?",
                    optionsFr = listOf("Les ampoules", "La batterie 12V", "Le panneau solaire"),
                    optionsSg = listOf("Ampoule", "Batterie 12V", "Panneau ti lâ"),
                    correctIndex = 1
                ),
                QuizQuestion(
                    id = "q_sol_2",
                    questionFr = "Comment garder un bon rendement malgré la poussière en saison sèche ?",
                    questionSg = "Tongana nye ti bata ngangu ti panneau na ngoi ti buru ?",
                    optionsFr = listOf("Essuyer le panneau avec un chiffon doux", "Mettre de l’huile dessus", "Couvrir le panneau"),
                    optionsSg = listOf("Sukula na bongo so a yeke woko", "Tuku mafuta da", "Kanga panneau ni"),
                    correctIndex = 0
                ),
                QuizQuestion(
                    id = "q_sol_3",
                    questionFr = "Où poser la batterie dans une boutique ?",
                    questionSg = "Ndo wa la a yeke nzoni ti zia batterie ?",
                    optionsFr = listOf("Sur la terre mouillée", "En plein soleil", "Au sec sur un support en bois"),
                    optionsSg = listOf("Na ndo ti sese ti ngu", "Na gbe ti lâ", "Na ndo ti keke so a huru"),
                    correctIndex = 2
                )
            )
        ),
        "pack_reparation_1" to PackLessonContent(
            contenuId = "pack_reparation_1",
            badgeTitreFr = "Diagnostic Charge Smartphone",
            badgeTitreSg = "Lekengo téléfone so a charger pepe",
            steps = listOf(
                LessonStep(
                    numero = 1,
                    titreFr = "Tester d’abord câble et chargeur",
                    titreSg = "Tara câble na chargeur kozo",
                    texteFr = "7 pannes de charge sur 10 viennent d’un câble cassé ou d’un adaptateur instable. Teste toujours avec un câble neuf connu avant d’ouvrir le téléphone.",
                    texteSg = "Mingi ti kpale ti charge a londo na câble. Tara na fini câble kozo si mo zi téléfone.",
                    conseilPratiqueFr = "Vérifie aussi s’il n’y a pas de poussière tassée au fond du port USB.",
                    conseilPratiqueSg = "Bâ wala fuku a kanga ya ti port USB pepe."
                ),
                LessonStep(
                    numero = 2,
                    titreFr = "Nettoyer et inspecter le port USB",
                    titreSg = "Sukula na bâ port USB",
                    texteFr = "Avec une aiguille fine ou brosse sèche + alcool isopropylique, retire les peluches puis vérifie à la loupe si une broche est tordue.",
                    texteSg = "Sukula ya ti port USB nzoni na brosse so a huru si mo bâ wala kete wen a kuku.",
                    conseilPratiqueFr = "Ne force jamais un câble dans un port oxydé : risque de court-circuit.",
                    conseilPratiqueSg = "Pusu câble na ngangu pepe."
                ),
                LessonStep(
                    numero = 3,
                    titreFr = "Mesurer la batterie au multimètre",
                    titreSg = "Hinga ngangu ti batterie na multimètre",
                    texteFr = "En mode DC (20V), une batterie saine affiche entre 3,7V et 4,2V. En dessous de 3,0V, elle doit être réveillée avec précaution.",
                    texteSg = "Na multimètre, nzoni batterie a yeke na popo ti 3,7V na 4,2V.",
                    conseilPratiqueFr = "Explique clairement le devis au client avant toute soudure.",
                    conseilPratiqueSg = "Fa ngere ti kua na client kozo si mo leke."
                )
            ),
            quiz = listOf(
                QuizQuestion(
                    id = "q_rep_1",
                    questionFr = "Quelle est la première vérification avant de démonter le téléphone ?",
                    questionSg = "Nye la mo sara kozo ti zi téléfone ?",
                    optionsFr = listOf("Changer l’écran", "Tester avec un câble et chargeur fiables", "Chauffer la carte mère"),
                    optionsSg = listOf("Gbian écran", "Tara na nzoni câble na chargeur", "Zia wâ na carte mère"),
                    correctIndex = 1
                ),
                QuizQuestion(
                    id = "q_rep_2",
                    questionFr = "Quelle tension indique une batterie smartphone chargée/saine ?",
                    questionSg = "Volt oke a fa so batterie ayeke nzoni ?",
                    optionsFr = listOf("3,7V à 4,2V", "0,5V", "12V"),
                    optionsSg = listOf("3,7V ti si na 4,2V", "0,5V", "12V"),
                    correctIndex = 0
                ),
                QuizQuestion(
                    id = "q_rep_3",
                    questionFr = "Que faire avant d’engager une soudure payante ?",
                    questionSg = "Nye la mo sara kozo ti to nda ti soudure ?",
                    optionsFr = listOf("Donner le prix et obtenir l’accord du client", "Effacer les photos", "Vendre la batterie"),
                    optionsSg = listOf("Fa ngere na client si lo yeda", "Lungula afoto", "Kä batterie"),
                    correctIndex = 0
                )
            )
        )
    )

    fun getForPack(contenuId: String): PackLessonContent {
        return packs[contenuId] ?: PackLessonContent(
            contenuId = contenuId,
            badgeTitreFr = "Maîtrise Pratique Lango",
            badgeTitreSg = "Hinga kua ti Lango",
            steps = listOf(
                LessonStep(
                    numero = 1,
                    titreFr = "Préparation et hygiène / sécurité",
                    titreSg = "Lekengo tere na batango ndo",
                    texteFr = "Prépare ton espace de travail proprement, vérifie tes outils et protège tes matières premières contre l’humidité et la poussière.",
                    texteSg = "Leke ndo ti kua ti mo nzoni, bâ aye ti kua kue si mo bata ni na sioni pupu na ngu.",
                    conseilPratiqueFr = "La propreté et la régularité inspirent confiance aux clients.",
                    conseilPratiqueSg = "Propreté a ga na confiance ti aclient."
                ),
                LessonStep(
                    numero = 2,
                    titreFr = "Mesurer, peser et noter chaque étape",
                    titreSg = "Diko, tara na sû aye kue",
                    texteFr = "Sépare toujours l’argent du fonds de roulement de ton bénéfice net, et contrôle la qualité à chaque étape.",
                    texteSg = "Kangbi nginza ti kua na bénéfice ti mo, bâ nzoni ti kua lâ kue.",
                    conseilPratiqueFr = "Un petit cahier évite les pertes invisibles en fin de semaine.",
                    conseilPratiqueSg = "Kete mbeti ti sûr nginza a bata mo."
                ),
                LessonStep(
                    numero = 3,
                    titreFr = "Livrer à l’heure et demander un avis",
                    titreSg = "Mû kua na ngoi ni si hunda avis",
                    texteFr = "Respecte le délai promis au client et demande-lui une vérification Lango dès que la prestation est réussie.",
                    texteSg = "Sara kua na ngoi so mo fa na client si mo hunda lo ti fa nzoni ti kua ti mo.",
                    conseilPratiqueFr = "3 avis validés transforment ton badge en profil vérifié.",
                    conseilPratiqueSg = "Avis ota a sara si iri ti mo aga kota."
                )
            ),
            quiz = listOf(
                QuizQuestion(
                    id = "q_gen_1",
                    questionFr = "Pourquoi séparer le capital du bénéfice ?",
                    questionSg = "Ngbanga ti nye a kangbi nginza ti kua na bénéfice ?",
                    optionsFr = listOf("Pour pouvoir racheter la marchandise", "Pour dépenser plus vite", "Sans raison"),
                    optionsSg = listOf("Ti kiri ti vo aye ti kua", "Ti buba nginza", "Sân nda ni"),
                    correctIndex = 0
                ),
                QuizQuestion(
                    id = "q_gen_2",
                    questionFr = "Combien de vérifications faut-il pour certifier un badge ?",
                    questionSg = "Azo oke a lingbi ti fa nzoni ti badge ?",
                    optionsFr = listOf("1 personne", "3 pairs ou clients", "20 personnes"),
                    optionsSg = listOf("Zo oko", "Azo ota", "Azo 20"),
                    correctIndex = 1
                ),
                QuizQuestion(
                    id = "q_gen_3",
                    questionFr = "Comment protéger les stocks (arachide, manioc, matériel) ?",
                    questionSg = "Tongana nye ti bata akobe wala aye ti kua ?",
                    optionsFr = listOf("À l’abri de l’humidité, dans un contenant propre", "À même le sol dehors", "Sous la pluie"),
                    optionsSg = listOf("Na ndo so a huru na a yeke propre", "Na sese na gigi", "Na gbe ti ngu"),
                    correctIndex = 0
                )
            )
        )
    }
}
