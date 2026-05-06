package com.beigel.leetSpeak_Generator.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

class SpeechInputManager(private val context: Context) {

    private var recognizer: SpeechRecognizer? = null

    fun startListening(
        onResult: (String) -> Unit,
        onError: (String) -> Unit,
        onStateChange: (Boolean) -> Unit,
        errorMessages: SpeechErrorMessages  // NEU: Strings von außen
    ) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError(errorMessages.unavailable)
            return
        }

        recognizer?.destroy()
        recognizer = SpeechRecognizer.createSpeechRecognizer(context)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        recognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) = onStateChange(true)
            override fun onEndOfSpeech() = onStateChange(false)

            override fun onError(error: Int) {
                onStateChange(false)
                val msg = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH       -> errorMessages.noMatch
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> errorMessages.timeout
                    SpeechRecognizer.ERROR_AUDIO          -> errorMessages.audio
                    else                                  -> errorMessages.generic
                }
                onError(msg)
            }

            override fun onResults(results: Bundle?) {
                onStateChange(false)
                results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    ?.let { onResult(it) }
            }

            override fun onPartialResults(partial: Bundle?) {
                partial
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    ?.let { onResult(it) }
            }

            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        recognizer?.startListening(intent)
    }

    fun stop() {
        recognizer?.stopListening()
        recognizer?.destroy()
        recognizer = null
    }

    data class SpeechErrorMessages(
        val unavailable: String,
        val noMatch: String,
        val timeout: String,
        val audio: String,
        val generic: String
    )
}