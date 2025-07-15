package com.beigel.leetSpeak_Generator.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.beigel.leetSpeak_Generator.ui.theme.LeetspeakGeneratorTheme

/**
 * Compose Previews für die LeetSpeak Generator App
 */

// ✅ MAIN SCREEN PREVIEWS
@Preview(name = "Main Screen - Empty")
@Composable
fun MainScreenEmptyPreview() {
    LeetspeakGeneratorTheme {
        MainScreenPreviewWrapper(
            inputText = "",
            outputText = "",
            currentMode = "Simple Leet"
        )
    }
}

@Preview(name = "Main Screen - With Content")
@Composable
fun MainScreenContentPreview() {
    LeetspeakGeneratorTheme {
        MainScreenPreviewWrapper(
            inputText = "Hello World",
            outputText = "#3110 W0r1|)",
            currentMode = "Extended Leet"
        )
    }
}

@Preview(name = "Main Screen - Long Text")
@Composable
fun MainScreenLongTextPreview() {
    LeetspeakGeneratorTheme {
        MainScreenPreviewWrapper(
            inputText = "This is a very long text to test how the adaptive font sizing works in the input and output fields. Lorem ipsum dolor sit amet.",
            outputText = "7#15 15 4 v3r`/ 10n6 73x7 70 7357 #0w 7#3 4|)4p71v3 |=0n7 515ln6 w0rk5 1n 7#3 1npu7 4n|) 0u7pu7 |=13l|)5.",
            currentMode = "Extended Leet"
        )
    }
}

// ✅ DARK MODE PREVIEWS
@Preview(name = "Main Screen - Dark Mode")
@Composable
fun MainScreenDarkPreview() {
    LeetspeakGeneratorTheme(darkTheme = true) {
        MainScreenPreviewWrapper(
            inputText = "Hello World",
            outputText = "#3110 W0r1|)",
            currentMode = "Simple Leet"
        )
    }
}

// ✅ EINZELNE KOMPONENTEN PREVIEWS
@Preview(name = "Input Card - Empty")
@Composable
fun InputCardEmptyPreview() {
    LeetspeakGeneratorTheme {
        InputCard(
            inputText = "",
            onInputChange = {},
            onClearText = {},
            showHeader = true
        )
    }
}

@Preview(name = "Input Card - With Text")
@Composable
fun InputCardWithTextPreview() {
    LeetspeakGeneratorTheme {
        InputCard(
            inputText = "Hello World! This is a test.",
            onInputChange = {},
            onClearText = {},
            showHeader = true
        )
    }
}

@Preview(name = "Output Card")
@Composable
fun OutputCardPreview() {
    LeetspeakGeneratorTheme {
        OutputCard(
            outputText = "#3110 W0r1|)! 7#15 15 4 7357.",
            currentMode = "Extended Leet",
            onCopyClick = {},
            showHeader = true
        )
    }
}

@Preview(name = "Button Section")
@Composable
fun ButtonSectionPreview() {
    LeetspeakGeneratorTheme {
        ButtonSection(
            currentMode = "Extended Leet",
            onLeetSelectorClick = {},
            onClearClick = {}
        )
    }
}

// ✅ VERSCHIEDENE MODI PREVIEWS
class ModePreviewProvider : PreviewParameterProvider<String> {
    override val values = sequenceOf(
        "Simple Leet",
        "Extended Leet",
        "Custom Leet",
        "Mein Custom Leet"
    )
}

@Preview(name = "Different Modes")
@Composable
fun DifferentModesPreview(
    @PreviewParameter(ModePreviewProvider::class) mode: String
) {
    LeetspeakGeneratorTheme {
        OutputCard(
            outputText = when(mode) {
                "Simple Leet" -> "H3110 W0r1d"
                "Extended Leet" -> "#3110 W0rl|)"
                else -> "H3ll0 W0rld"
            },
            currentMode = mode,
            onCopyClick = {},
            showHeader = true
        )
    }
}

// ✅ HELPER WRAPPER FÜR MAIN SCREEN
@Composable
private fun MainScreenPreviewWrapper(
    inputText: String,
    outputText: String,
    currentMode: String
) {
    // Simuliert den MainScreen State für Preview
    var input by remember { mutableStateOf(inputText) }
    var output by remember { mutableStateOf(outputText) }

    // Einfache Layout-Simulation
    androidx.compose.foundation.layout.Column(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
    ) {
        // Input Card
        InputCard(
            inputText = input,
            onInputChange = { input = it },
            onClearText = { input = "" },
            showHeader = true,
            modifier = androidx.compose.ui.Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        // Output Card (nur wenn Output vorhanden)
        if (output.isNotEmpty()) {
            OutputCard(
                outputText = output,
                currentMode = currentMode,
                onCopyClick = {},
                showHeader = true,
                modifier = androidx.compose.ui.Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }

        // Button Section
        ButtonSection(
            currentMode = currentMode,
            onLeetSelectorClick = {},
            onClearClick = {
                input = ""
                output = ""
            }
        )
    }
}

// ✅ DEVICE PREVIEWS
@Preview(
    name = "Phone",
    device = "spec:width=411dp,height=891dp"
)
@Composable
fun PhonePreview() {
    MainScreenContentPreview()
}

@Preview(
    name = "Tablet",
    device = "spec:width=1280dp,height=800dp,dpi=240"
)
@Composable
fun TabletPreview() {
    MainScreenContentPreview()
}

// ✅ FONT SCALE PREVIEWS
@Preview(
    name = "Large Font",
    fontScale = 1.5f
)
@Composable
fun LargeFontPreview() {
    MainScreenContentPreview()
}

@Preview(
    name = "Small Font",
    fontScale = 0.85f
)
@Composable
fun SmallFontPreview() {
    MainScreenContentPreview()
}