package com.example.data.repository

import android.util.Log
import com.example.data.local.entity.CoachMessageEntity
import com.example.data.local.entity.LangueCode
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout

private const val TAG = "LangoAiRepository"
private const val GEMINI_MODEL = "gemini-3.8-flash"
private const val AI_TIMEOUT_MS = 60_000L

sealed interface CoachStreamEvent {
    data class Partial(val fullText: String) : CoachStreamEvent
    data class Completed(val fullText: String) : CoachStreamEvent
}

class LangoAiRepository {

    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = GEMINI_MODEL,
            systemInstruction = content {
                text(
                    """
                    Tu es le Coach Métiers Lango, un conseiller pratique et bienveillant pour les jeunes, les artisans et les petits entrepreneurs en République Centrafricaine (Bangui, Bimbo, Berbérati, Bouar, Bambari).
                    Règles de réponse :
                    - Réponds de manière très concrète, structurée (étapes courtes ou listes claires) et adaptée au contexte local centrafricain (monnaie en FCFA, Mobile Money Orange Money / Telecel Money, ateliers locaux, sécurité électrique/solaire, couture, réparation téléphones/motos, agroalimentaire).
                    - Si l'utilisateur écrit ou choisit la langue Sango (SG), réponds en sango simple et naturel avec les termes techniques courants entre parenthèses en français. Sinon réponds en français clair et accessible, facile à écouter en audio (évite le jargon complexe et les tableaux Markdown).
                    - Reste concis (maximum 150 mots sauf demande de devis détaillé) pour une lecture agréable à voix haute.
                    """.trimIndent()
                )
            }
        )
    }

    fun streamAdvice(
        question: String,
        langue: String,
        userName: String,
        tradeLabel: String,
        city: String,
        recentHistory: List<CoachMessageEntity>
    ): Flow<CoachStreamEvent> = flow {
        val historyContent = recentHistory.takeLast(6).map { msg ->
            val role = if (msg.role == "MODEL") "model" else "user"
            content(role) { text(msg.texte) }
        }

        val langInstruction = if (langue == LangueCode.SG) {
            "Langue demandée : Sango (SG)."
        } else {
            "Langue demandée : Français (FR)."
        }

        val enrichedPrompt = buildString {
            append("[Contexte Lango RCA — $langInstruction")
            if (userName.isNotBlank()) append(" Utilisateur : $userName.")
            if (tradeLabel.isNotBlank()) append(" Métier : $tradeLabel.")
            if (city.isNotBlank()) append(" Ville : $city.")
            append("]\n")
            append(question.trim())
        }

        val chat = model.startChat(history = historyContent)
        val accumulated = StringBuilder()

        try {
            withTimeout(AI_TIMEOUT_MS) {
                chat.sendMessageStream(enrichedPrompt).collect { chunk ->
                    val piece = chunk.text.orEmpty()
                    if (piece.isNotEmpty()) {
                        accumulated.append(piece)
                        emit(CoachStreamEvent.Partial(accumulated.toString()))
                    }
                }
            }
            val finalText = accumulated.toString().trim()
            if (finalText.isBlank()) {
                throw IllegalStateException("Le coach IA n'a renvoyé aucune réponse.")
            }
            emit(CoachStreamEvent.Completed(finalText))
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Gemini streaming timed out after 60s", e)
            throw e
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Firebase AI Logic request failed", e)
            throw e
        }
    }
}
