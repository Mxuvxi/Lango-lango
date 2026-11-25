package com.example.ui.util

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

@Stable
class LangoSpeakerState(private val tts: TextToSpeech) {
    var isReady by mutableStateOf(false)
        internal set
    var activeUtteranceId by mutableStateOf<String?>(null)
        internal set

    fun toggleSpeak(utteranceId: String, text: String) {
        if (!isReady) return
        if (activeUtteranceId == utteranceId) {
            tts.stop()
            activeUtteranceId = null
        } else {
            activeUtteranceId = utteranceId
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        }
    }

    fun stop() {
        tts.stop()
        activeUtteranceId = null
    }
}

@Composable
fun rememberLangoSpeaker(): LangoSpeakerState {
    val context = LocalContext.current
    val stateHolder = remember { arrayOfNulls<LangoSpeakerState>(1) }
    val tts = remember {
        var engine: TextToSpeech? = null
        engine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                engine?.language = Locale.FRENCH
                engine?.setSpeechRate(0.9f)
                stateHolder[0]?.isReady = true
            }
        }.apply {
            setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) {
                    if (stateHolder[0]?.activeUtteranceId == utteranceId) {
                        stateHolder[0]?.activeUtteranceId = null
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    if (stateHolder[0]?.activeUtteranceId == utteranceId) {
                        stateHolder[0]?.activeUtteranceId = null
                    }
                }
            })
        }
        engine
    }

    val speaker = remember(tts) {
        LangoSpeakerState(tts).also { stateHolder[0] = it }
    }

    DisposableEffect(tts) {
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    return speaker
}
