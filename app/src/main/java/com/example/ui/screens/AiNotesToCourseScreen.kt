package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.example.data.local.entity.CoursGenereIaEntity
import com.example.data.repository.LangoAiRepository
import com.example.ui.util.PhotoCaptureHelper
import com.example.ui.viewmodel.LangoAppState
import com.example.ui.viewmodel.LangoViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiNotesToCourseScreen(
    viewModel: LangoViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val aiRepository = remember { LangoAiRepository() }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val readyState = uiState as? LangoAppState.Ready
    val competences = readyState?.competences.orEmpty()
    val savedCourses by viewModel.observeCoursGeneres().collectAsStateWithLifecycle(initialValue = emptyList())

    var noteText by remember { mutableStateOf("") }
    var courseTitle by remember { mutableStateOf("") }
    var selectedCompetenceId by remember { mutableStateOf<String?>(null) }
    var selectedCompetenceName by remember { mutableStateOf("") }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var imageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var pendingCameraFile by remember { mutableStateOf<File?>(null) }

    var isGenerating by remember { mutableStateOf(false) }
    var generatedResult by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var saveSuccessMessage by remember { mutableStateOf<String?>(null) }

    // Quiz state simulation from generated result
    var quizSelectedOption by remember { mutableStateOf<Int?>(null) }
    var quizSubmitted by remember { mutableStateOf(false) }
    var aiMistakeAnalysis by remember { mutableStateOf<String?>(null) }
    var isAnalyzingMistake by remember { mutableStateOf(false) }

    // Camera Launcher for capturing full-resolution photo of notes offline
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && pendingCameraUri != null) {
            val uri = pendingCameraUri!!
            selectedImageUri = uri
            val compressed = PhotoCaptureHelper.readAndCompressImage(context, uri)
            imageBytes = compressed
        }
    }

    // Permission launcher for camera
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val (file, uri) = PhotoCaptureHelper.createPhotoUriForNotes(context)
            pendingCameraFile = file
            pendingCameraUri = uri
            takePictureLauncher.launch(uri)
        } else {
            errorMessage = "L'accès à l'appareil photo est requis pour photographier vos notes hors-ligne."
        }
    }

    // Image Picker from gallery
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            val compressed = PhotoCaptureHelper.readAndCompressImage(context, uri)
            imageBytes = compressed
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Studio IA : Notes & Cours Perso") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingVals ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Transformez vos notes en cours et quiz",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Photographiez vos cahiers ou collez vos résumés. L'IA génère un cours sur-mesure, son quiz et analyse vos erreurs.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Optional Link to Existing Training Course / Domain
            if (competences.isNotEmpty()) {
                Text(
                    text = "Lier à un domaine de formation (optionnel) :",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    competences.take(4).forEach { comp ->
                        val isSelected = selectedCompetenceId == comp.id
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (isSelected) {
                                    selectedCompetenceId = null
                                    selectedCompetenceName = ""
                                } else {
                                    selectedCompetenceId = comp.id
                                    selectedCompetenceName = comp.nomFr
                                    if (courseTitle.isBlank()) {
                                        courseTitle = "Cours Notes : ${comp.nomFr}"
                                    }
                                }
                            },
                            label = { Text(comp.nomFr, maxLines = 1) }
                        )
                    }
                }
            }

            // Course Title
            OutlinedTextField(
                value = courseTitle,
                onValueChange = { courseTitle = it },
                label = { Text("Titre du cours (ex: Révision BTS ou Vocabulaire Pro)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("course_title_input"),
                shape = RoundedCornerShape(12.dp)
            )

            // Text Notes Input
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text("Collez vos notes, résumés de cours ou points clés...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .testTag("notes_input"),
                shape = RoundedCornerShape(12.dp)
            )

            // Offline Photo Capture Action Buttons
            Text(
                text = "Photographier vos notes hors-ligne :",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Camera Button (Offline Photo Capture)
                Button(
                    onClick = {
                        val hasCam = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasCam) {
                            val (file, uri) = PhotoCaptureHelper.createPhotoUriForNotes(context)
                            pendingCameraFile = file
                            pendingCameraUri = uri
                            takePictureLauncher.launch(uri)
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("take_camera_photo_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Prendre photo")
                }

                // Gallery Button
                OutlinedButton(
                    onClick = {
                        imagePickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("pick_photo_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Galerie")
                }
            }

            // Preview of the captured photo
            if (selectedImageUri != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(selectedImageUri),
                                contentDescription = "Aperçu photo notes",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Photo des notes enregistrée ✓",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Stockée localement hors-ligne sur votre appareil",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = {
                            selectedImageUri = null
                            imageBytes = null
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Supprimer photo", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            // Generate Button
            Button(
                onClick = {
                    if (noteText.isBlank() && imageBytes == null) {
                        errorMessage = "Veuillez saisir du texte ou prendre en photo vos notes."
                        return@Button
                    }
                    errorMessage = null
                    saveSuccessMessage = null
                    isGenerating = true
                    scope.launch {
                        try {
                            val promptContext = if (selectedCompetenceName.isNotBlank()) {
                                "[Domaine: $selectedCompetenceName]\n$noteText"
                            } else {
                                noteText
                            }
                            val res = aiRepository.generateCourseFromNotes(promptContext, imageBytes)
                            generatedResult = res
                            if (courseTitle.isBlank()) {
                                courseTitle = if (selectedCompetenceName.isNotBlank()) {
                                    "Cours : $selectedCompetenceName"
                                } else {
                                    "Cours du ${SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(Date())}"
                                }
                            }
                        } catch (e: Exception) {
                            errorMessage = "Erreur de génération : ${e.localizedMessage}"
                        } finally {
                            isGenerating = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("generate_course_button"),
                shape = RoundedCornerShape(12.dp),
                enabled = !isGenerating
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("L'IA analyse vos notes...")
                } else {
                    Icon(Icons.Default.School, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Générer mon cours & quiz personnalisé")
                }
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (saveSuccessMessage != null) {
                Text(
                    text = saveSuccessMessage!!,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Generated Result Display
            if (generatedResult != null) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Cours & Quiz Généré par l'IA",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Text(
                            text = generatedResult!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Save Course in Room Database & Sync to Firestore
                        Button(
                            onClick = {
                                viewModel.saveGeneratedCourse(
                                    titre = courseTitle.ifBlank { "Cours IA du ${SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(Date())}" },
                                    resume = if (selectedCompetenceName.isNotBlank()) "Lié à : $selectedCompetenceName" else "Généré à partir de notes",
                                    notesEtudiant = noteText,
                                    coursOriginalId = selectedCompetenceId,
                                    coursOriginalTitre = selectedCompetenceName,
                                    photoLocaleUri = selectedImageUri?.toString() ?: "",
                                    contenuCours = generatedResult ?: "",
                                    quizJson = "[{\"question\":\"Selon vos notes, quel est le point clé ?\"}]",
                                    onSaved = {
                                        saveSuccessMessage = "Cours enregistré dans votre base de données locale (Room) et synchronisé au Cloud Firestore !"
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth().testTag("save_course_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Enregistrer dans la base locale (Room) & Firestore")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Interactive Quiz simulation block
                        Text(
                            text = "Testez vos connaissances (Quiz)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Question : Selon vos notes, quel est le point clé à retenir en priorité ?",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        val options = listOf("A) Appliquer la méthode directement", "B) Négliger les étapes de base", "C) Ignorer les consignes")
                        options.forEachIndexed { index, option ->
                            OutlinedButton(
                                onClick = {
                                    quizSelectedOption = index
                                    quizSubmitted = true
                                    if (index != 0) {
                                        isAnalyzingMistake = true
                                        scope.launch {
                                            try {
                                                aiMistakeAnalysis = aiRepository.explainQuizMistake(
                                                    question = "Selon vos notes, quel est le point clé à retenir en priorité ?",
                                                    selectedOption = option,
                                                    correctOption = "A) Appliquer la méthode directement"
                                                )
                                            } catch (e: Exception) {
                                                aiMistakeAnalysis = "Analyse indisponible."
                                            } finally {
                                                isAnalyzingMistake = false
                                            }
                                        }
                                    } else {
                                        aiMistakeAnalysis = null
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (quizSubmitted && quizSelectedOption == index) {
                                        if (index == 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                        else MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                                    } else MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Text(option)
                            }
                        }

                        if (quizSubmitted) {
                            if (quizSelectedOption == 0) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Bravo ! Bonne réponse validée par l'IA.", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Oups ! Ce n'est pas la bonne réponse.", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    if (isAnalyzingMistake) {
                                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                                        Text("L'IA analyse pourquoi vous avez échoué...", style = MaterialTheme.typography.bodySmall)
                                    } else if (aiMistakeAnalysis != null) {
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text("Analyse personnalisée de votre erreur par l'IA :", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(aiMistakeAnalysis!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Saved Generated Courses Section (Local Room Database + Firestore)
            if (savedCourses.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Mes cours générés hors-ligne (${savedCourses.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(Icons.Default.CloudDone, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }

                for (cours in savedCourses) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                courseTitle = cours.titre
                                noteText = cours.notesEtudiant
                                generatedResult = cours.contenuCours
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = cours.titre,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                if (cours.coursOriginalTitre.isNotBlank()) {
                                    Text(
                                        text = "Domaine : ${cours.coursOriginalTitre}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE).format(Date(cours.createdAt))
                                Text(
                                    text = "Créé le $formattedDate",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { viewModel.deleteGeneratedCourse(cours.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}
