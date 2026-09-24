package com.cripeda.quicktilesrotate.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.StayCurrentLandscape
import androidx.compose.material.icons.filled.StayCurrentPortrait
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cripeda.quicktilesrotate.data.RotationPreferences
import com.cripeda.quicktilesrotate.data.RotationState
import com.cripeda.quicktilesrotate.service.OrientationOverlayService
import com.cripeda.quicktilesrotate.ui.theme.QuickTilesRotateTheme
import kotlinx.coroutines.launch

class TileSelectionActivity : ComponentActivity() {

    private lateinit var preferences: RotationPreferences

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        preferences = RotationPreferences.getInstance(this)

        setContent {
            QuickTilesRotateTheme {
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                val scope = rememberCoroutineScope()
                val currentState by preferences.stateFlow.collectAsState()

                ModalBottomSheet(
                    onDismissRequest = { finish() },
                    sheetState = sheetState,
                    containerColor = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                ) {
                    OrientationBottomSheetContent(
                        currentState = currentState,
                        onSelectState = { selectedState ->
                            if (selectedState != RotationState.OFF && !Settings.canDrawOverlays(this@TileSelectionActivity)) {
                                Toast.makeText(
                                    this@TileSelectionActivity,
                                    "È necessario abilitare prima il permesso di sovrapposizione",
                                    Toast.LENGTH_SHORT
                                ).show()
                                startActivity(Intent(this@TileSelectionActivity, MainActivity::class.java))
                                finish()
                            } else {
                                OrientationOverlayService.startWithState(this@TileSelectionActivity, selectedState)
                                scope.launch {
                                    sheetState.hide()
                                    finish()
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun OrientationBottomSheetContent(
    currentState: RotationState,
    onSelectState: (RotationState) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ScreenRotation,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Orientamento Schermo",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Scegli la modalità di rotazione da applicare",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Opzione 1: Orizzontale (Landscape)
        OrientationOptionCard(
            title = "Orizzontale (Landscape)",
            subtitle = "Forzato per app come Instagram",
            icon = Icons.Default.StayCurrentLandscape,
            isSelected = currentState == RotationState.LANDSCAPE,
            onClick = { onSelectState(RotationState.LANDSCAPE) }
        )

        // Opzione 2: Verticale (Portrait)
        OrientationOptionCard(
            title = "Verticale (Portrait)",
            subtitle = "Bloccato in modalità verticale",
            icon = Icons.Default.StayCurrentPortrait,
            isSelected = currentState == RotationState.PORTRAIT,
            onClick = { onSelectState(RotationState.PORTRAIT) }
        )

        // Opzione 3: Off (Sistema)
        OrientationOptionCard(
            title = "Disattivato (Sistema)",
            subtitle = "Ripristina la rotazione predefinita di Android",
            icon = Icons.Default.ScreenRotation,
            isSelected = currentState == RotationState.OFF,
            onClick = { onSelectState(RotationState.OFF) }
        )
    }
}

@Composable
fun OrientationOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            RadioButton(
                selected = isSelected,
                onClick = onClick
            )
        }
    }
}
