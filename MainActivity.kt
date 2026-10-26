package com.aicody.virtualxbox

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private val bg = Color(0xFF0B1018)
private val panel = Color(0xFF151E2B)
private val accent = Color(0xFF54D6A0)
private val muted = Color(0xFF8FA0B5)

class MainActivity : ComponentActivity() {
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 31) permissionLauncher.launch(arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN))
        setContent { ControllerApp() }
    }
}

@Composable
fun ControllerApp() {
    val scope = rememberCoroutineScope()
    var profile by remember { mutableStateOf("Default") }
    var mode by remember { mutableStateOf("USB / ADB") }
    var mac by remember { mutableStateOf("") }
    var shaId by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Disconnected") }
    var transport by remember { mutableStateOf<ControllerTransport?>(null) }
    var pressed by remember { mutableStateOf(setOf<String>()) }
    var lx by remember { mutableFloatStateOf(0f) }
    var ly by remember { mutableFloatStateOf(0f) }
    var rx by remember { mutableFloatStateOf(0f) }
    var ry by remember { mutableFloatStateOf(0f) }
    var lt by remember { mutableFloatStateOf(0f) }
    var rt by remember { mutableFloatStateOf(0f) }
    var sensitivity by remember { mutableFloatStateOf(1f) }
    var deadzone by remember { mutableFloatStateOf(0.08f) }
    val profiles = listOf("Default", "FPS", "Racing", "Platformer")
    val sendState = {
        val s = ControllerState(profile, pressed, applyDeadzone(lx * sensitivity, deadzone), applyDeadzone(ly * sensitivity, deadzone),
            applyDeadzone(rx * sensitivity, deadzone), applyDeadzone(ry * sensitivity, deadzone), lt, rt)
        scope.launch { runCatching { transport?.send(s) }.onFailure { status = "Send error: ${it.message}" } }
    }
    MaterialTheme(colorScheme = darkColorScheme(primary = accent, background = bg, surface = panel)) {
        Column(Modifier.fillMaxSize().background(bg).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("AI CODY", color = accent, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Text("VIRTUAL XBOX 360", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                }
                Surface(color = if (transport != null) Color(0xFF173A31) else panel, shape = RoundedCornerShape(20.dp)) {
                    Text(status, color = if (transport != null) accent else muted, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
                }
            }
            Card(colors = CardDefaults.cardColors(containerColor = panel)) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("CONNECTION", color = accent, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = mode == "USB / ADB", onClick = { mode = "USB / ADB" }, label = { Text("USB tunnel") })
                        FilterChip(selected = mode == "Bluetooth", onClick = { mode = "Bluetooth" }, label = { Text("Bluetooth SPP") })
                    }
                    if (mode == "Bluetooth") OutlinedTextField(mac, { mac = it }, label = { Text("PC Bluetooth MAC address") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(shaId, { shaId = it }, label = { Text("Optional SHA / device label (metadata only)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {
                            scope.launch {
                                runCatching {
                                    val next = if (mode == "USB / ADB") AdbTunnelTransport() else {
                                        require(mac.matches(Regex("(?i)^([0-9a-f]{2}:){5}[0-9a-f]{2}$"))) { "Enter a valid MAC address" }
                                        BluetoothSppTransport(mac)
                                    }
                                    next.connect()
                                    transport?.close()
                                    transport = next
                                    status = "Connected · ${next.description}"
                                }.onFailure { status = "Connection failed: ${it.message}" }
                            }
                        }) { Text("Connect") }
                        OutlinedButton(onClick = { transport?.close(); transport = null; pressed = emptySet(); status = "Disconnected" }) { Text("Disconnect") }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Card(Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = panel)) {
                    Column(Modifier.padding(10.dp)) {
                        Text("PROFILE", color = accent, fontWeight = FontWeight.Bold)
                        profiles.forEach { p -> Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = profile == p, onClick = { profile = p; sendState() })
                            Text(p, color = Color.White)
                        } }
                    }
                }
                Card(Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = panel)) {
                    Column(Modifier.padding(10.dp)) {
                        Text("TUNING", color = accent, fontWeight = FontWeight.Bold)
                        Text("Sensitivity ${(sensitivity * 100).toInt()}%", color = Color.White)
                        Slider(sensitivity, { sensitivity = it }, valueRange = 0.5f..1.5f)
                        Text("Dead zone ${(deadzone * 100).toInt()}%", color = Color.White)
                        Slider(deadzone, { deadzone = it }, valueRange = 0f..0.25f)
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("LEFT STICK", color = muted)
                    StickPad(lx, ly, { x, y -> lx=x; ly=y; sendState() }, { lx=0f; ly=0f; sendState() })
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("FACE BUTTONS", color = muted)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row { GameButton("Y", pressed.contains("Y"), { toggleButton("Y", pressed) { pressed = it; sendState() } }); Spacer(Modifier.width(8.dp)); GameButton("X", pressed.contains("X"), { toggleButton("X", pressed) { pressed = it; sendState() } }) }
                        Row { GameButton("B", pressed.contains("B"), { toggleButton("B", pressed) { pressed = it; sendState() } }); Spacer(Modifier.width(8.dp)); GameButton("A", pressed.contains("A"), { toggleButton("A", pressed) { pressed = it; sendState() } }) }
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("RIGHT STICK", color = muted)
                    StickPad(rx, ry, { x, y -> rx=x; ry=y; sendState() }, { rx=0f; ry=0f; sendState() })
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                listOf("LB", "BACK", "START", "RB").forEach { b ->
                    GameButton(b, pressed.contains(b), { toggleButton(b, pressed) { pressed = it; sendState() } }, compact = true)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) {
                    Text("LT ${(lt*100).toInt()}%", color = Color.White)
                    Slider(lt, { lt=it; sendState() })
                }
                Column(Modifier.weight(1f)) {
                    Text("RT ${(rt*100).toInt()}%", color = Color.White)
                    Slider(rt, { rt=it; sendState() })
                }
            }
            Text("USB uses ADB reverse. Bluetooth requires a compatible PC SPP server. This prototype does not impersonate genuine Microsoft hardware.", color = muted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun toggleButton(button: String, current: Set<String>, update: (Set<String>) -> Unit) {
    update(if (button in current) current - button else current + button)
}

@Composable
private fun GameButton(label: String, selected: Boolean, onClick: () -> Unit, compact: Boolean = false) {
    val colors = mapOf("A" to Color(0xFF1FA86A), "B" to Color(0xFFE04C55), "X" to Color(0xFF3786E8), "Y" to Color(0xFFE5C14B))
    Surface(onClick = onClick, shape = CircleShape, color = if (selected) accent else colors[label] ?: Color(0xFF27374A),
        modifier = Modifier.size(if (compact) 54.dp else 58.dp)) {
        Box(contentAlignment = Alignment.Center) { Text(label, color = Color.White, fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun StickPad(x: Float, y: Float, onMove: (Float, Float) -> Unit, onRelease: () -> Unit) {
    // Prototype stick: directional buttons provide predictable input without gesture-coordinate assumptions.
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        GameButton("↑", y < -0.1f, { onMove(x, -1f) }, true)
        Row(verticalAlignment = Alignment.CenterVertically) {
            GameButton("←", x < -0.1f, { onMove(-1f, y) }, true)
            Spacer(Modifier.width(6.dp))
            GameButton("●", x == 0f && y == 0f, onRelease, true)
            Spacer(Modifier.width(6.dp))
            GameButton("→", x > 0.1f, { onMove(1f, y) }, true)
        }
        GameButton("↓", y > 0.1f, { onMove(x, 1f) }, true)
    }
}
