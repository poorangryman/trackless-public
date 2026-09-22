import sys

old_str = """fun HeroCard(state: TracklessState, timeSinceLast: Long, onRecord: () -> Unit, onSos: () -> Unit, onUndo: () -> Unit, onCustomRecord: (Long) -> Unit) {
    val lang = state.language
    val context = androidx.compose.ui.platform.LocalContext.current
    ru.otvykaniye.tracker.ui.components.GlassCard(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(Strings.get(lang, "time_passed"), color = TextDim, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = {
                            val cal = java.util.Calendar.getInstance()
                            android.app.DatePickerDialog(context, { _, year, month, dayOfMonth ->
                                val timeCal = java.util.Calendar.getInstance()
                                android.app.TimePickerDialog(context, { _, hourOfDay, minute ->
                                    timeCal.set(year, month, dayOfMonth, hourOfDay, minute, 0)
                                    onCustomRecord(timeCal.timeInMillis)
                                }, cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE), true).show()
                            }, cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH), cal.get(java.util.Calendar.DAY_OF_MONTH)).show()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Custom Time", tint = TextDim)
                    }"""

new_str = """fun HeroCard(state: TracklessState, timeSinceLast: Long, onRecord: () -> Unit, onSos: () -> Unit, onUndo: () -> Unit, onCustomRecord: (Long) -> Unit) {
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

content = open('app/src/main/java/ru/otvykaniye/tracker/ui/screens/MainScreen.kt', encoding='utf8').read()
if old_str in content:
    with open('app/src/main/java/ru/otvykaniye/tracker/ui/screens/MainScreen.kt', 'w', encoding='utf8') as f:
        f.write(content.replace(old_str, new_str))
    print('Replaced successfully')
else:
    print('String not found in file!')
