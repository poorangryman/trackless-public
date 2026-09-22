import sys

content = open('app/src/main/java/ru/otvykaniye/tracker/ui/screens/MainScreen.kt', encoding='utf8').read()

# 1. Update StatsGrid call in MainScreen
sg_call_old = """StatsGrid(state, timeSinceLast, onDeleteEntry = { id -> viewModel.deleteEntry(id) })"""
sg_call_new = """StatsGrid(state, timeSinceLast, onDeleteEntry = { id -> viewModel.deleteEntry(id) }, onCustomRecord = { ts -> viewModel.recordUseAtTime(state.activeKind, ts) })"""
content = content.replace(sg_call_old, sg_call_new)

# 2. Remove showCustomTimePicker from HeroCard
hc_old = """fun HeroCard(state: TracklessState, timeSinceLast: Long, onRecord: () -> Unit, onSos: () -> Unit, onUndo: () -> Unit, onCustomRecord: (Long) -> Unit) {
    val lang = state.language
    var showCustomTimePicker by remember { mutableStateOf(false) }

    if (showCustomTimePicker) {
        ru.otvykaniye.tracker.ui.components.CustomTimePickerDialog(
            onDismiss = { showCustomTimePicker = false },
            onConfirm = { ts ->
                onCustomRecord(ts)
                showCustomTimePicker = false
            }
        )
    }

    ru.otvykaniye.tracker.ui.components.GlassCard(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(Strings.get(lang, "time_passed"), color = TextDim, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { showCustomTimePicker = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Custom Time", tint = TextDim)
                    }"""

hc_new = """fun HeroCard(state: TracklessState, timeSinceLast: Long, onRecord: () -> Unit, onSos: () -> Unit, onUndo: () -> Unit, onCustomRecord: (Long) -> Unit) {
    val lang = state.language
    ru.otvykaniye.tracker.ui.components.GlassCard(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(Strings.get(lang, "time_passed"), color = TextDim, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {"""

content = content.replace(hc_old, hc_new)

with open('app/src/main/java/ru/otvykaniye/tracker/ui/screens/MainScreen.kt', 'w', encoding='utf8') as f:
    f.write(content)
print("MainScreen patched successfully!")
