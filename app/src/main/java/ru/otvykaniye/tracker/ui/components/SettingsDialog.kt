package ru.otvykaniye.tracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.otvykaniye.tracker.TracklessViewModel
import ru.otvykaniye.tracker.ui.theme.*

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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Настройки", color = TextPrimary) },
        text = { 
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text("Ежедневный лимит (шт):", color = TextDim)
                OutlinedTextField(
                    value = dailyLimit,
                    onValueChange = { dailyLimit = it },
                    textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary),
                    colors = TextFieldDefaults.colors(focusedContainerColor = BgDeep, unfocusedContainerColor = BgDeep)
                )

                Text("Содержание никотина (мг):", color = TextDim)
                OutlinedTextField(
                    value = nicotinePerPouch,
                    onValueChange = { nicotinePerPouch = it },
                    textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary),
                    colors = TextFieldDefaults.colors(focusedContainerColor = BgDeep, unfocusedContainerColor = BgDeep)
                )

                Text("Сколько уходило в день до отказа:", color = TextDim)
                OutlinedTextField(
                    value = baseline,
                    onValueChange = { baseline = it },
                    textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary),
                    colors = TextFieldDefaults.colors(focusedContainerColor = BgDeep, unfocusedContainerColor = BgDeep)
                )

                Text("Цена за пачку:", color = TextDim)
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary),
                    colors = TextFieldDefaults.colors(focusedContainerColor = BgDeep, unfocusedContainerColor = BgDeep)
                )

                Text("Штук в пачке:", color = TextDim)
                OutlinedTextField(
                    value = perPack,
                    onValueChange = { perPack = it },
                    textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary),
                    colors = TextFieldDefaults.colors(focusedContainerColor = BgDeep, unfocusedContainerColor = BgDeep)
                )

                Text("Желаемая покупка (Цель):", color = TextDim)
                OutlinedTextField(
                    value = wishlistTitle,
                    onValueChange = { wishlistTitle = it },
                    textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary),
                    colors = TextFieldDefaults.colors(focusedContainerColor = BgDeep, unfocusedContainerColor = BgDeep)
                )

                Text("Стоимость цели:", color = TextDim)
                OutlinedTextField(
                    value = wishlistCost,
                    onValueChange = { wishlistCost = it },
                    textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary),
                    colors = TextFieldDefaults.colors(focusedContainerColor = BgDeep, unfocusedContainerColor = BgDeep)
                )
            }
        },
        confirmButton = {
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = Emerald),
                onClick = {
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
                }
            ) { Text("Сохранить", color = BgDeep) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена", color = TextDim) }
        },
        containerColor = BgCard
    )
}