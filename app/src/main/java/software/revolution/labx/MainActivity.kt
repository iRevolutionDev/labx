package software.revolution.labx

import android.graphics.Typeface
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import io.github.rosemoe.sora.langs.java.JavaLanguage
import io.github.rosemoe.sora.widget.CodeEditor
import software.revolution.labx.ui.theme.LabxTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            LabxTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AndroidView(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        factory = { context ->
                            CodeEditor(context).apply {
                                setText("void setup() {\n    // Your code here\n}\n\nvoid draw() {\n    // Your code here\n}")
                                typefaceText = Typeface.MONOSPACE
                                nonPrintablePaintingFlags =
                                    CodeEditor.FLAG_DRAW_WHITESPACE_LEADING or
                                            CodeEditor.FLAG_DRAW_LINE_SEPARATOR or
                                            CodeEditor.FLAG_DRAW_WHITESPACE_IN_SELECTION
                                setEditorLanguage(JavaLanguage())
                            }
                        }
                    )
                }
            }
        }
    }
}
