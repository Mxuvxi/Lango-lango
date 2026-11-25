package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.LangoSeedData
import com.example.data.local.entity.CompetenceEntity
import com.example.data.local.entity.LangueCode
import com.example.data.local.entity.ProfilEntity
import com.example.data.local.entity.RoleCode
import com.example.data.local.entity.UtilisateurEntity
import com.example.ui.components.LangoAvatar

@Composable
fun EditProfileScreen(
    langue: String,
    user: UtilisateurEntity?,
    profile: ProfilEntity?,
    competences: List<CompetenceEntity>,
    onBack: () -> Unit,
    onSave: (
        prenom: String,
        telephone: String,
        role: String,
        ville: String,
        quartier: String,
        metierPrincipal: String,
        niveauInitial: String,
        objectif: String,
        bioCourte: String,
        disponible: Boolean,
        pickedPhotoUri: Uri?,
        removePhoto: Boolean
    ) -> Unit
) {
    val isSango = langue == LangueCode.SG

    var prenom by remember(user?.prenom) { mutableStateOf(user?.prenom.orEmpty()) }
    var telephone by remember(user?.telephone) { mutableStateOf(user?.telephone ?: "+236 ") }
    var role by remember(user?.role) { mutableStateOf(user?.role ?: RoleCode.JEUNE) }
    var ville by remember(user?.ville) { mutableStateOf(user?.ville ?: LangoSeedData.rcaVilles.first()) }
    var quartier by remember(user?.quartier) { mutableStateOf(user?.quartier.orEmpty()) }

    var metier by remember(profile?.metierPrincipal) {
        mutableStateOf(profile?.metierPrincipal ?: competences.firstOrNull()?.id.orEmpty())
    }
    var niveau by remember(profile?.niveauInitial) {
        mutableStateOf(profile?.niveauInitial ?: "DEBUTANT")
    }
    var objectif by remember(profile?.objectif) {
        mutableStateOf(profile?.objectif ?: "EMPLOI")
    }
    var bio by remember(profile?.bioCourte) { mutableStateOf(profile?.bioCourte.orEmpty()) }
    var disponible by remember(profile?.disponible) { mutableStateOf(profile?.disponible ?: true) }

    var pickedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var removePhoto by remember { mutableStateOf(false) }

    val existingPhotoPath = if (removePhoto) "" else profile?.photoProfilUri.orEmpty()
    val hasAnyPhoto = pickedPhotoUri != null || existingPhotoPath.isNotBlank()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            pickedPhotoUri = uri
            removePhoto = false
        }
    }

    val canSave = prenom.trim().length >= 2 && telephone.filter { it.isDigit() }.length >= 8

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .align(Alignment.TopCenter)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("edit_profile_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Retour"
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isSango) "Lekengo profil ti mbi" else "Modifier mon profil",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            // Photo de profil card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LangoAvatar(
                        photoPath = existingPhotoPath,
                        previewUri = pickedPhotoUri,
                        displayName = prenom.ifBlank { "L" },
                        size = 92.dp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (isSango) "Foto ti profil ti mo" else "Photo de profil",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isSango) {
                            "A bata kete (< 40 Ko) ti gue sân réseau."
                        } else {
                            "Compressée (< 40 Ko) pour les réseaux 2G/3G (+3 pts fiabilité)."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("edit_profile_pick_photo_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AddAPhoto,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (hasAnyPhoto) {
                                    if (isSango) "Changer foto" else "Changer la photo"
                                } else {
                                    if (isSango) "Zia foto" else "Ajouter une photo"
                                }
                            )
                        }
                        if (hasAnyPhoto) {
                            OutlinedButton(
                                onClick = {
                                    pickedPhotoUri = null
                                    removePhoto = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("edit_profile_remove_photo_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = "Retirer la photo",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isSango) "Lungula" else "Retirer")
                            }
                        }
                    }
                }
            }

            // Identité & Contact
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (isSango) "Iri na numéro" else "Identité & contact",
                        style = MaterialTheme.typography.titleMedium
                    )
                    OutlinedTextField(
                        value = prenom,
                        onValueChange = { prenom = it },
                        label = { Text(if (isSango) "Iri ti mo" else "Prénom / Nom d’atelier") },
                        leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_profile_name_input")
                    )
                    OutlinedTextField(
                        value = telephone,
                        onValueChange = { telephone = it },
                        label = { Text(if (isSango) "Numéro (+236)" else "Téléphone (+236)") },
                        leadingIcon = { Icon(Icons.Rounded.Phone, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_profile_phone_input")
                    )
                    Text(
                        text = if (isSango) "Mo yeke zo wa ?" else "Statut sur Lango :",
                        style = MaterialTheme.typography.labelLarge
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val roles = listOf(
                            RoleCode.JEUNE to (if (isSango) "Maseka" else "Jeune"),
                            RoleCode.ARTISAN to (if (isSango) "Artisan" else "Artisan(e)"),
                            RoleCode.EMPLOYEUR to (if (isSango) "Patron / ONG" else "Employeur / ONG")
                        )
                        items(roles) { (code, label) ->
                            FilterChip(
                                selected = role == code,
                                onClick = { role = code },
                                label = { Text(label) },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            )
                        }
                    }
                }
            }

            // Localisation & Métier
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (isSango) "Kodoro na kua" else "Localisation & métier",
                        style = MaterialTheme.typography.titleMedium
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(LangoSeedData.rcaVilles) { cityOption ->
                            FilterChip(
                                selected = ville == cityOption,
                                onClick = { ville = cityOption },
                                label = { Text(cityOption) },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            )
                        }
                    }
                    OutlinedTextField(
                        value = quartier,
                        onValueChange = { quartier = it },
                        label = { Text(if (isSango) "Quartier" else "Quartier (ex : PK5, Boy-Rabe)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_profile_quartier_input")
                    )
                    Text(
                        text = if (isSango) "Kozo kua ti mo :" else "Métier principal :",
                        style = MaterialTheme.typography.labelLarge
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(competences, key = { it.id }) { comp ->
                            FilterChip(
                                selected = metier == comp.id,
                                onClick = { metier = comp.id },
                                label = { Text(if (isSango) comp.nomSg else comp.nomFr) },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            )
                        }
                    }
                }
            }

            // Bio, Niveau, Objectif & Disponibilité
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (isSango) "Présentation & disponibilité" else "Présentation & parcours",
                        style = MaterialTheme.typography.titleMedium
                    )
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { if (it.length <= 140) bio = it },
                        label = {
                            Text(
                                if (isSango) {
                                    "Kete tene na ndo ti kua ti mo"
                                } else {
                                    "Courte présentation (expérience, spécialité)"
                                }
                            )
                        },
                        supportingText = { Text("${bio.length}/140") },
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_profile_bio_input")
                    )

                    Text(
                        text = if (isSango) "Hinga ye ti mo :" else "Niveau :",
                        style = MaterialTheme.typography.labelLarge
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val levels = listOf(
                            "DEBUTANT" to (if (isSango) "Finzo" else "Débutant"),
                            "PRATIQUE" to (if (isSango) "Kete" else "Déjà pratiqué"),
                            "EXPERIMENTE" to (if (isSango) "Mingi" else "Expérimenté")
                        )
                        items(levels) { (code, label) ->
                            FilterChip(
                                selected = niveau == code,
                                onClick = { niveau = code },
                                label = { Text(label) },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            )
                        }
                    }

                    Text(
                        text = if (isSango) "Ye so mo ye :" else "Objectif :",
                        style = MaterialTheme.typography.labelLarge
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val goals = listOf(
                            "APPRENDRE" to (if (isSango) "Manda" else "Apprendre"),
                            "EMPLOI" to (if (isSango) "Wara kua" else "Emploi / Stage"),
                            "CLIENTS" to (if (isSango) "Aclient" else "Plus de clients")
                        )
                        items(goals) { (code, label) ->
                            FilterChip(
                                selected = objectif == code,
                                onClick = { objectif = code },
                                label = { Text(label) },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isSango) "Disponible ti sara kua laso" else "Disponible pour missions",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Switch(
                            checked = disponible,
                            onCheckedChange = { disponible = it },
                            modifier = Modifier.testTag("edit_profile_available_switch")
                        )
                    }
                }
            }

            Button(
                onClick = {
                    onSave(
                        prenom,
                        telephone,
                        role,
                        ville,
                        quartier,
                        metier,
                        niveau,
                        objectif,
                        bio,
                        disponible,
                        pickedPhotoUri,
                        removePhoto
                    )
                },
                enabled = canSave,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("edit_profile_save_button")
            ) {
                Icon(Icons.Rounded.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSango) "Bata profil ti mbi" else "Enregistrer mon profil",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
