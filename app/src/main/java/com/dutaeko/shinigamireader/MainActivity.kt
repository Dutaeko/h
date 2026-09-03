package com.dutaeko.shinigamireader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import com.dutaeko.shinigamireader.ui.theme.ShinigamiReaderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShinigamiReaderTheme {
                Surface {
                    ShinigamiReaderApp()
                }
            }
        }
    }
}
