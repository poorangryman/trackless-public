package ru.otvykaniye.tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ru.otvykaniye.tracker.TracklessViewModel
import ru.otvykaniye.tracker.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(viewModel: TracklessViewModel, onDismiss: () -> Unit) {
    val state by viewModel.state.collectAsState()
    val activeProfile = state.profiles[state.activeKind] ?: return
    
    var dailyLimit by remember { mutableStateOf(activeProfile.dailyLimit.toString()) }
    var wishlistTitle by remember { mutableStateOf(activeProfile.wishlistTitle) }
    var wishlistCost by remember { mutableStateOf(activeProfile.wishlistCost.toString()) }
    var nicotinePerPouch by remember { mutableStateOf(activeProfile.nicotinePerPouch.toString()) }
    var baseline by remember { mutableStateOf(activeProfile.baseline.toString()) }
    var price by remember { mutableStateOf(activeProfile.price.toString()) }
    var perPack by remember { mutableStateOf(activeProfile.perPack.toString()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            containerColor = BgDeep,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Настройки", color = TextPrimary, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Rounded.Close, contentDescription = "Закрыть", tint = TextPrimary)
                        }
                    },
                    actions = {
                        TextButton(onClick = {
                            val updatedProfile = activeProfile.copy(
                                dailyLimit = dailyLimit.toIntOrNull() ?: activeProfile.dailyLimit,
                                wishlistTitle = wishlistTitle,
                                wishlistCost = wishlistCost.toDoubleOrNull() ?: activeProfile.wishlistCost,
                                nicotinePerPouch = nicotinePerPouch.toDoubleOrNull() ?: activeProfile.nicotinePerPouch,
                                baseline = baseline.toIntOrNull() ?: activeProfile.baseline,
                                price = price.toDoubleOrNull() ?: activeProfile.price,
                                perPack = perPack.toIntOrNull() ?: activeProfile.perPack
                            )
                            val newProfiles = state.profiles.toMutableMap()
                            newProfiles[state.activeKind] = updatedProfile
                            viewModel.saveSettings(state.copy(profiles = newProfiles))
                            onDismiss()
                        }) {
                            Text("Сохранить", color = Emerald, fontWeight = FontWeight.Bold)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BgCard)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                SettingsGroup("ТРЕКЕР И ЛИМИТЫ") {
                    SettingsField("Дневной лимит (шт)", dailyLimit) { dailyLimit = it }
                    if (state.activeKind == "snus") {
                        SettingsField("Никотин (мг/пак)", nicotinePerPouch, isDecimal = true) { nicotinePerPouch = it }
                    }
                }

                SettingsGroup("ЦЕЛЬ (ВИШЛИСТ)") {
                    SettingsField("Название цели", wishlistTitle, isText = true) { wishlistTitle = it }
                    SettingsField("Стоимость", wishlistCost) { wishlistCost = it }
                }

                SettingsGroup("РАСЧЕТ ЭКОНОМИИ") {
                    SettingsField("До отказа (в день)", baseline) { baseline = it }
                    SettingsField("Цена за пачку", price, isDecimal = true) { price = it }
                    SettingsField("Штук в пачке", perPack) { perPack = it }
                }
                
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, color = TextDim, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = Modifier.padding(start = 16.dp, bottom = 8.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(BgCard)
        ) {
            content()
        }
    }
}

@Composable
fun SettingsField(label: String, value: String, isDecimal: Boolean = false, isText: Boolean = false, onValueChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextPrimary, fontSize = 16.sp)
        Box(modifier = Modifier.width(120.dp)) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = Emerald, 
                    fontSize = 16.sp, 
                    textAlign = TextAlign.End,
                    fontWeight = FontWeight.Bold
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = if (isText) KeyboardType.Text else if (isDecimal) KeyboardType.Decimal else KeyboardType.Number),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Emerald
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
    HorizontalDivider(color = BgDeep, thickness = 1.dp, modifier = Modifier.padding(start = 16.dp))
}