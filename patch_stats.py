import sys

content = open('app/src/main/java/ru/otvykaniye/tracker/ui/components/StatsGrid.kt', encoding='utf8').read()

# Add imports
imports_old = """import androidx.compose.material3.Text
import androidx.compose.runtime.Composable"""
imports_new = """import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable"""

content = content.replace(imports_old, imports_new)

# Modify SectionHeader
sh_old = """@Composable
fun SectionHeader(title: String, icon: ImageVector, iconTint: androidx.compose.ui.graphics.Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(title, color = iconTint, fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 0.5.sp)
    }
}"""
sh_new = """@Composable
fun SectionHeader(title: String, icon: ImageVector, iconTint: androidx.compose.ui.graphics.Color, action: @Composable () -> Unit = {}) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(title, color = iconTint, fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 0.5.sp)
        }
        action()
    }
}"""

content = content.replace(sh_old, sh_new)

# Modify StatsGrid signature and add state
sg_old = """@Composable
fun StatsGrid(state: TracklessState, timeSinceLast: Long, onDeleteEntry: (String) -> Unit) {
    val profile = state.profiles[state.activeKind] ?: return
    val lang = state.language"""
sg_new = """@Composable
fun StatsGrid(state: TracklessState, timeSinceLast: Long, onDeleteEntry: (String) -> Unit, onCustomRecord: (Long) -> Unit = {}) {
    val profile = state.profiles[state.activeKind] ?: return
    val lang = state.language

    var showCustomTimePicker by remember { mutableStateOf(false) }

    if (showCustomTimePicker) {
        CustomTimePickerDialog(
            onDismiss = { showCustomTimePicker = false },
            onConfirm = { ts ->
                onCustomRecord(ts)
                showCustomTimePicker = false
            }
        )
    }"""

content = content.replace(sg_old, sg_new)

# Modify History SectionHeader call
hist_old = """SectionHeader(Strings.get(lang, "history"), Icons.Rounded.History, TextPrimary)"""
hist_new = """SectionHeader(
                    Strings.get(lang, "history"), 
                    Icons.Rounded.History, 
                    TextPrimary,
                    action = {
                        IconButton(onClick = { showCustomTimePicker = true }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Add, contentDescription = "Add custom record", tint = TextDim)
                        }
                    }
                )"""

content = content.replace(hist_old, hist_new)

with open('app/src/main/java/ru/otvykaniye/tracker/ui/components/StatsGrid.kt', 'w', encoding='utf8') as f:
    f.write(content)
print("StatsGrid patched successfully!")
