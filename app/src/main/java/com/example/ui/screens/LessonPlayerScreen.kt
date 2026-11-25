package com.example.ui.screens

import android.speech.tts.TextToSpeech
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.local.LessonContentCatalog
import com.example.data.local.entity.BadgeEntity
import com.example.data.local.entity.ContenuPedagogiqueEntity
import com.example.data.local.entity.LangueCode
import java.util.Locale

private enum class LessonStage {
    STEPS, QUIZ, BADGE_CELEBRATION
}

@Composable
fun LessonPlayerScreen(
    contenu: ContenuPedagogiqueEntity,
    langue: String,
    initialStepIndex: Int,
    onBack: () -> Unit,
    onSaveProgress: (etape: Int, total: Int, scoreQuiz: Int, completed: Boolean, (BadgeEntity?) -> Unit) -> Unit,
    onGoToVerification: () -> Unit
) {
    val isSango = langue == LangueCode.SG
    val lessonData = remember(contenu.id) { LessonContentCatalog.getForPack(contenu.id) }
    val steps = lessonData.steps
    val quiz = lessonData.quiz

    var stage by remember { mutableStateOf(LessonStage.STEPS) }
    var currentStepIdx by remember {
        mutableIntStateOf(initialStepIndex.coerceIn(0, (steps.size - 1).coerceAtLeast(0)))
    }
    var quizIdx by remember { mutableIntStateOf(0) }
    var selectedAnswerIdx by remember { mutableIntStateOf(-1) }
    var correctCount by remember { mutableIntStateOf(0) }
    var awardedBadge by remember { mutableStateOf<BadgeEntity?>(null) }

    val context = LocalContext.current
    var isTtsReady by remember { mutableStateOf(false) }
    var isSpeaking by remember { mutableStateOf(false) }
    val tts = remember {
        var engine: TextToSpeech? = null
        engine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                engine?.language = Locale.FRENCH
                engine?.setSpeechRate(0.9f)
                isTtsReady = true
            }
        }
        engine
    }

    DisposableEffect(Unit) {
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    fun speakText(text: String) {
        if (!isTtsReady) return
        if (isSpeaking) {
            tts.stop()
            isSpeaking = false
        } else {
            isSpeaking = true
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "lango_lesson")
        }
    }

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
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        tts.stop()
                        onBack()
                    },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("lesson_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Retour")
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isSango) contenu.titreSg else contenu.titreFr,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = if (isSango) "Mandarango sân Internet" else "Pack hors-ligne • Audio intégré",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val totalUnits = steps.size + 1
            val currentProgress = when (stage) {
                LessonStage.STEPS -> (currentStepIdx + 1).toFloat() / totalUnits
                LessonStage.QUIZ -> (steps.size + (quizIdx + 1).toFloat() / quiz.size.coerceAtLeast(1)) / totalUnits
                LessonStage.BADGE_CELEBRATION -> 1f
            }
            LinearProgressIndicator(
                progress = { currentProgress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (stage) {
                LessonStage.STEPS -> {
                    val step = steps[currentStepIdx]
                    val stepTitle = if (isSango) step.titreSg else step.titreFr
                    val stepBody = if (isSango) step.texteSg else step.texteFr
                    val stepTip = if (isSango) step.conseilPratiqueSg else step.conseilPratiqueFr

                    // Audio Player Card (Sango / French)
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(52.dp)
                                    .clickable { speakText("$stepTitle. $stepBody. $stepTip") }
                                    .testTag("lesson_audio_play_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isSpeaking) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                        contentDescription = "Lire l'audio",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.GraphicEq,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isSango) "Audio na Sango / FR" else "Guide audio hors-ligne",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Text(
                                    text = if (isSpeaking) {
                                        if (isSango) "A yeke diko..." else "Lecture en cours..."
                                    } else {
                                        if (isSango) "Pika ti ma leçon so" else "Appuie pour écouter cette étape"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Lesson Content Card
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = if (isSango) {
                                        "Mbâge ${step.numero} / ${steps.size}"
                                    } else {
                                        "Étape ${step.numero} / ${steps.size}"
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = stepTitle,
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = stepBody,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Lightbulb,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stepTip,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (currentStepIdx > 0) {
                            OutlinedButton(
                                onClick = {
                                    tts.stop()
                                    isSpeaking = false
                                    currentStepIdx -= 1
                                },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                            ) {
                                Text(if (isSango) "Kiri" else "Précédent")
                            }
                        }
                        Button(
                            onClick = {
                                tts.stop()
                                isSpeaking = false
                                if (currentStepIdx < steps.lastIndex) {
                                    currentStepIdx += 1
                                    onSaveProgress(currentStepIdx + 1, steps.size, 0, false) {}
                                } else {
                                    stage = LessonStage.QUIZ
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1.4f)
                                .height(52.dp)
                                .testTag("lesson_next_step_button")
                        ) {
                            Text(
                                text = if (currentStepIdx < steps.lastIndex) {
                                    if (isSango) "Gue na li ni" else "Étape suivante"
                                } else {
                                    if (isSango) "Tara exercice" else "Passer au quiz"
                                },
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null)
                        }
                    }
                }

                LessonStage.QUIZ -> {
                    val question = quiz[quizIdx]
                    val qTitle = if (isSango) question.questionSg else question.questionFr
                    val options = if (isSango) question.optionsSg else question.optionsFr

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Quiz,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSango) {
                                    "Hundango tene ${quizIdx + 1} / ${quiz.size}"
                                } else {
                                    "Question ${quizIdx + 1} / ${quiz.size}"
                                },
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        OutlinedButton(
                            onClick = { speakText(qTitle) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isSango) "Diko" else "Écouter")
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Prominent Question Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (!question.imageUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = question.imageUrl,
                                    contentDescription = "Illustration question",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                            }
                            Text(
                                text = qTitle,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isSango) "Horo kiringo tongo ni:" else "Sélectionne la bonne réponse :",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    options.forEachIndexed { idx, optLabel ->
                        val isSelected = selectedAnswerIdx == idx
                        val isCorrect = idx == question.correctIndex
                        val borderColor = when {
                            selectedAnswerIdx == -1 -> MaterialTheme.colorScheme.outline
                            isSelected && isCorrect -> MaterialTheme.colorScheme.primary
                            isSelected && !isCorrect -> MaterialTheme.colorScheme.error
                            isCorrect -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.outline
                        }
                        val bgColor = when {
                            selectedAnswerIdx == -1 -> MaterialTheme.colorScheme.surface
                            isCorrect -> MaterialTheme.colorScheme.primaryContainer
                            isSelected && !isCorrect -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.surface
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .minimumInteractiveComponentSize()
                                .clickable(enabled = selectedAnswerIdx == -1) {
                                    selectedAnswerIdx = idx
                                    if (idx == question.correctIndex) {
                                        correctCount += 1
                                    }
                                }
                                .testTag("quiz_option_$idx"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = bgColor),
                            border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = optLabel,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )
                                if (selectedAnswerIdx != -1 && isCorrect) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    AnimatedVisibility(visible = selectedAnswerIdx != -1) {
                        Button(
                            onClick = {
                                if (quizIdx < quiz.lastIndex) {
                                    quizIdx += 1
                                    selectedAnswerIdx = -1
                                } else {
                                    val finalScore = ((correctCount.toFloat() / quiz.size) * 100).toInt()
                                    onSaveProgress(steps.size, steps.size, finalScore, true) { badge ->
                                        awardedBadge = badge
                                        stage = LessonStage.BADGE_CELEBRATION
                                    }
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("quiz_continue_button")
                        ) {
                            Text(
                                text = if (quizIdx < quiz.lastIndex) {
                                    if (isSango) "Question ti peko" else "Question suivante"
                                } else {
                                    if (isSango) "Hunzi & mû badge" else "Terminer et obtenir le badge"
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                LessonStage.BADGE_CELEBRATION -> {
                    val badgeTitle = if (isSango) {
                        awardedBadge?.titreSg ?: lessonData.badgeTitreSg
                    } else {
                        awardedBadge?.titreFr ?: lessonData.badgeTitreFr
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.tertiaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.EmojiEvents,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(56.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (isSango) "Nzoni kua ! Badge ti mo a si" else "Félicitations ! Badge débloqué",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (isSango) {
                                "Score ti mo : $correctCount / ${quiz.size}. Progression a bata na téléfone."
                            } else {
                                "Score : $correctCount / ${quiz.size}. Ta progression est enregistrée hors-ligne."
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Verified,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = badgeTitle,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isSango) {
                                        "Statut : PRATIQUE • Hunda azo ota ti fa ni"
                                    } else {
                                        "Statut : PRATIQUE • Passe à VÉRIFIÉ avec 3 avis"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(22.dp))

                        Button(
                            onClick = onGoToVerification,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("badge_request_verification_button")
                        ) {
                            Text(
                                text = if (isSango) "Hunda azo ota ti fa badge" else "Faire vérifier par 3 pairs / clients",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = onBack,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(if (isSango) "Kiri na mandarango" else "Retour à la bibliothèque")
                        }
                    }
                }
            }
        }
    }
}
