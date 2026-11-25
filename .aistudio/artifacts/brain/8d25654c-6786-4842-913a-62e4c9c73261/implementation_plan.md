# Plan d'évolution — Lango v2 (Artisans, Jeunes & Employeurs)

Ce plan fait passer **Lango** d'un passeport de compétences individuel à un écosystème complet de terrain adapté à la RCA : suivi de caisse et devis pour les artisans, gestion de recrutement pour les employeurs/ONG, passeport QR vérifiable sans Internet et coach métiers assisté par IA avec lecture vocale.

---

## Revue utilisateur & Décisions clés

> [!IMPORTANT]
> Les choix confirmés lors de l'entretien guident directement cette architecture. L'ajout de l'assistant IA nécessite d'activer **Firebase AI Logic** (uniquement pour les appels Gemini et App Check), tout en conservant **Room** et les **SMS/USSD** comme socle 100 % hors-ligne sans imposer de connexion Google.

- **Décision confirmée 1 — Périmètre des 4 fonctionnalités** : Espace Employeur/ONG, Cahier de caisse & devis artisans, QR Code Passeport hors-ligne et Coach Métiers IA vocal.
- **Décision confirmée 2 — Priorité hors-ligne (Room + SMS)** : Toutes les données métiers (caisse, devis, candidatures employeur, QR Passeport, historique des conseils) restent stockées localement dans Room avec file de synchronisation différée et repli SMS/USSD.
- **Décision confirmée 3 — Équilibre des publics** : L'interface reste centrée sur 3 parcours simples sans alourdir la navigation principale : *Apprendre* (avec Coach IA), *Prouver* (avec QR Passeport & Atelier/Caisse) et *Connecter* (avec Espace Recruteur).
- **Activation requise à l'exécution** : Lors de l'étape d'implémentation du Coach IA, une fenêtre de validation Firebase apparaîtra pour activer **Firebase AI Logic (`ai_logic`)** et **App Check** (aucune clé API ni base cloud Firestore requise).

---

## 1. Vue d'ensemble & Proposition de valeur

- **Ce que cela apporte** :
  1. **Espace Recruteur (Employeurs, PME & ONG)** : Gestion directe des candidatures reçues sur leurs offres (statuts *Reçue → Vue → Retenue / Refusée*), tri par score de fiabilité et réponse en 1 clic par SMS ou appel.
  2. **Atelier, Caisse & Devis (Artisans)** : Suivi simple des recettes et dépenses en FCFA, génération de devis/reçus clairs à partager par SMS/WhatsApp et encaissement rapide Orange Money (`*144#`) ou Telecel Money (`*145#`).
  3. **QR Code Passeport hors-ligne** : Carte d'identité professionnelle affichant un vrai QR Code scannable généré localement sans réseau, accompagné d'un écran de vérification rapide d'un code `LG-...` côté employeur.
  4. **Coach Métiers IA + Voix** : Assistant conversationnel spécialisé en métiers pratiques (couture, solaire, réparation, agroalimentaire, gestion) répondant en français ou avec synthèse sango simplifiée, lu à voix haute et consultable hors-ligne une fois enregistré.
- **Public cible** : Jeunes apprentis, artisans indépendants et employeurs/ONG opérant en zones à faible connectivité (2G/3G ou hors-ligne).

---

## 2. Expérience utilisateur & Design visuel

### Parcours clés

1. **Parcours Artisan — Caisse, Devis & Encaissement** :
   - Depuis l'onglet **Prouver**, un raccourci **« Ma Caisse & Devis »** ouvre le tableau de bord financier local : solde net du mois en FCFA, entrées/sorties rapides en 2 touches, et onglet *Devis & Reçus* avec partage texte/SMS ou encaissement USSD direct.
2. **Parcours Artisan/Jeune — QR Passeport & Coach IA** :
   - Sur la carte **Passeport Lango**, un bouton **« QR Code »** ouvre une fiche plein contraste (fond blanc pur pour lecture fiable par caméra) contenant le code QR vérifiable et la signature `LG-...`.
   - Depuis **Apprendre** (et une leçon), un bouton **« Demander au Coach IA »** ouvre le chat vocal/texte avec suggestions rapides par métier, streaming de la réponse et bouton d'écoute vocale automatique.
3. **Parcours Employeur / ONG — Suivi des candidats** :
   - Dans l'onglet **Connecter**, les offres publiées (ou le sous-onglet **« Recrutement »**) permettent d'ouvrir la liste des candidats classés par score de fiabilité, d'afficher leurs badges/vérifications et de changer leur statut avec notification SMS/in-app pré-remplie.

### Identité visuelle & Ergonomie

- **Direction esthétique** : Continuité exacte de la charte *Savane & Latérite* haute lisibilité pour écrans d'entrée de gamme en extérieur.
- **Jetons de thème** : Surface claire (`background` ivoire, `surface` blanc pur), accent principal émeraude (`primary`), accent chaleureux terre cuite (`secondary`), or (`tertiary`) pour les badges et statuts positifs/négatifs sémantiques en caisse.
- **Typographie & Hiérarchie** : `Outfit` pour les titres, montants en FCFA et scores ; `Plus Jakarta Sans` pour les libellés et formulaires, avec cibles tactiles minimales de `48.dp` et respect strict des marges système (`enableEdgeToEdge`).
- **États vides & Retours** : Animations sobres (`AnimatedVisibility`), indicateurs de streaming IA en temps réel, bandeaux clairs lorsque l'appareil est hors-ligne pour le Coach IA (sans jamais afficher de fausses réponses IA).

---

## 3. Décisions produit & Compromis techniques

- **Décision 1 — Générateur QR pur Kotlin sur Canvas vs librairie externe**
  - *Approche choisie* : Encodeur QR standard (ISO/IEC 18004) écrit en Kotlin pur et rendu vectoriel sur `Canvas` Compose.
  - *Pourquoi* : Zéro dépendance réseau supplémentaire, taille d'APK minimale pour les téléphones d'entrée de gamme, zéro permission caméra/stockage requise et fonctionnement garanti 100 % hors-ligne.
- **Décision 2 — Coach IA hybride : Firebase AI Logic en ligne + Cache Room hors-ligne**
  - *Approche choisie* : Appels Gemini (`gemini-3.8-flash` en streaming) via **Firebase AI Logic** et **App Check** lorsque la connexion est disponible, avec sauvegarde locale des échanges utiles dans Room et lecture vocale `TextToSpeech` hors-ligne.
  - *Pourquoi* : Offre une vraie intelligence métier sans exposer de clé API dans l'application, tout en informant honnêtement l'utilisateur en mode hors-ligne et en lui permettant de relire/écouter ses précédents conseils sans data.
- **Décision 3 — Stockage financier & recrutement 100 % local avec migration Room v2 → v3**
  - *Approche choisie* : Ajout de tables dédiées dans Room avec migration incrémentale non destructive (`MIGRATION_2_3`) et journalisation dans `sync_outbox`.
  - *Pourquoi* : Préserve toutes les données existantes (profil, photo, badges, portfolio) tout en garantissant l'effacement et l'export JSON conformes à la loi centrafricaine n° 24.001.

---

## 4. Architecture technique & Stratégie de données *(Référence technique)*

### Schéma d'architecture

```text
┌─────────────────────────────────────────────────────────────────────────┐
│                    Couche UI Jetpack Compose (Material 3)               │
│  ┌──────────────┐  ┌───────────────┐  ┌──────────────┐  ┌────────────┐  │
│  │  Apprendre   │  │    Prouver    │  │  Connecter   │  │ Coach IA   │  │
│  │ + Coach IA   │  │ + QR Passeport│  │ + Recruteur  │  │ (Stream +  │  │
│  │              │  │ + Caisse/Devis│  │ (Statuts)    │  │  TTS Voix) │  │
│  └──────┬───────┘  └───────┬───────┘  └──────┬───────┘  └─────┬──────┘  │
└─────────┼──────────────────┼─────────────────┼────────────────┼─────────┘
          │                  │                 │                │
          ▼                  ▼                 ▼                ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                   LangoViewModel & États Unifiés (StateFlow)            │
└───────────────┬──────────────────────────────────────────┬──────────────┘
                │                                          │
                ▼                                          ▼
┌────────────────────────────────────┐    ┌───────────────────────────────┐
│      LangoRepository (Room v3)     │    │  LangoAiRepository (Firebase) │
│  • caisse_operations               │    │  • Firebase.ai                │
│  • devis_artisans                  │    │    (gemini-3.8-flash stream)  │
│  • candidatures (statuts étendus)  │    │  • App Check (MainActivity)   │
│  • coach_messages (cache local)    │    │  • Repli explicite hors-ligne │
│  • sync_outbox + Export Loi 24.001 │    └───────────────────────────────┘
└────────────────────────────────────┘
```

### Modèle de données & État

- **Caisse & Devis (`caisse_operations`, `devis_artisans`)** :
  - Opération de caisse : type (`ENTREE` / `SORTIE`), montant en FCFA, catégorie (*Vente / Prestation / Matériel / Transport*), note courte, horodatage et statut de synchro.
  - Devis / Reçu artisan : nom et téléphone du client, intitulé de la prestation, montant FCFA, statut (`BROUILLON`, `ENVOYE`, `PAYE`) et mode de paiement (*Espèces / Orange Money / Telecel Money*).
- **Recrutement Employeur (`candidatures` + vue employeur)** :
  - Ajout d'un suivi complet des candidatures côté recruteur avec transition de statut (`ENVOYEE`, `VUE`, `RETENUE`, `REFUSEE`), note de l'employeur et déclenchement SMS pré-rempli.
- **Historique Coach IA (`coach_messages`)** :
  - Conservation locale des échanges (rôle, texte, métier associé, date) pour relecture/écoute vocale sans Internet et effacement complet via la Loi n° 24.001.

### Correspondance interactive & Flux de sécurité

- **App Check obligatoire** : Initialisation synchrone dans `MainActivity.onCreate()` avant la création du `ViewModel` (`configureAppCheck(this, intent)`) pour éviter toute erreur HTTP 403 lors des appels Gemini.
- **QR Code Passeport** : Encodage déterministe de la fiche synthétique (`LG-ID`, prénom, ville, score, nombre de badges vérifiés) avec module de contrôle de code côté employeur.
- **Calculs & Intégrité** : Fonctions pures dédiées au bilan de caisse (total entrées, sorties, solde net), au formatage des devis SMS et aux transitions de statut employeur, couvertes par tests unitaires JVM.
