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
    val imageUrl: String? = null,
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
        ),
        "pack_photo_1" to PackLessonContent(
            contenuId = "pack_photo_1",
            badgeTitreFr = "Photographie & Reportage Pro",
            badgeTitreSg = "Photo na video ti Afrika",
            steps = listOf(
                LessonStep(
                    numero = 1,
                    titreFr = "Gestion de la lumière naturelle (Golden Hour)",
                    titreSg = "Mû ngangu ti wâ ti lâ nzoni",
                    texteFr = "En Afrique centrale, évite le soleil zénithal direct de midi qui crée des ombres dures. Privilégie les matins (7h-9h) et fins d'après-midi (16h-18h).",
                    texteSg = "Na l'Afrique centrale, zia l'heure ti midi, gi tongo nda ti la wala huongo la.",
                    conseilPratiqueFr = "Place ton sujet dos au soleil pour un effet lumineux magnifique (contre-jour artistique).",
                    conseilPratiqueSg = "Zia zo na gbe ti lâ ti mû nzoni foto."
                ),
                LessonStep(
                    numero = 2,
                    titreFr = "Cadrage, règle des tiers et stabilité",
                    titreSg = "Zia moto na ndo so alingbi",
                    texteFr = "Active la grille sur ton smartphone ou appareil photo. Place les points d'intérêt sur les intersections pour attirer le regard du spectateur.",
                    texteSg = "Zia grille na yâ téléfone ti mo si foto ti mo a yeke na ndo ni.",
                    conseilPratiqueFr = "Garde les coudes serrés contre ton corps pour éviter les photos floues.",
                    conseilPratiqueSg = "Kanga tele ti mo nzoni si foto a tï flou pepe."
                ),
                LessonStep(
                    numero = 3,
                    titreFr = "Montage mobile et livraison client",
                    titreSg = "Leke foto na video na téléfone",
                    texteFr = "Trie tes meilleurs clichés, ajuste l'exposition et le contraste avec des applications légères, puis livre rapidement via WhatsApp ou clé USB.",
                    texteSg = "Horo nzoni foto na video, na tokua hio na client.",
                    conseilPratiqueFr = "Un reportage propre de mariage ou d'événement apporte de nouveaux clients par le bouche-à-oreille.",
                    conseilPratiqueSg = "Nzoni kua a mû tura na mo."
                )
            ),
            quiz = listOf(
                QuizQuestion(
                    id = "q_ph_1",
                    questionFr = "Quels sont les meilleurs moments de la journée pour photographier en extérieur ?",
                    questionSg = "Ngoi wa la a yeke nzoni ti mû foto na gigi ?",
                    optionsFr = listOf("Le matin (7h-9h) et le soir (16h-18h)", "À midi sous le soleil brûlant", "En pleine nuit sans flash"),
                    optionsSg = listOf("Tongo nda ti la na huongo la", "Na midi", "Na bï"),
                    correctIndex = 0
                ),
                QuizQuestion(
                    id = "q_ph_2",
                    questionFr = "À quoi sert la règle des tiers en cadrage photo ?",
                    questionSg = "Ngbanga ti nye a zia grille na foto ?",
                    optionsFr = listOf("À centrer l'attention sur les points forts de l'image", "À vider la batterie", "À colorer le fond en vert"),
                    optionsSg = listOf("Ti mû ngangu na foto ni", "Ti buba batterie", "Ti gbian couleur"),
                    correctIndex = 0
                )
            )
        ),
        "pack_cinema_1" to PackLessonContent(
            contenuId = "pack_cinema_1",
            badgeTitreFr = "Réalisation & Vidéo Mobile",
            badgeTitreSg = "Sarango video na smartphone",
            steps = listOf(
                LessonStep(
                    numero = 1,
                    titreFr = "Le scénario et les plans de coupe (B-Roll)",
                    titreSg = "Sû mbeti ti video na kete plan",
                    texteFr = "Prépare toujours une courte histoire (introduction, problème, solution). Filme des plans larges, des plans serrés sur les mains et des détails.",
                    texteSg = "Leke ye so mo ye ti filmé kozo, mû kota plan na kete plan.",
                    conseilPratiqueFr = "Le son est plus important que l'image : filme dans un endroit calme ou utilise un micro-cravate.",
                    conseilPratiqueSg = "Bongo ti ita na sango a yeke kota ye."
                ),
                LessonStep(
                    numero = 2,
                    titreFr = "Mouvements de caméra fluides",
                    titreSg = "Tambolengo ti camera nzoni",
                    texteFr = "Évite les mouvements brusques. Fais des panoramiques lents ou marche en stabilisant ton téléphone avec tes deux mains.",
                    texteSg = "Sara gerakan na ngangu pepe, tambola polele.",
                    conseilPratiqueFr = "Utilise un petit trépied ou appuie tes bras contre un mur.",
                    conseilPratiqueSg = "Zia camera na ndo ti kete gbakuru."
                ),
                LessonStep(
                    numero = 3,
                    titreFr = "Diffusion et monétisation locale",
                    titreSg = "Kä video na tura na azo",
                    texteFr = "Crée des mini-documentaires sur les talents locaux, les marchés et l'artisanat, puis publie sur les réseaux sociaux et plateformes africaines.",
                    texteSg = "Filmé kua ti azo ti kodro ti mo, na tisa azo.",
                    conseilPratiqueFr = "Le contenu authentique africain captive un public immense en ligne.",
                    conseilPratiqueSg = "Azo mingi a ye ti bâ kua ti Afrika."
                )
            ),
            quiz = listOf(
                QuizQuestion(
                    id = "q_cin_1",
                    questionFr = "Quel élément est souvent plus critique que l'image en vidéo mobile ?",
                    questionSg = "Kotakarao wa la a yeke kota na yâ video ?",
                    optionsFr = listOf("Le son et la clarté des voix", "La couleur du téléphone", "Le poids de la coque"),
                    optionsSg = listOf("Bongo ti ita", "Couleur ti téléfone", "Poids ni"),
                    correctIndex = 0
                ),
                QuizQuestion(
                    id = "q_cin_2",
                    questionFr = "Qu'appelle-t-on un plan de coupe (B-Roll) ?",
                    questionSg = "Nye la a yeke kete plan (B-Roll) ?",
                    optionsFr = listOf("Des images secondaires d'illustration pour enrichir le montage", "Une coupure de courant", "Un montage raté"),
                    optionsSg = listOf("Kete foto ti mû ngangu na video", "Kama ti wango", "Sioni video"),
                    correctIndex = 0
                )
            )
        ),
        "pack_ia_1" to PackLessonContent(
            contenuId = "pack_ia_1",
            badgeTitreFr = "IA & Assistants Mobiles",
            badgeTitreSg = "IA na Téléfone",
            steps = listOf(
                LessonStep(
                    numero = 1,
                    titreFr = "Poser les bonnes questions (Prompt)",
                    titreSg = "Hunda tënë nzoni na IA",
                    texteFr = "Pour obtenir un bon devis ou des conseils business d'une IA, donne un rôle précis, le contexte local (ex: Bangui, RCA) et le format souhaité.",
                    texteSg = "Tongana mo ye wango ti biisi na IA, fa ndo na ye so mo ye polele.",
                    conseilPratiqueFr = "Sois clair et simple dans tes instructions.",
                    conseilPratiqueSg = "Tënë ti mo alingbi ti duti biani kete na hio."
                ),
                LessonStep(
                    numero = 2,
                    titreFr = "Vérifier et adapter au terrain",
                    titreSg = "Bâ wala tënë alingbi na kodro",
                    texteFr = "Les réponses de l'IA sont générales : vérifie toujours les prix en FCFA, les lois locales et la faisabilité avant de les appliquer.",
                    texteSg = "Bâ nginza na FCFA na ndo ti kodro kozo si mo tï na yâ ni.",
                    conseilPratiqueFr = "L'IA est un assistant, c'est toi l'entrepreneur.",
                    conseilPratiqueSg = "IA a mû wango, mo mveni la mo leke ye."
                )
            ),
            quiz = listOf(
                QuizQuestion(
                    id = "q_ia_1",
                    questionFr = "Que faut-il donner à l'IA pour obtenir un bon résultat professionnel ?",
                    questionSg = "Nye la a lingbi ti fa na IA?",
                    optionsFr = listOf("Un contexte précis et le rôle attendu", "Des insultes", "Rien du tout"),
                    optionsSg = listOf("Tënë na ndo ti kua", "Sioni tënë", "Ye oko pepe"),
                    correctIndex = 0
                )
            )
        ),
        "pack_langues_1" to PackLessonContent(
            contenuId = "pack_langues_1",
            badgeTitreFr = "Français Pro & Anglais Commercial",
            badgeTitreSg = "Français na Anglais ti Kua",
            steps = listOf(
                LessonStep(
                    numero = 1,
                    titreFr = "Accueil et négociation courtoise",
                    titreSg = "Tisa client na nzoni tënë",
                    texteFr = "Utilisez un vocabulaire poli, clair et professionnel pour présenter vos services, fixer les tarifs et rassurer le client.",
                    texteSg = "Fa tënë na nzoni zo ti tene client a yeda na nginza ti kua.",
                    conseilPratiqueFr = "Un sourire et un ton poli valent plus qu'une longue discussion.",
                    conseilPratiqueSg = "Nzoni lê a mû ngangu na tî."
                ),
                LessonStep(
                    numero = 2,
                    titreFr = "Termes commerciaux de base (EN/FR)",
                    titreSg = "Tënë ti commerce (Anglais/Français)",
                    texteFr = "Apprenez les mots clés comme Invoice (facture), Discount (remise), Delivery (livraison) pour élargir votre clientèle aux partenaires internationaux.",
                    texteSg = "Hinga tënë ti Invoice na Discount ti tene zo a hinga mo na kodro nde.",
                    conseilPratiqueFr = "Notez les commandes importantes par écrit.",
                    conseilPratiqueSg = "Sû kua ti client na mbeti."
                )
            ),
            quiz = listOf(
                QuizQuestion(
                    id = "q_lang_1",
                    questionFr = "Que signifie 'Invoice' en anglais professionnel ?",
                    questionSg = "Nye la 'Invoice' a yeke?",
                    optionsFr = listOf("Une facture", "Un chat", "Une chaussure"),
                    optionsSg = listOf("Facture", "Kpani", "Bongo ti gbe"),
                    correctIndex = 0
                )
            )
        ),
        "pack_batiment_1" to PackLessonContent(
            contenuId = "pack_batiment_1",
            badgeTitreFr = "Briques de Terre Stabilisée & Maçonnerie",
            badgeTitreSg = "Lekengo da na sese",
            steps = listOf(
                LessonStep(
                    numero = 1,
                    titreFr = "Dosage argile, sable et ciment (BTS)",
                    titreSg = "Lekengo brik ti sese",
                    texteFr = "Mélanger de la terre argileuse tamisée avec du sable et 5% de ciment, puis compresser solidement pour obtenir des briques résistantes.",
                    texteSg = "Kanga sese na kete ciment si brik a ninga mingi.",
                    conseilPratiqueFr = "Sécher à l'ombre sous une bâches pendant 7 jours.",
                    conseilPratiqueSg = "Huru na gbe ti tïtî yenga oko."
                ),
                LessonStep(
                    numero = 2,
                    titreFr = "Imperméabilisation des fondations",
                    titreSg = "Bata gbe ti da na ngu",
                    texteFr = "Surélever les fondations d'au moins 30 cm avec des pierres et du mortier étanche pour protéger la construction contre l'humidité de la saison des pluies.",
                    texteSg = "Zia gbe ti da na nduzu ti tene ngu ti tï a lï da pepe.",
                    conseilPratiqueFr = "La fondation est la longévité de votre ouvrage.",
                    conseilPratiqueSg = "Nzoni gbe a mû ngangu na da."
                )
            ),
            quiz = listOf(
                QuizQuestion(
                    id = "q_bat_1",
                    questionFr = "Pourquoi surélever les fondations de 30 cm minimum ?",
                    questionSg = "Ngbanga ti nye a zia gbe ti da na nduzu?",
                    optionsFr = listOf("Pour protéger contre l'humidité et l'eau de pluie", "Pour faire joli", "Pour économiser du ciment"),
                    optionsSg = listOf("Ti bata da na ngu ti tï", "Ti zia nzoni", "Ti bata ciment"),
                    correctIndex = 0
                )
            )
        ),
        "pack_sante_1" to PackLessonContent(
            contenuId = "pack_sante_1",
            badgeTitreFr = "Premiers Secours & Eau Sûre",
            badgeTitreSg = "Batango tere na Ngu Nzoni",
            steps = listOf(
                LessonStep(
                    numero = 1,
                    titreFr = "Désinfection et potabilisation de l'eau",
                    titreSg = "Sukulungo ngu na kanga sioni",
                    texteFr = "Faire bouillir l'eau 5 minutes ou utiliser des pastilles épuratrices pour éliminer les microbes avant la consommation familiale.",
                    texteSg = "Huru ngu ngu oku wala zia kete ye ti sukula ngu.",
                    conseilPratiqueFr = "L'eau propre évite la typhoïde et le choléra.",
                    conseilPratiqueSg = "Nzoni ngu a bata zo na sioni kobela."
                ),
                LessonStep(
                    numero = 2,
                    titreFr = "Gestes d'urgence de base",
                    titreSg = "Mû maboko na zo so agomer",
                    texteFr = "En cas de malaise ou de petite blessure, nettoyer à l'eau propre et au savon, désinfecter et bander proprement sans serrer excessivement.",
                    texteSg = "Sukula tele na nzoni ngu na savon na yôro mbeti ti kanga.",
                    conseilPratiqueFr = "Alerter rapidement le centre de santé le plus proche.",
                    conseilPratiqueSg = "Tokua zo na hôpital hio tongana kpale a yeke kota."
                )
            ),
            quiz = listOf(
                QuizQuestion(
                    id = "q_sant_1",
                    questionFr = "Combien de temps faut-il faire bouillir l'eau douteuse pour la rendre sûre ?",
                    questionSg = "Ngoi oke a lingbi ti huru ngu?",
                    optionsFr = listOf("5 minutes", "5 secondes", "2 heures"),
                    optionsSg = listOf("Minute oku", "Second oku", "Hour ota"),
                    correctIndex = 0
                )
            )
        ),
        "pack_mecanique_1" to PackLessonContent(
            contenuId = "pack_mecanique_1",
            badgeTitreFr = "Entretien Moto & Dépannage Express",
            badgeTitreSg = "Lekengo Moto na vidange",
            steps = listOf(
                LessonStep(
                    numero = 1,
                    titreFr = "Vidange et changement d'huile",
                    titreSg = "Gbian mafuta ti moto",
                    texteFr = "Vidanger l'huile moteur tous les 2000 km, moteur chaud, et nettoyer le tamis métallique pour prolonger la durée de vie du moteur.",
                    texteSg = "Gbian mafuta ti moto na fä ti 2000 km.",
                    conseilPratiqueFr = "Ne jamais rouler avec un niveau d'huile insuffisant.",
                    conseilPratiqueSg = "Mû ngangu na moto na nzoni mafuta."
                ),
                LessonStep(
                    numero = 2,
                    titreFr = "Tension et graissage de la chaîne",
                    titreSg = "Kanga chaîne na mafuta",
                    texteFr = "Vérifier le jeu de la chaîne (environ 2 cm de battement), graisser régulièrement pour éviter l'usure prématurée des pignons.",
                    texteSg = "Bâ chaîne ti moto na zia mafuta da.",
                    conseilPratiqueFr = "Une chaîne bien tendue évite les casses sur piste difficile.",
                    conseilPratiqueSg = "Nzoni chaîne a bata moto na sese."
                )
            ),
            quiz = listOf(
                QuizQuestion(
                    id = "q_mec_1",
                    questionFr = "À quelle fréquence recommandée faut-il faire la vidange d'une moto ?",
                    questionSg = "Kilometre oke a lingbi ti gbian mafuta ti moto?",
                    optionsFr = listOf("Tous les 2000 km environ", "Tous les 10 ans", "Jamais"),
                    optionsSg = listOf("Na kilometre 2000", "Na ngu 10", "Ye oko pepe"),
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
