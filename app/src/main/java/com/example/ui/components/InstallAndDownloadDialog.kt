package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

@Composable
fun InstallAndDownloadDialog(
    isSwahili: Boolean,
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var copiedUrl by remember { mutableStateOf(false) }

    val sharedAppUrl = "https://ais-pre-4wenbbar7odvuk7myk5j3d-10485178411.europe-west2.run.app"

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(vertical = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.InstallMobile,
                            contentDescription = "Install",
                            tint = AccentNeonGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSwahili) "Jinsi ya Kupakua ZIP, GitHub & APK" else "Export & Install Guide",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Option 1: Export (ZIP, GitHub, Antigravity) via Code button
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Code, contentDescription = "Code", tint = AccentCyan)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isSwahili) "Njia 1: Export ya ZIP, GitHub au Antigravity" else "Method 1: Export ZIP, GitHub or Antigravity",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentCyan
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (isSwahili) {
                                        "Kama ulivyosema, ukibonyeza kitufe cha 'Code' kwenye interface ya AI Studio ndipo chaguzi zote zinapojitokeza:\n" +
                                                "1. Bonyeza kitufe cha 'Code' juu kwenye AI Studio.\n" +
                                                "2. Hapo ndipo utaona machaguo ya Export as ZIP, Push to GitHub, au Antigravity.\n" +
                                                "3. Unaweza kuchagua unayotaka kupakua msimbo wote au kuunganisha na hifadhi yako."
                                    } else {
                                        "As noted, clicking the 'Code' button in the AI Studio interface reveals the export options:\n" +
                                                "1. Click 'Code' in the top AI Studio toolbar.\n" +
                                                "2. The options for Export as ZIP, Push to GitHub, or Antigravity will appear directly.\n" +
                                                "3. Select your preferred format to download or sync the full codebase."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextWhite
                                )
                            }
                        }
                    }

                    // Option 2: Install as Web App on Android
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AccentNeonGreen.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Android, contentDescription = "App", tint = AccentNeonGreen)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isSwahili) "Njia 2: Weka App kwenye Simu (Add to Home Screen)" else "Method 2: Install App on Phone",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentNeonGreen
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (isSwahili) {
                                        "Kutumia app hii kama programu kamili ya simu yako:\n" +
                                                "1. Fungua kiungo cha hapa chini kwenye kivinjari cha Google Chrome cha simu yako.\n" +
                                                "2. Bonyeza nukta tatu (⋮) za Chrome.\n" +
                                                "3. Chagua 'Add to Home screen' (Weka kwenye Skrini ya Nyumbani).\n" +
                                                "4. Itakaa kwenye simu kama app za kawaida bila kupoteza data!"
                                    } else {
                                        "To run as a full native app on your phone:\n" +
                                                "1. Open the live URL in Google Chrome on Android.\n" +
                                                "2. Tap the Chrome 3-dots menu (⋮).\n" +
                                                "3. Select 'Add to Home screen'.\n" +
                                                "4. It installs instantly with the app icon!"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextWhite
                                )
                            }
                        }
                    }

                    // Direct Link Copy Card
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = if (isSwahili) "Kiungo cha Moja kwa Moja cha Simu (Direct Link):" else "Direct Live App URL:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    color = Color(0xFF04070F),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = sharedAppUrl,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = AccentCyan,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(sharedAppUrl))
                                        copiedUrl = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (copiedUrl) AccentNeonGreen else MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (copiedUrl)
                                            (if (isSwahili) "Imenakiliwa Kwenye Clipboard!" else "Copied to Clipboard!")
                                        else
                                            (if (isSwahili) "Nakili Kiungo Hiki (Copy URL)" else "Copy Live App Link"),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isSwahili) "Funga" else "Close")
                }
            }
        }
    }
}
